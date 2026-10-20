package io.freemycar;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Free My Car Inspector: shows what is really inside an Android head unit (hardware, the real Android version, the
 * apps that came with it, known risky packages) and lets the person share the report. Read-only: it changes nothing.
 */
public final class MainActivity extends Activity {
    static final String VERSION = "0.1";

    /**
     * Known risky packages: {package, level, why}. "malware" = remove the unit from the network and report it;
     * "check" = a legitimate app that was abused, worth checking for updates. Sources in the comments; add only with one.
     */
    static final String[][] RISKY = {
        // Kaspersky Securelist, 21/08/2026, "android-head-unit-malware": DoFun head units. TWCore is the vendor's own
        // updater that delivered the dropper; the vendor reportedly fixed it.
        {"com.tw.core", "check", "TWCore: atualizador de sistema usado para instalar malware em centrais DoFun (Kaspersky, ago/2026)"},
        {"com.tw.jar1", "malware", "JarService: dropper do grupo MoYu/BADBOX (Kaspersky, ago/2026)"},
        // Same report: TV set-top boxes, MoYu Group.
        {"com.abc.nexus", "malware", "dropper AdmoyuService do grupo MoYu (Kaspersky, ago/2026)"},
    };

    private static final String[] ANDROID = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
        "", "", "5.0", "5.1", "6", "7.0", "7.1", "8.0", "8.1", "9", "10", "11", "12", "12L", "13", "14", "15", "16"};

    private TextView text;
    private String report = "";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF101418);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Free My Car · Inspetor " + VERSION);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        bar.addView(button("Compartilhar", v -> share()));
        bar.addView(button("Salvar", v -> save()));
        root.addView(bar);

        text = new TextView(this);
        text.setTextColor(0xFFE6E6E6);
        text.setTypeface(Typeface.MONOSPACE);
        text.setTextSize(15);
        text.setTextIsSelectable(true);
        text.setText("Analisando…");
        ScrollView scroll = new ScrollView(this);
        scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        new Thread(() -> {
            long t0 = System.currentTimeMillis();
            String r;
            try {
                r = build();
            } catch (RuntimeException e) {
                r = "Erro ao analisar: " + e;
            }
            android.util.Log.i("FreeMyCar", "report in " + (System.currentTimeMillis() - t0) + " ms");
            write("ultimo-relatorio.txt", r);    // also kept on disk: readable over USB or ADB without the screen
            final String done = r;
            runOnUiThread(() -> {
                report = done;
                text.setText(done);
            });
        }).start();
    }

    // MARK: - Report

    private String build() {
        StringBuilder b = new StringBuilder();
        b.append("Free My Car Inspetor ").append(VERSION).append(" · ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(new Date())).append('\n');
        b.append("Só leitura: nada foi alterado.\n\n");

        b.append("== Aparelho ==\n");
        line(b, "fabricante", Build.MANUFACTURER);
        line(b, "marca", Build.BRAND);
        line(b, "modelo", Build.MODEL);
        line(b, "device", Build.DEVICE);
        line(b, "produto", Build.PRODUCT);
        line(b, "placa", Build.BOARD);
        line(b, "hardware", Build.HARDWARE);
        line(b, "plataforma", prop("ro.board.platform"));
        if (Build.VERSION.SDK_INT >= 31) line(b, "soc", Build.SOC_MANUFACTURER + " " + Build.SOC_MODEL);
        line(b, "cpu", cpu() + " · " + Runtime.getRuntime().availableProcessors() + " núcleos");
        String fw = prop("ro.build.display.id");
        line(b, "firmware", fw);
        line(b, "fingerprint", Build.FINGERPRINT);

        int sdk = Build.VERSION.SDK_INT;
        String real = sdk < ANDROID.length ? ANDROID[sdk] : "16+";
        line(b, "android anunciado", Build.VERSION.RELEASE);
        line(b, "android real", real + " (API " + sdk + ")");
        if (!major(Build.VERSION.RELEASE).equals(major(real.replace("L", "")))) {
            b.append("  ⚠ O sistema diz Android ").append(Build.VERSION.RELEASE).append(", mas o núcleo é Android ")
                    .append(real).append(". Versão falsificada.\n");
        }
        if (sdk >= 23) line(b, "patch de segurança", Build.VERSION.SECURITY_PATCH);

        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mem);
        line(b, "ram real", gb(mem.totalMem));
        StatFs data = new StatFs(Environment.getDataDirectory().getPath());
        line(b, "armazenamento", gb(data.getTotalBytes()) + " (livre " + gb(data.getAvailableBytes()) + ")");
        DisplayMetrics dm = getResources().getDisplayMetrics();
        line(b, "tela", dm.widthPixels + "x" + dm.heightPixels + " · " + dm.densityDpi + " dpi");

        PackageManager pm = getPackageManager();
        ResolveInfo home = pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        line(b, "tela inicial", home == null ? "?" : home.activityInfo.packageName);

        java.util.Set<String> icons = new java.util.HashSet<>();   // one query, not one per app (slow on weak units)
        for (ResolveInfo r : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            icons.add(r.activityInfo.packageName);
        }
        List<PackageInfo> all = pm.getInstalledPackages(0);
        Collections.sort(all, (x, y) -> x.packageName.compareTo(y.packageName));

        b.append("\n== Alertas ==\n");
        int alerts = 0;
        for (PackageInfo p : all) {
            for (String[] r : RISKY) {
                if (!r[0].equals(p.packageName)) continue;
                alerts++;
                b.append(r[1].equals("malware") ? "  ⛔ MALWARE " : "  ⚠ VERIFICAR ").append(r[0]).append(": ").append(r[2]).append('\n');
            }
        }
        if (alerts == 0) b.append("  Nenhum pacote da lista de risco conhecida (").append(RISKY.length).append(" itens).\n");

        List<String> system = new ArrayList<>(), user = new ArrayList<>();
        for (PackageInfo p : all) {
            ApplicationInfo a = p.applicationInfo;
            if (a == null) continue;
            boolean sys = (a.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            StringBuilder f = new StringBuilder();
            if ((a.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) f.append(" atualizado");
            if (!a.enabled) f.append(" desligado");
            if (icons.contains(p.packageName)) f.append(" ícone");
            String label = String.valueOf(a.loadLabel(pm)).replace('\n', ' ').trim();
            (sys ? system : user).add(p.packageName + " | " + label + " | " + p.versionName + " |" + f);
        }
        b.append("\n== Apps do sistema (").append(system.size()).append(") ==\n");
        for (String s : system) b.append("  ").append(s).append('\n');
        b.append("\n== Apps instalados depois (").append(user.size()).append(") ==\n");
        for (String s : user) b.append("  ").append(s).append('\n');
        return b.toString();
    }

    private static void line(StringBuilder b, String k, String v) {
        b.append("  ").append(k).append(": ").append(v == null || v.isEmpty() ? "?" : v).append('\n');
    }

    /** "13" from "13", "8" from "8.1.0"; Android 5 to 7 keep the minor ("7.1"). */
    static String major(String version) {
        if (version == null) return "";
        String[] p = version.trim().split("\\.");
        if (p.length > 1 && (p[0].equals("5") || p[0].equals("7") || p[0].equals("8"))) return p[0] + "." + p[1];
        return p[0];
    }

    private static String prop(String key) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            return "";
        }
    }

    /** /proc/cpuinfo "Hardware" (most ARM units) or "model name". */
    private static String cpu() {
        try (BufferedReader r = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String l, model = "";
            while ((l = r.readLine()) != null) {
                if (l.startsWith("Hardware")) return l.substring(l.indexOf(':') + 1).trim();
                if (model.isEmpty() && l.startsWith("model name")) model = l.substring(l.indexOf(':') + 1).trim();
            }
            return model;
        } catch (Exception e) {
            return "";
        }
    }

    private static String gb(long bytes) {
        return String.format(Locale.ROOT, "%.1f GB", bytes / 1e9);
    }

    // MARK: - Actions

    private void share() {
        if (report.isEmpty()) return;
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report)
                .putExtra(Intent.EXTRA_SUBJECT, "Free My Car: " + Build.MANUFACTURER + " " + Build.MODEL);
        try {
            startActivity(Intent.createChooser(send, "Compartilhar relatório"));
        } catch (RuntimeException e) {
            toast("Nenhum app para compartilhar. Use Salvar.");
        }
    }

    /** To the app's folder on the internal storage (Android/data/io.freemycar/files): no permission needed. */
    private void save() {
        if (report.isEmpty()) return;
        File f = write("free-my-car-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date()) + ".txt", report);
        toast(f != null ? "Salvo em " + f.getPath() : "Não consegui salvar.");
    }

    private File write(String name, String content) {
        File dir = getExternalFilesDir(null);
        if (dir == null) return null;
        File f = new File(dir, name);
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(content.getBytes("UTF-8"));
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private Button button(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(click);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}

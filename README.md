<div align="center">

# 🔍 DashUnlock

**Descubra o que vem escondido na sua central multimídia Android.**

![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-sem_Gradle-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Só leitura](https://img.shields.io/badge/só_leitura-sem_root_·_sem_internet-101418?style=flat-square)
![Licença](https://img.shields.io/badge/licença-MIT-3ddc97?style=flat-square)
[![Release](https://img.shields.io/github/v/release/pedrolucas201/dashunlock?style=flat-square&label=APK)](../../releases)

[Português](#português) · [English](#english)

</div>

---

## Português

Central genérica de carro costuma vir com a **versão do Android falsificada**, menos memória do que o anúncio diz, apps de fábrica inúteis e, às vezes, **malware de fábrica** ([Kaspersky, ago/2026](https://securelist.com/android-head-unit-malware/121106/)).

Esta primeira versão é um **inspetor só de leitura**: não altera nada, não usa internet e não precisa de root nem de PC.

### O que ele mostra

| | |
|---|---|
| 🧠 **Hardware real** | placa, chip (SoC), plataforma, RAM e armazenamento |
| 🤖 **Android real** | o número anunciado **e** o que o núcleo do sistema realmente é |
| 📦 **Apps** | todos os de sistema e de fábrica |
| ⚠️ **Pacotes de risco** | alerta para malware conhecido, cada item com a fonte |
| 📄 **Relatório** | pra compartilhar ou salvar em `Android/data/io.dashunlock/files` |

### Como ele pega o Android falso

O fabricante consegue trocar o texto da versão (`Build.VERSION.RELEASE`), mas não o nível de API do núcleo (`Build.VERSION.SDK_INT`). O DashUnlock converte o nível de API na versão real e compara as duas. Se a central diz "Android 13" e o núcleo é 8.1, aparece o aviso.

### Como testar na sua central
1. Baixe o `DashUnlock.apk` em [Releases](../../releases) e copie para um pen drive (ou baixe pelo navegador da central).
2. Abra o arquivo no gerenciador de arquivos da central e instale (pode ser preciso permitir "fontes desconhecidas").
3. Abra **DashUnlock** e toque em **Compartilhar** ou **Salvar**.
4. Mande o relatório numa [issue](../../issues/new). Cada relatório ajuda a montar as listas do que é lixo e do que é essencial em cada modelo, para a próxima versão desligar o lixo com segurança.

O relatório mostra a lista de apps e o modelo da central. **Não inclui** nome, localização, contas nem arquivos.

### Privacidade
Uma única permissão: listar os apps instalados (`QUERY_ALL_PACKAGES`). Sem internet, sem acesso a arquivos, nada é modificado.

### Próximos passos
- [x] Inspetor só de leitura
- [ ] Listas por modelo do que é lixo e do que é essencial (vem dos relatórios de vocês)
- [ ] Desligar o lixo com segurança, com desfazer

---

## English

See what really ships inside your Android car head unit. A **read-only inspector**: real hardware and Android version (head units often fake it), every factory app, known risky packages (with sources), and a shareable report. No changes, no internet, no root.

**How the fake-version check works:** vendors can rewrite the version string (`Build.VERSION.RELEASE`) but not the kernel's API level (`Build.VERSION.SDK_INT`). DashUnlock maps the API level to the real version and flags a mismatch.

Share your report in an issue: it builds the per-platform lists for safe debloating next. The report holds the app list and the unit model, never your name, location, accounts or files.

---

## Build

```bash
bash build.sh          # aapt2 -> javac -> d8 -> zipalign -> apksigner, sem Gradle
java tests/MajorTest.java   # testa a detecção de versão falsa (não precisa de Android)
```

Precisa de JDK 17+ e do Android SDK (build-tools 34+ e uma plataforma android-33+). Roda em Linux, macOS ou Git Bash no Windows.

Inspirado no [Free My TV](https://github.com/simbasjungles/free-my-tv). Licença MIT.

<div align="center"><sub>Feito por <a href="https://github.com/pedrolucas201">Pedro Gomes</a></sub></div>

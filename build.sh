#!/usr/bin/env bash
# Builds FreeMyCar.apk without Gradle: aapt2 -> javac -> d8 -> zipalign -> apksigner.
# Needs a JDK (17+) and the Android SDK (build-tools 34+, a platform android-33 or newer). Runs on Linux, macOS and
# Windows (Git Bash).
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ]; then
  for d in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" "${LOCALAPPDATA:-}/Android/Sdk" /usr/lib/android-sdk; do
    if [ -d "$d/build-tools" ]; then SDK="$d"; break; fi
  done
fi
[ -n "$SDK" ] && [ -d "$SDK/build-tools" ] || { echo "Android SDK not found: set ANDROID_HOME."; exit 1; }
BT="$SDK/build-tools/$(ls "$SDK/build-tools" | sort -V | tail -1)"
AJ="$(ls -d "$SDK"/platforms/android-*/android.jar 2>/dev/null | sort -V | tail -1 || true)"
[ -f "${AJ:-}" ] || { echo "Android platform not found: sdkmanager \"platforms;android-34\""; exit 1; }
J="${JAVA_HOME:+$JAVA_HOME/bin/}"
command -v "${J}javac" >/dev/null 2>&1 || { echo "JDK not found: install JDK 17+ and set JAVA_HOME."; exit 1; }

# Windows: ";" between classpath entries and .bat wrappers for d8 and apksigner.
SEP=":"; BAT=""
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";"; BAT=".bat" ;; esac

rm -rf out && mkdir -p out/classes
"$BT/aapt2" link -o out/base.apk -I "$AJ" --manifest app/AndroidManifest.xml --min-sdk-version 21 --target-sdk-version 33
"${J}javac" -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$AJ$SEP$BT/core-lambda-stubs.jar" \
  -d out/classes $(find app/src -name '*.java')
"$BT/d8$BAT" --release --min-api 21 --lib "$AJ" --output out $(find out/classes -name '*.class')
(cd out && "${J}jar" -u -M -f base.apk classes.dex)
"$BT/zipalign" -f -p 4 out/base.apk out/aligned.apk

# Your own signing key, created once (updates need the same key: keep freemycar.keystore).
if [ ! -f freemycar.keystore ]; then
  "${J}keytool" -genkeypair -keystore freemycar.keystore -alias freemycar -keyalg RSA -keysize 2048 -validity 20000 \
    -storepass freemycar -keypass freemycar -dname "CN=Free My Car" >/dev/null 2>&1
fi
"$BT/apksigner$BAT" sign --ks freemycar.keystore --ks-pass pass:freemycar --key-pass pass:freemycar \
  --out FreeMyCar.apk out/aligned.apk
echo "Built FreeMyCar.apk ($(du -h FreeMyCar.apk | cut -f1)). Install: adb install -r FreeMyCar.apk"

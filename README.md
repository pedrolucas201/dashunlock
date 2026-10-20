# Free My Car

**PT-BR** · Descubra o que vem escondido na sua central multimídia Android.

Centrais genéricas costumam vir com a versão do Android falsificada, menos memória do que o anúncio diz, apps de
fábrica inúteis e, às vezes, malware de fábrica ([Kaspersky, ago/2026](https://securelist.com/android-head-unit-malware/121106/)).

Esta primeira versão é um **inspetor só de leitura**: não altera nada, não usa internet e não precisa de root nem de PC.

- O hardware real (chip, RAM, armazenamento) e o **Android real** por trás do número anunciado.
- Todos os apps de sistema e de fábrica.
- Alerta para pacotes de risco conhecidos, cada um com fonte.
- Um relatório para compartilhar (ou salvar em `Android/data/io.freemycar/files`).

### Como testar na sua central
1. Baixe o `FreeMyCar.apk` em [Releases](../../releases) e copie para um pen drive (ou baixe pelo navegador da central).
2. Abra o arquivo no gerenciador de arquivos da central e instale (pode ser preciso permitir "fontes desconhecidas").
3. Abra **Free My Car** e toque em **Compartilhar** ou **Salvar**.
4. Mande o relatório numa [issue](../../issues/new). Cada relatório ajuda a montar as listas do que é lixo e do que é
   essencial em cada modelo, para a próxima versão desligar o lixo com segurança.

O relatório mostra a lista de apps e o modelo da central. Não inclui nome, localização, contas nem arquivos.

---

**EN** · See what really ships inside your Android car head unit. A read-only inspector: real hardware and Android
version (head units often fake it), every factory app, known risky packages (with sources), and a shareable report. No
changes, no internet, no root. Share your report in an issue: it builds the per-platform lists for safe debloating next.

## Build
`bash build.sh` (JDK 17+, Android SDK build-tools 34+ and a platform android-33+; Linux, macOS or Git Bash on Windows).
`java tests/MajorTest.java` checks the fake-version detection.

Inspired by [Free My TV](https://github.com/simbasjungles/free-my-tv). MIT license.

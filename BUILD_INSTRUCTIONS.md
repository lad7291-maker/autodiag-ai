# 📱 Инструкция по сборке APK

## Вариант 1: Android Studio (рекомендуется)

### 1. Установка Android Studio
```
1. Скачайте: https://developer.android.com/studio
2. Установите с дефолтными настройками
3. При первом запуске выберите "Standard" setup
```

### 2. Открытие проекта
```
File → Open → Выберите папку c:\AutoDiagAI
```

### 3. Синхронизация
Подождите 5-10 минут пока Gradle скачает зависимости.

### 4. Сборка Debug APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK будет создан в:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Сборка Release APK
```
Build → Generate Signed Bundle or APK
→ Выберите APK
→ Создайте новый keystore (или используйте существующий)
→ Готово!
```

---

## Вариант 2: Командная строка (для опытных)

### Требования:
- Java JDK 17 (уже установлен в `c:\AutoDiagAI\tools\jdk17`)
- Android SDK

### 1. Установка Android SDK
```powershell
# Скачайте Command Line Tools
# https://developer.android.com/studio#command-tools

# Распакуйте в c:\Android\Sdk
# Установите переменные окружения:
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\Sdk", "User")
```

### 2. Установка компонентов SDK
```powershell
cd C:\Android\Sdk\cmdline-tools\latest\bin

# Установка platform tools
sdkmanager.bat "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 3. Сборка
```powershell
cd c:\AutoDiagAI

# Установите переменные
$env:JAVA_HOME = "C:\AutoDiagAI\tools\jdk17\jdk-17"
$env:ANDROID_HOME = "C:\Android\Sdk"

# Сборка Debug
.\gradlew.bat assembleDebug

# Сборка Release (нужен keystore)
.\gradlew.bat assembleRelease
```

---

## Готовые скрипты

### Скрипт сборки Debug (PowerShell)
```powershell
# save as build-debug.ps1
$env:JAVA_HOME = "C:\AutoDiagAI\tools\jdk17\jdk-17"
$env:ANDROID_HOME = "C:\Android\Sdk"  # Измените путь
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

cd C:\AutoDiagAI
.\gradlew.bat clean assembleDebug --no-daemon

Write-Host "APK создан: app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Green
```

### Скрипт сборки Release (PowerShell)
```powershell
# save as build-release.ps1
$env:JAVA_HOME = "C:\AutoDiagAI\tools\jdk17\jdk-17"
$env:ANDROID_HOME = "C:\Android\Sdk"  # Измените путь

# Создание keystore (один раз)
# keytool -genkey -v -keystore autodiag.keystore -alias autodiag -keyalg RSA -keysize 2048 -validity 10000

cd C:\AutoDiagAI
.\gradlew.bat clean assembleRelease --no-daemon

Write-Host "APK создан: app/build/outputs/apk/release/app-release.apk" -ForegroundColor Green
```

---

## Устранение ошибок

### Ошибка: "SDK location not found"
**Решение:** Создайте файл `local.properties`:
```
sdk.dir=C\:\\Android\\Sdk
```

### Ошибка: "Could not find tools.jar"
**Решение:** Проверьте JAVA_HOME:
```powershell
$env:JAVA_HOME = "C:\AutoDiagAI\tools\jdk17\jdk-17"
```

### Ошибка: "Gradle sync failed"
**Решение:**
```powershell
cd c:\AutoDiagAI
.\gradlew.bat --stop
Remove-Item -Recurse -Force .gradle
.\gradlew.bat clean
```

---

## Установка APK на устройство

### Через ADB:
```powershell
# Включите отладку по USB на телефоне
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Через проводник:
1. Скопируйте APK на телефон
2. Откройте файл на телефоне
3. Разрешите установку из неизвестных источников

---

*Генерировано автоматически для проекта AutoDiagAI*

# Setup Secrets / Настройка секретов

## GitHub Secrets (для CI/CD)

Добавьте следующие secrets в настройках репозитория GitHub:
Settings → Secrets and variables → Actions → New repository secret

### Обязательные secrets:

| Secret Name | Description | How to obtain |
|-------------|-------------|---------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file | `base64 -w 0 your-keystore.jks` |
| `KEYSTORE_PASSWORD` | Keystore password | Password you used when creating keystore |
| `KEY_ALIAS` | Key alias in keystore | Alias used when creating key |
| `KEY_PASSWORD` | Key password | Password for the specific key |

### Создание keystore:

```bash
# Generate keystore
keytool -genkey -v \
  -keystore autodiag-release.keystore \
  -alias autodiag \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Encode for GitHub secret
base64 -w 0 autodiag-release.keystore
```

---

## Firebase Setup

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Name it: `autodiag-ai` (or your preferred name)
4. Enable Google Analytics (optional)
5. Create project

### 2. Add Android App

1. In Firebase Console, click the Android icon to add app
2. Package name: `com.autodiag.ai`
3. Download `google-services.json`
4. **Replace** the placeholder file at `app/google-services.json`

### 3. Enable Services

In Firebase Console:
- **Crashlytics**: Enable in Project settings → Integrations
- **Analytics**: Enabled by default

---

## Google Play Console Setup (для Fastlane)

### 1. Create Service Account

1. Google Play Console → Settings → API access
2. Link to Google Cloud project
3. Create service account
4. Grant permissions: Release manager
5. Download JSON key file

### 2. Configure Fastlane

```bash
# Store service account key
mkdir -p ~/.fastlane
cp service-account-key.json ~/.fastlane/google-play-key.json

# Update Appfile
fastlane init
```

### 3. Update Appfile

Edit `fastlane/Appfile`:
```ruby
json_key_file("~/.fastlane/google-play-key.json")
package_name("com.autodiag.ai")
```

---

## Локальная сборка

### Без подписи (debug):
```bash
./gradlew assembleDebug
```

### С подписью (release):
```bash
# Установите переменные окружения
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=autodiag
export KEY_PASSWORD=your_key_password

# Используйте скрипт
./scripts/build-release.sh /path/to/keystore.jks autodiag
```

---

## Проверка перед релизом

### 1. Запуск всех проверок:
```bash
./gradlew clean detekt testDebugUnitTest jacocoTestReport assembleRelease
```

### 2. Проверка APK:
```bash
# Проверка подписи
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Анализ размера
./gradlew app:analyzeDebugBundle
```

---

## Безопасность

⚠️ **Важно:**
- Никогда не коммитьте `google-services.json` с реальными данными
- Храните keystore в безопасном месте (не в репозитории)
- Используйте разные ключи для debug и release
- Регулярно ротируйте secrets

---

## Troubleshooting

### Ошибка: "Could not find google-services.json"
```bash
# Скачайте из Firebase Console и поместите в app/
curl -o app/google-services.json [YOUR_DOWNLOAD_URL]
```

### Ошибка: "Keystore file not found"
```bash
# Убедитесь что keystore.jks в папке app/ (для CI)
# Или используйте полный путь (для локальной сборки)
```

### Ошибка: "Invalid signature"
```bash
# Проверьте что все переменные окружения установлены
echo $KEYSTORE_PASSWORD
echo $KEY_ALIAS
echo $KEY_PASSWORD
```

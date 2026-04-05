# 🚀 Настройка GitHub Actions

## Быстрый старт (3 шага)

### 1. Создай репозиторий на GitHub
Открой в браузере: https://github.com/new

- **Repository name**: `autodiag-ai`
- **Visibility**: Public или Private
- **❌ НЕ** инициализируй README

Нажми **Create repository**

---

### 2. Запушь код

**Вариант A: Через PowerShell скрипт**
```powershell
cd c:\AutoDiagAI
.\push-to-github.ps1 -Username "ТВОЙ_GITHUB_USERNAME"
```

**Вариант B: Вручную**
```powershell
cd c:\AutoDiagAI
git remote add origin https://github.com/ТВОЙ_USERNAME/autodiag-ai.git
git branch -M main
git push -u origin main
```

---

### 3. Получи APK

1. Открой репозиторий: `https://github.com/ТВОЙ_USERNAME/autodiag-ai`
2. Перейди во вкладку **Actions**
3. Кликни на workflow **CI**
4. Дождись зеленой галочки ✅
5. Кликни на последний успешный запуск
6. В разделе **Artifacts** скачай `debug-apk`

---

## 📁 Структура артефактов

После успешной сборки доступны:

| Артефакт | Описание |
|----------|----------|
| `debug-apk` | Отладочная версия (рекомендуется для теста) |
| `release-apk-unsigned` | Релизная версия (без подписи) |
| `coverage-report` | Отчет о покрытии тестами |
| `test-results` | Результаты unit тестов |

---

## ⚙️ Настройка подписи (для Release)

Для подписанного release APK нужно добавить secrets:

1. Создай keystore:
```powershell
keytool -genkey -v -keystore autodiag.keystore -alias autodiag -keyalg RSA -keysize 2048 -validity 10000
```

2. Конвертируй в base64:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("autodiag.keystore")) | Set-Clipboard
```

3. Добавь в GitHub Secrets:
   - Открой репозиторий → Settings → Secrets → Actions
   - Добавь:
     - `KEYSTORE_BASE64` - скопированное значение
     - `KEYSTORE_PASSWORD` - пароль от keystore
     - `KEY_ALIAS` - `autodiag`
     - `KEY_PASSWORD` - пароль ключа

4. Запусти workflow заново

---

## 🔧 Устранение проблем

### Workflow не запускается
Проверь:
- Код запушен на GitHub
- Файл `.github/workflows/ci.yml` существует
- GitHub Actions включены в репозитории (Settings → Actions)

### Ошибка сборки
Проверь логи во вкладке Actions → выбери workflow → красный крестик

### Нет артефактов
Артефакты появляются только после успешной сборки. Проверь статус workflow.

---

## 📱 Установка APK

1. Скачай `debug-apk.zip`
2. Распакуй
3. Передай `app-debug.apk` на телефон
4. На телефоне: разреши установку из неизвестных источников
5. Установи!

---

*Генерировано для проекта AutoDiagAI*

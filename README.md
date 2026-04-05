# 🚗 AutoDiag AI - Android приложение для диагностики авто

Интеллектуальное Android-приложение для диагностики русских автомобилей через OBD2 с AI-анализом.

## 📱 Возможности

### 🔧 Диагностика
- **OBD2 сканирование** - подключение через Bluetooth
- **Чтение ошибок** - DTC коды с расшифровкой
- **Сброс ошибок** - очистка Check Engine
- **Параметры в реальном времени** - RPM, температура, нагрузка и др.

### 🤖 AI Анализ
- **Оценка состояния двигателя** (0-100%)
- **Выявление проблем** на основе параметров
- **Рекомендации по ремонту** - что и где заменить
- **Советы по эксплуатации** - персонализированные под авто

### 🚘 Поддержка русских авто
- **ВАЗ/Лада** - все модели
- **УАЗ** - Патриот, Хантер, Буханка
- **ГАЗ** - Газель, Соболь, Волга
- **КамАЗ** - грузовики
- **ПАЗ** - автобусы

### 📊 Функции
- История диагностик
- Графики параметров
- База 10,000+ ошибок
- Офлайн работа
- Темная тема

## 🏗️ Архитектура

```
app/src/main/java/com/autodiag/ai/
├── data/
│   ├── local/
│   │   ├── database/        # Room база данных
│   │   └── preferences/     # DataStore настройки
│   ├── model/               # Модели данных
│   └── repository/          # Репозитории
├── di/                      # Dependency Injection (Koin)
├── services/                # OBD2 сервисы
├── ui/
│   ├── navigation/          # Навигация
│   ├── screens/             # Экраны
│   ├── theme/               # Тема приложения
│   └── viewmodel/           # ViewModels
└── utils/                   # Утилиты
    └── ai/                  # AI модуль
```

## 🚀 Установка

### Требования
- Android 8.0+ (API 26+)
- Bluetooth адаптер
- OBD2 ELM327 адаптер

### Сборка

```bash
# Клонирование
git clone https://github.com/yourusername/autodiag-ai-android.git
cd autodiag-ai-android

# Сборка
./gradlew assembleDebug

# Установка
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Использование

### 1. Подключение OBD2
1. Вставьте OBD2 адаптер в диагностический разъем
2. Включите зажигание
3. В приложении нажмите "OBD2" → "Подключиться"
4. Выберите свой адаптер из списка

### 2. Диагностика
1. На главном экране нажмите "Диагностика"
2. Приложение просканирует все системы
3. Получите оценку состояния и рекомендации

### 3. Просмотр ошибок
- Коды ошибок с расшифровкой на русском
- Уровень критичности
- Рекомендации по ремонту
- Стоимость запчастей

### 4. Мониторинг
- Параметры в реальном времени
- Графики RPM, температуры
- Сохранение данных

## 🔧 Технологии

- **Kotlin** - язык программирования
- **Jetpack Compose** - UI фреймворк
- **Room** - локальная база данных
- **Koin** - dependency injection
- **TensorFlow Lite** - AI модели
- **OBD Java API** - работа с OBD2

## 📊 AI Модуль

### Анализ параметров
```kotlin
val analysis = diagnosisAI.analyzeEngineParameters(params, vehicle)
// healthScore: 87.5
// issues: ["Повышенная температура"]
// tips: ["Проверьте уровень антифриза"]
```

### Анализ DTC кодов
```kotlin
val dtcAnalysis = diagnosisAI.analyzeDtcCodes(codes, vehicle)
// totalCodes: 3
// criticalCount: 1
// estimatedCost: 15000 RUB
// canDrive: false
```

## 🗄️ База данных

### DTC коды
- 10,000+ кодов ошибок
- Расшифровка на русском
- Причины и решения
- Стоимость ремонта

### Неисправности
- Типичные проблемы по маркам
- Симптомы и диагностика
- Запчасти и цены
- Сложность ремонта

## 🎨 Скриншоты

| Главный экран | Диагностика | Параметры |
|--------------|-------------|-----------|
| ![Home](screenshots/home.png) | ![Diagnosis](screenshots/diagnosis.png) | ![Live](screenshots/live.png) |

## 🔒 Разрешения

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте ветку (`git checkout -b feature/amazing`)
3. Commit изменения (`git commit -m 'Add amazing'`)
4. Push в ветку (`git push origin feature/amazing`)
5. Откройте Pull Request

## 📄 Лицензия

MIT License - см. [LICENSE](LICENSE)

## ⚠️ Отказ от ответственности

Приложение создано для помощи в диагностике. Для сложных ремонтов обращайтесь к специалистам.

---

**Сделано с ❤️ для русских автомобилистов**

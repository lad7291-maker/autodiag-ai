# Скрипт для пуша на GitHub
# Использование: .\push-to-github.ps1 -Username "your-username" -RepoName "autodiag-ai"

param(
    [Parameter(Mandatory=$true)]
    [string]$Username,
    
    [string]$RepoName = "autodiag-ai"
)

Write-Host "Настройка GitHub remote..." -ForegroundColor Cyan

# Добавляем remote
git remote remove origin 2>$null
git remote add origin "https://github.com/$Username/$RepoName.git"

# Переименуем ветку в main
git branch -M main

Write-Host "Отправка кода на GitHub..." -ForegroundColor Cyan
Write-Host "URL: https://github.com/$Username/$RepoName" -ForegroundColor Yellow

try {
    git push -u origin main
    Write-Host "✅ Успешно отправлено!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Следующие шаги:" -ForegroundColor Cyan
    Write-Host "1. Откройте: https://github.com/$Username/$RepoName" -ForegroundColor White
    Write-Host "2. Перейдите во вкладку 'Actions'" -ForegroundColor White
    Write-Host "3. Дождитесь завершения workflow 'CI'" -ForegroundColor White
    Write-Host "4. Скачайте APK из артефактов" -ForegroundColor White
} catch {
    Write-Host "❌ Ошибка отправки. Убедитесь что:" -ForegroundColor Red
    Write-Host "   - Репозиторий создан на GitHub" -ForegroundColor Yellow
    Write-Host "   - Имя пользователя и репозиторий верны" -ForegroundColor Yellow
    Write-Host "   - У вас есть доступ к интернету" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Создайте репозиторий вручную:" -ForegroundColor Cyan
    Write-Host "https://github.com/new?name=$RepoName" -ForegroundColor Blue
}

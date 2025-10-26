# Script cai dat Git Hooks# Script cài đặt Git Hooks

# Su dung: .\install-hooks.ps1 [hook-type]# Sử dụng: .\install-hooks.ps1 [hook-type]

# Hook types: simple, smart, full# Hook types: simple, smart, full



param(param(

    [Parameter(Mandatory=$false)]    [Parameter(Mandatory=$false)]

    [ValidateSet('simple', 'smart', 'full')]    [ValidateSet('simple', 'smart', 'full')]

    [string]$HookType = 'simple'    [string]$HookType = 'simple'

))



$ErrorActionPreference = "Stop"$ErrorActionPreference = "Stop"



Write-Host "========================================" -ForegroundColor CyanWrite-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan

Write-Host "     Git Hooks Installation Script     " -ForegroundColor CyanWrite-Host "║     Git Hooks Installation Script     ║" -ForegroundColor Cyan

Write-Host "========================================" -ForegroundColor CyanWrite-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host ""Write-Host ""



# Xac dinh duong dan# Xác định đường dẫn

$rootDir = $PSScriptRoot$rootDir = $PSScriptRoot

$hooksSourceDir = Join-Path $rootDir ".githooks"$hooksSourceDir = Join-Path $rootDir ".githooks"

$hooksTargetDir = Join-Path $rootDir ".git\hooks"$hooksTargetDir = Join-Path $rootDir ".git\hooks"



# Kiem tra .git folder# Kiểm tra .git folder

if (-not (Test-Path (Join-Path $rootDir ".git"))) {if (-not (Test-Path (Join-Path $rootDir ".git"))) {

    Write-Host "ERROR: Thu muc .git khong ton tai!" -ForegroundColor Red    Write-Host "✗ ERROR: Thư mục .git không tồn tại!" -ForegroundColor Red

    Write-Host "  Dam bao rang ban dang o trong git repository." -ForegroundColor Yellow    Write-Host "  Đảm bảo rằng bạn đang ở trong git repository." -ForegroundColor Yellow

    exit 1    exit 1

}}



# Kiem tra hooks source# Kiểm tra hooks source

if (-not (Test-Path $hooksSourceDir)) {if (-not (Test-Path $hooksSourceDir)) {

    Write-Host "ERROR: Thu muc .githooks khong ton tai!" -ForegroundColor Red    Write-Host "✗ ERROR: Thư mục .githooks không tồn tại!" -ForegroundColor Red

    exit 1    exit 1

}}



# Xac dinh file hook nguon# Xác định file hook nguồn

$sourceHook = switch ($HookType) {$sourceHook = switch ($HookType) {

    'simple' { 'pre-push' }    'simple' { 'pre-push' }

    'smart' { 'pre-push-smart' }    'smart' { 'pre-push-smart' }

    'full' { 'pre-push-full' }    'full' { 'pre-push-full' }

}}



$sourceHookPath = Join-Path $hooksSourceDir $sourceHook$sourceHookPath = Join-Path $hooksSourceDir $sourceHook

$targetHookPath = Join-Path $hooksTargetDir "pre-push"$targetHookPath = Join-Path $hooksTargetDir "pre-push"



# Kiem tra file nguon# Kiểm tra file nguồn

if (-not (Test-Path $sourceHookPath)) {if (-not (Test-Path $sourceHookPath)) {

    Write-Host "ERROR: File hook '$sourceHook' khong ton tai!" -ForegroundColor Red    Write-Host "✗ ERROR: File hook '$sourceHook' không tồn tại!" -ForegroundColor Red

    exit 1    exit 1

}}



Write-Host "Hook type: $HookType" -ForegroundColor WhiteWrite-Host "Hook type: $HookType" -ForegroundColor White

Write-Host "Source: .githooks\$sourceHook" -ForegroundColor WhiteWrite-Host "Source: .githooks\$sourceHook" -ForegroundColor White

Write-Host "Target: .git\hooks\pre-push" -ForegroundColor WhiteWrite-Host "Target: .git\hooks\pre-push" -ForegroundColor White

Write-Host ""Write-Host ""



# Backup hook cu neu co# Backup hook cũ nếu có

if (Test-Path $targetHookPath) {if (Test-Path $targetHookPath) {

    $backupPath = "$targetHookPath.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"    $backupPath = "$targetHookPath.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

    Write-Host "Backing up existing hook to:" -ForegroundColor Yellow    Write-Host "ℹ Backing up existing hook to:" -ForegroundColor Yellow

    Write-Host "  $backupPath" -ForegroundColor Gray    Write-Host "  $backupPath" -ForegroundColor Gray

    Copy-Item $targetHookPath $backupPath    Copy-Item $targetHookPath $backupPath

}}



# Copy hook moi# Copy hook mới

try {try {

    Copy-Item $sourceHookPath $targetHookPath -Force    Copy-Item $sourceHookPath $targetHookPath -Force

    Write-Host ""    Write-Host ""

    Write-Host "Git hook da duoc cai dat thanh cong!" -ForegroundColor Green    Write-Host "✓ Git hook đã được cài đặt thành công!" -ForegroundColor Green

    Write-Host ""    Write-Host ""

        

    # Hien thi thong tin ve hook type    # Hiển thị thông tin về hook type

    Write-Host "Hook behavior:" -ForegroundColor Cyan    Write-Host "Hook behavior:" -ForegroundColor Cyan

    switch ($HookType) {    switch ($HookType) {

        'simple' {        'simple' {

            Write-Host "  - Build tat ca services truoc khi push" -ForegroundColor White            Write-Host "  • Build tất cả services trước khi push" -ForegroundColor White

            Write-Host "  - Nhanh va don gian" -ForegroundColor White            Write-Host "  • Nhanh và đơn giản" -ForegroundColor White

        }        }

        'smart' {        'smart' {

            Write-Host "  - Chi build cac services da thay doi" -ForegroundColor White            Write-Host "  • Chỉ build các services đã thay đổi" -ForegroundColor White

            Write-Host "  - Tiet kiem thoi gian" -ForegroundColor White            Write-Host "  • Tiết kiệm thời gian" -ForegroundColor White

            Write-Host "  - Phu hop cho development" -ForegroundColor White            Write-Host "  • Phù hợp cho development" -ForegroundColor White

        }        }

        'full' {        'full' {

            Write-Host "  - Build tat ca services" -ForegroundColor White            Write-Host "  • Build tất cả services" -ForegroundColor White

            Write-Host "  - Chay tat ca tests" -ForegroundColor White            Write-Host "  • Chạy tất cả tests" -ForegroundColor White

            Write-Host "  - Mat nhieu thoi gian nhat" -ForegroundColor White            Write-Host "  • Mất nhiều thời gian nhất" -ForegroundColor White

            Write-Host "  - Khuyen nghi cho production" -ForegroundColor White            Write-Host "  • Khuyến nghị cho production" -ForegroundColor White

        }        }

    }    }

        

    Write-Host ""    Write-Host ""

    Write-Host "Note:" -ForegroundColor Yellow    Write-Host "Note:" -ForegroundColor Yellow

    Write-Host "  - Hook se tu dong chay moi khi ban push" -ForegroundColor White    Write-Host "  • Hook sẽ tự động chạy mỗi khi bạn push" -ForegroundColor White

    Write-Host "  - De bo qua hook: git push --no-verify" -ForegroundColor White    Write-Host "  • Để bỏ qua hook: git push --no-verify" -ForegroundColor White

    Write-Host "  - De doi hook type: .\install-hooks.ps1 <type>" -ForegroundColor White    Write-Host "  • Để đổi hook type: .\install-hooks.ps1 <type>" -ForegroundColor White

    Write-Host ""    Write-Host ""

}}

catch {catch {

    Write-Host ""    Write-Host ""

    Write-Host "ERROR: Khong the cai dat hook!" -ForegroundColor Red    Write-Host "✗ ERROR: Không thể cài đặt hook!" -ForegroundColor Red

    Write-Host "  $_" -ForegroundColor Red    Write-Host "  $_" -ForegroundColor Red

    exit 1    exit 1

}}


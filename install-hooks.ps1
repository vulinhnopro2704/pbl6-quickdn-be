# Script cài đặt Git Hooks
# Sử dụng: .\install-hooks.ps1 [hook-type]
# Hook types: simple, smart, full

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('simple', 'smart', 'full')]
    [string]$HookType = 'simple'
)

$ErrorActionPreference = "Stop"

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     Git Hooks Installation Script     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Xác định đường dẫn
$rootDir = $PSScriptRoot
$hooksSourceDir = Join-Path $rootDir ".githooks"
$hooksTargetDir = Join-Path $rootDir ".git\hooks"

# Kiểm tra .git folder
if (-not (Test-Path (Join-Path $rootDir ".git"))) {
    Write-Host "✗ ERROR: Thư mục .git không tồn tại!" -ForegroundColor Red
    Write-Host "  Đảm bảo rằng bạn đang ở trong git repository." -ForegroundColor Yellow
    exit 1
}

# Kiểm tra hooks source
if (-not (Test-Path $hooksSourceDir)) {
    Write-Host "✗ ERROR: Thư mục .githooks không tồn tại!" -ForegroundColor Red
    exit 1
}

# Xác định file hook nguồn
$sourceHook = switch ($HookType) {
    'simple' { 'pre-push' }
    'smart' { 'pre-push-smart' }
    'full' { 'pre-push-full' }
}

$sourceHookPath = Join-Path $hooksSourceDir $sourceHook
$targetHookPath = Join-Path $hooksTargetDir "pre-push"

# Kiểm tra file nguồn
if (-not (Test-Path $sourceHookPath)) {
    Write-Host "✗ ERROR: File hook '$sourceHook' không tồn tại!" -ForegroundColor Red
    exit 1
}

Write-Host "Hook type: $HookType" -ForegroundColor White
Write-Host "Source: .githooks\$sourceHook" -ForegroundColor White
Write-Host "Target: .git\hooks\pre-push" -ForegroundColor White
Write-Host ""

# Backup hook cũ nếu có
if (Test-Path $targetHookPath) {
    $backupPath = "$targetHookPath.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Write-Host "ℹ Backing up existing hook to:" -ForegroundColor Yellow
    Write-Host "  $backupPath" -ForegroundColor Gray
    Copy-Item $targetHookPath $backupPath
}

# Copy hook mới
try {
    Copy-Item $sourceHookPath $targetHookPath -Force
    Write-Host ""
    Write-Host "✓ Git hook đã được cài đặt thành công!" -ForegroundColor Green
    Write-Host ""
    
    # Hiển thị thông tin về hook type
    Write-Host "Hook behavior:" -ForegroundColor Cyan
    switch ($HookType) {
        'simple' {
            Write-Host "  • Build tất cả services trước khi push" -ForegroundColor White
            Write-Host "  • Nhanh và đơn giản" -ForegroundColor White
        }
        'smart' {
            Write-Host "  • Chỉ build các services đã thay đổi" -ForegroundColor White
            Write-Host "  • Tiết kiệm thời gian" -ForegroundColor White
            Write-Host "  • Phù hợp cho development" -ForegroundColor White
        }
        'full' {
            Write-Host "  • Build tất cả services" -ForegroundColor White
            Write-Host "  • Chạy tất cả tests" -ForegroundColor White
            Write-Host "  • Mất nhiều thời gian nhất" -ForegroundColor White
            Write-Host "  • Khuyến nghị cho production" -ForegroundColor White
        }
    }
    
    Write-Host ""
    Write-Host "Note:" -ForegroundColor Yellow
    Write-Host "  • Hook sẽ tự động chạy mỗi khi bạn push" -ForegroundColor White
    Write-Host "  • Để bỏ qua hook: git push --no-verify" -ForegroundColor White
    Write-Host "  • Để đổi hook type: .\install-hooks.ps1 <type>" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host ""
    Write-Host "✗ ERROR: Không thể cài đặt hook!" -ForegroundColor Red
    Write-Host "  $_" -ForegroundColor Red
    exit 1
}

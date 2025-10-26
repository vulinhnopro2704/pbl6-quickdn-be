# Script quản lý build các service trong PBL6-QUICKDN-BACKEND
# Sử dụng: .\manage.ps1 <command> [service-name] [-DetailedLog]
# Commands: build, clean, test
# Service names: gateway, auth-service, order-service, goongmap-service, all

param(
    [Parameter(Mandatory=$true, Position=0)]
    [ValidateSet('build', 'clean', 'test', 'bootJar')]
    [string]$Command,
    
    [Parameter(Mandatory=$false, Position=1)]
    [ValidateSet('gateway', 'auth-service', 'order-service', 'goongmap-service', 'all')]
    [string]$Service = 'all',
    
    [Parameter(Mandatory=$false)]
    [switch]$DetailedLog,
    
    [Parameter(Mandatory=$false)]
    [switch]$Quiet
)

# Danh sách các service
$services = @('gateway', 'auth-service', 'order-service', 'goongmap-service')

# Hàm hiển thị thông báo màu
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = 'White'
    )
    Write-Host $Message -ForegroundColor $Color
}

# Hàm thực thi gradle command cho một service
function Invoke-GradleCommand {
    param(
        [string]$ServiceName,
        [string]$GradleCommand
    )
    
    Write-ColorOutput "`n========================================" -Color Cyan
    Write-ColorOutput "  $GradleCommand service: $ServiceName" -Color Cyan
    Write-ColorOutput "========================================`n" -Color Cyan
    
    $servicePath = Join-Path $PSScriptRoot $ServiceName
    
    if (-not (Test-Path $servicePath)) {
        Write-ColorOutput "ERROR: Service '$ServiceName' không tồn tại!" -Color Red
        return $false
    }
    
    $gradlewPath = Join-Path $servicePath "gradlew.bat"
    
    if (-not (Test-Path $gradlewPath)) {
        Write-ColorOutput "ERROR: Không tìm thấy gradlew.bat trong '$ServiceName'!" -Color Red
        return $false
    }
    
    # Chuyển đến thư mục service
    Push-Location $servicePath
    
    try {
        # Xây dựng các tham số Gradle
        $gradleArgs = @($GradleCommand)
        
        # Thêm các flags tùy theo mode
        if ($DetailedLog) {
            $gradleArgs += "--info"
            $gradleArgs += "--stacktrace"
            Write-ColorOutput "Mode: Detailed logging bat (--info --stacktrace)" -Color Yellow
        }
        elseif ($Quiet) {
            $gradleArgs += "--quiet"
            Write-ColorOutput "Mode: Quiet mode bat (--quiet)" -Color Yellow
        }
        
        Write-ColorOutput "Đang thực thi: .\gradlew.bat $($gradleArgs -join ' ')" -Color Yellow
        
        # Thực thi Gradle command
        if ($DetailedLog) {
            # Hiển thị full output khi DetailedLog được bật
            & ".\gradlew.bat" @gradleArgs
        }
        else {
            # Output bình thường
            & ".\gradlew.bat" @gradleArgs
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "`n✓ Thành công: $GradleCommand '$ServiceName'" -Color Green
            return $true
        } else {
            Write-ColorOutput "`n✗ Thất bại: $GradleCommand '$ServiceName' (Exit code: $LASTEXITCODE)" -Color Red
            return $false
        }
    }
    catch {
        Write-ColorOutput "`n✗ Lỗi khi thực thi: $_" -Color Red
        return $false
    }
    finally {
        # Quay lại thư mục gốc
        Pop-Location
    }
}

# Main execution
Write-ColorOutput "╔════════════════════════════════════════╗" -Color Magenta
Write-ColorOutput "║  PBL6 QUICKDN Backend Build Manager   ║" -Color Magenta
Write-ColorOutput "╚════════════════════════════════════════╝" -Color Magenta

$startTime = Get-Date

# Xác định danh sách service cần xử lý
$targetServices = @()
if ($Service -eq 'all') {
    $targetServices = $services
    Write-ColorOutput "`nChế độ: Build tất cả các services" -Color White
} else {
    $targetServices = @($Service)
    Write-ColorOutput "`nChế độ: Build service đơn lẻ" -Color White
}

Write-ColorOutput "Command: $Command" -Color White
Write-ColorOutput "Services: $($targetServices -join ', ')" -Color White

# Hiển thị logging mode
if ($DetailedLog) {
    Write-ColorOutput "Logging: Chi tiết (--info --stacktrace)" -Color Cyan
}
elseif ($Quiet) {
    Write-ColorOutput "Logging: Tối giản (--quiet)" -Color Cyan
}
else {
    Write-ColorOutput "Logging: Mặc định" -Color Cyan
}

# Thực thi command cho từng service
$results = @{}
$successCount = 0
$failCount = 0

foreach ($svc in $targetServices) {
    $result = Invoke-GradleCommand -ServiceName $svc -GradleCommand $Command
    $results[$svc] = $result
    
    if ($result) {
        $successCount++
    } else {
        $failCount++
    }
}

# Hiển thị kết quả tổng hợp
$endTime = Get-Date
$duration = $endTime - $startTime

Write-ColorOutput "`n╔════════════════════════════════════════╗" -Color Magenta
Write-ColorOutput "║           KẾT QUẢ TỔNG HỢP            ║" -Color Magenta
Write-ColorOutput "╚════════════════════════════════════════╝" -Color Magenta

foreach ($svc in $targetServices) {
    $status = if ($results[$svc]) { "✓ THÀNH CÔNG" } else { "✗ THẤT BẠI" }
    $color = if ($results[$svc]) { "Green" } else { "Red" }
    Write-ColorOutput "  $svc : $status" -Color $color
}

Write-ColorOutput "`nTổng số services: $($targetServices.Count)" -Color White
Write-ColorOutput "Thành công: $successCount" -Color Green
Write-ColorOutput "Thất bại: $failCount" -Color Red
Write-ColorOutput "Thời gian thực thi: $($duration.ToString('mm\:ss'))" -Color Cyan

if ($failCount -gt 0) {
    Write-ColorOutput "`n⚠ Có lỗi xảy ra trong quá trình build!" -Color Yellow
    exit 1
} else {
    Write-ColorOutput "`n✓ Hoàn thành toàn bộ quá trình!" -Color Green
    exit 0
}

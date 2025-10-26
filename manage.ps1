# Script quan ly build cac service trong PBL6-QUICKDN-BACKEND
# Su dung: .\manage.ps1 <command> [service-name] [-DetailedLog]
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

# Danh sach cac service
$services = @('gateway', 'auth-service', 'order-service', 'goongmap-service')

# Ham hien thi thong bao mau
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = 'White'
    )
    Write-Host $Message -ForegroundColor $Color
}

# Ham thuc thi gradle command cho mot service
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
        Write-ColorOutput "ERROR: Service '$ServiceName' khong ton tai!" -Color Red
        return $false
    }
    
    $gradlewPath = Join-Path $servicePath "gradlew.bat"
    
    if (-not (Test-Path $gradlewPath)) {
        Write-ColorOutput "ERROR: Khong tim thay gradlew.bat trong '$ServiceName'!" -Color Red
        return $false
    }
    
    # Chuyen den thu muc service
    Push-Location $servicePath
    
    try {
        # Xay dung cac tham so Gradle
        $gradleArgs = @($GradleCommand)
        
        # Them cac flags tuy theo mode
        if ($DetailedLog) {
            $gradleArgs += '--info'
            $gradleArgs += '--stacktrace'
            Write-ColorOutput 'Mode: Detailed logging bat (--info --stacktrace)' -Color Yellow
        }
        elseif ($Quiet) {
            $gradleArgs += '--quiet'
            Write-ColorOutput 'Mode: Quiet mode bat (--quiet)' -Color Yellow
        }
        
        Write-ColorOutput "Dang thuc thi: .\gradlew.bat $($gradleArgs -join ' ')" -Color Yellow
        
        # Thuc thi Gradle command
        if ($DetailedLog) {
            # Hien thi full output khi DetailedLog duoc bat
            & .\gradlew.bat @gradleArgs
        }
        else {
            # Output binh thuong
            & .\gradlew.bat @gradleArgs
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "`nThanh cong: $GradleCommand '$ServiceName'" -Color Green
            return $true
        } else {
            Write-ColorOutput "`nThat bai: $GradleCommand '$ServiceName' (Exit code: $LASTEXITCODE)" -Color Red
            return $false
        }
    }
    catch {
        Write-ColorOutput "`nLoi khi thuc thi: $_" -Color Red
        return $false
    }
    finally {
        # Quay lai thu muc goc
        Pop-Location
    }
}

# Main execution
Write-ColorOutput "========================================" -Color Magenta
Write-ColorOutput "  PBL6 QUICKDN Backend Build Manager   " -Color Magenta
Write-ColorOutput "========================================" -Color Magenta

$startTime = Get-Date

# Xac dinh danh sach service can xu ly
$targetServices = @()
if ($Service -eq 'all') {
    $targetServices = $services
    Write-ColorOutput "`nChe do: Build tat ca cac services" -Color White
} else {
    $targetServices = @($Service)
    Write-ColorOutput "`nChe do: Build service don le" -Color White
}

Write-ColorOutput "Command: $Command" -Color White
Write-ColorOutput "Services: $($targetServices -join ', ')" -Color White

# Hien thi logging mode
if ($DetailedLog) {
    Write-ColorOutput 'Logging: Chi tiet (--info --stacktrace)' -Color Cyan
}
elseif ($Quiet) {
    Write-ColorOutput 'Logging: Toi gian (--quiet)' -Color Cyan
}
else {
    Write-ColorOutput 'Logging: Mac dinh' -Color Cyan
}

# Thuc thi command cho tung service
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

# Hien thi ket qua tong hop
$endTime = Get-Date
$duration = $endTime - $startTime

Write-ColorOutput "`n========================================" -Color Magenta
Write-ColorOutput "           KET QUA TONG HOP            " -Color Magenta
Write-ColorOutput "========================================" -Color Magenta

foreach ($svc in $targetServices) {
    $status = if ($results[$svc]) { "THANH CONG" } else { "THAT BAI" }
    $color = if ($results[$svc]) { "Green" } else { "Red" }
    Write-ColorOutput "  $svc : $status" -Color $color
}

Write-ColorOutput "`nTong so services: $($targetServices.Count)" -Color White
Write-ColorOutput "Thanh cong: $successCount" -Color Green
Write-ColorOutput "That bai: $failCount" -Color Red
Write-ColorOutput "Thoi gian thuc thi: $($duration.ToString('mm\:ss'))" -Color Cyan

if ($failCount -gt 0) {
    Write-ColorOutput "`nCo loi xay ra trong qua trinh build!" -Color Yellow
    exit 1
} else {
    Write-Host ""
    Write-ColorOutput "Hoan thanh toan bo qua trinh!" -Color Green
    exit 0
}

# Chinese Poetry App - Windows Packaging Script
# This script packages the application for deployment to Linux servers

# Fix console encoding for Chinese characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Starting packaging process..." -ForegroundColor Green

# Configuration - Use parent directory as project root
$ProjectRoot = Split-Path $PSScriptRoot -Parent
$OutputFile = "chinese-poetry-app.zip"
$OutputPath = Join-Path $ProjectRoot $OutputFile

# Items to exclude from packaging
$ExcludeItems = @(
    "venv",
    "__pycache__",
    "*.pyc",
    "logs",
    ".git",
    ".vscode",
    ".idea",
    "*.log",
    "deploy",
    ".gitignore",
    $OutputFile
)

Write-Host "[Project] $ProjectRoot" -ForegroundColor Cyan
Write-Host "[Package] $OutputFile" -ForegroundColor Cyan
Write-Host ""

# Delete old package if exists
if (Test-Path $OutputPath) {
    Write-Host "[Cleanup] Removing old package..." -ForegroundColor Yellow
    Remove-Item $OutputPath -Force
}

# Get all files excluding specified patterns
Write-Host "[Scan] Collecting files to package..." -ForegroundColor Cyan

$FilesToPackage = Get-ChildItem -Path $ProjectRoot -Recurse -File | Where-Object {
    $file = $_
    $shouldExclude = $false
    
    foreach ($pattern in $ExcludeItems) {
        # Check if file path contains excluded directory
        if ($file.FullName -like "*\$pattern\*") {
            $shouldExclude = $true
            break
        }
        # Check if file matches excluded pattern
        if ($file.Name -like $pattern) {
            $shouldExclude = $true
            break
        }
    }
    
    -not $shouldExclude
}

Write-Host "[Success] Found $($FilesToPackage.Count) files to package" -ForegroundColor Green
Write-Host ""

# Create temporary directory for packaging
$TempDir = Join-Path $env:TEMP "chinese-poetry-app-temp"
if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

Write-Host "[Copy] Copying files to temporary directory..." -ForegroundColor Cyan

# Copy files maintaining directory structure
foreach ($file in $FilesToPackage) {
    $relativePath = $file.FullName.Substring($ProjectRoot.Length + 1)
    $destPath = Join-Path $TempDir $relativePath
    $destDir = Split-Path $destPath -Parent
    
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }
    
    Copy-Item $file.FullName -Destination $destPath
}

Write-Host "[Success] Files copied successfully" -ForegroundColor Green
Write-Host ""

# Create zip archive
Write-Host "[Compress] Creating zip archive..." -ForegroundColor Cyan

try {
    Compress-Archive -Path "$TempDir\*" -DestinationPath $OutputPath -Force
    Write-Host "[Success] Package created successfully!" -ForegroundColor Green
} catch {
    Write-Host "[Error] Failed to create package: $_" -ForegroundColor Red
    exit 1
} finally {
    # Clean up temporary directory
    Remove-Item $TempDir -Recurse -Force
}

# Display package info
if (Test-Path $OutputPath) {
    $PackageSize = (Get-Item $OutputPath).Length / 1MB
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Gray
    Write-Host "Package ready for deployment!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Gray
    Write-Host "[Package] $OutputFile" -ForegroundColor Cyan
    Write-Host "[Size] $("{0:N2}" -f $PackageSize) MB" -ForegroundColor Cyan
    Write-Host "[Location] $OutputPath" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "   1. Upload to server: scp $OutputFile user@your-server-ip:/tmp/" -ForegroundColor White
    Write-Host "   2. SSH to server: ssh user@your-server-ip" -ForegroundColor White
    Write-Host "   3. Extract: sudo unzip /tmp/$OutputFile -d /var/www/chinese-poetry-app" -ForegroundColor White
    Write-Host "   4. Configure: sudo cp config/constants.py.example config/constants.py" -ForegroundColor White
    Write-Host "   5. Deploy: sudo ./deploy.sh" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "[Error] Package file not found at $OutputPath" -ForegroundColor Red
    exit 1
}

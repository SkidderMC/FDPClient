<#
FDPClient - Lunar native delivery by opZywl
https://github.com/SkidderMC/FDPClient/ | https://lucas-lima.xyz
Build the current payload and native loader, then update the ZAVZ resources.
#>
[CmdletBinding()]
param(
    [string]$Jdk8,
    [string]$Jdk17,
    [string]$Cmake,
    [string]$ZavzRoot = (Join-Path $PSScriptRoot '../../../YZYInjector'),
    [switch]$SkipZavzBuild
)
$ErrorActionPreference = 'Stop'
$taskRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$ZavzRoot = (Resolve-Path -LiteralPath $ZavzRoot).Path
if (!(Test-Path -LiteralPath (Join-Path $ZavzRoot 'src-tauri/Cargo.toml'))) {
    throw 'ZavzRoot must point to the YZYInjector checkout.'
}

function Find-Jdk([string]$Explicit, [string]$Version) {
    $candidates = @($Explicit, $env:JAVA_HOME, $env:JDK8_HOME, $env:JDK17_HOME)
    foreach ($directory in @((Join-Path $env:USERPROFILE '.jdks'), 'C:/src', (Join-Path $env:ProgramFiles 'Java'))) {
        if (Test-Path -LiteralPath $directory) {
            $candidates += @(Get-ChildItem -LiteralPath $directory -Directory | ForEach-Object FullName)
        }
    }
    foreach ($candidate in $candidates) {
        if (!$candidate) { continue }
        $release = Join-Path $candidate 'release'
        if ((Test-Path -LiteralPath (Join-Path $candidate 'bin/javac.exe')) -and
            (Test-Path -LiteralPath $release) -and
            ((Get-Content -LiteralPath $release -Raw) -match ('JAVA_VERSION="' + [regex]::Escape($Version) + '[.\"]'))) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    throw "Set -Jdk8 and -Jdk17 to installed JDKs. Java $Version was not found."
}

$Jdk8 = Find-Jdk $Jdk8 '1.8'
$Jdk17 = Find-Jdk $Jdk17 '17'
if (!$Cmake) {
    $command = Get-Command cmake -ErrorAction SilentlyContinue
    if ($command) { $Cmake = $command.Source }
    else {
        $vswhere = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio/Installer/vswhere.exe'
        if (Test-Path -LiteralPath $vswhere) {
            $Cmake = & $vswhere -latest -products '*' -find 'Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/cmake.exe' | Select-Object -First 1
        }
    }
}
if (!$Cmake -or !(Test-Path -LiteralPath $Cmake)) { throw 'Install Visual Studio C++ Build Tools with CMake, or pass -Cmake.' }
$previousJava = $env:JAVA_HOME
Push-Location -LiteralPath $taskRoot
try {
    $env:JAVA_HOME = $Jdk8
    & ./gradlew.bat check build "-PzavzDir=$ZavzRoot"
    if ($LASTEXITCODE -ne 0) { throw 'FDPClient checks or build failed.' }
    $env:JAVA_HOME = $Jdk17
    $cmakeJava = $Jdk17.Replace('\', '/')
    & $Cmake -S native/lunar -B build/native-lunar -G 'Visual Studio 17 2022' -A x64 "-DJAVA_HOME:PATH=$cmakeJava"
    if ($LASTEXITCODE -ne 0) { throw 'Native CMake configuration failed.' }
    & $Cmake --build build/native-lunar --config Release
    if ($LASTEXITCODE -ne 0) { throw 'Native loader build failed.' }
    $dll = Join-Path $taskRoot 'build/native-lunar/Release/fdp-lunar.dll'
    Copy-Item -LiteralPath $dll -Destination (Join-Path $ZavzRoot 'src/load/fdp-lunar.dll')
    $nativeSources = Join-Path $ZavzRoot 'native/fdp-lunar'
    New-Item -ItemType Directory -Force -Path $nativeSources | Out-Null
    foreach ($source in @('CMakeLists.txt', 'loader.cpp', 'version.rc')) {
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot $source) -Destination $nativeSources
    }
    $bundle = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'build/libs') -Filter 'fdpclient-inject-*.jar' | Sort-Object LastWriteTime | Select-Object -Last 1; if (!$bundle) { throw 'Inject bundle not found in build/libs.' }
    $desktop = [Environment]::GetFolderPath('DesktopDirectory')
    Copy-Item -LiteralPath $bundle.FullName -Destination (Join-Path $desktop $bundle.Name)
    if (!$SkipZavzBuild) {
        Push-Location -LiteralPath $ZavzRoot
        try {
            & npx tauri build --no-bundle
            if ($LASTEXITCODE -ne 0) { throw 'ZAVZ build failed. Run npm install in YZYInjector if dependencies are missing.' }
            $exe = Join-Path $ZavzRoot 'src-tauri/target/release/ZAVZ.exe'
            Copy-Item -LiteralPath $exe -Destination (Join-Path $desktop 'ZAVZ.exe')
        } finally { Pop-Location }
    }
    Write-Output "Updated ZAVZ resources in $ZavzRoot and deliverables in $desktop"
} finally {
    $env:JAVA_HOME = $previousJava
    Pop-Location
}

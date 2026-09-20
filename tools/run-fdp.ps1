#Requires -Version 5
# ============================================================================
#  FDPClient - Validate & Run
#  Compila a jar (validando com detekt/test), instala no .minecraft e abre o
#  launcher oficial para voce entrar como um usuario normal.
# ============================================================================

$ErrorActionPreference = 'Stop'

function Info($m)  { Write-Host $m -ForegroundColor Cyan }
function Ok($m)    { Write-Host "  [OK] $m" -ForegroundColor Green }
function Warn($m)  { Write-Host "  [!] $m" -ForegroundColor Yellow }
function Fail($m)  { Write-Host "  [ERRO] $m" -ForegroundColor Red }
function Step($n,$m) { Write-Host "`n[$n] $m" -ForegroundColor White -BackgroundColor DarkBlue }

# Raiz do projeto (a pasta acima de /tools)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$mc = Join-Path $env:APPDATA '.minecraft'
$launcherAppId = 'Microsoft.4297127D64EC6_8wekyb3d8bbwe!Minecraft'

Info "============================================================"
Info "  FDPClient - Validate & Run  ($root)"
Info "============================================================"

# ---------------------------------------------------------------------------
Step 1 "Pre-flight"
if (-not (Test-Path (Join-Path $root 'gradlew.bat'))) { Fail "gradlew.bat nao encontrado. Rode a partir da raiz do projeto."; exit 1 }
Ok "gradle wrapper encontrado"

if (-not (Test-Path $mc)) { Fail ".minecraft nao encontrado em $mc"; exit 1 }
Ok ".minecraft: $mc"

$forge = Get-ChildItem (Join-Path $mc 'versions') -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '1\.8\.9' -and $_.Name -match 'forge' } | Select-Object -First 1
if ($forge) { Ok "Perfil Forge 1.8.9: $($forge.Name)" }
else { Warn "Nenhum perfil Forge 1.8.9 encontrado. Instale o Forge 1.8.9 (11.15.1.2318) antes de jogar." }

# ---------------------------------------------------------------------------
Step 2 "Compilando a jar (build + detekt + test + reobf)"
$env:GRADLE_OPTS = '-Xmx3g'
Info "  Rodando: .\gradlew.bat build reobfShadowJar --console=plain"
& .\gradlew.bat build reobfShadowJar --console=plain
if ($LASTEXITCODE -ne 0) { Fail "Build falhou (exit $LASTEXITCODE). Corrija os erros acima e rode de novo."; exit 1 }
Ok "Build concluido"

# ---------------------------------------------------------------------------
Step 3 "Localizando a jar gerada"
$jar = Get-ChildItem (Join-Path $root 'build\libs') -Filter 'FDPClient*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '-(sources|dev|api|all)\.jar$' } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { Fail "Nao achei a jar em build\libs. O build produziu algo?"; exit 1 }
Ok "Jar: $($jar.Name)  ($([math]::Round($jar.Length/1MB,1)) MB, $($jar.LastWriteTime.ToString('HH:mm:ss')))"

# ---------------------------------------------------------------------------
Step 4 "Instalando no .minecraft\mods"
$modsDir = Join-Path $mc 'mods'
if (-not (Test-Path $modsDir)) { New-Item -ItemType Directory -Path $modsDir -Force | Out-Null }
# Remove versoes antigas do FDP para nao ter coremod duplicado
Get-ChildItem $modsDir -Filter 'FDP*.jar' -ErrorAction SilentlyContinue | ForEach-Object {
    Warn "Removendo jar antiga: $($_.Name)"
    Remove-Item $_.FullName -Force
}
$dest = Join-Path $modsDir $jar.Name
Copy-Item $jar.FullName $dest -Force
Ok "Copiada para: $dest"

# ---------------------------------------------------------------------------
Step 5 "Escrevendo a config de Scaffold (Telly)"
$settingsDir = Join-Path $mc 'FDPCLIENT\settings'
if (-not (Test-Path $settingsDir)) { New-Item -ItemType Directory -Path $settingsDir -Force | Out-Null }
$telly = @'
# ==================================================================
#  FDPClient - Config Scaffold "Telly" (b16 -> b17)
#  Carregue no jogo com:  .localsettings load telly
# ==================================================================
Scaffold ScaffoldMode Telly
Scaffold Sprint true
Scaffold BlockSafe true
Scaffold SpeedModifier 1.1272727
Scaffold Strafe true
Scaffold ResetTicks 1
'@
$tellyPath = Join-Path $settingsDir 'telly.txt'
# UTF-8 sem BOM: o parser trata a 1a linha como setting; um BOM viraria erro de sintaxe.
[System.IO.File]::WriteAllText($tellyPath, ($telly -replace "`r`n", "`n"))
Ok "Config salva: $tellyPath"
Info "  (Rotations = Normal ja e o padrao; nao seto por colisao de nome de valor)"

# ---------------------------------------------------------------------------
Step 6 "Abrindo o Minecraft Launcher"
try {
    Start-Process "shell:AppsFolder\$launcherAppId"
    Ok "Launcher aberto"
} catch {
    Warn "Nao consegui abrir o launcher automaticamente. Abra o 'Minecraft Launcher' manualmente."
}

# ---------------------------------------------------------------------------
Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "  PRONTO. Agora, como um usuario normal:" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  1) No launcher, selecione o perfil " -NoNewline; Write-Host "Forge 1.8.9" -ForegroundColor Yellow
Write-Host "     ($($forge.Name))"
Write-Host "  2) Clique em JOGAR e entre no servidor de teste"
Write-Host "  3) No chat do jogo, aplique a config de Scaffold com:"
Write-Host "        .localsettings load telly" -ForegroundColor Yellow
Write-Host "     (aplica: ScaffoldMode=Telly, Sprint=on, BlockSafe=on,"
Write-Host "      SpeedModifier=1.1272727, Strafe=on, ResetTicks=1)"
Write-Host "  4) Toggle o Scaffold e teste no Grim (modern e legacy)."
Write-Host ""
Write-Host "  Sua config atual (values.json) NAO foi sobrescrita." -ForegroundColor DarkGray
Write-Host ""

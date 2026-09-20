@echo off
setlocal
cd /d "%~dp0"
title FDPClient - Validate ^& Run
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\run-fdp.ps1"
set "RC=%ERRORLEVEL%"
echo.
if not "%RC%"=="0" (
  echo [ERRO] Falha ao preparar/iniciar o FDPClient ^(codigo %RC%^). Veja as mensagens acima.
) else (
  echo [OK] Tudo pronto. Pode fechar esta janela.
)
echo.
pause
endlocal

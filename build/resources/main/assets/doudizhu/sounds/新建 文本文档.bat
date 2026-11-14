@echo off
chcp 65001 >nul
echo ========================================================
echo       正在将当前文件夹下的所有文件名转换为小写...
echo ========================================================
echo.

:: 切换到当前脚本所在的目录
cd /d "%~dp0"

:: 调用 PowerShell 强力改名 (排除脚本自身)
powershell -Command "Get-ChildItem -File | Where-Object { $_.Name -ne '一键转小写.bat' } | ForEach-Object { $lowerName = $_.Name.ToLower(); if ($_.Name -cne $lowerName) { Rename-Item -Path $_.FullName -NewName $lowerName; Write-Host '已修改: ' $_.Name ' -> ' $lowerName -ForegroundColor Green } }"

echo.
echo ========================================================
echo                 全部完成！请检查文件名。
echo ========================================================
pause
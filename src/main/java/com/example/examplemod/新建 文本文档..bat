@echo off
chcp 65001
echo 正在提取文件名...
dir /b /a-d *.* > 文件名列表.txt
echo 提取完成！请查看“文件名列表.txt”
pause
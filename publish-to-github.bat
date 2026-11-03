@echo off
chcp 65001 > nul
echo.
echo ========================================
echo    GitHub 发布脚本
echo ========================================
echo.

:: 检查 Git 是否安装
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Git 未安装
    echo 请先安装 Git: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo [检查] Git 已安装
git --version

:: 检查是否已经在 Git 仓库中
if exist ".git" (
    echo [检查] Git 仓库已存在
) else (
    echo [初始化] 初始化 Git 仓库...
    git init
    git checkout -B main
)

:: 添加所有文件
echo.
echo [添加] 添加所有文件到 Git...
git add .

:: 检查是否有文件
git status --porcelain > temp_status.txt
findstr /R "." temp_status.txt > nul
if %errorlevel% neq 0 (
    echo [错误] 没有文件需要提交
    del temp_status.txt
    pause
    exit /b 1
)

:: 显示将要提交的文件数量
set /p file_count=<temp_status.txt
for /f %%i in ('find /c /v "" ^< temp_status.txt') do set count=%%i
echo [添加] 已添加 %count% 个文件
del temp_status.txt

:: 创建提交
echo.
echo [提交] 创建初始提交...
git commit -m "Initial commit: Complete recipe app with Room database, multi-dimensional search, and Unsplash images"

:: 配置远程仓库
echo.
echo [配置] 配置远程仓库...
git remote remove origin >nul 2>&1
git remote add origin https://github.com/LkkEternal/aaa.git

:: 推送代码
echo.
echo [推送] 准备推送代码到 GitHub...
echo.
echo [提示] 如果是第一次推送，系统会要求输入 GitHub 用户名和密码
echo [提示] 用户名: LkkEternal
echo [提示] 密码: 请使用 Personal Access Token（不是 GitHub 密码！）
echo [提示] Token 生成地址: https://github.com/settings/tokens
echo.

git push -u origin main --force

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    ✓ 发布成功！
    echo ========================================
    echo.
    echo 仓库地址: https://github.com/LkkEternal/aaa
    echo.
) else (
    echo.
    echo ========================================
    echo    ✗ 发布失败
    echo ========================================
    echo.
    echo 请检查:
    echo 1. 网络连接是否正常
    echo 2. 用户名和密码是否正确
    echo 3. 仓库是否存在
    echo.
)

echo 按任意键退出...
pause > nul
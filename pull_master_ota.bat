@echo off
chcp 65001 >nul
title 拉取最新 master（保留 OTA）
cd /d "E:\学习资料\make\navel-orange-monitoring"

echo ============================================
echo  拉取 Gitee 最新 master + 保留 OTA 功能
echo ============================================
echo.

echo [1/6] 强制拉取远程 master 引用 ...
git fetch origin +refs/heads/master:refs/remotes/origin/master
echo.

echo [2/6] 查看远程最新提交 ...
git log origin/master --oneline -5
echo.

echo [3/6] 切到 master ...
git checkout master
echo.

echo [4/6] 合并远程 master ...
git merge origin/master --no-edit
echo.

echo [5/6] 合并后 master 提交记录 ...
git log master --oneline -10
echo.

echo [6/6] 检查 OTA 文件是否保留 ...
echo.
echo --- Git 跟踪的 OTA 文件 ---
git ls-files *ota* *OTA* *firmware*
echo.
echo --- 包含 OTA 引用的代码文件 ---
git grep -il "ota_update\|OTA\|firmware\|固件" -- "*.java" "*.vue" "*.ts" "*.js" 2>nul
echo.
echo --- 工作目录 OTA 文件 ---
dir /s /b *ota* *OTA* *firmware* 2>nul
echo.
echo ============================================
echo  完成！
echo ============================================
pause

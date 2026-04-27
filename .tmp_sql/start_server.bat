@echo off
chcp 65001 >nul
cd /d e:\ch\ruoyi-vue-pro\yudao-server
call mvn exec:exec "-Dmaven.repo.local=F:\repo" > e:\ch\server-run-hide-iot.log 2>&1

@echo off
chcp 65001 >nul
title 产品状态一键验证

echo ================================================================================
echo 产品状态一键验证脚本
echo ================================================================================
echo.
echo 此脚本将检查:
echo   [1] 物模型配置
echo   [2] TDengine超级表
echo   [3] 产品发布状态
echo   [4] 设备列表
echo   [5] RocketMQ指南
echo   [6] EMQX指南
echo.
echo ================================================================================
echo.

echo 正在启动验证脚本...
echo.

python ruoyi-vue-pro\sql\mysql\verify_product_status.py

echo.
echo ================================================================================
echo 验证完成
echo ================================================================================
echo.

echo 如果需要导入物模型，请运行:
echo   python ruoyi-vue-pro\sql\mysql\import_thing_model_tsl.py
echo.

pause


















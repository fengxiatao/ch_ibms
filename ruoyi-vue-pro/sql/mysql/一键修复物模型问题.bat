@echo off
chcp 65001 >nul
title 一键修复物模型问题

echo ================================================================================
echo 一键修复物模型问题
echo ================================================================================
echo.
echo 问题描述:
echo   - 点击发布后TDengine未创建超级表
echo   - 后端日志显示"物模型的属性配置为空"
echo.
echo 根本原因:
echo   - 导入脚本的数据结构错误
echo   - property字段未正确嵌套
echo.
echo 解决方案:
echo   1. 清理旧的错误数据
echo   2. 使用修复后的脚本重新导入
echo   3. 重新发布产品
echo.
echo ================================================================================
echo.

pause

echo.
echo [步骤1/2] 清理旧的物模型数据...
echo.
python clear_and_reimport_thing_model.py

echo.
echo.
echo [步骤2/2] 重新导入物模型TSL...
echo.
python import_thing_model_tsl.py

echo.
echo ================================================================================
echo 修复完成
echo ================================================================================
echo.
echo 下一步操作:
echo   1. 前端: 智慧物联 -^> 产品管理 -^> 网络摄像头(枪机)
echo   2. 点击"撤销发布"
echo   3. 再次点击"发布"
echo   4. 查看后端日志，应该显示"超级表创建成功"
echo.
echo 验证超级表:
echo   访问TDengine Web界面
echo   USE ch_ibms;
echo   DESCRIBE product_property_3;
echo.

pause


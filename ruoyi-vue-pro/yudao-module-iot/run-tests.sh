#!/bin/bash

# IoT模块测试运行脚本
# 使用方法：
#   ./run-tests.sh              # 运行所有测试
#   ./run-tests.sh unit         # 只运行单元测试
#   ./run-tests.sh integration  # 只运行集成测试
#   ./run-tests.sh coverage     # 生成覆盖率报告

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

print_info "IoT模块测试脚本启动..."
print_info "工作目录: $SCRIPT_DIR"

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    print_error "Maven未安装，请先安装Maven"
    exit 1
fi

# 根据参数决定运行哪些测试
TEST_TYPE=${1:-all}

case $TEST_TYPE in
    "unit")
        print_info "运行单元测试..."
        mvn clean test -Dtest=!*IntegrationTest
        ;;
    
    "integration")
        print_info "运行集成测试..."
        mvn clean test -Dtest=*IntegrationTest
        ;;
    
    "coverage")
        print_info "运行测试并生成覆盖率报告..."
        mvn clean test jacoco:report
        
        # 打开覆盖率报告
        REPORT_PATH="target/site/jacoco/index.html"
        if [ -f "$REPORT_PATH" ]; then
            print_info "覆盖率报告已生成: $REPORT_PATH"
            
            # 尝试打开浏览器（跨平台）
            if command -v xdg-open &> /dev/null; then
                xdg-open "$REPORT_PATH"
            elif command -v open &> /dev/null; then
                open "$REPORT_PATH"
            elif command -v start &> /dev/null; then
                start "$REPORT_PATH"
            else
                print_warn "请手动打开覆盖率报告: $REPORT_PATH"
            fi
        else
            print_error "覆盖率报告生成失败"
        fi
        ;;
    
    "gateway")
        print_info "运行Gateway模块测试..."
        cd yudao-module-iot-gateway
        mvn clean test
        cd ..
        ;;
    
    "biz")
        print_info "运行Biz模块测试..."
        cd yudao-module-iot-biz
        mvn clean test
        cd ..
        ;;
    
    "all"|*)
        print_info "运行所有测试..."
        mvn clean test
        ;;
esac

# 检查测试结果
if [ $? -eq 0 ]; then
    print_info "✅ 测试执行成功！"
else
    print_error "❌ 测试执行失败！"
    exit 1
fi

# 显示测试摘要
print_info "测试执行完成，查看详细报告："
print_info "  - Surefire报告: target/surefire-reports/"
print_info "  - HTML报告: target/site/surefire-report.html"

exit 0













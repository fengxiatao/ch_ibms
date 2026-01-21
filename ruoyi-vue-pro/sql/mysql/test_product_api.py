#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试产品管理API - 验证添加产品功能"""

import requests
import json
import sys
from datetime import datetime

# ========== 配置 ==========
BASE_URL = "http://localhost:48888/admin-api"
USERNAME = "admin"
PASSWORD = "admin123"

class ProductAPITester:
    def __init__(self):
        self.session = requests.Session()
        self.token = None
        self.test_results = []
        
    def log_test(self, test_name, passed, message=""):
        """记录测试结果"""
        status = "[PASS]" if passed else "[FAIL]"
        self.test_results.append({
            "test": test_name,
            "passed": passed,
            "message": message
        })
        print(f"   {status} {test_name}")
        if message:
            print(f"          {message}")
    
    def test_login(self):
        """测试1: 登录功能"""
        print("\n[Test 1] 登录系统")
        
        # 先获取租户信息
        tenant_url = f"{BASE_URL}/system/tenant/get-by-website?website=localhost:48888"
        try:
            response = self.session.get(tenant_url)
            result = response.json()
            if result.get("code") == 0:
                tenant = result.get("data")
                if tenant:
                    self.tenant_id = tenant["id"]
                    print(f"   租户: {tenant.get('name')} (ID: {self.tenant_id})")
        except:
            self.tenant_id = 1  # 默认租户
            print(f"   使用默认租户ID: {self.tenant_id}")
        
        url = f"{BASE_URL}/system/auth/login"
        data = {
            "username": USERNAME,
            "password": PASSWORD
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                self.token = result["data"]["accessToken"]
                self.session.headers.update({
                    "Authorization": f"Bearer {self.token}",
                    "Content-Type": "application/json",
                    "tenant-id": str(self.tenant_id if self.tenant_id else 1)
                })
                self.log_test("登录成功", True, f"Token: {self.token[:30]}...")
                return True
            else:
                self.log_test("登录失败", False, result.get("msg"))
                return False
                
        except Exception as e:
            self.log_test("登录异常", False, str(e))
            return False
    
    def test_get_categories(self):
        """测试2: 获取产品分类列表"""
        print("\n[Test 2] 获取产品分类")
        
        url = f"{BASE_URL}/iot/product-category/list"
        
        try:
            response = self.session.get(url)
            result = response.json()
            
            if result.get("code") == 0:
                categories = result.get("data", [])
                self.log_test("获取分类列表", True, f"共 {len(categories)} 个分类")
                
                # 显示前5个分类
                for i, cat in enumerate(categories[:5]):
                    print(f"          - {cat['name']} (ID: {cat['id']})")
                
                return categories
            else:
                self.log_test("获取分类失败", False, result.get("msg"))
                return []
                
        except Exception as e:
            self.log_test("获取分类异常", False, str(e))
            return []
    
    def test_create_category(self):
        """测试3: 创建产品分类"""
        print("\n[Test 3] 创建产品分类")
        
        url = f"{BASE_URL}/iot/product-category/create"
        data = {
            "name": f"测试分类_{datetime.now().strftime('%H%M%S')}",
            "description": "自动化测试创建的分类",
            "status": 0
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                category_id = result["data"]
                self.log_test("创建分类成功", True, f"分类ID: {category_id}")
                return category_id
            else:
                self.log_test("创建分类失败", False, result.get("msg"))
                return None
                
        except Exception as e:
            self.log_test("创建分类异常", False, str(e))
            return None
    
    def test_create_product(self, category_id):
        """测试4: 创建产品"""
        print("\n[Test 4] 创建产品")
        
        url = f"{BASE_URL}/iot/product/create"
        data = {
            "name": f"测试产品_{datetime.now().strftime('%H%M%S')}",
            "categoryId": category_id,
            "transportProtocol": "TCP",
            "dataFormat": "JSON",
            "deviceType": 0,
            "status": 0,
            "description": "自动化测试创建的产品"
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                product_id = result["data"]
                self.log_test("创建产品成功", True, f"产品ID: {product_id}")
                return product_id
            else:
                self.log_test("创建产品失败", False, result.get("msg"))
                return None
                
        except Exception as e:
            self.log_test("创建产品异常", False, str(e))
            return None
    
    def test_get_product(self, product_id):
        """测试5: 查询产品详情"""
        print("\n[Test 5] 查询产品详情")
        
        url = f"{BASE_URL}/iot/product/get?id={product_id}"
        
        try:
            response = self.session.get(url)
            result = response.json()
            
            if result.get("code") == 0:
                product = result["data"]
                self.log_test("查询产品成功", True, f"产品: {product['name']}")
                print(f"          - 产品标识: {product.get('productKey')}")
                print(f"          - 传输协议: {product.get('transportProtocol')}")
                print(f"          - 数据格式: {product.get('dataFormat')}")
                return product
            else:
                self.log_test("查询产品失败", False, result.get("msg"))
                return None
                
        except Exception as e:
            self.log_test("查询产品异常", False, str(e))
            return None
    
    def test_add_property(self, product_id):
        """测试6: 添加产品属性"""
        print("\n[Test 6] 添加产品属性")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        data = {
            "productId": product_id,
            "type": 1,  # 属性
            "identifier": "test_temperature",
            "name": "测试温度",
            "description": "测试用的温度属性",
            "accessMode": "r",
            "required": False,
            "dataType": "float",
            "dataSpecs": json.dumps({
                "min": -50,
                "max": 100,
                "unit": "℃",
                "step": 0.1
            })
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                self.log_test("添加属性成功", True, "test_temperature")
                return True
            else:
                self.log_test("添加属性失败", False, result.get("msg"))
                return False
                
        except Exception as e:
            self.log_test("添加属性异常", False, str(e))
            return False
    
    def test_add_service(self, product_id):
        """测试7: 添加产品服务"""
        print("\n[Test 7] 添加产品服务")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        data = {
            "productId": product_id,
            "type": 2,  # 服务
            "identifier": "test_reboot",
            "name": "测试重启",
            "description": "测试用的重启服务",
            "callType": "async",
            "dataType": "object",
            "dataSpecs": json.dumps({
                "inputParams": [],
                "outputParams": []
            })
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                self.log_test("添加服务成功", True, "test_reboot")
                return True
            else:
                self.log_test("添加服务失败", False, result.get("msg"))
                return False
                
        except Exception as e:
            self.log_test("添加服务异常", False, str(e))
            return False
    
    def test_add_event(self, product_id):
        """测试8: 添加产品事件"""
        print("\n[Test 8] 添加产品事件")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        data = {
            "productId": product_id,
            "type": 3,  # 事件
            "identifier": "test_alert",
            "name": "测试告警",
            "description": "测试用的告警事件",
            "eventType": "alert",
            "dataType": "object",
            "dataSpecs": json.dumps({
                "outputParams": [
                    {
                        "identifier": "message",
                        "name": "告警信息",
                        "dataType": {"type": "text"}
                    }
                ]
            })
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                self.log_test("添加事件成功", True, "test_alert")
                return True
            else:
                self.log_test("添加事件失败", False, result.get("msg"))
                return False
                
        except Exception as e:
            self.log_test("添加事件异常", False, str(e))
            return False
    
    def test_delete_product(self, product_id):
        """测试9: 删除产品（清理）"""
        print("\n[Test 9] 删除测试产品")
        
        url = f"{BASE_URL}/iot/product/delete?id={product_id}"
        
        try:
            response = self.session.delete(url)
            result = response.json()
            
            if result.get("code") == 0:
                self.log_test("删除产品成功", True, "测试数据已清理")
                return True
            else:
                self.log_test("删除产品失败", False, result.get("msg"))
                return False
                
        except Exception as e:
            self.log_test("删除产品异常", False, str(e))
            return False
    
    def print_summary(self):
        """打印测试总结"""
        print("\n" + "=" * 80)
        print("测试总结")
        print("=" * 80)
        
        total = len(self.test_results)
        passed = sum(1 for r in self.test_results if r["passed"])
        failed = total - passed
        
        print(f"总测试数: {total}")
        print(f"通过: {passed} ({passed*100//total}%)")
        print(f"失败: {failed} ({failed*100//total}%)")
        
        if failed > 0:
            print("\n失败的测试:")
            for r in self.test_results:
                if not r["passed"]:
                    print(f"  [X] {r['test']}: {r['message']}")
        
        print("=" * 80)
        
        return failed == 0
    
    def run_all_tests(self):
        """运行所有测试"""
        print("=" * 80)
        print("IoT产品管理API测试")
        print("=" * 80)
        print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"后端: {BASE_URL}")
        
        # Test 1: 登录
        if not self.test_login():
            print("\n[ERROR] 登录失败，无法继续测试")
            return False
        
        # Test 2: 获取分类
        categories = self.test_get_categories()
        
        # Test 3: 创建分类
        category_id = self.test_create_category()
        if not category_id and categories:
            category_id = categories[0]["id"]
            print(f"   使用已有分类: {categories[0]['name']} (ID: {category_id})")
        
        if not category_id:
            print("\n[ERROR] 无法获取分类ID，测试终止")
            return False
        
        # Test 4: 创建产品
        product_id = self.test_create_product(category_id)
        if not product_id:
            print("\n[ERROR] 创建产品失败，后续测试无法进行")
            self.print_summary()
            return False
        
        # Test 5: 查询产品
        self.test_get_product(product_id)
        
        # Test 6-8: 添加物模型
        self.test_add_property(product_id)
        self.test_add_service(product_id)
        self.test_add_event(product_id)
        
        # Test 9: 清理（删除测试产品）
        self.test_delete_product(product_id)
        
        # 打印总结
        all_passed = self.print_summary()
        
        if all_passed:
            print("\n[SUCCESS] 所有测试通过！产品管理功能正常。")
            print("\n下一步:")
            print("1. 可以手动在前端添加大华摄像头产品")
            print("2. 或运行: python auto_add_dahua_camera_product.py")
            return True
        else:
            print("\n[WARNING] 部分测试失败，请检查后端服务。")
            return False

def main():
    tester = ProductAPITester()
    success = tester.run_all_tests()
    return 0 if success else 1

if __name__ == '__main__':
    sys.exit(main())


# IoT 模块测试文档

## 概述

本文档描述了 `yudao-module-iot` 模块的测试策略、测试覆盖范围和如何运行测试。

## 测试架构

### 测试层次

```
测试金字塔
    ┌─────────────────┐
    │   E2E测试（少）  │  端到端业务流程测试
    ├─────────────────┤
    │ 集成测试（中等）  │  多组件协作测试
    ├─────────────────┤
    │ 单元测试（大量）  │  单个组件/方法测试
    └─────────────────┘
```

### 测试分类

| 类型 | 目录 | 说明 |
|------|------|------|
| **单元测试** | `src/test/java/**/service/**` | 测试单个Service类的业务逻辑 |
| **集成测试** | `src/test/java/**/integration/**` | 测试多个组件协作的完整流程 |
| **Gateway测试** | `yudao-module-iot-gateway/src/test/` | 测试ONVIF协议网关功能 |

---

## 测试覆盖范围

### Week 2 核心功能测试（已完成）

#### 1. Gateway层测试

**OnvifServiceInvoker测试** (`OnvifServiceInvokerTest.java`)
- ✅ 抓拍服务成功场景
- ✅ 抓拍服务无配置文件场景
- ✅ 开始录像服务
- ✅ 停止录像服务
- ✅ 云台控制成功场景
- ✅ 云台控制参数缺失场景
- ✅ 云台控制无效方向场景
- ✅ 不支持的服务类型
- ✅ 服务调用异常处理

**测试用例数**: 9个
**代码覆盖率**: 目标 80%+

#### 2. Biz层测试

**DeviceActivationService测试** (`IotDeviceActivationServiceImplTest.java`)
- ✅ 激活新设备
- ✅ 激活已存在设备
- ✅ 获取激活状态
- ✅ 处理设备上线
- ✅ 处理设备上线（设备不存在）
- ✅ 处理设备离线
- ✅ 断开设备连接

**测试用例数**: 7个
**代码覆盖率**: 目标 85%+

**DeviceServiceInvokeService测试** (`IotDeviceServiceInvokeServiceImplTest.java`)
- ✅ 调用设备服务成功
- ✅ 处理服务调用结果
- ✅ 获取服务日志

**测试用例数**: 3个
**代码覆盖率**: 目标 75%+

#### 3. 集成测试

**DeviceActivationIntegrationTest** (`DeviceActivationIntegrationTest.java`)
- ✅ 完整设备激活流程
  - 设备发现 → 激活 → 上线 → 验证状态 → 离线
- ✅ 再次激活已存在设备

**测试用例数**: 2个
**覆盖流程**: 设备生命周期完整流程

---

## 测试技术栈

| 技术 | 用途 |
|------|------|
| **JUnit 5** | 测试框架 |
| **Mockito** | Mock对象和行为验证 |
| **Spring Boot Test** | Spring集成测试支持 |
| **H2 Database** | 内存数据库（集成测试） |
| **AssertJ** | 流式断言库 |

---

## 运行测试

### 运行所有测试

```bash
# Maven
mvn clean test

# Gradle
./gradlew clean test
```

### 运行特定模块测试

```bash
# 只运行Biz模块测试
mvn test -pl yudao-module-iot-biz

# 只运行Gateway模块测试
mvn test -pl yudao-module-iot-gateway
```

### 运行特定测试类

```bash
# 运行单个测试类
mvn test -Dtest=IotDeviceActivationServiceImplTest

# 运行特定测试方法
mvn test -Dtest=IotDeviceActivationServiceImplTest#testActivateDevice_NewDevice
```

### 运行集成测试

```bash
# 运行所有集成测试
mvn test -Dtest=*IntegrationTest
```

### 生成测试报告

```bash
# 生成HTML测试报告
mvn surefire-report:report

# 生成代码覆盖率报告（使用JaCoCo）
mvn clean test jacoco:report

# 报告位置
# - target/site/surefire-report.html
# - target/site/jacoco/index.html
```

---

## 测试最佳实践

### 1. 测试命名规范

```java
// ✅ 好的命名
@Test
void testActivateDevice_NewDevice() { }

@Test
void testHandleSnapshot_NoProfiles() { }

// ❌ 不好的命名
@Test
void test1() { }

@Test
void testMethod() { }
```

### 2. 测试结构（AAA模式）

```java
@Test
void testExample() {
    // Arrange（准备）
    // 准备测试数据和Mock对象
    
    // Act（执行）
    // 调用被测试的方法
    
    // Assert（断言）
    // 验证结果和行为
}
```

### 3. Mock使用原则

```java
// ✅ 只Mock外部依赖
@Mock
private IotDeviceMapper deviceMapper;

@Mock
private IotMessageBus messageBus;

// ✅ 使用@InjectMocks自动注入Mock
@InjectMocks
private IotDeviceActivationServiceImpl activationService;
```

### 4. 断言最佳实践

```java
// ✅ 使用AssertJ流式断言
assertThat(device)
    .isNotNull()
    .extracting("ip", "state")
    .containsExactly("192.168.1.202", IotDeviceStateEnum.ONLINE.getState());

// ✅ 使用ArgumentCaptor验证参数
ArgumentCaptor<IotDeviceDO> captor = ArgumentCaptor.forClass(IotDeviceDO.class);
verify(deviceMapper).insert(captor.capture());
assertEquals("192.168.1.202", captor.getValue().getIp());
```

---

## 测试数据管理

### 测试数据隔离

- 每个测试方法独立准备数据
- 使用 `@BeforeEach` 初始化通用测试数据
- 使用 `@AfterEach` 清理测试数据（如需要）

### 测试数据库

```yaml
# application-test.yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
```

---

## CI/CD集成

### GitHub Actions示例

```yaml
name: IoT Module Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run Tests
        run: mvn clean test -pl yudao-module-iot
      - name: Generate Coverage Report
        run: mvn jacoco:report
      - name: Upload Coverage
        uses: codecov/codecov-action@v2
```

---

## 测试覆盖率目标

| 模块 | 目标覆盖率 | 当前覆盖率 |
|------|-----------|-----------|
| `iot-gateway` | 75%+ | 🎯 待测量 |
| `iot-biz` | 80%+ | 🎯 待测量 |
| `iot-core` | 85%+ | 🎯 待测量 |
| **整体** | **80%+** | 🎯 **待测量** |

---

## 待完成测试

### Week 3-4 测试规划

- [ ] 视频预览组件测试
- [ ] 设备配置界面测试
- [ ] 告警通知测试
- [ ] 数据分析大屏测试
- [ ] 性能测试
- [ ] 压力测试

---

## 常见问题

### Q: 测试运行很慢怎么办？

A: 
1. 使用 `-DskipTests` 跳过测试（仅开发时）
2. 并行运行测试：`mvn test -T 4`
3. 只运行失败的测试：`mvn test -Dsurefire.rerunFailingTestsCount=1`

### Q: Mock对象不生效？

A:
1. 确保使用 `@ExtendWith(MockitoExtension.class)`
2. 确保Mock对象有 `@Mock` 注解
3. 确保被测对象有 `@InjectMocks` 注解

### Q: 集成测试数据库连接失败？

A:
1. 检查 `application-test.yaml` 配置
2. 确保H2依赖已添加
3. 使用 `@ActiveProfiles("test")` 激活测试配置

---

## 贡献指南

### 添加新测试

1. 在对应模块的 `src/test/java` 目录创建测试类
2. 遵循命名规范：`XXXTest.java` 或 `XXXIntegrationTest.java`
3. 确保测试覆盖主要业务逻辑和异常场景
4. 运行测试确保通过：`mvn test -Dtest=YourNewTest`

### 代码审查清单

- [ ] 测试命名清晰，说明测试场景
- [ ] 使用AAA模式组织测试代码
- [ ] Mock对象使用合理，不过度Mock
- [ ] 断言充分，覆盖正常和异常场景
- [ ] 测试数据独立，不依赖外部环境
- [ ] 测试通过且稳定（不是偶尔失败）

---

## 联系方式

测试相关问题请联系：
- 项目负责人：长辉信息科技有限公司
- 技术支持：[GitHub Issues](https://github.com/your-repo/issues)

---

**最后更新**: 2025-10-26  
**文档版本**: 1.0.0













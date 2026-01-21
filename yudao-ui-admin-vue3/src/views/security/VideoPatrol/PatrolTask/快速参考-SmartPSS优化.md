# SmartPSS Plus 优化快速参考

## 🎯 核心改动一览

### 1️⃣ 对话框配置
```vue
<el-dialog
  width="92%"      ← 从 90% 增加
  top="5vh"        ← 新增，避免贴边
>
```

### 2️⃣ 顶部表单
```vue
<!-- 标签添加冒号 -->
<el-form-item label="任务名称:">     ← 添加 :
<el-form-item label="任务专属时间:">  ← 添加 :

<!-- 数字输入框去掉箭头 -->
<el-input-number :controls="false" />  ← 新增属性

<!-- 按钮右对齐 -->
<div style="margin-left: auto;">  ← 新增
  <el-button type="primary">确定</el-button>
  <el-button>取消</el-button>
</div>
```

### 3️⃣ 设备树选项
```vue
<!-- 改名更通用 -->
<el-option label="设备树" value="nvr" />  ← 从"NVR"改为"设备树"
```

### 4️⃣ 视频格子
```vue
<!-- 空格子添加提示 -->
<div class="empty-hint">
  <Icon icon="ep:plus" :size="24" />
  <span>拖拽通道到此处</span>  ← 新增提示文字
</div>

<!-- 通道数改为浮动徽章 -->
<div class="channel-count-badge">  ← 新增徽章样式
  已绑定 {{ cell.channels.length }} 个通道
</div>
```

### 5️⃣ 关键样式
```scss
// 徽章样式
.channel-count-badge {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);  ← 半透明黑色
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  z-index: 1;
}

// 配置表单
.config-form {
  display: flex;
  align-items: center;
  
  :deep(.el-form-item__label) {
    color: #606266;
    font-weight: normal;  ← 非加粗
  }
}
```

## 📋 检查清单

使用前请确认：

- [ ] 对话框宽度为 92%
- [ ] 对话框顶部间距 5vh
- [ ] 标签后有冒号（:）
- [ ] 数字输入框无上下箭头
- [ ] 按钮无图标
- [ ] 按钮在右侧
- [ ] 设备树选项为"设备树"
- [ ] 空格子有提示文字
- [ ] 通道数为浮动徽章
- [ ] 徽章背景半透明

## 🎨 颜色速查

```scss
// 文字
#303133  主要文字
#606266  常规文字
#909399  次要文字

// 边框
#dcdfe6  基础边框
#e4e7ed  浅色边框

// 背景
#ffffff  白色
#f5f5f5  浅灰
#2b2b2b  深灰（视频区）
#1a1a1a  更深（视频格子）

// 主题色
#409eff  主题蓝
```

## 🚀 测试要点

1. **打开对话框**
   - 点击"编辑任务 (SmartPSS 风格)"按钮
   - 检查对话框尺寸和位置

2. **顶部配置**
   - 检查标签冒号
   - 检查输入框宽度
   - 检查按钮位置

3. **设备树**
   - 检查下拉选项文字
   - 测试搜索功能
   - 测试节点展开

4. **视频格子**
   - 检查空格子提示
   - 拖拽设备到格子
   - 检查徽章显示

5. **通道列表**
   - 检查表格显示
   - 测试编辑功能
   - 测试排序功能

## 📝 常见问题

**Q: 徽章不显示？**
A: 检查 CSS 中 `position: absolute` 和 `z-index: 1`

**Q: 按钮没有右对齐？**
A: 确保使用 `margin-left: auto`

**Q: 数字输入框还有箭头？**
A: 添加 `:controls="false"` 属性

**Q: 拖拽没反应？**
A: 检查 `draggable="true"` 和拖拽事件绑定

## 🔗 相关文档

- [SmartPSS风格任务编辑说明.md](./SmartPSS风格任务编辑说明.md) - 完整使用指南
- [SmartPSS优化细节.md](./SmartPSS优化细节.md) - 详细优化说明
- [界面改造对比.md](./界面改造对比.md) - 改造前后对比

---

**版本**: v1.1  
**更新**: 2025-11-19  
**状态**: ✅ 已优化

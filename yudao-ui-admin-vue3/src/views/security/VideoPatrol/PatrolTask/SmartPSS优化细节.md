# SmartPSS Plus 界面优化细节

## 📸 基于官方截图的优化

根据大华 SmartPSS Plus 官方界面截图，我们进行了以下细节优化：

### ✅ 已优化的细节

#### 1. **对话框尺寸调整**
```vue
width="92%"  // 从 90% 调整为 92%，更宽的显示区域
top="5vh"    // 添加顶部间距，避免贴边
```

#### 2. **顶部配置区优化**

**调整前：**
- 标签宽度：80px
- 按钮带图标
- 间距不够统一

**调整后：**
```vue
<el-form :model="formData" label-width="100px" inline class="config-form">
  <el-form-item label="任务名称:">
    <el-input v-model="formData.taskName" style="width: 180px;" />
  </el-form-item>
  <el-form-item label="任务专属时间:" style="margin-left: 20px;">
    <el-checkbox v-model="formData.dedicatedTime" />
    <el-input-number 
      v-model="formData.duration" 
      :controls="false"  // 隐藏上下箭头
      style="width: 80px;" 
    />
    <span style="margin-left: 6px; color: #606266;">秒</span>
  </el-form-item>
  <div style="margin-left: auto;">
    <el-button type="primary">确定</el-button>
    <el-button>取消</el-button>
  </div>
</el-form>
```

**优化点：**
- ✅ 标签后添加冒号 `:`
- ✅ 标签宽度增加到 100px
- ✅ 输入框宽度调整（任务名称 180px，时长 80px）
- ✅ 数字输入框隐藏上下箭头
- ✅ 按钮移除图标，更简洁
- ✅ 按钮右对齐（`margin-left: auto`）
- ✅ 统一间距和颜色

#### 3. **左侧设备树优化**

**调整前：**
```vue
<el-option label="组织树" value="organization" />
<el-option label="NVR" value="nvr" />
```

**调整后：**
```vue
<el-select placeholder="选择视图">
  <el-option label="组织树" value="organization" />
  <el-option label="设备树" value="nvr" />  // NVR 改为"设备树"
</el-select>
```

**优化点：**
- ✅ 添加 placeholder 提示
- ✅ "NVR" 改为更通用的"设备树"
- ✅ 保持与 SmartPSS Plus 一致的术语

#### 4. **视频格子优化**

**调整前：**
- 空格子只显示加号图标
- "已绑定 X 个通道"文字居中显示

**调整后：**
```vue
<!-- 空格子 -->
<div class="empty-cell">
  <div class="empty-hint">
    <Icon icon="ep:plus" :size="24" style="color: #999;" />
    <span style="color: #999; font-size: 12px; margin-top: 8px;">
      拖拽通道到此处
    </span>
  </div>
</div>

<!-- 有通道的格子 -->
<div class="channel-info">
  <div class="channel-count-badge">已绑定 {{ cell.channels.length }} 个通道</div>
  <div class="channel-preview">
    <img v-if="cell.snapshot" :src="cell.snapshot" />
    <div v-else class="preview-placeholder">
      <Icon icon="ep:video-camera" :size="40" />
    </div>
  </div>
</div>
```

**样式优化：**
```scss
.channel-count-badge {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);  // 半透明黑色背景
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  white-space: nowrap;
  z-index: 1;
}
```

**优化点：**
- ✅ 空格子添加提示文字"拖拽通道到此处"
- ✅ "已绑定 X 个通道"改为浮动徽章样式
- ✅ 徽章使用半透明背景，更专业
- ✅ 徽章位于格子顶部居中
- ✅ 预览区域自适应剩余空间

#### 5. **样式细节优化**

```scss
// 顶部配置区
.top-config {
  padding: 14px 20px;  // 从 16px 调整为 14px
  border-bottom: 1px solid #dcdfe6;  // 使用 Element Plus 标准颜色
  
  .config-form {
    display: flex;
    align-items: center;
    
    :deep(.el-form-item__label) {
      color: #606266;  // 标准文字颜色
      font-weight: normal;  // 非加粗
    }
  }
}

// 视频格子
.video-cell {
  background: #1a1a1a;
  border: 2px dashed #444;  // 虚线边框
  
  &.has-channel {
    border-style: solid;  // 有通道时改为实线
    border-color: #409eff;  // 蓝色高亮
  }
  
  &:hover {
    border-color: #666;  // 悬停时边框变亮
  }
}
```

### 📊 对比表格

| 项目 | 优化前 | 优化后 | 说明 |
|------|--------|--------|------|
| **对话框宽度** | 90% | 92% | 更宽的显示区域 |
| **对话框顶部** | 默认 | 5vh | 避免贴边 |
| **标签文字** | 任务名称 | 任务名称: | 添加冒号 |
| **标签宽度** | 80px | 100px | 更统一的对齐 |
| **数字输入框** | 有箭头 | 无箭头 | 更简洁 |
| **按钮图标** | 有 | 无 | 更简洁 |
| **按钮位置** | 左侧 | 右侧 | 更符合习惯 |
| **设备树选项** | NVR | 设备树 | 更通用的术语 |
| **空格子提示** | 仅图标 | 图标+文字 | 更友好 |
| **通道数显示** | 居中文字 | 浮动徽章 | 更专业 |
| **徽章背景** | 无 | 半透明黑色 | 更清晰 |

### 🎨 颜色规范

根据 SmartPSS Plus 和 Element Plus 标准：

```scss
// 主色调
$primary-color: #409eff;      // 主题蓝色
$text-primary: #303133;       // 主要文字
$text-regular: #606266;       // 常规文字
$text-secondary: #909399;     // 次要文字
$text-placeholder: #c0c4cc;   // 占位文字

// 边框
$border-base: #dcdfe6;        // 基础边框
$border-light: #e4e7ed;       // 浅色边框
$border-lighter: #ebeef5;     // 更浅边框
$border-extra-light: #f2f6fc; // 极浅边框

// 背景
$bg-white: #ffffff;           // 白色背景
$bg-light: #f5f5f5;          // 浅色背景
$bg-dark: #2b2b2b;           // 深色背景（视频区）
$bg-darker: #1a1a1a;         // 更深背景（视频格子）

// 视频区特殊颜色
$video-border-empty: #444;    // 空格子边框
$video-border-hover: #666;    // 悬停边框
$video-border-active: #409eff; // 激活边框
```

### 📐 间距规范

```scss
// 内边距
$padding-xs: 4px;
$padding-sm: 8px;
$padding-md: 12px;
$padding-lg: 16px;
$padding-xl: 20px;

// 外边距
$margin-xs: 4px;
$margin-sm: 8px;
$margin-md: 12px;
$margin-lg: 16px;
$margin-xl: 20px;

// 间隙
$gap-xs: 4px;
$gap-sm: 8px;
$gap-md: 12px;
$gap-lg: 16px;
```

### 🔍 细节对比

#### 顶部配置区

**SmartPSS Plus 截图特征：**
- 标签后有冒号
- 输入框紧凑排列
- 按钮在右侧
- 整体高度较小

**我们的实现：**
```vue
<div class="top-config">
  <el-form inline class="config-form">
    <!-- 左侧配置项 -->
    <el-form-item label="任务名称:">...</el-form-item>
    <el-form-item label="任务专属时间:">...</el-form-item>
    
    <!-- 右侧按钮 -->
    <div style="margin-left: auto;">
      <el-button type="primary">确定</el-button>
      <el-button>取消</el-button>
    </div>
  </el-form>
</div>
```

#### 视频格子

**SmartPSS Plus 截图特征：**
- 深色背景 (#2b2b2b)
- 格子间有间隙
- "已绑定 X 个通道"文字在顶部
- 空格子有虚线边框

**我们的实现：**
```scss
.video-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 8px;  // 格子间隙
  
  .video-cell {
    background: #1a1a1a;
    border: 2px dashed #444;
    min-height: 200px;
    
    .channel-count-badge {
      position: absolute;
      top: 12px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(0, 0, 0, 0.6);
      // ...
    }
  }
}
```

### ✨ 用户体验提升

1. **视觉一致性**
   - 与 SmartPSS Plus 保持高度一致
   - 用户无需学习新界面
   - 降低使用门槛

2. **操作便捷性**
   - 拖拽提示更明确
   - 按钮位置符合习惯
   - 视觉反馈更清晰

3. **专业感**
   - 深色视频区域
   - 浮动徽章设计
   - 细腻的颜色过渡

### 🚀 下一步优化建议

1. **功能完善**
   - [ ] 实现实际的 NVR 数据加载
   - [ ] 添加视频截图预览
   - [ ] 实现通道预览播放
   - [ ] 添加场景树和预案功能

2. **交互优化**
   - [ ] 添加拖拽时的视觉反馈
   - [ ] 实现格子右键菜单
   - [ ] 添加键盘快捷键支持
   - [ ] 实现表格行拖拽排序

3. **性能优化**
   - [ ] 图片懒加载
   - [ ] 虚拟滚动优化
   - [ ] 数据缓存策略

### 📝 总结

通过参考 SmartPSS Plus 官方界面截图，我们完成了以下优化：

✅ **布局优化**：调整对话框尺寸和间距  
✅ **样式优化**：统一颜色、字体、间距规范  
✅ **交互优化**：改进拖拽提示、按钮布局  
✅ **视觉优化**：浮动徽章、半透明背景  
✅ **术语优化**：使用更通用的"设备树"  

这些优化使我们的界面更接近专业桌面应用的水准，提升了用户体验和专业感。

---

**更新时间**：2025-11-19  
**版本**：v1.1  
**状态**：✅ 已完成细节优化

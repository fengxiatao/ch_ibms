<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <div class="page-header">
      <div class="header-title">
        <h2>工装配置</h2>
        <p>视频分析设备配置管理</p>
      </div>
    </div>
    <div class="content">
      <el-empty description="工装配置页面" />
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    .header-title {
      h2 { margin: 0 0 8px 0; color: #303133; font-size: 24px; }
      p { margin: 0; color: #909399; font-size: 14px; }
    }
  }
  .content {
    background: #1a1a1a;
    padding: 40px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>







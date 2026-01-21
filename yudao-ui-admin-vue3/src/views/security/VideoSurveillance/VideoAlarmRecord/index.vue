<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <div class="page-header">
      <div class="header-title">
        <h2>视频告警记录</h2>
        <p>视频监控告警记录查询</p>
      </div>
    </div>
    <div class="content">
      <el-empty description="视频告警记录页面" />
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    
    .header-title {
      h2 { 
        margin: 0 0 8px 0; 
        color: rgba(255, 255, 255, 0.9); 
        font-size: 24px;
        font-weight: 500;
      }
      p { 
        margin: 0; 
        color: rgba(255, 255, 255, 0.6); 
        font-size: 14px; 
      }
    }
  }

  .content {
    flex: 1;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 40px;
    border-radius: 8px;
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-empty) {
      .el-empty__description {
        color: rgba(255, 255, 255, 0.6);
      }
    }
  }
}
</style>







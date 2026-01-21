<!-- 无聊天对话时，在 message 区域，可以新增对话 -->
<template>
  <div class="flex flex-row justify-center w-100% h-100% dark-theme-page">
    <div class="flex flex-col justify-center">
      <div class="text-14px text-#858585">点击下方按钮，开始你的对话吧</div>
      <div class="flex flex-row justify-center mt-20px">
        <el-button type="primary" round @click="handlerNewChat">新建对话</el-button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
const emits = defineEmits(['onNewConversation'])

/** 新建 conversation 聊天对话 */
const handlerNewChat = () => {
  emits('onNewConversation')
}
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';
</style>

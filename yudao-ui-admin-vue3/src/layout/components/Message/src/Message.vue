<script lang="ts" setup>
import { formatDate } from '@/utils/formatTime'
import * as NotifyMessageApi from '@/api/system/notify/message'
import { useUserStoreWithOut } from '@/store/modules/user'
import { iotWebSocket } from '@/utils/iotWebSocket'
import { ElNotification } from 'element-plus'

defineOptions({ name: 'Message' })

const { push } = useRouter()
const userStore = useUserStoreWithOut()
const activeName = ref('notice')
const unreadCount = ref(0) // 未读消息数量
const alertCount = ref(0) // 报警事件未读数量
const list = ref<any[]>([]) // 消息列表

// 获得消息列表
const getList = async () => {
  list.value = await NotifyMessageApi.getUnreadNotifyMessageList()
  // 强制设置 unreadCount 为 0，避免小红点因为轮询太慢，不消除
  unreadCount.value = 0
}

// 获得未读消息数
const getUnreadCount = async () => {
  NotifyMessageApi.getUnreadNotifyMessageCount().then((data) => {
    unreadCount.value = data
  })
}

// 跳转我的站内信
const goMyList = () => {
  push({
    name: 'MyNotifyMessage'
  })
  // 清除报警计数
  alertCount.value = 0
}

// 计算总未读数（站内信 + 报警事件）
const totalUnreadCount = computed(() => unreadCount.value + alertCount.value)

// 处理报警事件
const handleAlertEvent = (data: any) => {
  console.log('[Message] 📢 收到报警事件:', data)
  // 增加报警计数
  alertCount.value++
  
  // 显示桌面通知
  ElNotification({
    title: '⚠️ 报警事件',
    message: data?.message || data?.alertType || '收到新的报警事件',
    type: 'warning',
    duration: 5000
  })
}

// ========== 初始化 =========
onMounted(() => {
  // 首次加载小红点
  getUnreadCount()
  // 轮询刷新小红点
  setInterval(
    () => {
      if (userStore.getIsSetUser) {
        getUnreadCount()
      } else {
        unreadCount.value = 0
      }
    },
    1000 * 60 * 2
  )

  // 监听 IoT WebSocket 的报警事件（复用已连接的 WebSocket）
  iotWebSocket.on('alert', handleAlertEvent)
  iotWebSocket.on('alarm_event', handleAlertEvent) // 兼容不同的事件类型名
})

// 组件卸载时取消监听
onUnmounted(() => {
  iotWebSocket.off('alert', handleAlertEvent)
  iotWebSocket.off('alarm_event', handleAlertEvent)
})
</script>
<template>
  <div class="message">
    <ElPopover :width="400" placement="bottom" trigger="click">
      <template #reference>
        <ElBadge :value="totalUnreadCount" :max="99" :hidden="totalUnreadCount === 0" class="item">
          <Icon :size="18" class="cursor-pointer" icon="ep:bell" @click="getList" />
        </ElBadge>
      </template>
      <ElTabs v-model="activeName">
        <ElTabPane label="我的站内信" name="notice">
          <el-scrollbar class="message-list">
            <template v-for="item in list" :key="item.id">
              <div class="message-item">
                <img alt="" class="message-icon" src="@/assets/imgs/avatar.gif" />
                <div class="message-content">
                  <span class="message-title">
                    {{ item.templateNickname }}：{{ item.templateContent }}
                  </span>
                  <span class="message-date">
                    {{ formatDate(item.createTime) }}
                  </span>
                </div>
              </div>
            </template>
          </el-scrollbar>
        </ElTabPane>
      </ElTabs>
      <!-- 更多 -->
      <div style="margin-top: 10px; text-align: right">
        <XButton preIcon="ep:view" title="查看全部" type="primary" @click="goMyList" />
      </div>
    </ElPopover>
  </div>
</template>
<style lang="scss" scoped>
.message-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 260px;
  line-height: 45px;
}

.message-list {
  display: flex;
  height: 400px;
  flex-direction: column;

  .message-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid var(--el-border-color-light);

    &:last-child {
      border: none;
    }

    .message-icon {
      width: 40px;
      height: 40px;
      margin: 0 20px 0 5px;
    }

    .message-content {
      display: flex;
      flex-direction: column;

      .message-title {
        margin-bottom: 5px;
      }

      .message-date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}
</style>

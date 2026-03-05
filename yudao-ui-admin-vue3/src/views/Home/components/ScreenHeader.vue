<template>
  <div class="screen-header">
    <div class="screen-header__left">
      <div class="screen-header__logo">
        <img src="@/assets/imgs/logo.png" alt="Logo" />
      </div>
      <div class="screen-header__title">
        <h1>长辉IBMS智慧楼宇管理平台</h1>
        <p>Changhui Intelligent Building Management System</p>
      </div>
    </div>
    
    <div class="screen-header__center">
      <div class="screen-header__decor">
        <div class="screen-header__decor-line screen-header__decor-line--left"></div>
        <div class="screen-header__decor-diamond"></div>
        <div class="screen-header__decor-line screen-header__decor-line--right"></div>
      </div>
    </div>
    
    <div class="screen-header__right">
      <div class="screen-header__weather">
        <Icon icon="ep:sunny" :size="22" color="#f59e0b" />
        <span class="screen-header__weather-temp">{{ weather.temperature }}°C</span>
        <span class="screen-header__weather-desc">{{ weather.description }}</span>
      </div>
      <div class="screen-header__divider"></div>
      <div class="screen-header__datetime">
        <div class="screen-header__date">{{ currentDate }}</div>
        <div class="screen-header__time">{{ currentTime }}</div>
      </div>
      <div class="screen-header__divider"></div>
      <div class="screen-header__user">
        <el-avatar :size="36" :src="userAvatar">
          <img src="@/assets/imgs/avatar.gif" alt="avatar" />
        </el-avatar>
        <span>{{ username }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'
import { useUserStore } from '@/store/modules/user'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

interface Props {
  weather?: {
    temperature: number
    description: string
  }
}

withDefaults(defineProps<Props>(), {
  weather: () => ({ temperature: 26, description: '晴' })
})

const userStore = useUserStore()
const userAvatar = computed(() => userStore.getUser.avatar)
const username = computed(() => userStore.getUser.nickname || '管理员')

const currentDate = ref('')
const currentTime = ref('')

const updateDateTime = () => {
  currentDate.value = dayjs().format('YYYY年MM月DD日 dddd')
  currentTime.value = dayjs().format('HH:mm:ss')
}

onMounted(() => {
  updateDateTime()
  setInterval(updateDateTime, 1000)
})
</script>

<style lang="scss" scoped>
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
  background: linear-gradient(180deg, 
    rgba(5, 20, 45, 0.98) 0%, 
    rgba(8, 28, 58, 0.95) 50%,
    rgba(3, 15, 35, 0.9) 100%
  );
  border-bottom: 1px solid rgba(0, 180, 255, 0.3);
  position: relative;
  flex-shrink: 0;
  
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, 
      transparent 0%, 
      rgba(0, 180, 255, 0.6) 20%,
      #00b4ff 50%,
      rgba(0, 180, 255, 0.6) 80%,
      transparent 100%
    );
  }
  
  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  &__logo {
    width: 40px;
    height: 40px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
  
  &__title {
    h1 {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      margin: 0;
      letter-spacing: 3px;
      text-shadow: 0 0 20px rgba(0, 180, 255, 0.5);
      background: linear-gradient(180deg, #fff 0%, #00b4ff 100%);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    
    p {
      font-size: 10px;
      color: rgba(0, 180, 255, 0.7);
      margin: 2px 0 0;
      letter-spacing: 1px;
    }
  }
  
  &__center {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
  }
  
  &__decor {
    display: flex;
    align-items: center;
    gap: 8px;
    
    &-line {
      width: 120px;
      height: 2px;
      
      &--left {
        background: linear-gradient(90deg, transparent, #00b4ff);
      }
      
      &--right {
        background: linear-gradient(90deg, #00b4ff, transparent);
      }
    }
    
    &-diamond {
      width: 12px;
      height: 12px;
      background: #00b4ff;
      transform: rotate(45deg);
      box-shadow: 0 0 15px #00b4ff;
      animation: pulse-glow 2s ease-in-out infinite;
    }
  }
  
  &__right {
    display: flex;
    align-items: center;
    gap: 24px;
  }
  
  &__weather {
    display: flex;
    align-items: center;
    gap: 6px;
    
    &-temp {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
    }
    
    &-desc {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.7);
    }
  }
  
  &__divider {
    width: 1px;
    height: 24px;
    background: linear-gradient(180deg, transparent, rgba(0, 180, 255, 0.5), transparent);
  }
  
  &__datetime {
    text-align: right;
  }
  
  &__date {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 1px;
  }
  
  &__time {
    font-size: 16px;
    font-weight: 600;
    color: #00b4ff;
    font-family: 'Consolas', monospace;
    text-shadow: 0 0 10px rgba(0, 180, 255, 0.5);
  }
  
  &__user {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: rgba(0, 180, 255, 0.1);
    border: 1px solid rgba(0, 180, 255, 0.3);
    border-radius: 16px;
    
    span {
      font-size: 12px;
      color: #fff;
    }
  }
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 15px #00b4ff;
    opacity: 1;
  }
  50% {
    box-shadow: 0 0 25px #00b4ff, 0 0 35px #00b4ff;
    opacity: 0.8;
  }
}
</style>

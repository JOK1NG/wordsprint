<template>
  <div class="welcome-section">
    <PageHeader eyebrow="WordSprint" title="学习仪表盘" :description="welcomeText">
      <template #actions>
        <el-button type="primary" @click="$emit('go-study')">
          <el-icon><EditPen /></el-icon> 开始学习
        </el-button>
        <el-button @click="$emit('go-wrong-words')">
          <el-icon><RefreshRight /></el-icon> 错题复习
        </el-button>
        <el-button @click="$emit('go-statistics')">
          <el-icon><DataAnalysis /></el-icon> 查看统计
        </el-button>
      </template>
    </PageHeader>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { EditPen, RefreshRight, DataAnalysis } from '@element-plus/icons-vue'
import PageHeader from '../common/PageHeader.vue'
import { useUserStore } from '../../stores/user'

defineEmits(['go-study', 'go-wrong-words', 'go-statistics'])

const userStore = useUserStore()

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

const welcomeText = computed(() => {
  const greeting = getGreeting()
  const displayName = userStore.userInfo?.nickname || userStore.userInfo?.username
  return displayName
    ? `${greeting}，${displayName}。先看今天状态，再继续学习。`
    : `${greeting}。先看今天状态，再继续学习。`
})
</script>

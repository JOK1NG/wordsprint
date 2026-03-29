<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">WordSprint</p>
        <h1>学习仪表盘</h1>
        <p class="page-description">{{ welcomeText }}</p>
      </div>
    </div>

    <div class="card-grid">
      <el-card shadow="never">
        <template #header>后端</template>
        <p>Spring Boot 3 + MyBatis-Plus + Redis + Spring Security</p>
      </el-card>
      <el-card shadow="never">
        <template #header>前端</template>
        <p>Vue 3 + Vite + Pinia + Vue Router + Element Plus</p>
      </el-card>
      <el-card shadow="never">
        <template #header>下一步</template>
        <p>当前已完成登录注册和用户态接入，下一步可继续联调单词卡 CRUD 与学习训练流程。</p>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const welcomeText = computed(() => {
  const displayName = userStore.userInfo?.nickname || userStore.userInfo?.username
  return displayName
    ? `欢迎回来，${displayName}。现在可以从仪表盘继续进入学习流程。`
    : '项目骨架已初始化，下一步可以直接接入登录、单词卡 CRUD 和学习训练流程。'
})
</script>

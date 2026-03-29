<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <div class="brand">WordSprint</div>
      <nav class="nav-list">
        <router-link to="/">学习仪表盘</router-link>
        <router-link to="/word-cards">我的单词卡</router-link>
        <router-link to="/study">学习训练</router-link>
      </nav>
    </aside>
    <main class="app-main">
      <header class="app-topbar">
        <div>
          <div class="topbar-title">学习仪表盘</div>
          <div class="topbar-subtitle">保持每天一点点积累。</div>
        </div>
        <div class="topbar-actions">
          <span v-if="userStore.userInfo" class="topbar-user">
            {{ userStore.userInfo.nickname || userStore.userInfo.username }}
          </span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

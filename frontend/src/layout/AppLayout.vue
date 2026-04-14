<template>
  <div class="app-shell">
    <!-- 移动端遮罩 -->
    <div
      class="sidebar-overlay"
      :class="{ 'is-open': sidebarOpen }"
      @click="sidebarOpen = false"
    />

    <!-- 汉堡菜单按钮 -->
    <button class="hamburger-btn" @click="sidebarOpen = !sidebarOpen">
      <el-icon><Fold v-if="sidebarOpen" /><Expand v-else /></el-icon>
    </button>

    <!-- 侧边栏 -->
    <aside class="app-sidebar" :class="{ 'is-open': sidebarOpen }">
      <div class="brand">WordSprint</div>

      <nav class="nav-list">
        <SidebarItem to="/dashboard" :icon="Odometer" label="学习仪表盘" @click="closeSidebar" />
        <SidebarItem to="/word-cards" :icon="Collection" label="我的单词卡" @click="closeSidebar" />
        <SidebarItem to="/study" :icon="EditPen" label="学习训练" @click="closeSidebar" />
        <SidebarItem to="/wrong-words" :icon="DocumentRemove" label="错题本" @click="closeSidebar" />
        <SidebarItem to="/statistics" :icon="DataLine" label="学习统计" @click="closeSidebar" />
        <SidebarItem to="/public-library" :icon="Reading" label="公共词库" @click="closeSidebar" />
        <SidebarItem to="/rank" :icon="Trophy" label="排行榜" @click="closeSidebar" />
        <SidebarItem to="/profile" :icon="User" label="个人中心" @click="closeSidebar" />
      </nav>

      <!-- 底部用户区 -->
      <div class="sidebar-user">
        <el-avatar :size="36" :src="userStore.userInfo?.avatar || undefined">
          {{ avatarFallback }}
        </el-avatar>
        <div class="sidebar-user-info">
          <div class="sidebar-user-name">{{ displayName }}</div>
          <div class="sidebar-user-role">{{ roleLabel }}</div>
        </div>
        <el-button text size="small" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="app-main">
      <header class="app-topbar">
        <div>
          <div class="topbar-title">{{ topbarTitle }}</div>
          <div class="topbar-subtitle">{{ topbarSubtitle }}</div>
        </div>
      </header>

      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Odometer,
  Collection,
  EditPen,
  DocumentRemove,
  DataLine,
  Reading,
  Trophy,
  User,
  SwitchButton,
  Fold,
  Expand,
} from '@element-plus/icons-vue'

import SidebarItem from './SidebarItem.vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)

function closeSidebar() {
  sidebarOpen.value = false
}

const displayName = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
})

const avatarFallback = computed(() => {
  return (displayName.value || '').slice(0, 1).toUpperCase()
})

const roleLabel = computed(() => {
  return userStore.userInfo?.role === 'ADMIN' ? '管理员' : '学习者'
})

const routeMetaMap = {
  dashboard: {
    title: '学习仪表盘',
    subtitle: '保持每天一点点积累。',
  },
  wordCardList: {
    title: '我的单词卡',
    subtitle: '管理你的个人词库，支持搜索、编辑与导入。',
  },
  wordCardCreate: {
    title: '新增单词卡',
    subtitle: '补充新词，逐步扩展你的学习词库。',
  },
  wordCardEdit: {
    title: '编辑单词卡',
    subtitle: '修正词义、例句与标签，保持词库准确。',
  },
  studyTraining: {
    title: '学习训练',
    subtitle: '按模式刷题并即时获得反馈，持续提升熟练度。',
  },
  wrongWordList: {
    title: '错题本',
    subtitle: '聚焦薄弱项，针对性复习并及时纠正。',
  },
  rank: {
    title: '排行榜',
    subtitle: '查看你的积分和连续学习排名。',
  },
  statistics: {
    title: '学习统计',
    subtitle: '回顾你的学习投入、正确率与阶段趋势。',
  },
  publicLibrary: {
    title: '公共词库',
    subtitle: '从公共词库挑选词条，一键加入你的单词卡。',
  },
  profile: {
    title: '个人中心',
    subtitle: '维护你的基础资料，保持账号信息最新。',
  },
}

const topbarTitle = computed(() => routeMetaMap[route.name]?.title || routeMetaMap.dashboard.title)
const topbarSubtitle = computed(() => routeMetaMap[route.name]?.subtitle || routeMetaMap.dashboard.subtitle)

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await userStore.logout()
  } finally {
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

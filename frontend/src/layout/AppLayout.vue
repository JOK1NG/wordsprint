<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <div class="brand">WordSprint</div>
      <nav class="nav-list">
        <router-link to="/">学习仪表盘</router-link>
        <router-link to="/word-cards">我的单词卡</router-link>
        <router-link to="/study">学习训练</router-link>
        <router-link to="/wrong-words">错题本</router-link>
        <router-link to="/statistics">学习统计</router-link>
        <router-link to="/public-library">公共词库</router-link>
        <router-link to="/rank">🏆 排行榜</router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>
    </aside>
    <main class="app-main">
      <header class="app-topbar">
        <div>
          <div class="topbar-title">{{ topbarTitle }}</div>
          <div class="topbar-subtitle">{{ topbarSubtitle }}</div>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<template>
  <section class="page-section">
    <PageHeader eyebrow="Rankings" title="排行榜" description="看看谁是学习达人，向优秀看齐！" />

    <el-card shadow="never" class="rank-card">
      <el-alert
        v-if="errorText"
        :title="errorText"
        type="error"
        :closable="false"
        show-icon
        class="load-error-alert"
      >
        <template #default>
          <el-button link type="primary" @click="loadRankData">重试加载</el-button>
        </template>
      </el-alert>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="rank-tabs">
        <el-tab-pane label="积分榜" name="points" />
        <el-tab-pane label="连续打卡榜" name="streak" />
      </el-tabs>

      <MyRankCard :rank="myRank" :type="activeTab" />
      <RankTable :list="rankList" :loading="loading" :type="activeTab" />
    </el-card>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPointsRank, getStreakRank } from '../../api/rank'
import { useUserStore } from '../../stores/user'
import { extractErrorMessage } from '../../utils/error'

import PageHeader from '../../components/common/PageHeader.vue'
import MyRankCard from '../../components/rank/MyRankCard.vue'
import RankTable from '../../components/rank/RankTable.vue'

const userStore = useUserStore()

const activeTab = ref('points')
const loading = ref(false)
const rankList = ref([])
const myRank = ref(null)
const errorText = ref('')

const loadRankData = async () => {
  loading.value = true
  errorText.value = ''
  try {
    const res = activeTab.value === 'points'
      ? await getPointsRank(20)
      : await getStreakRank(20)

    if (res.code === 200) {
      rankList.value = res.data?.list || []
      if (res.data?.myRank !== undefined && res.data.myScore !== undefined) {
        myRank.value = {
          rank: res.data.myRank,
          score: res.data.myScore,
          nickname: userStore.userInfo?.nickname,
          username: userStore.userInfo?.username,
          avatar: userStore.userInfo?.avatar,
        }
      } else {
        myRank.value = null
      }
    } else {
      rankList.value = []
      myRank.value = null
      errorText.value = res.message || '获取榜单失败'
    }
  } catch (error) {
    rankList.value = []
    myRank.value = null
    errorText.value = extractErrorMessage(error, '获取榜单失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => loadRankData()

onMounted(() => loadRankData())
</script>

<style scoped>
.rank-card {
  min-height: 500px;
}

.rank-tabs {
  margin-bottom: 20px;
}

.load-error-alert {
  margin-bottom: 12px;
}
</style>

<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">WordSprint</p>
        <h1>学习仪表盘</h1>
        <p class="page-description">{{ welcomeText }}</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="goToStudy">开始学习</el-button>
        <el-button @click="goToWrongWords">错题复习</el-button>
        <el-button @click="goToStatistics">查看统计</el-button>
      </div>
    </div>

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="warning"
      :closable="false"
      show-icon
      class="dashboard-alert"
    />

    <div class="card-grid" v-loading="loading">
      <el-card v-for="card in summaryCards" :key="card.title" shadow="never" class="summary-card">
        <div class="summary-label">{{ card.title }}</div>
        <div class="summary-value">{{ card.value }}</div>
        <div class="summary-hint">{{ card.hint }}</div>
      </el-card>
    </div>

    <el-card shadow="never" class="trend-card" v-loading="loading">
      <template #header>
        <div class="section-header">
          <span>最近 7 天趋势</span>
          <span class="section-description">按日查看学习数、答对数和正确率</span>
        </div>
      </template>

      <el-empty v-if="!loading && trendItems.length === 0" description="最近 7 天暂无学习趋势数据" />

      <div v-else class="trend-list">
        <div v-for="item in trendItems" :key="item.key" class="trend-item">
          <div>
            <div class="trend-date">{{ item.label }}</div>
            <div class="trend-subtitle">学习 {{ item.studyCount }} 题</div>
          </div>
          <div class="trend-metrics">
            <span>答对 {{ item.correctCount }} 题</span>
            <span>{{ item.accuracyText }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="today-card" v-loading="loading">
      <template #header>
        <div class="section-header">
          <span>今日学习摘要</span>
          <span class="section-description">直接展示后端统计结果</span>
        </div>
      </template>

      <el-empty v-if="!loading && !todaySummary" description="今日摘要暂不可用" />

      <div v-else class="today-grid">
        <div class="today-item">
          <span class="today-item-label">统计日期</span>
          <span class="today-item-value">{{ todayDateText }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">打卡状态</span>
          <span class="today-item-value">{{ todayCheckinText }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">学习时长</span>
          <span class="today-item-value">{{ todayDurationText }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">今日积分</span>
          <span class="today-item-value">{{ todayPointsText }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">今日复习数</span>
          <span class="today-item-value">{{ todayReviewCount }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">今日学习目标</span>
          <span class="today-item-value">{{ dailyTargetText }}</span>
        </div>
        <div class="today-item">
          <span class="today-item-label">今日复习目标</span>
          <span class="today-item-value">{{ reviewTargetText }}</span>
        </div>
      </div>

      <div v-if="todaySummary" class="goal-progress">
        <div class="goal-progress-block">
          <div class="goal-progress-head">
            <span>学习目标达成进度</span>
            <span>{{ goalProgressText }}</span>
          </div>
          <el-progress :percentage="goalProgressPercent ?? 0" :status="goalProgressPercent === 100 ? 'success' : undefined" />
          <div class="goal-progress-hint">{{ goalProgressHint }}</div>
        </div>

        <div class="goal-progress-block">
          <div class="goal-progress-head">
            <span>复习目标达成进度</span>
            <span>{{ reviewGoalProgressText }}</span>
          </div>
          <el-progress :percentage="reviewGoalProgressPercent ?? 0" :status="reviewGoalProgressPercent === 100 ? 'success' : undefined" />
          <div class="goal-progress-hint">{{ reviewGoalProgressHint }}</div>
          <div v-if="hasPendingWrongWords" class="goal-progress-actions">
            <el-button text type="primary" @click="goToWrongWords">去错题复习</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getPointsRank, getStreakRank } from '../api/rank'
import { getStudyPlan } from '../api/studyPlan'
import { getStudyStatistics, getTodayStudySummary } from '../api/study'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorText = ref('')
const todaySummary = ref(null)
const statistics = ref(null)
const currentPoints = ref(null)
const currentStreak = ref(null)
const studyPlan = ref(null)

const welcomeText = computed(() => {
  const displayName = userStore.userInfo?.nickname || userStore.userInfo?.username
  return displayName
    ? `欢迎回来，${displayName}。先看今天状态，再继续学习。`
    : '欢迎回来。先看今天状态，再继续学习。'
})

const formatMetricValue = (value, fallback = '--') => {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

const formatPercent = (value, fallback = '--') => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return fallback
  }

  const percent = value <= 1 ? value * 100 : value
  return `${Math.round(percent)}%`
}

const formatDuration = (seconds) => {
  if (typeof seconds !== 'number' || Number.isNaN(seconds) || seconds <= 0) {
    return '--'
  }

  const minutes = Math.floor(seconds / 60)
  const remainSeconds = seconds % 60

  if (minutes === 0) {
    return `${remainSeconds} 秒`
  }

  return `${minutes} 分 ${remainSeconds} 秒`
}

const unwrapResponse = (response, fallbackMessage) => {
  if (response?.code === 200) {
    return response.data ?? null
  }

  throw new Error(response?.message || fallbackMessage)
}

const normalizeTrendItems = (data) => {
  const rawList = data?.trend || data?.dailyTrends || data?.trendList || data?.list || data?.days || []

  if (!Array.isArray(rawList)) {
    return []
  }

  return rawList
    .map((item, index) => {
      const studyCount = Number(item?.studyCount ?? item?.count ?? item?.totalCount ?? 0)
      const correctCount = Number(item?.correctCount ?? item?.correct ?? item?.rightCount ?? 0)
      const accuracy = typeof item?.accuracyRate === 'number'
        ? item.accuracyRate
        : typeof item?.accuracy === 'number'
          ? item.accuracy
        : studyCount > 0
          ? correctCount / studyCount
          : null
      const dateText = item?.date || item?.day || item?.statDate || `DAY-${index + 1}`
      const date = new Date(dateText)
      const label = Number.isNaN(date.getTime())
        ? dateText
        : date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })

      return {
        key: `${dateText}-${index}`,
        label,
        studyCount,
        correctCount,
        accuracyText: `正确率 ${formatPercent(accuracy, '0%')}`,
        sortValue: Number.isNaN(date.getTime()) ? index : date.getTime(),
      }
    })
    .sort((a, b) => a.sortValue - b.sortValue)
    .slice(-7)
}

const todayStudyCount = computed(() => formatMetricValue(todaySummary.value?.studyCount))
const todayCorrectCount = computed(() => formatMetricValue(todaySummary.value?.correctCount))
const todayAccuracyText = computed(() => formatPercent(todaySummary.value?.accuracyRate))
const todayDateText = computed(() => todaySummary.value?.date || '--')
const todayCheckinText = computed(() => {
  if (typeof todaySummary.value?.checkedIn !== 'boolean') {
    return '--'
  }

  return todaySummary.value.checkedIn ? '已打卡' : '未打卡'
})
const todayDurationText = computed(() => formatDuration(todaySummary.value?.durationSeconds))
const todayPointsText = computed(() => formatMetricValue(todaySummary.value?.pointsEarned))
const todayReviewCount = computed(() => formatMetricValue(todaySummary.value?.reviewCount))
const pendingReviewCount = computed(() => {
  if (typeof todaySummary.value?.pendingReviewCount === 'number') {
    return todaySummary.value.pendingReviewCount
  }

  if (typeof statistics.value?.pendingReviewCount === 'number') {
    return statistics.value.pendingReviewCount
  }

  return null
})

const hasPendingWrongWords = computed(() => {
  return typeof pendingReviewCount.value === 'number' && pendingReviewCount.value > 0
})

const todayStudyCountNumber = computed(() => {
  return typeof todaySummary.value?.studyCount === 'number' ? todaySummary.value.studyCount : null
})

const todayReviewCountNumber = computed(() => {
  return typeof todaySummary.value?.reviewCount === 'number' ? todaySummary.value.reviewCount : null
})

const dailyTargetCount = computed(() => {
  return typeof studyPlan.value?.dailyTargetCount === 'number' ? studyPlan.value.dailyTargetCount : null
})

const reviewTargetCount = computed(() => {
  return typeof studyPlan.value?.reviewTargetCount === 'number' ? studyPlan.value.reviewTargetCount : null
})

const dailyTargetText = computed(() => {
  if (dailyTargetCount.value === null) {
    return '--'
  }

  return `${dailyTargetCount.value} 题`
})

const reviewTargetText = computed(() => {
  if (reviewTargetCount.value === null) {
    return '--'
  }

  return `${reviewTargetCount.value} 题`
})

const goalProgressPercent = computed(() => {
  if (dailyTargetCount.value === null || dailyTargetCount.value <= 0 || todayStudyCountNumber.value === null) {
    return null
  }

  return Math.min(100, Math.round((todayStudyCountNumber.value / dailyTargetCount.value) * 100))
})

const goalProgressText = computed(() => {
  if (dailyTargetCount.value === null || todayStudyCountNumber.value === null) {
    return '--'
  }

  return `${todayStudyCountNumber.value} / ${dailyTargetCount.value}`
})

const goalProgressHint = computed(() => {
  if (dailyTargetCount.value === null || todayStudyCountNumber.value === null) {
    return '学习计划未配置，去个人中心设置目标后即可展示达成进度。'
  }

  if (todayStudyCountNumber.value >= dailyTargetCount.value) {
    return '今日学习目标已达成，继续练习可拉开排行榜差距。'
  }

  return `还差 ${dailyTargetCount.value - todayStudyCountNumber.value} 题可达成今日目标。`
})

const reviewGoalProgressPercent = computed(() => {
  if (reviewTargetCount.value === null || reviewTargetCount.value <= 0 || todayReviewCountNumber.value === null) {
    return null
  }

  return Math.min(100, Math.round((todayReviewCountNumber.value / reviewTargetCount.value) * 100))
})

const reviewGoalProgressText = computed(() => {
  if (reviewTargetCount.value === null || todayReviewCountNumber.value === null) {
    return '--'
  }

  return `${todayReviewCountNumber.value} / ${reviewTargetCount.value}`
})

const reviewGoalProgressHint = computed(() => {
  if (reviewTargetCount.value === null || todayReviewCountNumber.value === null) {
    return '学习计划未配置，去个人中心设置目标后即可展示达成进度。'
  }

  if (todayReviewCountNumber.value >= reviewTargetCount.value) {
    return '今日复习目标已达成，记忆巩固状态良好。'
  }

  return `还差 ${reviewTargetCount.value - todayReviewCountNumber.value} 题可达成今日复习目标。`
})

const summaryCards = computed(() => [
  {
    title: '今日学习数',
    value: todayStudyCount.value,
    hint: todaySummary.value ? '今日学习数据汇总' : '今日摘要暂不可用',
  },
  {
    title: '今日正确数',
    value: todayCorrectCount.value,
    hint: `正确率 ${todayAccuracyText.value}`,
  },
  {
    title: '连续学习天数',
    value: formatMetricValue(currentStreak.value),
    hint: '已累计连续学习天数',
  },
  {
    title: '当前积分',
    value: formatMetricValue(currentPoints.value),
    hint: '你的当前总积分',
  },
  {
    title: '待复习数量',
    value: formatMetricValue(pendingReviewCount.value),
    hint: '当前活跃错题数量',
  },
  {
    title: '学习目标达成',
    value: goalProgressText.value,
    hint: goalProgressPercent.value === null ? '学习计划暂不可用' : `今日进度 ${goalProgressPercent.value}%`,
  },
  {
    title: '复习目标达成',
    value: reviewGoalProgressText.value,
    hint: reviewGoalProgressPercent.value === null ? '学习计划暂不可用' : `今日进度 ${reviewGoalProgressPercent.value}%`,
  },
])

const trendItems = computed(() => normalizeTrendItems(statistics.value))

const goToStudy = () => {
  router.push('/study')
}

const goToWrongWords = () => {
  router.push('/wrong-words')
}

const goToStatistics = () => {
  router.push('/statistics')
}

const loadDashboard = async () => {
  loading.value = true
  errorText.value = ''

  const errors = []
  const [todayResult, statisticsResult, pointsResult, streakResult, studyPlanResult] = await Promise.allSettled([
    getTodayStudySummary(),
    getStudyStatistics({ rangeType: 'WEEK' }),
    getPointsRank(20),
    getStreakRank(20),
    getStudyPlan(),
  ])

  try {
    if (todayResult.status === 'fulfilled') {
      todaySummary.value = unwrapResponse(todayResult.value, '获取今日摘要失败')
    } else {
      throw todayResult.reason
    }
  } catch (error) {
    todaySummary.value = null
    errors.push(error?.message || '今日摘要加载失败')
  }

  try {
    if (statisticsResult.status === 'fulfilled') {
      statistics.value = unwrapResponse(statisticsResult.value, '获取趋势统计失败')
    } else {
      throw statisticsResult.reason
    }
  } catch (error) {
    statistics.value = null
    errors.push(error?.message || '趋势统计加载失败')
  }

  try {
    if (pointsResult.status === 'fulfilled') {
      const data = unwrapResponse(pointsResult.value, '获取积分失败')
      currentPoints.value = data?.myScore ?? null
    } else {
      throw pointsResult.reason
    }
  } catch (error) {
    currentPoints.value = null
    errors.push(error?.message || '积分加载失败')
  }

  try {
    if (streakResult.status === 'fulfilled') {
      const data = unwrapResponse(streakResult.value, '获取连续学习天数失败')
      currentStreak.value = statistics.value?.streakDays ?? data?.myScore ?? null
    } else {
      throw streakResult.reason
    }
  } catch (error) {
    currentStreak.value = statistics.value?.streakDays ?? null
    errors.push(error?.message || '连续学习天数加载失败')
  }

  try {
    if (studyPlanResult.status === 'fulfilled') {
      studyPlan.value = unwrapResponse(studyPlanResult.value, '获取学习计划失败')
    } else {
      throw studyPlanResult.reason
    }
  } catch (error) {
    studyPlan.value = null
    errors.push(error?.message || '学习计划加载失败')
  }

  if (errors.length > 0) {
    errorText.value = `部分数据加载失败：${errors[0]}`
  }

  loading.value = false
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.dashboard-alert {
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  min-height: 132px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.summary-value {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.2;
}

.summary-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.trend-card,
.today-card {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-description {
  font-size: 13px;
  color: #909399;
}

.trend-list {
  display: grid;
  gap: 12px;
}

.trend-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
}

.trend-date {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.trend-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.trend-metrics {
  display: flex;
  gap: 16px;
  color: #606266;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.goal-progress {
  margin-top: 16px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  background: #ffffff;
}

.goal-progress-block + .goal-progress-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
}

.goal-progress-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #303133;
}

.goal-progress-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.goal-progress-actions {
  margin-top: 4px;
}

.today-item {
  padding: 16px;
  border-radius: 12px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.today-item-label {
  font-size: 13px;
  color: #909399;
}

.today-item-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .page-actions {
    width: 100%;
  }

  .page-actions :deep(.el-button) {
    flex: 1;
  }

  .trend-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .trend-metrics {
    justify-content: flex-start;
  }
}
</style>

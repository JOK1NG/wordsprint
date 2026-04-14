<template>
  <section class="page-section">
    <WelcomeSection
      @go-study="router.push('/study')"
      @go-wrong-words="router.push('/wrong-words')"
      @go-statistics="router.push('/statistics')"
    />

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="warning"
      :closable="false"
      show-icon
      class="dashboard-alert"
    />

    <div class="card-grid" v-loading="loading">
      <SummaryCard
        v-for="card in summaryCards"
        :key="card.title"
        :title="card.title"
        :value="card.value"
        :hint="card.hint"
        :icon="card.icon"
        :color="card.color"
      />
    </div>

    <GoalProgressCard
      :loading="loading"
      :study-count="todayStudyCountNumber"
      :review-count="todayReviewCountNumber"
      :daily-target="dailyTargetCount"
      :review-target="reviewTargetCount"
      :pending-review-count="pendingReviewCount"
      @go-wrong-words="router.push('/wrong-words')"
    />

    <TrendList :items="trendItems" :loading="loading" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, Finished, Calendar, Star, Bell, TrendCharts, Refresh } from '@element-plus/icons-vue'

import { getPointsRank, getStreakRank } from '../api/rank'
import { getStudyPlan } from '../api/studyPlan'
import { getStudyStatistics, getTodayStudySummary } from '../api/study'
import { formatMetricValue, formatPercent, unwrapResponse } from '../utils/format'

import WelcomeSection from '../components/dashboard/WelcomeSection.vue'
import SummaryCard from '../components/common/SummaryCard.vue'
import GoalProgressCard from '../components/dashboard/GoalProgressCard.vue'
import TrendList from '../components/dashboard/TrendList.vue'

const router = useRouter()

const loading = ref(false)
const errorText = ref('')
const todaySummary = ref(null)
const statistics = ref(null)
const currentPoints = ref(null)
const currentStreak = ref(null)
const studyPlan = ref(null)

/* ── 趋势数据处理 ── */
const normalizeTrendItems = (data) => {
  const rawList = data?.trend || data?.dailyTrends || data?.trendList || data?.list || data?.days || []
  if (!Array.isArray(rawList)) return []

  return rawList
    .map((item, index) => {
      const studyCount = Number(item?.studyCount ?? item?.count ?? item?.totalCount ?? 0)
      const correctCount = Number(item?.correctCount ?? item?.correct ?? item?.rightCount ?? 0)
      const accuracy = typeof item?.accuracyRate === 'number'
        ? item.accuracyRate
        : typeof item?.accuracy === 'number'
          ? item.accuracy
          : studyCount > 0 ? correctCount / studyCount : null
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

/* ── Computed ── */
const todayStudyCountNumber = computed(() =>
  typeof todaySummary.value?.studyCount === 'number' ? todaySummary.value.studyCount : null
)
const todayReviewCountNumber = computed(() =>
  typeof todaySummary.value?.reviewCount === 'number' ? todaySummary.value.reviewCount : null
)
const dailyTargetCount = computed(() =>
  typeof studyPlan.value?.dailyTargetCount === 'number' ? studyPlan.value.dailyTargetCount : null
)
const reviewTargetCount = computed(() =>
  typeof studyPlan.value?.reviewTargetCount === 'number' ? studyPlan.value.reviewTargetCount : null
)
const pendingReviewCount = computed(() =>
  todaySummary.value?.pendingReviewCount ?? statistics.value?.pendingReviewCount ?? null
)

const goalProgressText = computed(() => {
  if (dailyTargetCount.value === null || todayStudyCountNumber.value === null) return '--'
  return `${todayStudyCountNumber.value} / ${dailyTargetCount.value}`
})
const goalProgressPercent = computed(() => {
  if (dailyTargetCount.value === null || dailyTargetCount.value <= 0 || todayStudyCountNumber.value === null) return null
  return Math.min(100, Math.round((todayStudyCountNumber.value / dailyTargetCount.value) * 100))
})
const reviewGoalProgressText = computed(() => {
  if (reviewTargetCount.value === null || todayReviewCountNumber.value === null) return '--'
  return `${todayReviewCountNumber.value} / ${reviewTargetCount.value}`
})
const reviewGoalProgressPercent = computed(() => {
  if (reviewTargetCount.value === null || reviewTargetCount.value <= 0 || todayReviewCountNumber.value === null) return null
  return Math.min(100, Math.round((todayReviewCountNumber.value / reviewTargetCount.value) * 100))
})

const summaryCards = computed(() => [
  { title: '今日学习数', value: formatMetricValue(todaySummary.value?.studyCount), hint: '今日学习数据汇总', icon: EditPen, color: '#2B6BE6' },
  { title: '今日正确数', value: formatMetricValue(todaySummary.value?.correctCount), hint: `正确率 ${formatPercent(todaySummary.value?.accuracyRate)}`, icon: Finished, color: '#22c55e' },
  { title: '连续学习天数', value: formatMetricValue(currentStreak.value), hint: '已累计连续学习天数', icon: Calendar, color: '#f59e0b' },
  { title: '当前积分', value: formatMetricValue(currentPoints.value), hint: '你的当前总积分', icon: Star, color: '#8b5cf6' },
  { title: '待复习数量', value: formatMetricValue(pendingReviewCount.value), hint: '当前活跃错题数量', icon: Bell, color: '#ef4444' },
  { title: '学习目标达成', value: goalProgressText.value, hint: goalProgressPercent.value === null ? '学习计划暂不可用' : `今日进度 ${goalProgressPercent.value}%`, icon: TrendCharts, color: '#2B6BE6' },
  { title: '复习目标达成', value: reviewGoalProgressText.value, hint: reviewGoalProgressPercent.value === null ? '学习计划暂不可用' : `今日进度 ${reviewGoalProgressPercent.value}%`, icon: Refresh, color: '#06b6d4' },
])

const trendItems = computed(() => normalizeTrendItems(statistics.value))

/* ── 数据加载 ── */
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
    todaySummary.value = todayResult.status === 'fulfilled'
      ? unwrapResponse(todayResult.value, '获取今日摘要失败')
      : (() => { throw todayResult.reason })()
  } catch (e) { todaySummary.value = null; errors.push(e?.message || '今日摘要加载失败') }

  try {
    statistics.value = statisticsResult.status === 'fulfilled'
      ? unwrapResponse(statisticsResult.value, '获取趋势统计失败')
      : (() => { throw statisticsResult.reason })()
  } catch (e) { statistics.value = null; errors.push(e?.message || '趋势统计加载失败') }

  try {
    const data = pointsResult.status === 'fulfilled'
      ? unwrapResponse(pointsResult.value, '获取积分失败')
      : (() => { throw pointsResult.reason })()
    currentPoints.value = data?.myScore ?? null
  } catch (e) { currentPoints.value = null; errors.push(e?.message || '积分加载失败') }

  try {
    const data = streakResult.status === 'fulfilled'
      ? unwrapResponse(streakResult.value, '获取连续天数失败')
      : (() => { throw streakResult.reason })()
    currentStreak.value = statistics.value?.streakDays ?? data?.myScore ?? null
  } catch (e) { currentStreak.value = statistics.value?.streakDays ?? null }

  try {
    studyPlan.value = studyPlanResult.status === 'fulfilled'
      ? unwrapResponse(studyPlanResult.value, '获取学习计划失败')
      : (() => { throw studyPlanResult.reason })()
  } catch (e) { studyPlan.value = null }

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
.dashboard-alert {
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
</style>

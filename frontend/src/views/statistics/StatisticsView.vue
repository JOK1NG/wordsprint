<template>
  <section class="page-section">
    <div class="page-header statistics-header">
      <PageHeader eyebrow="Statistics" title="学习统计" description="查看累计学习成果，并按时间范围回顾最近趋势。" />
      <el-radio-group v-model="activeRange" @change="handleRangeChange">
        <el-radio-button label="WEEK">近 7 天</el-radio-button>
        <el-radio-button label="MONTH">近 30 天</el-radio-button>
        <el-radio-button label="ALL">全部</el-radio-button>
      </el-radio-group>
    </div>

    <el-alert v-if="fallbackNotice" :title="fallbackNotice" type="info" :closable="false" show-icon />
    <el-alert v-if="errorText" :title="errorText" type="warning" :closable="false" show-icon />

    <!-- 摘要卡片网格 -->
    <div class="card-grid" v-loading="loading">
      <SummaryCard
        v-for="card in summaryCards"
        :key="card.title"
        :title="card.title"
        :value="card.value"
        :hint="card.hint"
      />
    </div>

    <!-- 趋势图 + 额外信息 -->
    <div class="statistics-content-grid">
      <TrendChart
        :items="trendItems"
        :loading="loading"
        :description="trendDescription"
        :date-range="rangeDateText"
        @retry="loadStatistics(activeRange)"
      />

      <ExtraInfoCard
        :items="extraItems"
        :loading="loading"
        @retry="loadStatistics(activeRange)"
      />
    </div>

    <!-- 熟练度分布 + 打卡日历 -->
    <div class="supplementary-grid">
      <FamiliarityChart
        :items="familiarityDistribution"
        :loading="familiarityLoading"
        :error-text="familiarityErrorText"
        @retry="loadFamiliarityDistribution"
      />

      <CheckinCalendar
        :items="calendarItems"
        :loading="calendarLoading"
        :error-text="calendarErrorText"
        :streak-days="calendarCurrentStreakDays"
        @retry="loadCheckinCalendar"
      />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import {
  getCheckinCalendar,
  getStudyFamiliarityDistribution,
  getStudyStatistics,
} from '../../api/study'
import { formatMetricValue, formatPercent, formatDuration, unwrapResponse } from '../../utils/format'

import PageHeader from '../../components/common/PageHeader.vue'
import SummaryCard from '../../components/common/SummaryCard.vue'
import TrendChart from '../../components/statistics/TrendChart.vue'
import FamiliarityChart from '../../components/statistics/FamiliarityChart.vue'
import CheckinCalendar from '../../components/statistics/CheckinCalendar.vue'
import ExtraInfoCard from '../../components/statistics/ExtraInfoCard.vue'

/* ── 状态 ── */
const RANGE_OPTIONS = ['WEEK', 'MONTH', 'ALL']
const rangeLabelMap = { WEEK: '近 7 天', MONTH: '近 30 天', ALL: '全部' }

const loading = ref(false)
const activeRange = ref('WEEK')
const appliedRange = ref('WEEK')
const statistics = ref(null)
const errorText = ref('')
const fallbackNotice = ref('')


const familiarityLoading = ref(false)
const familiarityDistribution = ref([])
const familiarityErrorText = ref('')

const calendarLoading = ref(false)
const calendarItems = ref([])
const calendarCurrentStreakDays = ref(null)
const calendarErrorText = ref('')

/* ── 数据标准化 ── */
const normalizeFamiliarityDistribution = (data) => {
  const rawList = Array.isArray(data)
    ? data
    : Array.isArray(data?.list) ? data.list
    : Array.isArray(data?.distribution) ? data.distribution
    : Array.isArray(data?.items) ? data.items
    : Array.isArray(data?.levels) ? data.levels
    : null

  if (!rawList) {
    return Array.from({ length: 6 }, (_, level) => ({
      level,
      count: Number(data?.[level] ?? data?.[`level${level}`] ?? data?.[`level_${level}`] ?? 0) || 0,
    }))
  }

  const levelMap = new Map()
  rawList.forEach((item, index) => {
    const level = Number(item?.level ?? item?.familiarityLevel ?? item?.familiarity ?? item?.value ?? index)
    const count = Number(item?.count ?? item?.total ?? item?.wordCount ?? item?.quantity ?? item?.valueCount ?? 0)
    if (Number.isFinite(level)) {
      levelMap.set(Math.min(Math.max(level, 0), 5), Number.isFinite(count) ? count : 0)
    }
  })

  return Array.from({ length: 6 }, (_, level) => ({ level, count: levelMap.get(level) ?? 0 }))
}

const normalizeCalendarDays = (list) => {
  const checkedMap = new Map()
  if (Array.isArray(list)) {
    list.forEach((item) => {
      const date = item?.date ?? item?.checkinDate ?? item?.day
      if (!date) return
      checkedMap.set(date, {
        checked: Boolean(item?.checked ?? item?.checkedIn ?? item?.hasStudy ?? item?.studied ?? false),
        studyCount: Number(item?.studyCount ?? item?.count ?? 0) || 0,
        pointsEarned: Number(item?.pointsEarned ?? item?.points ?? 0) || 0,
      })
    })
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return Array.from({ length: 30 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - (29 - index))
    const isoDate = date.toLocaleDateString('en-CA')
    const dayData = checkedMap.get(isoDate)
    return {
      date: isoDate,
      day: `${date.getDate()}`,
      weekday: date.toLocaleDateString('zh-CN', { weekday: 'short' }),
      checked: Boolean(dayData?.checked),
      isToday: isoDate === today.toLocaleDateString('en-CA'),
      meta: dayData?.checked ? `${dayData.studyCount} 题 · +${dayData.pointsEarned}` : '未学习',
    }
  })
}

/* ── Computed ── */
const summaryCards = computed(() => [
  { title: '总学习数', value: formatMetricValue(statistics.value?.totalStudied), hint: '累计完成的答题数量' },
  { title: '总正确数', value: formatMetricValue(statistics.value?.totalCorrect), hint: '累计答对题目数量' },
  { title: '正确率', value: formatPercent(statistics.value?.totalAccuracyRate), hint: '基于累计学习结果计算' },
  { title: '总学习时长', value: formatDuration(statistics.value?.rangeDurationSeconds), hint: `当前展示区间：${rangeLabelMap[appliedRange.value]}` },
  { title: '当前连续天数', value: formatMetricValue(statistics.value?.streakDays), hint: '连续学习或打卡天数' },
])

const extraItems = computed(() => [
  { label: '总积分', value: formatMetricValue(statistics.value?.totalPoints), hint: '累计积分' },
  { label: '最大连续天数', value: formatMetricValue(statistics.value?.maxStreakDays), hint: '历史最佳连续记录' },
  { label: '待复习数量', value: formatMetricValue(statistics.value?.pendingReviewCount), hint: '当前 ACTIVE 错题数量' },
  { label: '总错题数', value: formatMetricValue(statistics.value?.totalWrongCount), hint: '累计错题统计' },
])

const trendItems = computed(() => {
  const rawList = Array.isArray(statistics.value?.trend) ? statistics.value.trend : []
  return rawList.map((item, index) => {
    const dateText = item?.date || `DAY-${index + 1}`
    const date = new Date(dateText)
    const label = Number.isNaN(date.getTime()) ? dateText : date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
    return {
      key: `${dateText}-${index}`, label,
      studyCount: Number(item?.studyCount ?? 0),
      correctCount: Number(item?.correctCount ?? 0),
    }
  })
})

const rangeDateText = computed(() => {
  if (!statistics.value?.startDate || !statistics.value?.endDate) return '--'
  return `${statistics.value.startDate} 至 ${statistics.value.endDate}`
})

const trendDescription = computed(() => {
  const rangeStudyCount = formatMetricValue(statistics.value?.rangeStudyCount, 0)
  const rangeCorrectCount = formatMetricValue(statistics.value?.rangeCorrectCount, 0)
  const rangeAccuracy = formatPercent(statistics.value?.rangeAccuracyRate, '0%')
  return `${rangeLabelMap[appliedRange.value]}内学习 ${rangeStudyCount} 题，答对 ${rangeCorrectCount} 题，${rangeAccuracy}`
})

/* ── 数据加载 ── */
const loadStatistics = async (rangeType = activeRange.value) => {
  loading.value = true
  errorText.value = ''
  fallbackNotice.value = ''

  try {
    statistics.value = unwrapResponse(await getStudyStatistics({ rangeType }), '获取学习统计失败')
    appliedRange.value = rangeType
  } catch (error) {
    if (rangeType !== 'WEEK') {
      try {
        statistics.value = unwrapResponse(await getStudyStatistics({ rangeType: 'WEEK' }), '获取学习统计失败')
        appliedRange.value = 'WEEK'
        fallbackNotice.value = `${rangeLabelMap[rangeType]}暂不可用，已回退展示近 7 天统计。`
      } catch (fallbackError) {
        statistics.value = null
        errorText.value = fallbackError?.message || '获取学习统计失败'
      }
    } else {
      statistics.value = null
      errorText.value = error?.message || '获取学习统计失败'
    }
  } finally {
    loading.value = false
  }
}

const loadFamiliarityDistribution = async () => {
  familiarityLoading.value = true
  familiarityErrorText.value = ''
  try {
    const data = unwrapResponse(await getStudyFamiliarityDistribution(), '获取熟练度分布失败')
    familiarityDistribution.value = normalizeFamiliarityDistribution(data)
  } catch (error) {
    familiarityDistribution.value = []
    familiarityErrorText.value = error?.message || '获取熟练度分布失败'
  } finally {
    familiarityLoading.value = false
  }
}

const loadCheckinCalendar = async () => {
  calendarLoading.value = true
  calendarErrorText.value = ''
  try {
    const result = unwrapResponse(await getCheckinCalendar(), '获取打卡日历失败')
    calendarCurrentStreakDays.value = typeof result?.currentStreakDays === 'number' ? result.currentStreakDays : null
    calendarItems.value = normalizeCalendarDays(result?.days)
  } catch (error) {
    calendarItems.value = []
    calendarCurrentStreakDays.value = null
    calendarErrorText.value = error?.message || '获取打卡日历失败'
  } finally {
    calendarLoading.value = false
  }
}

const handleRangeChange = (value) => {
  if (!RANGE_OPTIONS.includes(value)) return
  loadStatistics(value)
}

onMounted(() => {
  loadStatistics('WEEK')
  loadFamiliarityDistribution()
  loadCheckinCalendar()
})
</script>

<style scoped>
.statistics-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.statistics-content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 20px;
}

.supplementary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 960px) {
  .statistics-content-grid,
  .supplementary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .statistics-header :deep(.el-radio-group) {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .statistics-header :deep(.el-radio-button),
  .statistics-header :deep(.el-radio-button__inner) {
    width: 100%;
  }
}
</style>

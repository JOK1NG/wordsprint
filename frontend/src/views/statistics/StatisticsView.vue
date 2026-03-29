<template>
  <section class="page-section">
    <div class="page-header statistics-header">
      <div>
        <p class="eyebrow">Statistics</p>
        <h1>学习统计</h1>
        <p class="page-description">查看累计学习成果，并按时间范围回顾最近趋势。</p>
      </div>

      <el-radio-group v-model="activeRange" @change="handleRangeChange">
        <el-radio-button label="WEEK">近 7 天</el-radio-button>
        <el-radio-button label="MONTH">近 30 天</el-radio-button>
        <el-radio-button label="ALL">全部</el-radio-button>
      </el-radio-group>
    </div>

    <el-alert
      v-if="fallbackNotice"
      :title="fallbackNotice"
      type="info"
      :closable="false"
      show-icon
    />

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="warning"
      :closable="false"
      show-icon
    />

    <div class="card-grid" v-loading="loading">
      <el-card v-for="card in summaryCards" :key="card.title" shadow="never" class="summary-card">
        <div class="summary-label">{{ card.title }}</div>
        <div class="summary-value">{{ card.value }}</div>
        <div class="summary-hint">{{ card.hint }}</div>
      </el-card>
    </div>

    <div class="statistics-content-grid">
      <el-card shadow="never" class="trend-card" v-loading="loading">
        <template #header>
          <div class="section-header">
            <div>
              <div>学习趋势</div>
              <div class="section-description">{{ trendDescription }}</div>
            </div>
            <div class="range-date">{{ rangeDateText }}</div>
          </div>
        </template>

        <el-empty v-if="!loading && trendItems.length === 0" description="当前范围暂无趋势数据">
          <el-button type="primary" @click="loadStatistics(activeRange)">重新加载</el-button>
        </el-empty>

        <div v-else class="trend-list">
          <div v-for="item in trendItems" :key="item.key" class="trend-item">
            <div class="trend-main">
              <div class="trend-date">{{ item.label }}</div>
              <div class="trend-subtitle">
                学习 {{ item.studyCount }} 题 · 答对 {{ item.correctCount }} 题 · {{ item.accuracyText }}
              </div>
            </div>
            <div class="trend-bar-wrap">
              <div class="trend-bar-track">
                <div class="trend-bar-fill" :style="{ width: item.barWidth }" />
              </div>
              <div class="trend-side-metrics">
                <span>{{ item.durationText }}</span>
                <span>+{{ item.pointsEarned }} 分</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="extra-card" v-loading="loading">
        <template #header>
          <div class="section-header">
            <span>更多信息</span>
            <span class="section-description">累计状态与复习压力</span>
          </div>
        </template>

        <el-empty v-if="!loading && !statistics" description="统计信息暂不可用">
          <el-button type="primary" @click="loadStatistics(activeRange)">重试</el-button>
        </el-empty>

        <div v-else class="extra-grid">
          <div v-for="item in extraItems" :key="item.label" class="extra-item">
            <span class="extra-label">{{ item.label }}</span>
            <span class="extra-value">{{ item.value }}</span>
            <span class="extra-hint">{{ item.hint }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <div class="supplementary-grid">
      <el-card shadow="never" class="supplementary-card" v-loading="familiarityLoading">
        <template #header>
          <div class="section-header">
            <div>
              <div>熟练度分布</div>
              <div class="section-description">按当前词卡熟练度查看掌握情况</div>
            </div>
            <span class="section-description">共 {{ familiarityTotal }} 张</span>
          </div>
        </template>

        <el-alert
          v-if="familiarityErrorText"
          :title="familiarityErrorText"
          type="warning"
          :closable="false"
          show-icon
          class="module-alert"
        />

        <el-empty v-else-if="!familiarityLoading && familiarityItems.length === 0" description="熟练度分布暂不可用">
          <el-button type="primary" @click="loadFamiliarityDistribution">重试</el-button>
        </el-empty>

        <div v-else class="familiarity-list">
          <div v-for="item in familiarityItems" :key="item.key" class="familiarity-item">
            <div class="familiarity-head">
              <div>
                <div class="familiarity-label">{{ item.label }}</div>
                <div class="familiarity-hint">{{ item.hint }}</div>
              </div>
              <div class="familiarity-metrics">
                <span>{{ item.count }} 张</span>
                <span>{{ item.percentText }}</span>
              </div>
            </div>
            <div class="familiarity-track">
              <div class="familiarity-fill" :style="{ width: item.barWidth }" />
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="supplementary-card" v-loading="calendarLoading">
        <template #header>
          <div class="section-header">
            <div>
              <div>最近 30 天打卡</div>
              <div class="section-description">高亮已学习日期，快速查看近况</div>
            </div>
            <span class="section-description">当前连续 {{ calendarStreakText }}</span>
          </div>
        </template>

        <el-alert
          v-if="calendarErrorText"
          :title="calendarErrorText"
          type="warning"
          :closable="false"
          show-icon
          class="module-alert"
        />

        <el-empty v-else-if="!calendarLoading && calendarItems.length === 0" description="最近 30 天暂无打卡记录">
          <el-button type="primary" @click="loadCheckinCalendar">重新加载</el-button>
        </el-empty>

        <div v-else>
          <div class="calendar-legend">
            <span class="calendar-legend-item">
              <span class="calendar-legend-dot" />
              已学习
            </span>
            <span class="calendar-legend-item muted">未学习</span>
          </div>

          <div class="calendar-grid">
            <div
              v-for="item in calendarItems"
              :key="item.date"
              class="calendar-day"
              :class="{
                'is-checked': item.checked,
                'is-today': item.isToday,
              }"
            >
              <span class="calendar-day-week">{{ item.weekday }}</span>
              <span class="calendar-day-number">{{ item.day }}</span>
              <span class="calendar-day-meta">{{ item.meta }}</span>
            </div>
          </div>
        </div>
      </el-card>
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

const RANGE_OPTIONS = ['WEEK', 'MONTH', 'ALL']

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
const calendarDays = ref([])
const calendarCurrentStreakDays = ref(null)
const calendarErrorText = ref('')

const FAMILIARITY_HINT_MAP = {
  0: '刚开始学习',
  1: '需要反复巩固',
  2: '已有初步印象',
  3: '进入稳定记忆',
  4: '掌握较稳定',
  5: '接近完全掌握',
}

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
  if (typeof seconds !== 'number' || Number.isNaN(seconds) || seconds < 0) {
    return '--'
  }

  if (seconds === 0) {
    return '0 秒'
  }

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainSeconds = seconds % 60

  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分`
  }

  if (minutes > 0) {
    return `${minutes} 分 ${remainSeconds} 秒`
  }

  return `${remainSeconds} 秒`
}

const unwrapResponse = (response, fallbackMessage) => {
  if (response?.code === 200) {
    return response.data ?? null
  }

  throw new Error(response?.message || fallbackMessage)
}

const normalizeFamiliarityDistribution = (data) => {
  const rawList = Array.isArray(data)
    ? data
    : Array.isArray(data?.list)
      ? data.list
      : Array.isArray(data?.distribution)
        ? data.distribution
        : Array.isArray(data?.items)
          ? data.items
          : Array.isArray(data?.levels)
            ? data.levels
            : null

  if (!rawList) {
    return Array.from({ length: 6 }, (_, level) => {
      const rawCount = data?.[level] ?? data?.[`level${level}`] ?? data?.[`level_${level}`] ?? 0
      return {
        level,
        count: Number(rawCount) || 0,
      }
    })
  }

  const levelMap = new Map()
  rawList.forEach((item, index) => {
    const level = Number(item?.level ?? item?.familiarityLevel ?? item?.familiarity ?? item?.value ?? index)
    const count = Number(item?.count ?? item?.total ?? item?.wordCount ?? item?.quantity ?? item?.valueCount ?? 0)

    if (Number.isFinite(level)) {
      levelMap.set(Math.min(Math.max(level, 0), 5), Number.isFinite(count) ? count : 0)
    }
  })

  return Array.from({ length: 6 }, (_, level) => ({
    level,
    count: levelMap.get(level) ?? 0,
  }))
}

const normalizeCalendarDays = (list) => {
  const checkedMap = new Map()

  if (Array.isArray(list)) {
    list.forEach((item) => {
      const date = item?.date ?? item?.checkinDate ?? item?.day
      if (!date) {
        return
      }

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
      meta: dayData?.checked
        ? `${dayData.studyCount} 题 · +${dayData.pointsEarned}`
        : '未学习',
      studyCount: dayData?.studyCount ?? 0,
      pointsEarned: dayData?.pointsEarned ?? 0,
    }
  })
}

const normalizeTrendItems = (data) => {
  const rawList = Array.isArray(data?.trend) ? data.trend : []

  if (rawList.length === 0) {
    return []
  }

  const maxStudyCount = rawList.reduce((max, item) => {
    const studyCount = Number(item?.studyCount ?? 0)
    return Math.max(max, Number.isFinite(studyCount) ? studyCount : 0)
  }, 0)

  return rawList.map((item, index) => {
    const studyCount = Number(item?.studyCount ?? 0)
    const correctCount = Number(item?.correctCount ?? 0)
    const pointsEarned = Number(item?.pointsEarned ?? 0)
    const dateText = item?.date || `DAY-${index + 1}`
    const date = new Date(dateText)
    const label = Number.isNaN(date.getTime())
      ? dateText
      : date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
    const barRatio = maxStudyCount > 0 ? studyCount / maxStudyCount : 0

    return {
      key: `${dateText}-${index}`,
      label,
      studyCount,
      correctCount,
      pointsEarned,
      accuracyText: `正确率 ${formatPercent(item?.accuracyRate, studyCount > 0 ? `${Math.round((correctCount / studyCount) * 100)}%` : '0%')}`,
      durationText: formatDuration(item?.durationSeconds ?? 0),
      barWidth: `${Math.max(barRatio * 100, studyCount > 0 ? 12 : 0)}%`,
    }
  })
}

const summaryCards = computed(() => [
  {
    title: '总学习数',
    value: formatMetricValue(statistics.value?.totalStudied),
    hint: '累计完成的答题数量',
  },
  {
    title: '总正确数',
    value: formatMetricValue(statistics.value?.totalCorrect),
    hint: '累计答对题目数量',
  },
  {
    title: '正确率',
    value: formatPercent(statistics.value?.totalAccuracyRate),
    hint: '基于累计学习结果计算',
  },
  {
    title: '总学习时长',
    value: formatDuration(statistics.value?.rangeDurationSeconds),
    hint: `当前展示区间：${rangeLabelMap[appliedRange.value]}`,
  },
  {
    title: '当前连续天数',
    value: formatMetricValue(statistics.value?.streakDays),
    hint: '连续学习或打卡天数',
  },
])

const extraItems = computed(() => [
  {
    label: '总积分',
    value: formatMetricValue(statistics.value?.totalPoints),
    hint: '累计积分',
  },
  {
    label: '最大连续天数',
    value: formatMetricValue(statistics.value?.maxStreakDays),
    hint: '历史最佳连续记录',
  },
  {
    label: '待复习数量',
    value: formatMetricValue(statistics.value?.pendingReviewCount),
    hint: '当前 ACTIVE 错题数量',
  },
  {
    label: '总错题数',
    value: formatMetricValue(statistics.value?.totalWrongCount),
    hint: '累计错题统计',
  },
])

const trendItems = computed(() => normalizeTrendItems(statistics.value))

const familiarityItems = computed(() => {
  const distribution = normalizeFamiliarityDistribution(familiarityDistribution.value)
  const total = distribution.reduce((sum, item) => sum + item.count, 0)

  return distribution.map((item) => {
    const percent = total > 0 ? (item.count / total) * 100 : 0

    return {
      key: `level-${item.level}`,
      label: `Lv.${item.level}`,
      hint: FAMILIARITY_HINT_MAP[item.level],
      count: item.count,
      percentText: `${Math.round(percent)}%`,
      barWidth: `${Math.max(percent, item.count > 0 ? 10 : 0)}%`,
    }
  })
})

const familiarityTotal = computed(() => familiarityItems.value.reduce((sum, item) => sum + item.count, 0))

const calendarItems = computed(() => calendarDays.value)

const calendarStreakText = computed(() => {
  if (typeof calendarCurrentStreakDays.value !== 'number') {
    return '-- 天'
  }

  return `${calendarCurrentStreakDays.value} 天`
})

const rangeLabelMap = {
  WEEK: '近 7 天',
  MONTH: '近 30 天',
  ALL: '全部',
}

const rangeDateText = computed(() => {
  if (!statistics.value?.startDate || !statistics.value?.endDate) {
    return '--'
  }

  return `${statistics.value.startDate} 至 ${statistics.value.endDate}`
})

const trendDescription = computed(() => {
  const rangeStudyCount = formatMetricValue(statistics.value?.rangeStudyCount, 0)
  const rangeCorrectCount = formatMetricValue(statistics.value?.rangeCorrectCount, 0)
  const rangeAccuracy = formatPercent(statistics.value?.rangeAccuracyRate, '0%')
  return `${rangeLabelMap[appliedRange.value]}内学习 ${rangeStudyCount} 题，答对 ${rangeCorrectCount} 题，${rangeAccuracy}`
})

const requestStatistics = async (rangeType) => {
  const response = await getStudyStatistics({ rangeType })
  return unwrapResponse(response, '获取学习统计失败')
}

const requestFamiliarityDistribution = async () => {
  const response = await getStudyFamiliarityDistribution()
  return unwrapResponse(response, '获取熟练度分布失败')
}

const requestCheckinCalendar = async (params) => {
  const response = await getCheckinCalendar(params)
  return unwrapResponse(response, '获取打卡日历失败')
}

const loadStatistics = async (rangeType = activeRange.value) => {
  loading.value = true
  errorText.value = ''
  fallbackNotice.value = ''

  try {
    statistics.value = await requestStatistics(rangeType)
    appliedRange.value = rangeType
  } catch (error) {
    if (rangeType !== 'WEEK') {
      try {
        statistics.value = await requestStatistics('WEEK')
        appliedRange.value = 'WEEK'
        fallbackNotice.value = `${rangeLabelMap[rangeType]}暂不可用，已回退展示近 7 天统计。`
      } catch (fallbackError) {
        statistics.value = null
        appliedRange.value = rangeType
        errorText.value = fallbackError?.message || '获取学习统计失败'
      }
    } else {
      statistics.value = null
      appliedRange.value = 'WEEK'
      errorText.value = error?.message || '获取学习统计失败'
    }
  } finally {
    loading.value = false
  }
}

const handleRangeChange = (value) => {
  if (!RANGE_OPTIONS.includes(value)) {
    return
  }

  loadStatistics(value)
}

const loadFamiliarityDistribution = async () => {
  familiarityLoading.value = true
  familiarityErrorText.value = ''

  try {
    familiarityDistribution.value = await requestFamiliarityDistribution()
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
    const result = await requestCheckinCalendar()
    calendarCurrentStreakDays.value = typeof result?.currentStreakDays === 'number' ? result.currentStreakDays : null
    calendarDays.value = normalizeCalendarDays(result?.days)
  } catch (error) {
    calendarDays.value = []
    calendarCurrentStreakDays.value = null
    calendarErrorText.value = error?.message || '获取打卡日历失败'
  } finally {
    calendarLoading.value = false
  }
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

.statistics-content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 20px;
}

.trend-card,
.extra-card,
.supplementary-card {
  min-height: 420px;
}

.supplementary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.module-alert {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-description,
.range-date {
  font-size: 13px;
  color: #909399;
}

.trend-list {
  display: grid;
  gap: 12px;
}

.trend-item {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(220px, 1fr);
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
}

.trend-main {
  min-width: 0;
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

.trend-bar-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.trend-bar-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #ebeef5;
  overflow: hidden;
}

.trend-bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #818cf8 0%, #4f46e5 100%);
}

.trend-side-metrics {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.extra-grid {
  display: grid;
  gap: 12px;
}

.extra-item {
  padding: 16px;
  border-radius: 12px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.extra-label {
  font-size: 13px;
  color: #909399;
}

.extra-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.extra-hint {
  font-size: 13px;
  color: #606266;
}

.familiarity-list {
  display: grid;
  gap: 14px;
}

.familiarity-item {
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
}

.familiarity-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 10px;
}

.familiarity-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.familiarity-hint {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.familiarity-metrics {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.familiarity-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #ebeef5;
  overflow: hidden;
}

.familiarity-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #34d399 0%, #059669 100%);
}

.calendar-legend {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
  font-size: 13px;
  color: #606266;
}

.calendar-legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.calendar-legend-item.muted {
  color: #909399;
}

.calendar-legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #4f46e5;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.calendar-day {
  min-height: 88px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.calendar-day.is-checked {
  border-color: #818cf8;
  background: #eef2ff;
}

.calendar-day.is-today {
  box-shadow: inset 0 0 0 1px #4f46e5;
}

.calendar-day-week {
  font-size: 12px;
  color: #909399;
}

.calendar-day-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.calendar-day-meta {
  margin-top: auto;
  font-size: 12px;
  color: #606266;
}

@media (max-width: 960px) {
  .statistics-content-grid {
    grid-template-columns: 1fr;
  }

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

  .statistics-header :deep(.el-radio-button) {
    width: 100%;
  }

  .statistics-header :deep(.el-radio-button__inner) {
    width: 100%;
  }

  .trend-item {
    grid-template-columns: 1fr;
  }

  .trend-side-metrics {
    flex-wrap: wrap;
  }

  .familiarity-head {
    flex-direction: column;
  }

  .familiarity-metrics {
    white-space: normal;
  }

  .calendar-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .calendar-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

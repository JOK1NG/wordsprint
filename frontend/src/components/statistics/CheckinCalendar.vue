<template>
  <el-card shadow="never" class="calendar-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <div>
          <div>最近 30 天打卡</div>
          <div class="section-description">高亮已学习日期，快速查看近况</div>
        </div>
        <span class="section-description">当前连续 {{ streakText }}</span>
      </div>
    </template>

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="warning"
      :closable="false"
      show-icon
      class="module-alert"
    />

    <el-empty v-else-if="!loading && items.length === 0" description="最近 30 天暂无打卡记录">
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
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
          v-for="item in items"
          :key="item.date"
          class="calendar-day"
          :class="{ 'is-checked': item.checked, 'is-today': item.isToday }"
        >
          <span class="calendar-day-week">{{ item.weekday }}</span>
          <span class="calendar-day-number">{{ item.day }}</span>
          <span class="calendar-day-meta">{{ item.meta }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorText: { type: String, default: '' },
  streakDays: { type: Number, default: null },
})

defineEmits(['retry'])

const streakText = computed(() => {
  if (typeof props.streakDays !== 'number') return '-- 天'
  return `${props.streakDays} 天`
})
</script>

<style scoped>
.calendar-card {
  min-height: 420px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  font-weight: 600;
}

.section-description {
  font-size: 13px;
  font-weight: 400;
  color: var(--ws-text-muted);
}

.module-alert {
  margin-bottom: 16px;
}

.calendar-legend {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--ws-text-body);
}

.calendar-legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.calendar-legend-item.muted {
  color: var(--ws-text-muted);
}

.calendar-legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--ws-primary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.calendar-day {
  min-height: 88px;
  padding: 12px;
  border: 1px solid rgba(43, 107, 230, 0.06);
  border-radius: var(--ws-radius-sm);
  background: var(--ws-gray-50);
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border-color var(--ws-transition-fast), background-color var(--ws-transition-fast);
}

.calendar-day.is-checked {
  border-color: var(--ws-primary-light);
  background: var(--ws-primary-lighter);
}

.calendar-day.is-today {
  box-shadow: inset 0 0 0 1px var(--ws-primary);
}

.calendar-day-week {
  font-size: 12px;
  color: var(--ws-text-muted);
}

.calendar-day-number {
  font-size: 24px;
  font-weight: 600;
  color: var(--ws-text-title);
  line-height: 1;
}

.calendar-day-meta {
  margin-top: auto;
  font-size: 12px;
  color: var(--ws-text-body);
}

@media (max-width: 768px) {
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

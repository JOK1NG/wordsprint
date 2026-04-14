<template>
  <el-card shadow="never" class="goal-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <span>今日目标进度</span>
      </div>
    </template>

    <div v-if="!hasGoals" class="goal-empty">
      <p>学习计划未配置，去个人中心设置目标后即可展示达成进度。</p>
    </div>

    <div v-else class="goal-progress">
      <div class="goal-progress-block">
        <div class="goal-progress-head">
          <span>学习目标达成</span>
          <span>{{ studyProgressText }}</span>
        </div>
        <el-progress :percentage="studyPercent" :status="studyPercent === 100 ? 'success' : undefined" />
        <div class="goal-progress-hint">{{ studyHint }}</div>
      </div>

      <div class="goal-progress-block">
        <div class="goal-progress-head">
          <span>复习目标达成</span>
          <span>{{ reviewProgressText }}</span>
        </div>
        <el-progress :percentage="reviewPercent" :status="reviewPercent === 100 ? 'success' : undefined" />
        <div class="goal-progress-hint">{{ reviewHint }}</div>
        <div v-if="hasPendingReview" class="goal-progress-actions">
          <el-button text type="primary" @click="$emit('go-wrong-words')">去错题复习</el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  loading: { type: Boolean, default: false },
  studyCount: { type: Number, default: null },
  reviewCount: { type: Number, default: null },
  dailyTarget: { type: Number, default: null },
  reviewTarget: { type: Number, default: null },
  pendingReviewCount: { type: Number, default: null },
})

defineEmits(['go-wrong-words'])

const hasGoals = computed(() => props.dailyTarget !== null || props.reviewTarget !== null)
const hasPendingReview = computed(() => typeof props.pendingReviewCount === 'number' && props.pendingReviewCount > 0)

const studyPercent = computed(() => {
  if (props.dailyTarget === null || props.dailyTarget <= 0 || props.studyCount === null) return 0
  return Math.min(100, Math.round((props.studyCount / props.dailyTarget) * 100))
})

const studyProgressText = computed(() => {
  if (props.dailyTarget === null || props.studyCount === null) return '--'
  return `${props.studyCount} / ${props.dailyTarget}`
})

const studyHint = computed(() => {
  if (props.dailyTarget === null || props.studyCount === null) return ''
  if (props.studyCount >= props.dailyTarget) return '今日学习目标已达成，继续练习可拉开排行榜差距。'
  return `还差 ${props.dailyTarget - props.studyCount} 题可达成今日目标。`
})

const reviewPercent = computed(() => {
  if (props.reviewTarget === null || props.reviewTarget <= 0 || props.reviewCount === null) return 0
  return Math.min(100, Math.round((props.reviewCount / props.reviewTarget) * 100))
})

const reviewProgressText = computed(() => {
  if (props.reviewTarget === null || props.reviewCount === null) return '--'
  return `${props.reviewCount} / ${props.reviewTarget}`
})

const reviewHint = computed(() => {
  if (props.reviewTarget === null || props.reviewCount === null) return ''
  if (props.reviewCount >= props.reviewTarget) return '今日复习目标已达成，记忆巩固状态良好。'
  return `还差 ${props.reviewTarget - props.reviewCount} 题可达成今日复习目标。`
})
</script>

<style scoped>
.goal-card {
  margin-bottom: 20px;
}

.section-header {
  font-weight: 600;
}

.goal-empty p {
  color: var(--ws-text-muted);
  font-size: 14px;
}

.goal-progress-block + .goal-progress-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed rgba(43, 107, 230, 0.08);
}

.goal-progress-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--ws-text-title);
}

.goal-progress-hint {
  margin-top: 8px;
  font-size: 13px;
  color: var(--ws-text-muted);
}

.goal-progress-actions {
  margin-top: 4px;
}
</style>

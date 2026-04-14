<template>
  <el-card shadow="never" class="summary-card">
    <div class="summary-icon-wrap" v-if="icon" :style="{ background: iconBg }">
      <el-icon :size="22" :style="{ color: iconColor }"><component :is="icon" /></el-icon>
    </div>
    <div class="summary-label">{{ title }}</div>
    <div class="summary-value">{{ value }}</div>
    <div v-if="hint" class="summary-hint">{{ hint }}</div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [String, Number], default: '--' },
  hint: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  color: { type: String, default: '' },
})

const iconBg = computed(() => {
  if (!props.color) return 'var(--ws-primary-lighter)'
  return `${props.color}18`
})

const iconColor = computed(() => {
  return props.color || 'var(--ws-primary)'
})
</script>

<style scoped>
.summary-card {
  min-height: 132px;
  position: relative;
  transition: transform var(--ws-transition-normal), box-shadow var(--ws-transition-normal);
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--ws-shadow-md);
}

.summary-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.summary-label {
  font-size: 14px;
  color: var(--ws-text-muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--ws-text-title);
  line-height: 1.2;
}

.summary-hint {
  margin-top: 10px;
  font-size: 13px;
  color: var(--ws-text-muted);
}
</style>

<template>
  <el-card shadow="never" class="session-card">
    <h3>本轮完成</h3>
    <div class="session-grid">
      <div class="session-item">
        <div class="session-label">已答题</div>
        <div class="session-value">{{ answered }}</div>
      </div>
      <div class="session-item">
        <div class="session-label">答对题</div>
        <div class="session-value">{{ correct }}</div>
      </div>
      <div class="session-item">
        <div class="session-label">正确率</div>
        <div class="session-value">{{ accuracyText }}</div>
      </div>
    </div>
    <div class="session-actions">
      <el-button type="primary" @click="$emit('restart')">再来一轮</el-button>
      <el-button @click="$emit('go-home')">返回仪表盘</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  answered: { type: Number, default: 0 },
  correct: { type: Number, default: 0 },
})

defineEmits(['restart', 'go-home'])

const accuracyText = computed(() => {
  if (props.answered === 0) return '0%'
  return `${Math.round((props.correct / props.answered) * 100)}%`
})
</script>

<style scoped>
.session-card {
  max-width: 900px;
}

.session-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.session-item {
  padding: 12px;
  border: 1px solid rgba(43, 107, 230, 0.06);
  border-radius: var(--ws-radius-sm);
}

.session-label {
  color: var(--ws-text-muted);
  font-size: 13px;
}

.session-value {
  margin-top: 6px;
  color: var(--ws-text-title);
  font-size: 26px;
  font-weight: 700;
}

.session-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

@media (max-width: 900px) {
  .session-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <el-card shadow="never" class="extra-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <span>更多信息</span>
        <span class="section-description">累计状态与复习压力</span>
      </div>
    </template>

    <el-empty v-if="!loading && items.length === 0" description="统计信息暂不可用">
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
    </el-empty>

    <div v-else class="extra-grid">
      <div v-for="item in items" :key="item.label" class="extra-item">
        <span class="extra-label">{{ item.label }}</span>
        <span class="extra-value">{{ item.value }}</span>
        <span class="extra-hint">{{ item.hint }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})

defineEmits(['retry'])
</script>

<style scoped>
.extra-card {
  min-height: 420px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  font-weight: 600;
}

.section-description {
  font-size: 13px;
  font-weight: 400;
  color: var(--ws-text-muted);
}

.extra-grid {
  display: grid;
  gap: 12px;
}

.extra-item {
  padding: 16px;
  border-radius: var(--ws-radius-sm);
  background: var(--ws-gray-50);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.extra-label {
  font-size: 13px;
  color: var(--ws-text-muted);
}

.extra-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--ws-text-title);
}

.extra-hint {
  font-size: 13px;
  color: var(--ws-text-body);
}
</style>

<template>
  <el-card shadow="never" class="trend-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <span>最近 7 天趋势</span>
        <span class="section-description">按日查看学习数、答对数和正确率</span>
      </div>
    </template>

    <el-empty v-if="!loading && items.length === 0" description="最近 7 天暂无学习趋势数据" />

    <div v-else class="trend-list">
      <div v-for="item in items" :key="item.key" class="trend-item">
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
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})
</script>

<style scoped>
.trend-card {
  margin-bottom: 20px;
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
  border: 1px solid rgba(43, 107, 230, 0.06);
  border-radius: var(--ws-radius-sm);
  background: var(--ws-gray-50);
}

.trend-date {
  font-size: 15px;
  font-weight: 600;
  color: var(--ws-text-title);
}

.trend-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ws-text-muted);
}

.trend-metrics {
  display: flex;
  gap: 16px;
  color: var(--ws-text-body);
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .trend-item {
    align-items: flex-start;
    flex-direction: column;
  }
  .trend-metrics {
    justify-content: flex-start;
  }
}
</style>

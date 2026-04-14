<template>
  <div class="page-header">
    <div>
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, default: '' },
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .page-actions {
    width: 100%;
  }
  .page-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>

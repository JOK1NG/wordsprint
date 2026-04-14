<template>
  <el-card shadow="never" class="mode-card">
    <div class="mode-row">
      <el-form inline>
        <el-form-item label="训练模式">
          <el-select v-model="mode" style="width: 220px" :disabled="disabled">
            <el-option label="看词忆义" value="WORD_TO_MEANING" />
            <el-option label="看义拼词" value="MEANING_TO_WORD" />
            <el-option label="选择题" value="CHOICE" />
            <el-option label="错题重练" value="WRONG_REVIEW" />
          </el-select>
        </el-form-item>
        <el-form-item label="题量">
          <el-input-number v-model="size" :min="1" :max="30" :disabled="disabled" />
        </el-form-item>
      </el-form>
      <el-button type="primary" :loading="loading" @click="$emit('start')">
        {{ started ? '重新开始' : '开始训练' }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
const mode = defineModel('mode', { type: String, default: 'WORD_TO_MEANING' })
const size = defineModel('size', { type: Number, default: 10 })

defineProps({
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  started: { type: Boolean, default: false },
})

defineEmits(['start'])
</script>

<style scoped>
.mode-card {
  max-width: 900px;
}

.mode-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
</style>

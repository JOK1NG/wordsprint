<template>
  <el-card shadow="never" class="question-card">
    <p class="question-mode">{{ modeLabel }}</p>
    <h2 class="question-title">{{ title }}</h2>

    <!-- 选择题选项 -->
    <template v-if="hasOptions">
      <el-radio-group v-model="answer" class="choice-list" :disabled="submitted">
        <el-radio
          v-for="option in options"
          :key="option"
          :label="option"
          border
          class="choice-item"
        >
          {{ option }}
        </el-radio>
      </el-radio-group>
    </template>

    <!-- 输入框 -->
    <el-input
      v-else
      v-model="answer"
      type="textarea"
      :rows="3"
      placeholder="请输入你的答案"
      :disabled="submitted"
    />

    <div class="action-row">
      <el-button v-if="!submitted" type="primary" :loading="submitting" @click="$emit('submit')">提交答案</el-button>
      <el-button v-else-if="!finished" type="primary" @click="$emit('next')">下一题</el-button>
    </div>

    <!-- 答题反馈 -->
    <div v-if="submitted && feedback" class="feedback-block" :class="feedback.isCorrect ? 'feedback-correct' : 'feedback-wrong'">
      <div class="feedback-icon">{{ feedback.isCorrect ? '✅' : '❌' }}</div>
      <div class="feedback-title">{{ feedback.isCorrect ? '回答正确' : '回答错误' }}</div>
      <div class="feedback-detail">正确答案：{{ feedback.correctAnswer }}</div>
      <div class="feedback-meta">熟练度 Lv.{{ feedback.familiarityLevel }} · {{ feedback.memoryStatus }}</div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const answer = defineModel('answer', { type: String, default: '' })

const props = defineProps({
  title: { type: String, default: '' },
  modeLabel: { type: String, default: '' },
  options: { type: Array, default: () => [] },
  submitted: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  finished: { type: Boolean, default: false },
  feedback: { type: Object, default: null },
})

defineEmits(['submit', 'next'])

const hasOptions = computed(() => Array.isArray(props.options) && props.options.length > 0)
</script>

<style scoped>
.question-card {
  max-width: 900px;
}

.question-mode {
  margin: 0;
  color: var(--ws-primary);
  font-size: 13px;
  font-weight: 600;
}

.question-title {
  margin: 8px 0 16px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--ws-text-title);
}

.choice-list {
  width: 100%;
  display: grid;
  gap: 10px;
}

.choice-item {
  margin-right: 0;
}

.action-row {
  margin: 16px 0;
}

.feedback-block {
  margin-top: 4px;
  padding: 20px 24px;
  border-radius: var(--ws-radius-sm);
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: feedback-in 0.3s ease;
}

.feedback-correct {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid rgba(34, 197, 94, 0.15);
}

.feedback-wrong {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.feedback-icon {
  font-size: 28px;
  line-height: 1;
}

.feedback-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ws-text-title);
}

.feedback-detail {
  font-size: 15px;
  color: var(--ws-text-body);
}

.feedback-meta {
  font-size: 13px;
  color: var(--ws-text-muted);
}

@keyframes feedback-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .question-title {
    font-size: 22px;
  }
}
</style>

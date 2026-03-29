<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Study</p>
        <h1>学习训练</h1>
        <p class="page-description">选择模式后开始训练，提交答案即可得到即时反馈。</p>
      </div>
    </div>

    <el-card shadow="never" class="mode-card">
      <div class="mode-row">
        <el-form inline>
          <el-form-item label="训练模式">
            <el-select v-model="studyMode" style="width: 220px" :disabled="submitting || loadingQuestions">
              <el-option label="看词忆义" value="WORD_TO_MEANING" />
              <el-option label="看义拼词" value="MEANING_TO_WORD" />
              <el-option label="选择题" value="CHOICE" />
              <el-option label="错题重练" value="WRONG_REVIEW" />
            </el-select>
          </el-form-item>
          <el-form-item label="题量">
            <el-input-number v-model="questionSize" :min="1" :max="30" :disabled="submitting || loadingQuestions" />
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="loadingQuestions" @click="startTraining">
          {{ started ? '重新开始' : '开始训练' }}
        </el-button>
      </div>
    </el-card>

    <el-alert
      v-if="questionErrorText"
      :title="questionErrorText"
      type="error"
      :closable="false"
      show-icon
      class="load-error-alert"
    >
      <template #default>
        <el-button link type="primary" @click="startTraining">重试获取题目</el-button>
      </template>
    </el-alert>

    <el-card v-if="!started" shadow="never">
      <el-empty description="请选择训练模式后开始" />
    </el-card>

    <el-card v-else-if="loadingQuestions" shadow="never">
      <el-skeleton :rows="4" animated />
    </el-card>

    <el-card v-else-if="questions.length === 0" shadow="never">
      <el-empty description="当前模式暂无可用题目，换个模式试试" />
    </el-card>

    <template v-else>
      <el-card shadow="never">
        <div class="progress-row">
          <span>第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <span>会话 ID：{{ sessionId }}</span>
        </div>
        <el-progress :percentage="progressPercent" :show-text="false" />
      </el-card>

      <el-card shadow="never" class="question-card">
        <p class="question-mode">{{ modeTitle }}</p>
        <h2 class="question-title">{{ questionTitle }}</h2>

        <template v-if="hasOptions">
          <el-radio-group v-model="answerContent" class="choice-list" :disabled="submitted && !isFinished">
            <el-radio
              v-for="option in currentQuestion.options"
              :key="option"
              :label="option"
              border
              class="choice-item"
            >
              {{ option }}
            </el-radio>
          </el-radio-group>
        </template>
        <el-input
          v-else
          v-model="answerContent"
          type="textarea"
          :rows="3"
          placeholder="请输入你的答案"
          :disabled="submitted && !isFinished"
        />

        <div class="action-row">
          <el-button v-if="!submitted" type="primary" :loading="submitting" @click="submitAnswer">提交答案</el-button>
          <el-button v-else-if="!isFinished" type="primary" @click="nextQuestion">下一题</el-button>
        </div>

        <el-alert
          v-if="submitted"
          :title="feedbackTitle"
          :type="feedback?.isCorrect ? 'success' : 'error'"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="feedback-body">
              <div>正确答案：{{ feedback?.correctAnswer }}</div>
              <div>熟练度：{{ feedback?.familiarityLevel }}，状态：{{ feedback?.memoryStatus }}</div>
            </div>
          </template>
        </el-alert>
      </el-card>

      <el-card v-if="isFinished" shadow="never" class="summary-card">
        <h3>本轮完成</h3>
        <div class="summary-grid">
          <div class="summary-item">
            <div class="summary-label">已答题</div>
            <div class="summary-value">{{ sessionStats.answered }}</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">答对题</div>
            <div class="summary-value">{{ sessionStats.correct }}</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">正确率</div>
            <div class="summary-value">{{ accuracyText }}</div>
          </div>
        </div>
      </el-card>
    </template>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getStudyRandom, submitStudyAnswer } from '../../api/study'
import { extractErrorMessage } from '../../utils/error'

const studyMode = ref('WORD_TO_MEANING')
const questionSize = ref(10)
const loadingQuestions = ref(false)
const submitting = ref(false)
const started = ref(false)
const submitted = ref(false)
const isFinished = ref(false)
const sessionId = ref('')
const questions = ref([])
const currentIndex = ref(0)
const answerContent = ref('')
const feedback = ref(null)
const questionStartAt = ref(0)
const questionErrorText = ref('')

const sessionStats = reactive({
  answered: 0,
  correct: 0,
})

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const hasOptions = computed(() => Array.isArray(currentQuestion.value?.options) && currentQuestion.value.options.length > 0)

const progressPercent = computed(() => {
  if (!questions.value.length) {
    return 0
  }
  return Math.round(((currentIndex.value + 1) / questions.value.length) * 100)
})

const modeTitle = computed(() => {
  const map = {
    WORD_TO_MEANING: '看单词，回忆词义',
    MEANING_TO_WORD: '看词义，拼写单词',
    CHOICE: '选择题模式',
    WRONG_REVIEW: '错题复习模式',
  }
  return map[studyMode.value] || '学习训练'
})

const questionTitle = computed(() => {
  if (!currentQuestion.value) {
    return ''
  }
  return currentQuestion.value.word || currentQuestion.value.meaning || '题目加载中'
})

const accuracyText = computed(() => {
  if (sessionStats.answered === 0) {
    return '0%'
  }
  return `${Math.round((sessionStats.correct / sessionStats.answered) * 100)}%`
})

const feedbackTitle = computed(() => (feedback.value?.isCorrect ? '回答正确' : '回答错误'))

const resetSessionData = () => {
  sessionStats.answered = 0
  sessionStats.correct = 0
  currentIndex.value = 0
  answerContent.value = ''
  feedback.value = null
  submitted.value = false
  isFinished.value = false
}

const startTraining = async () => {
  loadingQuestions.value = true
  questionErrorText.value = ''
  try {
    const res = await getStudyRandom({
      mode: studyMode.value,
      size: questionSize.value,
    })
    if (res.code !== 200) {
      questionErrorText.value = res.message || '获取题目失败'
      return
    }

    started.value = true
    resetSessionData()
    sessionId.value = res.data?.sessionId || ''
    questions.value = res.data?.questions || []

    if (questions.value.length === 0) {
      ElMessage.info('暂无可用题目')
      return
    }

    questionStartAt.value = Date.now()
  } catch (error) {
    questionErrorText.value = extractErrorMessage(error, '获取题目失败')
  } finally {
    loadingQuestions.value = false
  }
}

const submitAnswer = async () => {
  if (!currentQuestion.value) {
    return
  }
  if (!answerContent.value.trim()) {
    ElMessage.warning('请先填写答案')
    return
  }

  submitting.value = true
  try {
    const durationSeconds = Math.max(0, Math.floor((Date.now() - questionStartAt.value) / 1000))
    const res = await submitStudyAnswer({
      wordCardId: currentQuestion.value.wordCardId,
      studyMode: studyMode.value,
      answerContent: answerContent.value.trim(),
      durationSeconds,
    })

    if (res.code !== 200) {
      ElMessage.error(res.message || '提交失败')
      return
    }

    submitted.value = true
    feedback.value = res.data
    sessionStats.answered += 1
    if (res.data?.isCorrect) {
      sessionStats.correct += 1
    }
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '提交失败'))
  } finally {
    submitting.value = false
  }
}

const nextQuestion = () => {
  if (currentIndex.value >= questions.value.length - 1) {
    isFinished.value = true
    return
  }
  currentIndex.value += 1
  answerContent.value = ''
  feedback.value = null
  submitted.value = false
  questionStartAt.value = Date.now()
}
</script>

<style scoped>
.mode-card,
.question-card,
.summary-card {
  max-width: 900px;
}

.load-error-alert {
  max-width: 900px;
}

.mode-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.progress-row {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  color: #6b7280;
  font-size: 13px;
}

.question-mode {
  margin: 0;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 600;
}

.question-title {
  margin: 8px 0 16px;
  font-size: 28px;
  line-height: 1.2;
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

.feedback-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.summary-label {
  color: #6b7280;
  font-size: 13px;
}

.summary-value {
  margin-top: 6px;
  color: #111827;
  font-size: 26px;
  font-weight: 700;
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .question-title {
    font-size: 22px;
  }
}
</style>

<template>
  <section class="page-section">
    <PageHeader eyebrow="Study" title="学习训练" description="选择模式后开始训练，提交答案即可得到即时反馈。" />

    <ModeSelector
      v-model:mode="studyMode"
      v-model:size="questionSize"
      :disabled="submitting || loadingQuestions"
      :loading="loadingQuestions"
      :started="started"
      @start="startTraining"
    />

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
      <!-- 进度条 -->
      <el-card shadow="never">
        <div class="progress-row">
          <span>第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <span>会话 ID：{{ sessionId }}</span>
        </div>
        <el-progress :percentage="progressPercent" :show-text="false" />
      </el-card>

      <!-- 题目卡片 -->
      <QuestionCard
        v-model:answer="answerContent"
        :title="questionTitle"
        :mode-label="modeTitle"
        :options="currentQuestion?.options || []"
        :submitted="submitted"
        :submitting="submitting"
        :finished="isFinished"
        :feedback="feedback"
        @submit="submitAnswer"
        @next="nextQuestion"
      />

      <!-- 本轮总结 -->
      <SessionSummary
        v-if="isFinished"
        :answered="sessionStats.answered"
        :correct="sessionStats.correct"
        @restart="startTraining"
        @go-home="router.push('/')"
      />
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getStudyRandom, submitStudyAnswer } from '../../api/study'
import { extractErrorMessage } from '../../utils/error'

import PageHeader from '../../components/common/PageHeader.vue'
import ModeSelector from '../../components/study/ModeSelector.vue'
import QuestionCard from '../../components/study/QuestionCard.vue'
import SessionSummary from '../../components/study/SessionSummary.vue'

const route = useRoute()
const router = useRouter()

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

const sessionStats = reactive({ answered: 0, correct: 0 })

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

const progressPercent = computed(() => {
  if (!questions.value.length) return 0
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
  if (!currentQuestion.value) return ''
  return currentQuestion.value.word || currentQuestion.value.meaning || '题目加载中'
})

onMounted(() => {
  const mode = route.query.mode
  if (mode && ['WORD_TO_MEANING', 'MEANING_TO_WORD', 'CHOICE', 'WRONG_REVIEW'].includes(mode)) {
    studyMode.value = mode
    startTraining()
  }
})

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
    const res = await getStudyRandom({ mode: studyMode.value, size: questionSize.value })
    if (res.code !== 200) { questionErrorText.value = res.message || '获取题目失败'; return }

    started.value = true
    resetSessionData()
    sessionId.value = res.data?.sessionId || ''
    questions.value = res.data?.questions || []

    if (questions.value.length === 0) { ElMessage.info('暂无可用题目'); return }
    questionStartAt.value = Date.now()
  } catch (error) {
    questionErrorText.value = extractErrorMessage(error, '获取题目失败')
  } finally {
    loadingQuestions.value = false
  }
}

const submitAnswer = async () => {
  if (!currentQuestion.value) return
  if (!answerContent.value.trim()) { ElMessage.warning('请先填写答案'); return }

  submitting.value = true
  try {
    const durationSeconds = Math.max(0, Math.floor((Date.now() - questionStartAt.value) / 1000))
    const res = await submitStudyAnswer({
      wordCardId: currentQuestion.value.wordCardId,
      studyMode: studyMode.value,
      answerContent: answerContent.value.trim(),
      durationSeconds,
    })
    if (res.code !== 200) { ElMessage.error(res.message || '提交失败'); return }

    submitted.value = true
    feedback.value = res.data
    sessionStats.answered += 1
    if (res.data?.isCorrect) sessionStats.correct += 1
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '提交失败'))
  } finally {
    submitting.value = false
  }
}

const nextQuestion = () => {
  if (currentIndex.value >= questions.value.length - 1) { isFinished.value = true; return }
  currentIndex.value += 1
  answerContent.value = ''
  feedback.value = null
  submitted.value = false
  questionStartAt.value = Date.now()
}
</script>

<style scoped>
.load-error-alert {
  max-width: 900px;
}

.progress-row {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  color: var(--ws-text-muted);
  font-size: 13px;
}
</style>

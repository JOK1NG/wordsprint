<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Account</p>
        <h1>个人中心</h1>
        <p class="page-description">查看当前账号信息，并更新昵称与头像地址。</p>
      </div>
    </div>

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="error"
      :closable="false"
      show-icon
      class="profile-alert"
    />

    <el-card shadow="never" class="profile-card">
      <div class="profile-preview">
        <el-avatar :size="72" :src="form.avatar || undefined">{{ avatarFallback }}</el-avatar>
        <div class="profile-preview-text">
          <div class="profile-name">{{ form.nickname || userStore.userInfo?.username || '未命名用户' }}</div>
          <div class="profile-username">用户名：{{ form.username || '--' }}</div>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
        <el-form-item label="当前用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像 URL" prop="avatar">
          <el-input v-model.trim="form.avatar" placeholder="请输入头像图片地址" />
        </el-form-item>
        <div class="profile-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="profile-card plan-card">
      <template #header>
        <div class="card-header">
          <span>学习计划设置</span>
        </div>
      </template>

      <el-skeleton :loading="planLoading" animated>
        <template #template>
          <el-skeleton-item variant="text" style="width: 40%; height: 20px; margin-bottom: 20px" />
          <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 16px" />
          <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 16px" />
          <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 16px" />
          <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 16px" />
        </template>

        <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-position="top" class="profile-form">
          <el-form-item label="每日学习目标" prop="dailyTargetCount">
            <el-input-number
              v-model="planForm.dailyTargetCount"
              :min="1"
              :max="500"
              :disabled="planSubmitting"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="每日复习目标" prop="reviewTargetCount">
            <el-input-number
              v-model="planForm.reviewTargetCount"
              :min="1"
              :max="500"
              :disabled="planSubmitting"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="提醒开关" prop="reminderEnabled">
            <el-switch v-model="planForm.reminderEnabled" :disabled="planSubmitting" />
          </el-form-item>

          <el-form-item label="提醒时间" prop="reminderTime">
            <el-time-picker
              v-model="planReminderTime"
              format="HH:mm"
              value-format="HH:mm:ss"
              placeholder="请选择提醒时间"
              :disabled="planSubmitting || !planForm.reminderEnabled"
              clearable
            />
          </el-form-item>

          <div class="profile-actions">
            <el-button type="primary" :loading="planSubmitting" @click="handlePlanSubmit">保存学习计划</el-button>
          </div>
        </el-form>
      </el-skeleton>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import { getStudyPlan, updateStudyPlan } from '../../api/studyPlan'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const formRef = ref()
const planFormRef = ref()
const submitting = ref(false)
const planLoading = ref(false)
const planSubmitting = ref(false)
const errorText = ref('')
const form = reactive({
  username: userStore.userInfo?.username || '',
  nickname: userStore.userInfo?.nickname || '',
  avatar: userStore.userInfo?.avatar || '',
})
const planForm = reactive({
  dailyTargetCount: 20,
  reviewTargetCount: 10,
  reminderEnabled: false,
  reminderTime: '',
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, message: '昵称至少 2 个字符', trigger: 'blur' },
    { max: 20, message: '昵称不能超过 20 个字符', trigger: 'blur' },
  ],
  avatar: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }

        try {
          const url = new URL(value)
          if (url.protocol === 'http:' || url.protocol === 'https:') {
            callback()
            return
          }
        } catch {
          // Fall through to validation error.
        }

        callback(new Error('请输入有效的 http 或 https 图片地址'))
      },
      trigger: 'blur',
    },
  ],
}

const planRules = {
  dailyTargetCount: [
    { required: true, message: '请输入每日学习目标', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (Number.isInteger(value) && value > 0) {
          callback()
          return
        }

        callback(new Error('每日学习目标需为大于 0 的整数'))
      },
      trigger: 'change',
    },
  ],
  reviewTargetCount: [
    { required: true, message: '请输入每日复习目标', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (Number.isInteger(value) && value > 0) {
          callback()
          return
        }

        callback(new Error('每日复习目标需为大于 0 的整数'))
      },
      trigger: 'change',
    },
  ],
  reminderTime: [
    {
      validator: (_rule, value, callback) => {
        if (!planForm.reminderEnabled) {
          callback()
          return
        }

        if (value) {
          callback()
          return
        }

        callback(new Error('开启提醒后请选择提醒时间'))
      },
      trigger: 'change',
    },
  ],
}

const avatarFallback = computed(() => {
  const source = form.nickname || form.username || ''
  return source.slice(0, 1).toUpperCase()
})

const planReminderTime = computed({
  get: () => planForm.reminderTime || null,
  set: (value) => {
    planForm.reminderTime = value || ''
  },
})

watch(
  () => planForm.reminderEnabled,
  (enabled) => {
    if (!enabled) {
      planForm.reminderTime = ''
      planFormRef.value?.clearValidate?.('reminderTime')
    }
  },
)

onMounted(() => {
  loadStudyPlan()
})

function applyStudyPlan(plan = {}) {
  planForm.dailyTargetCount = Number.isInteger(plan.dailyTargetCount) ? plan.dailyTargetCount : 20
  planForm.reviewTargetCount = Number.isInteger(plan.reviewTargetCount) ? plan.reviewTargetCount : 10
  planForm.reminderEnabled = Boolean(plan.reminderEnabled)
  planForm.reminderTime = plan.reminderEnabled ? plan.reminderTime || '' : ''
}

async function loadStudyPlan() {
  planLoading.value = true
  try {
    const response = await getStudyPlan()
    applyStudyPlan(response?.data)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '学习计划加载失败，请稍后重试')
  } finally {
    planLoading.value = false
  }
}

async function handleSubmit() {
  if (submitting.value) {
    return
  }

  errorText.value = ''
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const nextUserInfo = await userStore.updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
    })
    form.username = nextUserInfo?.username || form.username
    form.nickname = nextUserInfo?.nickname || ''
    form.avatar = nextUserInfo?.avatar || ''
    ElMessage.success('个人资料已保存')
  } catch (error) {
    errorText.value = error.response?.data?.message || error.message || '保存失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

async function handlePlanSubmit() {
  if (planSubmitting.value) {
    return
  }

  const valid = await planFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  planSubmitting.value = true
  try {
    const payload = {
      dailyTargetCount: planForm.dailyTargetCount,
      reviewTargetCount: planForm.reviewTargetCount,
      reminderEnabled: planForm.reminderEnabled,
      reminderTime: planForm.reminderEnabled ? planForm.reminderTime || null : null,
    }
    const response = await updateStudyPlan(payload)
    applyStudyPlan(response?.data ?? payload)
    ElMessage.success('学习计划已保存')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '学习计划保存失败，请稍后重试')
  } finally {
    planSubmitting.value = false
  }
}
</script>

<style scoped>
.profile-alert {
  margin-bottom: 16px;
}

.profile-card {
  max-width: 720px;
}

.plan-card {
  margin-top: 16px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--ws-text-title);
}

.profile-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.profile-preview-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--ws-text-title);
}

.profile-username {
  font-size: 14px;
  color: var(--ws-text-muted);
}

.profile-form {
  max-width: 480px;
}

.profile-actions {
  display: flex;
  justify-content: flex-start;
}

@media (max-width: 640px) {
  .profile-preview {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

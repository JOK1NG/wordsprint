<template>
  <div class="auth-page">
    <!-- 装饰浮动圆 -->
    <div class="auth-decor auth-decor-1" />
    <div class="auth-decor auth-decor-2" />
    <div class="auth-decor auth-decor-3" />

    <div class="auth-card">
      <div class="auth-brand">
        <div class="auth-logo">W</div>
        <h1 class="auth-brand-name">WordSprint</h1>
        <p class="auth-brand-tagline">蔚蓝澄净 · 轻松记忆</p>
      </div>

      <p class="auth-subtitle">输入账号后即可进入学习仪表盘。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" size="large" @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-button type="primary" class="auth-submit" size="large" :loading="submitting" @click="handleSubmit">登录</el-button>
      </el-form>

      <div class="auth-footer">
        <router-link to="/register">没有账号？去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  if (submitting.value) {
    return
  }

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '登录失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #F0F7FF 0%, #E0EFFF 40%, #FFFFFF 100%);
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* ── 装饰浮动圆 ── */
.auth-decor {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  opacity: 0.12;
}

.auth-decor-1 {
  width: 420px;
  height: 420px;
  background: var(--ws-primary);
  top: -120px;
  right: -80px;
  animation: float-1 12s ease-in-out infinite;
}

.auth-decor-2 {
  width: 280px;
  height: 280px;
  background: var(--ws-primary-light);
  bottom: -60px;
  left: -40px;
  animation: float-2 15s ease-in-out infinite;
}

.auth-decor-3 {
  width: 160px;
  height: 160px;
  background: var(--ws-primary);
  top: 40%;
  left: 15%;
  animation: float-3 10s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 20px) scale(1.05); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -25px) scale(1.08); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(15px, 15px) scale(0.95); }
}

/* ── 卡片 ── */
.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--ws-radius-lg);
  box-shadow: var(--ws-shadow-lg);
  border: 1px solid rgba(43, 107, 230, 0.06);
}

/* ── 品牌区 ── */
.auth-brand {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--ws-primary) 0%, var(--ws-primary-light) 100%);
  color: white;
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 12px;
  box-shadow: 0 4px 16px rgba(43, 107, 230, 0.2);
}

.auth-brand-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--ws-text-title);
  margin: 0;
  letter-spacing: -0.01em;
}

.auth-brand-tagline {
  color: var(--ws-text-muted);
  font-size: 14px;
  margin: 4px 0 0;
}

.auth-subtitle {
  text-align: center;
  color: var(--ws-text-body);
  font-size: 15px;
  margin: 0 0 28px;
}

/* ── 提交按钮 ── */
.auth-submit {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--ws-radius-full) !important;
  margin-top: 8px;
}

/* ── 底部链接 ── */
.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.auth-footer a {
  color: var(--ws-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--ws-transition-fast);
}

.auth-footer a:hover {
  color: var(--ws-primary-light);
}

@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px;
  }
  .auth-brand-name {
    font-size: 24px;
  }
}
</style>

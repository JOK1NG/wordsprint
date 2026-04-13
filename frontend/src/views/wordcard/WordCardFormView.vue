<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Word Card</p>
        <h1>{{ isEdit ? '编辑单词卡' : '新增单词卡' }}</h1>
        <p class="page-description">{{ isEdit ? '修改单词卡信息' : '添加一个新的单词到词库' }}</p>
      </div>
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回列表
      </el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="detailLoading">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="word-card-form"
      >
        <el-form-item label="单词" prop="word">
          <el-input
            v-model="formData.word"
            placeholder="请输入单词"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="音标" prop="phonetic">
          <el-input
            v-model="formData.phonetic"
            placeholder="请输入音标，如 /əˈbændən/"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="词义" prop="meaning">
          <el-input
            v-model="formData.meaning"
            type="textarea"
            :rows="2"
            placeholder="请输入词义"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="例句" prop="exampleSentence">
          <el-input
            v-model="formData.exampleSentence"
            type="textarea"
            :rows="3"
            placeholder="请输入例句"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="formData.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入或选择标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in presetTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
          <div class="form-tip">常用标签：cet4, cet6, ielts, toefl, gre, verb, noun, adj</div>
        </el-form-item>

        <el-form-item label="公开">
          <el-switch v-model="formData.isPublic" />
          <span class="switch-text">{{ formData.isPublic ? '公开给其他用户' : '仅自己可见' }}</span>
        </el-form-item>

        <el-form-item v-if="isEdit" label="熟练度">
          <el-rate v-model="formData.familiarityLevel" disabled show-score text-color="#ff9900" />
          <span class="form-tip">熟练度会在学习过程中自动更新</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '立即创建' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createWordCard, updateWordCard, getWordCardDetail } from '../../api/wordCard'
import { extractErrorMessage } from '../../utils/error'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const detailLoading = ref(false)

const isEdit = computed(() => !!route.params.id)
const wordCardId = computed(() => route.params.id)

const presetTags = ['cet4', 'cet6', 'ielts', 'toefl', 'gre', 'verb', 'noun', 'adj', 'adv']

const formData = reactive({
  word: '',
  phonetic: '',
  meaning: '',
  exampleSentence: '',
  tags: [],
  isPublic: false,
  familiarityLevel: 0,
})

const formRules = {
  word: [
    { required: true, message: '请输入单词', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' },
  ],
  meaning: [
    { required: true, message: '请输入词义', trigger: 'blur' },
    { min: 1, max: 255, message: '长度在 1 到 255 个字符', trigger: 'blur' },
  ],
}

const loadDetail = async () => {
  if (!isEdit.value) return
  detailLoading.value = true
  try {
    const res = await getWordCardDetail(wordCardId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      formData.word = data.word || ''
      formData.phonetic = data.phonetic || ''
      formData.meaning = data.meaning || ''
      formData.exampleSentence = data.exampleSentence || ''
      formData.isPublic = data.isPublic || false
      formData.familiarityLevel = data.familiarityLevel || 0
      if (data.tags) {
        try {
          formData.tags = JSON.parse(data.tags)
        } catch {
          formData.tags = data.tags.split(',').map(t => t.trim()).filter(Boolean)
        }
      }
    } else {
      ElMessage.error(extractErrorMessage({ response: { data: res } }, '获取单词卡详情失败'))
      router.push('/word-cards')
    }
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '获取单词卡详情失败'))
    router.push('/word-cards')
  } finally {
    detailLoading.value = false
  }
}

const handleSubmit = async () => {
  if (submitting.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      word: formData.word,
      phonetic: formData.phonetic,
      meaning: formData.meaning,
      exampleSentence: formData.exampleSentence,
      tags: formData.tags,
      isPublic: formData.isPublic,
    }
    const res = isEdit.value
      ? await updateWordCard(wordCardId.value, payload)
      : await createWordCard(payload)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
      router.push('/word-cards')
    } else {
      ElMessage.error(res.message || (isEdit.value ? '修改失败' : '创建失败'))
    }
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  if (isEdit.value) {
    loadDetail()
  } else {
    formRef.value.resetFields()
    formData.tags = []
  }
}

const handleBack = () => {
  router.push('/word-cards')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.form-card {
  max-width: 700px;
}

.word-card-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.switch-text {
  margin-left: 10px;
  font-size: 13px;
  color: #606266;
}
</style>

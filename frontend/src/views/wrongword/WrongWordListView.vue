<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Wrong Words</p>
        <h1>我的错题本</h1>
        <p class="page-description">温故知新，消灭每一个错题。</p>
      </div>
      <el-button type="primary" @click="handleStartPractice">
        <el-icon><Flag /></el-icon>开始训练
      </el-button>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ activeCount }}</div>
          <div class="stat-label">活跃错题</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ totalCount }}</div>
          <div class="stat-label">累计错题</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <el-alert
        v-if="errorText"
        :title="errorText"
        type="error"
        :closable="false"
        show-icon
        class="load-error-alert"
      >
        <template #default>
          <el-button link type="primary" @click="loadData">重试加载</el-button>
        </template>
      </el-alert>

      <el-tabs v-model="currentStatus" @tab-change="handleStatusChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="活跃" name="ACTIVE" />
        <el-tab-pane label="已解决" name="RESOLVED" />
      </el-tabs>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="word" label="单词" width="150" />
        <el-table-column prop="meaning" label="词义" show-overflow-tooltip />
        <el-table-column prop="errorCount" label="错误次数" width="100" align="center" />
        <el-table-column prop="lastWrongAt" label="最近答错时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastWrongAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'RESOLVED' ? '已解决' : '活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePracticeSingle(row)">
              <el-icon><Edit /></el-icon>重练
            </el-button>
            <el-button 
              v-if="row.status === 'ACTIVE'" 
              link 
              type="danger" 
              @click="handleRemove(row)"
            >
              <el-icon><Delete /></el-icon>移除
            </el-button>
            <el-button 
              v-if="row.status === 'RESOLVED'" 
              link 
              type="success" 
              @click="handleRestore(row)"
            >
              <el-icon><RefreshLeft /></el-icon>撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <el-empty 
        v-if="!loading && list.length === 0" 
        description="当前没有错题，继续学习吧！" 
        :image-size="120"
      />
    </el-card>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Flag, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { getWrongWordList, removeWrongWord, restoreWrongWord } from '../../api/wrongWord'
import { extractErrorMessage } from '../../utils/error'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref('')
const activeCount = ref(0)
const totalCount = ref(0)
const errorText = ref('')

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const loadData = async () => {
  loading.value = true
  errorText.value = ''
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: currentStatus.value || undefined,
    }
    const res = await getWrongWordList(params)
    if (res.code === 200) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
      activeCount.value = res.data.activeCount || 0
      totalCount.value = res.data.totalCount || 0
    } else {
      list.value = []
      total.value = 0
      activeCount.value = 0
      totalCount.value = 0
      errorText.value = res.message || '获取错题列表失败'
    }
  } catch (error) {
    list.value = []
    total.value = 0
    activeCount.value = 0
    totalCount.value = 0
    errorText.value = extractErrorMessage(error, '获取错题列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleStartPractice = () => {
  router.push('/study?mode=WRONG_REVIEW')
}

const handlePracticeSingle = (row) => {
  router.push(`/study?mode=WRONG_REVIEW&wordId=${row.wordCardId}`)
}

const handleRemove = (row) => {
  ElMessageBox.confirm(
    `确定要将 "${row.word}" 从错题本中移除吗？`,
    '确认移除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await removeWrongWord(row.wordCardId)
      if (res.code === 200) {
        ElMessage.success('移除成功')
        loadData()
      } else {
        ElMessage.error(res.message || '移除失败')
      }
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '移除失败'))
    }
  }).catch(() => {})
}

const handleRestore = (row) => {
  ElMessageBox.confirm(
    `确定要将 "${row.word}" 恢复为活跃状态吗？`,
    '确认撤销',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(async () => {
    try {
      const res = await restoreWrongWord(row.wordCardId)
      if (res.code === 200) {
        ElMessage.success('撤销成功')
        loadData()
      } else {
        ElMessage.error(res.message || '撤销失败')
      }
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '撤销失败'))
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 16px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--ws-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--ws-text-muted);
  margin-top: 8px;
}

.table-card {
  min-height: 400px;
  overflow: hidden;
}

.load-error-alert {
  margin-bottom: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .table-card :deep(.el-table) {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .pagination-wrapper {
    justify-content: center;
  }
}
</style>

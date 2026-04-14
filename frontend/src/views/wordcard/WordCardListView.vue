<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Word Cards</p>
        <h1>我的单词卡</h1>
        <p class="page-description">管理你的单词卡片，支持新增、编辑、删除和搜索。</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleOpenImportDialog">
          CSV 导入
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增单词卡
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索单词或词义"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="searchForm.tag"
            placeholder="标签筛选"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.memoryStatus" placeholder="全部状态" clearable>
            <el-option label="学习中" value="LEARNING" />
            <el-option label="已掌握" value="MASTERED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-alert
        v-if="loadErrorText"
        :title="loadErrorText"
        type="error"
        :closable="false"
        show-icon
        class="load-error-alert"
      >
        <template #default>
          <el-button link type="primary" @click="loadData">重试加载</el-button>
        </template>
      </el-alert>

      <el-table :data="tableData" v-loading="loading" stripe>
        <template #empty>
          <el-empty v-if="!loadErrorText" description="还没有单词卡，点击上方按钮新增吧" :image-size="120" />
        </template>
        <el-table-column prop="word" label="单词" width="150" />
        <el-table-column prop="phonetic" label="音标" width="120" />
        <el-table-column prop="meaning" label="词义" show-overflow-tooltip />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in parseTags(row.tags)" :key="tag" size="small" class="tag-item">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="熟练度" width="100">
          <template #default="{ row }">
            <el-rate :model-value="row.familiarityLevel" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.memoryStatus === 'MASTERED' ? 'success' : 'info'" size="small">
              {{ row.memoryStatus === 'MASTERED' ? '已掌握' : '学习中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="正/错" width="90" align="center">
          <template #default="{ row }">
            <span class="stat-text">{{ row.correctCount || 0 }}/{{ row.wrongCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="importDialogVisible"
      title="CSV 导入词卡"
      width="620px"
      :close-on-click-modal="!importing"
      :close-on-press-escape="!importing"
      :show-close="!importing"
    >
      <div class="import-guidelines">
        <div class="import-guidelines-head">
          <p>文件要求：</p>
          <el-button link type="primary" @click="handleDownloadTemplate">下载 CSV 模板</el-button>
        </div>
        <ul>
          <li>仅支持 <code>.csv</code> 文件，建议 UTF-8 编码。</li>
          <li>列顺序：<code>word,meaning,phonetic,exampleSentence,tags,isPublic</code>。</li>
          <li>前两列 <code>word</code> 和 <code>meaning</code> 必填；首行表头可选。</li>
          <li><code>tags</code> 建议用 <code>|</code> 分隔，如 <code>cet4|verb</code>。</li>
        </ul>
      </div>

      <el-upload
        v-model:file-list="importFileList"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".csv,text/csv"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将 CSV 文件拖到此处，或<em>点击上传</em>
        </div>
      </el-upload>

      <div v-if="importResult" class="import-result">
        <el-alert
          :type="importResult.failedCount > 0 ? 'warning' : 'success'"
          :title="`导入完成：共 ${importResult.totalRows} 行，成功 ${importResult.successCount} 行，失败 ${importResult.failedCount} 行`"
          :closable="false"
          show-icon
        />

        <div v-if="importResult.failedCount > 0" class="import-error-list">
          <p>失败明细：</p>
          <ul>
            <li v-for="(item, index) in importResult.errors || []" :key="`${item.lineNumber}-${index}`">
              第 {{ item.lineNumber }} 行：{{ item.reason }}
            </li>
          </ul>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button :disabled="importing" @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="importing" @click="handleImportSubmit">开始导入</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshLeft, Edit, Delete, UploadFilled } from '@element-plus/icons-vue'
import { deleteWordCard, getWordCardList, importWordCardsCsv } from '../../api/wordCard'
import { extractErrorMessage } from '../../utils/error'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const loadErrorText = ref('')
const importDialogVisible = ref(false)
const importFile = ref(null)
const importFileList = ref([])
const importing = ref(false)
const importResult = ref(null)

const searchForm = reactive({
  keyword: '',
  tag: '',
  memoryStatus: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const parseTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  if (typeof tags === 'string') {
    try {
      return JSON.parse(tags)
    } catch {
      return tags.split(',').map(t => t.trim()).filter(Boolean)
    }
  }
  return []
}

const loadData = async () => {
  loading.value = true
  loadErrorText.value = ''
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const res = await getWordCardList(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
      loadErrorText.value = res.message || '获取单词卡列表失败'
    }
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    loadErrorText.value = extractErrorMessage(error, '获取单词卡列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.tag = ''
  searchForm.memoryStatus = ''
  pagination.pageNum = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleAdd = () => {
  router.push('/word-cards/create')
}

const handleEdit = (row) => {
  router.push(`/word-cards/edit/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除单词 "${row.word}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteWordCard(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '删除失败'))
    }
  }).catch(() => {})
}

const handleOpenImportDialog = () => {
  importDialogVisible.value = true
  importFile.value = null
  importFileList.value = []
  importResult.value = null
}

const handleImportFileChange = (file, fileList) => {
  const raw = file.raw
  if (!raw) {
    return
  }

  const isCsv = raw.name.toLowerCase().endsWith('.csv') || raw.type.includes('csv')
  if (!isCsv) {
    ElMessage.error('仅支持上传 .csv 文件')
    importFile.value = null
    importFileList.value = []
    return
  }

  if (raw.size > 20 * 1024 * 1024) {
    ElMessage.error('文件不能超过 20MB')
    importFile.value = null
    importFileList.value = []
    return
  }

  importFile.value = raw
  importFileList.value = fileList.slice(-1)
}

const handleImportFileRemove = () => {
  importFile.value = null
  importFileList.value = []
}

const handleImportSubmit = async () => {
  if (importing.value) {
    return
  }

  if (!importFile.value) {
    ElMessage.warning('请先选择 CSV 文件')
    return
  }

  importing.value = true
  try {
    const res = await importWordCardsCsv(importFile.value)
    if (res.code !== 200) {
      ElMessage.error(res.message || '导入失败')
      return
    }

    importResult.value = res.data
    if ((res.data?.successCount || 0) > 0) {
      loadData()
    }

    if ((res.data?.failedCount || 0) > 0) {
      ElMessage.warning('导入已完成，部分行失败，请检查失败明细')
    } else {
      ElMessage.success('导入成功')
    }
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '导入失败'))
  } finally {
    importing.value = false
  }
}

const handleDownloadTemplate = () => {
  const csvTemplate = [
    'word,meaning,phonetic,exampleSentence,tags,isPublic',
    'abandon,放弃,/əˈbændən/,He had to abandon the plan.,cet4|verb,false',
    'benefit,好处,/ˈbenɪfɪt/,Regular exercise benefits your health.,cet4|noun,false',
  ].join('\n')

  const blob = new Blob([`\uFEFF${csvTemplate}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'wordsprint-word-cards-template.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.table-card {
  min-height: 400px;
}

.load-error-alert {
  margin-bottom: 12px;
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 4px;
}

.stat-text {
  color: #666;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.import-guidelines {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}

.import-guidelines p {
  margin: 0 0 8px;
  font-weight: 600;
}

.import-guidelines-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.import-guidelines ul {
  margin: 0;
  padding-left: 18px;
}

.import-guidelines li {
  margin-bottom: 6px;
}

.import-result {
  margin-top: 14px;
}

.import-error-list {
  margin-top: 10px;
  max-height: 180px;
  overflow: auto;
  font-size: 13px;
  color: #606266;
}

.import-error-list p {
  margin: 0 0 8px;
  font-weight: 600;
}

.import-error-list ul {
  margin: 0;
  padding-left: 18px;
}

@media (max-width: 900px) {
  .table-card :deep(.el-table) {
    overflow-x: auto;
  }

  .search-card :deep(.el-form--inline) {
    display: flex;
    flex-direction: column;
    gap: 0;
  }
}

@media (max-width: 480px) {
  .pagination-wrapper {
    justify-content: center;
  }
}
</style>

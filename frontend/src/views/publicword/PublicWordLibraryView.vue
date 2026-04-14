<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Public Library</p>
        <h1>公共词库</h1>
        <p class="page-description">从公共词库挑选词条，一键加入你的单词卡。</p>
      </div>
      <div class="header-actions">
        <el-button v-if="isAdmin" @click="handleOpenImportDialog">管理员 CSV 导入</el-button>
        <el-button @click="goToMyWordCards">查看我的单词卡</el-button>
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
        <el-form-item label="难度标签">
          <el-select v-model="searchForm.levelTag" placeholder="全部标签" clearable>
            <el-option label="CET4" value="CET4" />
            <el-option label="CET6" value="CET6" />
            <el-option label="IELTS" value="IELTS" />
            <el-option label="TOEFL" value="TOEFL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
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
        <el-table-column prop="word" label="单词" width="180" />
        <el-table-column prop="phonetic" label="音标" width="140" />
        <el-table-column prop="meaning" label="词义" min-width="220" show-overflow-tooltip />
        <el-table-column prop="levelTag" label="标签" width="100" />
        <el-table-column prop="sourceName" label="来源" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :loading="importingId === row.id"
              @click="handleImport(row)"
            >
              导入到我的词库
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无可用公共词条" />

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
      title="管理员 CSV 导入公共词库"
      width="620px"
      :close-on-click-modal="!importingCsv"
      :close-on-press-escape="!importingCsv"
      :show-close="!importingCsv"
    >
      <div class="import-guidelines">
        <div class="import-guidelines-head">
          <p>文件要求：</p>
          <el-button link type="primary" @click="handleDownloadTemplate">下载模板</el-button>
        </div>
        <ul>
          <li>仅支持 <code>.csv</code> 文件，建议 UTF-8 编码。</li>
          <li>列顺序：<code>word,meaning,phonetic,exampleSentence,levelTag,sourceName,status</code>。</li>
          <li>前两列 <code>word</code> 和 <code>meaning</code> 必填；首行表头可选。</li>
          <li><code>status</code> 支持 <code>1/0/true/false</code>，默认 <code>1</code>。</li>
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

      <div v-if="csvImportResult" class="import-result">
        <el-alert
          :type="csvImportResult.failedCount > 0 ? 'warning' : 'success'"
          :title="`导入完成：共 ${csvImportResult.totalRows} 行，新增 ${csvImportResult.insertedCount} 行，更新 ${csvImportResult.updatedCount} 行，失败 ${csvImportResult.failedCount} 行`"
          :closable="false"
          show-icon
        />

        <div v-if="csvImportResult.failedCount > 0" class="import-error-list">
          <p>失败明细：</p>
          <ul>
            <li v-for="(item, index) in csvImportResult.errors || []" :key="`${item.lineNumber}-${index}`">
              第 {{ item.lineNumber }} 行：{{ item.reason }}
            </li>
          </ul>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button :disabled="importingCsv" @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="importingCsv" @click="handleImportCsvSubmit">开始导入</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

import { getPublicWordList, importPublicWord, importPublicWordsCsv } from '../../api/publicWord'
import { useUserStore } from '../../stores/user'
import { extractErrorMessage } from '../../utils/error'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const importingId = ref(null)
const loadErrorText = ref('')
const importDialogVisible = ref(false)
const importFile = ref(null)
const importFileList = ref([])
const importingCsv = ref(false)
const csvImportResult = ref(null)

const searchForm = reactive({
  keyword: '',
  levelTag: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const loadData = async () => {
  loading.value = true
  loadErrorText.value = ''
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      levelTag: searchForm.levelTag || undefined,
    }
    const res = await getPublicWordList(params)
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
      loadErrorText.value = res.message || '获取公共词库失败'
    }
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    loadErrorText.value = extractErrorMessage(error, '获取公共词库失败')
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
  searchForm.levelTag = ''
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

const handleImport = async (row) => {
  if (importingId.value) {
    return
  }
  importingId.value = row.id
  try {
    const res = await importPublicWord(row.id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '导入失败')
      return
    }

    if (res.data?.imported) {
      ElMessage.success(`已导入 ${row.word}`)
      return
    }

    ElMessage.info(`"${row.word}" 已在你的词库中`)
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '导入失败'))
  } finally {
    importingId.value = null
  }
}

const goToMyWordCards = () => {
  router.push('/word-cards')
}

const handleOpenImportDialog = () => {
  if (!isAdmin.value) {
    return
  }
  importDialogVisible.value = true
  importFile.value = null
  importFileList.value = []
  csvImportResult.value = null
}

const handleImportFileChange = (file, fileList) => {
  const raw = file.raw
  if (!raw) {
    return
  }
  const isCsvFile = raw.name.toLowerCase().endsWith('.csv') || raw.type.includes('csv')
  if (!isCsvFile) {
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

const handleImportCsvSubmit = async () => {
  if (!isAdmin.value) {
    ElMessage.error('仅管理员可导入公共词库')
    return
  }
  if (importingCsv.value) {
    return
  }
  if (!importFile.value) {
    ElMessage.warning('请先选择 CSV 文件')
    return
  }

  importingCsv.value = true
  try {
    const res = await importPublicWordsCsv(importFile.value)
    if (res.code !== 200) {
      ElMessage.error(res.message || '导入失败')
      return
    }
    csvImportResult.value = res.data
    if ((res.data?.failedCount || 0) > 0) {
      ElMessage.warning('导入已完成，部分行失败，请检查明细')
    } else {
      ElMessage.success('导入成功')
    }
    loadData()
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '导入失败'))
  } finally {
    importingCsv.value = false
  }
}

const handleDownloadTemplate = () => {
  const csvTemplate = [
    'word,meaning,phonetic,exampleSentence,levelTag,sourceName,status',
    'abandon,放弃,/əˈbændən/,He had to abandon the plan.,CET4,official,1',
    'benefit,好处,/ˈbenɪfɪt/,Regular exercise benefits your health.,CET4,official,1',
  ].join('\n')

  const blob = new Blob([`\uFEFF${csvTemplate}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'wordsprint-public-library-template.csv'
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
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  min-height: 400px;
}

.load-error-alert {
  margin-bottom: 12px;
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

.import-guidelines-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.import-guidelines p {
  margin: 0 0 8px;
  font-weight: 600;
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

import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { extractErrorMessage } from '../utils/error'

/**
 * 封装 CSV 导入弹窗逻辑。
 * WordCardList 和 PublicWordLibrary 的 CSV 导入高度雷同。
 *
 * 用法:
 *   const csv = useCsvImport(importApiFunc, { onSuccess: () => loadData() })
 */
export function useCsvImport(apiFn, options = {}) {
  const dialogVisible = ref(false)
  const importing = ref(false)
  const importFile = ref(null)
  const importFileList = ref([])
  const importResult = ref(null)

  const maxSizeMB = options.maxSizeMB ?? 20

  function openDialog() {
    dialogVisible.value = true
    importFile.value = null
    importFileList.value = []
    importResult.value = null
  }

  function closeDialog() {
    dialogVisible.value = false
  }

  function handleFileChange(file, fileList) {
    const raw = file.raw
    if (!raw) return

    const isCsv = raw.name.toLowerCase().endsWith('.csv') || raw.type.includes('csv')
    if (!isCsv) {
      ElMessage.error('仅支持上传 .csv 文件')
      importFile.value = null
      importFileList.value = []
      return
    }

    if (raw.size > maxSizeMB * 1024 * 1024) {
      ElMessage.error(`文件不能超过 ${maxSizeMB}MB`)
      importFile.value = null
      importFileList.value = []
      return
    }

    importFile.value = raw
    importFileList.value = fileList.slice(-1)
  }

  function handleFileRemove() {
    importFile.value = null
    importFileList.value = []
  }

  async function handleSubmit() {
    if (importing.value) return
    if (!importFile.value) {
      ElMessage.warning('请先选择 CSV 文件')
      return
    }

    importing.value = true
    try {
      const res = await apiFn(importFile.value)
      if (res.code !== 200) {
        ElMessage.error(res.message || '导入失败')
        return
      }

      importResult.value = res.data
      const failedCount = res.data?.failedCount || 0

      if (failedCount > 0) {
        ElMessage.warning('导入已完成，部分行失败，请检查明细')
      } else {
        ElMessage.success('导入成功')
      }

      options.onSuccess?.()
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '导入失败'))
    } finally {
      importing.value = false
    }
  }

  return {
    dialogVisible,
    importing,
    importFile,
    importFileList,
    importResult,
    openDialog,
    closeDialog,
    handleFileChange,
    handleFileRemove,
    handleSubmit,
  }
}

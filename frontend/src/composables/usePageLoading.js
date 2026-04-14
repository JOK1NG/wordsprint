import { ref } from 'vue'
import { extractErrorMessage } from '../utils/error'

/**
 * 封装页面级 加载 / 错误 / 重试 三状态。
 *
 * 用法:
 *   const { loading, errorText, runAsync } = usePageLoading()
 *   await runAsync(() => fetchData(), '获取数据失败')
 */
export function usePageLoading() {
  const loading = ref(false)
  const errorText = ref('')

  async function runAsync(fn, fallbackMessage = '请求失败，请稍后重试') {
    loading.value = true
    errorText.value = ''
    try {
      const result = await fn()
      return result
    } catch (error) {
      errorText.value = extractErrorMessage(error, fallbackMessage)
      return null
    } finally {
      loading.value = false
    }
  }

  function clearError() {
    errorText.value = ''
  }

  return { loading, errorText, runAsync, clearError }
}

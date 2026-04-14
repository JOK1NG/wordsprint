import { reactive } from 'vue'

/**
 * 封装分页状态 + 事件处理。
 * WordCardList、WrongWordList、PublicWordLibrary 三页完全相同的分页逻辑。
 *
 * 用法:
 *   const { pagination, handlePageChange, handleSizeChange, resetPage } = usePagination(loadData)
 */
export function usePagination(loadFn, initialPageSize = 10) {
  const pagination = reactive({
    pageNum: 1,
    pageSize: initialPageSize,
    total: 0,
  })

  function handlePageChange(page) {
    pagination.pageNum = page
    loadFn?.()
  }

  function handleSizeChange(size) {
    pagination.pageSize = size
    pagination.pageNum = 1
    loadFn?.()
  }

  function resetPage() {
    pagination.pageNum = 1
  }

  function updateTotal(total) {
    pagination.total = total ?? 0
  }

  return { pagination, handlePageChange, handleSizeChange, resetPage, updateTotal }
}

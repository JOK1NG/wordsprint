/**
 * WordSprint 统一格式化工具
 *
 * 原先散落在 DashboardView、StatisticsView 中的重复函数统一收口。
 */

/**
 * 安全格式化数值。非有限数字时返回 fallback。
 */
export function formatMetricValue(value, fallback = '--') {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

/**
 * 格式化百分比。
 *  - value <= 1 时视为小数，自动 ×100
 *  - 其他情况直接取整
 */
export function formatPercent(value, fallback = '--') {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return fallback
  }
  const percent = value <= 1 ? value * 100 : value
  return `${Math.round(percent)}%`
}

/**
 * 格式化秒数为可读时长。
 *  - < 60s  → "N 秒"
 *  - < 3600 → "N 分 N 秒"
 *  - >= 3600 → "N 小时 N 分"
 */
export function formatDuration(seconds) {
  if (typeof seconds !== 'number' || Number.isNaN(seconds) || seconds < 0) {
    return '--'
  }
  if (seconds === 0) return '0 秒'

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainSeconds = seconds % 60

  if (hours > 0) return `${hours} 小时 ${minutes} 分`
  if (minutes > 0) return `${minutes} 分 ${remainSeconds} 秒`
  return `${remainSeconds} 秒`
}

/**
 * 格式化日期字符串为本地化显示。
 */
export function formatDate(dateStr, options) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', options ?? {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 统一解包 API 响应。code === 200 时返回 data，否则抛异常。
 */
export function unwrapResponse(response, fallbackMessage) {
  if (response?.code === 200) {
    return response.data ?? null
  }
  throw new Error(response?.message || fallbackMessage)
}

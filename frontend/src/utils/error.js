export function extractErrorMessage(error, fallback = '请求失败，请稍后重试') {
  return error?.response?.data?.message || error?.message || fallback
}

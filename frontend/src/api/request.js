import axios from 'axios'

import { clearToken, getToken } from '../utils/token'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      clearToken()
      if (window.location.pathname !== '/login') {
        const redirect = `${window.location.pathname}${window.location.search}${window.location.hash}`
        const loginUrl = redirect && redirect !== '/' ? `/login?redirect=${encodeURIComponent(redirect)}` : '/login'
        window.location.href = loginUrl
      }
    }
    return Promise.reject(error)
  },
)

export default request

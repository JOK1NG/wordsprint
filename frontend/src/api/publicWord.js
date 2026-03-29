import request from './request'

export function getPublicWordList(params) {
  return request({
    url: '/api/public-words',
    method: 'get',
    params,
  })
}

export function importPublicWord(id) {
  return request({
    url: `/api/public-words/${id}/import`,
    method: 'post',
  })
}

export function importPublicWordsCsv(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/admin/public-words/import/csv',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    timeout: 30000,
  })
}

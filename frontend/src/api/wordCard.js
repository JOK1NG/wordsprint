import request from './request'

export function getWordCardList(params) {
  return request({
    url: '/api/word-cards',
    method: 'get',
    params,
  })
}

export function getWordCardDetail(id) {
  return request({
    url: `/api/word-cards/${id}`,
    method: 'get',
  })
}

export function createWordCard(data) {
  return request({
    url: '/api/word-cards',
    method: 'post',
    data,
  })
}

export function updateWordCard(id, data) {
  return request({
    url: `/api/word-cards/${id}`,
    method: 'put',
    data,
  })
}

export function deleteWordCard(id) {
  return request({
    url: `/api/word-cards/${id}`,
    method: 'delete',
  })
}

export function importWordCardsCsv(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/word-cards/import/csv',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    timeout: 30000,
  })
}

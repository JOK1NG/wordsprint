import request from './request'

export function getWrongWordList(params) {
  return request({
    url: '/api/wrong-words',
    method: 'get',
    params,
  })
}

export function removeWrongWord(wordCardId) {
  return request({
    url: `/api/wrong-words/${wordCardId}/remove`,
    method: 'post',
  })
}

export function restoreWrongWord(wordCardId) {
  return request({
    url: `/api/wrong-words/${wordCardId}/restore`,
    method: 'post',
  })
}

export function getWrongWordPractice(size = 10) {
  return request({
    url: '/api/wrong-words/practice',
    method: 'get',
    params: { size },
  })
}

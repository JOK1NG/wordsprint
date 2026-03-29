import request from './request'

export function getStudyPlan() {
  return request({
    url: '/api/study-plan',
    method: 'get',
  })
}

export function updateStudyPlan(data) {
  return request({
    url: '/api/study-plan',
    method: 'put',
    data,
  })
}

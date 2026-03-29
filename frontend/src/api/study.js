import request from './request'

export function getStudyRandom(params) {
  return request({
    url: '/api/study/random',
    method: 'get',
    params,
  })
}

export function submitStudyAnswer(data) {
  return request({
    url: '/api/study/submit',
    method: 'post',
    data,
  })
}

export function getTodayStudySummary() {
  return request({
    url: '/api/study/today-summary',
    method: 'get',
  })
}

export function getStudyStatistics(params) {
  return request({
    url: '/api/study/statistics',
    method: 'get',
    params,
  })
}

export function getStudyFamiliarityDistribution() {
  return request({
    url: '/api/study/familiarity-distribution',
    method: 'get',
  })
}

export function getCheckinCalendar(params) {
  return request({
    url: '/api/checkin/calendar',
    method: 'get',
    params,
  })
}

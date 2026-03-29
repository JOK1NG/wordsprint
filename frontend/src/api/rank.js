import request from './request'

export function getPointsRank(limit = 20) {
  return request.get('/api/rank/points', {
    params: { limit },
  })
}

export function getStreakRank(limit = 20) {
  return request.get('/api/rank/streak', {
    params: { limit },
  })
}

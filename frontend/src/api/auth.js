import request from './request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data,
  })
}

export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data,
  })
}

export function getCurrentUser() {
  return request({
    url: '/api/user/me',
    method: 'get',
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/api/user/profile',
    method: 'put',
    data,
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post',
  })
}

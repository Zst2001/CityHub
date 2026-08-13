import request from './request'

export const sendCode = (phone) => request.post('/user/code', null, { params: { phone } })
export const login = (data) => request.post('/user/login', data)
export const getCurrentUser = () => request.get('/user/me')

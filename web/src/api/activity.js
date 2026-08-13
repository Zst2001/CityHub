import request from './request'

export const getActivityCategories = () => request.get('/activity-category/list')
export const getActivityById = (id) => request.get(`/activity/${id}`)
export const getActivitiesPage = (params) => request.get('/activity/page', { params, returnResult: true })
export const getActivitiesByCategory = (params) => request.get('/activity/of/category', { params })
export const searchActivitiesByName = (params) => request.get('/activity/of/name', { params })

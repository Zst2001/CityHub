import request from './request'
export const adminLogin = (data) => request.post('/admin/login', data)
export const getAdminActivities = (params) => request.get('/admin/activities', { params, returnResult: true })
export const updateAdminActivity = (data) => request.put('/admin/activities', data)
export const getAdminTickets = (activityId) => request.get(`/admin/activities/${activityId}/tickets`)
export const updateAdminTicket = (ticketId, data) => request.put(`/admin/tickets/${ticketId}`, data)

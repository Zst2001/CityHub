import request from './request'
export const getTicketsByActivity = (activityId) => request.get(`/ticket/list/${activityId}`)

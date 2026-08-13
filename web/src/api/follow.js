import request from './request'

export const setFollow = (userId, isFollow) => request.put(`/follow/${userId}/${isFollow}`)
export const getFollowStatus = (userId) => request.get(`/follow/or/not/${userId}`)

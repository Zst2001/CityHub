import request from './request'

export const followUser = (id, isFollow) => request.put(`/follow/${id}/${isFollow}`)
export const isFollowing = (id) => request.get(`/follow/or/not/${id}`)
export const getCommonFollows = (id) => request.get(`/follow/follow/common/${id}`)

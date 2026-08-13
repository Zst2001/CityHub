import request from './request'

export const getHotBlogs = (current = 1) => request.get('/blog/hot', { params: { current } })
export const getBlogById = (id) => request.get(`/blog/${id}`)
export const getBlogsByActivity = (params) => request.get('/blog/of/activity', { params })
export const publishBlog = (data) => request.post('/blog', data)
export const toggleBlogLike = (id) => request.put(`/blog/like/${id}`)
export const getBlogLikes = (id) => request.get(`/blog/likes/${id}`)
export const getFollowFeed = (params) => request.get('/blog/of/follow', { params })

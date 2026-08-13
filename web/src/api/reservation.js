import request from './request'
export const seckillTicket = (ticketId) => request.post(`/reservation/seckill/${ticketId}`, null, { silentBusinessError: true })

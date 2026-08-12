---
--- Created by 17926.
--- DateTime: 2025/9/12 15:44
---

local ticketId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. ticketId
local orderKey = 'seckill:order:' .. ticketId


if( tonumber(redis.call('get',stockKey)) <= 0) then
    return 1
end

if( redis.call('sismember',orderKey,userId) == 1) then
    return 2
end

redis.call('incrby',stockKey,-1)
redis.call('sadd',orderKey,userId)
return 0

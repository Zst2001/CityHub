# CityHub Demo Enhancement Admin Report

## Final Status

`DEMO_ADMIN_ENHANCEMENT: PASS`

The Ticket update failure is fixed and the backend, Docker/Nginx, and required browser admin flow are verified.

## Ticket 500 Root Cause

`Ticket.stock`, `beginTime`, and `endTime` are non-persistent fields on `Ticket`; their actual storage is `tb_seckill_ticket`. A stock-only admin JSON request still called `updateById(ticket)`, producing an empty `UPDATE tb_ticket ... SET` statement in MyBatis-Plus and a runtime SQL exception.

Fixed in `backend/core/src/main/java/com/cityhub/service/impl/TicketServiceImpl.java`: persistent ticket fields are updated only when present, `SeckillTicket` is required, and stock continues to synchronize the existing `seckill:stock:{ticketId}` key.

## Verified Results

| Item | Result | Evidence |
|---|---|---|
| Ticket update HTTP 500 | FIXED | Stock-only `PUT /admin/tickets/11` returned success |
| Ticket update | PASS | Ticket 11 updated through ADMIN API |
| MySQL stock | PASS | `tb_seckill_ticket.stock = 107` after edit |
| Redis stock | PASS | `seckill:stock:11 = 107` after edit |
| Admin UI data source | PASS | Re-read ticket API returned `stock = 107` |
| Admin reservation | PASS | ADMIN reservation succeeded; Redis 107 -> 106; one order persisted |
| Duplicate reservation | PASS | Same ADMIN/Ticket was rejected by existing Lua one-user-one-order rule |
| Reservation path | PASS | Existing RedisIdWorker -> Lua -> ArrayBlockingQueue consumer -> Redisson -> MySQL path retained and exercised |
| User schema | PASS | `tb_user` and `User` contain `username`, `password`, `role` |
| Password storage | PASS | Admin password is BCrypt-formatted; no hash or secret is recorded here |
| ADMIN permission | PASS | ADMIN token accesses `/admin/activities` |
| USER permission | PASS | Redis-backed USER token receives HTTP 403 on `/admin/activities` |
| Unauthenticated admin | PASS | No token receives HTTP 401 |
| Activity cache consistency | PASS | Admin activity update invalidates `cache:activity:{id}` |
| AI data regression | PASS | Activity address was temporarily changed through ADMIN API; real consultant ActivityTool returned the current ASCII verification value; display value was restored |
| Desktop admin entry | PASS | Header conditionally renders admin route for ADMIN |
| Mobile admin entry | PASS | Drawer conditionally renders `活动管理` only for ADMIN |
| Login 1440 / 390 | PASS | Playwright screenshots and viewport checks passed; no horizontal overflow |
| Admin page 1440 / 390 | PASS | Playwright screenshots and viewport checks passed; no horizontal overflow |
| Mobile Drawer admin entry | PASS | 390px Drawer showed `活动管理` for ADMIN |
| Docker/Nginx E2E | PASS | `http://127.0.0.1:8088`: admin login, `/user/me` role ADMIN, admin activities API, and `/admin/activities` SPA refresh all passed |
| Maven clean compile | PASS | `mvn -f backend/pom.xml clean compile -DskipTests` |
| npm build | PASS | `npm --prefix web run build` |
| Docker config/build | PASS | `docker compose config --quiet`, package, build, and all five services up |
| Secret check | PASS | `.env` is ignored; tracked matches are placeholders or environment-variable references only |

## Data Handling

- The reservation test intentionally consumed one real ticket for the ADMIN user; its remaining stock is 106 in both MySQL and Redis.
- Temporary Activity addresses used for AI verification were restored.
- No extra stock key or stock model was introduced.

## Git

Final commit and push are performed after this report update. User-owned prompt files, temporary screenshots, and `.env` are excluded from the commit.

## Freeze Note

No additional administrator feature was added. CityHub administrator enhancement is frozen after this final fix.

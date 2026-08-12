# Phase 3A-R Reset Plan

## A. Current Phase 3 implementation to withdraw

The following files belong to the superseded Venue / ActivitySession design and will be removed or rewritten before the first CityHub commit:

- `backend/core/src/main/java/com/cityhub/entity/Venue.java`
- `backend/core/src/main/java/com/cityhub/entity/ActivitySession.java`
- `backend/core/src/main/java/com/cityhub/mapper/VenueMapper.java`
- `backend/core/src/main/java/com/cityhub/mapper/ActivitySessionMapper.java`
- `backend/core/src/main/java/com/cityhub/service/IVenueService.java`
- `backend/core/src/main/java/com/cityhub/service/IActivitySessionService.java`
- `backend/core/src/main/java/com/cityhub/service/impl/VenueServiceImpl.java`
- `backend/core/src/main/java/com/cityhub/service/impl/ActivitySessionServiceImpl.java`
- `backend/core/src/main/java/com/cityhub/controller/VenueController.java`
- Venue / ActivitySession-specific DTO and VO files, plus the current `Activity` and `ActivityCategory` query implementation.
- `backend/core/src/main/resources/db/cityhub_domain_v1.sql`
- `backend/core/src/main/resources/db/cityhub_domain_seed.sql`
- Venue and ActivitySession paths in `MvcConfig`.

The current `ReservationOrder` contains `sessionId`, and its SQL contains a `(user_id, session_id)` unique key. It will be rewritten to the lightweight `ticketId` model.

## B. Phase 1 / Phase 2 baseline retained

- Maven coordinates `cityhub-parent`, `cityhub-core`, `cityhub-ai`.
- Java package `com.cityhub`, `CityHubApplication`, and `CityHubAiApplication`.
- Environment-variable configuration, `.env.example`, `.gitignore`, and engineering hygiene changes.
- Documentation under `docs/refactor/phase1` and `docs/refactor/phase2`.

## C. Legacy source confirmed before migration

The pre-Phase-3 source still contains all lightweight migration sources:

- `ShopType`, `Shop`, `Voucher`, `SeckillVoucher`, `VoucherOrder` entities and mappers.
- `ShopTypeController`, `ShopController`, `VoucherController`, `VoucherOrderController`.
- `IShopTypeService`, `IShopService`, `IVoucherService`, `ISeckillVoucherService`, `IVoucherOrderService` and implementations.
- Existing `seckill.lua`, `CacheClient`, Redisson use, `RedisIdWorker`, and BlockingQueue-based asynchronous order processing.

## D. Target after reset

The final lightweight baseline is:

```text
ActivityCategory
Activity
Ticket
SeckillTicket
ReservationOrder(ticketId)
```

No `Venue`, `ActivitySession`, `venueId`, `sessionId`, `/venues`, or `/activities/{id}/sessions` runtime design remains. The migration keeps the existing cache, Lua, one-user-one-order, Redisson, Redis ID, asynchronous seckill, and Redis token algorithms; only their business names and required identifiers change.

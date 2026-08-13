package com.cityhub.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cityhub.dto.Result;
import com.cityhub.entity.ReservationOrder;
import com.cityhub.service.IReservationOrderService;
import com.cityhub.service.ISeckillTicketService;
import com.cityhub.utils.RedisConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SeckillReservationIntegrationTest {

    private static final long TICKET_ID = 1L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ISeckillTicketService seckillTicketService;

    @Autowired
    private IReservationOrderService reservationOrderService;

    @BeforeEach
    void prepareTicket() {
        resetTicket(20);
    }

    @AfterEach
    void cleanUp() {
        reservationOrderService.remove(new QueryWrapper<ReservationOrder>().eq("ticket_id", TICKET_ID));
        seckillTicketService.update().set("stock", 100).eq("ticket_id", TICKET_ID).update();
        stringRedisTemplate.delete(RedisConstants.SECKILL_STOCK_KEY + TICKET_ID);
        stringRedisTemplate.delete(RedisConstants.SECKILL_ORDER_KEY + TICKET_ID);
    }

    @Test
    void verifiesRealLuaQueueRedissonAndMysqlReservationFlow() throws Exception {
        String userA = login("13800000001");
        ResponseEntity<String> anonymous = restTemplate.postForEntity(seckillUrl(), null, String.class);
        assertEquals(401, anonymous.getStatusCodeValue());

        resetTicket(3);
        JSONObject normal = reserve(userA);
        assertTrue(normal.getBool("success"));
        Long orderId = normal.getLong("data");
        assertNotNull(orderId);
        assertTrue(orderId > 0L);
        awaitOrderCount(1);
        assertEquals(2, mysqlStock());
        assertEquals("2", redisStock());
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.hasKey("icr:" + RedisConstants.RESERVATION_ORDER_ID_KEY
                + ":" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd")))));

        JSONObject duplicate = reserve(userA);
        assertFalse(duplicate.getBool("success"));
        assertEquals(1L, orderCountFor(userId(userA)));

        resetTicket(2);
        String userB = login("13800000002");
        String userC = login("13800000003");
        String userD = login("13800000004");
        assertTrue(reserve(userB).getBool("success"));
        assertTrue(reserve(userC).getBool("success"));
        assertFalse(reserve(userD).getBool("success"));
        awaitOrderCount(2);
        assertEquals(0, mysqlStock());
        assertEquals("0", redisStock());
        assertNoDuplicateOrders();

        resetTicket(10);
        List<String> concurrentUsers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            concurrentUsers.add(login(String.format("1390000%04d", i)));
        }
        List<JSONObject> multiUserResults = submitConcurrently(concurrentUsers);
        assertEquals(10, successCount(multiUserResults));
        awaitOrderCount(10);
        assertEquals(0, mysqlStock());
        assertEquals("0", redisStock());
        assertNoDuplicateOrders();

        resetTicket(10);
        String sameUser = login("13800000005");
        List<String> sameUserRequests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sameUserRequests.add(sameUser);
        }
        List<JSONObject> sameUserResults = submitConcurrently(sameUserRequests);
        assertEquals(1, successCount(sameUserResults));
        awaitOrderCount(1);
        assertEquals(1L, orderCountFor(userId(sameUser)));
        assertEquals(9, mysqlStock());
        assertEquals("9", redisStock());
        assertNoDuplicateOrders();
    }

    private void resetTicket(int stock) {
        reservationOrderService.remove(new QueryWrapper<ReservationOrder>().eq("ticket_id", TICKET_ID));
        seckillTicketService.update().set("stock", stock).eq("ticket_id", TICKET_ID).update();
        stringRedisTemplate.delete(RedisConstants.SECKILL_STOCK_KEY + TICKET_ID);
        stringRedisTemplate.delete(RedisConstants.SECKILL_ORDER_KEY + TICKET_ID);
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + TICKET_ID, String.valueOf(stock));
    }

    private String login(String phone) {
        String code = "123456";
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code);
        String body = "{\"phone\":\"" + phone + "\",\"code\":\"" + code + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/user/login",
                new HttpEntity<String>(body, headers), String.class);
        JSONObject json = JSONUtil.parseObj(response.getBody());
        assertTrue(json.getBool("success"));
        return json.getStr("data");
    }

    private JSONObject reserve(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", token);
        ResponseEntity<String> response = restTemplate.exchange(seckillUrl(), HttpMethod.POST,
                new HttpEntity<Object>(headers), String.class);
        assertEquals(200, response.getStatusCodeValue());
        return JSONUtil.parseObj(response.getBody());
    }

    private List<JSONObject> submitConcurrently(List<String> tokens) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tokens.size());
        CountDownLatch ready = new CountDownLatch(tokens.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<JSONObject>> futures = new ArrayList<>();
        try {
            for (String token : tokens) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return reserve(token);
                }));
            }
            ready.await();
            start.countDown();
            List<JSONObject> results = new ArrayList<>();
            for (Future<JSONObject> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private int successCount(List<JSONObject> results) {
        int count = 0;
        for (JSONObject result : results) {
            if (result.getBool("success")) {
                count++;
            }
        }
        return count;
    }

    private void awaitOrderCount(int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < deadline) {
            if (reservationOrderService.count(new QueryWrapper<ReservationOrder>().eq("ticket_id", TICKET_ID)) == expected) {
                return;
            }
            Thread.sleep(100L);
        }
        assertEquals(expected, reservationOrderService.count(new QueryWrapper<ReservationOrder>().eq("ticket_id", TICKET_ID)));
    }

    private int mysqlStock() {
        return seckillTicketService.getById(TICKET_ID).getStock();
    }

    private String redisStock() {
        return stringRedisTemplate.opsForValue().get(RedisConstants.SECKILL_STOCK_KEY + TICKET_ID);
    }

    private long orderCountFor(Long userId) {
        return reservationOrderService.count(new QueryWrapper<ReservationOrder>()
                .eq("user_id", userId).eq("ticket_id", TICKET_ID));
    }

    private Long userId(String token) {
        return Long.valueOf(stringRedisTemplate.opsForHash()
                .entries(RedisConstants.LOGIN_USER_KEY + token).get("id").toString());
    }

    private void assertNoDuplicateOrders() {
        List<ReservationOrder> orders = reservationOrderService.list(
                new QueryWrapper<ReservationOrder>().eq("ticket_id", TICKET_ID));
        assertEquals(orders.size(), orders.stream()
                .map(order -> order.getUserId() + ":" + order.getTicketId()).distinct().count());
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private String seckillUrl() {
        return baseUrl() + "/reservation/seckill/" + TICKET_ID;
    }
}

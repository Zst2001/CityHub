package com.cityhub.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cityhub.entity.Blog;
import com.cityhub.entity.Follow;
import com.cityhub.entity.User;
import com.cityhub.utils.RedisConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses the real HTTP, MySQL and Redis paths for the lightweight activity community.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommunityFlowIntegrationTest {

    private static final List<String> PHONES = Arrays.asList("13700000001", "13700000002", "13700000003");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IBlogService blogService;

    @Autowired
    private IFollowService followService;

    @Autowired
    private IUserService userService;

    private final List<Long> testUserIds = new ArrayList<>();
    private Long publishedBlogId;

    @AfterEach
    void cleanUp() {
        if (publishedBlogId != null) {
            blogService.removeById(publishedBlogId);
            stringRedisTemplate.delete(RedisConstants.BLOG_LIKED_KEY + publishedBlogId);
        }
        if (!testUserIds.isEmpty()) {
            followService.remove(new QueryWrapper<Follow>().in("user_id", testUserIds));
            followService.remove(new QueryWrapper<Follow>().in("follow_user_id", testUserIds));
            for (Long userId : testUserIds) {
                stringRedisTemplate.delete(RedisConstants.USER_FOLLOW_KEY + userId);
                stringRedisTemplate.delete(RedisConstants.FEED_KEY + userId);
            }
        }
        userService.remove(new QueryWrapper<User>().in("phone", PHONES));
        for (String phone : PHONES) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        }
    }

    @Test
    void verifiesActivityBlogLikeFollowCommonAndFeedFlow() {
        String tokenA = login(PHONES.get(0));
        String tokenB = login(PHONES.get(1));
        String tokenC = login(PHONES.get(2));
        Long userA = userId(tokenA);
        Long userB = userId(tokenB);
        Long userC = userId(tokenC);
        testUserIds.addAll(Arrays.asList(userA, userB, userC));

        assertTrue(follow(tokenA, userB, true).getBool("success"));
        assertTrue(follow(tokenA, userC, true).getBool("success"));
        assertTrue(follow(tokenB, userC, true).getBool("success"));
        assertEquals(1L, followService.count(new QueryWrapper<Follow>()
                .eq("user_id", userA).eq("follow_user_id", userB)));

        JSONObject common = get(tokenA, "/follow/follow/common/" + userB);
        assertTrue(common.getBool("success"));
        assertTrue(containsUser(JSONUtil.parseArray(common.get("data")), userC));

        JSONObject invalidActivity = post(tokenB, "/blog", "{\"shopId\":0,\"activityId\":999999,\"title\":\"无效活动\","
                + "\"images\":\"/imgs/activities/creative-market.jpg\",\"content\":\"不应保存\"}");
        assertFalse(invalidActivity.getBool("success"));

        JSONObject published = post(tokenB, "/blog", "{\"shopId\":0,\"activityId\":1,\"title\":\"Phase4 活动体验动态\","
                + "\"images\":\"/imgs/activities/creative-market.jpg\",\"content\":\"真实社区链路集成测试\"}");
        assertTrue(published.getBool("success"));
        publishedBlogId = published.getLong("data");
        Blog stored = blogService.getById(publishedBlogId);
        assertNotNull(stored);
        assertEquals(1L, stored.getActivityId());

        JSONObject detail = get(tokenA, "/blog/" + publishedBlogId);
        assertTrue(detail.getBool("success"));
        assertEquals(publishedBlogId, JSONUtil.parseObj(detail.get("data")).getLong("id"));

        JSONObject activityBlogs = get(tokenA, "/blog/of/activity?activityId=1&current=1");
        JSONArray activityData = JSONUtil.parseArray(activityBlogs.get("data"));
        assertTrue(containsBlog(activityData, publishedBlogId));
        for (Object item : activityData) {
            assertEquals(1L, JSONUtil.parseObj(item).getLong("activityId"));
        }

        JSONObject feed = get(tokenA, "/blog/of/follow?lastId=" + System.currentTimeMillis() + "&offset=0");
        assertTrue(feed.getBool("success"));
        JSONObject scroll = JSONUtil.parseObj(feed.get("data"));
        assertTrue(containsBlog(JSONUtil.parseArray(scroll.get("list")), publishedBlogId));
        assertNotNull(scroll.getLong("minTime"));
        assertNotNull(scroll.getInt("offset"));

        assertTrue(put(tokenA, "/blog/like/" + publishedBlogId).getBool("success"));
        assertEquals(1, blogService.getById(publishedBlogId).getLiked());
        assertNotNull(stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + publishedBlogId, userA.toString()));

        JSONObject likes = get(tokenA, "/blog/likes/" + publishedBlogId);
        assertTrue(likes.getBool("success"));
        assertTrue(containsUser(JSONUtil.parseArray(likes.get("data")), userA));

        assertTrue(put(tokenA, "/blog/like/" + publishedBlogId).getBool("success"));
        assertEquals(0, blogService.getById(publishedBlogId).getLiked());
        assertNull(stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + publishedBlogId, userA.toString()));

        assertTrue(follow(tokenA, userB, false).getBool("success"));
        assertEquals(0L, followService.count(new QueryWrapper<Follow>()
                .eq("user_id", userA).eq("follow_user_id", userB)));
        assertFalse(Boolean.TRUE.equals(stringRedisTemplate.opsForSet()
                .isMember(RedisConstants.USER_FOLLOW_KEY + userA, userB.toString())));
    }

    private JSONObject follow(String token, Long followUserId, boolean isFollow) {
        return exchange(token, "/follow/" + followUserId + "/" + isFollow, HttpMethod.PUT, null);
    }

    private JSONObject post(String token, String path, String body) {
        return exchange(token, path, HttpMethod.POST, body);
    }

    private JSONObject put(String token, String path) {
        return exchange(token, path, HttpMethod.PUT, null);
    }

    private JSONObject get(String token, String path) {
        return exchange(token, path, HttpMethod.GET, null);
    }

    private JSONObject exchange(String token, String path, HttpMethod method, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", token);
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + path, method,
                new HttpEntity<String>(body, headers), String.class);
        assertEquals(200, response.getStatusCodeValue());
        return JSONUtil.parseObj(response.getBody());
    }

    private String login(String phone) {
        String code = "123456";
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/user/login",
                new HttpEntity<String>("{\"phone\":\"" + phone + "\",\"code\":\"" + code + "\"}", headers), String.class);
        JSONObject json = JSONUtil.parseObj(response.getBody());
        assertTrue(json.getBool("success"));
        return json.getStr("data");
    }

    private Long userId(String token) {
        return Long.valueOf(stringRedisTemplate.opsForHash()
                .entries(RedisConstants.LOGIN_USER_KEY + token).get("id").toString());
    }

    private boolean containsBlog(JSONArray items, Long blogId) {
        for (Object item : items) {
            if (blogId.equals(JSONUtil.parseObj(item).getLong("id"))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUser(JSONArray items, Long userId) {
        for (Object item : items) {
            if (userId.equals(JSONUtil.parseObj(item).getLong("id"))) {
                return true;
            }
        }
        return false;
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }
}

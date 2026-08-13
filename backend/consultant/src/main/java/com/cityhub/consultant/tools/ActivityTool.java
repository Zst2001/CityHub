package com.cityhub.consultant.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ActivityTool {
    private final JdbcTemplate jdbcTemplate;

    public ActivityTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool("按关键词搜索 CityHub 中真实存在的活动，返回活动 id、标题、分类、时间、地点和参考价格。")
    public List<Map<String, Object>> searchActivities(@P("活动关键词，例如 摄影、音乐、市集") String keyword) {
        log.info("CityHub AI tool invoked: searchActivities");
        return jdbcTemplate.queryForList("SELECT a.id,a.title,c.name category,a.open_hours,a.area,a.address,a.avg_price FROM tb_activity a LEFT JOIN tb_activity_category c ON a.category_id=c.id WHERE a.title LIKE ? ORDER BY a.score DESC,a.sold DESC LIMIT 10", "%" + keyword + "%");
    }

    @Tool("按 CityHub 活动分类查询真实活动。分类名称必须来自真实分类，例如 展览、音乐、市集、演出、讲座、手作、体育、亲子。")
    public List<Map<String, Object>> listActivitiesByCategory(@P("分类名称") String categoryName) {
        log.info("CityHub AI tool invoked: listActivitiesByCategory");
        return jdbcTemplate.queryForList("SELECT a.id,a.title,c.name category,a.open_hours,a.area,a.address,a.avg_price FROM tb_activity a JOIN tb_activity_category c ON a.category_id=c.id WHERE c.name LIKE ? ORDER BY a.score DESC,a.sold DESC LIMIT 10", "%" + categoryName + "%");
    }

    @Tool("查询指定 CityHub 活动的真实详情，输入活动 id。")
    public List<Map<String, Object>> getActivityDetail(@P("活动 id") Long activityId) {
        log.info("CityHub AI tool invoked: getActivityDetail");
        return jdbcTemplate.queryForList("SELECT a.id,a.title,c.name category,a.open_hours,a.area,a.address,a.avg_price,a.score,a.sold FROM tb_activity a LEFT JOIN tb_activity_category c ON a.category_id=c.id WHERE a.id=?", activityId);
    }

    @Tool("查询指定 CityHub 活动的真实预约凭证、价格、规则和限量库存，输入活动 id。")
    public List<Map<String, Object>> getActivityTickets(@P("活动 id") Long activityId) {
        log.info("CityHub AI tool invoked: getActivityTickets");
        return jdbcTemplate.queryForList("SELECT t.id,t.activity_id,t.title,t.sub_title,t.rules,t.actual_value,t.type,s.stock,s.begin_time,s.end_time FROM tb_ticket t LEFT JOIN tb_seckill_ticket s ON t.id=s.ticket_id WHERE t.activity_id=? AND t.status=1 ORDER BY t.id", activityId);
    }
}

package com.xiangyongshe.swim_admin.stats;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    @Select("""
            select date(created_at) as day, count(*) as total
            from post
            where deleted = 0 and created_at >= date_sub(curdate(), interval #{days} day)
            group by date(created_at)
            order by day asc
            """)
    List<DailyCount> postDailyCount(@Param("days") int days);

    @Select("""
            select date(created_at) as day, count(*) as total
            from post_comment
            where deleted = 0 and created_at >= date_sub(curdate(), interval #{days} day)
            group by date(created_at)
            order by day asc
            """)
    List<DailyCount> commentDailyCount(@Param("days") int days);

    @Select("select count(*) from sys_user where deleted = 0")
    int userCount();

    @Select("select count(*) from post where deleted = 0")
    int postCount();

    @Select("select count(*) from baby_profile where deleted = 0")
    int babyCount();

    @Select("select count(*) from post_comment where deleted = 0")
    int commentCount();

    @Select("""
            select c.name as name, count(p.id) as value
            from post_category c
            left join post p on p.category_id = c.id and p.deleted = 0
            where c.deleted = 0 and c.status = 1
            group by c.id, c.name
            order by value desc
            """)
    List<Map<String, Object>> categoryPostStats();

    @Select("""
            select p.id, p.title, u.display_name as authorName, p.status, p.created_at as createdAt
            from post p
            join sys_user u on u.id = p.user_id
            where p.deleted = 0
            order by p.created_at desc
            limit 5
            """)
    List<Map<String, Object>> recentPosts();
}


package com.xiangyongshe.swim_admin.stats;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}

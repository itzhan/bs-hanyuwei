package com.xiangyongshe.swim_admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.constant.UserRole;
import com.xiangyongshe.swim_admin.common.constant.UserStatus;
import com.xiangyongshe.swim_admin.post.PostCategory;
import com.xiangyongshe.swim_admin.post.PostCategoryService;
import com.xiangyongshe.swim_admin.post.PostTag;
import com.xiangyongshe.swim_admin.post.PostTagService;
import com.xiangyongshe.swim_admin.post.PostTopic;
import com.xiangyongshe.swim_admin.post.PostTopicService;
import com.xiangyongshe.swim_admin.user.SysUser;
import com.xiangyongshe.swim_admin.user.SysUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final PostCategoryService postCategoryService;
    private final PostTopicService postTopicService;
    private final PostTagService postTagService;

    public DataInitializer(SysUserService sysUserService,
                           PasswordEncoder passwordEncoder,
                           PostCategoryService postCategoryService,
                           PostTopicService postTopicService,
                           PostTagService postTagService) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.postCategoryService = postCategoryService;
        this.postTopicService = postTopicService;
        this.postTagService = postTagService;
    }

    @Override
    public void run(String... args) {
        SysUser admin = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin")
                .eq(SysUser::getDeleted, 0));
        if (admin == null) {
            SysUser user = new SysUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setRole(UserRole.ADMIN.getValue());
            user.setStatus(UserStatus.ACTIVE.getValue());
            user.setDisplayName("系统管理员");
            sysUserService.save(user);
        }

        if (postCategoryService.count() == 0) {
            List<PostCategory> categories = List.of(
                    createCategory("喂养", 1),
                    createCategory("睡眠", 2),
                    createCategory("健康", 3),
                    createCategory("教育", 4)
            );
            postCategoryService.saveBatch(categories);
        }

        if (postTopicService.count() == 0) {
            List<PostTopic> topics = List.of(
                    createTopic("新手爸妈"),
                    createTopic("经验分享"),
                    createTopic("求助问答")
            );
            postTopicService.saveBatch(topics);
        }

        if (postTagService.count() == 0) {
            List<PostTag> tags = List.of(
                    createTag("母乳"),
                    createTag("辅食"),
                    createTag("疫苗"),
                    createTag("身高体重")
            );
            postTagService.saveBatch(tags);
        }
    }

    private PostCategory createCategory(String name, int order) {
        PostCategory category = new PostCategory();
        category.setName(name);
        category.setSortOrder(order);
        category.setStatus(1);
        return category;
    }

    private PostTopic createTopic(String name) {
        PostTopic topic = new PostTopic();
        topic.setName(name);
        topic.setStatus(1);
        return topic;
    }

    private PostTag createTag(String name) {
        PostTag tag = new PostTag();
        tag.setName(name);
        return tag;
    }
}

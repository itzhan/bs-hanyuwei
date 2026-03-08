# DB Design (MySQL)

Project: Parenting community & growth record platform (Spring Boot + MySQL)

## Conventions
- PK: BIGINT auto_increment
- Time: created_at, updated_at
- Soft delete: deleted TINYINT(1) default 0
- Charset: utf8mb4

## Table: sys_user
- Purpose: parent/admin accounts
- Columns:
  - id BIGINT PK
  - username VARCHAR(50) NOT NULL UNIQUE
  - password VARCHAR(255) NOT NULL
  - role SMALLINT NOT NULL DEFAULT 1 COMMENT '1=parent,2=admin'
  - status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=active,2=disabled'
  - display_name VARCHAR(50) NULL
  - avatar_path VARCHAR(255) NULL
  - last_login_at DATETIME NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - uq_user_username(username)
  - idx_user_role(role)
  - idx_user_status(status)
- Notes: password stored as BCrypt hash

## Table: baby_profile
- Purpose: multiple babies per parent
- Columns:
  - id BIGINT PK
  - user_id BIGINT NOT NULL
  - name VARCHAR(50) NOT NULL
  - gender SMALLINT NULL COMMENT '0=unknown,1=male,2=female'
  - birthday DATE NULL
  - relation VARCHAR(20) NULL COMMENT 'parent relation'
  - avatar_path VARCHAR(255) NULL
  - note VARCHAR(255) NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_baby_user(user_id)
  - idx_baby_birthday(birthday)
- Relations:
  - user_id -> sys_user.id

## Table: growth_log
- Purpose: narrative logs (feeding, sleep, illness, milestone, measurement, etc.)
- Columns:
  - id BIGINT PK
  - user_id BIGINT NOT NULL
  - baby_id BIGINT NOT NULL
  - log_type SMALLINT NOT NULL COMMENT 'dictionary: feeding/sleep/illness/milestone/measurement/etc'
  - title VARCHAR(100) NULL
  - content TEXT NULL
  - log_time DATETIME NOT NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_log_baby_time(baby_id, log_time)
  - idx_log_user_time(user_id, log_time)
- Relations:
  - user_id -> sys_user.id
  - baby_id -> baby_profile.id

## Table: growth_metric
- Purpose: numeric metrics recorded from logs (for charts)
- Columns:
  - id BIGINT PK
  - user_id BIGINT NOT NULL
  - baby_id BIGINT NOT NULL
  - source_log_id BIGINT NULL
  - metric_type SMALLINT NOT NULL COMMENT 'dictionary: height/weight/head_circumference/temperature/etc'
  - metric_value DECIMAL(10,2) NOT NULL
  - unit VARCHAR(20) NULL
  - recorded_at DATETIME NOT NULL
  - note VARCHAR(255) NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_metric_baby_type_time(baby_id, metric_type, recorded_at)
- Relations:
  - user_id -> sys_user.id
  - baby_id -> baby_profile.id
  - source_log_id -> growth_log.id

## Table: post_category
- Purpose: category for posts
- Columns:
  - id BIGINT PK
  - name VARCHAR(50) NOT NULL
  - description VARCHAR(255) NULL
  - sort_order INT NOT NULL DEFAULT 0
  - status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=enabled,2=disabled'
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - uq_category_name(name)
  - idx_category_status(status)

## Table: post_topic
- Purpose: topic for posts
- Columns:
  - id BIGINT PK
  - name VARCHAR(50) NOT NULL
  - description VARCHAR(255) NULL
  - status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=enabled,2=disabled'
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - uq_topic_name(name)
  - idx_topic_status(status)

## Table: post_tag
- Purpose: tags for posts
- Columns:
  - id BIGINT PK
  - name VARCHAR(50) NOT NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - uq_tag_name(name)

## Table: post
- Purpose: community posts
- Columns:
  - id BIGINT PK
  - user_id BIGINT NOT NULL
  - category_id BIGINT NULL
  - topic_id BIGINT NULL
  - title VARCHAR(100) NOT NULL
  - content TEXT NOT NULL
  - status SMALLINT NOT NULL DEFAULT 2 COMMENT '1=published,2=pending,3=hidden,4=rejected,5=blocked'
  - comment_count INT NOT NULL DEFAULT 0
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_post_user_time(user_id, created_at)
  - idx_post_category_time(category_id, created_at)
  - idx_post_status_time(status, created_at)
- Relations:
  - user_id -> sys_user.id
  - category_id -> post_category.id
  - topic_id -> post_topic.id

## Table: post_tag_rel
- Purpose: n-n relation between post and tag
- Columns:
  - id BIGINT PK
  - post_id BIGINT NOT NULL
  - tag_id BIGINT NOT NULL
- Indexes:
  - uq_post_tag(post_id, tag_id)
  - idx_post_tag_tag(tag_id)
- Relations:
  - post_id -> post.id
  - tag_id -> post_tag.id

## Table: post_comment
- Purpose: comments with reply support
- Columns:
  - id BIGINT PK
  - post_id BIGINT NOT NULL
  - user_id BIGINT NOT NULL
  - parent_id BIGINT NULL
  - reply_to_user_id BIGINT NULL
  - content TEXT NOT NULL
  - status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=published,2=hidden,3=deleted'
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_comment_post_time(post_id, created_at)
  - idx_comment_parent(parent_id)
- Relations:
  - post_id -> post.id
  - user_id -> sys_user.id
  - parent_id -> post_comment.id
  - reply_to_user_id -> sys_user.id

## Table: content_report
- Purpose: reports for post/comment
- Columns:
  - id BIGINT PK
  - reporter_id BIGINT NOT NULL
  - post_id BIGINT NULL
  - comment_id BIGINT NULL
  - reason SMALLINT NOT NULL COMMENT 'dictionary: spam/abuse/privacy/illegal/etc'
  - reason_desc VARCHAR(255) NULL
  - status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=pending,2=processed,3=rejected'
  - handled_by BIGINT NULL
  - handled_at DATETIME NULL
  - handle_result SMALLINT NULL COMMENT '1=ignore,2=delete,3=block,4=warn'
  - handle_note VARCHAR(255) NULL
  - created_at DATETIME NOT NULL
  - updated_at DATETIME NOT NULL
  - deleted TINYINT(1) NOT NULL DEFAULT 0
- Indexes:
  - idx_report_target_post(post_id)
  - idx_report_target_comment(comment_id)
  - idx_report_status(status, created_at)
  - idx_report_reporter(reporter_id)
- Relations:
  - reporter_id -> sys_user.id
  - post_id -> post.id
  - comment_id -> post_comment.id
  - handled_by -> sys_user.id
- Notes: exactly one of post_id/comment_id should be set

## Table: content_audit
- Purpose: audit history for moderation actions
- Columns:
  - id BIGINT PK
  - operator_id BIGINT NOT NULL
  - post_id BIGINT NULL
  - comment_id BIGINT NULL
  - report_id BIGINT NULL
  - action SMALLINT NOT NULL COMMENT '1=approve,2=reject,3=block,4=hide,5=restore'
  - reason VARCHAR(255) NULL
  - created_at DATETIME NOT NULL
- Indexes:
  - idx_audit_target_post(post_id)
  - idx_audit_target_comment(comment_id)
  - idx_audit_operator_time(operator_id, created_at)
- Relations:
  - operator_id -> sys_user.id
  - post_id -> post.id
  - comment_id -> post_comment.id
  - report_id -> content_report.id
- Notes: exactly one of post_id/comment_id should be set

## Table: file_object
- Purpose: local file storage metadata (optional use)
- Columns:
  - id BIGINT PK
  - owner_id BIGINT NOT NULL
  - biz_type SMALLINT NOT NULL COMMENT '1=post,2=log,3=avatar'
  - biz_id BIGINT NULL
  - original_name VARCHAR(255) NOT NULL
  - file_name VARCHAR(255) NOT NULL
  - file_path VARCHAR(255) NOT NULL
  - mime_type VARCHAR(100) NULL
  - size BIGINT NOT NULL
  - created_at DATETIME NOT NULL
- Indexes:
  - idx_file_owner(owner_id)
  - idx_file_biz(biz_type, biz_id)
- Relations:
  - owner_id -> sys_user.id

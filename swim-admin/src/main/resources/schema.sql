-- MySQL 8.x schema

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role SMALLINT NOT NULL DEFAULT 1 COMMENT '1=parent,2=admin',
  status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=active,2=disabled',
  display_name VARCHAR(50) NULL,
  avatar_path VARCHAR(255) NULL,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_user_username (username),
  KEY idx_user_role (role),
  KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS baby_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  gender SMALLINT NULL COMMENT '0=unknown,1=male,2=female',
  birthday DATE NULL,
  relation VARCHAR(20) NULL,
  avatar_path VARCHAR(255) NULL,
  note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_baby_user (user_id),
  KEY idx_baby_birthday (birthday),
  CONSTRAINT fk_baby_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS growth_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  baby_id BIGINT NOT NULL,
  log_type SMALLINT NOT NULL COMMENT 'dictionary: feeding/sleep/illness/milestone/measurement/etc',
  title VARCHAR(100) NULL,
  content TEXT NULL,
  log_time DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_log_baby_time (baby_id, log_time),
  KEY idx_log_user_time (user_id, log_time),
  CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_log_baby FOREIGN KEY (baby_id) REFERENCES baby_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS growth_metric (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  baby_id BIGINT NOT NULL,
  source_log_id BIGINT NULL,
  metric_type SMALLINT NOT NULL COMMENT 'dictionary: height/weight/head_circumference/temperature/etc',
  metric_value DECIMAL(10,2) NOT NULL,
  unit VARCHAR(20) NULL,
  recorded_at DATETIME NOT NULL,
  note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_metric_baby_type_time (baby_id, metric_type, recorded_at),
  CONSTRAINT fk_metric_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_metric_baby FOREIGN KEY (baby_id) REFERENCES baby_profile(id),
  CONSTRAINT fk_metric_source_log FOREIGN KEY (source_log_id) REFERENCES growth_log(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=enabled,2=disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_category_name (name),
  KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_topic (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=enabled,2=disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_topic_name (name),
  KEY idx_topic_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  topic_id BIGINT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status SMALLINT NOT NULL DEFAULT 2 COMMENT '1=published,2=pending,3=hidden,4=rejected,5=blocked',
  comment_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_post_user_time (user_id, created_at),
  KEY idx_post_category_time (category_id, created_at),
  KEY idx_post_status_time (status, created_at),
  CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES post_category(id),
  CONSTRAINT fk_post_topic FOREIGN KEY (topic_id) REFERENCES post_topic(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_tag_rel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  UNIQUE KEY uq_post_tag (post_id, tag_id),
  KEY idx_post_tag_tag (tag_id),
  CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES post_tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  reply_to_user_id BIGINT NULL,
  content TEXT NOT NULL,
  status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=published,2=hidden,3=deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_comment_post_time (post_id, created_at),
  KEY idx_comment_parent (parent_id),
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES post_comment(id),
  CONSTRAINT fk_comment_reply_user FOREIGN KEY (reply_to_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_id BIGINT NOT NULL,
  post_id BIGINT NULL,
  comment_id BIGINT NULL,
  reason SMALLINT NOT NULL COMMENT 'dictionary: spam/abuse/privacy/illegal/etc',
  reason_desc VARCHAR(255) NULL,
  status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=pending,2=processed,3=rejected',
  handled_by BIGINT NULL,
  handled_at DATETIME NULL,
  handle_result SMALLINT NULL COMMENT '1=ignore,2=delete,3=block,4=warn',
  handle_note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_report_target_post (post_id),
  KEY idx_report_target_comment (comment_id),
  KEY idx_report_status (status, created_at),
  KEY idx_report_reporter (reporter_id),
  CONSTRAINT fk_reporter_user FOREIGN KEY (reporter_id) REFERENCES sys_user(id),
  CONSTRAINT fk_report_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_report_comment FOREIGN KEY (comment_id) REFERENCES post_comment(id),
  CONSTRAINT fk_report_handler FOREIGN KEY (handled_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NOT NULL,
  post_id BIGINT NULL,
  comment_id BIGINT NULL,
  report_id BIGINT NULL,
  action SMALLINT NOT NULL COMMENT '1=approve,2=reject,3=block,4=hide,5=restore',
  reason VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_target_post (post_id),
  KEY idx_audit_target_comment (comment_id),
  KEY idx_audit_operator_time (operator_id, created_at),
  CONSTRAINT fk_audit_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id),
  CONSTRAINT fk_audit_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_audit_comment FOREIGN KEY (comment_id) REFERENCES post_comment(id),
  CONSTRAINT fk_audit_report FOREIGN KEY (report_id) REFERENCES content_report(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_object (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  biz_type SMALLINT NOT NULL COMMENT '1=post,2=log,3=avatar',
  biz_id BIGINT NULL,
  original_name VARCHAR(255) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  mime_type VARCHAR(100) NULL,
  size BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_file_owner (owner_id),
  KEY idx_file_biz (biz_type, biz_id),
  CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

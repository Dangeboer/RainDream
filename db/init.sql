CREATE DATABASE IF NOT EXISTS raindream_database
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE raindream_database;

-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 项目表
CREATE TABLE IF NOT EXISTS item
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,

    media_type    TINYINT      NOT NULL COMMENT '格式类型',
    content_type  TINYINT      NOT NULL COMMENT '内容类型',

    store_url     VARCHAR(2048) COMMENT '存储链接（媒体类型）',
    content       TEXT COMMENT '内容（文本类型）',
    title         VARCHAR(200),

    fandom        VARCHAR(100) NOT NULL,
    cp            VARCHAR(100) NOT NULL,
    author        VARCHAR(100),

    source_url    VARCHAR(2048),

    release_year  YEAR,
    size_bytes    BIGINT,

    tracking_type TINYINT               DEFAULT 5 COMMENT '状态',
    rating        DECIMAL(3, 1),

    notes         TEXT,
    summary       TEXT,

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_item_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,

    UNIQUE KEY uk_item_user_source (user_id, source_url(255)),

    INDEX idx_item_media_type (media_type),
    INDEX idx_item_content_type (content_type),
    INDEX idx_item_author (author),
    INDEX idx_item_tracking (tracking_type),
    INDEX idx_item_rating (rating)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 标签表
CREATE TABLE IF NOT EXISTS tag
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id  BIGINT       NOT NULL,
    tag_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,

    UNIQUE KEY uk_tag_user_name (user_id, tag_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 平台表
CREATE TABLE IF NOT EXISTS plt
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id  BIGINT       NOT NULL,
    plt_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_plt_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,

    UNIQUE KEY uk_plt_user_name (user_id, plt_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 同人文信息补充表
CREATE TABLE IF NOT EXISTS fanfic_info
(
    item_id      BIGINT PRIMARY KEY,

    era          TINYINT NOT NULL,
    char_setting VARCHAR(200),
    length_type  TINYINT NOT NULL,
    work_type    TINYINT NOT NULL,
    update_date  DATE,
    ending_type  TINYINT,
    read_count   INT,

    CONSTRAINT fk_fanfic_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,

    INDEX idx_fanfic_era (era),
    INDEX idx_fanfic_length (length_type),
    INDEX idx_fanfic_work (work_type),
    INDEX idx_fanfic_ending (ending_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 媒体信息补充表
CREATE TABLE IF NOT EXISTS media_info
(
    item_id   BIGINT PRIMARY KEY,

    thumb_url VARCHAR(2048),
    live_url  VARCHAR(2048),

    CONSTRAINT fk_media_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 项目-标签表，多对多
CREATE TABLE IF NOT EXISTS item_tag
(
    item_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,

    PRIMARY KEY (item_id, tag_id),

    CONSTRAINT fk_item_tag_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,

    CONSTRAINT fk_item_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE,

    INDEX idx_item_tag_tag (tag_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 项目-平台表，多对多
CREATE TABLE IF NOT EXISTS item_plt
(
    item_id BIGINT NOT NULL,
    plt_id  BIGINT NOT NULL,

    PRIMARY KEY (item_id, plt_id),

    CONSTRAINT fk_item_plt_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,

    CONSTRAINT fk_item_plt_plt FOREIGN KEY (plt_id) REFERENCES plt (id) ON DELETE CASCADE,

    INDEX idx_item_plt_plt (plt_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


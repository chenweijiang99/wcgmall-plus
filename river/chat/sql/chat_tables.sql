-- =============================================
-- 聊天室功能数据库表
-- =============================================

-- 1. 聊天室表
CREATE TABLE `chat_room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '聊天室ID',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '聊天室名称(群聊时使用)',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '聊天室头像(群聊时使用)',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '聊天室类型: 1-私聊 2-群聊',
    `owner_id` BIGINT DEFAULT NULL COMMENT '群主ID(群聊时使用)',
    `owner_type` TINYINT DEFAULT 1 COMMENT '群主类型: 1-普通用户 2-管理员',
    `announcement` VARCHAR(500) DEFAULT NULL COMMENT '群公告',
    `max_member_count` INT DEFAULT 500 COMMENT '最大成员数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室表';

-- 2. 聊天室成员表
CREATE TABLE `chat_room_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_id` BIGINT NOT NULL COMMENT '聊天室ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-普通用户 2-管理员',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群内昵称',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通成员 1-管理员 2-群主',
    `muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否禁言: 0-否 1-是',
    `muted_end_time` DATETIME DEFAULT NULL COMMENT '禁言结束时间',
    `last_read_msg_id` BIGINT DEFAULT 0 COMMENT '最后已读消息ID',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后已读时间',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已退出 1-正常',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_user` (`room_id`, `user_id`, `user_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室成员表';

-- 3. 聊天消息表
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `room_id` BIGINT NOT NULL COMMENT '聊天室ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `sender_type` TINYINT NOT NULL DEFAULT 1 COMMENT '发送者类型: 1-普通用户 2-管理员',
    `msg_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-文本 2-图片 3-视频 4-语音 5-表情 6-系统消息',
    `content` TEXT COMMENT '消息内容(文本/表情代码)',
    `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体文件URL(图片/视频/语音)',
    `media_duration` INT DEFAULT NULL COMMENT '媒体时长(秒,语音/视频)',
    `media_size` BIGINT DEFAULT NULL COMMENT '媒体文件大小(字节)',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL(视频)',
    `reply_msg_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID',
    `at_user_ids` VARCHAR(500) DEFAULT NULL COMMENT '@的用户ID列表,逗号分隔',
    `is_revoked` TINYINT NOT NULL DEFAULT 0 COMMENT '是否撤回: 0-否 1-是',
    `revoke_time` DATETIME DEFAULT NULL COMMENT '撤回时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_sender` (`sender_id`, `sender_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_room_time` (`room_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 4. 用户会话表
CREATE TABLE `chat_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-普通用户 2-管理员',
    `room_id` BIGINT NOT NULL COMMENT '聊天室ID',
    `last_msg_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    `last_msg_content` VARCHAR(200) DEFAULT NULL COMMENT '最后一条消息内容摘要',
    `last_msg_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰: 0-否 1-是',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除会话: 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_room` (`user_id`, `user_type`, `room_id`),
    KEY `idx_user_id` (`user_id`, `user_type`),
    KEY `idx_last_msg_time` (`last_msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 5. 好友关系表
CREATE TABLE `chat_friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-普通用户 2-管理员',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `friend_type` TINYINT NOT NULL DEFAULT 1 COMMENT '好友类型: 1-普通用户 2-管理员',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注',
    `room_id` BIGINT DEFAULT NULL COMMENT '私聊聊天室ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已删除 1-正常 2-拉黑',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `user_type`, `friend_id`, `friend_type`),
    KEY `idx_user_id` (`user_id`, `user_type`),
    KEY `idx_friend_id` (`friend_id`, `friend_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 6. 好友申请表
CREATE TABLE `chat_friend_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `from_user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `from_user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '申请人类型: 1-普通用户 2-管理员',
    `to_user_id` BIGINT NOT NULL COMMENT '被申请人ID',
    `to_user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '被申请人类型: 1-普通用户 2-管理员',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '申请消息',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-已同意 2-已拒绝 3-已过期',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_user` (`from_user_id`, `from_user_type`),
    KEY `idx_to_user` (`to_user_id`, `to_user_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- =============================================
-- 初始化内置群聊
-- 群主为ID=1848的管理员
-- =============================================

-- 创建内置群聊（ID固定为1，方便引用）
INSERT INTO `chat_room` (`id`, `name`, `avatar`, `type`, `owner_id`, `owner_type`, `announcement`, `max_member_count`, `status`, `create_by`, `create_time`)
VALUES (1, '文创购官方交流群', NULL, 2, 1848, 2, '欢迎加入文创购官方交流群！请遵守群规，文明交流。', 10000, 1, 'system', NOW());

-- 将群主（ID=1848的管理员）加入群聊
INSERT INTO `chat_room_member` (`room_id`, `user_id`, `user_type`, `nickname`, `role`, `muted`, `last_read_msg_id`, `join_time`, `status`)
VALUES (1, 1848, 2, NULL, 2, 0, 0, NOW(), 1);

-- 发送一条系统欢迎消息
INSERT INTO `chat_message` (`room_id`, `sender_id`, `sender_type`, `msg_type`, `content`, `is_revoked`, `status`, `create_time`)
VALUES (1, 1848, 2, 6, '欢迎来到文创购官方交流群！', 0, 1, NOW());

-- =============================================
-- 将所有现有用户加入默认群聊
-- 执行此脚本前请确保 chat_room 表中已有ID=1的群聊
-- =============================================

-- 将所有现有普通用户加入默认群聊（排除已加入的）
INSERT INTO `chat_room_member` (`room_id`, `user_id`, `user_type`, `nickname`, `role`, `muted`, `last_read_msg_id`, `join_time`, `status`)
SELECT 1, u.id, 1, NULL, 0, 0, 0, NOW(), 1
FROM `sys_user` u
WHERE u.status = 1
AND NOT EXISTS (
    SELECT 1 FROM `chat_room_member` m
    WHERE m.room_id = 1 AND m.user_id = u.id AND m.user_type = 1
);

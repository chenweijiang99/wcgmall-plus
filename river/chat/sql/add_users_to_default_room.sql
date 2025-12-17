-- =============================================
-- 将指定用户加入默认群聊
-- 包括群成员记录和会话记录
-- =============================================

-- 将管理员 1848, 1852-1858 加入群聊成员表（管理员类型为2）
INSERT IGNORE INTO `chat_room_member` (`room_id`, `user_id`, `user_type`, `nickname`, `role`, `muted`, `last_read_msg_id`, `join_time`, `status`)
VALUES
(1, 1848, 2, NULL, 2, 0, 0, NOW(), 1),
(1, 1852, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1853, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1854, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1855, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1856, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1857, 2, NULL, 0, 0, 0, NOW(), 1),
(1, 1858, 2, NULL, 0, 0, 0, NOW(), 1);

-- 为这些管理员创建会话记录（这样才能在会话列表中看到群聊）
INSERT IGNORE INTO `chat_conversation` (`user_id`, `user_type`, `room_id`, `last_msg_id`, `last_msg_content`, `last_msg_time`, `unread_count`, `is_top`, `is_muted`, `is_deleted`, `create_time`)
VALUES
(1848, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1852, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1853, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1854, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1855, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1856, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1857, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW()),
(1858, 2, 1, NULL, '欢迎来到文创购官方交流群！', NOW(), 0, 0, 0, 0, NOW());

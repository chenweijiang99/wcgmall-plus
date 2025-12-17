package com.river.config.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.river.dto.ChatMessageDTO;
import com.river.service.ChatMessageService;
import com.river.service.ChatRoomService;
import com.river.vo.ChatMemberVO;
import com.river.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService messageService;
    private final ChatRoomService roomService;
    private final StringRedisTemplate redisTemplate;

    // 在线用户会话管理 key: userType:userId
    private static final Map<String, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    // Redis在线用户key前缀
    private static final String ONLINE_USER_KEY = "chat:online:";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        Integer userType = (Integer) session.getAttributes().get("userType");

        if (userId != null && userType != null) {
            String userKey = userType + ":" + userId;
            ONLINE_USERS.put(userKey, session);

            // 记录到Redis
            redisTemplate.opsForValue().set(ONLINE_USER_KEY + userKey, "1");

            log.info("用户上线: {}", userKey);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        Integer userType = (Integer) session.getAttributes().get("userType");

        if (userId == null || userType == null) {
            sendError(session, "未登录");
            return;
        }

        try {
            JSONObject json = JSONUtil.parseObj(message.getPayload());
            String type = json.getStr("type");

            switch (type) {
                case "SEND_MSG" -> handleSendMessage(session, json.getJSONObject("data"), userId, userType);
                case "READ_MSG" -> handleReadMessage(json.getJSONObject("data"), userId, userType);
                case "HEARTBEAT" -> handleHeartbeat(session);
                default -> sendError(session, "未知消息类型: " + type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        Integer userType = (Integer) session.getAttributes().get("userType");

        if (userId != null && userType != null) {
            String userKey = userType + ":" + userId;
            ONLINE_USERS.remove(userKey);

            // 从Redis移除
            redisTemplate.delete(ONLINE_USER_KEY + userKey);

            log.info("用户下线: {}", userKey);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误", exception);
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(WebSocketSession session, JSONObject data, Long userId, Integer userType) {
        ChatMessageDTO dto = data.toBean(ChatMessageDTO.class);

        // 发送消息
        ChatMessageVO messageVO = messageService.sendMessage(dto, userId, userType);

        // 广播给聊天室所有在线成员
        broadcastToRoom(dto.getRoomId(), "NEW_MSG", messageVO);
    }

    /**
     * 处理已读消息
     */
    private void handleReadMessage(JSONObject data, Long userId, Integer userType) {
        Long roomId = data.getLong("roomId");
        Long msgId = data.getLong("msgId");

        if (roomId != null && msgId != null) {
            messageService.markAsRead(roomId, msgId, userId, userType);
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session) throws IOException {
        JSONObject response = new JSONObject();
        response.set("type", "HEARTBEAT");
        response.set("data", "pong");
        session.sendMessage(new TextMessage(response.toString()));
    }

    /**
     * 广播消息给聊天室所有在线成员
     */
    public void broadcastToRoom(Long roomId, String type, Object data) {
        List<ChatMemberVO> members = roomService.getRoomMembers(roomId);

        for (ChatMemberVO member : members) {
            String userKey = member.getUserType() + ":" + member.getUserId();
            WebSocketSession session = ONLINE_USERS.get(userKey);

            if (session != null && session.isOpen()) {
                try {
                    // 为每个接收者设置isSelf字段
                    JSONObject msgData = JSONUtil.parseObj(JSONUtil.toJsonStr(data));
                    if (data instanceof ChatMessageVO) {
                        ChatMessageVO msgVO = (ChatMessageVO) data;
                        boolean isSelf = member.getUserId().equals(msgVO.getSenderId())
                                && member.getUserType().equals(msgVO.getSenderType());
                        msgData.set("isSelf", isSelf);
                    }

                    JSONObject message = new JSONObject();
                    message.set("type", type);
                    message.set("data", msgData);
                    session.sendMessage(new TextMessage(message.toString()));
                } catch (IOException e) {
                    log.error("发送消息给用户{}失败", userKey, e);
                }
            }
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long userId, Integer userType, String type, Object data) {
        String userKey = userType + ":" + userId;
        WebSocketSession session = ONLINE_USERS.get(userKey);

        if (session != null && session.isOpen()) {
            try {
                JSONObject message = new JSONObject();
                message.set("type", type);
                message.set("data", data);
                session.sendMessage(new TextMessage(message.toString()));
            } catch (IOException e) {
                log.error("发送消息给用户{}失败", userKey, e);
            }
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String errorMsg) throws IOException {
        JSONObject response = new JSONObject();
        response.set("type", "ERROR");
        response.set("data", errorMsg);
        session.sendMessage(new TextMessage(response.toString()));
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId, Integer userType) {
        String userKey = userType + ":" + userId;
        return ONLINE_USERS.containsKey(userKey) || Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_USER_KEY + userKey));
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return ONLINE_USERS.size();
    }
}

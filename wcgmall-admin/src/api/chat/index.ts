import request from '@/utils/request'

// ==================== 聊天室相关 ====================

// 创建群聊
export function createGroupRoom(data: { name: string; avatar?: string; memberIds: number[]; memberTypes?: number[] }) {
  return request({
    url: '/chat/room/create',
    method: 'post',
    data
  })
}

// 获取聊天室详情
export function getRoomInfo(roomId: number) {
  return request({
    url: `/chat/room/info/${roomId}`,
    method: 'get'
  })
}

// 更新聊天室信息
export function updateRoom(data: { id: number; name?: string; avatar?: string; announcement?: string }) {
  return request({
    url: '/chat/room/update',
    method: 'put',
    data
  })
}

// 邀请成员
export function inviteMembers(roomId: number, memberIds: number[], memberTypes?: number[]) {
  return request({
    url: '/chat/room/invite',
    method: 'post',
    params: { roomId, memberIds, memberTypes }
  })
}

// 踢出成员
export function kickMember(roomId: number, memberId: number, memberType: number = 2) {
  return request({
    url: '/chat/room/kick',
    method: 'post',
    params: { roomId, memberId, memberType }
  })
}

// 退出群聊
export function quitRoom(roomId: number) {
  return request({
    url: '/chat/room/quit',
    method: 'post',
    params: { roomId }
  })
}

// 获取聊天室成员列表
export function getRoomMembers(roomId: number) {
  return request({
    url: `/chat/room/members/${roomId}`,
    method: 'get'
  })
}

// 解散群聊
export function dissolveRoom(roomId: number) {
  return request({
    url: '/chat/room/dissolve',
    method: 'post',
    params: { roomId }
  })
}

// 获取或创建私聊聊天室
export function getOrCreatePrivateRoom(friendId: number, friendType: number = 1) {
  return request({
    url: '/chat/room/private',
    method: 'get',
    params: { friendId, friendType }
  })
}

// ==================== 消息相关 ====================

// 发送消息
export function sendMessage(data: {
  roomId: number
  msgType: number
  content?: string
  mediaUrl?: string
  mediaDuration?: number
  mediaSize?: number
  thumbnailUrl?: string
  replyMsgId?: number
  atUserIds?: number[]
}) {
  return request({
    url: '/chat/message/send',
    method: 'post',
    data
  })
}

// 获取历史消息
export function getHistoryMessages(roomId: number, lastMsgId?: number, pageSize: number = 20) {
  return request({
    url: '/chat/message/history',
    method: 'get',
    params: { roomId, lastMsgId, pageSize }
  })
}

// 撤回消息
export function revokeMessage(msgId: number) {
  return request({
    url: `/chat/message/revoke/${msgId}`,
    method: 'post'
  })
}

// 标记消息已读
export function markAsRead(roomId: number, msgId: number) {
  return request({
    url: '/chat/message/read',
    method: 'post',
    params: { roomId, msgId }
  })
}

// ==================== 好友相关 ====================

// 获取好友列表
export function getFriendList() {
  return request({
    url: '/chat/friend/list',
    method: 'get'
  })
}

// 发送好友申请
export function sendFriendRequest(data: { toUserId: number; toUserType?: number; message?: string }) {
  return request({
    url: '/chat/friend/request',
    method: 'post',
    data
  })
}

// 获取收到的好友申请列表
export function getReceivedRequestList() {
  return request({
    url: '/chat/friend/request/list',
    method: 'get'
  })
}

// 处理好友申请
export function handleFriendRequest(data: { requestId: number; action: number }) {
  return request({
    url: '/chat/friend/request/handle',
    method: 'post',
    data
  })
}

// 删除好友
export function deleteFriend(friendId: number, friendType: number = 1) {
  return request({
    url: '/chat/friend/delete',
    method: 'delete',
    params: { friendId, friendType }
  })
}

// ==================== 会话相关 ====================

// 获取会话列表
export function getConversationList() {
  return request({
    url: '/chat/conversation/list',
    method: 'get'
  })
}

// 置顶/取消置顶会话
export function toggleConversationTop(conversationId: number) {
  return request({
    url: '/chat/conversation/top',
    method: 'post',
    params: { conversationId }
  })
}

// 设置/取消免打扰
export function toggleConversationMute(conversationId: number) {
  return request({
    url: '/chat/conversation/mute',
    method: 'post',
    params: { conversationId }
  })
}

// 删除会话
export function deleteConversation(conversationId: number) {
  return request({
    url: `/chat/conversation/delete/${conversationId}`,
    method: 'delete'
  })
}

// 清空未读消息数
export function clearUnread(roomId: number) {
  return request({
    url: '/chat/conversation/clearUnread',
    method: 'post',
    params: { roomId }
  })
}

// ==================== 消息类型枚举 ====================
export const MessageType = {
  TEXT: 1,
  IMAGE: 2,
  VIDEO: 3,
  VOICE: 4,
  EMOJI: 5,
  SYSTEM: 6
}

export const MessageTypeLabel: Record<number, string> = {
  1: '文本',
  2: '图片',
  3: '视频',
  4: '语音',
  5: '表情',
  6: '系统消息'
}

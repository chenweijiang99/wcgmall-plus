<template>
  <!-- 悬浮聊天按钮 -->
  <div
    v-if="isLoggedIn && !isAuthPage"
    class="chat-float-btn"
    @click="openChat"
  >
    <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" :max="99">
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </el-badge>
  </div>

  <!-- 聊天对话框 -->
  <el-dialog
    v-model="chatVisible"
    title="聊天室"
    width="900px"
    :close-on-click-modal="false"
    class="chat-dialog"
    destroy-on-close
  >
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <el-tabs v-model="activeTab" class="chat-tabs">
          <el-tab-pane label="会话" name="conversations" />
          <el-tab-pane label="好友" name="friends" />
          <el-tab-pane name="requests">
            <template #label>
              <el-badge :value="unreadRequestCount" :hidden="unreadRequestCount === 0" :max="99" class="tab-badge">
                申请
              </el-badge>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 会话列表 -->
        <div v-if="activeTab === 'conversations'" class="list-container">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            :class="['list-item', { active: currentConversation?.id === conv.id }]"
            @click="selectConversation(conv)"
          >
            <el-badge :value="conv.unreadCount" :hidden="conv.unreadCount === 0" :max="99">
              <el-avatar :size="40" :src="conv.avatar || defaultAvatar" />
            </el-badge>
            <div class="item-info">
              <div class="item-header">
                <span class="item-name">{{ conv.name }}</span>
                <span class="item-time">{{ formatTime(conv.lastMsgTime) }}</span>
              </div>
              <div class="item-msg">{{ conv.lastMsgContent || '暂无消息' }}</div>
            </div>
          </div>
          <el-empty v-if="conversations.length === 0" description="暂无会话" :image-size="60" />
        </div>

        <!-- 好友列表 -->
        <div v-if="activeTab === 'friends'" class="list-container">
          <div
            v-for="friend in friends"
            :key="friend.id"
            class="list-item"
            @click="startPrivateChat(friend)"
          >
            <el-avatar :size="40" :src="friend.avatar || defaultAvatar" />
            <div class="item-info">
              <div class="item-name">{{ friend.remark || friend.nickname }}</div>
              <div class="item-status">{{ friend.online ? '在线' : '离线' }}</div>
            </div>
          </div>
          <el-empty v-if="friends.length === 0" description="暂无好友" :image-size="60" />
        </div>

        <!-- 好友申请列表 -->
        <div v-if="activeTab === 'requests'" class="list-container">
          <div v-for="req in friendRequests" :key="req.id" class="request-item">
            <el-avatar :size="40" :src="req.fromUserAvatar || defaultAvatar" />
            <div class="item-info">
              <div class="item-name">{{ req.fromUserNickname }}</div>
              <div class="item-msg">{{ req.message || '请求添加你为好友' }}</div>
            </div>
            <div v-if="req.status === 0" class="request-actions">
              <el-button size="small" @click="handleRequest(req.id, 2)">拒绝</el-button>
              <el-button size="small" type="primary" @click="handleRequest(req.id, 1)">同意</el-button>
            </div>
            <span v-else class="request-status">{{ req.status === 1 ? '已同意' : '已拒绝' }}</span>
          </div>
          <el-empty v-if="friendRequests.length === 0" description="暂无申请" :image-size="60" />
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-main">
        <template v-if="currentConversation">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <el-avatar :size="36" :src="currentConversation.avatar || defaultAvatar" />
            <div class="header-info">
              <div class="header-name">{{ currentConversation.name }}</div>
              <div class="header-status">{{ currentConversation.roomType === 2 ? '群聊' : (currentConversation.online ? '在线' : '离线') }}</div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list" @scroll="handleScroll">
            <div v-if="loading" class="loading-tip">加载中...</div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message-item', { 'message-self': msg.isSelf }]"
            >
              <el-avatar :size="32" :src="msg.senderAvatar || defaultAvatar" />
              <div class="message-wrapper">
                <div class="message-meta">
                  <span v-if="!msg.isSelf" class="sender-name">{{ msg.senderNickname }}</span>
                  <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                </div>
                <div v-if="msg.isRevoked" class="message-revoked">消息已撤回</div>
                <div v-else :class="['message-bubble', { 'bubble-self': msg.isSelf }]">
                  <template v-if="msg.msgType === 1">{{ msg.content }}</template>
                  <template v-else-if="msg.msgType === 2">
                    <el-image :src="msg.mediaUrl" :preview-src-list="[msg.mediaUrl]" style="max-width: 150px" fit="cover" />
                  </template>
                  <template v-else-if="msg.msgType === 5">
                    <span class="emoji-msg">{{ msg.content }}</span>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input">
            <div class="input-toolbar">
              <el-upload :show-file-list="false" :before-upload="handleImageSelect" accept="image/*">
                <el-button text size="small"><el-icon><Picture /></el-icon></el-button>
              </el-upload>
              <el-popover trigger="click" width="280">
                <template #reference>
                  <el-button text size="small"><el-icon><Sunny /></el-icon></el-button>
                </template>
                <div class="emoji-picker">
                  <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
                </div>
              </el-popover>
            </div>
            <div class="input-area">
              <div
                ref="editorRef"
                class="message-editor"
                contenteditable="true"
                @paste="handlePaste"
                @keydown.enter.exact.prevent="sendMessage"
                @input="handleEditorInput"
                :data-placeholder="'输入消息...'"
              ></div>
              <el-button type="primary" size="small" @click="sendMessage" :disabled="!canSend">发送</el-button>
            </div>
          </div>
        </template>

        <div v-else class="no-conversation">
          <el-icon :size="48"><ChatDotRound /></el-icon>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import {
  getConversationList,
  getFriendList,
  getReceivedRequestList,
  getHistoryMessages,
  sendMessage as sendMessageApi,
  handleFriendRequest,
  getOrCreatePrivateRoom,
  clearUnread,
  MessageType
} from '@/api/chat'
import { uploadApi } from '@/api/file'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Picture, Sunny } from '@element-plus/icons-vue'

interface Conversation {
  id: number
  roomId: number
  roomType: number
  name: string
  avatar: string
  lastMsgContent: string
  lastMsgTime: string
  unreadCount: number
  online?: boolean
}

interface Friend {
  id: number
  friendId: number
  friendType: number
  nickname: string
  avatar: string
  remark: string
  roomId: number
  online?: boolean
}

interface FriendRequest {
  id: number
  fromUserId: number
  fromUserType: number
  fromUserNickname: string
  fromUserAvatar: string
  message: string
  status: number
}

interface ChatMessage {
  id: number
  roomId: number
  senderId: number
  senderType: number
  senderNickname: string
  senderAvatar: string
  msgType: number
  content: string
  mediaUrl?: string
  isRevoked: number
  isSelf?: boolean
  createTime: string
}

const userStore = useUserStore()
const route = useRoute()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const emojis = ['😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊', '😋', '😎', '😍', '😘', '🥰', '😗', '🙂', '🤗', '🤩', '🤔', '😐', '🙄', '😏', '😢', '😭', '😤', '😱', '🥱', '😴']

const isLoggedIn = computed(() => !!userStore.user?.id)
const isAuthPage = computed(() => !!route.meta.hideLayout)

const chatVisible = ref(false)
const activeTab = ref('conversations')
const conversations = ref<Conversation[]>([])
const friends = ref<Friend[]>([])
const friendRequests = ref<FriendRequest[]>([])
const currentConversation = ref<Conversation | null>(null)
const messages = ref<ChatMessage[]>([])
const loading = ref(false)
const messageListRef = ref<HTMLElement | null>(null)
const editorRef = ref<HTMLElement | null>(null)
const ws = ref<WebSocket | null>(null)
const connected = ref(false)
const canSend = ref(false)

const unreadRequestCount = computed(() => friendRequests.value.filter(r => r.status === 0).length)
const totalUnreadCount = computed(() => conversations.value.reduce((sum, c) => sum + c.unreadCount, 0))

// 打开聊天
async function openChat() {
  chatVisible.value = true
  await fetchConversations()
  await fetchFriends()
  await fetchFriendRequests()
  connectWebSocket()
}

// 获取数据
async function fetchConversations() {
  try {
    const res = await getConversationList()
    conversations.value = res.data || []
  } catch (error) {
    console.error('获取会话列表失败', error)
  }
}

async function fetchFriends() {
  try {
    const res = await getFriendList()
    friends.value = res.data || []
  } catch (error) {
    console.error('获取好友列表失败', error)
  }
}

async function fetchFriendRequests() {
  try {
    const res = await getReceivedRequestList()
    friendRequests.value = res.data || []
  } catch (error) {
    console.error('获取好友申请列表失败', error)
  }
}

// WebSocket
function connectWebSocket() {
  if (ws.value?.readyState === WebSocket.OPEN) return
  const token = getToken()
  if (!token) return

  const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/chat?token=${token}`
  ws.value = new WebSocket(wsUrl)

  ws.value.onopen = () => {
    connected.value = true
    startHeartbeat()
  }

  ws.value.onmessage = async (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'NEW_MSG') {
        const msg = data.data as ChatMessage
        const isCurrentRoom = msg.roomId === currentConversation.value?.roomId
        if (isCurrentRoom) {
          // 当前会话的消息，添加到消息列表
          messages.value.push(msg)
          scrollToBottom()
          // 自动清空当前会话的未读数
          clearUnread(msg.roomId).catch(() => {})
        }
        // 刷新会话列表以更新未读数和最新消息
        await fetchConversations()
        // 如果是当前会话，确保未读数为0
        if (isCurrentRoom && currentConversation.value) {
          const conv = conversations.value.find(c => c.roomId === currentConversation.value?.roomId)
          if (conv) conv.unreadCount = 0
        }
      }
    } catch (error) {
      console.error('解析消息失败', error)
    }
  }

  ws.value.onclose = () => {
    connected.value = false
  }
}

let heartbeatTimer: number | null = null

function startHeartbeat() {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  heartbeatTimer = window.setInterval(() => {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify({ type: 'HEARTBEAT', data: 'ping' }))
    }
  }, 30000)
}

function disconnectWebSocket() {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  ws.value?.close()
}

// 选择会话
async function selectConversation(conv: Conversation) {
  currentConversation.value = conv
  messages.value = []
  await loadMessages()
  scrollToBottom()
  // 清空未读数
  if (conv.unreadCount > 0) {
    try {
      await clearUnread(conv.roomId)
      conv.unreadCount = 0
    } catch (error) {
      console.error('清空未读数失败', error)
    }
  }
}

async function loadMessages(lastMsgId?: number) {
  if (!currentConversation.value) return
  loading.value = true
  try {
    const res = await getHistoryMessages(currentConversation.value.roomId, lastMsgId)
    if (lastMsgId) {
      messages.value = [...(res.data || []), ...messages.value]
    } else {
      messages.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function handleScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop === 0 && messages.value.length > 0) {
    loadMessages(messages.value[0]?.id)
  }
}

// 处理编辑器输入
function handleEditorInput() {
  if (editorRef.value) {
    canSend.value = editorRef.value.innerHTML.trim() !== '' && editorRef.value.innerHTML !== '<br>'
  }
}

// 插入表情到编辑器
function insertEmoji(emoji: string) {
  if (!editorRef.value) return
  editorRef.value.focus()
  document.execCommand('insertText', false, emoji)
  handleEditorInput()
}

// 选择图片插入到编辑器
async function handleImageSelect(file: File) {
  if (!editorRef.value) return false
  try {
    const formData = new FormData()
    formData.append('file', file)
    const uploadRes = await uploadApi(formData, 'chat')
    const imgUrl = uploadRes.data
    // 插入图片到编辑器
    editorRef.value.focus()
    document.execCommand('insertHTML', false, `<img src="${imgUrl}" class="editor-image" data-url="${imgUrl}" style="max-width: 100px; max-height: 100px; vertical-align: middle; margin: 2px;" />`)
    handleEditorInput()
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
  return false
}

// 处理粘贴事件
async function handlePaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (file) {
        await handleImageSelect(file)
      }
      return
    }
  }
}

// 发送消息
async function sendMessage() {
  if (!currentConversation.value || !editorRef.value) return

  const editor = editorRef.value
  const content = editor.innerHTML.trim()
  if (!content || content === '<br>') return

  try {
    // 检查是否只有一张图片
    const images = editor.querySelectorAll('img.editor-image')
    const textContent = editor.innerText.trim()

    if (images.length === 1 && !textContent) {
      // 只有一张图片，发送图片消息
      const imgUrl = images[0].getAttribute('data-url')
      if (imgUrl) {
        await sendMessageApi({
          roomId: currentConversation.value.roomId,
          msgType: MessageType.IMAGE,
          mediaUrl: imgUrl
        })
      }
    } else if (images.length > 0) {
      // 有图片和文字混合，分别发送
      // 先发送文字
      if (textContent) {
        await sendMessageApi({
          roomId: currentConversation.value.roomId,
          msgType: MessageType.TEXT,
          content: textContent
        })
      }
      // 再发送图片
      for (const img of images) {
        const imgUrl = img.getAttribute('data-url')
        if (imgUrl) {
          await sendMessageApi({
            roomId: currentConversation.value.roomId,
            msgType: MessageType.IMAGE,
            mediaUrl: imgUrl
          })
        }
      }
    } else {
      // 只有文字
      await sendMessageApi({
        roomId: currentConversation.value.roomId,
        msgType: MessageType.TEXT,
        content: textContent
      })
    }

    // 清空编辑器
    editor.innerHTML = ''
    canSend.value = false
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

// 开始私聊
async function startPrivateChat(friend: Friend) {
  try {
    const res = await getOrCreatePrivateRoom(friend.friendId, friend.friendType)
    activeTab.value = 'conversations'
    await fetchConversations()
    const conv = conversations.value.find(c => c.roomId === res.data.id)
    if (conv) selectConversation(conv)
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

// 处理好友申请
async function handleRequest(requestId: number, action: number) {
  try {
    await handleFriendRequest({ requestId, action })
    ElMessage.success(action === 1 ? '已同意' : '已拒绝')
    await fetchFriendRequests()
    if (action === 1) await fetchFriends()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

// 关闭对话框时断开WebSocket
watch(() => chatVisible.value, (val) => {
  if (!val) {
    disconnectWebSocket()
    currentConversation.value = null
    messages.value = []
  }
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.chat-float-btn {
  position: fixed;
  right: 95px;
  bottom: 160px;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
  z-index: 999;
  color: white;
}

.chat-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}

.chat-container {
  display: flex;
  height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.chat-sidebar {
  width: 280px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-tabs {
  padding: 0 12px;
}

.list-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.list-item:hover {
  background: #f0f0f0;
}

.list-item.active {
  background: #e6f4ff;
}

.item-info {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.item-time {
  font-size: 11px;
  color: #909399;
}

.item-msg, .item-status {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.request-actions {
  display: flex;
  gap: 6px;
}

.request-status {
  font-size: 12px;
  color: #909399;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.header-info {
  margin-left: 10px;
}

.header-name {
  font-weight: 500;
  font-size: 15px;
}

.header-status {
  font-size: 12px;
  color: #909399;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.loading-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 8px;
}

.message-item {
  display: flex;
  margin-bottom: 12px;
}

.message-item.message-self {
  flex-direction: row-reverse;
}

.message-item.message-self .message-wrapper {
  align-items: flex-end;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  margin: 0 8px;
  max-width: 65%;
}

.message-meta {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.sender-name {
  margin-right: 6px;
}

.message-bubble {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 12px;
  background: #f4f4f5;
  word-break: break-word;
  font-size: 14px;
}

.message-bubble.bubble-self {
  background: #409eff;
  color: #fff;
}

.message-revoked {
  font-style: italic;
  color: #909399;
  font-size: 12px;
}

.emoji-msg {
  font-size: 20px;
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding: 10px;
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.input-area {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.message-editor {
  flex: 1;
  min-height: 50px;
  max-height: 120px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow-y: auto;
  outline: none;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-editor:focus {
  border-color: #409eff;
}

.message-editor:empty::before {
  content: attr(data-placeholder);
  color: #c0c4cc;
  pointer-events: none;
}

.message-editor img.editor-image {
  max-width: 100px;
  max-height: 100px;
  vertical-align: middle;
  margin: 2px;
  border-radius: 4px;
}

.emoji-picker {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.emoji-item {
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  text-align: center;
}

.emoji-item:hover {
  background: #f0f0f0;
}

.no-conversation {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.no-conversation p {
  margin-top: 12px;
}

.tab-badge :deep(.el-badge__content) {
  top: 8px;
}
</style>

<style>
.chat-dialog .el-dialog__body {
  padding: 0;
}
</style>

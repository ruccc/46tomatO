import { axios } from '../../utils/request'

// 消息相关的类型定义
export interface Message {
  id: number
  senderId: number
  senderName: string
  senderAvatar: string
  receiverId: number
  receiverName: string
  receiverAvatar: string
  content: string
  contentType: 'text' | 'image' | 'link'
  status: number // 1-已发送, 2-已读
  createTime: string
  isSender: boolean
}

export interface ConversationResponse {
  content: Message[]
  pageable: {
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
    offset: number
    pageNumber: number
    pageSize: number
    paged: boolean
    unpaged: boolean
  }
  totalPages: number
  totalElements: number
  last: boolean
  size: number
  number: number
  sort: {
    sorted: boolean
    unsorted: boolean
    empty: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}

export interface SendMessageRequest {
  senderId: number
  receiverId: number
  content: string
  contentType: 'text' | 'image' | 'link'
}

// 发送私信
export const sendMessage = (data: SendMessageRequest) => {
  return axios.post('/api/messages/send', data)
}

// 获取对话消息
export const getConversation = (userId: number, contactId: number, page = 0, size = 20) => {
  return axios.get('/api/messages/conversation', {
    params: {
      userId,
      contactId,
      page,
      size
    }
  })
}

// 获取联系人列表
export const getContacts = (userId: number) => {
  return axios.get('/api/messages/contacts', {
    params: { userId }
  })
}

// 获取最新消息
export const getLatestMessages = (userId: number, contactId: number, size = 5) => {
  return axios.get('/api/messages/latest', {
    params: {
      userId,
      contactId,
      size
    }
  })
}

// 获取未读消息总数
export const getUnreadCount = (userId: number) => {
  return axios.get('/api/messages/unread/count', {
    params: { userId }
  })
}

// 获取来自特定用户的未读消息数
export const getUnreadCountFromUser = (userId: number, senderId: number) => {
  return axios.get('/api/messages/unread/count/user', {
    params: {
      userId,
      senderId
    }
  })
}

// 标记消息为已读
export const markAsRead = (userId: number, senderId: number) => {
  return axios.post('/api/messages/mark-as-read', null, {
    params: {
      userId,
      senderId
    }
  })
}

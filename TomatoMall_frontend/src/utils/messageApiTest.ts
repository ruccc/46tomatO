import { sendMessage, getConversation, getContacts } from '../api/messages'
import type { SendMessageRequest } from '../api/messages'

// 消息API测试工具
export class MessageApiTester {
  private userId: number

  constructor(userId = 1) {
    this.userId = userId
  }

  // 测试发送消息
  async testSendMessage(receiverId = 2, content = '测试消息', contentType: 'text' | 'image' | 'link' = 'text') {
    try {
      console.log('=== 测试发送消息 ===')
      console.log('发送参数:', { 
        senderId: this.userId, 
        receiverId, 
        content, 
        contentType 
      })
      
      const messageData: SendMessageRequest = {
        senderId: this.userId,
        receiverId,
        content,
        contentType
      }
      
      const response = await sendMessage(messageData)
      
      console.log('发送消息响应:', response.data)
      return response.data
    } catch (error) {
      console.error('发送消息失败:', error)
      throw error
    }
  }

  // 测试获取对话
  async testGetConversation(contactId = 2, page = 0, size = 20) {
    try {
      console.log('=== 测试获取对话 ===')
      console.log('查询参数:', { userId: this.userId, contactId, page, size })
      
      const response = await getConversation(this.userId, contactId, page, size)
      
      console.log('对话响应:', response.data)
      return response.data
    } catch (error) {
      console.error('获取对话失败:', error)
      throw error
    }
  }

  // 测试获取联系人
  async testGetContacts() {
    try {
      console.log('=== 测试获取联系人 ===')
      console.log('查询参数:', { userId: this.userId })
      
      const response = await getContacts(this.userId)
      
      console.log('联系人响应:', response.data)
      return response.data
    } catch (error) {
      console.error('获取联系人失败:', error)
      throw error
    }
  }

  // 综合测试
  async runAllTests() {
    console.log('🚀 开始消息API综合测试...')
    
    try {
      // 1. 测试获取联系人
      await this.testGetContacts()
      
      // 2. 测试发送消息
      await this.testSendMessage(2, '这是一条测试消息')
      
      // 3. 测试获取对话
      await this.testGetConversation(2)
      
      console.log('✅ 所有测试完成')
    } catch (error) {
      console.error('❌ 测试过程中出现错误:', error)
    }
  }
}

// 快速测试函数
export const quickTest = async (userId = 1) => {
  const tester = new MessageApiTester(userId)
  await tester.runAllTests()
}

// 在浏览器控制台中使用的全局测试函数
declare global {
  interface Window {
    messageApiTest: typeof quickTest
    MessageApiTester: typeof MessageApiTester
  }
}

if (typeof window !== 'undefined') {
  window.messageApiTest = quickTest
  window.MessageApiTester = MessageApiTester
}

/**
 * API测试工具 - 用于测试评论API是否正常工作
 */
import { COMMENTS_MODULE } from '../api/_prefix'
import axios from 'axios'

export const testCommentApi = async () => {
  console.group('评论API测试')
  
  // 使用硬编码的测试数据
  const testData = {
    bookId: "1",  // 使用一个已知存在的书籍ID
    userId: "3",  // 使用一个测试用户ID
    content: "这是一个测试评论 " + new Date().toISOString(),
    rating: 5
  }
  
  console.log('测试数据:', testData)
  
  try {
    // 直接使用axios发送请求，避免依赖现有实现
    const response = await axios({
      method: 'post',
      url: `${COMMENTS_MODULE}/`,
      data: testData,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('API响应:', response.data)
    console.log('测试结果: 成功')
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    console.error('API错误:', error)
    console.error('测试结果: 失败')
    
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误响应数据:', error.response.data)
    }
    
    return {
      success: false,
      error
    }
  } finally {
    console.groupEnd()
  }
}

// 如果需要，可以在控制台调用: import('./utils/apiTester').then(m => m.testCommentApi())

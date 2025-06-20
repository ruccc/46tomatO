import axios from 'axios'
import { COMMENTS_MODULE } from '../api/_prefix'

/**
 * 直接测试评论API的函数
 * 可以在浏览器控制台中调用以测试API
 */
export const testCommentApi = async () => {
  // 测试所有可能的格式组合
  const testCases = [
    // 测试1: 字符串ID + 字符串书ID
    {
      bookId: "1",
      userId: "3",
      content: "测试评论 - 字符串ID - " + new Date().toISOString(),
      rating: 5
    },
    // 测试2: 数字ID + 字符串书ID
    {
      bookId: "1",
      userId: 3,
      content: "测试评论 - 数字ID - " + new Date().toISOString(),
      rating: 5
    },
    // 测试3: 数字ID + 数字书ID
    {
      bookId: 1,
      userId: 3,
      content: "测试评论 - 全数字ID - " + new Date().toISOString(),
      rating: 5
    },
    // 测试4: 额外字段
    {
      bookId: 1,
      userId: 3,
      userName: "测试用户",
      content: "测试评论 - 额外字段 - " + new Date().toISOString(),
      rating: 5
    }
  ]
  
  console.group('评论API测试')
  
  for (let i = 0; i < testCases.length; i++) {
    const testCase = testCases[i]
    console.log(`测试${i+1}:`, testCase)
    
    try {
      const response = await axios.post(`${COMMENTS_MODULE}/`, testCase)
      console.log(`测试${i+1}成功:`, response.data)
    } catch (error) {
      console.error(`测试${i+1}失败:`, error)
      if (error.response) {
        console.error('错误状态码:', error.response.status)
        console.error('错误详情:', error.response.data)
      }
    }
  }
  
  console.groupEnd()
}

// 在控制台中使用: import('./utils/commentApiTest').then(m => m.testCommentApi())

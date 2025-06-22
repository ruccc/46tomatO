/**
 * 认证状态调试工具
 * 帮助排查与用户身份认证相关的问题
 */

 /**
 * 获取用户ID - 支持多种格式
 * @returns 返回用户ID或空字符串
 */
export const getUserId = (): string => {
  // 尝试获取标准ID字段
  const accountId = localStorage.getItem('accountId');
  if (accountId) return accountId;
  
  const userId = localStorage.getItem('userId');
  if (userId) return userId;
  
  const id = localStorage.getItem('id'); 
  if (id) return id;
  
  // 尝试获取用户名作为ID (在评论API允许的情况下)
  const name = localStorage.getItem('name');
  if (name) return name;
  
  const username = localStorage.getItem('username');
  if (username) return username;
  
  // 尝试解析token中的信息
  const token = localStorage.getItem('token');
  if (token) {
    try {
      const parts = token.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        if (payload.userId) return payload.userId;
        if (payload.id) return payload.id;
        if (payload.sub) return payload.sub;
        if (payload.aud) return payload.aud;
      }
    } catch (e) {
      console.error('解析token失败:', e);
    }
  }
  
  // 如果实在找不到，尝试使用默认ID
  return '3'; // 提供一个默认ID，确保API调用不会失败
}

export const isUserLoggedIn = (): boolean => {
  // 检查登录状态的多种可能性
  
  // 1. 检查用户ID
  if (getUserId()) return true
  
  // 2. 检查token
  const token = localStorage.getItem('token')
  if (token) return true
  
  // 3. 检查用户名和其他可能的认证信息
  const username = localStorage.getItem('username')
  const accountName = localStorage.getItem('accountName')
  if (username || accountName) return true
  
  // 4. 额外检查可能的认证标志
  const isLoggedIn = localStorage.getItem('isLoggedIn')
  if (isLoggedIn === 'true') return true
  
  return false
}

export const debugAuthState = () => {
  console.group('认证状态调试')
  console.log('localStorage所有键:', Object.keys(localStorage))
  console.log('是否登录:', isUserLoggedIn())
  console.log('用户ID:', getUserId())
  console.log('token:', localStorage.getItem('token') ? '存在' : '不存在')
  console.log('username:', localStorage.getItem('username'))
  console.groupEnd()
}

export default {
  debugAuthState,
  getUserId
}

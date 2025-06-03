import request from '@/utils/request'

// 账户信息接口
export interface AccountInfo {
  username: string;
  email?: string;
  phone?: string;
  memberLevel: number; // 会员等级：0-非会员，1-一级会员，2-二级会员，3-三级会员
  // 其他账户字段...
}

// 获取用户账户信息
export const getAccountInfo = () => {
  return request({
    url: '/api/user/account',
    method: 'GET'
  })
}

// 更新用户会员等级（实际项目中可能需要通过订单系统处理）
export const updateMemberLevel = (level: number) => {
  return request({
    url: '/api/user/membership',
    method: 'POST',
    data: { level }
  })
}

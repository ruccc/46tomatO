import {axios} from '../../utils/request'
import {PRODUCTS_MODULE} from '../_prefix'

//查询指定商品的库存
export const getStockpileInfo = (productId: string) => {
    return axios.get(`${PRODUCTS_MODULE}/stockpile/${productId}`)
        .then(res => {
            return res
        })
        .catch(error => {
            console.error('获取库存信息失败:', error)
            throw error
        })
}

//调整指定商品库存 - 使用PATCH方法调用库存API
export const adjustStockpile = (productId: string, amount: number) => {
    console.log(`正在调用库存调整API - 产品ID: ${productId}, 调整量: ${amount}`)
    
    // 直接使用PATCH方法，不再添加自定义CORS头部
    return axios.patch(`${PRODUCTS_MODULE}/stockpile/${productId}`, { 
        amount: amount
    }, {
        headers: { 'Content-Type': 'application/json' },
        timeout: 60000
    })
    .then(res => {
        console.log('库存调整API返回结果:', res)
        return res
    })
    .catch(error => {
        console.error('库存调整API错误:', error)
        
        if (error.message === 'Network Error') {
            error.isNetworkError = true
            error.friendlyMessage = '网络连接错误，请检查您的网络连接或确认后端API路径是否正确'
        } else if (error.response) {
            console.error(`请求失败: ${error.response.status} - ${JSON.stringify(error.response.data)}`)
        }
        
        throw error
    })
}
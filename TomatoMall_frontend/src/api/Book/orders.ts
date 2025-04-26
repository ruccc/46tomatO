import {axios} from '../../utils/request'
import { API_MODULE } from '../_prefix'

const ORDER_MODULE = `${API_MODULE}/orders`

// 订单信息类型定义
export interface Order {
    orderId: string;
    username: string;
    totalAmount: number;
    paymentMethod: string;
    createTime: string;
    status: string;
}

// 支付信息类型定义
export interface PaymentInfo {
    paymentForm: string;
    orderId: string;
    totalAmount: number;
    paymentMethod: string;
}

// 初始化支付
export const initPayment = (orderId: string) => {
    return axios.post(`${ORDER_MODULE}/${orderId}/pay`, null, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    }).catch(error => {
        console.error('初始化支付失败:', error)
        throw error
    })
}

// 处理支付回调
export const handlePaymentCallback = (params: any) => {
    return axios.post(`${ORDER_MODULE}/notify`, params, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    }).catch(error => {
        console.error('处理支付回调失败:', error)
        throw error
    })
}

// 获取订单列表
export const getOrders = () => {
    return axios.get(`${ORDER_MODULE}`)
        .then(res => {
            return res
        }).catch(error => {
            console.error('获取订单列表失败:', error)
            throw error
        })
}

// 获取订单详情
export const getOrderDetail = (orderId: string) => {
    return axios.get(`${ORDER_MODULE}/${orderId}`)
        .then(res => {
            return res
        }).catch(error => {
            console.error('获取订单详情失败:', error)
            throw error
        })
}
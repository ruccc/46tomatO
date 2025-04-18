import {axios} from '../../utils/request'
import { API_MODULE } from '../_prefix'

const CART_MODULE = `${API_MODULE}/cart`

// 购物车商品类型定义
export interface CartItem {
    cartItemId: string;
    productId: string;
    title: string;
    price: number;
    description: string;
    cover: string;
    detail: string;
    quantity: number;
    isExistingItem?: boolean; // 添加isExistingItem字段，可选参数
}

// 购物车响应类型
export interface CartResponse {
    items: CartItem[];
    total: number;
    totalAmount: number;
}

// 添加商品到购物车
export const addToCart = (productId: string, quantity: number) => {
    return axios.post(`${CART_MODULE}`, {
        productId,
        quantity
    }, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    }).catch(error => {
        console.error('添加到购物车失败:', error)
        throw error
    })
}

// 从购物车中删除商品
export const removeFromCart = (cartItemId: string) => {
    return axios.delete(`${CART_MODULE}/${cartItemId}`)
        .then(res => {
            return res
        }).catch(error => {
            console.error('从购物车删除商品失败:', error)
            throw error
        })
}

// 修改购物车商品数量
export const updateCartQuantity = (cartItemId: string, quantity: number) => {
    return axios.patch(`${CART_MODULE}/${cartItemId}`, {
        quantity
    }, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    }).catch(error => {
        console.error('更新购物车数量失败:', error)
        throw error
    })
}

// 获取购物车商品列表
export const getCartItems = () => {
    return axios.get(`${CART_MODULE}`)
        .then(res => {
            return res
        }).catch(error => {
            console.error('获取购物车列表失败:', error)
            throw error
        })
}

// 地址信息类型
export interface ShippingAddress {
    name: string;
    phone: string;
    province: string;
    city: string;
    district: string;
    address: string;
    zipCode: string;
}

// 结算请求
export interface CheckoutRequest {
    cartItemIds: string[];
    shipping_address: ShippingAddress;
    payment_method: string;
}

// 结算购物车
export const checkout = (checkoutRequest: CheckoutRequest) => {
    return axios.post(`${CART_MODULE}/checkout`, checkoutRequest, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    }).catch(error => {
        console.error('结算购物车失败:', error)
        throw error
    })
}
import {axios} from '../../utils/request'
import {PRODUCTS_MODULE} from '../_prefix'

export type UpdateInfo = {
    id: string,
    title?: string,
    price?: number,
    rate?: number,
    description?: string,
    cover?: string,
    detail?: string,
    specifications?: Set<Specification>;
}

export interface Specification {
    id: string;
    item: string;
    value: string;
    productId: string;
}

export type AddInfo = {
    title: string,
    price: number,
    rate: number,
    description?: string,
    cover?: string,
    detail?: string,
    specifications?: Specification[],
    stockAmount?: number
}

//获取商品列表
export const getListInfo = () => {
    return axios.get(`${PRODUCTS_MODULE}`)
        .then(res => {
            return res
        })
}

//获取指定商品
export const getInfo = (id: string) => {
    return axios.get(`${PRODUCTS_MODULE}/${id}`)
        .then(res => {
            return res
        })
}

// 更新商品信息
export const updateInfo = (updateInfo: UpdateInfo) => {
    return axios.put(`${PRODUCTS_MODULE}`, updateInfo, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    })
}

export const addInfo = (addInfo: AddInfo) => {
    return axios.post(`${PRODUCTS_MODULE}`, addInfo, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res
    })
}

// 删除商品
export const deleteInfo = (id: string) => {
    return axios.delete(`${PRODUCTS_MODULE}/${id}`)
        .then(res => {
            return res
        })
}

// 简化搜索商品API实现，避免可能的参数错误
export const searchBooks = (keyword: string) => {
    // 使用 encodeURIComponent 确保关键字中的特殊字符被正确编码
    return axios.get(`${PRODUCTS_MODULE}/search?keyword=${encodeURIComponent(keyword)}`)
        .then(res => {
            return res
        })
        .catch(error => {
            // 添加错误日志，帮助排查问题
            console.error('搜索API请求失败:', error)
            throw error
        })
}
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
    specifications?: Set<Specification>;
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
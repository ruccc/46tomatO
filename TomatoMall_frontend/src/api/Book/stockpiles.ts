import {axios} from '../../utils/request'
import {STOCKPILES_MODULE} from '../_prefix'

//查询指定商品的库存
export const getStockpileInfo = (productId: string) => {
    return axios.get(`${STOCKPILES_MODULE}/${productId}`)
        .then(res => {
            return res
        })
}

//调整指定商品库存
export const adjustStockpile = (productId: string, amount: number) => {
    return axios.patch(`${STOCKPILES_MODULE}/${productId}`, { amount }, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        return res;
    })
};
import { axios } from '../../utils/request'

const PREFIX = '/api'

export interface Advertisement {
  id?: string
  title: string
  content: string
  imgUrl: string
  productId: string
}

interface Result<T> {
  code: number
  msg?: string
  data: T
}

export const getAdvertisements = () => {
  return axios.get<Result<Advertisement[]>>(`${PREFIX}/advertisements`)
}

export const createAdvertisement = (data: Advertisement) => {
  return axios.post<Result<Advertisement>>(`${PREFIX}/advertisements`, data)
}

export const updateAdvertisement = (data: Advertisement) => {
  return axios.put<Result<string>>(`${PREFIX}/advertisements`, data)
}

export const deleteAdvertisement = (id: string) => {
  return axios.delete<Result<string>>(`${PREFIX}/advertisements/${id}`)
}
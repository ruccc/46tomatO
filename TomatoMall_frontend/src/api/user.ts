import {axios} from '../utils/request'
/**
 * 或者
 * import axios from '../utils/request'
 * import { AxiosResponse } from 'axios';
 * */
import {USER_MODULE} from './_prefix'
import type {AxiosResponse} from "axios";


type  UserDetail = {
    username: string,
    name: string,
    role: string;
    avatar: string,
    telephone: string,
    email: string,
    location: string,
}

type RegisterInfo = {
    username: string,
    password: string,
    name: string,
    avatar?: string,
    role: string,
    telephone?: string,
    email?: string,
    location?: string,
}

type LoginInfo = {
    username: string,
    password: string
}

type UpdateInfo = {
    password?: string,
    name?: string,
    avatar?: string,
    role?: string,
    telephone?: string,
    email?: string,
    location?: string,
}

//接口响应类型
interface BaseResponse<T> {
    code: string;  // code 改为字符串类型
    msg: string | null; // 新增 msg 字段
    data: T;
}

type UserDetailResponse = BaseResponse<UserDetail>;
type RegisterResponse = BaseResponse<string>; // data 为字符串
type LoginResponse = BaseResponse<string>;     // data 为 token 字符串
type UpdateResponse = BaseResponse<string>;   // data 为 "更新成功" 等字符串

// 如果有“Vue: This may be converted to an async function”警告，可以不管
/**
 * 1. 获取用户详情
 * GET /api/accounts/{username}
 * Headers 需携带 cookie
 */
export const getUserDetail = (username: string, token: string) => {
    return axios.get<UserDetailResponse>(`${USER_MODULE}/${username}`, {
        headers: {token}
    }).then((res: AxiosResponse<UserDetailResponse>) => res.data);
};

/**
 * 2. 创建用户
 * POST /api/accounts
 * 请求参数为 JSON body
 */
export const createUser = (registerInfo: RegisterInfo) => {
    return axios.post<RegisterResponse>(`${USER_MODULE}`, registerInfo, {
        headers: { 'Content-Type': 'application/json' }
    }).then((res:AxiosResponse<RegisterResponse>) => res.data); // 只返回 { code, data }，方便前端处理
};

/**
 * 3. 用户登录
 * POST /api/accounts/login
 * 返回的 token 会通过 Set-Cookie 自动保存，后续请求需携带 cookie
 */
export const userLogin = (loginInfo: LoginInfo) => {
    return axios.post<LoginResponse>(`${USER_MODULE}/login`, loginInfo, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        const token = res.data.data;
        localStorage.setItem('token', token);
        return res.data;
    });
};

/**
 * 4. 更新用户信息
 * PUT /api/accounts
 * Headers 需携带 cookie
 */
export const updateUserInfo = (updateInfo: UpdateInfo, token: string) => {
    return axios.put<UpdateResponse>(`${USER_MODULE}`, updateInfo, {
        headers: {
            'Content-Type': 'application/json',
            token
        }
    }
    ).then((res: AxiosResponse<UpdateResponse>) => res.data)
};

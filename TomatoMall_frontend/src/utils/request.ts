import axios from 'axios'

// //创建一个axios的实例service
// const service = axios.create()
//

// 配置 baseURL（使用全局或自定义）
const service = axios.create({
    baseURL: 'http://localhost:8080', // 本地后端服务器地址
    timeout: 30000
});

// //判断是否登录
// function hasToken() {
//     return !(sessionStorage.getItem('token') == '')
// }

//当前实例的请求拦截器，对所有要发送给后端的请求进行处理，在其中加入token
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.token = token;
        }
        return config;
    },
    error => {
        //console.log(error);
        return Promise.reject(error);
    }
);

//当前实例的响应拦截器，对所有从后端收到的请求进行处理，检验http的状态码
service.interceptors.response.use(
    response => {
            return response;
    },
    error => {
        if (error.response?.status === 401) {
            // 清除所有用户相关信息
            localStorage.removeItem('token');
            localStorage.removeItem('role');
            localStorage.removeItem('username');
            
            // 获取当前路径，用于登录后重定向回来
            const currentPath = window.location.pathname;
            const queryString = currentPath !== '/login' ? `?redirect=${encodeURIComponent(currentPath)}` : '';
            
            // 使用window.location进行导航，确保能跨越所有页面情况
            window.location.href = `/login${queryString}`;
        }
        return Promise.reject(error);
    }
);

//设置为全局变量
export {service as axios}

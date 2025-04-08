import {createRouter, createWebHashHistory} from "vue-router"
import {getUserDetail} from "../api/user";

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            redirect: '/register',
        },
        {
            path: '/login',
            component: () => import('../views/user/Login.vue'),
            meta: {title: '用户登录', anonymous: true}// 允许未登录访问
        },
        {
            path: '/register',
            component: () => import('../views/user/Register.vue'),
            meta: {title: '用户注册', anonymous: true}
        },
        {
            path: '/home',
            redirect: '/dashboard',
            component: () => import('../views/Home.vue'),
            meta: {requiresAuth: false},
            children: [
                {
                    path: '/dashboard',
                    name: 'Dashboard',
                    component: () => import('../views/user/Dashboard.vue'),
                    meta: {title: '个人信息'}
                },
            ]
        },
        {
            path: '/404',
            name: '404',
            component: () => import('../views/NotFound.vue'),
            meta: {title: '404'}
        },
        {
            path: '/:catchAll(.*)',
            redirect: '/404'
        }
    ]
});

// 全局前置路由守卫,_实际为from
router.beforeEach(async (to, _, next) => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role')

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title as string;
    }

    // 允许匿名访问的路由（如登录、注册）
    if(to.meta.anonymous) {
        next();
        return;
    }

    // 需要登录的页面
    if (to.meta.requiresAuth) {
        if (!token) {
            // 未登录跳转到登录页
            next('/login');
            return;
        }

        // 动态获取用户角色（如果本地未存储）
        if (!role) {
            try {
                const username = localStorage.getItem('username');// 假设用户名已存储
                const res = await getUserDetail(username!, token);
                localStorage.setItem('role', res.data.role);
            } catch (error) {
                // Token 失效处理
                localStorage.removeItem('role');
                localStorage.removeItem('token');
                next('/login');
                return;
            }
        }
        //检查权限
        if (to.meta.permission) {
            const requiredRoles = to.meta.permission as string[];
            if (!requiredRoles.includes(localStorage.getItem('role')!)) {
                next('/404');
                return;
            }
        }

        next();
    }
    else {
        next();
    }
});

export {router}

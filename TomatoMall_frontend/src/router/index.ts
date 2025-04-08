import { createRouter, createWebHistory } from 'vue-router'
import { getUserDetail } from "../api/user"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/user/Login.vue'),
      meta: { 
        title: '用户登录',
        anonymous: true 
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/user/Register.vue'),
      meta: { 
        title: '用户注册',
        anonymous: true 
      }
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('../views/Main.vue'),
      meta: { 
        title: '主页面',
        requiresAuth: true 
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { 
        title: '个人信息',
        requiresAuth: true 
      }
    },
    {
      path: '/products',
      name: 'productList',
      component: () => import('../views/product/ProductList.vue'),
      meta: { 
        title: '商品列表',
        requiresAuth: true 
      }
    },
    {
      path: '/products/create',
      name: 'productCreate',
      component: () => import('../views/product/ProductCreate.vue'),
      meta: { 
        title: '新建商品',
        requiresAuth: true,
        permission: ['admin']
      }
    },
    {
      path: '/products/:id',
      name: 'productDetail',
      component: () => import('../views/product/ProductDetail.vue'),
      meta: { 
        title: '商品详情',
        requiresAuth: true 
      }
    },
    // 添加新的路由配置
    {
      path: '/products/:id/update',
      name: 'productUpdate',
      component: () => import('../views/product/ProductUpdate.vue'),
      meta: { 
        title: '更新商品',
        requiresAuth: true,
        permission: ['admin']
      }
    },
    {
      path: '/products/:id/stockpile',
      name: 'stockpile',
      component: () => import('../views/product/Stockpile.vue'),
      meta: { 
        title: '库存管理',
        requiresAuth: true,
        permission: ['admin']
      }
    },
    {
      path: '/404',
      name: '404',
      component: () => import('../views/NotFound.vue'),
      meta: { title: '404' }
    },
    {
      path: '/:catchAll(.*)',
      redirect: '/404'
    }
  ]
})

// 保持原有的路由守卫代码
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

export { router }
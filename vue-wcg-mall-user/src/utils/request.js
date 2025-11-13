//定制请求的实例

//导入axios  npm install axios
import { ElMessage } from 'element-plus'
import axios from 'axios';
//定义一个变量,记录公共的前缀  ,  baseURL
const baseURL = '/api';
const instance = axios.create({
    baseURL,
    headers: { "Content-Type": "application/json;charset=utf-8" },
    timeout: 300000,
});
//请求拦截器
import { useTokenStore } from '@/stores/token.js';
// import { useRouter } from 'vue-router';
// const router = useRouter();
import router from '@/router';
instance.interceptors.request.use(
    config => {
        // console.log(config)
        //添加token
        const tokenStore = useTokenStore();
        if (tokenStore.token) {
            config.headers['Authorization'] = tokenStore.token.token;
        }
        return config;
    },
    err => {
        // alert('请求错误');
        return Promise.reject(err);
    }
)

//添加响应拦截器
let is401Handled = false;
instance.interceptors.response.use(
    result => {
        return result.data;
        // if(result.data.code === 1){
        //     return result.data;
        // }
        // // alert(result.data.msg?result.data.msg:'服务异常')
        // ElMessage.error(result.data.msg?result.data.msg:'服务异常')
        // return Promise.reject(result.data);
    },
    err => {
        if (err.response.status === 401) {
            if (!is401Handled) {
                ElMessage({
                    showClose: true,
                    type: 'error',
                    message: '请先登录',
                    plain: true,
                });
                const tokenStore = useTokenStore();
                tokenStore.removeToken();
                router.push('/login')
                is401Handled = true;
            }
            router.push('/login')
        } else {
            ElMessage({
                showClose: true,
                type: 'error',
                message: '服务异常',
                plain: true,
            });
        }

        return Promise.reject(err);//异步的状态转化成失败的状态
    }
)

export default instance;
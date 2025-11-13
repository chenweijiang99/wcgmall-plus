// import './assets/main.css'
// import './assets/bootstrap.min.css'
// import '@/assets/base.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-persistedstate-plugin'

import App from './App.vue'
import router from '@/router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'bootstrap'
import 'bootstrap/dist/css/bootstrap.min.css'
import "bootstrap-icons/font/bootstrap-icons.css"
import "animate.css"
import 'cropperjs/dist/cropper.css';
import BaiduMap from 'vue-baidu-map-3x'


const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
const pinia = createPinia()
const persist = createPersistedState()

pinia.use(persist)
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(BaiduMap, {
    ak: 'Na1xGATrBDbjHikDCkl5u1WviNf8wF3P'
})
app.mount('#app')


import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { setupStore } from '@/store'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/global.scss'

import { setupElIcons, setupPermission } from "@/plugins";
import ButtonGroup from '@/components/ButtonGroup/index.vue'
import permission from '@/directives/permission'

const app = createApp(App)

import SvgIcon from '@/components/SvgIcon/index.vue'
app.component('svg-icon', SvgIcon)

import mavonEditor from "mavon-editor";
import "mavon-editor/dist/css/index.css";
app.use(mavonEditor);

// 初始化权限
setupStore(app)

setupPermission() // 初始化路由权限

app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.component('ButtonGroup', ButtonGroup)

setupPermission()
setupElIcons(app)

app.directive('permission', permission)

app.mount('#app')
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css'; 
import '@/styles/style.css'; 
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import 'virtual:svg-icons-register'; 
import SvgIcon from '@/components/svgIcon/index.vue'; 
import { setupStore } from '@/stores'

const app = createApp(App);

setupStore(app)
app.component('svg-icon', SvgIcon);

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(router);
app.use(ElementPlus);
router.isReady().then(() => {
  app.mount('#app')
})
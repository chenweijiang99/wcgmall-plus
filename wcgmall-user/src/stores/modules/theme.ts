import { reactive, watch } from 'vue';
// 定义状态接口
interface AppState {
  theme: 'light' | 'dark';
}

// 初始化状态
export const themeStore = reactive<AppState>({
  theme: 'light',
});

// --- Theme Logic ---
export const toggleTheme = () => {
  themeStore.theme = themeStore.theme === 'light' ? 'dark' : 'light';
};

watch(() => themeStore.theme, (newTheme) => {
  if (newTheme === 'dark') {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
}, { immediate: true });


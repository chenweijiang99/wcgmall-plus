import { defineStore } from 'pinia'
import { getPublicSiteConfigApi } from '@/api/system/config'

export interface SettingsState {
  theme: 'light' | 'dark'
  themeColor: string
  showTags: boolean
  showLogo: boolean
  fontSize: 'small' | 'default' | 'large'
  pageAnimation: 'slide' | 'fade' | 'dissolve' | 'none'
  watermark: boolean
  tagsStyle: 'card' | 'border' | 'modern'
  dynamicTitle: boolean
  greyMode: boolean
  showFooter: boolean
  title: string
  sidebarStyle: 'dark' | 'light'
  // 网站配置
  siteConfig: Record<string, string>
  siteConfigLoaded: boolean
}

export const useSettingsStore = defineStore({
  id: 'settings',
  
  state: (): SettingsState => ({
    theme: 'light',
    themeColor: '#7374a6',
    showTags: true,
    showLogo: true,
    fontSize: 'default',
    pageAnimation: 'dissolve',
    watermark: false,
    tagsStyle: 'border',
    dynamicTitle: false,
    greyMode: false,
    showFooter: true,
    title: '文创购管理系统',
    sidebarStyle: 'light',
    siteConfig: {},
    siteConfigLoaded: false
  }),

  getters: {
    // 获取后台网站名称
    adminTitle: (state) => state.siteConfig.site_admin_title || state.title,
    // 获取后台Logo
    adminLogo: (state) => state.siteConfig.site_admin_logo || ''
  },

  actions: {
    // 预览设置
    previewSettings(settings: Partial<SettingsState>) {
      // 应用主题色
      if (settings.themeColor) {
        // 更新 CSS 变量
        document.documentElement.style.setProperty('--el-color-primary', settings.themeColor)
        // 生成不同透明度的主题色变体
        const colorObj = this.generateThemeColors(settings.themeColor)
        Object.entries(colorObj).forEach(([key, value]) => {
          document.documentElement.style.setProperty(key, value)
        })
      }
      
      // 添加字体大小设置
      if (settings.fontSize) {
        document.documentElement.setAttribute('data-size', settings.fontSize)
      }
      console.log(settings)
      // 更新设置并立即保存
      Object.assign(this, settings)
      sessionStorage.setItem('settings', JSON.stringify(this.$state))
    },

    // 生成主题色的不同透明度变体
    generateThemeColors(primary: string) {
      const colors = {
        '--el-color-primary': primary,
        '--el-color-primary-light-1': this.lighten(primary, 0.1),
        '--el-color-primary-light-2': this.lighten(primary, 0.2),
        '--el-color-primary-light-3': this.lighten(primary, 0.3),
        '--el-color-primary-light-4': this.lighten(primary, 0.4),
        '--el-color-primary-light-5': this.lighten(primary, 0.5),
        '--el-color-primary-light-6': this.lighten(primary, 0.6),
        '--el-color-primary-light-7': this.lighten(primary, 0.7),
        '--el-color-primary-light-8': this.lighten(primary, 0.8),
        '--el-color-primary-light-9': this.lighten(primary, 0.9),
        '--el-color-primary-dark-1': this.darken(primary, 0.1),
        '--el-color-primary-dark-2': this.darken(primary, 0.2),
      }
      return colors
    },

    // 颜色变浅
    lighten(color: string, amount: number): string {
      return this.adjustColor(color, amount, true)
    },

    // 颜色变深
    darken(color: string, amount: number): string {
      return this.adjustColor(color, amount, false)
    },

    // 调整颜色
    adjustColor(color: string, amount: number, lighten: boolean): string {
      const rgb = this.hexToRgb(color)
      if (!rgb) return color

      const adjust = (value: number) => {
        if (lighten) {
          return Math.min(255, value + (255 - value) * amount)
        }
        return Math.max(0, value * (1 - amount))
      }

      const r = Math.round(adjust(rgb.r))
      const g = Math.round(adjust(rgb.g))
      const b = Math.round(adjust(rgb.b))

      return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`
    },

    // 十六进制转 RGB
    hexToRgb(hex: string) {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
      return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
      } : null
    },

    // 保存设置
    saveSettings(settings: any) {

      // 更新状态
      Object.assign(this, settings)
      // 保存到 sessionStorage
      sessionStorage.setItem('settings', JSON.stringify(this.$state))
      
      // 应用灰色模式
      if ('greyMode' in settings) {
        if (settings.greyMode) {
          document.documentElement.classList.add('grey-mode')
        } else {
          document.documentElement.classList.remove('grey-mode')
        }
      }
      
      // 初始化深色模式
      if (settings.theme === 'dark') {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
      if (settings.themeColor) {
        document.documentElement.style.setProperty('--el-color-primary', settings.themeColor)
        const colors = this.generateThemeColors(settings.themeColor)
        Object.entries(colors).forEach(([key, value]) => {
          document.documentElement.style.setProperty(key, value)
        })
      }
      // 添加字体大小设置
      if (settings.fontSize) {
        document.documentElement.setAttribute('data-size', settings.fontSize)
      }
    },

    // 重置设置
    resetSettings() {
      const defaultSettings: SettingsState = {
        theme: 'light' as const,
        themeColor: '#7374a6',
        showTags: true,
        showLogo: true,
        fontSize: 'default' as const,
        pageAnimation: 'dissolve' as const,
        watermark: false,
        tagsStyle: 'modern',
        dynamicTitle: true,
        greyMode: false,
        showFooter: true,
        title: '文创购管理系统',
        sidebarStyle: 'light'
      }
      this.saveSettings(defaultSettings)
    },

    // 修改初始化方法
    initTheme() {
      const savedSettings = sessionStorage.getItem('settings')
      if (savedSettings) {
        try {
          const settings = JSON.parse(savedSettings)
          this.saveSettings(settings)
        } catch (error) {
          console.error('Failed to parse saved settings:', error)
        }
      }
    },

    // 获取网站配置
    async fetchSiteConfig() {
      if (this.siteConfigLoaded) return

      try {
        const res = await getPublicSiteConfigApi()
        if (res.code === 200 && res.data) {
          this.siteConfig = res.data
          this.siteConfigLoaded = true
        }
      } catch (error) {
        console.error('获取网站配置失败:', error)
      }
    },

    // 刷新网站配置（强制重新获取）
    async refreshSiteConfig() {
      this.siteConfigLoaded = false
      await this.fetchSiteConfig()
    }

  }
}) 
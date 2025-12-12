import { defineStore } from 'pinia'
import { getSiteConfigApi } from '@/api/siteConfig'

interface SiteConfigState {
  config: Record<string, string>
  loaded: boolean
}

export const useSiteConfigStore = defineStore('siteConfig', {
  state: (): SiteConfigState => ({
    config: {},
    loaded: false
  }),

  getters: {
    // Logo相关
    adminLogo: (state) => state.config.site_admin_logo || '',
    adminTitle: (state) => state.config.site_admin_title || 'WCG管理系统',
    userHeaderLogo: (state) => state.config.site_user_header_logo || '',
    userFooterLogo: (state) => state.config.site_user_footer_logo || '',
    userTitle: (state) => state.config.site_user_title || 'WCG Store',

    // Footer相关
    footerDescription: (state) => state.config.site_footer_description || '',
    footerCopyright: (state) => state.config.site_footer_copyright || '',
    footerAddress: (state) => state.config.site_footer_address || '',
    footerEmail: (state) => state.config.site_footer_email || '',
    footerIcp: (state) => state.config.site_footer_icp || ''
  },

  actions: {
    async fetchConfig() {
      if (this.loaded) return

      try {
        const res = await getSiteConfigApi()
        if (res.code === 200 && res.data) {
          this.config = res.data
          this.loaded = true
        }
      } catch (error) {
        console.error('获取网站配置失败:', error)
      }
    },

    getConfigValue(key: string, defaultValue: string = '') {
      return this.config[key] || defaultValue
    }
  }
})

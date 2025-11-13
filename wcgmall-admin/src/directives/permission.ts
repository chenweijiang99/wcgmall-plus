import { DirectiveBinding } from 'vue'
import { useUserStore } from '@/store/modules/user'

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.user.permissions

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(permission => {
        return value.includes(permission)
      })

      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  }
} 
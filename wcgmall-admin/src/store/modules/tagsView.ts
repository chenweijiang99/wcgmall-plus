import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'
import { useRouter } from 'vue-router'
import path from 'path-browserify'

export interface TagView extends Partial<RouteLocationNormalized> {
  title?: string
}

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref<TagView[]>([])
  const cachedViews = ref<string[]>([])
  const router = useRouter()

  // 初始化固定标签
  const initTags = () => {
    // 首先添加仪表盘
    const dashboardRoute = router.getRoutes().find(route => route.path === '/dashboard' || route.path === '/')
    if (dashboardRoute) {
      addVisitedView(dashboardRoute)
    }

    // 然后添加其他固定标签
    const affixTags = filterAffixTags(router.getRoutes())
    affixTags
      .filter(tag => tag.path !== '/dashboard' && tag.path !== '/') // 排除仪表盘
      .forEach(tag => {
        addVisitedView(tag)
      })
  }

  // 过滤固定标签
  const filterAffixTags = (routes: any[], basePath = '/') => {
    let tags: any[] = []
    routes.forEach(route => {
      if (route.meta?.affix) {
        const tagPath = path.resolve(basePath, route.path)
        tags.push({
          ...route,
          path: tagPath
        })
      }
      if (route.children) {
        const childTags = filterAffixTags(route.children, route.path)
        if (childTags.length >= 1) {
          tags = [...tags, ...childTags]
        }
      }
    })
    return tags
  }

  const addVisitedView = (view: TagView) => {
    // 不添加 redirect 路由的标签
    if (view.path.includes('/redirect')) return
    
    // 如果已经存在相同路径的标签，则不添加
    if (visitedViews.value.some(v => v.path === view.path)) return
    
    // 创建新标签
    const newTag = Object.assign({}, view, {
      title: view.meta?.title || 'no-name'
    })

    // 如果是仪表盘，插入到数组开头
    if (view.path === '/dashboard' || view.path === '/') {
      visitedViews.value.unshift(newTag)
    } else {
      // 其他标签添加到末尾
      visitedViews.value.push(newTag)
    }

    // 对标签进行排序，确保仪表盘始终在第一位
    visitedViews.value.sort((a, b) => {
      if (a.path === '/dashboard' || a.path === '/') return -1
      if (b.path === '/dashboard' || b.path === '/') return 1
      return 0
    })
  }

  const addCachedView = (view: TagView) => {
    if (cachedViews.value.includes(view.name as string)) return
    if (view.meta?.keepAlive !== false) {
      cachedViews.value.push(view.name as string)
    }
  }

  const delVisitedView = (view: TagView) => {
    // 不能删除固定标签
    if (view.meta?.affix) return
    
    const i = visitedViews.value.findIndex(v => v.path === view.path)
    if (i > -1) {
      visitedViews.value.splice(i, 1)
    }
  }

  const delCachedView = (view: TagView) => {
    const index = cachedViews.value.indexOf(view.name as string)
    if (index > -1) {
      cachedViews.value.splice(index, 1)
    }
  }

  const delOthersViews = (view: TagView) => {
    visitedViews.value = visitedViews.value.filter(v => {
      return v.meta?.affix || v.path === view.path
    })
  }

  const delAllViews = () => {
    // 保留固定的标签
    visitedViews.value = visitedViews.value.filter(tag => tag.meta?.affix)
    cachedViews.value = []
  }

  const delLeftViews = (view: TagView) => {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) {
      visitedViews.value = visitedViews.value.filter((v, i) => {
        return v.meta?.affix || i >= index
      })
    }
  }

  const delRightViews = (view: TagView) => {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) {
      visitedViews.value = visitedViews.value.filter((v, i) => {
        return v.meta?.affix || i <= index
      })
    }
  }

  return {
    visitedViews,
    cachedViews,
    addVisitedView,
    addCachedView,
    delVisitedView,
    delCachedView,
    delOthersViews,
    delAllViews,
    delLeftViews,
    delRightViews,
    initTags
  }
}) 
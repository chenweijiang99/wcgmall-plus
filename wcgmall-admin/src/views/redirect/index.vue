<script setup lang="ts">
import { onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/store/modules/tagsView'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

onBeforeMount(() => {
  const { params, query } = route
  const { path } = params
  const redirectPath = '/' + (Array.isArray(path) ? path.join('/') : path)
  
  // 删除当前的 redirect 标签
  tagsViewStore.delVisitedView(route)
  
  router.replace({
    path: redirectPath,
    query
  })
})
</script>

<template>
  <div></div>
</template> 
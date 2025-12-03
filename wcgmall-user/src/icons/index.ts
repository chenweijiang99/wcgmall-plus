// 导入所有 svg 图标
const importAll = (requireContext: __WebpackModuleApi.RequireContext) => {
  return requireContext.keys().map(requireContext)
}

try {
  // 导入 svg 目录下的所有 .svg 文件
  const req = require.context('./svg', false, /\.svg$/)
  importAll(req)
} catch (error) {
  console.log('导入图标失败', error)
}

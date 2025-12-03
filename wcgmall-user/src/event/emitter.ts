import mitt from 'mitt';

// 定义事件名称和对应的参数类型
type Events = {
  // 当购物车发生变化时触发（添加、删除、修改数量）
  'refresh': void; 
  // 你可以在这里扩展其他全局事件
};

// 创建并导出类型安全的 emitter 实例
export const emitter = mitt<Events>();
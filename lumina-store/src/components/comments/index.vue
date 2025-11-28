<script setup lang="ts">
import { ref } from 'vue';
import { Send } from 'lucide-vue-next';
import { useUserStore } from "@/stores/modules/user";
import { Comment } from '@/types';

const store = useUserStore();
const comments = ref<Comment[]>([]);

const emit = defineEmits<{
  (e: 'add-comment', text: string): void
}>();

const text = ref('');

const handleSubmit = () => {
  if (text.value.trim()) {
    emit('add-comment', text.value);
    text.value = '';
  }
};
</script>

<template>
  <div class="mt-12 border-t border-gray-200 dark:border-zinc-800 pt-8">
    <h3 class="text-2xl font-bold mb-6">评论({{ comments.length }})</h3>
    
    <!-- 评论列表 -->
    <div class="space-y-6 mb-8">
      <div v-if="comments.length === 0" class="text-gray-500 italic">
        成为第一个分享你想法的人
      </div>
      <div v-else v-for="comment in comments" :key="comment.id" class="flex gap-4">
        <img :src="comment.userAvatar" :alt="comment.userName" class="w-10 h-10 rounded-full object-cover bg-gray-200" />
        <div>
          <div class="flex items-center gap-2 mb-1">
            <span class="font-semibold">{{ comment.userName }}</span>
            <span class="text-xs text-gray-400">{{ comment.date }}</span>
          </div>
          <p class="text-gray-700 dark:text-gray-300 leading-relaxed">{{ comment.content }}</p>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div v-if="store.user" class="relative flex gap-4 items-start">
      <img :src="store.user.avatar" alt="You" class="w-10 h-10 rounded-full hidden sm:block" />
      <div class="flex-1">
        <el-input
          v-model="text"
          placeholder="写下你的想法"
          size="large"
          class="w-full"
          @keyup.enter="handleSubmit"
        >
          <template #suffix>
            <el-button 
              type="primary" 
              link 
              @click="handleSubmit" 
              :disabled="!text.trim()"
              class="!p-0"
            >
              <Send :size="18" />
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
    <div v-else class="p-4 bg-gray-50 dark:bg-zinc-900 rounded-xl text-center text-sm text-gray-500">
      请登录发表评论
    </div>
  </div>
</template>
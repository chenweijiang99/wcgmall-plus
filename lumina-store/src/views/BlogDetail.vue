<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getBlogById } from '@/services/mockData';
import { BlogPost, Comment } from '@/types';
import Comments from '@/components/comments/index.vue';


const route = useRoute();
const post = ref<BlogPost | undefined>(undefined);

watch(() => route.params.id, async (id) => {
  if (id) {
    post.value = await getBlogById(id as string);
  }
}, { immediate: true });

const handleAddComment = (text: string) => {
  if (!post.value || !store.user) return;
  const newComment: Comment = {
    id: `bc-${Date.now()}`,
    userId: store.user.id,
    userName: store.user.name,
    userAvatar: store.user.avatar,
    content: text,
    date: 'Just now'
  };
  post.value.comments = [newComment, ...post.value.comments];
};
</script>

<template>
  <div v-if="!post" class="pt-32 text-center">Loading story...</div>
  <div v-else class="pt-24 pb-20">
    <article class="max-w-3xl mx-auto px-4 sm:px-6 mb-16">
      <div class="text-center mb-12">
        <div class="flex justify-center gap-2 mb-6">
          <span v-for="tag in post.tags" :key="tag" class="text-blue-600 font-medium text-sm uppercase tracking-wider">{{ tag }}</span>
        </div>
        <h1 class="text-4xl md:text-5xl font-bold mb-6 leading-tight">{{ post.title }}</h1>
        <div class="flex items-center justify-center gap-3 text-gray-500">
          <span>By {{ post.author }}</span>
          <span>•</span>
          <span>{{ post.date }}</span>
        </div>
      </div>
    </article>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 mb-16">
      <img :src="post.image" :alt="post.title" class="w-full h-[500px] object-cover rounded-3xl" />
    </div>

    <div class="max-w-3xl mx-auto px-4 sm:px-6">
      <div class="prose prose-lg dark:prose-invert prose-zinc mx-auto mb-16">
        <p class="lead text-xl text-gray-600 dark:text-gray-300">{{ post.excerpt }}</p>
        <div class="mt-8 text-gray-800 dark:text-gray-200 whitespace-pre-wrap leading-relaxed">
          {{ post.content }}
          <p class="mt-4">
            [Full article content placeholder. In a real app, this would be rich text retrieved from the CMS or backend.]
          </p>
        </div>
      </div>

      <Comments :comments="post.comments" @add-comment="handleAddComment" />
    </div>
  </div>
</template>
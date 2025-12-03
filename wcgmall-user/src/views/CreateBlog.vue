<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const formData = ref({
  title: '',
  excerpt: '',
  content: '',
  image: ''
});
const isSubmitting = ref(false);

onMounted(() => {
  
});

const handleSubmit = () => {
  isSubmitting.value = true;
  // Simulate API call
  setTimeout(() => {
    isSubmitting.value = false;
    // alert('Blog post published successfully! (Mock)');
    router.push('/blog');
  }, 1000);
};
</script>

<template>
  <div class="pt-24 pb-20 max-w-3xl mx-auto px-4 sm:px-6">
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">Write a Story</h1>
      <p class="text-gray-500">Share your thoughts with the community.</p>
    </div>

    <el-form :model="formData" label-position="top" size="large" @submit.prevent="handleSubmit">
      <el-form-item label="Title" required>
        <el-input 
          v-model="formData.title" 
          placeholder="Enter an engaging title..." 
          class="!text-2xl font-bold"
        />
      </el-form-item>

      <el-form-item label="Cover Image URL" required>
        <el-input 
          v-model="formData.image" 
          placeholder="https://..."
        >
          <template #prepend>HTTPS://</template>
        </el-input>
      </el-form-item>

      <el-form-item label="Excerpt" required>
        <el-input 
          v-model="formData.excerpt" 
          type="textarea" 
          :rows="2" 
          placeholder="A short summary..."
        />
      </el-form-item>

      <el-form-item label="Content" required>
        <el-input 
          v-model="formData.content" 
          type="textarea" 
          :rows="10" 
          placeholder="Tell your story..."
        />
      </el-form-item>

      <div class="flex justify-end gap-4 pt-4">
        <el-button @click="router.push('/blog')">Cancel</el-button>
        <el-button 
          type="primary" 
          native-type="submit" 
          :loading="isSubmitting"
          class="!rounded-full !px-8 !text-base !font-bold"
        >
          Publish Story
        </el-button>
      </div>
    </el-form>
  </div>
</template>
<script setup>
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
</script>
<template>
  <headerView></headerView>

  <router-view></router-view>

  <footerView></footerView>
</template>
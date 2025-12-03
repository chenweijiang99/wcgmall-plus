import { defineStore } from "pinia";
import { ref } from "vue";
import { Cart } from "@/types";

export const useCartStore = defineStore("cart", () => {
  // 存储用户在购物车勾选的商品列表
  const selectedCartItems = ref<Cart[]>([]);
  
  // 存储选中的总金额
  const selectedTotal = ref(0);

  const setSelectedItems = (items: Cart[]) => {
    selectedCartItems.value = items;
    selectedTotal.value = items.reduce((acc, item) => acc + item.productPrice * item.number, 0);
  };

  return { selectedCartItems, selectedTotal, setSelectedItems };
});
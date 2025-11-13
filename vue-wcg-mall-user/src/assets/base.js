import { ref, onMounted } from 'vue';
		
window.onload = function() {
  // 获取当前页面的滚动位置
  let currentPosition = window.pageYOffset || document.documentElement.scrollTop;

  // 如果当前位置不是0，则执行滚动到顶部的动画
  if (currentPosition > 0) {
    window.scrollTo({
      top: 0,
      behavior: 'smooth' // 使用平滑滚动效果
    });
  }
};
export const useScrollToTop = () => {
  const backtopStyle = ref({
    backgroundColor: "#474747",
    color: "#ffffff",
  });

  const handleMouseEnter = () => {
    backtopStyle.value.backgroundColor = "#4cc1f0";
  };

  const handleMouseLeave = () => {
    backtopStyle.value.backgroundColor = "#474747";
  };

  const showLoader = ref(true);

  onMounted(() => {
    setTimeout(() => {
      showLoader.value = false;
    }, 500);
  });

  return {
    backtopStyle,
    showLoader,
    handleMouseEnter,
    handleMouseLeave,
  };
};
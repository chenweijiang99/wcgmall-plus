<template>
  <el-button type="primary" @click="dialogVisible = true">上传</el-button>

  <el-dialog v-model="dialogVisible" title="更改头像" width="500">
    <el-upload
      action="#"
      :auto-upload="false"
      :on-change="handleImageChange"
      :before-upload="beforeUpload"
    >
      <el-button type="primary">选择图片</el-button>
    </el-upload>
    <div v-if="imageUrl">
        <img ref="image" :src="imageUrl" alt="Cropper Image" style="max-width: 100%; height: auto;" />
    </div>
    <el-button type="primary" @click="cropImage">剪裁并上传</el-button>
  </el-dialog>
</template>

<script setup>
import { ref } from "vue";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
import { ElMessage } from "element-plus";

const dialogVisible = ref(true);
const imageUrl = ref(null);
const image = ref(null);
let cropper = ref(null);

const handleImageChange = (file) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    imageUrl.value = e.target.result;
    setTimeout(initCropper, 0); // 延迟调用
  };
  reader.readAsDataURL(file.raw);
};

const beforeUpload = (file) => {
  const isJpgOrPng = file.type === "image/jpeg" || file.type === "image/png";
  if (!isJpgOrPng) {
    ElMessage.error("只能上传JPG/PNG格式的图片!");
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
  }
  return isJpgOrPng && isLt2M;
};

      
const initCropper = () => {
  if (image.value) {
    if (cropper.value) {
      cropper.value.destroy();
    }
    cropper.value = new Cropper(image.value, {
      viewMode: 1,
      dragMode: "move",
      background: true,
      autoCropArea: 0.8,
      zoomOnWheel: true,
      center: true,
      aspectRatio: 1,
    });
  } else {
    console.error("Image element not found");
  }
};

      
	

const cropImage = () => {
  if (cropper.value) {
    const canvas = cropper.value.getCroppedCanvas();
    canvas.toBlob(async (blob) => {
      if (blob) {
        const formData = new FormData();
        formData.append("file", blob);
        // 这里可以调用上传图片的服务
        const result = await uploadImageService(formData); // 假设你有一个上传服务
        if (result.code === 1) {
          uploadSuccess(result); // 调用上传成功的处理函数
        } else {
          ElMessage.error(result.message ? result.message : "上传失败");
        }
      } else {
        ElMessage.error("剪裁失败，请重试");
      }
    });
  }
};
</script>
<template>
  <div style="padding: 20px 0">
    <div style="margin-bottom: 20px">
      <div style="font-size: 24px; font-weight: bold; margin-bottom: 10px">
        评论 {{ data.commentCount }}
      </div>
      <el-input
        style="margin-bottom: 5px"
        type="textarea"
        :rows="5"
        v-model="data.form.comment"
        placeholder="请输入评论"
      ></el-input>
      <div style="text-align: right">
        <el-button color="#444343" @click="addComment(null)" round size="large"
          >评论</el-button
        >
      </div>
    </div>

    <div>
      <div v-for="item in data.commentList" :key="item.id">
        <div
          style="
            display: flex;
            align-items: flex-start;
            grid-gap: 20px;
            margin-bottom: 10px;
          "
        >
          <img
            :src="item.userAvatar"
            alt=""
            style="width: 50px; height: 50px; border-radius: 50%"
          />
          <div style="flex: 1; width: 0">
            <div style="margin-bottom: 10px; color: #666">
              {{ item.username }}
            </div>
            <div style="margin-bottom: 10px; text-align: justify">
              {{ item.comment }}
            </div>
            <div style="margin-bottom: 10px; color: #666; font-size: 13px">
              <span>{{ item.createTime }}</span>
              <span
                style="margin: 0 20px; cursor: pointer"
                @click="handleReply(item)"
                :class="{ 'active-btn': item.showReply }"
                >回复</span
              >
              <span
                style="cursor: pointer"
                v-if="item.userId === data.user.id"
                @click="del(item.id)"
                >删除</span
              >
            </div>
            <div v-if="item.showReply">
              <el-input
                type="textarea"
                v-model="item.replyContent"
                placeholder="请输入评论"
              ></el-input>
              <div style="margin-top: 5px; text-align: right">
                <el-button type="primary" @click="addComment(item)">保存</el-button>
              </div>
            </div>
          </div>
        </div>
        <!-- 二级评论开始 -->
        <div style="padding-left: 70px" v-if="item.children?.length">
          <div v-for="subItem in item.children" :key="subItem.id">
            <div
              style="
                display: flex;
                align-items: flex-start;
                grid-gap: 20px;
                margin-bottom: 10px;
              "
            >
              <img
                :src="subItem.userAvatar"
                alt=""
                style="width: 50px; height: 50px; border-radius: 50%"
              />
              <div style="flex: 1; width: 0">
                <div style="margin-bottom: 10px; color: #666">
                  {{ subItem.username }}
                  <span v-if="subItem.parentUserName !== item.username"
                    >回复 @{{ subItem.parentUserName }}</span
                  >
                </div>
                <div style="margin-bottom: 10px; text-align: justify">
                  {{ subItem.comment }}
                </div>
                <div style="margin-bottom: 10px; color: #666; font-size: 13px">
                  <span>{{ subItem.createTime }}</span>
                  <span
                    style="margin: 0 20px; cursor: pointer"
                    @click="handleReply(subItem)"
                    :class="{ 'active-btn': subItem.showReply }"
                    >回复</span
                  >
                  <span
                    style="cursor: pointer"
                    v-if="subItem.userId === data.user.id"
                    @click="del(subItem.id)"
                    >删除</span
                  >
                </div>
                <div v-if="subItem.showReply">
                  <el-input
                    type="textarea"
                    v-model="subItem.replyContent"
                    placeholder="请输入评论"
                  ></el-input>
                  <div style="margin-top: 5px; text-align: right">
                    <el-button type="primary" @click="addComment(subItem)"
                      >保存</el-button
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 二级评论结束 -->
      </div>

      <div style="padding: 20px 0" v-if="data.total">
        <el-pagination
          size="small"
          @current-change="load"
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addCommentsApi,
  getCountApi,
  deleteCommentsApi,
  selectTreeApi,
} from "@/api/comments.js";
import { CommentView } from "@/types";
import { useUserStore } from "@/stores/modules/user";
const userStore = useUserStore();
const route = useRoute();
const data = reactive({
  user: userStore.user,
  fid: route.params.id,
  commentCount: 0,
  form: {} as any,
  commentList: [] as CommentView[],
  pageNum: 1,
  pageSize: 8,
  total: 0,
});

const props = defineProps({
  module: String,
});

const handleReply = (comment: any) => {
  comment.showReply = !comment.showReply;
};

const del = (id: any) => {
  ElMessageBox.confirm("删除后数据无法恢复，您确定删除吗？", "删除确认", {
    type: "warning",
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(async (res) => {
      await deleteCommentsApi(id);
      ElMessage({
        showClose: true,
        type: "success",
        message: "删除成功",
        plain: true,
      });
      load();
    })
    .catch((err) => {
      console.error(err);
    });
};

const load = async () => {
  let result = await selectTreeApi({
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    fid: data.fid,
    module: props.module,
  });
  if (result.code === 200) {
    data.commentList = result.data?.records;
    data.total = result.data?.total;
  }

  let result2 = await getCountApi(data.fid, props.module);
  if (result2.code === 200) {
    data.commentCount = result2.data;
  }
};
// load();

const addComment = async (parentComment : any) => {
  if (parentComment) {
    data.form.pid = parentComment.id;
    data.form.comment = parentComment.replyContent;
  }
  if (!data.form.comment) {
    ElMessage({
      showClose: true,
      type: "error",
      message: "评论不能为空",
      plain: true,
    });
    return;
  }
  data.form.fid = data.fid;
  data.form.module = props.module;
  let result = await addCommentsApi(data.form);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "发表评论成功",
      plain: true,
    });
    data.form = {};
    load();
  }
};

onMounted(() => {
  load();
});
</script>

<style scoped>
.active-btn {
  color: #1890ff;
}
</style>

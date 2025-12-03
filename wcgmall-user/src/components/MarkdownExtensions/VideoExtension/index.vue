<template>
    <div class="video-extension">
        <DropdownToolbar title="视频" :visible="state.visible" @onChange="onChange">
            <template #overlay>
                <div class="video-dropdown">
                    <div class="video-options">
                        <div class="video-option" @click="handleUploadVideo">
                            <el-icon>
                                <Upload />
                            </el-icon>
                            <span>上传视频</span>
                        </div>
                        <div class="video-option" @click="handleAddVideoUrl">
                            <el-icon>
                                <Link />
                            </el-icon>
                            <span>添加视频地址</span>
                        </div>
                        <div class="video-option" @click="handleAddBilibiliVideo">
                            <el-icon>
                                <VideoPlay />
                            </el-icon>
                            <span>添加B站视频嵌入代码</span>
                        </div>
                    </div>
                </div>
            </template>
            <template #trigger>
                <svg class="bi bi-camera-video" width="1em" height="1em" viewBox="0 0 16 16" fill="currentColor"
                    xmlns="http://www.w3.org/2000/svg" stroke="currentColor" stroke-width="0.5">
                    <path fill-rule="evenodd"
                        d="M2.667 3.5c-.645 0-1.167.522-1.167 1.167v6.666c0 .645.522 1.167 1.167 1.167h6.666c.645 0 1.167-.522 1.167-1.167V4.667c0-.645-.522-1.167-1.167-1.167H2.667zM.5 4.667C.5 3.47 1.47 2.5 2.667 2.5h6.666c1.197 0 2.167.97 2.167 2.167v6.666c0 1.197-.97 2.167-2.167 2.167H2.667A2.167 2.167 0 0 1 .5 11.333V4.667z" />
                    <path fill-rule="evenodd"
                        d="M11.25 5.65l2.768-1.605a.318.318 0 0 1 .482.263v7.384c0 .228-.26.393-.482.264l-2.767-1.605-.502.865 2.767 1.605c.859.498 1.984-.095 1.984-1.129V4.308c0-1.033-1.125-1.626-1.984-1.128L10.75 4.785l.502.865z" />
                </svg>
            </template>
        </DropdownToolbar>

        <!-- 添加视频地址对话框 -->
        <el-dialog v-model="dialogVisible" center title="添加视频地址" width="400px">
            <el-input v-model="videoUrl" placeholder="请输入视频地址，例如：https://example.com/video.mp4" />
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="insertVideoUrl">确 定</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 添加B站视频嵌入代码对话框 -->
        <el-dialog v-model="bilibiliDialogVisible" center title="添加B站视频嵌入代码" width="400px">
            <el-input v-model="bilibiliCode" placeholder="请输入B站视频嵌入代码" />
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="bilibiliDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="addBilibiliVideo">确 定</el-button>
                </div>
            </template>
        </el-dialog>


        <!-- 视频上传组件（隐藏） -->
        <el-upload ref="uploadRef" style="display: none" :show-file-list="false" name="file" action=""
            :http-request="handleUploadRequest" multiple>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { PropType } from 'vue'
import type { InsertContentGenerator } from 'md-editor-v3'
import { DropdownToolbar } from 'md-editor-v3'
import { VideoCamera, Upload, Link, VideoPlay } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { uploadApi, deleteFileApi } from '@/api/file'
const props = defineProps({
    onInsert: {
        type: Function as PropType<(generator: InsertContentGenerator) => void>,
        default: () => () => null
    },
    onUploadVideo: {
        type: Function as PropType<(options: UploadRequestOptions) => void>,
        default: () => () => null
    }
})

const state = reactive({
    visible: false
})

const dialogVisible = ref(false)
const videoUrl = ref('')
const uploadRef = ref()

const bilibiliDialogVisible = ref(false)
const bilibiliCode = ref('')



const onChange = (visible: boolean) => {
    state.visible = visible
}

const handleUploadVideo = () => {
    // 触发文件选择
    state.visible = false
    setTimeout(() => {
        uploadRef.value?.$el.querySelector('input').click()
    }, 100)
}

const handleAddVideoUrl = () => {
    state.visible = false
    dialogVisible.value = true
    videoUrl.value = ''
}

const handleAddBilibiliVideo = () => {
    state.visible = false
    bilibiliDialogVisible.value = true
    bilibiliCode.value = ''
}



const handleUploadRequest = (options: UploadRequestOptions): Promise<void> => {
    // 调用父组件的上传处理方法
    props.onUploadVideo(options)
    // 返回一个 resolve 的 Promise 以满足类型要求
    return Promise.resolve()
}

const insertVideoUrl = () => {
    if (!videoUrl.value) {
        ElMessage.warning('请输入视频地址')
        return
    }

    try {
        new URL(videoUrl.value)
    } catch (e) {
        ElMessage.warning('请输入有效的视频地址')
        return
    }

    // 生成视频标签并插入
    const videoTag = `\n<video controls style="width: 100%; height: auto;" src="${videoUrl.value}">\n  您的浏览器不支持视频标签\n</video>\n`

    const generator: InsertContentGenerator = () => {
        return {
            targetValue: videoTag,
            select: true,
            deviationStart: 0,
            deviationEnd: 0
        }
    }

    props.onInsert(generator)
    dialogVisible.value = false
    videoUrl.value = ''
}
const addBilibiliVideo = () => {
    if (!bilibiliCode.value) {
        ElMessage.warning('请输入B站视频嵌入代码')
        return
    }

    const generator: InsertContentGenerator = () => {
        return {
            targetValue: bilibiliCode.value,
            select: true,
            deviationStart: 0,
            deviationEnd: 0
        }
    }

    props.onInsert(generator)
    bilibiliDialogVisible.value = false
    bilibiliCode.value = ''

}


</script>

<script lang="ts">
export default {
    name: 'VideoExtension'
}
</script>

<style scoped>
*{
    font-family: 'Handwriting', 'sans-serif';;
}
.video-extension {
    display: inline-flex;
    align-items: center;
    height: 100%;
}

.video-toolbar-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 4px;
    cursor: pointer;
    margin: 0 2px;
    color: #606266;
}

.video-toolbar-icon:hover {
    background-color: #f0f0f0;
    color: #409eff;
}

.video-dropdown {
    padding: 10px;
    min-width: 150px;
}

.video-options {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.video-option {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    color: #606266;
}

.video-option:hover {
    background-color: #f0f0f0;
    color: #409eff;
}

.video-option .el-icon {
    margin-right: 8px;
    font-size: 16px;
}

.dialog-footer {
    text-align: right;
    margin-top: 20px;
}

:root[data-theme='dark'] .video-toolbar-icon:hover {
    background-color: #444;
}

:root[data-theme='dark'] .video-option:hover {
    background-color: #444;
}

:root[data-theme='dark'] .video-toolbar-icon {
    color: #c0c0c0;
}
</style>
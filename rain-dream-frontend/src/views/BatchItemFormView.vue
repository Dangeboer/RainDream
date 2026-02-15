<template>
  <section class="card-panel panel">
    <h2>批量上传资源</h2>
    <el-form
      :model="form"
      label-position="top"
      size="small"
      class="form-grid compact-form"
    >
      <el-form-item label="媒体类型*">
        <el-select v-model="form.mediaType" placeholder="请选择媒体类型">
          <el-option
            v-for="option in mediaTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="内容类型*">
        <el-select v-model="form.contentType" placeholder="请选择内容类型">
          <el-option
            v-for="option in contentTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="原作*">
        <el-input v-model="form.fandom" />
      </el-form-item>

      <el-form-item label="CP*">
        <el-input v-model="form.cp" />
      </el-form-item>

      <el-form-item class="span-2" label="批量文件*">
        <el-upload
          drag
          multiple
          :auto-upload="false"
          :show-file-list="false"
          :accept="acceptByMediaType"
          @change="onUploadChange"
        >
          <div class="upload-tip">拖拽文件到这里，或点击选择文件</div>
          <div class="upload-sub-tip">
            当前媒体类型：{{ currentMediaTypeLabel || "未选择" }}
          </div>
        </el-upload>

        <div class="file-toolbar" v-if="selectedFiles.length">
          <span>已选择 {{ selectedFiles.length }} 个文件</span>
          <el-button @click="clearFiles">清空</el-button>
        </div>

        <el-table
          v-if="selectedFiles.length"
          :data="selectedFiles"
          stripe
          size="small"
          class="file-table"
          max-height="280"
        >
          <el-table-column prop="name" label="文件名" min-width="300" />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">
              {{ formatSize(row.size) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button type="danger" @click="removeFile($index)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>

      <el-form-item class="span-2">
        <el-button type="primary" :loading="submitting" @click="submit"
          >保存</el-button
        >
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { createBatchItemApi } from "../api/item";
import { deleteOssObjectApi, presignOssUploadApi } from "../api/oss";

const router = useRouter();
const submitting = ref(false);
const selectedFiles = ref([]);

const mediaTypeOptions = [
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 5, label: "视频" },
];

const contentTypeOptions = [
  { value: 2, label: "图绘" },
  { value: 3, label: "精修" },
  { value: 4, label: "混剪" },
  { value: 5, label: "解析" },
  { value: 6, label: "吐槽" },
  { value: 7, label: "主创说" },
  { value: 8, label: "RPS" },
  { value: 9, label: "其他" },
];

const form = reactive({
  mediaType: null,
  contentType: null,
  fandom: "风声",
  cp: "玉梦",
});

const currentMediaTypeLabel = computed(() => {
  const match = mediaTypeOptions.find(
    (item) => item.value === Number(form.mediaType),
  );
  return match?.label || "";
});

const acceptByMediaType = computed(() => {
  if (Number(form.mediaType) === 5) return "video/*,.mp4,.mov,.m4v,.webm";
  return "image/*,.jpg,.jpeg,.png,.webp,.gif,.heic,.heif";
});

const formatSize = (size = 0) => {
  if (!size) return "0 B";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
};

const onUploadChange = (uploadFile) => {
  const file = uploadFile?.raw;
  if (!file) return;
  if (!form.mediaType) {
    ElMessage.warning("请先选择媒体类型");
    return;
  }

  const duplicate = selectedFiles.value.some(
    (entry) =>
      entry.name === file.name &&
      entry.size === file.size &&
      entry.lastModified === file.lastModified,
  );
  if (duplicate) return;

  selectedFiles.value.push(file);
};

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1);
};

const clearFiles = () => {
  selectedFiles.value = [];
};

watch(
  () => form.mediaType,
  (next, prev) => {
    if (next !== prev && selectedFiles.value.length) {
      clearFiles();
    }
  },
);

const uploadFileToOss = async (file) => {
  const contentType = file?.type || "application/octet-stream";
  const presign = await presignOssUploadApi({
    file_name: file?.name || "file.bin",
    content_type: contentType,
    size: file?.size || 0,
    media_type: Number(form.mediaType),
  });

  const uploadUrl = presign?.uploadUrl ?? presign?.upload_url;
  const fileUrl = presign?.fileUrl ?? presign?.file_url;
  if (!uploadUrl || !fileUrl) {
    throw new Error("签名返回值不完整");
  }

  await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": contentType },
    body: file,
  }).then(async (res) => {
    if (!res.ok) {
      const detail = await res.text().catch(() => "");
      throw new Error(detail || `OSS 上传失败(${res.status})`);
    }
  });

  return fileUrl;
};

const submit = async () => {
  if (submitting.value) return;
  if (!form.mediaType || !form.contentType || !form.fandom || !form.cp) {
    return ElMessage.warning("请先填写必填项");
  }
  if (!selectedFiles.value.length) {
    return ElMessage.warning("请至少选择一个文件");
  }

  submitting.value = true;
  const uploadedStoreUrls = [];
  try {
    for (const file of selectedFiles.value) {
      const storeUrl = await uploadFileToOss(file);
      uploadedStoreUrls.push(storeUrl);
    }

    const payload = {
      media_type: Number(form.mediaType),
      content_type: Number(form.contentType),
      fandom: String(form.fandom).trim(),
      cp: String(form.cp).trim(),
      store_urls: uploadedStoreUrls,
    };

    const createdIds = await createBatchItemApi(payload);
    ElMessage.success(`批量创建成功，共 ${createdIds?.length || 0} 条`);

    await router.push({
      path: "/content",
      query: {
        mode: "content",
        contentType: Number(form.contentType),
        mediaType: Number(form.mediaType),
      },
    });
  } catch (error) {
    await Promise.all(
      uploadedStoreUrls.map((url) =>
        deleteOssObjectApi(url, { suppressError: true }).catch(() => {}),
      ),
    );
    throw error;
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.panel {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 0 10px;
}

.span-2 {
  grid-column: 1 / span 6;
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.compact-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  line-height: 1.2;
}

.upload-tip {
  font-size: 14px;
  font-weight: 600;
}

.upload-sub-tip {
  margin-top: 4px;
  color: var(--grey);
  font-size: 12px;
}

.file-toolbar {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-table {
  margin-top: 8px;
}

@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .span-2 {
    grid-column: 1 / span 4;
  }
}

@media (max-width: 800px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>

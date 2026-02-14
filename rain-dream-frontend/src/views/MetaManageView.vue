<template>
  <section class="meta-manage">
    <article class="card-panel panel">
      <h2>标签管理</h2>
      <div class="row">
        <el-input v-model="tagName" placeholder="输入新标签" />
        <el-button type="primary" :disabled="!normalizeName(tagName)" @click="createTag">新增</el-button>
      </div>
      <div class="chips">
        <div v-for="tag in tags" :key="tag.id" class="chip">
          <el-input v-model="tag.name" size="small" />
          <el-button text :disabled="!hasNameChanged(tag)" @click="updateTag(tag)">更新</el-button>
          <el-button text type="danger" @click="removeTag(tag.id)"
            >删除</el-button
          >
        </div>
      </div>
    </article>

    <article class="card-panel panel">
      <h2>平台管理</h2>
      <div class="row">
        <el-input v-model="pltName" placeholder="输入新平台" />
        <el-button type="primary" :disabled="!normalizeName(pltName)" @click="createPlt">新增</el-button>
      </div>
      <div class="chips">
        <div v-for="plt in plts" :key="plt.id" class="chip">
          <el-input v-model="plt.name" size="small" />
          <el-button text :disabled="!hasNameChanged(plt)" @click="updatePlt(plt)">更新</el-button>
          <el-button text type="danger" @click="removePlt(plt.id)"
            >删除</el-button
          >
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getTagApi,
  createTagApi,
  updateTagApi,
  deleteTagApi,
  getPltApi,
  createPltApi,
  updatePltApi,
  deletePltApi,
} from "../api/meta";

const tags = ref([]);
const plts = ref([]);
const tagName = ref("");
const pltName = ref("");
const getApiErrorMessage = (error) =>
  String(error?.response?.data?.message || error?.message || "");
const normalizeName = (value) => String(value || "").trim();
const hasNameChanged = (item) =>
  normalizeName(item?.name) !== normalizeName(item?.originalName);

const fetchTags = async () => {
  const list = await getTagApi();
  tags.value = (list || []).map((item) => ({
    ...item,
    originalName: item.name,
  }));
};

const fetchPlts = async () => {
  const list = await getPltApi();
  plts.value = (list || []).map((item) => ({
    ...item,
    originalName: item.name,
  }));
};

const createTag = async () => {
  const name = normalizeName(tagName.value);
  if (!name) return;
  await createTagApi({ name });
  ElMessage.success("新增成功");
  tagName.value = "";
  fetchTags();
};

const updateTag = async (tag) => {
  const originalName = normalizeName(tag.originalName || tag.name);
  const name = normalizeName(tag.name);
  if (!name) return;
  if (!hasNameChanged(tag)) return;
  tag.name = name;
  try {
    await updateTagApi(tag.id, { name }, { suppressError: true });
    tag.originalName = name;
    ElMessage.success("更新成功");
  } catch (error) {
    if (!getApiErrorMessage(error).includes("确认后可继续更新")) throw error;
    try {
      await ElMessageBox.confirm(
        `当前标签「${originalName}」有关联条目，确认更新吗？`,
        "二次确认",
        {
          confirmButtonText: "确认更新",
          cancelButtonText: "取消",
          type: "warning",
        },
      );
    } catch {
      tag.name = originalName;
      return;
    }
    await updateTagApi(tag.id, { name }, { force: true });
    tag.originalName = name;
    ElMessage.success("更新成功");
  }
};

const removeTag = async (id) => {
  await deleteTagApi(id);
  ElMessage.success("删除成功");
  fetchTags();
};

const createPlt = async () => {
  const name = normalizeName(pltName.value);
  if (!name) return;
  await createPltApi({ name });
  ElMessage.success("新增成功");
  pltName.value = "";
  fetchPlts();
};

const updatePlt = async (plt) => {
  const originalName = normalizeName(plt.originalName || plt.name);
  const name = normalizeName(plt.name);
  if (!name) return;
  if (!hasNameChanged(plt)) return;
  plt.name = name;
  try {
    await updatePltApi(plt.id, { name }, { suppressError: true });
    plt.originalName = name;
    ElMessage.success("更新成功");
  } catch (error) {
    if (!getApiErrorMessage(error).includes("确认后可继续更新")) throw error;
    try {
      await ElMessageBox.confirm(
        `当前平台「${originalName}」有关联条目，确认更新吗？`,
        "二次确认",
        {
          confirmButtonText: "确认更新",
          cancelButtonText: "取消",
          type: "warning",
        },
      );
    } catch {
      plt.name = originalName;
      return;
    }
    await updatePltApi(plt.id, { name }, { force: true });
    plt.originalName = name;
    ElMessage.success("更新成功");
  }
};

const removePlt = async (id) => {
  await deletePltApi(id);
  ElMessage.success("删除成功");
  fetchPlts();
};

onMounted(() => {
  fetchTags();
  fetchPlts();
});
</script>

<style scoped>
.meta-manage {
  display: grid;
  gap: 16px;
}

.panel {
  padding: 16px;
}

.row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.chips {
  display: grid;
  gap: 10px;
}

.chip {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  align-items: center;
}
</style>

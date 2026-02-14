<template>
  <section class="meta-manage">
    <article class="card-panel panel">
      <div class="panel-head">
        <h2>标签管理</h2>
        <span class="count">{{ tags.length }}</span>
      </div>
      <div class="create-row">
        <el-input
          v-model="tagName"
          class="create-input"
          size="small"
          placeholder="输入新标签"
        />
        <el-button
          type="primary"
          size="small"
          :disabled="!normalizeName(tagName)"
          @click="createTag"
        >
          新增
        </el-button>
      </div>
      <div class="chips">
        <div v-for="tag in tags" :key="tag.id" class="chip">
          <el-input v-model="tag.name" size="small" />
          <div class="chip-actions">
            <el-button
              text
              size="small"
              :disabled="!hasNameChanged(tag)"
              @click="updateTag(tag)"
            >
              更新
            </el-button>
            <el-button text size="small" type="danger" @click="removeTag(tag.id)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </article>

    <article class="card-panel panel">
      <div class="panel-head">
        <h2>平台管理</h2>
        <span class="count">{{ plts.length }}</span>
      </div>
      <div class="create-row">
        <el-input
          v-model="pltName"
          class="create-input"
          size="small"
          placeholder="输入新平台"
        />
        <el-button
          type="primary"
          size="small"
          :disabled="!normalizeName(pltName)"
          @click="createPlt"
        >
          新增
        </el-button>
      </div>
      <div class="chips">
        <div v-for="plt in plts" :key="plt.id" class="chip">
          <el-input v-model="plt.name" size="small" />
          <div class="chip-actions">
            <el-button
              text
              size="small"
              :disabled="!hasNameChanged(plt)"
              @click="updatePlt(plt)"
            >
              更新
            </el-button>
            <el-button text size="small" type="danger" @click="removePlt(plt.id)">
              删除
            </el-button>
          </div>
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
  gap: 14px;
}

.panel {
  padding: 14px 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.panel-head h2 {
  margin: 0;
  font-size: 28px;
}

.count {
  min-width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(235, 122, 83, 0.16);
  color: var(--text-main);
  font-size: 13px;
  font-weight: 700;
}

.create-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.create-input {
  width: min(280px, 100%);
}

.chips {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 8px;
}

.chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.55);
}

.chip :deep(.el-input) {
  min-width: 0;
  flex: 1;
}

.chip-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

@media (max-width: 720px) {
  .chips {
    grid-template-columns: 1fr;
  }

  .create-input {
    width: 100%;
  }
}
</style>

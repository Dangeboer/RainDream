<template>
  <section class="meta-manage">
    <article class="card-panel panel">
      <h2>标签管理</h2>
      <div class="row">
        <el-input v-model="tagName" placeholder="输入新标签" />
        <el-button type="primary" @click="createTag">新增</el-button>
      </div>
      <div class="chips">
        <div v-for="tag in tags" :key="tag.id" class="chip">
          <el-input v-model="tag.name" size="small" />
          <el-button text @click="updateTag(tag)">保存</el-button>
          <el-button text type="danger" @click="removeTag(tag.id)">删除</el-button>
        </div>
      </div>
    </article>

    <article class="card-panel panel">
      <h2>平台管理</h2>
      <div class="row">
        <el-input v-model="pltName" placeholder="输入新平台" />
        <el-button type="primary" @click="createPlt">新增</el-button>
      </div>
      <div class="chips">
        <div v-for="plt in plts" :key="plt.id" class="chip">
          <el-input v-model="plt.name" size="small" />
          <el-button text @click="updatePlt(plt)">保存</el-button>
          <el-button text type="danger" @click="removePlt(plt.id)">删除</el-button>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTagApi,
  createTagApi,
  updateTagApi,
  deleteTagApi,
  getPltApi,
  createPltApi,
  updatePltApi,
  deletePltApi
} from '../api/meta'

const tags = ref([])
const plts = ref([])
const tagName = ref('')
const pltName = ref('')

const fetchTags = async () => {
  tags.value = await getTagApi()
}

const fetchPlts = async () => {
  plts.value = await getPltApi()
}

const createTag = async () => {
  if (!tagName.value) return
  await createTagApi({ name: tagName.value })
  ElMessage.success('新增成功')
  tagName.value = ''
  fetchTags()
}

const updateTag = async (tag) => {
  await updateTagApi(tag.id, { name: tag.name })
  ElMessage.success('更新成功')
}

const removeTag = async (id) => {
  await deleteTagApi(id)
  ElMessage.success('删除成功')
  fetchTags()
}

const createPlt = async () => {
  if (!pltName.value) return
  await createPltApi({ name: pltName.value })
  ElMessage.success('新增成功')
  pltName.value = ''
  fetchPlts()
}

const updatePlt = async (plt) => {
  await updatePltApi(plt.id, { name: plt.name })
  ElMessage.success('更新成功')
}

const removePlt = async (id) => {
  await deletePltApi(id)
  ElMessage.success('删除成功')
  fetchPlts()
}

onMounted(() => {
  fetchTags()
  fetchPlts()
})
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

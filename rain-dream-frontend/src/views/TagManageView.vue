<template>
  <section class="card-panel panel">
    <h2>标签管理</h2>
    <div class="row">
      <el-input v-model="name" placeholder="输入新标签" />
      <el-button type="primary" @click="create">新增</el-button>
    </div>
    <div class="chips">
      <div v-for="tag in list" :key="tag.id" class="chip">
        <el-input v-model="tag.name" size="small" />
        <el-button text @click="update(tag)">保存</el-button>
        <el-button text type="danger" @click="remove(tag.id)">删除</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getTagApi, createTagApi, updateTagApi, deleteTagApi } from '../api/meta'

const list = ref([])
const name = ref('')

const fetchData = async () => {
  list.value = await getTagApi()
}

const create = async () => {
  if (!name.value) return
  await createTagApi({ name: name.value })
  ElMessage.success('新增成功')
  name.value = ''
  fetchData()
}

const update = async (tag) => {
  await updateTagApi(tag.id, { name: tag.name })
  ElMessage.success('更新成功')
}

const remove = async (id) => {
  await deleteTagApi(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.panel { padding: 16px; }
.row { display: flex; gap: 10px; margin-bottom: 14px; }
.chips { display: grid; gap: 10px; }
.chip { display: grid; grid-template-columns: 1fr auto auto; gap: 10px; align-items: center; }
</style>

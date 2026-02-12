<template>
  <section class="card-panel panel">
    <h2>平台管理</h2>
    <div class="row">
      <el-input v-model="name" placeholder="输入新平台" />
      <el-button type="primary" @click="create">新增</el-button>
    </div>
    <div class="chips">
      <div v-for="plt in list" :key="plt.id" class="chip">
        <el-input v-model="plt.name" size="small" />
        <el-button text @click="update(plt)">保存</el-button>
        <el-button text type="danger" @click="remove(plt.id)">删除</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPltApi, createPltApi, updatePltApi, deletePltApi } from '../api/meta'

const list = ref([])
const name = ref('')

const fetchData = async () => {
  list.value = await getPltApi()
}

const create = async () => {
  if (!name.value) return
  await createPltApi({ name: name.value })
  ElMessage.success('新增成功')
  name.value = ''
  fetchData()
}

const update = async (plt) => {
  await updatePltApi(plt.id, { name: plt.name })
  ElMessage.success('更新成功')
}

const remove = async (id) => {
  await deletePltApi(id)
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

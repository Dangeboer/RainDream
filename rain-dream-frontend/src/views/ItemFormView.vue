<template>
  <section class="card-panel panel">
    <h2>{{ isEdit ? '编辑作品' : '新增作品' }}</h2>
    <el-form :model="form" label-position="top" class="form-grid">
      <el-form-item label="媒体类型（必填）">
        <el-input-number v-model="form.mediaType" :min="1" />
      </el-form-item>
      <el-form-item label="内容类型（必填）">
        <el-input-number v-model="form.contentType" :min="1" />
      </el-form-item>
      <el-form-item label="Fandom（必填）">
        <el-input v-model="form.fandom" />
      </el-form-item>
      <el-form-item label="CP（必填）">
        <el-input v-model="form.cp" />
      </el-form-item>

      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="作者">
        <el-input v-model="form.author" />
      </el-form-item>
      <el-form-item label="来源链接" class="span-2">
        <el-input v-model="form.sourceUrl" />
      </el-form-item>
      <el-form-item label="年份">
        <el-input-number v-model="form.releaseYear" :min="1900" :max="3000" />
      </el-form-item>
      <el-form-item label="评分(0-10)">
        <el-input-number v-model="form.rating" :step="0.5" :min="0" :max="10" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tags" multiple filterable allow-create default-first-option>
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="平台">
        <el-select v-model="form.plts" multiple filterable allow-create default-first-option>
          <el-option v-for="plt in plts" :key="plt.id" :label="plt.name" :value="plt.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="简介" class="span-2">
        <el-input v-model="form.summary" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item label="备注" class="span-2">
        <el-input v-model="form.notes" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item class="span-2">
        <el-button type="primary" @click="submit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createItemApi, getItemDetailApi, updateItemApi } from '../api/item'
import { getTagApi, getPltApi } from '../api/meta'

const route = useRoute()
const router = useRouter()
const tags = ref([])
const plts = ref([])

const form = reactive({
  mediaType: 1,
  contentType: 1,
  content: '',
  title: '',
  fandom: '',
  cp: '',
  author: '',
  sourceUrl: '',
  releaseYear: null,
  trackingType: null,
  rating: null,
  notes: '',
  summary: '',
  fanficForm: null,
  tags: [],
  plts: []
})

const isEdit = computed(() => !!route.params.id)

const loadDetail = async () => {
  if (!isEdit.value) return
  const data = await getItemDetailApi(route.params.id)
  Object.assign(form, data)
}

const submit = async () => {
  if (!form.mediaType || !form.contentType || !form.fandom || !form.cp) {
    return ElMessage.warning('请先填写必填项')
  }
  if (isEdit.value) {
    await updateItemApi(route.params.id, form)
    ElMessage.success('更新成功')
  } else {
    await createItemApi(form)
    ElMessage.success('创建成功')
  }
  router.push('/items')
}

onMounted(async () => {
  tags.value = await getTagApi()
  plts.value = await getPltApi()
  loadDetail()
})
</script>

<style scoped>
.panel { padding: 16px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
.span-2 { grid-column: 1 / span 2; }
@media (max-width: 800px) {
  .form-grid { grid-template-columns: 1fr; }
  .span-2 { grid-column: auto; }
}
</style>

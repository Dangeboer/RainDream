<template>
  <section>
    <h1>首页</h1>
    <p class="sub">最近新增 Fanfic + 视觉素材预览</p>

    <div class="card-panel block">
      <h2>最近添加</h2>
      <el-empty v-if="fanfics.length === 0" description="暂无数据" />
      <div v-for="item in fanfics" :key="item.id" class="fanfic-row">
        <div>
          <strong>{{ item.title || '未命名作品' }}</strong>
          <div class="muted">{{ item.author || 'Unknown' }} · {{ item.fandom || '-' }} / {{ item.cp || '-' }}</div>
        </div>
        <el-button text @click="$router.push(`/fanfic/${item.id}`)">查看</el-button>
      </div>
    </div>

    <div class="card-panel block">
      <h2>最近添加</h2>
      <div class="grid">
        <div v-for="(item, idx) in visuals" :key="idx" class="visual">
          <div class="cover">{{ item.title || 'Visual Item' }}</div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getFanficListApi, getItemListApi } from '../api/item'

const fanfics = ref([])
const visuals = ref([])

onMounted(async () => {
  const fanficData = await getFanficListApi({ page: 1, size: 5 })
  fanfics.value = fanficData?.records || []

  const itemData = await getItemListApi({ page: 1, size: 8 })
  visuals.value = (itemData?.records || []).filter((item) => item.mediaType !== 1).slice(0, 6)
})
</script>

<style scoped>
h1 { margin: 0 0 8px; }
.sub { color: var(--text-secondary); margin-bottom: 18px; }
.block { margin-bottom: 18px; padding: 16px; }
.fanfic-row { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,.08); padding: 12px 0; }
.muted { color: var(--text-secondary); margin-top: 4px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.visual { height: 170px; border-radius: 12px; background: linear-gradient(135deg, #2d1f4c, #16213f); display: grid; place-items: center; }
.cover { color: #e9e2fb; font-size: 13px; text-align: center; padding: 8px; }
</style>

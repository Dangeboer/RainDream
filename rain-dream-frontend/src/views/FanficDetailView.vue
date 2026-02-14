<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>{{ detail.title || "Fanfic 详情" }}</h2>
      <el-button text @click="$router.back()">返回</el-button>
    </div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="标题">{{
        show(detail.title)
      }}</el-descriptions-item>
      <el-descriptions-item label="作者">{{
        show(detail.author)
      }}</el-descriptions-item>
      <el-descriptions-item label="原作">{{
        show(detail.fandom)
      }}</el-descriptions-item>
      <el-descriptions-item label="CP">{{
        show(detail.cp)
      }}</el-descriptions-item>
      <el-descriptions-item label="内容类型">{{
        show(detail.content_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="媒介类型">{{
        show(detail.media_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="追踪状态">{{
        show(detail.tracking_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="评分">{{
        show(detail.rating)
      }}</el-descriptions-item>
      <el-descriptions-item label="发布年份">{{
        show(detail.release_year)
      }}</el-descriptions-item>
      <el-descriptions-item label="阅读次数">{{
        show(detail.fanfic_vo?.read_count)
      }}</el-descriptions-item>
      <el-descriptions-item label="年代">{{
        show(detail.fanfic_vo?.era_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="篇幅">{{
        show(detail.fanfic_vo?.length_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="作品状态">{{
        show(detail.fanfic_vo?.work_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="结局">{{
        show(detail.fanfic_vo?.ending_type_label)
      }}</el-descriptions-item>
      <el-descriptions-item label="设定">{{
        show(detail.fanfic_vo?.char_setting)
      }}</el-descriptions-item>
      <el-descriptions-item label="上次更新日期">{{
        show(detail.fanfic_vo?.update_date)
      }}</el-descriptions-item>
      <el-descriptions-item label="访问链接">
        <el-link
          v-if="detail.store_url"
          :href="detail.store_url"
          target="_blank"
          rel="noopener noreferrer"
        >
          点击查看
        </el-link>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="文件大小">{{
        show(detail.size_bytes)
      }}</el-descriptions-item>
      <el-descriptions-item label="平台" :span="2">{{
        platformText
      }}</el-descriptions-item>
      <el-descriptions-item label="标签" :span="2">{{
        tagText
      }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{
        show(detail.notes)
      }}</el-descriptions-item>
      <el-descriptions-item label="总结" :span="2">{{
        show(detail.summary)
      }}</el-descriptions-item>
      <el-descriptions-item label="来源" :span="2">
        <el-link
          v-if="detail.source_url"
          :href="detail.source_url"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ detail.source_url }}
        </el-link>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="内容" :span="2">{{
        show(detail.content)
      }}</el-descriptions-item>
    </el-descriptions>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getFanficDetailApi } from "../api/item";

const route = useRoute();
const detail = ref({});

const show = (value) =>
  value === null || value === undefined || value === "" ? "-" : value;

const tagText = computed(() => {
  const tags = detail.value.tag_vos;
  if (!Array.isArray(tags) || tags.length === 0) return "-";
  return (
    tags
      .map((tag) => tag.tag_name)
      .filter(Boolean)
      .join(" / ") || "-"
  );
});

const platformText = computed(() => {
  const platforms = detail.value.plt_vos;
  if (!Array.isArray(platforms) || platforms.length === 0) return "-";
  return (
    platforms
      .map((platform) => platform.plt_name)
      .filter(Boolean)
      .join(" / ") || "-"
  );
});

onMounted(async () => {
  detail.value = await getFanficDetailApi(route.params.id);
});
</script>

<style scoped>
.panel {
  padding: 16px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

<template>
  <div class="media-grid-wrap">
    <el-empty v-if="rows.length === 0" description="暂无图片" />

    <div v-else class="media-grid image-grid">
      <article
        v-for="row in rows"
        :key="row.id"
        class="image-card"
        @click="openPreview(row)"
      >
        <img
          class="image-main"
          :src="getStoreUrl(row)"
          :alt="row.title || 'image'"
          loading="lazy"
        />

        <div class="image-overlay">
          <div class="overlay-main">
            <h3 class="title">{{ row.title || '未命名图片' }}</h3>
            <p class="sub">
              {{ row.author || 'Unknown' }}
              <span v-if="mediaTypeLabelMap[row.mediaType]">· {{ mediaTypeLabelMap[row.mediaType] }}</span>
            </p>
          </div>

          <div class="overlay-actions">
            <a
              class="action"
              :href="getStoreUrl(row)"
              download
              target="_blank"
              rel="noopener noreferrer"
              @click.stop
            >
              下载
            </a>
            <button class="action" type="button" @click.stop="openPreview(row)">大图看</button>
            <button class="action danger" type="button" @click.stop="$emit('remove', row.id)">
              删除
            </button>
          </div>
        </div>
      </article>
    </div>

    <el-dialog v-model="previewVisible" width="min(900px, 90vw)" destroy-on-close>
      <template #header>
        <div class="dialog-head">
          <span>{{ previewItem?.title || '图片预览' }}</span>
          <span class="dialog-sub">{{ previewItem?.author || 'Unknown' }}</span>
        </div>
      </template>
      <div class="preview-body" v-if="previewItem">
        <img class="preview-image" :src="getStoreUrl(previewItem)" :alt="previewItem.title || 'preview'" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from "vue";

defineProps({
  rows: {
    type: Array,
    default: () => [],
  },
  mediaTypeLabelMap: {
    type: Object,
    default: () => ({}),
  },
});

defineEmits(["detail", "edit", "remove"]);

const previewVisible = ref(false);
const previewItem = ref(null);

const getStoreUrl = (row) => row?.storeUrl || row?.store_url || "";

const openPreview = (row) => {
  if (!getStoreUrl(row)) return;
  previewItem.value = row;
  previewVisible.value = true;
};
</script>

<style scoped>
.media-grid-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.image-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: var(--bg-panel-strong);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  aspect-ratio: 4 / 3;
  cursor: pointer;
}

.image-main {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px;
  color: #fff;
  background: linear-gradient(
    to top,
    rgba(38, 38, 38, 0.72) 0%,
    rgba(38, 38, 38, 0.36) 45%,
    rgba(38, 38, 38, 0.05) 100%
  );
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

@media (hover: none) {
  .image-overlay {
    opacity: 1;
  }
}

.title {
  margin: 0;
  font-size: 20px;
  line-height: 1.25;
  font-weight: 700;
}

.sub {
  margin: 4px 0 0;
  font-size: 14px;
  opacity: 0.92;
}

.overlay-actions {
  display: flex;
  gap: 8px;
}

.action {
  border: 0;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 13px;
  line-height: 1;
  color: #fff;
  background: rgba(38, 38, 38, 0.55);
  text-decoration: none;
  cursor: pointer;
}

.action:hover {
  background: rgba(38, 38, 38, 0.75);
}

.action.danger {
  background: rgba(235, 122, 83, 0.82);
}

.dialog-head {
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.dialog-sub {
  color: var(--text-secondary);
  font-size: 13px;
}

.preview-body {
  display: flex;
  justify-content: center;
  align-items: center;
  max-height: 72vh;
}

.preview-image {
  max-width: 100%;
  max-height: 72vh;
  border-radius: 8px;
  object-fit: contain;
}
</style>

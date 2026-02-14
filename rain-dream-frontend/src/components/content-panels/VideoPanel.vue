<template>
  <div class="media-grid-wrap">
    <el-empty v-if="rows.length === 0" description="暂无视频" />

    <div v-else class="media-grid video-grid">
      <article
        v-for="row in rows"
        :key="row.id"
        class="video-card"
        @click="openPlayer(row)"
      >
        <video
          class="video-bg"
          :src="getStoreUrl(row)"
          preload="metadata"
          muted
          playsinline
        />

        <div class="video-mask"></div>

        <button class="play-btn" type="button" @click.stop="openPlayer(row)">
          <span class="play-triangle"></span>
        </button>

        <div class="video-info">
          <h3 class="title">{{ row.title || '未命名视频' }}</h3>
          <p class="sub">
            {{ row.author || 'Unknown' }}
            <span v-if="getDuration(row)">· {{ getDuration(row) }}</span>
            <span v-else-if="row.rating">· 评分 {{ row.rating }}</span>
          </p>

          <div class="actions" @click.stop>
            <a
              class="action"
              :href="getStoreUrl(row)"
              download
              target="_blank"
              rel="noopener noreferrer"
            >
              下载
            </a>
            <button class="action" type="button" @click="openPlayer(row)">播放</button>
            <button class="action danger" type="button" @click="$emit('remove', row.id)">删除</button>
          </div>
        </div>
      </article>
    </div>

    <el-dialog v-model="playerVisible" width="min(960px, 92vw)" destroy-on-close>
      <template #header>
        <div class="dialog-head">
          <span>{{ playerItem?.title || '视频播放' }}</span>
          <span class="dialog-sub">{{ playerItem?.author || 'Unknown' }}</span>
        </div>
      </template>
      <div class="player-body" v-if="playerItem">
        <video class="video-player" :src="getStoreUrl(playerItem)" controls autoplay playsinline />
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
});

defineEmits(["detail", "edit", "remove"]);

const playerVisible = ref(false);
const playerItem = ref(null);

const getStoreUrl = (row) => row?.storeUrl || row?.store_url || "";

const getDuration = (row) => row?.duration || row?.videoDuration || row?.video_duration || "";

const openPlayer = (row) => {
  if (!getStoreUrl(row)) return;
  playerItem.value = row;
  playerVisible.value = true;
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

.video-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: var(--bg-panel-strong);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  aspect-ratio: 4 / 3;
  cursor: pointer;
}

.video-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(0.92);
}

.video-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(38, 38, 38, 0.78) 0%,
    rgba(38, 38, 38, 0.45) 46%,
    rgba(38, 38, 38, 0.14) 100%
  );
}

.play-btn {
  position: absolute;
  left: 50%;
  top: 42%;
  transform: translate(-50%, -50%);
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 0;
  background: var(--accent);
  display: grid;
  place-items: center;
  box-shadow: 0 8px 24px rgba(38, 38, 38, 0.3);
  cursor: pointer;
}

.play-triangle {
  width: 0;
  height: 0;
  border-top: 11px solid transparent;
  border-bottom: 11px solid transparent;
  border-left: 16px solid var(--dark);
  margin-left: 3px;
}

.video-info {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  color: #fff;
  padding: 12px 14px;
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

.actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
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

.player-body {
  max-height: 72vh;
}

.video-player {
  width: 100%;
  max-height: 72vh;
  border-radius: 8px;
  background: #000;
}
</style>

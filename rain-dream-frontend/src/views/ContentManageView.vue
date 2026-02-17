<template>
  <section ref="panelRef" class="card-panel panel">
    <div class="head">
      <h2>{{ pageTitle }}</h2>
      <el-button @click="goCreate">+ 新增此资源</el-button>
    </div>

    <div v-if="showMediaTabs" class="media-tabs">
      <el-button
        v-for="entry in availableMediaGroups"
        :key="entry.value"
        :class="{
          'is-main-tab-active': isMediaGroupActive(entry.value),
          'is-main-tab-pill': usePillStyleForMediaTabs,
        }"
        plain
        size="small"
        @click="onMediaGroupChange(entry.value)"
      >
        {{ entry.label }}
      </el-button>
    </div>

    <div v-if="showImageTypeTabs" class="media-sub-tabs">
      <el-button
        v-for="entry in availableImageTypeEntries"
        :key="entry.value"
        :class="{ 'is-sub-tab-active': isImageSubActive(entry.value) }"
        plain
        size="small"
        @click="onImageTypeChange(entry.value)"
      >
        {{ entry.label }}
      </el-button>
    </div>

    <template v-if="showGenericTable">
      <AllMixedPanel
        :rows="rows"
        :loading="isLoading"
        @edit="onEdit"
        @remove="remove"
      />
    </template>
    <template v-else>
      <component
        :is="currentPanel"
        :rows="rows"
        :loading="isLoading"
        :media-type-label-map="mediaTypeLabelMap"
        @detail="onDetail"
        @edit="onEdit"
        @remove="remove"
        @updated="onPanelUpdated"
      />
    </template>

    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="onPageChange"
    />
  </section>
</template>

<script setup>
import AllMixedPanel from "../components/AllMixedPanel.vue";
import { useContentManageViewModel } from "../composables/useContentManageViewModel";

const {
  availableImageTypeEntries,
  availableMediaGroups,
  currentPanel,
  goCreate,
  isImageSubActive,
  isMediaGroupActive,
  mediaTypeLabelMap,
  onDetail,
  onEdit,
  onImageTypeChange,
  onMediaGroupChange,
  onPageChange,
  onPanelUpdated,
  pageTitle,
  panelRef,
  query,
  remove,
  rows,
  showGenericTable,
  showImageTypeTabs,
  showMediaTabs,
  total,
  isLoading,
  usePillStyleForMediaTabs,
} = useContentManageViewModel();
</script>

<style scoped>
.panel {
  padding: 16px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.media-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.media-tabs .el-button.is-main-tab-pill {
  font-size: 14px;
}

.media-tabs .el-button.is-main-tab-pill.is-main-tab-active {
  --el-button-bg-color: var(--xhs-yellow);
  --el-button-border-color: var(--xhs-yellow);
  --el-button-text-color: var(--xhs-black);
}

.media-sub-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.media-sub-tabs .el-button {
  font-size: 14px;
}

.media-sub-tabs .el-button.is-sub-tab-active {
  --el-button-bg-color: var(--xhs-yellow);
  --el-button-border-color: var(--xhs-yellow);
  --el-button-text-color: var(--xhs-black);
}

</style>

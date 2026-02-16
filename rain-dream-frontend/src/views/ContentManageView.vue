<template>
  <section ref="panelRef" class="card-panel panel">
    <div class="head">
      <h2>{{ pageTitle }}</h2>
      <el-button @click="goCreate">+ 新增资源</el-button>
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
      <div class="table-wrap">
        <el-table :data="rows" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="author" label="作者" width="140" />
          <el-table-column prop="mediaType" label="格式" width="120">
            <template #default="{ row }">
              {{ mediaTypeLabelMap[row.mediaType] || `类型 ${row.mediaType}` }}
            </template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="100" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button text @click="onDetail(row.id)">详情</el-button>
              <el-button text @click="onEdit(row.id)">编辑</el-button>
              <el-button text type="danger" @click="remove(row.id)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
    <template v-else>
      <component
        :is="currentPanel"
        :rows="rows"
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

.table-wrap {
  min-width: 0;
  margin-bottom: 12px;
}
</style>

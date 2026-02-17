<template>
  <div class="table-wrap" @wheel.capture="onTableWheel">
    <el-table ref="tableRef" :data="rows" stripe>
      <el-table-column
        prop="title"
        label="标题"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column prop="author" label="作者" width="140" />
      <!-- <el-table-column prop="summary" label="总结" width="140">
        <template #default="{ row }" show-overflow-tooltip>
          <span>{{ row.summary ?? "-" }}</span>
        </template>
      </el-table-column> -->
      <el-table-column label="来源" width="140">
        <template #default="{ row }">
          <el-link
            v-if="row.sourceUrl"
            :href="row.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
            >点击跳转</el-link
          >
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <div class="action-cell">
            <el-link @click="$emit('detail', row.id)">详情</el-link>
            <el-link @click="$emit('edit', row.id)">编辑</el-link>
            <el-link type="danger" @click="$emit('remove', row.id)"
              >删除</el-link
            >
          </div>
        </template>
      </el-table-column>
    </el-table>
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

const tableRef = ref(null);

const onTableWheel = (event) => {
  if (!event.shiftKey) return;
  const tableEl = tableRef.value?.$el;
  if (!tableEl) return;
  const bodyWrap =
    tableEl.querySelector(".el-table__body-wrapper .el-scrollbar__wrap") ||
    tableEl.querySelector(".el-table__body-wrapper");
  if (!bodyWrap || bodyWrap.scrollWidth <= bodyWrap.clientWidth) return;
  const delta = Math.abs(event.deltaX) > 0 ? event.deltaX : event.deltaY;
  bodyWrap.scrollLeft += delta;
  event.preventDefault();
};
</script>

<style scoped>
.table-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.action-cell {
  display: flex;
  justify-content: left;
  gap: 18px;
}
</style>

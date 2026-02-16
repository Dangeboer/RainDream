<template>
  <div class="table-wrap" @wheel.capture="onTableWheel">
    <el-table ref="tableRef" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="author" label="作者" width="140" />
      <el-table-column prop="sourceUrl" label="链接地址" min-width="260" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button text @click="$emit('detail', row.id)">详情</el-button>
          <el-button text @click="$emit('edit', row.id)">编辑</el-button>
          <el-button text type="danger" @click="$emit('remove', row.id)">删除</el-button>
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
</style>

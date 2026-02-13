<template>
  <section class="card-panel panel">
    <h2>{{ isEdit ? "编辑作品" : "新增作品" }}</h2>
    <el-form
      :model="form"
      label-position="top"
      size="small"
      class="form-grid compact-form"
    >
      <ItemFormFields
        :form="form"
        :tags="tags"
        :plts="plts"
        :is-fanfic-type="isFanficType"
        :content-input-mode="contentInputMode"
        :content-file-name="contentFileName"
        :content-type-options="contentTypeOptions"
        :media-type-options="mediaTypeOptions"
        :tracking-type-options="trackingTypeOptions"
        :ending-type-options="endingTypeOptions"
        :era-options="eraOptions"
        :length-type-options="lengthTypeOptions"
        :work-type-options="workTypeOptions"
        :on-content-file-change="onContentFileChange"
        :clear-content-file="clearContentFile"
        :on-wheel-field="onWheelField"
        :on-wheel-fanfic-field="onWheelFanficField"
        @update:content-input-mode="contentInputMode = $event"
      />
      <el-form-item class="span-2">
        <el-button type="primary" @click="submit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import ItemFormFields from "../components/item/ItemFormFields.vue";
import { useItemForm } from "../composables/useItemForm";

const route = useRoute();
const router = useRouter();

const {
  form,
  isEdit,
  isFanficType,
  tags,
  plts,
  contentInputMode,
  contentFileName,
  contentTypeOptions,
  mediaTypeOptions,
  trackingTypeOptions,
  endingTypeOptions,
  eraOptions,
  lengthTypeOptions,
  workTypeOptions,
  onContentFileChange,
  clearContentFile,
  onWheelField,
  onWheelFanficField,
  submit,
  init,
} = useItemForm({ route, router });

onMounted(init);
</script>

<style scoped>
.panel {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 0 10px;
}

.span-2 {
  grid-column: 1 / span 6;
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.compact-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  line-height: 1.2;
}

.compact-form :deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .span-2 {
    grid-column: 1 / span 4;
  }
}

@media (max-width: 800px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>

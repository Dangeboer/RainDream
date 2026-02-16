<template>
  <el-form-item label="媒体类型*">
    <el-select v-model="form.mediaType" placeholder="请选择媒体类型" clearable>
      <el-option
        v-for="option in mediaTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item label="内容类型*">
    <el-select
      v-model="form.contentType"
      placeholder="请选择内容类型"
      clearable
    >
      <el-option
        v-for="option in contentTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item label="原作*">
    <el-input v-model="form.fandom" />
  </el-form-item>
  <el-form-item label="CP*">
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
  <el-form-item label="内容" class="span-2">
    <div class="content-input">
      <el-radio-group v-model="contentInputModeProxy" size="small">
        <el-radio-button label="text">直接输入</el-radio-button>
        <el-radio-button label="file">上传文件</el-radio-button>
      </el-radio-group>

      <el-input
        v-if="contentInputModeProxy === 'text'"
        v-model="form.content"
        type="textarea"
        rows="5"
        placeholder="可直接粘贴/输入内容"
      />

      <div v-else class="content-upload">
        <el-upload
          :auto-upload="false"
          :show-file-list="false"
          :on-change="onContentFileChange"
          :limit="1"
          accept=".txt,.md,.json,.csv,.html,.htm,.xml,.yml,.yaml,image/*,video/*"
        >
          <el-button>选择本地文件</el-button>
        </el-upload>
        <span class="file-name">{{ contentFileName || "未选择文件" }}</span>
        <el-button text @click="clearContentFile">清空</el-button>
      </div>
    </div>
  </el-form-item>
  <el-form-item label="年份">
    <el-input-number
      v-model="form.releaseYear"
      placeholder="可滑动选取"
      :min="2019"
      :max="2030"
      @wheel.prevent="onWheelField('releaseYear', $event, 1, 2019, 2030)"
    />
  </el-form-item>
  <el-form-item label="追踪状态">
    <el-select
      v-model="form.trackingType"
      placeholder="请选择追踪状态"
      clearable
    >
      <el-option
        v-for="option in trackingTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item label="评分(0-10)">
    <el-input-number
      v-model="form.rating"
      placeholder="可滑动选取"
      :step="0.5"
      :min="0"
      :max="10"
      @wheel.prevent="onWheelField('rating', $event, 0.5, 0, 10)"
    />
  </el-form-item>
  <el-form-item label="标签">
    <div class="meta-picker">
      <el-select
        v-model="form.tags"
        placeholder="请选择标签"
        multiple
        filterable
        allow-create
        default-first-option
        class="meta-picker-select"
        @change="onTagsChange"
      >
        <el-option
          v-for="tag in tags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.name"
        />
      </el-select>
      <el-button class="meta-add-btn" @click="onCreateTagQuick"
        >+ 新增</el-button
      >
    </div>
  </el-form-item>
  <el-form-item label="平台">
    <div class="meta-picker">
      <el-select
        v-model="form.plts"
        placeholder="请选择平台"
        multiple
        filterable
        allow-create
        default-first-option
        class="meta-picker-select"
        @change="onPltsChange"
      >
        <el-option
          v-for="plt in plts"
          :key="plt.id"
          :label="plt.name"
          :value="plt.name"
        />
      </el-select>
      <el-button class="meta-add-btn" @click="onCreatePltQuick"
        >+ 新增</el-button
      >
    </div>
  </el-form-item>
  <el-form-item label="备注" class="span-2">
    <el-input v-model="form.notes" type="textarea" rows="3" />
  </el-form-item>
  <el-form-item label="总结" class="span-2">
    <el-input v-model="form.summary" type="textarea" rows="3" />
  </el-form-item>
  <el-form-item v-if="isFanficType" label="年代">
    <el-select v-model="form.fanficForm.era" placeholder="请选择年代" clearable>
      <el-option
        v-for="option in eraOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item v-if="isFanficType" label="篇幅">
    <el-select
      v-model="form.fanficForm.lengthType"
      placeholder="请选择篇幅"
      clearable
    >
      <el-option
        v-for="option in lengthTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item v-if="isFanficType" label="作品状态">
    <el-select
      v-model="form.fanficForm.workType"
      placeholder="请选择作品状态"
      clearable
    >
      <el-option
        v-for="option in workTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item v-if="isFanficType" label="结局">
    <el-select
      v-model="form.fanficForm.endingType"
      placeholder="请选择结局"
      clearable
    >
      <el-option
        v-for="option in endingTypeOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item v-if="isFanficType" label="设定" class="fanfic-setting">
    <el-input v-model="form.fanficForm.charSetting" />
  </el-form-item>
  <el-form-item v-if="isFanficType" label="上次更新日期">
    <el-date-picker
      v-model="form.fanficForm.updateDate"
      type="date"
      value-format="YYYY-MM-DD"
      placeholder="请选择日期"
      clearable
    />
  </el-form-item>
  <el-form-item v-if="isFanficType" label="阅读次数">
    <el-input-number
      v-model="form.fanficForm.readCount"
      placeholder="可滑动选取"
      :min="0"
      @wheel.prevent="onWheelFanficField('readCount', $event, 1, 0)"
    />
  </el-form-item>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  form: { type: Object, required: true },
  tags: { type: Array, default: () => [] },
  plts: { type: Array, default: () => [] },
  isFanficType: { type: Boolean, default: false },
  contentInputMode: { type: String, default: "text" },
  contentFileName: { type: String, default: "" },
  contentTypeOptions: { type: Array, default: () => [] },
  mediaTypeOptions: { type: Array, default: () => [] },
  trackingTypeOptions: { type: Array, default: () => [] },
  endingTypeOptions: { type: Array, default: () => [] },
  eraOptions: { type: Array, default: () => [] },
  lengthTypeOptions: { type: Array, default: () => [] },
  workTypeOptions: { type: Array, default: () => [] },
  onContentFileChange: { type: Function, required: true },
  clearContentFile: { type: Function, required: true },
  onWheelField: { type: Function, required: true },
  onWheelFanficField: { type: Function, required: true },
  onTagsChange: { type: Function, required: true },
  onPltsChange: { type: Function, required: true },
  onCreateTagQuick: { type: Function, required: true },
  onCreatePltQuick: { type: Function, required: true },
});

const emit = defineEmits(["update:contentInputMode"]);

const contentInputModeProxy = computed({
  get: () => props.contentInputMode,
  set: (value) => emit("update:contentInputMode", value),
});
</script>

<style scoped>
.span-2 {
  grid-column: 1 / span 6;
}

.fanfic-setting {
  grid-column: 5 / span 2;
}

.content-input {
  display: grid;
  gap: 8px;
  width: 100%;
}

.content-input :deep(.el-textarea) {
  width: 100%;
}

.content-input :deep(.el-textarea__inner) {
  width: 100%;
}

.content-upload {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-picker {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.meta-picker-select {
  min-width: 0;
  flex: 1;
}

.meta-add-btn {
  flex: 0 0 auto;
}

.file-name {
  color: var(--text-secondary);
  font-size: 12px;
}

@media (max-width: 1200px) {
  .span-2 {
    grid-column: 1 / span 4;
  }

  .fanfic-setting {
    grid-column: 1 / span 4;
  }
}

@media (max-width: 800px) {
  .span-2 {
    grid-column: auto;
  }

  .fanfic-setting {
    grid-column: auto;
  }
}
</style>

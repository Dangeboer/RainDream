<template>
  <section class="card-panel panel">
    <h2>{{ isEdit ? "编辑作品" : "新增作品" }}</h2>
    <el-form
      :model="form"
      label-position="top"
      size="small"
      class="form-grid compact-form"
    >
      <el-form-item label="媒体类型*">
        <el-select
          v-model="form.mediaType"
          placeholder="请选择媒体类型"
          clearable
        >
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
          <el-radio-group v-model="contentInputMode" size="small">
            <el-radio-button label="text">直接输入</el-radio-button>
            <el-radio-button label="file">上传本地文件</el-radio-button>
          </el-radio-group>

          <el-input
            v-if="contentInputMode === 'text'"
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
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.name"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="平台">
        <el-select
          v-model="form.plts"
          multiple
          filterable
          allow-create
          default-first-option
        >
          <el-option
            v-for="plt in plts"
            :key="plt.id"
            :label="plt.name"
            :value="plt.name"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" class="span-2">
        <el-input v-model="form.notes" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item label="总结" class="span-2">
        <el-input v-model="form.summary" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item v-if="isFanficType" label="年代">
        <el-select
          v-model="form.fanficForm.era"
          placeholder="请选择年代"
          clearable
        >
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
      <el-form-item class="span-2">
        <el-button type="primary" @click="submit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { createItemApi, getItemDetailApi, updateItemApi } from "../api/item";
import { createPltApi, createTagApi, getPltApi, getTagApi } from "../api/meta";

const route = useRoute();
const router = useRouter();
const tags = ref([]);
const plts = ref([]);
const contentInputMode = ref("text");
const contentFileName = ref("");

const contentTypeOptions = [
  { value: 1, label: "文章" },
  { value: 2, label: "图绘" },
  { value: 3, label: "精修" },
  { value: 4, label: "混剪" },
  { value: 5, label: "解析" },
  { value: 6, label: "吐槽" },
  { value: 7, label: "主创说" },
  { value: 8, label: "RPS" },
  { value: 9, label: "其他" },
];

const mediaTypeOptions = [
  { value: 1, label: "文本" },
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 4, label: "实况照片" },
  { value: 5, label: "视频" },
  { value: 6, label: "链接" },
];

const trackingTypeOptions = [
  { value: 1, label: "未看" },
  { value: 2, label: "在看" },
  { value: 3, label: "追平" },
  { value: 4, label: "看过" },
  { value: 5, label: "归档" },
];

const endingTypeOptions = [
  { value: 1, label: "HE" },
  { value: 2, label: "BE" },
  { value: 3, label: "OE" },
  { value: 4, label: "ME" },
];

const eraOptions = [
  { value: 1, label: "古代" },
  { value: 2, label: "民国" },
  { value: 3, label: "现代" },
];

const lengthTypeOptions = [
  { value: 1, label: "短篇" },
  { value: 2, label: "中篇" },
  { value: 3, label: "长篇" },
];

const workTypeOptions = [
  { value: 1, label: "已完结" },
  { value: 2, label: "更新中" },
  { value: 3, label: "断更" },
  { value: 4, label: "已弃" },
];

const emptyFanficForm = () => ({
  era: null,
  charSetting: "",
  lengthType: null,
  workType: null,
  updateDate: null,
  endingType: null,
  readCount: 1,
});

const form = reactive({
  mediaType: null,
  contentType: null,
  content: null,
  title: "",
  fandom: "风声",
  cp: "玉梦",
  author: "",
  sourceUrl: "",
  releaseYear: null,
  trackingType: null,
  rating: null,
  notes: "",
  summary: "",
  fanficForm: emptyFanficForm(),
  tags: [],
  plts: [],
});

const isEdit = computed(() => !!route.params.id);
const isFanficType = computed(() => Number(form.contentType) === 1);

const readFileAsTextOrDataUrl = (file, asText) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result || "");
    reader.onerror = reject;
    if (asText) {
      reader.readAsText(file);
    } else {
      reader.readAsDataURL(file);
    }
  });

const looksLikeTextFile = (file) => {
  const textExtensions =
    /\.(txt|md|json|csv|html?|xml|ya?ml|js|ts|vue|java|kt|py|go|rs|sql|sh)$/i;
  return file.type?.startsWith("text/") || textExtensions.test(file.name || "");
};

const onContentFileChange = async (uploadFile) => {
  const file = uploadFile?.raw;
  if (!file) return;
  const asText = looksLikeTextFile(file);
  form.content = await readFileAsTextOrDataUrl(file, asText);
  contentFileName.value = file.name || "";
  if (!asText) {
    ElMessage.info("已读取为 Base64 Data URL");
  }
};

const clearContentFile = () => {
  contentFileName.value = "";
  form.content = null;
};

const loadDetail = async () => {
  if (!isEdit.value) return;
  const data = await getItemDetailApi(route.params.id);
  const fanfic = data?.fanficForm || data?.fanfic_form || {};
  form.mediaType = data?.mediaType ?? data?.media_type ?? null;
  form.contentType = data?.contentType ?? data?.content_type ?? null;
  form.content = data?.content ?? null;
  form.title = data?.title ?? "";
  form.fandom = data?.fandom ?? "风声";
  form.cp = data?.cp ?? "玉梦";
  form.author = data?.author ?? "";
  form.sourceUrl = data?.sourceUrl ?? data?.source_url ?? "";
  form.releaseYear = data?.releaseYear ?? data?.release_year ?? null;
  form.trackingType = data?.trackingType ?? data?.tracking_type ?? null;
  form.rating = data?.rating ?? null;
  form.notes = data?.notes ?? "";
  form.summary = data?.summary ?? "";
  form.fanficForm = {
    era: fanfic?.era ?? null,
    charSetting: fanfic?.charSetting ?? fanfic?.char_setting ?? "",
    lengthType: fanfic?.lengthType ?? fanfic?.length_type ?? null,
    workType: fanfic?.workType ?? fanfic?.work_type ?? null,
    updateDate: fanfic?.updateDate ?? fanfic?.update_date ?? null,
    endingType: fanfic?.endingType ?? fanfic?.ending_type ?? null,
    readCount: fanfic?.readCount ?? fanfic?.read_count ?? 1,
  };
  form.tags = (data?.tags || data?.tag_vos || [])
    .map((item) => item?.name || item?.tag_name || item)
    .filter(Boolean);
  form.plts = (data?.plts || data?.plt_vos || [])
    .map((item) => item?.name || item?.plt_name || item)
    .filter(Boolean);
  contentInputMode.value =
    typeof form.content === "string" && form.content.startsWith("data:")
      ? "file"
      : "text";
  contentFileName.value = "";
};

const loadMeta = async () => {
  const [tagList, pltList] = await Promise.all([getTagApi(), getPltApi()]);
  tags.value = Array.isArray(tagList) ? tagList : [];
  plts.value = Array.isArray(pltList) ? pltList : [];
};

const ensureNamesExist = async (values, listRef, createApi) => {
  const uniqueNames = [
    ...new Set(
      (values || []).map((item) => String(item || "").trim()).filter(Boolean),
    ),
  ];
  const existing = new Set(
    (listRef.value || []).map((item) => item?.name).filter(Boolean),
  );
  const missing = uniqueNames.filter((name) => !existing.has(name));
  if (missing.length === 0) return;
  await Promise.all(missing.map((name) => createApi({ name })));
};

const clampNumber = (value, min, max) => {
  let next = value;
  if (typeof min === "number") next = Math.max(min, next);
  if (typeof max === "number") next = Math.min(max, next);
  return next;
};

const WHEEL_DELTA_THRESHOLD = 80;
const WHEEL_IDLE_RESET_MS = 180;
const wheelState = new Map();

const consumeWheelSteps = (key, event) => {
  const now = Date.now();
  const state = wheelState.get(key) || { acc: 0, lastAt: 0 };
  if (now - state.lastAt > WHEEL_IDLE_RESET_MS) state.acc = 0;
  state.lastAt = now;
  state.acc += -event.deltaY;

  let steps = 0;
  if (state.acc >= WHEEL_DELTA_THRESHOLD) {
    steps = Math.floor(state.acc / WHEEL_DELTA_THRESHOLD);
  } else if (state.acc <= -WHEEL_DELTA_THRESHOLD) {
    steps = Math.ceil(state.acc / WHEEL_DELTA_THRESHOLD);
  }

  if (steps !== 0) {
    state.acc -= steps * WHEEL_DELTA_THRESHOLD;
  }
  wheelState.set(key, state);
  return steps;
};

const nextByWheel = (current, steps, step, min, max) => {
  if (steps === 0) return current;
  const base =
    typeof current === "number" ? current : typeof min === "number" ? min : 0;
  const delta = steps * step;
  const raw = Number((base + delta).toFixed(6));
  return clampNumber(raw, min, max);
};

const onWheelField = (field, event, step = 1, min, max) => {
  const steps = consumeWheelSteps(`form:${field}`, event);
  form[field] = nextByWheel(form[field], steps, step, min, max);
};

const onWheelFanficField = (field, event, step = 1, min, max) => {
  const steps = consumeWheelSteps(`fanfic:${field}`, event);
  form.fanficForm[field] = nextByWheel(
    form.fanficForm[field],
    steps,
    step,
    min,
    max,
  );
};

const toNullableString = (value) => {
  if (value === null || value === undefined) return null;
  const text = String(value).trim();
  return text === "" ? null : text;
};

const buildPayload = () => ({
  media_type: form.mediaType,
  content_type: form.contentType,
  content: toNullableString(form.content),
  title: toNullableString(form.title),
  fandom: toNullableString(form.fandom) ?? "风声",
  cp: toNullableString(form.cp) ?? "玉梦",
  author: toNullableString(form.author),
  source_url: toNullableString(form.sourceUrl),
  release_year: form.releaseYear,
  tracking_type: form.trackingType,
  rating: form.rating,
  notes: toNullableString(form.notes),
  summary: toNullableString(form.summary),
  fanfic_form: isFanficType.value
    ? {
        era: form.fanficForm.era,
        char_setting: toNullableString(form.fanficForm.charSetting),
        length_type: form.fanficForm.lengthType,
        work_type: form.fanficForm.workType,
        update_date: form.fanficForm.updateDate,
        ending_type: form.fanficForm.endingType,
        read_count: form.fanficForm.readCount ?? 1,
      }
    : null,
  tags: [
    ...new Set(
      (form.tags || [])
        .map((item) => String(item || "").trim())
        .filter(Boolean),
    ),
  ],
  plts: [
    ...new Set(
      (form.plts || [])
        .map((item) => String(item || "").trim())
        .filter(Boolean),
    ),
  ],
});

const getSuccessRoute = () => {
  const contentType = Number(form.contentType);
  if (contentType === 1) return { path: "/fanfic" };
  if (contentType > 1) {
    return { path: "/items", query: { contentType } };
  }
  return { path: "/items" };
};

const submit = async () => {
  if (!form.mediaType || !form.contentType || !form.fandom || !form.cp) {
    return ElMessage.warning("请先填写必填项");
  }
  await ensureNamesExist(form.tags, tags, createTagApi);
  await ensureNamesExist(form.plts, plts, createPltApi);
  await loadMeta();
  const payload = buildPayload();
  if (isEdit.value) {
    await updateItemApi(route.params.id, payload);
    ElMessage.success("更新成功");
  } else {
    await createItemApi(payload);
    ElMessage.success("创建成功");
  }
  router.push(getSuccessRoute());
};

onMounted(async () => {
  await loadMeta();
  await loadDetail();
});
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
.fanfic-setting {
  grid-column: 5 / span 2;
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
.file-name {
  color: var(--text-secondary);
  font-size: 12px;
}
@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .span-2 {
    grid-column: 1 / span 4;
  }
  .fanfic-setting {
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
  .fanfic-setting {
    grid-column: auto;
  }
}
</style>

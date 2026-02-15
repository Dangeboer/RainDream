import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { createItemApi, getItemDetailApi, updateItemApi } from "../api/item";
import { createPltApi, createTagApi, getPltApi, getTagApi } from "../api/meta";
import { deleteOssObjectApi, presignOssUploadApi } from "../api/oss";

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

const clampNumber = (value, min, max) => {
  let next = value;
  if (typeof min === "number") next = Math.max(min, next);
  if (typeof max === "number") next = Math.min(max, next);
  return next;
};

const WHEEL_DELTA_THRESHOLD = 80;
const WHEEL_IDLE_RESET_MS = 180;

const consumeWheelSteps = (wheelState, key, event) => {
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

const toNullableString = (value) => {
  if (value === null || value === undefined) return null;
  const text = String(value).trim();
  return text === "" ? null : text;
};

const isMediaUploadType = (mediaType) =>
  [1, 2, 3, 4, 5].includes(Number(mediaType));

const uploadFileToOss = async (file, mediaType) => {
  const contentType = file?.type || "application/octet-stream";
  const presign = await presignOssUploadApi({
    file_name: file?.name || "file.bin",
    content_type: contentType,
    size: file?.size || 0,
    media_type: mediaType,
  });
  const uploadUrl = presign?.uploadUrl ?? presign?.upload_url;
  const fileUrl = presign?.fileUrl ?? presign?.file_url;
  if (!uploadUrl || !fileUrl) {
    throw new Error("签名返回值不完整");
  }
  await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": contentType },
    body: file,
  }).then(async (res) => {
    if (!res.ok) {
      const detail = await res.text().catch(() => "");
      throw new Error(detail || `OSS 上传失败(${res.status})`);
    }
  });
  return fileUrl;
};

export const useItemForm = ({ route, router }) => {
  const tags = ref([]);
  const plts = ref([]);
  const pendingUploadFile = ref(null);
  const contentInputMode = ref("text");
  const contentFileName = ref("");
  const wheelState = new Map();

  const form = reactive({
    mediaType: null,
    contentType: null,
    storeUrl: "",
    sizeBytes: null,
    content: null,
    title: "",
    fandom: "风声",
    cp: "玉梦",
    author: "",
    sourceUrl: "",
    releaseYear: null,
    trackingType: 5,
    rating: null,
    notes: "",
    summary: "",
    fanficForm: emptyFanficForm(),
    tags: [],
    plts: [],
  });

  const isEdit = computed(() => !!route.params.id);
  const isFanficType = computed(() => Number(form.contentType) === 1);

  const applyQueryDefaults = () => {
    if (isEdit.value) return;
    const contentType = Number.parseInt(route.query.contentType, 10);
    const mediaType = Number.parseInt(route.query.mediaType, 10);
    if (!Number.isNaN(contentType) && contentType > 0) {
      form.contentType = contentType;
    }
    if (!Number.isNaN(mediaType) && mediaType > 0) {
      form.mediaType = mediaType;
    }
  };

  const onContentFileChange = async (uploadFile) => {
    const file = uploadFile?.raw;
    if (!file) return;
    try {
      if (isMediaUploadType(form.mediaType)) {
        pendingUploadFile.value = file;
        form.content = null;
        form.sizeBytes = file.size || null;
        contentFileName.value = file.name || "";
        ElMessage.success("文件已选择，将在保存时上传");
        return;
      }
      const asText = looksLikeTextFile(file);
      form.content = await readFileAsTextOrDataUrl(file, asText);
      form.sizeBytes = file.size || null;
      contentFileName.value = file.name || "";
    } catch (error) {
      ElMessage.error(error?.message || "上传失败，请稍后重试");
    }
  };

  const clearContentFile = () => {
    pendingUploadFile.value = null;
    contentFileName.value = "";
    form.content = null;
    form.storeUrl = null;
    form.sizeBytes = null;
  };

  const loadDetail = async () => {
    if (!isEdit.value) return;
    const data = await getItemDetailApi(route.params.id);
    const fanfic = data?.fanficForm || data?.fanfic_form || {};
    form.mediaType = data?.mediaType ?? data?.media_type ?? null;
    form.contentType = data?.contentType ?? data?.content_type ?? null;
    form.storeUrl = data?.storeUrl ?? data?.store_url ?? null;
    form.sizeBytes = data?.sizeBytes ?? data?.size_bytes ?? null;
    form.content = data?.content ?? null;
    form.title = data?.title ?? null;
    form.fandom = data?.fandom ?? "风声";
    form.cp = data?.cp ?? "玉梦";
    form.author = data?.author ?? null;
    form.sourceUrl = data?.sourceUrl ?? data?.source_url ?? null;
    form.releaseYear = data?.releaseYear ?? data?.release_year ?? null;
    form.trackingType = data?.trackingType ?? data?.tracking_type ?? 5;
    form.rating = data?.rating ?? null;
    form.notes = data?.notes ?? null;
    form.summary = data?.summary ?? null;
    form.fanficForm = {
      era: fanfic?.era ?? null,
      charSetting: fanfic?.charSetting ?? fanfic?.char_setting ?? null,
      lengthType: fanfic?.lengthType ?? fanfic?.length_type ?? null,
      workType: fanfic?.workType ?? fanfic?.work_type ?? null,
      updateDate: fanfic?.updateDate ?? fanfic?.update_date ?? null,
      endingType: fanfic?.endingType ?? fanfic?.ending_type ?? null,
      readCount: fanfic?.readCount ?? fanfic?.read_count ?? null,
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
    pendingUploadFile.value = null;
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

  const sanitizeNameList = (values) => [
    ...new Set(
      (values || []).map((item) => String(item || "").trim()).filter(Boolean),
    ),
  ];

  const onTagsChange = (values) => {
    form.tags = sanitizeNameList(values);
  };

  const onPltsChange = (values) => {
    form.plts = sanitizeNameList(values);
  };

  const promptAndSelectMeta = async ({ title, message, field }) => {
    let inputValue = "";
    try {
      const result = await ElMessageBox.prompt(message, title, {
        confirmButtonText: "新增",
        cancelButtonText: "取消",
        inputPlaceholder: "请输入名称",
      });
      inputValue = result?.value ?? "";
    } catch (error) {
      if (error === "cancel" || error === "close") return;
      throw error;
    }
    const name = String(inputValue || "").trim();
    if (!name) return;
    form[field] = sanitizeNameList([...(form[field] || []), name]);
  };

  const onCreateTagQuick = async () => {
    await promptAndSelectMeta({
      title: "新增标签",
      message: "输入新标签名称",
      field: "tags",
    });
  };

  const onCreatePltQuick = async () => {
    await promptAndSelectMeta({
      title: "新增平台",
      message: "输入新平台名称",
      field: "plts",
    });
  };

  const uploadPendingMediaIfNeeded = async () => {
    if (!isMediaUploadType(form.mediaType) || !pendingUploadFile.value) {
      return null;
    }
    const uploadedUrl = await uploadFileToOss(
      pendingUploadFile.value,
      form.mediaType,
    );
    form.storeUrl = uploadedUrl;
    form.sizeBytes = pendingUploadFile.value.size || form.sizeBytes;
    pendingUploadFile.value = null;
    return uploadedUrl;
  };

  const onWheelField = (field, event, step = 1, min, max) => {
    const steps = consumeWheelSteps(wheelState, `form:${field}`, event);
    form[field] = nextByWheel(form[field], steps, step, min, max);
  };

  const onWheelFanficField = (field, event, step = 1, min, max) => {
    const steps = consumeWheelSteps(wheelState, `fanfic:${field}`, event);
    form.fanficForm[field] = nextByWheel(
      form.fanficForm[field],
      steps,
      step,
      min,
      max,
    );
  };

  const buildPayload = () => ({
    media_type: form.mediaType,
    content_type: form.contentType,
    store_url: isMediaUploadType(form.mediaType)
      ? toNullableString(form.storeUrl)
      : null,
    content:
      Number(form.mediaType) === 1 && contentInputMode.value === "text"
        ? toNullableString(form.content)
        : null,
    title: toNullableString(form.title),
    fandom: toNullableString(form.fandom) ?? "风声",
    cp: toNullableString(form.cp) ?? "玉梦",
    author: toNullableString(form.author),
    source_url: toNullableString(form.sourceUrl),
    release_year: form.releaseYear,
    size_bytes: form.sizeBytes,
    tracking_type: form.trackingType ?? 5,
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
    media_form:
      Number(form.mediaType) === 4
        ? {
            live_url: null,
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
    const mediaType = Number(form.mediaType);
    if (contentType === 1) return { path: "/fanfic" };
    if (contentType > 1) {
      return {
        path: "/content",
        query: {
          contentType,
          mediaType: Number.isNaN(mediaType) ? undefined : mediaType,
        },
      };
    }
    return { path: "/items" };
  };

  const submit = async () => {
    if (!form.mediaType || !form.contentType || !form.fandom || !form.cp) {
      return ElMessage.warning("请先填写必填项");
    }

    const previousStoreUrl = form.storeUrl;
    let uploadedStoreUrl = null;
    try {
      uploadedStoreUrl = await uploadPendingMediaIfNeeded();
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
    } catch (error) {
      if (uploadedStoreUrl) {
        await deleteOssObjectApi(uploadedStoreUrl, {
          suppressError: true,
        }).catch(() => {});
      }
      if (uploadedStoreUrl) {
        form.storeUrl = previousStoreUrl;
      }
      throw error;
    }

    if (
      isEdit.value &&
      uploadedStoreUrl &&
      previousStoreUrl &&
      previousStoreUrl !== uploadedStoreUrl
    ) {
      await deleteOssObjectApi(previousStoreUrl, { suppressError: true }).catch(
        () => {},
      );
    }
    router.push(getSuccessRoute());
  };

  const init = async () => {
    applyQueryDefaults();
    await loadMeta();
    await loadDetail();
  };

  return {
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
    onTagsChange,
    onPltsChange,
    onCreateTagQuick,
    onCreatePltQuick,
    submit,
    init,
  };
};

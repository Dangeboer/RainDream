export const DEFAULT_PAGE_SIZE = 8;
export const MAX_PAGE_SIZE = 100;
export const IMAGE_ALL_FETCH_BATCH_SIZE = 24;
export const IMAGE_DUAL_FETCH_BATCH_SIZE = 12;
export const VIDEO_FETCH_BATCH_SIZE = 12;
export const VIDEO_AUTO_PAGE_SIZE_CAP = 12;
export const GRID_CARD_MIN_WIDTH = 260;
export const GRID_CARD_GAP = 14;

export const contentTypeLabelMap = {
  2: "图绘",
  3: "精修",
  4: "混剪",
  5: "解析",
  6: "吐槽",
  7: "主创说",
  8: "RPS",
  9: "其他",
};

export const mediaTypeOptions = [
  { value: 1, label: "文本" },
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 5, label: "视频" },
  { value: 6, label: "链接" },
];

export const contentMediaGroupOptions = [
  { value: "all", label: "全部" },
  { value: "text", label: "文本" },
  { value: "image", label: "图片" },
  { value: "video", label: "视频" },
  { value: "link", label: "链接" },
];

export const globalMediaGroupOptions = [
  { value: "text", label: "文本" },
  { value: "image", label: "图片" },
  { value: "video", label: "视频" },
  { value: "link", label: "链接" },
];

export const mediaTypeLabelMap = Object.fromEntries(
  mediaTypeOptions.map((item) => [item.value, item.label]),
);
export const contentMediaGroupLabelMap = Object.fromEntries(
  contentMediaGroupOptions.map((item) => [item.value, item.label]),
);
export const globalMediaGroupLabelMap = Object.fromEntries(
  globalMediaGroupOptions.map((item) => [item.value, item.label]),
);

export const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  4: "image",
  5: "video",
  6: "link",
};

export const mediaTypesByGroup = {
  text: [1],
  image: [2, 3],
  video: [5],
  link: [6],
};

export const contentTypeFixedMediaGroupMap = {
  2: "image",
  3: "image",
  4: "video",
};

export const IMAGE_MEDIA_TYPES = [2, 3];
export const IMAGE_SUBTYPE_ENABLED_CONTENT_TYPES = [2, 3];
export const PILL_STYLE_CONTENT_TYPES = [5, 6, 7, 8, 9];

export const getAllowedContentGroups = (contentType) => {
  const fixed = contentTypeFixedMediaGroupMap[contentType];
  if (fixed) return [fixed];
  return contentMediaGroupOptions.map((item) => item.value);
};

export const parsePositiveInt = (value) => {
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) || parsed <= 0 ? undefined : parsed;
};

export const clampPageSize = (value) =>
  Math.min(MAX_PAGE_SIZE, Math.max(1, Number(value) || DEFAULT_PAGE_SIZE));

export const parsePageSize = (value) => {
  const parsed = parsePositiveInt(value);
  return parsed ? clampPageSize(parsed) : undefined;
};

export const normalizeItem = (item = {}) => ({
  ...item,
  id: item.id,
  contentType: item.contentType,
  mediaType: item.mediaType,
  storeUrl: item.storeUrl ?? "",
  sourceUrl: item.sourceUrl ?? "",
  trackingType: item.trackingType,
  trackingTypeLabel: item.trackingTypeLabel,
});

export const extractListPayload = (payload) => {
  if (Array.isArray(payload?.records)) return payload.records;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload)) return payload;
  return [];
};

export const inferMediaTypeFromStoreUrl = (storeUrl = "") => {
  const clean = String(storeUrl).split("?")[0].toLowerCase();
  if (!clean) return undefined;
  if (clean.endsWith(".gif") || clean.endsWith(".webp")) return 3;
  if (
    clean.endsWith(".mp4") ||
    clean.endsWith(".mov") ||
    clean.endsWith(".m4v") ||
    clean.endsWith(".webm")
  ) {
    return 5;
  }
  return 2;
};

export const resolveItemMediaType = (item) => {
  const fromPayload = Number(item?.mediaType);
  if (Number.isFinite(fromPayload) && fromPayload > 0) {
    return fromPayload;
  }
  return inferMediaTypeFromStoreUrl(item?.storeUrl || "");
};

export const filterByImageSubType = (list, imageSubType) => {
  const target = imageSubType === "all" ? "all" : Number(imageSubType);
  return list.filter((item) => {
    const resolvedType = resolveItemMediaType(item);
    if (target === "all") return IMAGE_MEDIA_TYPES.includes(resolvedType);
    return resolvedType === target;
  });
};

export const filterByContentType = (list, contentType) => {
  if (!contentType) return list;
  return list.filter((item) => {
    if (
      item?.contentType === undefined ||
      item?.contentType === null ||
      item?.contentType === ""
    ) {
      return true;
    }
    return Number(item.contentType) === Number(contentType);
  });
};

export const canUseImageSubType = ({ mode, contentType, mediaGroup }) => {
  if (mediaGroup !== "image") return false;
  if (mode !== "content") return true;
  return IMAGE_SUBTYPE_ENABLED_CONTENT_TYPES.includes(Number(contentType));
};

export const shouldUseDualImageRequests = ({ mediaGroup }) => {
  if (mediaGroup !== "image") return false;
  return true;
};

export const resolveImageSubType = (
  rawValue,
  currentType,
  group,
  allowImageSubType = true,
) => {
  if (group !== "image") return "all";
  if (!allowImageSubType) return "all";
  if (String(rawValue) === "all") return "all";
  const parsed = parsePositiveInt(rawValue);
  if (IMAGE_MEDIA_TYPES.includes(parsed)) return parsed;
  if (IMAGE_MEDIA_TYPES.includes(currentType)) return currentType;
  return "all";
};

export const resolveMediaTypeForGroup = (
  group,
  currentType,
  imageSubType = "all",
  allowImageSubType = true,
) => {
  if (group === "all") return undefined;
  if (group === "image" && (!allowImageSubType || imageSubType === "all")) {
    return undefined;
  }
  const candidates = mediaTypesByGroup[group] || [];
  if (candidates.length === 0) return currentType;
  if (candidates.includes(currentType)) return currentType;
  return candidates[0];
};

<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>{{ pageTitle }}</h2>
      <el-button type="primary" @click="goCreate">新增资源</el-button>
    </div>

    <div v-if="query.mode === 'content'" class="media-tabs">
      <el-button
        v-for="entry in availableMediaGroups"
        :key="entry.value"
        :type="currentMediaGroup === entry.value ? 'primary' : 'default'"
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
        :type="isImageSubActive(entry.value) ? 'primary' : 'default'"
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
              <el-button text type="danger" @click="remove(row.id)">删除</el-button>
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
import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { deleteItemApi, getItemListApi } from "../api/item";
import TextPanel from "../components/content-panels/TextPanel.vue";
import ImagePanel from "../components/content-panels/ImagePanel.vue";
import VideoPanel from "../components/content-panels/VideoPanel.vue";
import LinkPanel from "../components/content-panels/LinkPanel.vue";

const route = useRoute();
const router = useRouter();
const rows = ref([]);
const total = ref(0);

const contentTypeLabelMap = {
  2: "图绘",
  3: "精修",
  4: "混剪",
  5: "解析",
  6: "吐槽",
  7: "主创说",
  8: "RPS",
  9: "其他",
};

const mediaTypeOptions = [
  { value: 1, label: "文本" },
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 4, label: "实况照片" },
  { value: 5, label: "视频" },
  { value: 6, label: "链接" },
];

const contentMediaGroupOptions = [
  { value: "all", label: "全部" },
  { value: "text", label: "文本" },
  { value: "image", label: "图片" },
  { value: "video", label: "视频" },
  { value: "link", label: "链接" },
];

const globalMediaGroupOptions = [
  { value: "text", label: "文本" },
  { value: "image", label: "图片" },
  { value: "video", label: "视频" },
  { value: "link", label: "链接" },
];

const mediaTypeLabelMap = Object.fromEntries(
  mediaTypeOptions.map((item) => [item.value, item.label]),
);
const contentMediaGroupLabelMap = Object.fromEntries(
  contentMediaGroupOptions.map((item) => [item.value, item.label]),
);
const globalMediaGroupLabelMap = Object.fromEntries(
  globalMediaGroupOptions.map((item) => [item.value, item.label]),
);

const mediaPanelMap = {
  text: TextPanel,
  image: ImagePanel,
  video: VideoPanel,
  link: LinkPanel,
};

const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  4: "image",
  5: "video",
  6: "link",
};

const mediaTypesByGroup = {
  text: [1],
  image: [2, 3, 4],
  video: [5],
  link: [6],
};

const contentTypeFixedMediaGroupMap = {
  2: "image",
  3: "image",
  4: "video",
};

const getAllowedContentGroups = (contentType) => {
  const fixed = contentTypeFixedMediaGroupMap[contentType];
  if (fixed) return [fixed];
  return contentMediaGroupOptions.map((item) => item.value);
};

const IMAGE_MEDIA_TYPES = [2, 3, 4];
const IMAGE_ALL_FETCH_SIZE = 1000;

const query = reactive({
  mode: "content",
  page: 1,
  size: 10,
  contentType: 2,
  mediaGroup: "all",
  imageSubType: "all",
  mediaType: undefined,
});

const parsePositiveInt = (value) => {
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) || parsed <= 0 ? undefined : parsed;
};

const currentMediaGroup = computed(() => {
  if (query.mode === "content") {
    const fixed = contentTypeFixedMediaGroupMap[query.contentType];
    if (fixed) return fixed;
    if (query.mediaGroup === "all") return "all";
    if (query.mediaGroup) return query.mediaGroup;
    return mediaGroupByType[query.mediaType] || "all";
  }
  return query.mediaGroup || mediaGroupByType[query.mediaType] || "text";
});

const availableMediaGroups = computed(() => {
  if (query.mode !== "content") return globalMediaGroupOptions;
  const allowed = getAllowedContentGroups(query.contentType);
  return contentMediaGroupOptions.filter((item) => allowed.includes(item.value));
});

const availableImageTypeEntries = computed(() => {
  const imageOptions = mediaTypeOptions.filter((item) =>
    IMAGE_MEDIA_TYPES.includes(item.value),
  );
  return [{ value: "all", label: "全部图片" }, ...imageOptions];
});

const showImageTypeTabs = computed(() => currentMediaGroup.value === "image");

const showGenericTable = computed(
  () => query.mode === "content" && currentMediaGroup.value === "all",
);

const currentPanel = computed(() => {
  if (showGenericTable.value) return null;
  return mediaPanelMap[currentMediaGroup.value] || TextPanel;
});

const pageTitle = computed(() => {
  if (query.mode === "content") {
    const content = contentTypeLabelMap[query.contentType] || "内容";
    const mediaGroupLabel =
      contentMediaGroupLabelMap[currentMediaGroup.value] || "全部";
    if (currentMediaGroup.value === "image") {
      if (query.imageSubType === "all") {
        return `${content} / 图片 / 全部图片`;
      }
      return `${content} / 图片 / ${mediaTypeLabelMap[query.mediaType] || "图片"}`;
    }
    return `${content} / ${mediaGroupLabel}`;
  }

  const mediaGroupLabel =
    globalMediaGroupLabelMap[currentMediaGroup.value] || "格式";
  if (currentMediaGroup.value === "image") {
    if (query.imageSubType === "all") return `全部内容 / 图片 / 全部图片`;
    return `全部内容 / 图片 / ${mediaTypeLabelMap[query.mediaType] || "图片"}`;
  }
  return `全部内容 / ${mediaGroupLabel}`;
});

const isImageSubActive = (value) => {
  if (value === "all") return query.imageSubType === "all";
  return Number(query.mediaType) === Number(value) && query.imageSubType !== "all";
};

const resolveImageSubType = (rawValue, currentType, group) => {
  if (group !== "image") return "all";
  const parsed = parsePositiveInt(rawValue);
  if (IMAGE_MEDIA_TYPES.includes(parsed)) return parsed;
  if (IMAGE_MEDIA_TYPES.includes(currentType)) return currentType;
  return "all";
};

const resolveMediaTypeForGroup = (group, currentType, imageSubType = "all") => {
  if (group === "all") return undefined;
  if (group === "image" && imageSubType === "all") return undefined;
  const candidates = mediaTypesByGroup[group] || [];
  if (candidates.length === 0) return currentType;
  if (candidates.includes(currentType)) return currentType;
  return candidates[0];
};

const syncQueryFromRoute = async () => {
  const mode = route.query.mode === "media" ? "media" : "content";
  const page = parsePositiveInt(route.query.page) || 1;

  const contentType =
    mode === "content"
      ? parsePositiveInt(route.query.contentType) || 2
      : undefined;

  const routeMediaGroup = String(route.query.mediaGroup || "");
  const routeMediaType = parsePositiveInt(route.query.mediaType);

  let mediaGroup;
  if (mode === "content") {
    const allowedGroups = getAllowedContentGroups(contentType);
    mediaGroup = routeMediaGroup || allowedGroups[0];
    if (!allowedGroups.includes(mediaGroup)) {
      mediaGroup = allowedGroups[0];
    }
  } else {
    mediaGroup = routeMediaGroup || mediaGroupByType[routeMediaType] || "text";
    if (!["text", "image", "video", "link"].includes(mediaGroup)) {
      mediaGroup = "text";
    }
  }

  const imageSubType = resolveImageSubType(
    route.query.imageSubType,
    routeMediaType,
    mediaGroup,
  );
  const mediaType = resolveMediaTypeForGroup(
    mediaGroup,
    routeMediaType,
    imageSubType,
  );

  const hasMismatch =
    String(mode) !== String(route.query.mode || "") ||
    String((mode === "content" ? contentType : undefined) || "") !==
      String(route.query.contentType || "") ||
    String((mode === "content" ? mediaGroup : mediaGroup) || "") !==
      String(route.query.mediaGroup || "") ||
    String((mediaGroup === "image" ? imageSubType : undefined) || "") !==
      String(route.query.imageSubType || "") ||
    String(mediaType || "") !== String(route.query.mediaType || "") ||
    String(page) !== String(route.query.page || "");

  query.mode = mode;
  query.page = page;
  query.contentType = contentType;
  query.mediaGroup = mediaGroup;
  query.imageSubType = imageSubType;
  query.mediaType = mediaType;

  if (hasMismatch) {
    await router.replace({
      path: "/content",
      query: {
        mode,
        contentType: mode === "content" ? contentType : undefined,
        mediaGroup,
        imageSubType: mediaGroup === "image" ? imageSubType : undefined,
        mediaType: mediaType || undefined,
        page,
      },
    });
  }
};

const fetchImageUnionAndPaginate = async ({ contentType }) => {
  const params = { page: 1, size: IMAGE_ALL_FETCH_SIZE };
  if (contentType) params.contentType = contentType;
  const raw = await getItemListApi(params);
  const imageRecords = (raw?.records || []).filter((item) =>
    IMAGE_MEDIA_TYPES.includes(Number(item?.mediaType)),
  );
  total.value = imageRecords.length;
  const start = (query.page - 1) * query.size;
  rows.value = imageRecords.slice(start, start + query.size);
};

const fetchData = async () => {
  if (currentMediaGroup.value === "image" && query.imageSubType === "all") {
    await fetchImageUnionAndPaginate({
      contentType: query.mode === "content" ? query.contentType : undefined,
    });
    return;
  }

  const params = {
    page: query.page,
    size: query.size,
  };

  if (query.mode === "content" && query.contentType) {
    params.contentType = query.contentType;
  }
  if (query.mediaType) {
    params.mediaType = query.mediaType;
  }

  const data = await getItemListApi(params);
  rows.value = data?.records || [];
  total.value = Number(data?.total || 0);
};

const onPageChange = async (page) => {
  await router.push({
    path: "/content",
    query: {
      mode: query.mode,
      contentType: query.mode === "content" ? query.contentType : undefined,
      mediaGroup: query.mediaGroup,
      imageSubType: query.mediaGroup === "image" ? query.imageSubType : undefined,
      mediaType: query.mediaType || undefined,
      page,
    },
  });
};

const onMediaGroupChange = async (group) => {
  const imageSubType = group === "image" ? "all" : "all";
  const mediaType = resolveMediaTypeForGroup(group, query.mediaType, imageSubType);
  await router.push({
    path: "/content",
    query: {
      mode: query.mode,
      contentType: query.mode === "content" ? query.contentType : undefined,
      mediaGroup: group,
      imageSubType: group === "image" ? imageSubType : undefined,
      mediaType: mediaType || undefined,
      page: 1,
    },
  });
};

const onImageTypeChange = async (imageType) => {
  const mediaType = imageType === "all" ? undefined : Number(imageType);
  await router.push({
    path: "/content",
    query: {
      mode: query.mode,
      contentType: query.mode === "content" ? query.contentType : undefined,
      mediaGroup: "image",
      imageSubType: imageType,
      mediaType: mediaType || undefined,
      page: 1,
    },
  });
};

const goCreate = () => {
  router.push({
    path: "/items/new",
    query: {
      contentType: query.mode === "content" ? query.contentType : undefined,
      mediaType: query.mediaType,
    },
  });
};

const onDetail = (id) => {
  router.push(`/items/${id}`);
};

const onEdit = (id) => {
  router.push(`/items/edit/${id}`);
};

const remove = async (id) => {
  await ElMessageBox.confirm("确认删除该资源吗？", "提示", { type: "warning" });
  await deleteItemApi(id);
  ElMessage.success("删除成功");
  fetchData();
};

watch(
  () => route.query,
  async () => {
    await syncQueryFromRoute();
    await fetchData();
  },
  { immediate: true },
);
</script>

<style scoped>
.panel {
  padding: 16px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.media-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.media-sub-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.table-wrap {
  min-width: 0;
  margin-bottom: 12px;
}
</style>

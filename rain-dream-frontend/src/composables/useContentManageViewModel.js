import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { deleteItemApi } from "../api/item";
import TextPanel from "../components/TextPanel.vue";
import ImagePanel from "../components/ImagePanel.vue";
import VideoPanel from "../components/VideoPanel.vue";
import LinkPanel from "../components/LinkPanel.vue";
import {
  DEFAULT_PAGE_SIZE,
  GRID_CARD_GAP,
  GRID_CARD_MIN_WIDTH,
  PILL_STYLE_CONTENT_TYPES,
  VIDEO_AUTO_PAGE_SIZE_CAP,
  canUseImageSubType,
  clampPageSize,
  contentMediaGroupLabelMap,
  contentMediaGroupOptions,
  contentTypeLabelMap,
  getAllowedContentGroups,
  globalMediaGroupLabelMap,
  globalMediaGroupOptions,
  mediaGroupByType,
  mediaTypeLabelMap,
  mediaTypeOptions,
  mediaTypesByGroup,
  parsePageSize,
  parsePositiveInt,
  resolveImageSubType,
  resolveMediaTypeForGroup,
} from "./contentManageConfig";
import { useContentManageData } from "./useContentManageData";

const mediaPanelMap = {
  text: TextPanel,
  image: ImagePanel,
  video: VideoPanel,
  link: LinkPanel,
};

export const useContentManageViewModel = () => {
  const route = useRoute();
  const router = useRouter();
  const rows = ref([]);
  const total = ref(0);
  const isLoading = ref(false);
  const panelRef = ref(null);
  let panelResizeObserver = null;
  let resizeTimer = null;

  const query = reactive({
    mode: "content",
    page: 1,
    size: DEFAULT_PAGE_SIZE,
    contentType: 2,
    mediaGroup: "all",
    imageSubType: "all",
    mediaType: undefined,
  });

  const currentMediaGroup = computed(() => {
    if (query.mode === "content") {
      const allowedGroups = getAllowedContentGroups(query.contentType);
      if (allowedGroups.length === 1) return allowedGroups[0];
      if (query.mediaGroup === "all") return "all";
      if (query.mediaGroup) return query.mediaGroup;
      return mediaGroupByType[query.mediaType] || "all";
    }
    return query.mediaGroup || mediaGroupByType[query.mediaType] || "text";
  });

  const availableMediaGroups = computed(() => {
    if (query.mode !== "content") return globalMediaGroupOptions;
    const allowed = getAllowedContentGroups(query.contentType);
    return contentMediaGroupOptions.filter((item) =>
      allowed.includes(item.value),
    );
  });

  const availableImageTypeEntries = computed(() => {
    const imageOptions = mediaTypeOptions.filter((item) =>
      [2, 3].includes(item.value),
    );
    return [{ value: "all", label: "全部" }, ...imageOptions];
  });

  const showMediaTabs = computed(
    () => query.mode === "content" && availableMediaGroups.value.length > 1,
  );

  const showImageTypeTabs = computed(() =>
    canUseImageSubType({
      mode: query.mode,
      contentType: query.contentType,
      mediaGroup: currentMediaGroup.value,
    }),
  );

  const usePillStyleForMediaTabs = computed(() => {
    if (query.mode !== "content") return false;
    return PILL_STYLE_CONTENT_TYPES.includes(Number(query.contentType));
  });

  const showGenericTable = computed(
    () => query.mode === "content" && currentMediaGroup.value === "all",
  );

  const isAdaptiveSizeView = computed(() => {
    if (currentMediaGroup.value === "video") return true;
    if (currentMediaGroup.value === "image") return true;
    return false;
  });

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
        if (
          !canUseImageSubType({
            mode: query.mode,
            contentType: query.contentType,
            mediaGroup: currentMediaGroup.value,
          }) ||
          query.imageSubType === "all"
        ) {
          return `${content} / 全部`;
        }
        return `${content} / ${mediaTypeLabelMap[query.mediaType] || "图片"}`;
      }
      return `${content} / ${mediaGroupLabel}`;
    }

    const mediaGroupLabel =
      globalMediaGroupLabelMap[currentMediaGroup.value] || "格式";
    if (currentMediaGroup.value === "image") {
      if (query.imageSubType === "all") return "全部内容 / 图片 / 全部";
      return `全部内容 / 图片 / ${mediaTypeLabelMap[query.mediaType] || "图片"}`;
    }
    return `全部内容 / ${mediaGroupLabel}`;
  });

  const { fetchData, resetCaches } = useContentManageData({
    query,
    rows,
    total,
    currentMediaGroup,
  });

  const isImageSubActive = (value) => {
    if (value === "all") return query.imageSubType === "all";
    return (
      Number(query.mediaType) === Number(value) && query.imageSubType !== "all"
    );
  };

  const buildRouteQuery = (overrides = {}) => {
    const pick = (key, fallback) =>
      Object.prototype.hasOwnProperty.call(overrides, key)
        ? overrides[key]
        : fallback;

    const mode = pick("mode", query.mode);
    const mediaGroup = pick("mediaGroup", query.mediaGroup);
    const contentType = pick(
      "contentType",
      mode === "content" ? query.contentType : undefined,
    );
    const imageSubTypeEnabled = canUseImageSubType({
      mode,
      contentType,
      mediaGroup,
    });
    const imageSubType = pick(
      "imageSubType",
      mediaGroup === "image" && imageSubTypeEnabled
        ? query.imageSubType
        : undefined,
    );
    const mediaType = pick("mediaType", query.mediaType || undefined);
    const page = pick("page", query.page);
    const size = pick("size", query.size);

    return {
      mode,
      contentType: mode === "content" ? contentType : undefined,
      mediaGroup,
      imageSubType:
        mediaGroup === "image" && imageSubTypeEnabled
          ? imageSubType
          : undefined,
      mediaType,
      page,
      size,
    };
  };

  const calculateAutoPageSize = () => {
    if (!isAdaptiveSizeView.value) return DEFAULT_PAGE_SIZE;

    const panelEl = panelRef.value;
    if (!panelEl) return query.size || DEFAULT_PAGE_SIZE;

    const gridWrap = panelEl.querySelector(".media-grid-wrap");
    const paginationEl = panelEl.querySelector(".el-pagination");
    if (!gridWrap || !paginationEl) return query.size || DEFAULT_PAGE_SIZE;

    const availableWidth = Math.max(
      panelEl.clientWidth - 32,
      GRID_CARD_MIN_WIDTH,
    );
    const columns = Math.max(
      1,
      Math.floor(
        (availableWidth + GRID_CARD_GAP) /
          (GRID_CARD_MIN_WIDTH + GRID_CARD_GAP),
      ),
    );
    const cardWidth =
      (availableWidth - (columns - 1) * GRID_CARD_GAP) / columns;
    const cardHeight = cardWidth * 0.75;

    const gridTop = gridWrap.getBoundingClientRect().top;
    const paginationHeight = paginationEl.getBoundingClientRect().height || 32;
    const availableHeight = Math.max(
      120,
      window.innerHeight - gridTop - paginationHeight - 24,
    );
    const rowsCount = Math.max(
      1,
      Math.floor(
        (availableHeight + GRID_CARD_GAP) / (cardHeight + GRID_CARD_GAP),
      ),
    );

    const calculated = clampPageSize(columns * rowsCount);
    if (currentMediaGroup.value === "video") {
      return clampPageSize(Math.min(calculated, VIDEO_AUTO_PAGE_SIZE_CAP));
    }
    return calculated;
  };

  const syncAdaptivePageSize = async () => {
    await nextTick();
    const targetSize = isAdaptiveSizeView.value
      ? calculateAutoPageSize()
      : DEFAULT_PAGE_SIZE;
    if (targetSize === query.size) return false;
    await router.replace({
      path: "/content",
      query: buildRouteQuery({ size: targetSize }),
    });
    return true;
  };

  const scheduleAdaptivePageSizeSync = () => {
    if (resizeTimer) {
      clearTimeout(resizeTimer);
    }
    resizeTimer = setTimeout(() => {
      syncAdaptivePageSize();
    }, 120);
  };

  const syncQueryFromRoute = async () => {
    const mode = route.query.mode === "media" ? "media" : "content";
    const page = parsePositiveInt(route.query.page) || 1;
    const size = parsePageSize(route.query.size) || DEFAULT_PAGE_SIZE;

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
      mediaGroup =
        routeMediaGroup || mediaGroupByType[routeMediaType] || "text";
      if (!["text", "image", "video", "link"].includes(mediaGroup)) {
        mediaGroup = "text";
      }
    }

    const imageSubTypeEnabled = canUseImageSubType({
      mode,
      contentType,
      mediaGroup,
    });

    const imageSubType = resolveImageSubType(
      route.query.imageSubType,
      routeMediaType,
      mediaGroup,
      imageSubTypeEnabled,
    );
    const mediaType = resolveMediaTypeForGroup(
      mediaGroup,
      routeMediaType,
      imageSubType,
      imageSubTypeEnabled,
    );

    const hasMismatch =
      String(mode) !== String(route.query.mode || "") ||
      String((mode === "content" ? contentType : undefined) || "") !==
        String(route.query.contentType || "") ||
      String((mode === "content" ? mediaGroup : mediaGroup) || "") !==
        String(route.query.mediaGroup || "") ||
      String(
        (mediaGroup === "image" && imageSubTypeEnabled
          ? imageSubType
          : undefined) || "",
      ) !== String(route.query.imageSubType || "") ||
      String(mediaType || "") !== String(route.query.mediaType || "") ||
      String(page) !== String(route.query.page || "") ||
      String(size) !== String(route.query.size || "");

    query.mode = mode;
    query.page = page;
    query.size = size;
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
          imageSubType:
            mediaGroup === "image" && imageSubTypeEnabled
              ? imageSubType
              : undefined,
          mediaType: mediaType || undefined,
          page,
          size,
        },
      });
      return true;
    }

    return false;
  };

  const onPanelUpdated = async () => {
    resetCaches();
    await fetchData({ loadingRef: isLoading });
  };

  const onPageChange = async (page) => {
    await router.push({
      path: "/content",
      query: buildRouteQuery({ page }),
    });
  };

  const onMediaGroupChange = async (group) => {
    const imageSubType = group === "image" ? "all" : "all";
    const imageSubTypeEnabled = canUseImageSubType({
      mode: query.mode,
      contentType: query.contentType,
      mediaGroup: group,
    });
    const mediaType = resolveMediaTypeForGroup(
      group,
      query.mediaType,
      imageSubType,
      imageSubTypeEnabled,
    );
    await router.push({
      path: "/content",
      query: buildRouteQuery({
        mediaGroup: group,
        imageSubType:
          group === "image" && imageSubTypeEnabled ? imageSubType : undefined,
        mediaType: mediaType || undefined,
        page: 1,
      }),
    });
  };

  const onImageTypeChange = async (imageType) => {
    const mediaType = imageType === "all" ? undefined : Number(imageType);
    await router.push({
      path: "/content",
      query: buildRouteQuery({
        mediaGroup: "image",
        imageSubType: imageType,
        mediaType: mediaType || undefined,
        page: 1,
      }),
    });
  };

  const isMediaGroupActive = (value) => currentMediaGroup.value === value;

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
    try {
      await ElMessageBox.confirm("确认删除该资源吗？", "提示", {
        type: "warning",
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        beforeClose: async (action, instance, done) => {
          if (action !== "confirm") {
            done();
            return;
          }
          instance.confirmButtonLoading = true;
          instance.confirmButtonText = "确认...";
          try {
            await deleteItemApi(id);
            ElMessage.success("删除成功");
            resetCaches();
            await fetchData({ loadingRef: isLoading });
            done();
          } finally {
            instance.confirmButtonLoading = false;
            instance.confirmButtonText = "确认";
          }
        },
      });
    } catch (error) {
      if (error !== "cancel" && error !== "close") {
        throw error;
      }
    }
  };

  watch(
    () => route.query,
    async () => {
      const replaced = await syncQueryFromRoute();
      if (replaced) return;
      const sizeReplaced = await syncAdaptivePageSize();
      if (sizeReplaced) return;
      await fetchData({ loadingRef: isLoading });
    },
    { immediate: true },
  );

  watch(
    () => currentMediaGroup.value,
    () => {
      scheduleAdaptivePageSizeSync();
    },
  );

  onMounted(() => {
    panelResizeObserver = new ResizeObserver(() => {
      scheduleAdaptivePageSizeSync();
    });
    if (panelRef.value) {
      panelResizeObserver.observe(panelRef.value);
    }
    window.addEventListener("resize", scheduleAdaptivePageSizeSync);
  });

  onBeforeUnmount(() => {
    if (panelResizeObserver) {
      panelResizeObserver.disconnect();
      panelResizeObserver = null;
    }
    window.removeEventListener("resize", scheduleAdaptivePageSizeSync);
    if (resizeTimer) {
      clearTimeout(resizeTimer);
      resizeTimer = null;
    }
  });

  return {
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
  };
};

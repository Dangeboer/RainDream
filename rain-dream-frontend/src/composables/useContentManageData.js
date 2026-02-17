import { reactive } from "vue";
import { getItemListApi } from "../api/item";
import {
  IMAGE_ALL_FETCH_BATCH_SIZE,
  IMAGE_DUAL_FETCH_BATCH_SIZE,
  IMAGE_MEDIA_TYPES,
  MIXED_CONTENT_FETCH_BATCH_SIZE,
  PILL_STYLE_CONTENT_TYPES,
  VIDEO_FETCH_BATCH_SIZE,
  canUseImageSubType,
  extractListPayload,
  filterByContentType,
  filterByImageSubType,
  normalizeItem,
  resolveItemMediaType,
  shouldUseDualImageRequests,
} from "./contentManageConfig";

export const useContentManageData = ({ query, rows, total, currentMediaGroup }) => {
  let latestRequestId = 0;

  const imageAllCache = reactive({
    key: "",
    nextRawPage: 1,
    rawTotal: 0,
    rawLoadedCount: 0,
    rawExhausted: false,
    records: [],
  });
  const imageDualCache = reactive({
    key: "",
    records: [],
  });
  const imageDualTypeState = reactive({
    2: {
      nextRawPage: 1,
      rawTotal: 0,
      rawLoadedCount: 0,
      rawExhausted: false,
    },
    3: {
      nextRawPage: 1,
      rawTotal: 0,
      rawLoadedCount: 0,
      rawExhausted: false,
    },
  });
  const videoCache = reactive({
    key: "",
    nextRawPage: 1,
    rawTotal: 0,
    rawLoadedCount: 0,
    rawExhausted: false,
    records: [],
  });
  const mixedContentCache = reactive({
    key: "",
    nextRawPage: 1,
    rawTotal: 0,
    rawLoadedCount: 0,
    rawExhausted: false,
    records: [],
  });

  let imageAllCacheLoadingPromise = null;
  let imageDualCacheLoadingPromise = null;
  let videoCacheLoadingPromise = null;
  let mixedContentCacheLoadingPromise = null;

  const resetImageAllCache = () => {
    imageAllCache.key = "";
    imageAllCache.nextRawPage = 1;
    imageAllCache.rawTotal = 0;
    imageAllCache.rawLoadedCount = 0;
    imageAllCache.rawExhausted = false;
    imageAllCache.records = [];
    imageAllCacheLoadingPromise = null;
  };

  const resetImageDualCache = () => {
    imageDualCache.key = "";
    imageDualCache.records = [];
    for (const mediaType of IMAGE_MEDIA_TYPES) {
      imageDualTypeState[mediaType].nextRawPage = 1;
      imageDualTypeState[mediaType].rawTotal = 0;
      imageDualTypeState[mediaType].rawLoadedCount = 0;
      imageDualTypeState[mediaType].rawExhausted = false;
    }
    imageDualCacheLoadingPromise = null;
  };

  const resetVideoCache = () => {
    videoCache.key = "";
    videoCache.nextRawPage = 1;
    videoCache.rawTotal = 0;
    videoCache.rawLoadedCount = 0;
    videoCache.rawExhausted = false;
    videoCache.records = [];
    videoCacheLoadingPromise = null;
  };
  const resetMixedContentCache = () => {
    mixedContentCache.key = "";
    mixedContentCache.nextRawPage = 1;
    mixedContentCache.rawTotal = 0;
    mixedContentCache.rawLoadedCount = 0;
    mixedContentCache.rawExhausted = false;
    mixedContentCache.records = [];
    mixedContentCacheLoadingPromise = null;
  };

  const resetCaches = () => {
    resetImageAllCache();
    resetImageDualCache();
    resetVideoCache();
    resetMixedContentCache();
  };

  const buildImageAllCacheKey = (contentType) => {
    const contentSegment = contentType ? String(contentType) : "all";
    return `${query.mode}|${contentSegment}`;
  };

  const buildImageDualCacheKey = (contentType) => {
    const contentSegment = contentType ? String(contentType) : "all";
    return `${query.mode}|${contentSegment}|2+3`;
  };

  const buildVideoCacheKey = (contentType, mediaType) => {
    const contentSegment = contentType ? String(contentType) : "all";
    const mediaSegment = mediaType ? String(mediaType) : "video";
    return `${query.mode}|${contentSegment}|${mediaSegment}`;
  };
  const buildMixedContentCacheKey = (contentType) => {
    const contentSegment = contentType ? String(contentType) : "all";
    return `${query.mode}|${contentSegment}|mixed`;
  };

  const hasMoreRawPagesForImageAll = () => !imageAllCache.rawExhausted;

  const hasMoreRawPagesForImageDual = () =>
    IMAGE_MEDIA_TYPES.some(
      (mediaType) => !imageDualTypeState[mediaType].rawExhausted,
    );

  const hasMoreRawPagesForVideo = () => !videoCache.rawExhausted;
  const hasMoreRawPagesForMixedContent = () => !mixedContentCache.rawExhausted;

  const appendUniqueImageRecords = (records) => {
    if (!Array.isArray(records) || records.length === 0) return;
    const exists = new Set(imageAllCache.records.map((item) => item.id));
    let addedCount = 0;
    for (const record of records) {
      const key = record?.id;
      if (!key || exists.has(key)) continue;
      imageAllCache.records.push(record);
      exists.add(key);
      addedCount += 1;
    }
    return addedCount;
  };

  const appendUniqueDualImageRecords = (records) => {
    if (!Array.isArray(records) || records.length === 0) return;
    const exists = new Set(imageDualCache.records.map((item) => item.id));
    for (const record of records) {
      const key = record?.id;
      if (!key || exists.has(key)) continue;
      imageDualCache.records.push(record);
      exists.add(key);
    }
  };

  const appendUniqueVideoRecords = (records) => {
    if (!Array.isArray(records) || records.length === 0) return;
    const exists = new Set(videoCache.records.map((item) => item.id));
    let addedCount = 0;
    for (const record of records) {
      const key = record?.id;
      if (!key || exists.has(key)) continue;
      videoCache.records.push(record);
      exists.add(key);
      addedCount += 1;
    }
    return addedCount;
  };
  const appendUniqueMixedContentRecords = (records) => {
    if (!Array.isArray(records) || records.length === 0) return;
    const exists = new Set(mixedContentCache.records.map((item) => item.id));
    let addedCount = 0;
    for (const record of records) {
      const key = record?.id;
      if (!key || exists.has(key)) continue;
      mixedContentCache.records.push(record);
      exists.add(key);
      addedCount += 1;
    }
    return addedCount;
  };

  const fetchNextImageAllRawPage = async (contentType) => {
    const params = {
      page: imageAllCache.nextRawPage,
      size: IMAGE_ALL_FETCH_BATCH_SIZE,
    };
    if (contentType) {
      params.contentType = contentType;
    }

    const raw = await getItemListApi(params);
    const normalizedList = extractListPayload(raw).map(normalizeItem);
    const filteredByContentType = filterByContentType(normalizedList, contentType);
    const imageRecords = filterByImageSubType(filteredByContentType, "all");

    const addedCount = appendUniqueImageRecords(imageRecords) || 0;
    imageAllCache.rawTotal = Number(raw?.total || imageAllCache.rawTotal || 0);
    imageAllCache.rawLoadedCount += normalizedList.length;
    imageAllCache.nextRawPage += 1;

    if (
      imageAllCache.rawTotal > 0 &&
      imageAllCache.rawLoadedCount >= imageAllCache.rawTotal
    ) {
      imageAllCache.rawExhausted = true;
    }
    if (normalizedList.length === 0) {
      imageAllCache.rawExhausted = true;
    }
    if (normalizedList.length < IMAGE_ALL_FETCH_BATCH_SIZE) {
      imageAllCache.rawExhausted = true;
    }
    if (addedCount === 0) {
      imageAllCache.rawExhausted = true;
    }
  };

  const fetchNextImageDualRawPage = async (contentType) => {
    const requests = IMAGE_MEDIA_TYPES.filter(
      (mediaType) => !imageDualTypeState[mediaType].rawExhausted,
    ).map(async (mediaType) => {
      const state = imageDualTypeState[mediaType];
      const raw = await getItemListApi({
        page: state.nextRawPage,
        size: IMAGE_DUAL_FETCH_BATCH_SIZE,
        contentType,
        mediaType,
      });
      return { mediaType, raw };
    });

    if (requests.length === 0) return;
    const responses = await Promise.all(requests);

    for (const { mediaType, raw } of responses) {
      const state = imageDualTypeState[mediaType];
      const normalizedList = extractListPayload(raw).map(normalizeItem);
      appendUniqueDualImageRecords(normalizedList);

      state.rawTotal = Number(raw?.total || state.rawTotal || 0);
      state.rawLoadedCount += normalizedList.length;
      state.nextRawPage += 1;

      if (state.rawTotal > 0 && state.rawLoadedCount >= state.rawTotal) {
        state.rawExhausted = true;
      }
      if (normalizedList.length === 0) {
        state.rawExhausted = true;
      }
      if (normalizedList.length < IMAGE_DUAL_FETCH_BATCH_SIZE) {
        state.rawExhausted = true;
      }
    }
  };

  const fetchNextVideoRawPage = async (contentType, mediaType) => {
    const params = {
      page: videoCache.nextRawPage,
      size: VIDEO_FETCH_BATCH_SIZE,
    };
    if (contentType) {
      params.contentType = contentType;
    }
    if (mediaType) {
      params.mediaType = mediaType;
    }

    const raw = await getItemListApi(params);
    const normalizedList = extractListPayload(raw).map(normalizeItem);
    const filteredByContentType = filterByContentType(normalizedList, contentType);
    const addedCount = appendUniqueVideoRecords(filteredByContentType) || 0;

    videoCache.rawTotal = Number(raw?.total || videoCache.rawTotal || 0);
    videoCache.rawLoadedCount += normalizedList.length;
    videoCache.nextRawPage += 1;

    if (videoCache.rawTotal > 0 && videoCache.rawLoadedCount >= videoCache.rawTotal) {
      videoCache.rawExhausted = true;
    }
    if (normalizedList.length === 0 || normalizedList.length < VIDEO_FETCH_BATCH_SIZE) {
      videoCache.rawExhausted = true;
    }
    if (addedCount === 0) {
      videoCache.rawExhausted = true;
    }
  };
  const fetchNextMixedContentRawPage = async (contentType) => {
    const params = {
      page: mixedContentCache.nextRawPage,
      size: MIXED_CONTENT_FETCH_BATCH_SIZE,
    };
    if (contentType) {
      params.contentType = contentType;
    }

    const raw = await getItemListApi(params);
    const normalizedList = extractListPayload(raw).map(normalizeItem);
    const filteredByContentType = filterByContentType(normalizedList, contentType);
    const addedCount = appendUniqueMixedContentRecords(filteredByContentType) || 0;

    mixedContentCache.rawTotal = Number(raw?.total || mixedContentCache.rawTotal || 0);
    mixedContentCache.rawLoadedCount += normalizedList.length;
    mixedContentCache.nextRawPage += 1;

    if (
      mixedContentCache.rawTotal > 0 &&
      mixedContentCache.rawLoadedCount >= mixedContentCache.rawTotal
    ) {
      mixedContentCache.rawExhausted = true;
    }
    if (
      normalizedList.length === 0 ||
      normalizedList.length < MIXED_CONTENT_FETCH_BATCH_SIZE
    ) {
      mixedContentCache.rawExhausted = true;
    }
    if (addedCount === 0) {
      mixedContentCache.rawExhausted = true;
    }
  };

  const ensureImageAllCacheRecords = async ({ contentType, requiredCount }) => {
    const cacheKey = buildImageAllCacheKey(contentType);
    if (imageAllCache.key !== cacheKey) {
      resetImageAllCache();
      imageAllCache.key = cacheKey;
    }

    while (
      imageAllCache.records.length < requiredCount &&
      hasMoreRawPagesForImageAll()
    ) {
      if (!imageAllCacheLoadingPromise) {
        imageAllCacheLoadingPromise = fetchNextImageAllRawPage(contentType).finally(
          () => {
            imageAllCacheLoadingPromise = null;
          },
        );
      }
      await imageAllCacheLoadingPromise;
    }
  };

  const ensureImageDualCacheRecords = async ({ contentType, requiredCount }) => {
    const cacheKey = buildImageDualCacheKey(contentType);
    if (imageDualCache.key !== cacheKey) {
      resetImageDualCache();
      imageDualCache.key = cacheKey;
    }

    while (
      imageDualCache.records.length < requiredCount &&
      hasMoreRawPagesForImageDual()
    ) {
      if (!imageDualCacheLoadingPromise) {
        imageDualCacheLoadingPromise = fetchNextImageDualRawPage(contentType).finally(
          () => {
            imageDualCacheLoadingPromise = null;
          },
        );
      }
      await imageDualCacheLoadingPromise;
    }
  };

  const ensureVideoCacheRecords = async ({ contentType, mediaType, requiredCount }) => {
    const cacheKey = buildVideoCacheKey(contentType, mediaType);
    if (videoCache.key !== cacheKey) {
      resetVideoCache();
      videoCache.key = cacheKey;
    }

    while (videoCache.records.length < requiredCount && hasMoreRawPagesForVideo()) {
      if (!videoCacheLoadingPromise) {
        videoCacheLoadingPromise = fetchNextVideoRawPage(contentType, mediaType).finally(
          () => {
            videoCacheLoadingPromise = null;
          },
        );
      }
      await videoCacheLoadingPromise;
    }
  };
  const resolveMediaGroupForItem = (item) => {
    const mediaType = Number(resolveItemMediaType(item));
    if (mediaType === 2 || mediaType === 3 || mediaType === 4) return "image";
    if (mediaType === 5) return "video";
    if (mediaType === 6) return "link";
    return "text";
  };
  const filterByMediaGroup = (list, mediaGroup) => {
    if (mediaGroup === "all") return list;
    return list.filter((item) => resolveMediaGroupForItem(item) === mediaGroup);
  };
  const ensureMixedContentCacheRecords = async ({
    contentType,
    mediaGroup,
    requiredCount,
  }) => {
    const cacheKey = buildMixedContentCacheKey(contentType);
    if (mixedContentCache.key !== cacheKey) {
      resetMixedContentCache();
      mixedContentCache.key = cacheKey;
    }

    while (hasMoreRawPagesForMixedContent()) {
      const filteredCount = filterByMediaGroup(
        mixedContentCache.records,
        mediaGroup,
      ).length;
      if (filteredCount >= requiredCount) break;
      if (!mixedContentCacheLoadingPromise) {
        mixedContentCacheLoadingPromise = fetchNextMixedContentRawPage(contentType).finally(
          () => {
            mixedContentCacheLoadingPromise = null;
          },
        );
      }
      await mixedContentCacheLoadingPromise;
    }
  };

  const ensureImageFilteredCacheRecords = async ({
    contentType,
    imageSubType,
    requiredCount,
  }) => {
    await ensureImageAllCacheRecords({ contentType, requiredCount: 1 });

    while (hasMoreRawPagesForImageAll()) {
      const filteredCount = filterByImageSubType(
        imageAllCache.records,
        imageSubType,
      ).length;
      if (filteredCount >= requiredCount) {
        break;
      }
      await ensureImageAllCacheRecords({
        contentType,
        requiredCount: imageAllCache.records.length + 1,
      });
    }
  };

  const fetchImageFromCacheAndPaginate = async ({ contentType, imageSubType }) => {
    if (
      shouldUseDualImageRequests({
        mode: query.mode,
        contentType,
        mediaGroup: currentMediaGroup.value,
      })
    ) {
      const requiredCount = query.page * query.size;
      await ensureImageDualCacheRecords({ contentType, requiredCount });

      const filteredRecords = filterByImageSubType(imageDualCache.records, imageSubType);
      const start = (query.page - 1) * query.size;
      const pagedRows = filteredRecords.slice(start, start + query.size);

      if (hasMoreRawPagesForImageDual()) {
        return {
          rows: pagedRows,
          total: filteredRecords.length + query.size,
        };
      }
      return {
        rows: pagedRows,
        total: filteredRecords.length,
      };
    }

    const requiredCount = query.page * query.size;
    await ensureImageFilteredCacheRecords({
      contentType,
      imageSubType,
      requiredCount,
    });

    const filteredRecords = filterByImageSubType(imageAllCache.records, imageSubType);
    const start = (query.page - 1) * query.size;
    const pagedRows = filteredRecords.slice(start, start + query.size);

    if (hasMoreRawPagesForImageAll()) {
      return {
        rows: pagedRows,
        total: filteredRecords.length + query.size,
      };
    }
    return {
      rows: pagedRows,
      total: filteredRecords.length,
    };
  };

  const fetchVideoFromCacheAndPaginate = async ({ contentType, mediaType }) => {
    const requiredCount = query.page * query.size;
    await ensureVideoCacheRecords({ contentType, mediaType, requiredCount });

    const start = (query.page - 1) * query.size;
    const pagedRows = videoCache.records.slice(start, start + query.size);

    if (hasMoreRawPagesForVideo()) {
      return {
        rows: pagedRows,
        total: videoCache.records.length + query.size,
      };
    }
    return {
      rows: pagedRows,
      total: videoCache.records.length,
    };
  };
  const fetchMixedContentFromCacheAndPaginate = async ({
    contentType,
    mediaGroup,
  }) => {
    const requiredCount = query.page * query.size;
    await ensureMixedContentCacheRecords({
      contentType,
      mediaGroup,
      requiredCount,
    });

    const filteredRecords = filterByMediaGroup(mixedContentCache.records, mediaGroup);
    const start = (query.page - 1) * query.size;
    const pagedRows = filteredRecords.slice(start, start + query.size);

    if (hasMoreRawPagesForMixedContent()) {
      return {
        rows: pagedRows,
        total: filteredRecords.length + query.size,
      };
    }
    return {
      rows: pagedRows,
      total: filteredRecords.length,
    };
  };

  const fetchData = async ({ loadingRef } = {}) => {
    const requestId = ++latestRequestId;
    if (loadingRef) {
      loadingRef.value = true;
    }

    const commitResult = (nextRows, nextTotal) => {
      if (requestId !== latestRequestId) return;
      rows.value = nextRows;
      total.value = nextTotal;
    };

    try {
      const shouldUseMixedContentCache =
        query.mode === "content" &&
        PILL_STYLE_CONTENT_TYPES.includes(Number(query.contentType));
      if (shouldUseMixedContentCache) {
        const result = await fetchMixedContentFromCacheAndPaginate({
          contentType: query.contentType,
          mediaGroup: currentMediaGroup.value || "all",
        });
        commitResult(result.rows, result.total);
        return;
      }

      if (currentMediaGroup.value === "image") {
        const effectiveImageSubType = canUseImageSubType({
          mode: query.mode,
          contentType: query.contentType,
          mediaGroup: currentMediaGroup.value,
        })
          ? query.imageSubType
          : "all";
        const result = await fetchImageFromCacheAndPaginate({
          contentType: query.mode === "content" ? query.contentType : undefined,
          imageSubType: effectiveImageSubType,
        });
        commitResult(result.rows, result.total);
        return;
      }
      if (currentMediaGroup.value === "video") {
        const result = await fetchVideoFromCacheAndPaginate({
          contentType: query.mode === "content" ? query.contentType : undefined,
          mediaType: query.mediaType || 5,
        });
        commitResult(result.rows, result.total);
        return;
      }

      const params = {};
      params.page = query.page;
      params.size = query.size;

      if (query.mode === "content" && query.contentType) {
        params.contentType = query.contentType;
      }
      if (query.mediaType) {
        params.mediaType = query.mediaType;
      }

      const data = await getItemListApi(params);
      let list = extractListPayload(data).map(normalizeItem);
      list = filterByContentType(
        list,
        query.mode === "content" ? query.contentType : undefined,
      );
      if (currentMediaGroup.value === "image") {
        list = filterByImageSubType(list, query.imageSubType);
      }
      commitResult(list, Number(data?.total ?? list.length));
    } finally {
      if (loadingRef && requestId === latestRequestId) {
        loadingRef.value = false;
      }
    }
  };

  return {
    fetchData,
    resetCaches,
  };
};

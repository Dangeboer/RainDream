import http from "./http";

export const presignOssUploadApi = (payload) =>
  http.post("/api/oss/presign", payload);

export const deleteOssObjectApi = (storeUrl, options = {}) =>
  http.delete("/api/oss/object", {
    params: { storeUrl },
    suppressError: !!options.suppressError,
  });

import http from "./http";

export const presignOssUploadApi = (payload) =>
  http.post("/api/oss/presign", payload);

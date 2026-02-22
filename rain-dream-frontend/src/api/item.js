import http from "./http";

export const getItemListApi = (params) =>
  http.get("/api/item/list", { params });
export const getItemDetailApi = (itemId) =>
  http.get(`/api/item/detail/${itemId}`);
export const getFanficListApi = (params) =>
  http.get("/api/item/fanfic/list", { params });
export const getFanficDetailApi = (itemId, options = {}) =>
  http.get(`/api/item/fanfic/detail/${itemId}`, options);
export const createItemApi = (payload) =>
  http.post("/api/item/create", payload);
export const createBatchItemApi = (payload) =>
  http.post("/api/item/create/batch", payload);
export const updateItemApi = (itemId, payload) =>
  http.put(`/api/item/update/${itemId}`, payload);
export const setItemFavoriteApi = (itemId, isFavorite) =>
  http.put(`/api/item/favorite/${itemId}`, null, { params: { isFavorite } });
export const deleteItemApi = (itemId) =>
  http.delete(`/api/item/delete/${itemId}`);

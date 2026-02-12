import http from './http'

export const getItemListApi = (params) => http.get('/api/item/list', { params })
export const getItemDetailApi = (itemId) => http.get(`/api/item/detail/${itemId}`)
export const getFanficListApi = (params) => http.get('/api/item/fanfic/list', { params })
export const getFanficDetailApi = (itemId) => http.get(`/api/item/fanfic/detail/${itemId}`)
export const createItemApi = (payload) => http.post('/api/item/create', payload)
export const updateItemApi = (itemId, payload) => http.put(`/api/item/update/${itemId}`, payload)
export const deleteItemApi = (itemId) => http.delete(`/api/item/delete/${itemId}`)

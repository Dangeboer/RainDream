import http from './http'

export const getTagApi = () => http.get('/api/tag')
export const createTagApi = (payload) => http.post('/api/tag/create', payload)
export const updateTagApi = (tagId, payload) => http.put(`/api/tag/update/${tagId}`, payload)
export const deleteTagApi = (tagId) => http.delete(`/api/tag/delete/${tagId}`)

export const getPltApi = () => http.get('/api/plt')
export const createPltApi = (payload) => http.post('/api/plt/create', payload)
export const updatePltApi = (pltId, payload) => http.put(`/api/plt/update/${pltId}`, payload)
export const deletePltApi = (pltId) => http.delete(`/api/plt/delete/${pltId}`)

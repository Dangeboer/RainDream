import http from './http'

const normalizeMetaList = (list, key) => {
  if (!Array.isArray(list)) return []
  return list
    .map((item) => ({
      ...item,
      id: item?.id,
      name: item?.name || item?.[key]
    }))
    .filter((item) => item?.id !== undefined && item?.id !== null && item?.name)
}

export const getTagApi = async () => {
  const data = await http.get('/api/tag')
  return normalizeMetaList(data, 'tag_name')
}
export const createTagApi = (payload) => http.post('/api/tag/create', payload)
export const updateTagApi = (tagId, payload, options = {}) =>
  http.put(`/api/tag/update/${tagId}`, payload, {
    params: { force: !!options.force },
    suppressError: !!options.suppressError
  })
export const deleteTagApi = (tagId) => http.delete(`/api/tag/delete/${tagId}`)

export const getPltApi = async () => {
  const data = await http.get('/api/plt')
  return normalizeMetaList(data, 'plt_name')
}
export const createPltApi = (payload) => http.post('/api/plt/create', payload)
export const updatePltApi = (pltId, payload, options = {}) =>
  http.put(`/api/plt/update/${pltId}`, payload, {
    params: { force: !!options.force },
    suppressError: !!options.suppressError
  })
export const deletePltApi = (pltId) => http.delete(`/api/plt/delete/${pltId}`)

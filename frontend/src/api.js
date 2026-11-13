import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 10000 })

export const contactApi = {
  list: (keyword = '') => http.get('/contacts', { params: { keyword } }),
  create: (data) => http.post('/contacts', data),
  update: (id, data) => http.put(`/contacts/${id}`, data),
  remove: (id) => http.delete(`/contacts/${id}`),
}

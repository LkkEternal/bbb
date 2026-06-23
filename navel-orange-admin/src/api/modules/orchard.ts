import request from '../request'

export function getOrchardList() {
  return request.get('/orchards')
}

export function getOrchardDetail(id: number) {
  return request.get(`/orchards/${id}`)
}

export function createOrchard(data: {
  name: string
  formattedAddress: string
  areaMu?: number
  treeAge?: number
  variety?: string
}) {
  return request.post('/orchards', data)
}

export function updateOrchard(id: number, data: Partial<{
  name: string
  formattedAddress: string
  areaMu: number
  treeAge: number
  variety: string
}>) {
  return request.put(`/orchards/${id}`, data)
}

export function deleteOrchard(id: number) {
  return request.delete(`/orchards/${id}`)
}

export function addMember(orchardId: number, data: { userId: number; roleType: string }) {
  return request.post(`/orchards/${orchardId}/members`, data)
}

export function removeMember(orchardId: number, userId: number) {
  return request.delete(`/orchards/${orchardId}/members/${userId}`)
}

export function getDeviceList(orchardId: number) {
  return request.get(`/orchards/${orchardId}/devices`)
}

export function registerDevice(data: {
  deviceCode: string
  orchardId: number
  name: string
  type: string
  model?: string
  locationDesc?: string
}) {
  return request.post('/devices', data)
}

export function updateDevice(id: number, data: Partial<{ name: string; model: string; locationDesc: string }>) {
  return request.put(`/devices/${id}`, data)
}

export function getDeviceStatus(id: number) {
  return request.get(`/devices/${id}/status`)
}

export function deleteDevice(id: number) {
  return request.delete(`/devices/${id}`)
}

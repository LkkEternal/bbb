import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import authMock from './modules/auth.json'
import usersMock from './modules/users.json'
import orchardsMock from './modules/orchards.json'
import detectionsMock from './modules/detections.json'
import alertsMock from './modules/alerts.json'
import messagesMock from './modules/messages.json'
import dashboardMock from './modules/dashboard.json'
import calendarMock from './modules/calendar.json'
import feedbackMock from './modules/feedback.json'
import knowledgeMock from './modules/knowledge.json'

let mockInstance: MockAdapter | null = null

export function setupMock(axiosInstance: any) {
  if (import.meta.env.VITE_USE_MOCK !== 'true') return

  mockInstance = new MockAdapter(axiosInstance, { delayResponse: 300 })

  // Auth
  mockInstance.onPost('/auth/login/password').reply(200, authMock.loginSuccess)
  mockInstance.onPost('/auth/login/sms').reply(200, authMock.loginSuccess)
  mockInstance.onPost('/auth/register').reply(200, authMock.registerSuccess)
  mockInstance.onPost('/auth/sms/send').reply(200, { code: 200, message: '验证码已发送', data: { expireSeconds: 300 }, timestamp: Date.now() })
  mockInstance.onPost('/auth/refresh').reply(200, authMock.refreshSuccess)
  mockInstance.onPost('/auth/logout').reply(200, { code: 200, message: '退出成功', data: null, timestamp: Date.now() })

  // Users
  mockInstance.onGet('/admin/users').reply(200, usersMock.userList)
  mockInstance.onGet(/\/admin\/users\/\d+/).reply(200, usersMock.userDetail)
  mockInstance.onPut(/\/admin\/users\/\d+\/status/).reply(200, { code: 200, message: '更新成功', data: null, timestamp: Date.now() })
  mockInstance.onPut(/\/admin\/users\/\d+\/roles/).reply(200, { code: 200, message: '分配成功', data: null, timestamp: Date.now() })
  mockInstance.onGet('/admin/logs').reply(200, usersMock.operationLogs)
  mockInstance.onGet('/user/profile').reply(200, usersMock.profile)

  // Orchards
  mockInstance.onGet('/orchards').reply(200, orchardsMock.orchardList)
  mockInstance.onGet(/\/orchards\/\d+$/).reply(200, orchardsMock.orchardDetail)
  mockInstance.onPost('/orchards').reply(200, { code: 200, message: '创建成功', data: { id: 2003, name: '新果园' }, timestamp: Date.now() })
  mockInstance.onPut(/\/orchards\/\d+$/).reply(200, { code: 200, message: '更新成功', data: null, timestamp: Date.now() })
  mockInstance.onDelete(/\/orchards\/\d+$/).reply(200, { code: 200, message: '删除成功', data: null, timestamp: Date.now() })
  mockInstance.onGet(/\/orchards\/\d+\/devices/).reply(200, orchardsMock.deviceList)
  mockInstance.onPost('/devices').reply(200, { code: 200, message: '注册成功', data: { id: 3003, deviceCode: 'DEV-2026-003', apiKey: 'ak_test123' }, timestamp: Date.now() })

  // Detections
  mockInstance.onGet('/detections').reply(200, detectionsMock.detectionList)
  mockInstance.onGet('/detections/stats').reply(200, detectionsMock.detectionStats)
  mockInstance.onGet('/environment/trend').reply(200, detectionsMock.environmentTrend)

  // Alerts
  mockInstance.onGet('/admin/rules').reply(200, alertsMock.ruleList)
  mockInstance.onPut(/\/admin\/rules\/\d+/).reply(200, { code: 200, message: '更新成功', data: null, timestamp: Date.now() })
  mockInstance.onPut(/\/admin\/rules\/\d+\/toggle/).reply(200, { code: 200, message: '切换成功', data: null, timestamp: Date.now() })
  mockInstance.onGet('/alert/risk').reply(200, alertsMock.riskAssessment)

  // Messages
  mockInstance.onGet('/messages').reply(200, messagesMock.messageList)
  mockInstance.onGet('/messages/unread-count').reply(200, { code: 200, message: 'success', data: { count: 5 }, timestamp: Date.now() })
  mockInstance.onPut(/\/messages\/\d+\/read$/).reply(200, { code: 200, message: '标记成功', data: null, timestamp: Date.now() })
  mockInstance.onPut(/\/messages\/\d+\/unread/).reply(200, { code: 200, message: '标记成功', data: null, timestamp: Date.now() })
  mockInstance.onPut('/messages/read-all').reply(200, { code: 200, message: '全部标记成功', data: null, timestamp: Date.now() })

  // Dashboard
  mockInstance.onGet('/miniapp/dashboard').reply(200, dashboardMock.dashboard)

  // Calendar
  mockInstance.onGet('/admin/calendar').reply(200, calendarMock.calendarList)
  mockInstance.onGet('/miniapp/calendar/current').reply(200, calendarMock.currentReminder)

  // Knowledge
  mockInstance.onGet('/admin/knowledge/documents').reply(200, knowledgeMock.documentList)
  mockInstance.onPost('/knowledge/search').reply(200, knowledgeMock.searchResult)
  mockInstance.onPost('/knowledge/qa').reply(200, knowledgeMock.qaResult)

  // Feedback
  mockInstance.onGet('/admin/feedbacks').reply(200, feedbackMock.feedbackList)
  mockInstance.onGet(/\/admin\/feedbacks\/\d+/).reply(200, feedbackMock.feedbackDetail)
  mockInstance.onPost(/\/admin\/feedbacks\/\d+\/review/).reply(200, { code: 200, message: '审核成功', data: null, timestamp: Date.now() })

  // Video Config
  mockInstance.onGet('/config/video').reply(200, { code: 200, message: 'success', data: { host: '10.168.80.149', wsPort: 8001, httpPort: 8000 }, timestamp: Date.now() })
}

export function getMockInstance() {
  return mockInstance
}

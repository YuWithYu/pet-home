/**
 * 预约变更申请 API（用户提交变更，工作人员同意/拒绝）
 */
import request from '@/utils/request'

const base = '/appointment-change-request'

/** 待确认的变更申请列表 */
export function listPending() {
  return request({
    url: `${base}/pending`,
    method: 'get'
  })
}

/** 同意变更 */
export function approve(id) {
  return request({
    url: `${base}/${id}/approve`,
    method: 'post'
  })
}

/** 拒绝变更 */
export function reject(id) {
  return request({
    url: `${base}/${id}/reject`,
    method: 'post'
  })
}

/** 按预约查询待处理变更（弹窗审核用） */
export function getPendingByAppointment(appointmentType, appointmentId) {
  return request({
    url: `${base}/pending-by-appointment`,
    method: 'get',
    params: { appointmentType, appointmentId }
  })
}

export default {
  listPending,
  approve,
  reject,
  getPendingByAppointment
}

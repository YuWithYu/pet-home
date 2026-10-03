/**
 * 取消预约申请 API（用户提交取消原因，工作人员同意/拒绝；同意后执行取消并应用 0-2 小时违约金）
 */
import request from '@/utils/request'

const base = '/appointment-cancellation-request'

/** 待确认的取消预约申请列表 */
export function listPending() {
  return request({
    url: `${base}/pending`,
    method: 'get'
  })
}

/** 同意取消（执行取消并应用违约金规则） */
export function approve(id) {
  return request({
    url: `${base}/${id}/approve`,
    method: 'post'
  })
}

/** 拒绝取消（rejectReason 必填） */
export function reject(id, rejectReason) {
  return request({
    url: `${base}/${id}/reject`,
    method: 'post',
    data: { rejectReason }
  })
}

/** 按预约查询待处理取消申请（弹窗审核用） */
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

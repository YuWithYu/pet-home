import request from '@/utils/request'

// 服务预约时间管理API

/**
 * 获取可预约时间段
 */
export function getAvailableTimeSlots(serviceType, date) {
  return request({
    url: '/schedule/available',
    method: 'get',
    params: { serviceType, date }
  })
}

/**
 * 获取服务人员在指定日期的排班
 */
export function getMemberSchedule(memberId, date) {
  return request({
    url: `/schedule/member/${memberId}`,
    method: 'get',
    params: { date }
  })
}


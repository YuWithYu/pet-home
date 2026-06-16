import request from '@/utils/request'

// ==================== 管理员登录与信息 ====================

/**
 * 管理员登录
 */
export function adminLogin(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录管理员信息
 */
export function getCurrentAdmin() {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

/**
 * 当前用户更新头像（任意后台用户可换头像，全系统同步）
 * @param {string} avatar - 头像 URL（上传接口返回的地址）
 */
export function updateMyAvatar(avatar) {
  return request({
    url: '/admin/profile/avatar',
    method: 'put',
    data: { avatar }
  })
}

// ==================== 团队管理相关接口（使用 admin 表） ====================

/**
 * 获取员工列表（可选按部门、按门店过滤）
 * @param {string} [department] - 部门名称
 * @param {number} [storeId] - 所属服务门店ID，传则只返回该门店员工
 */
export function getAdminStaffList(department, storeId) {
  return request({
    url: '/admin/staff/list',
    method: 'get',
    params: { department, storeId }
  })
}

/**
 * 添加员工
 */
export function addAdminStaff(data) {
  return request({
    url: '/admin/staff/add',
    method: 'post',
    data
  })
}

/**
 * 更新员工信息
 */
export function updateAdminStaff(data) {
  return request({
    url: '/admin/staff/update',
    method: 'put',
    data
  })
}

/**
 * 删除员工
 */
export function deleteAdminStaff(id) {
  return request({
    url: `/admin/staff/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID查询员工
 */
export function getAdminStaffById(id) {
  return request({
    url: `/admin/staff/${id}`,
    method: 'get'
  })
}

/**
 * 获取员工的预约/任务列表（项目参与、任务分配）
 */
export function getStaffAppointments(adminId) {
  return request({
    url: `/admin/staff/${adminId}/appointments`,
    method: 'get'
  })
}

/**
 * 获取服务人员的所有服务评价
 */
export function getMemberRatings(memberId) {
  return request({
    url: `/appointment/rating/member/${memberId}`,
    method: 'get'
  })
}

// ==================== 社区管理相关接口（保留原有功能） ====================

// 获取社区统计数据
export function getCommunityStatistics() {
  return request({
    url: '/admin/community/statistics',
    method: 'get'
  })
}

// 获取所有帖子（管理员）
export function getAdminPosts(params) {
  return request({
    url: '/admin/community/posts',
    method: 'get',
    params
  })
}

// 更新帖子状态
export function updatePostStatus(postId, status) {
  return request({
    url: `/admin/community/posts/${postId}/status`,
    method: 'put',
    params: { status }
  })
}

// 设置帖子置顶
export function updatePostTop(postId, isTop) {
  return request({
    url: `/admin/community/posts/${postId}/top`,
    method: 'put',
    params: { isTop }
  })
}

// 设置帖子热门
export function updatePostHot(postId, isHot) {
  return request({
    url: `/admin/community/posts/${postId}/hot`,
    method: 'put',
    params: { isHot }
  })
}

// 删除帖子（管理员）
export function deletePost(postId) {
  return request({
    url: `/admin/community/posts/${postId}`,
    method: 'delete'
  })
}

// 获取所有评论（管理员）
export function getAdminComments(params) {
  return request({
    url: '/admin/community/comments',
    method: 'get',
    params
  })
}

// 更新评论状态
export function updateCommentStatus(commentId, status) {
  return request({
    url: `/admin/community/comments/${commentId}/status`,
    method: 'put',
    params: { status }
  })
}

// 删除评论（管理员）
export function deleteComment(commentId) {
  return request({
    url: `/admin/community/comments/${commentId}`,
    method: 'delete'
  })
}

// 获取用户的所有帖子
export function getUserPosts(userId) {
  return request({
    url: `/admin/community/users/${userId}/posts`,
    method: 'get'
  })
}

// 获取用户的所有评论
export function getUserComments(userId) {
  return request({
    url: `/admin/community/users/${userId}/comments`,
    method: 'get'
  })
}

// ==================== 每日专题管理相关接口 ====================

// 获取每日专题帖子列表
export function getDailyTopicPosts(params) {
  return request({
    url: '/admin/community/daily-topics/posts',
    method: 'get',
    params
  })
}

// 获取每日专题Banner列表
export function getDailyTopicBanners() {
  return request({
    url: '/admin/community/daily-topics/banners',
    method: 'get'
  })
}

// 创建或更新每日专题Banner
export function saveDailyTopicBanner(data) {
  return request({
    url: '/admin/community/daily-topics/banners',
    method: 'post',
    data
  })
}

// 删除每日专题Banner
export function deleteDailyTopicBanner(id) {
  return request({
    url: `/admin/community/daily-topics/banners/${id}`,
    method: 'delete'
  })
}

// ==================== 每日专题管理相关接口（新版本） ====================

// 获取专题列表（管理端）
export function getDailyTopicsList(params) {
  return request({
    url: '/admin/daily-topics',
    method: 'get',
    params
  })
}

// 获取专题详情
export function getDailyTopicDetail(id) {
  return request({
    url: `/admin/daily-topics/${id}`,
    method: 'get'
  })
}

// 创建专题
export function createDailyTopic(data) {
  return request({
    url: '/admin/daily-topics',
    method: 'post',
    data
  })
}

// 更新专题
export function updateDailyTopic(id, data) {
  return request({
    url: `/admin/daily-topics/${id}`,
    method: 'put',
    data
  })
}

// 删除专题
export function deleteDailyTopic(id) {
  return request({
    url: `/admin/daily-topics/${id}`,
    method: 'delete'
  })
}

// 发布专题
export function publishDailyTopic(id) {
  return request({
    url: `/admin/daily-topics/${id}/publish`,
    method: 'post'
  })
}

// 下线专题
export function offlineDailyTopic(id) {
  return request({
    url: `/admin/daily-topics/${id}/offline`,
    method: 'post'
  })
}

// 获取专题关联的帖子
export function getDailyTopicPostsById(id) {
  return request({
    url: `/admin/daily-topics/${id}/posts`,
    method: 'get'
  })
}

// 关联帖子到专题
export function associatePostsToTopic(id, postIds) {
  return request({
    url: `/admin/daily-topics/${id}/posts`,
    method: 'post',
    data: { postIds }
  })
}

// 搜索帖子（用于关联）
export function searchPostsForTopic(params) {
  return request({
    url: '/admin/daily-topics/posts/search',
    method: 'get',
    params
  })
}

// ==================== 专题主题分类（后台增删改查，与小程序 Tab 同步） ====================
export function getDailyTopicThemes() {
  return request({
    url: '/admin/daily-topic-themes',
    method: 'get'
  })
}

export function createDailyTopicTheme(data) {
  return request({
    url: '/admin/daily-topic-themes',
    method: 'post',
    data
  })
}

export function updateDailyTopicTheme(id, data) {
  return request({
    url: `/admin/daily-topic-themes/${id}`,
    method: 'put',
    data
  })
}

export function deleteDailyTopicTheme(id) {
  return request({
    url: `/admin/daily-topic-themes/${id}`,
    method: 'delete'
  })
}
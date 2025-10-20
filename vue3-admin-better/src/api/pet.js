import request from "@/utils/request";

// 宠物管理API
export function getPetList(params) {
  return request({
    url: "/pets/page",
    method: "get",
    params: {
      page: params.pageNo || params.page || 1,
      size: params.pageSize || params.size || 10
    },
  });
}

export function getPetById(id) {
  return request({
    url: `/pets/${id}`,
    method: "get",
  });
}

export function createPet(data) {
  return request({
    url: "/pets",
    method: "post",
    data,
  });
}

export function updatePet(id, data) {
  return request({
    url: `/pets/${id}`,
    method: "put",
    data,
  });
}

export function deletePet(id) {
  return request({
    url: `/pets/${id}`,
    method: "delete",
  });
}

// 用户管理API
export function getUserList(params) {
  return request({
    url: "/users/list",
    method: "get",
    params: {
      page: params.pageNo || params.page || 1,
      size: params.pageSize || params.size || 10
    },
  });
}

export function getUserById(id) {
  return request({
    url: `/api/users/${id}`,
    method: "get",
  });
}

export function updateUser(id, data) {
  return request({
    url: `/api/users/${id}`,
    method: "put",
    data,
  });
}

// 商品管理API
export function getProductList(params) {
  return request({
    url: "/products",
    method: "get",
    params,
  });
}

export function createProduct(data) {
  return request({
    url: "/products",
    method: "post",
    data,
  });
}

export function updateProduct(id, data) {
  return request({
    url: `/api/products/${id}`,
    method: "put",
    data,
  });
}

export function deleteProduct(id) {
  return request({
    url: `/api/products/${id}`,
    method: "delete",
  });
}

// 订单管理API
export function getOrderList(params) {
  return request({
    url: "/orders",
    method: "get",
    params,
  });
}

export function getOrderById(id) {
  return request({
    url: `/api/orders/${id}`,
    method: "get",
  });
}

export function updateOrderStatus(id, status) {
  return request({
    url: `/api/orders/${id}/status`,
    method: "put",
    data: { status },
  });
}

// 社区管理API
export function getCommunityList(params) {
  return request({
    url: "/community",
    method: "get",
    params,
  });
}

export function deleteCommunityPost(id) {
  return request({
    url: `/api/community/${id}`,
    method: "delete",
  });
}

// 预约管理API
export function getAppointmentList(params) {
  return request({
    url: "/appointments",
    method: "get",
    params,
  });
}

export function updateAppointmentStatus(id, status) {
  return request({
    url: `/api/door-cleaning/${id}/status`,
    method: "put",
    params: { status },
  });
}

// 统计数据API
export function getStatistics() {
  return request({
    url: "/statistics",
    method: "get",
  });
}

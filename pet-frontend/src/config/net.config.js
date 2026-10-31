const network = {
  // 本项目仅本地运行：直连本地后端（不再区分生产环境域名）
  baseURL: "http://localhost:8080/api",
  contentType: "application/json;charset=UTF-8",
  messageDuration: 3000,
  requestTimeout: 30000,
  // 后端 Result 约定：200=成功，401=未登录/Token失效，403=无权限，400=参数错误，500=系统错误
  successCode: [200],
  invalidCode: 401,
  noPermissionCode: 403,
};
module.exports = network;

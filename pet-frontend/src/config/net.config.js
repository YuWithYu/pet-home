const network = {
  // 生产环境后端地址（开发环境走 devServer 代理，见 rspack.config.js devServer.proxy）
  baseURL:
    process.env.NODE_ENV === "production"
      ? "https://situationship.icu/api"
      : "http://localhost:8080/api",
  contentType: "application/json;charset=UTF-8",
  messageDuration: 3000,
  requestTimeout: 30000,
  // 后端 Result 约定：200=成功，401=未登录/Token失效，403=无权限，400=参数错误，500=系统错误
  successCode: [200],
  invalidCode: 401,
  noPermissionCode: 403,
};
module.exports = network;

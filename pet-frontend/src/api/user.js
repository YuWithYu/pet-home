import request from "@/utils/request";

export async function login(data) {
  return request({
    url: "/user/login",
    method: "post",
    data,
  });
}

export function getUserInfo(accessToken) {
  return request({
    url: "/user/current",
    method: "get",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
    },
  });
}

export function logout() {
  // JWT是无状态的，前端直接清除token即可
  return Promise.resolve({ code: 200, msg: "退出成功" });
}

export function sendSmsCode(phone) {
  return request({
    url: "/sms/send",
    method: "post",
    data: { phone },
  });
}

export function register(data) {
  return request({
    url: "/user/register",
    method: "post",
    data,
  });
}

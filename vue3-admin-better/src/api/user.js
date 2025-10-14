import request from "@/utils/request";
import { tokenName } from "@/config";

export async function login(data) {
  return request({
    url: "/users/login",
    method: "post",
    data,
  });
}

export function getUserInfo(accessToken) {
  return request({
    url: "/users/current",
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

export function register(data) {
  return request({
    url: "/users/register",
    method: "post",
    data,
  });
}

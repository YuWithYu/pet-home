import { storage, tokenTableName } from "@/config";

const getStorage = () =>
  storage === "sessionStorage" ? sessionStorage : localStorage;

export function getAccessToken() {
  return getStorage().getItem(tokenTableName);
}

export function setAccessToken(accessToken) {
  getStorage().setItem(tokenTableName, accessToken);
}

export function removeAccessToken() {
  getStorage().removeItem(tokenTableName);
}

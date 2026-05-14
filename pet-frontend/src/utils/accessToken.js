import { storage, tokenTableName } from '@/config'

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 获取accessToken
 * @returns {string|ActiveX.IXMLDOMNode|Promise<any>|any|IDBRequest<any>|MediaKeyStatus|FormDataEntryValue|Function|Promise<Credential | null>}
 */
export function getAccessToken() {
  if (storage) {
    if ('localStorage' === storage) {
      return localStorage.getItem(tokenTableName)
    } else if ('sessionStorage' === storage) {
      return sessionStorage.getItem(tokenTableName)
    } else {
      return localStorage.getItem(tokenTableName)
    }
  } else {
    return localStorage.getItem(tokenTableName)
  }
}

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 存储accessToken
 * @param accessToken
 * @returns {void|*}
 */
export function setAccessToken(accessToken) {
  if (storage) {
    if ('localStorage' === storage) {
      return localStorage.setItem(tokenTableName, accessToken)
    } else if ('sessionStorage' === storage) {
      return sessionStorage.setItem(tokenTableName, accessToken)
    } else {
      return localStorage.setItem(tokenTableName, accessToken)
    }
  } else {
    return localStorage.setItem(tokenTableName, accessToken)
  }
}

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 移除accessToken
 * @returns {void|Promise<void>}
 */
export function removeAccessToken() {
  console.trace('removeAccessToken 被调用 - 调用栈');
  if (storage) {
    if ('localStorage' === storage) {
      console.warn('清除 localStorage token:', tokenTableName);
      return localStorage.removeItem(tokenTableName)
    } else if ('sessionStorage' === storage) {
      console.warn('清除 sessionStorage token:', tokenTableName);
      // 只清除token，不清除整个sessionStorage
      return sessionStorage.removeItem(tokenTableName)
    } else {
      console.warn('清除 localStorage token (default):', tokenTableName);
      return localStorage.removeItem(tokenTableName)
    }
  } else {
    console.warn('清除 localStorage token (no storage config):', tokenTableName);
    return localStorage.removeItem(tokenTableName)
  }
}

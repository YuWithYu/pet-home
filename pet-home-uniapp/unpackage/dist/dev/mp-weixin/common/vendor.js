(global["webpackJsonp"] = global["webpackJsonp"] || []).push([["common/vendor"],{

/***/ 1:
/*!*********************************************************!*\
  !*** ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var objectKeys = ['qy', 'env', 'error', 'version', 'lanDebug', 'cloud', 'serviceMarket', 'router', 'worklet', '__webpack_require_UNI_MP_PLUGIN__'];
var singlePageDisableKey = ['lanDebug', 'router', 'worklet'];
var target = typeof globalThis !== 'undefined' ? globalThis : function () {
  return this;
}();
var key = ['w', 'x'].join('');
var oldWx = target[key];
var launchOption = oldWx.getLaunchOptionsSync ? oldWx.getLaunchOptionsSync() : null;
function isWxKey(key) {
  if (launchOption && launchOption.scene === 1154 && singlePageDisableKey.includes(key)) {
    return false;
  }
  return objectKeys.indexOf(key) > -1 || typeof oldWx[key] === 'function';
}
function initWx() {
  var newWx = {};
  for (var _key in oldWx) {
    if (isWxKey(_key)) {
      // TODO wrapper function
      newWx[_key] = oldWx[_key];
    }
  }
  return newWx;
}
target[key] = initWx();
if (!target[key].canIUse('getAppBaseInfo')) {
  target[key].getAppBaseInfo = target[key].getSystemInfoSync;
}
if (!target[key].canIUse('getWindowInfo')) {
  target[key].getWindowInfo = target[key].getSystemInfoSync;
}
if (!target[key].canIUse('getDeviceInfo')) {
  target[key].getDeviceInfo = target[key].getSystemInfoSync;
}
var _default = target[key];
exports.default = _default;

/***/ }),

/***/ 10:
/*!****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/nonIterableRest.js ***!
  \****************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _nonIterableRest() {
  throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}
module.exports = _nonIterableRest, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 11:
/*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/defineProperty.js ***!
  \***************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var toPropertyKey = __webpack_require__(/*! ./toPropertyKey.js */ 12);
function _defineProperty(obj, key, value) {
  key = toPropertyKey(key);
  if (key in obj) {
    Object.defineProperty(obj, key, {
      value: value,
      enumerable: true,
      configurable: true,
      writable: true
    });
  } else {
    obj[key] = value;
  }
  return obj;
}
module.exports = _defineProperty, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 12:
/*!**************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toPropertyKey.js ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var _typeof = __webpack_require__(/*! ./typeof.js */ 13)["default"];
var toPrimitive = __webpack_require__(/*! ./toPrimitive.js */ 14);
function toPropertyKey(t) {
  var i = toPrimitive(t, "string");
  return "symbol" == _typeof(i) ? i : i + "";
}
module.exports = toPropertyKey, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 123:
/*!**************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/common/js/api/cart.js ***!
  \**************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _api = __webpack_require__(/*! ../api.js */ 38);
// 购物车相关API
var _default = {
  // 添加商品到购物车
  addToCart: function addToCart(data) {
    return _api.api.request({
      url: '/api/cart/add',
      method: 'POST',
      data: data
    });
  },
  // 获取购物车列表
  fetchCartList: function fetchCartList(userId) {
    // 构建URL，如果提供了userId，则作为参数传递
    var url = '/api/cart/list';
    if (userId) {
      url += "?userId=".concat(userId);
    }
    return _api.api.request({
      url: url,
      method: 'GET'
    });
  },
  // 删除购物车商品
  deleteCartItem: function deleteCartItem(params) {
    return _api.api.request({
      url: '/api/cart/delete',
      method: 'POST',
      data: params
    });
  },
  // 更新购物车商品数量
  updateQuantity: function updateQuantity(params) {
    return _api.api.request({
      url: '/api/cart/update/quantity',
      method: 'POST',
      data: params
    });
  },
  // 清空购物车
  clearCartList: function clearCartList() {
    return _api.api.request({
      url: '/api/cart/clear',
      method: 'POST'
    });
  }
};
exports.default = _default;

/***/ }),

/***/ 13:
/*!*******************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/typeof.js ***!
  \*******************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _typeof(o) {
  "@babel/helpers - typeof";

  return (module.exports = _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (o) {
    return typeof o;
  } : function (o) {
    return o && "function" == typeof Symbol && o.constructor === Symbol && o !== Symbol.prototype ? "symbol" : typeof o;
  }, module.exports.__esModule = true, module.exports["default"] = module.exports), _typeof(o);
}
module.exports = _typeof, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 14:
/*!************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toPrimitive.js ***!
  \************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var _typeof = __webpack_require__(/*! ./typeof.js */ 13)["default"];
function toPrimitive(t, r) {
  if ("object" != _typeof(t) || !t) return t;
  var e = t[Symbol.toPrimitive];
  if (void 0 !== e) {
    var i = e.call(t, r || "default");
    if ("object" != _typeof(i)) return i;
    throw new TypeError("@@toPrimitive must return a primitive value.");
  }
  return ("string" === r ? String : Number)(t);
}
module.exports = toPrimitive, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 15:
/*!**********************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/construct.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var setPrototypeOf = __webpack_require__(/*! ./setPrototypeOf.js */ 16);
var isNativeReflectConstruct = __webpack_require__(/*! ./isNativeReflectConstruct.js */ 17);
function _construct(t, e, r) {
  if (isNativeReflectConstruct()) return Reflect.construct.apply(null, arguments);
  var o = [null];
  o.push.apply(o, e);
  var p = new (t.bind.apply(t, o))();
  return r && setPrototypeOf(p, r.prototype), p;
}
module.exports = _construct, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 16:
/*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/setPrototypeOf.js ***!
  \***************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _setPrototypeOf(o, p) {
  module.exports = _setPrototypeOf = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function _setPrototypeOf(o, p) {
    o.__proto__ = p;
    return o;
  }, module.exports.__esModule = true, module.exports["default"] = module.exports;
  return _setPrototypeOf(o, p);
}
module.exports = _setPrototypeOf, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 17:
/*!*************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/isNativeReflectConstruct.js ***!
  \*************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _isNativeReflectConstruct() {
  try {
    var t = !Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function () {}));
  } catch (t) {}
  return (module.exports = _isNativeReflectConstruct = function _isNativeReflectConstruct() {
    return !!t;
  }, module.exports.__esModule = true, module.exports["default"] = module.exports)();
}
module.exports = _isNativeReflectConstruct, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 18:
/*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toConsumableArray.js ***!
  \******************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var arrayWithoutHoles = __webpack_require__(/*! ./arrayWithoutHoles.js */ 19);
var iterableToArray = __webpack_require__(/*! ./iterableToArray.js */ 20);
var unsupportedIterableToArray = __webpack_require__(/*! ./unsupportedIterableToArray.js */ 8);
var nonIterableSpread = __webpack_require__(/*! ./nonIterableSpread.js */ 21);
function _toConsumableArray(arr) {
  return arrayWithoutHoles(arr) || iterableToArray(arr) || unsupportedIterableToArray(arr) || nonIterableSpread();
}
module.exports = _toConsumableArray, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 19:
/*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayWithoutHoles.js ***!
  \******************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var arrayLikeToArray = __webpack_require__(/*! ./arrayLikeToArray.js */ 9);
function _arrayWithoutHoles(arr) {
  if (Array.isArray(arr)) return arrayLikeToArray(arr);
}
module.exports = _arrayWithoutHoles, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 2:
/*!************************************************************!*\
  !*** ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js ***!
  \************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(wx, global) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createApp = createApp;
exports.createComponent = createComponent;
exports.createPage = createPage;
exports.createPlugin = createPlugin;
exports.createSubpackageApp = createSubpackageApp;
exports.default = void 0;
var _slicedToArray2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/slicedToArray */ 5));
var _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/defineProperty */ 11));
var _construct2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/construct */ 15));
var _toConsumableArray2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/toConsumableArray */ 18));
var _typeof2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/typeof */ 13));
var _uniI18n = __webpack_require__(/*! @dcloudio/uni-i18n */ 22);
var _vue = _interopRequireDefault(__webpack_require__(/*! vue */ 25));
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2.default)(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
var realAtob;
var b64 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
var b64re = /^(?:[A-Za-z\d+/]{4})*?(?:[A-Za-z\d+/]{2}(?:==)?|[A-Za-z\d+/]{3}=?)?$/;
if (typeof atob !== 'function') {
  realAtob = function realAtob(str) {
    str = String(str).replace(/[\t\n\f\r ]+/g, '');
    if (!b64re.test(str)) {
      throw new Error("Failed to execute 'atob' on 'Window': The string to be decoded is not correctly encoded.");
    }

    // Adding the padding if missing, for semplicity
    str += '=='.slice(2 - (str.length & 3));
    var bitmap;
    var result = '';
    var r1;
    var r2;
    var i = 0;
    for (; i < str.length;) {
      bitmap = b64.indexOf(str.charAt(i++)) << 18 | b64.indexOf(str.charAt(i++)) << 12 | (r1 = b64.indexOf(str.charAt(i++))) << 6 | (r2 = b64.indexOf(str.charAt(i++)));
      result += r1 === 64 ? String.fromCharCode(bitmap >> 16 & 255) : r2 === 64 ? String.fromCharCode(bitmap >> 16 & 255, bitmap >> 8 & 255) : String.fromCharCode(bitmap >> 16 & 255, bitmap >> 8 & 255, bitmap & 255);
    }
    return result;
  };
} else {
  // 注意atob只能在全局对象上调用，例如：`const Base64 = {atob};Base64.atob('xxxx')`是错误的用法
  realAtob = atob;
}
function b64DecodeUnicode(str) {
  return decodeURIComponent(realAtob(str).split('').map(function (c) {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
  }).join(''));
}
function getCurrentUserInfo() {
  var token = wx.getStorageSync('uni_id_token') || '';
  var tokenArr = token.split('.');
  if (!token || tokenArr.length !== 3) {
    return {
      uid: null,
      role: [],
      permission: [],
      tokenExpired: 0
    };
  }
  var userInfo;
  try {
    userInfo = JSON.parse(b64DecodeUnicode(tokenArr[1]));
  } catch (error) {
    throw new Error('获取当前用户信息出错，详细错误信息为：' + error.message);
  }
  userInfo.tokenExpired = userInfo.exp * 1000;
  delete userInfo.exp;
  delete userInfo.iat;
  return userInfo;
}
function uniIdMixin(Vue) {
  Vue.prototype.uniIDHasRole = function (roleId) {
    var _getCurrentUserInfo = getCurrentUserInfo(),
      role = _getCurrentUserInfo.role;
    return role.indexOf(roleId) > -1;
  };
  Vue.prototype.uniIDHasPermission = function (permissionId) {
    var _getCurrentUserInfo2 = getCurrentUserInfo(),
      permission = _getCurrentUserInfo2.permission;
    return this.uniIDHasRole('admin') || permission.indexOf(permissionId) > -1;
  };
  Vue.prototype.uniIDTokenValid = function () {
    var _getCurrentUserInfo3 = getCurrentUserInfo(),
      tokenExpired = _getCurrentUserInfo3.tokenExpired;
    return tokenExpired > Date.now();
  };
}
var _toString = Object.prototype.toString;
var hasOwnProperty = Object.prototype.hasOwnProperty;
function isFn(fn) {
  return typeof fn === 'function';
}
function isStr(str) {
  return typeof str === 'string';
}
function isObject(obj) {
  return obj !== null && (0, _typeof2.default)(obj) === 'object';
}
function isPlainObject(obj) {
  return _toString.call(obj) === '[object Object]';
}
function hasOwn(obj, key) {
  return hasOwnProperty.call(obj, key);
}
function noop() {}

/**
 * Create a cached version of a pure function.
 */
function cached(fn) {
  var cache = Object.create(null);
  return function cachedFn(str) {
    var hit = cache[str];
    return hit || (cache[str] = fn(str));
  };
}

/**
 * Camelize a hyphen-delimited string.
 */
var camelizeRE = /-(\w)/g;
var camelize = cached(function (str) {
  return str.replace(camelizeRE, function (_, c) {
    return c ? c.toUpperCase() : '';
  });
});
function sortObject(obj) {
  var sortObj = {};
  if (isPlainObject(obj)) {
    Object.keys(obj).sort().forEach(function (key) {
      sortObj[key] = obj[key];
    });
  }
  return !Object.keys(sortObj) ? obj : sortObj;
}
var HOOKS = ['invoke', 'success', 'fail', 'complete', 'returnValue'];
var globalInterceptors = {};
var scopedInterceptors = {};
function mergeHook(parentVal, childVal) {
  var res = childVal ? parentVal ? parentVal.concat(childVal) : Array.isArray(childVal) ? childVal : [childVal] : parentVal;
  return res ? dedupeHooks(res) : res;
}
function dedupeHooks(hooks) {
  var res = [];
  for (var i = 0; i < hooks.length; i++) {
    if (res.indexOf(hooks[i]) === -1) {
      res.push(hooks[i]);
    }
  }
  return res;
}
function removeHook(hooks, hook) {
  var index = hooks.indexOf(hook);
  if (index !== -1) {
    hooks.splice(index, 1);
  }
}
function mergeInterceptorHook(interceptor, option) {
  Object.keys(option).forEach(function (hook) {
    if (HOOKS.indexOf(hook) !== -1 && isFn(option[hook])) {
      interceptor[hook] = mergeHook(interceptor[hook], option[hook]);
    }
  });
}
function removeInterceptorHook(interceptor, option) {
  if (!interceptor || !option) {
    return;
  }
  Object.keys(option).forEach(function (hook) {
    if (HOOKS.indexOf(hook) !== -1 && isFn(option[hook])) {
      removeHook(interceptor[hook], option[hook]);
    }
  });
}
function addInterceptor(method, option) {
  if (typeof method === 'string' && isPlainObject(option)) {
    mergeInterceptorHook(scopedInterceptors[method] || (scopedInterceptors[method] = {}), option);
  } else if (isPlainObject(method)) {
    mergeInterceptorHook(globalInterceptors, method);
  }
}
function removeInterceptor(method, option) {
  if (typeof method === 'string') {
    if (isPlainObject(option)) {
      removeInterceptorHook(scopedInterceptors[method], option);
    } else {
      delete scopedInterceptors[method];
    }
  } else if (isPlainObject(method)) {
    removeInterceptorHook(globalInterceptors, method);
  }
}
function wrapperHook(hook, params) {
  return function (data) {
    return hook(data, params) || data;
  };
}
function isPromise(obj) {
  return !!obj && ((0, _typeof2.default)(obj) === 'object' || typeof obj === 'function') && typeof obj.then === 'function';
}
function queue(hooks, data, params) {
  var promise = false;
  for (var i = 0; i < hooks.length; i++) {
    var hook = hooks[i];
    if (promise) {
      promise = Promise.resolve(wrapperHook(hook, params));
    } else {
      var res = hook(data, params);
      if (isPromise(res)) {
        promise = Promise.resolve(res);
      }
      if (res === false) {
        return {
          then: function then() {}
        };
      }
    }
  }
  return promise || {
    then: function then(callback) {
      return callback(data);
    }
  };
}
function wrapperOptions(interceptor) {
  var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
  ['success', 'fail', 'complete'].forEach(function (name) {
    if (Array.isArray(interceptor[name])) {
      var oldCallback = options[name];
      options[name] = function callbackInterceptor(res) {
        queue(interceptor[name], res, options).then(function (res) {
          /* eslint-disable no-mixed-operators */
          return isFn(oldCallback) && oldCallback(res) || res;
        });
      };
    }
  });
  return options;
}
function wrapperReturnValue(method, returnValue) {
  var returnValueHooks = [];
  if (Array.isArray(globalInterceptors.returnValue)) {
    returnValueHooks.push.apply(returnValueHooks, (0, _toConsumableArray2.default)(globalInterceptors.returnValue));
  }
  var interceptor = scopedInterceptors[method];
  if (interceptor && Array.isArray(interceptor.returnValue)) {
    returnValueHooks.push.apply(returnValueHooks, (0, _toConsumableArray2.default)(interceptor.returnValue));
  }
  returnValueHooks.forEach(function (hook) {
    returnValue = hook(returnValue) || returnValue;
  });
  return returnValue;
}
function getApiInterceptorHooks(method) {
  var interceptor = Object.create(null);
  Object.keys(globalInterceptors).forEach(function (hook) {
    if (hook !== 'returnValue') {
      interceptor[hook] = globalInterceptors[hook].slice();
    }
  });
  var scopedInterceptor = scopedInterceptors[method];
  if (scopedInterceptor) {
    Object.keys(scopedInterceptor).forEach(function (hook) {
      if (hook !== 'returnValue') {
        interceptor[hook] = (interceptor[hook] || []).concat(scopedInterceptor[hook]);
      }
    });
  }
  return interceptor;
}
function invokeApi(method, api, options) {
  for (var _len = arguments.length, params = new Array(_len > 3 ? _len - 3 : 0), _key = 3; _key < _len; _key++) {
    params[_key - 3] = arguments[_key];
  }
  var interceptor = getApiInterceptorHooks(method);
  if (interceptor && Object.keys(interceptor).length) {
    if (Array.isArray(interceptor.invoke)) {
      var res = queue(interceptor.invoke, options);
      return res.then(function (options) {
        // 重新访问 getApiInterceptorHooks, 允许 invoke 中再次调用 addInterceptor,removeInterceptor
        return api.apply(void 0, [wrapperOptions(getApiInterceptorHooks(method), options)].concat(params));
      });
    } else {
      return api.apply(void 0, [wrapperOptions(interceptor, options)].concat(params));
    }
  }
  return api.apply(void 0, [options].concat(params));
}
var promiseInterceptor = {
  returnValue: function returnValue(res) {
    if (!isPromise(res)) {
      return res;
    }
    return new Promise(function (resolve, reject) {
      res.then(function (res) {
        if (!res) {
          resolve(res);
          return;
        }
        if (res[0]) {
          reject(res[0]);
        } else {
          resolve(res[1]);
        }
      });
    });
  }
};
var SYNC_API_RE = /^\$|__f__|Window$|WindowStyle$|sendHostEvent|sendNativeEvent|restoreGlobal|requireGlobal|getCurrentSubNVue|getMenuButtonBoundingClientRect|^report|interceptors|Interceptor$|getSubNVueById|requireNativePlugin|rpx2px|upx2px|hideKeyboard|canIUse|^create|Sync$|Manager$|base64ToArrayBuffer|arrayBufferToBase64|getLocale|setLocale|invokePushCallback|getWindowInfo|getDeviceInfo|getAppBaseInfo|getSystemSetting|getAppAuthorizeSetting|initUTS|requireUTS|registerUTS/;
var CONTEXT_API_RE = /^create|Manager$/;

// Context例外情况
var CONTEXT_API_RE_EXC = ['createBLEConnection'];

// 同步例外情况
var ASYNC_API = ['createBLEConnection', 'createPushMessage'];
var CALLBACK_API_RE = /^on|^off/;
function isContextApi(name) {
  return CONTEXT_API_RE.test(name) && CONTEXT_API_RE_EXC.indexOf(name) === -1;
}
function isSyncApi(name) {
  return SYNC_API_RE.test(name) && ASYNC_API.indexOf(name) === -1;
}
function isCallbackApi(name) {
  return CALLBACK_API_RE.test(name) && name !== 'onPush';
}
function handlePromise(promise) {
  return promise.then(function (data) {
    return [null, data];
  }).catch(function (err) {
    return [err];
  });
}
function shouldPromise(name) {
  if (isContextApi(name) || isSyncApi(name) || isCallbackApi(name)) {
    return false;
  }
  return true;
}

/* eslint-disable no-extend-native */
if (!Promise.prototype.finally) {
  Promise.prototype.finally = function (callback) {
    var promise = this.constructor;
    return this.then(function (value) {
      return promise.resolve(callback()).then(function () {
        return value;
      });
    }, function (reason) {
      return promise.resolve(callback()).then(function () {
        throw reason;
      });
    });
  };
}
function promisify(name, api) {
  if (!shouldPromise(name) || !isFn(api)) {
    return api;
  }
  return function promiseApi() {
    var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    for (var _len2 = arguments.length, params = new Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
      params[_key2 - 1] = arguments[_key2];
    }
    if (isFn(options.success) || isFn(options.fail) || isFn(options.complete)) {
      return wrapperReturnValue(name, invokeApi.apply(void 0, [name, api, Object.assign({}, options)].concat(params)));
    }
    return wrapperReturnValue(name, handlePromise(new Promise(function (resolve, reject) {
      invokeApi.apply(void 0, [name, api, Object.assign({}, options, {
        success: resolve,
        fail: reject
      })].concat(params));
    })));
  };
}
var EPS = 1e-4;
var BASE_DEVICE_WIDTH = 750;
var isIOS = false;
var deviceWidth = 0;
var deviceDPR = 0;
function checkDeviceWidth() {
  var windowWidth, pixelRatio, platform;
  {
    var windowInfo = typeof wx.getWindowInfo === 'function' && wx.getWindowInfo() ? wx.getWindowInfo() : wx.getSystemInfoSync();
    var deviceInfo = typeof wx.getDeviceInfo === 'function' && wx.getDeviceInfo() ? wx.getDeviceInfo() : wx.getSystemInfoSync();
    windowWidth = windowInfo.windowWidth;
    pixelRatio = windowInfo.pixelRatio;
    platform = deviceInfo.platform;
  }
  deviceWidth = windowWidth;
  deviceDPR = pixelRatio;
  isIOS = platform === 'ios';
}
function upx2px(number, newDeviceWidth) {
  if (deviceWidth === 0) {
    checkDeviceWidth();
  }
  number = Number(number);
  if (number === 0) {
    return 0;
  }
  var result = number / BASE_DEVICE_WIDTH * (newDeviceWidth || deviceWidth);
  if (result < 0) {
    result = -result;
  }
  result = Math.floor(result + EPS);
  if (result === 0) {
    if (deviceDPR === 1 || !isIOS) {
      result = 1;
    } else {
      result = 0.5;
    }
  }
  return number < 0 ? -result : result;
}
var LOCALE_ZH_HANS = 'zh-Hans';
var LOCALE_ZH_HANT = 'zh-Hant';
var LOCALE_EN = 'en';
var LOCALE_FR = 'fr';
var LOCALE_ES = 'es';
var messages = {};
function getLocaleLanguage() {
  var localeLanguage = '';
  {
    var appBaseInfo = typeof wx.getAppBaseInfo === 'function' && wx.getAppBaseInfo() ? wx.getAppBaseInfo() : wx.getSystemInfoSync();
    var language = appBaseInfo && appBaseInfo.language ? appBaseInfo.language : LOCALE_EN;
    localeLanguage = normalizeLocale(language) || LOCALE_EN;
  }
  return localeLanguage;
}
var locale;
{
  locale = getLocaleLanguage();
}
function initI18nMessages() {
  if (!isEnableLocale()) {
    return;
  }
  var localeKeys = Object.keys(__uniConfig.locales);
  if (localeKeys.length) {
    localeKeys.forEach(function (locale) {
      var curMessages = messages[locale];
      var userMessages = __uniConfig.locales[locale];
      if (curMessages) {
        Object.assign(curMessages, userMessages);
      } else {
        messages[locale] = userMessages;
      }
    });
  }
}
initI18nMessages();
var i18n = (0, _uniI18n.initVueI18n)(locale, {});
var t = i18n.t;
var i18nMixin = i18n.mixin = {
  beforeCreate: function beforeCreate() {
    var _this = this;
    var unwatch = i18n.i18n.watchLocale(function () {
      _this.$forceUpdate();
    });
    this.$once('hook:beforeDestroy', function () {
      unwatch();
    });
  },
  methods: {
    $$t: function $$t(key, values) {
      return t(key, values);
    }
  }
};
var setLocale = i18n.setLocale;
var getLocale = i18n.getLocale;
function initAppLocale(Vue, appVm, locale) {
  var state = Vue.observable({
    locale: locale || i18n.getLocale()
  });
  var localeWatchers = [];
  appVm.$watchLocale = function (fn) {
    localeWatchers.push(fn);
  };
  Object.defineProperty(appVm, '$locale', {
    get: function get() {
      return state.locale;
    },
    set: function set(v) {
      state.locale = v;
      localeWatchers.forEach(function (watch) {
        return watch(v);
      });
    }
  });
}
function isEnableLocale() {
  return typeof __uniConfig !== 'undefined' && __uniConfig.locales && !!Object.keys(__uniConfig.locales).length;
}
function include(str, parts) {
  return !!parts.find(function (part) {
    return str.indexOf(part) !== -1;
  });
}
function startsWith(str, parts) {
  return parts.find(function (part) {
    return str.indexOf(part) === 0;
  });
}
function normalizeLocale(locale, messages) {
  if (!locale) {
    return;
  }
  locale = locale.trim().replace(/_/g, '-');
  if (messages && messages[locale]) {
    return locale;
  }
  locale = locale.toLowerCase();
  if (locale === 'chinese') {
    // 支付宝
    return LOCALE_ZH_HANS;
  }
  if (locale.indexOf('zh') === 0) {
    if (locale.indexOf('-hans') > -1) {
      return LOCALE_ZH_HANS;
    }
    if (locale.indexOf('-hant') > -1) {
      return LOCALE_ZH_HANT;
    }
    if (include(locale, ['-tw', '-hk', '-mo', '-cht'])) {
      return LOCALE_ZH_HANT;
    }
    return LOCALE_ZH_HANS;
  }
  var lang = startsWith(locale, [LOCALE_EN, LOCALE_FR, LOCALE_ES]);
  if (lang) {
    return lang;
  }
}
// export function initI18n() {
//   const localeKeys = Object.keys(__uniConfig.locales || {})
//   if (localeKeys.length) {
//     localeKeys.forEach((locale) =>
//       i18n.add(locale, __uniConfig.locales[locale])
//     )
//   }
// }

function getLocale$1() {
  // 优先使用 $locale
  if (isFn(getApp)) {
    var app = getApp({
      allowDefault: true
    });
    if (app && app.$vm) {
      return app.$vm.$locale;
    }
  }
  return getLocaleLanguage();
}
function setLocale$1(locale) {
  var app = isFn(getApp) ? getApp() : false;
  if (!app) {
    return false;
  }
  var oldLocale = app.$vm.$locale;
  if (oldLocale !== locale) {
    app.$vm.$locale = locale;
    onLocaleChangeCallbacks.forEach(function (fn) {
      return fn({
        locale: locale
      });
    });
    return true;
  }
  return false;
}
var onLocaleChangeCallbacks = [];
function onLocaleChange(fn) {
  if (onLocaleChangeCallbacks.indexOf(fn) === -1) {
    onLocaleChangeCallbacks.push(fn);
  }
}
if (typeof global !== 'undefined') {
  global.getLocale = getLocale$1;
}
var interceptors = {
  promiseInterceptor: promiseInterceptor
};
var baseApi = /*#__PURE__*/Object.freeze({
  __proto__: null,
  upx2px: upx2px,
  rpx2px: upx2px,
  getLocale: getLocale$1,
  setLocale: setLocale$1,
  onLocaleChange: onLocaleChange,
  addInterceptor: addInterceptor,
  removeInterceptor: removeInterceptor,
  interceptors: interceptors
});
function findExistsPageIndex(url) {
  var pages = getCurrentPages();
  var len = pages.length;
  while (len--) {
    var page = pages[len];
    if (page.$page && page.$page.fullPath === url) {
      return len;
    }
  }
  return -1;
}
var redirectTo = {
  name: function name(fromArgs) {
    if (fromArgs.exists === 'back' && fromArgs.delta) {
      return 'navigateBack';
    }
    return 'redirectTo';
  },
  args: function args(fromArgs) {
    if (fromArgs.exists === 'back' && fromArgs.url) {
      var existsPageIndex = findExistsPageIndex(fromArgs.url);
      if (existsPageIndex !== -1) {
        var delta = getCurrentPages().length - 1 - existsPageIndex;
        if (delta > 0) {
          fromArgs.delta = delta;
        }
      }
    }
  }
};
var previewImage = {
  args: function args(fromArgs) {
    var currentIndex = parseInt(fromArgs.current);
    if (isNaN(currentIndex)) {
      return;
    }
    var urls = fromArgs.urls;
    if (!Array.isArray(urls)) {
      return;
    }
    var len = urls.length;
    if (!len) {
      return;
    }
    if (currentIndex < 0) {
      currentIndex = 0;
    } else if (currentIndex >= len) {
      currentIndex = len - 1;
    }
    if (currentIndex > 0) {
      fromArgs.current = urls[currentIndex];
      fromArgs.urls = urls.filter(function (item, index) {
        return index < currentIndex ? item !== urls[currentIndex] : true;
      });
    } else {
      fromArgs.current = urls[0];
    }
    return {
      indicator: false,
      loop: false
    };
  }
};
var UUID_KEY = '__DC_STAT_UUID';
var deviceId;
function useDeviceId(result) {
  deviceId = deviceId || wx.getStorageSync(UUID_KEY);
  if (!deviceId) {
    deviceId = Date.now() + '' + Math.floor(Math.random() * 1e7);
    wx.setStorage({
      key: UUID_KEY,
      data: deviceId
    });
  }
  result.deviceId = deviceId;
}
function addSafeAreaInsets(result) {
  if (result.safeArea) {
    var safeArea = result.safeArea;
    result.safeAreaInsets = {
      top: safeArea.top,
      left: safeArea.left,
      right: result.windowWidth - safeArea.right,
      bottom: result.screenHeight - safeArea.bottom
    };
  }
}
function getOSInfo(system, platform) {
  var osName = '';
  var osVersion = '';
  if (platform && "mp-weixin" === 'mp-baidu') {
    osName = platform;
    osVersion = system;
  } else {
    osName = system.split(' ')[0] || platform;
    osVersion = system.split(' ')[1] || '';
  }
  osName = osName.toLocaleLowerCase();
  switch (osName) {
    case 'harmony': // alipay
    case 'ohos': // weixin
    case 'openharmony':
      // feishu
      osName = 'harmonyos';
      break;
    case 'iphone os':
      // alipay
      osName = 'ios';
      break;
    case 'mac': // weixin qq
    case 'darwin':
      // feishu
      osName = 'macos';
      break;
    case 'windows_nt':
      // feishu
      osName = 'windows';
      break;
  }
  return {
    osName: osName,
    osVersion: osVersion
  };
}
function populateParameters(result) {
  var _result$brand = result.brand,
    brand = _result$brand === void 0 ? '' : _result$brand,
    _result$model = result.model,
    model = _result$model === void 0 ? '' : _result$model,
    _result$system = result.system,
    system = _result$system === void 0 ? '' : _result$system,
    _result$language = result.language,
    language = _result$language === void 0 ? '' : _result$language,
    theme = result.theme,
    version = result.version,
    platform = result.platform,
    fontSizeSetting = result.fontSizeSetting,
    SDKVersion = result.SDKVersion,
    pixelRatio = result.pixelRatio,
    deviceOrientation = result.deviceOrientation;
  // const isQuickApp = "mp-weixin".indexOf('quickapp-webview') !== -1

  var extraParam = {};

  // osName osVersion
  var _getOSInfo = getOSInfo(system, platform),
    osName = _getOSInfo.osName,
    osVersion = _getOSInfo.osVersion;
  var hostVersion = version;

  // deviceType
  var deviceType = getGetDeviceType(result, model);

  // deviceModel
  var deviceBrand = getDeviceBrand(brand);

  // hostName
  var _hostName = getHostName(result);

  // deviceOrientation
  var _deviceOrientation = deviceOrientation; // 仅 微信 百度 支持

  // devicePixelRatio
  var _devicePixelRatio = pixelRatio;

  // SDKVersion
  var _SDKVersion = SDKVersion;

  // hostLanguage
  var hostLanguage = (language || '').replace(/_/g, '-');

  // wx.getAccountInfoSync

  var parameters = {
    appId: "__UNI__12345678",
    appName: "宠物之家",
    appVersion: "1.0.0",
    appVersionCode: "100",
    appLanguage: getAppLanguage(hostLanguage),
    uniCompileVersion: "4.76",
    uniCompilerVersion: "4.76",
    uniRuntimeVersion: "4.76",
    uniPlatform: undefined || "mp-weixin",
    deviceBrand: deviceBrand,
    deviceModel: model,
    deviceType: deviceType,
    devicePixelRatio: _devicePixelRatio,
    deviceOrientation: _deviceOrientation,
    osName: osName.toLocaleLowerCase(),
    osVersion: osVersion,
    hostTheme: theme,
    hostVersion: hostVersion,
    hostLanguage: hostLanguage,
    hostName: _hostName,
    hostSDKVersion: _SDKVersion,
    hostFontSizeSetting: fontSizeSetting,
    windowTop: 0,
    windowBottom: 0,
    // TODO
    osLanguage: undefined,
    osTheme: undefined,
    ua: undefined,
    hostPackageName: undefined,
    browserName: undefined,
    browserVersion: undefined,
    isUniAppX: false
  };
  Object.assign(result, parameters, extraParam);
}
function getGetDeviceType(result, model) {
  var deviceType = result.deviceType || 'phone';
  {
    var deviceTypeMaps = {
      ipad: 'pad',
      windows: 'pc',
      mac: 'pc'
    };
    var deviceTypeMapsKeys = Object.keys(deviceTypeMaps);
    var _model = model.toLocaleLowerCase();
    for (var index = 0; index < deviceTypeMapsKeys.length; index++) {
      var _m = deviceTypeMapsKeys[index];
      if (_model.indexOf(_m) !== -1) {
        deviceType = deviceTypeMaps[_m];
        break;
      }
    }
  }
  return deviceType;
}
function getDeviceBrand(brand) {
  var deviceBrand = brand;
  if (deviceBrand) {
    deviceBrand = brand.toLocaleLowerCase();
  }
  return deviceBrand;
}
function getAppLanguage(defaultLanguage) {
  return getLocale$1 ? getLocale$1() : defaultLanguage;
}
function getHostName(result) {
  var _platform = 'WeChat';
  var _hostName = result.hostName || _platform; // mp-jd
  {
    if (result.environment) {
      _hostName = result.environment;
    } else if (result.host && result.host.env) {
      _hostName = result.host.env;
    }
  }
  return _hostName;
}
var getSystemInfo = {
  returnValue: function returnValue(result) {
    useDeviceId(result);
    addSafeAreaInsets(result);
    populateParameters(result);
  }
};
var showActionSheet = {
  args: function args(fromArgs) {
    if ((0, _typeof2.default)(fromArgs) === 'object') {
      fromArgs.alertText = fromArgs.title;
    }
  }
};
var getAppBaseInfo = {
  returnValue: function returnValue(result) {
    var _result = result,
      version = _result.version,
      language = _result.language,
      SDKVersion = _result.SDKVersion,
      theme = _result.theme;
    var _hostName = getHostName(result);
    var hostLanguage = (language || '').replace('_', '-');
    result = sortObject(Object.assign(result, {
      appId: "__UNI__12345678",
      appName: "宠物之家",
      appVersion: "1.0.0",
      appVersionCode: "100",
      appLanguage: getAppLanguage(hostLanguage),
      hostVersion: version,
      hostLanguage: hostLanguage,
      hostName: _hostName,
      hostSDKVersion: SDKVersion,
      hostTheme: theme,
      isUniAppX: false,
      uniPlatform: undefined || "mp-weixin",
      uniCompileVersion: "4.76",
      uniCompilerVersion: "4.76",
      uniRuntimeVersion: "4.76"
    }));
  }
};
var getDeviceInfo = {
  returnValue: function returnValue(result) {
    var _result2 = result,
      brand = _result2.brand,
      model = _result2.model,
      _result2$system = _result2.system,
      system = _result2$system === void 0 ? '' : _result2$system,
      _result2$platform = _result2.platform,
      platform = _result2$platform === void 0 ? '' : _result2$platform;
    var deviceType = getGetDeviceType(result, model);
    var deviceBrand = getDeviceBrand(brand);
    useDeviceId(result);
    var _getOSInfo2 = getOSInfo(system, platform),
      osName = _getOSInfo2.osName,
      osVersion = _getOSInfo2.osVersion;
    result = sortObject(Object.assign(result, {
      deviceType: deviceType,
      deviceBrand: deviceBrand,
      deviceModel: model,
      osName: osName,
      osVersion: osVersion
    }));
  }
};
var getWindowInfo = {
  returnValue: function returnValue(result) {
    addSafeAreaInsets(result);
    result = sortObject(Object.assign(result, {
      windowTop: 0,
      windowBottom: 0
    }));
  }
};
var getAppAuthorizeSetting = {
  returnValue: function returnValue(result) {
    var locationReducedAccuracy = result.locationReducedAccuracy;
    result.locationAccuracy = 'unsupported';
    if (locationReducedAccuracy === true) {
      result.locationAccuracy = 'reduced';
    } else if (locationReducedAccuracy === false) {
      result.locationAccuracy = 'full';
    }
  }
};

// import navigateTo from 'uni-helpers/navigate-to'

var compressImage = {
  args: function args(fromArgs) {
    // https://developers.weixin.qq.com/community/develop/doc/000c08940c865011298e0a43256800?highLine=compressHeight
    if (fromArgs.compressedHeight && !fromArgs.compressHeight) {
      fromArgs.compressHeight = fromArgs.compressedHeight;
    }
    if (fromArgs.compressedWidth && !fromArgs.compressWidth) {
      fromArgs.compressWidth = fromArgs.compressedWidth;
    }
  }
};
var protocols = {
  redirectTo: redirectTo,
  // navigateTo,  // 由于在微信开发者工具的页面参数，会显示__id__参数，因此暂时关闭mp-weixin对于navigateTo的AOP
  previewImage: previewImage,
  getSystemInfo: getSystemInfo,
  getSystemInfoSync: getSystemInfo,
  showActionSheet: showActionSheet,
  getAppBaseInfo: getAppBaseInfo,
  getDeviceInfo: getDeviceInfo,
  getWindowInfo: getWindowInfo,
  getAppAuthorizeSetting: getAppAuthorizeSetting,
  compressImage: compressImage
};
var todos = ['vibrate', 'preloadPage', 'unPreloadPage', 'loadSubPackage'];
var canIUses = [];
var CALLBACKS = ['success', 'fail', 'cancel', 'complete'];
function processCallback(methodName, method, returnValue) {
  return function (res) {
    return method(processReturnValue(methodName, res, returnValue));
  };
}
function processArgs(methodName, fromArgs) {
  var argsOption = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var returnValue = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};
  var keepFromArgs = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : false;
  if (isPlainObject(fromArgs)) {
    // 一般 api 的参数解析
    var toArgs = keepFromArgs === true ? fromArgs : {}; // returnValue 为 false 时，说明是格式化返回值，直接在返回值对象上修改赋值
    if (isFn(argsOption)) {
      argsOption = argsOption(fromArgs, toArgs) || {};
    }
    for (var key in fromArgs) {
      if (hasOwn(argsOption, key)) {
        var keyOption = argsOption[key];
        if (isFn(keyOption)) {
          keyOption = keyOption(fromArgs[key], fromArgs, toArgs);
        }
        if (!keyOption) {
          // 不支持的参数
          console.warn("The '".concat(methodName, "' method of platform '\u5FAE\u4FE1\u5C0F\u7A0B\u5E8F' does not support option '").concat(key, "'"));
        } else if (isStr(keyOption)) {
          // 重写参数 key
          toArgs[keyOption] = fromArgs[key];
        } else if (isPlainObject(keyOption)) {
          // {name:newName,value:value}可重新指定参数 key:value
          toArgs[keyOption.name ? keyOption.name : key] = keyOption.value;
        }
      } else if (CALLBACKS.indexOf(key) !== -1) {
        if (isFn(fromArgs[key])) {
          toArgs[key] = processCallback(methodName, fromArgs[key], returnValue);
        }
      } else {
        if (!keepFromArgs) {
          toArgs[key] = fromArgs[key];
        }
      }
    }
    return toArgs;
  } else if (isFn(fromArgs)) {
    fromArgs = processCallback(methodName, fromArgs, returnValue);
  }
  return fromArgs;
}
function processReturnValue(methodName, res, returnValue) {
  var keepReturnValue = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : false;
  if (isFn(protocols.returnValue)) {
    // 处理通用 returnValue
    res = protocols.returnValue(methodName, res);
  }
  return processArgs(methodName, res, returnValue, {}, keepReturnValue);
}
function wrapper(methodName, method) {
  if (hasOwn(protocols, methodName)) {
    var protocol = protocols[methodName];
    if (!protocol) {
      // 暂不支持的 api
      return function () {
        console.error("Platform '\u5FAE\u4FE1\u5C0F\u7A0B\u5E8F' does not support '".concat(methodName, "'."));
      };
    }
    return function (arg1, arg2) {
      // 目前 api 最多两个参数
      var options = protocol;
      if (isFn(protocol)) {
        options = protocol(arg1);
      }
      arg1 = processArgs(methodName, arg1, options.args, options.returnValue);
      var args = [arg1];
      if (typeof arg2 !== 'undefined') {
        args.push(arg2);
      }
      if (isFn(options.name)) {
        methodName = options.name(arg1);
      } else if (isStr(options.name)) {
        methodName = options.name;
      }
      var returnValue = wx[methodName].apply(wx, args);
      if (isSyncApi(methodName)) {
        // 同步 api
        return processReturnValue(methodName, returnValue, options.returnValue, isContextApi(methodName));
      }
      return returnValue;
    };
  }
  return method;
}
var todoApis = Object.create(null);
var TODOS = ['onTabBarMidButtonTap', 'subscribePush', 'unsubscribePush', 'onPush', 'offPush', 'share'];
function createTodoApi(name) {
  return function todoApi(_ref) {
    var fail = _ref.fail,
      complete = _ref.complete;
    var res = {
      errMsg: "".concat(name, ":fail method '").concat(name, "' not supported")
    };
    isFn(fail) && fail(res);
    isFn(complete) && complete(res);
  };
}
TODOS.forEach(function (name) {
  todoApis[name] = createTodoApi(name);
});
var providers = {
  oauth: ['weixin'],
  share: ['weixin'],
  payment: ['wxpay'],
  push: ['weixin']
};
function getProvider(_ref2) {
  var service = _ref2.service,
    success = _ref2.success,
    fail = _ref2.fail,
    complete = _ref2.complete;
  var res = false;
  if (providers[service]) {
    res = {
      errMsg: 'getProvider:ok',
      service: service,
      provider: providers[service]
    };
    isFn(success) && success(res);
  } else {
    res = {
      errMsg: 'getProvider:fail service not found'
    };
    isFn(fail) && fail(res);
  }
  isFn(complete) && complete(res);
}
var extraApi = /*#__PURE__*/Object.freeze({
  __proto__: null,
  getProvider: getProvider
});
var getEmitter = function () {
  var Emitter;
  return function getUniEmitter() {
    if (!Emitter) {
      Emitter = new _vue.default();
    }
    return Emitter;
  };
}();
function apply(ctx, method, args) {
  return ctx[method].apply(ctx, args);
}
function $on() {
  return apply(getEmitter(), '$on', Array.prototype.slice.call(arguments));
}
function $off() {
  return apply(getEmitter(), '$off', Array.prototype.slice.call(arguments));
}
function $once() {
  return apply(getEmitter(), '$once', Array.prototype.slice.call(arguments));
}
function $emit() {
  return apply(getEmitter(), '$emit', Array.prototype.slice.call(arguments));
}
var eventApi = /*#__PURE__*/Object.freeze({
  __proto__: null,
  $on: $on,
  $off: $off,
  $once: $once,
  $emit: $emit
});

/**
 * 框架内 try-catch
 */
/**
 * 开发者 try-catch
 */
function tryCatch(fn) {
  return function () {
    try {
      return fn.apply(fn, arguments);
    } catch (e) {
      // TODO
      console.error(e);
    }
  };
}
function getApiCallbacks(params) {
  var apiCallbacks = {};
  for (var name in params) {
    var param = params[name];
    if (isFn(param)) {
      apiCallbacks[name] = tryCatch(param);
      delete params[name];
    }
  }
  return apiCallbacks;
}
var cid;
var cidErrMsg;
var enabled;
function normalizePushMessage(message) {
  try {
    return JSON.parse(message);
  } catch (e) {}
  return message;
}
function invokePushCallback(args) {
  if (args.type === 'enabled') {
    enabled = true;
  } else if (args.type === 'clientId') {
    cid = args.cid;
    cidErrMsg = args.errMsg;
    invokeGetPushCidCallbacks(cid, args.errMsg);
  } else if (args.type === 'pushMsg') {
    var message = {
      type: 'receive',
      data: normalizePushMessage(args.message)
    };
    for (var i = 0; i < onPushMessageCallbacks.length; i++) {
      var callback = onPushMessageCallbacks[i];
      callback(message);
      // 该消息已被阻止
      if (message.stopped) {
        break;
      }
    }
  } else if (args.type === 'click') {
    onPushMessageCallbacks.forEach(function (callback) {
      callback({
        type: 'click',
        data: normalizePushMessage(args.message)
      });
    });
  }
}
var getPushCidCallbacks = [];
function invokeGetPushCidCallbacks(cid, errMsg) {
  getPushCidCallbacks.forEach(function (callback) {
    callback(cid, errMsg);
  });
  getPushCidCallbacks.length = 0;
}
function getPushClientId(args) {
  if (!isPlainObject(args)) {
    args = {};
  }
  var _getApiCallbacks = getApiCallbacks(args),
    success = _getApiCallbacks.success,
    fail = _getApiCallbacks.fail,
    complete = _getApiCallbacks.complete;
  var hasSuccess = isFn(success);
  var hasFail = isFn(fail);
  var hasComplete = isFn(complete);
  Promise.resolve().then(function () {
    if (typeof enabled === 'undefined') {
      enabled = false;
      cid = '';
      cidErrMsg = 'uniPush is not enabled';
    }
    getPushCidCallbacks.push(function (cid, errMsg) {
      var res;
      if (cid) {
        res = {
          errMsg: 'getPushClientId:ok',
          cid: cid
        };
        hasSuccess && success(res);
      } else {
        res = {
          errMsg: 'getPushClientId:fail' + (errMsg ? ' ' + errMsg : '')
        };
        hasFail && fail(res);
      }
      hasComplete && complete(res);
    });
    if (typeof cid !== 'undefined') {
      invokeGetPushCidCallbacks(cid, cidErrMsg);
    }
  });
}
var onPushMessageCallbacks = [];
// 不使用 defineOnApi 实现，是因为 defineOnApi 依赖 UniServiceJSBridge ，该对象目前在小程序上未提供，故简单实现
var onPushMessage = function onPushMessage(fn) {
  if (onPushMessageCallbacks.indexOf(fn) === -1) {
    onPushMessageCallbacks.push(fn);
  }
};
var offPushMessage = function offPushMessage(fn) {
  if (!fn) {
    onPushMessageCallbacks.length = 0;
  } else {
    var index = onPushMessageCallbacks.indexOf(fn);
    if (index > -1) {
      onPushMessageCallbacks.splice(index, 1);
    }
  }
};
function __f__(type) {
  for (var _len3 = arguments.length, args = new Array(_len3 > 1 ? _len3 - 1 : 0), _key3 = 1; _key3 < _len3; _key3++) {
    args[_key3 - 1] = arguments[_key3];
  }
  console[type].apply(console, args);
}
var baseInfo = wx.getAppBaseInfo && wx.getAppBaseInfo();
if (!baseInfo) {
  baseInfo = wx.getSystemInfoSync();
}
var host = baseInfo ? baseInfo.host : null;
var shareVideoMessage = host && host.env === 'SAAASDK' ? wx.miniapp.shareVideoMessage : wx.shareVideoMessage;
var api = /*#__PURE__*/Object.freeze({
  __proto__: null,
  shareVideoMessage: shareVideoMessage,
  getPushClientId: getPushClientId,
  onPushMessage: onPushMessage,
  offPushMessage: offPushMessage,
  invokePushCallback: invokePushCallback,
  __f__: __f__
});
var mocks = ['__route__', '__wxExparserNodeId__', '__wxWebviewId__'];
function findVmByVueId(vm, vuePid) {
  var $children = vm.$children;
  // 优先查找直属(反向查找:https://github.com/dcloudio/uni-app/issues/1200)
  for (var i = $children.length - 1; i >= 0; i--) {
    var childVm = $children[i];
    if (childVm.$scope._$vueId === vuePid) {
      return childVm;
    }
  }
  // 反向递归查找
  var parentVm;
  for (var _i = $children.length - 1; _i >= 0; _i--) {
    parentVm = findVmByVueId($children[_i], vuePid);
    if (parentVm) {
      return parentVm;
    }
  }
}
function initBehavior(options) {
  return Behavior(options);
}
function isPage() {
  return !!this.route;
}
function initRelation(detail) {
  this.triggerEvent('__l', detail);
}
function selectAllComponents(mpInstance, selector, $refs) {
  var components = mpInstance.selectAllComponents(selector) || [];
  components.forEach(function (component) {
    var ref = component.dataset.ref;
    $refs[ref] = component.$vm || toSkip(component);
    {
      if (component.dataset.vueGeneric === 'scoped') {
        component.selectAllComponents('.scoped-ref').forEach(function (scopedComponent) {
          selectAllComponents(scopedComponent, selector, $refs);
        });
      }
    }
  });
}
function syncRefs(refs, newRefs) {
  var oldKeys = (0, _construct2.default)(Set, (0, _toConsumableArray2.default)(Object.keys(refs)));
  var newKeys = Object.keys(newRefs);
  newKeys.forEach(function (key) {
    var oldValue = refs[key];
    var newValue = newRefs[key];
    if (Array.isArray(oldValue) && Array.isArray(newValue) && oldValue.length === newValue.length && newValue.every(function (value) {
      return oldValue.includes(value);
    })) {
      return;
    }
    refs[key] = newValue;
    oldKeys.delete(key);
  });
  oldKeys.forEach(function (key) {
    delete refs[key];
  });
  return refs;
}
function initRefs(vm) {
  var mpInstance = vm.$scope;
  var refs = {};
  Object.defineProperty(vm, '$refs', {
    get: function get() {
      var $refs = {};
      selectAllComponents(mpInstance, '.vue-ref', $refs);
      // TODO 暂不考虑 for 中的 scoped
      var forComponents = mpInstance.selectAllComponents('.vue-ref-in-for') || [];
      forComponents.forEach(function (component) {
        var ref = component.dataset.ref;
        if (!$refs[ref]) {
          $refs[ref] = [];
        }
        $refs[ref].push(component.$vm || toSkip(component));
      });
      return syncRefs(refs, $refs);
    }
  });
}
function handleLink(event) {
  var _ref3 = event.detail || event.value,
    vuePid = _ref3.vuePid,
    vueOptions = _ref3.vueOptions; // detail 是微信,value 是百度(dipatch)

  var parentVm;
  if (vuePid) {
    parentVm = findVmByVueId(this.$vm, vuePid);
  }
  if (!parentVm) {
    parentVm = this.$vm;
  }
  vueOptions.parent = parentVm;
}
function markMPComponent(component) {
  // 在 Vue 中标记为小程序组件
  var IS_MP = '__v_isMPComponent';
  Object.defineProperty(component, IS_MP, {
    configurable: true,
    enumerable: false,
    value: true
  });
  return component;
}
function toSkip(obj) {
  var OB = '__ob__';
  var SKIP = '__v_skip';
  if (isObject(obj) && Object.isExtensible(obj)) {
    // 避免被 @vue/composition-api 观测
    Object.defineProperty(obj, OB, {
      configurable: true,
      enumerable: false,
      value: (0, _defineProperty2.default)({}, SKIP, true)
    });
  }
  return obj;
}
var WORKLET_RE = /_(.*)_worklet_factory_/;
function initWorkletMethods(mpMethods, vueMethods) {
  if (vueMethods) {
    Object.keys(vueMethods).forEach(function (name) {
      var matches = name.match(WORKLET_RE);
      if (matches) {
        var workletName = matches[1];
        mpMethods[name] = vueMethods[name];
        mpMethods[workletName] = vueMethods[workletName];
      }
    });
  }
}
var MPPage = Page;
var MPComponent = Component;
var customizeRE = /:/g;
var customize = cached(function (str) {
  return camelize(str.replace(customizeRE, '-'));
});
function initTriggerEvent(mpInstance) {
  var oldTriggerEvent = mpInstance.triggerEvent;
  var newTriggerEvent = function newTriggerEvent(event) {
    for (var _len4 = arguments.length, args = new Array(_len4 > 1 ? _len4 - 1 : 0), _key4 = 1; _key4 < _len4; _key4++) {
      args[_key4 - 1] = arguments[_key4];
    }
    // 事件名统一转驼峰格式，仅处理：当前组件为 vue 组件、当前组件为 vue 组件子组件
    if (this.$vm || this.dataset && this.dataset.comType) {
      event = customize(event);
    } else {
      // 针对微信/QQ小程序单独补充驼峰格式事件，以兼容历史项目
      var newEvent = customize(event);
      if (newEvent !== event) {
        oldTriggerEvent.apply(this, [newEvent].concat(args));
      }
    }
    return oldTriggerEvent.apply(this, [event].concat(args));
  };
  try {
    // 京东小程序 triggerEvent 为只读
    mpInstance.triggerEvent = newTriggerEvent;
  } catch (error) {
    mpInstance._triggerEvent = newTriggerEvent;
  }
}
function initHook(name, options, isComponent) {
  var oldHook = options[name];
  options[name] = function () {
    markMPComponent(this);
    initTriggerEvent(this);
    if (oldHook) {
      for (var _len5 = arguments.length, args = new Array(_len5), _key5 = 0; _key5 < _len5; _key5++) {
        args[_key5] = arguments[_key5];
      }
      return oldHook.apply(this, args);
    }
  };
}
if (!MPPage.__$wrappered) {
  MPPage.__$wrappered = true;
  Page = function Page() {
    var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    initHook('onLoad', options);
    return MPPage(options);
  };
  Page.after = MPPage.after;
  Component = function Component() {
    var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    initHook('created', options);
    return MPComponent(options);
  };
}
var PAGE_EVENT_HOOKS = ['onPullDownRefresh', 'onReachBottom', 'onAddToFavorites', 'onShareTimeline', 'onShareAppMessage', 'onPageScroll', 'onResize', 'onTabItemTap'];
function initMocks(vm, mocks) {
  var mpInstance = vm.$mp[vm.mpType];
  mocks.forEach(function (mock) {
    if (hasOwn(mpInstance, mock)) {
      vm[mock] = mpInstance[mock];
    }
  });
}
function hasHook(hook, vueOptions) {
  if (!vueOptions) {
    return true;
  }
  if (_vue.default.options && Array.isArray(_vue.default.options[hook])) {
    return true;
  }
  vueOptions = vueOptions.default || vueOptions;
  if (isFn(vueOptions)) {
    if (isFn(vueOptions.extendOptions[hook])) {
      return true;
    }
    if (vueOptions.super && vueOptions.super.options && Array.isArray(vueOptions.super.options[hook])) {
      return true;
    }
    return false;
  }
  if (isFn(vueOptions[hook]) || Array.isArray(vueOptions[hook])) {
    return true;
  }
  var mixins = vueOptions.mixins;
  if (Array.isArray(mixins)) {
    return !!mixins.find(function (mixin) {
      return hasHook(hook, mixin);
    });
  }
}
function initHooks(mpOptions, hooks, vueOptions) {
  hooks.forEach(function (hook) {
    if (hasHook(hook, vueOptions)) {
      mpOptions[hook] = function (args) {
        return this.$vm && this.$vm.__call_hook(hook, args);
      };
    }
  });
}
function initUnknownHooks(mpOptions, vueOptions) {
  var excludes = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : [];
  findHooks(vueOptions).forEach(function (hook) {
    return initHook$1(mpOptions, hook, excludes);
  });
}
function findHooks(vueOptions) {
  var hooks = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
  if (vueOptions) {
    Object.keys(vueOptions).forEach(function (name) {
      if (name.indexOf('on') === 0 && isFn(vueOptions[name])) {
        hooks.push(name);
      }
    });
  }
  return hooks;
}
function initHook$1(mpOptions, hook, excludes) {
  if (excludes.indexOf(hook) === -1 && !hasOwn(mpOptions, hook)) {
    mpOptions[hook] = function (args) {
      return this.$vm && this.$vm.__call_hook(hook, args);
    };
  }
}
function initVueComponent(Vue, vueOptions) {
  vueOptions = vueOptions.default || vueOptions;
  var VueComponent;
  if (isFn(vueOptions)) {
    VueComponent = vueOptions;
  } else {
    VueComponent = Vue.extend(vueOptions);
  }
  vueOptions = VueComponent.options;
  return [VueComponent, vueOptions];
}
function initSlots(vm, vueSlots) {
  if (Array.isArray(vueSlots) && vueSlots.length) {
    var $slots = Object.create(null);
    vueSlots.forEach(function (slotName) {
      $slots[slotName] = true;
    });
    vm.$scopedSlots = vm.$slots = $slots;
  }
}
function initVueIds(vueIds, mpInstance) {
  vueIds = (vueIds || '').split(',');
  var len = vueIds.length;
  if (len === 1) {
    mpInstance._$vueId = vueIds[0];
  } else if (len === 2) {
    mpInstance._$vueId = vueIds[0];
    mpInstance._$vuePid = vueIds[1];
  }
}
function initData(vueOptions, context) {
  var data = vueOptions.data || {};
  var methods = vueOptions.methods || {};
  if (typeof data === 'function') {
    try {
      data = data.call(context); // 支持 Vue.prototype 上挂的数据
    } catch (e) {
      if (Object({"NODE_ENV":"development","VUE_APP_DARK_MODE":"false","VUE_APP_NAME":"宠物之家","VUE_APP_PLATFORM":"mp-weixin","BASE_URL":"/"}).VUE_APP_DEBUG) {
        console.warn('根据 Vue 的 data 函数初始化小程序 data 失败，请尽量确保 data 函数中不访问 vm 对象，否则可能影响首次数据渲染速度。', data);
      }
    }
  } else {
    try {
      // 对 data 格式化
      data = JSON.parse(JSON.stringify(data));
    } catch (e) {}
  }
  if (!isPlainObject(data)) {
    data = {};
  }
  Object.keys(methods).forEach(function (methodName) {
    if (context.__lifecycle_hooks__.indexOf(methodName) === -1 && !hasOwn(data, methodName)) {
      data[methodName] = methods[methodName];
    }
  });
  return data;
}
var PROP_TYPES = [String, Number, Boolean, Object, Array, null];
function createObserver(name) {
  return function observer(newVal, oldVal) {
    if (this.$vm) {
      this.$vm[name] = newVal; // 为了触发其他非 render watcher
    }
  };
}

function initBehaviors(vueOptions, initBehavior) {
  var vueBehaviors = vueOptions.behaviors;
  var vueExtends = vueOptions.extends;
  var vueMixins = vueOptions.mixins;
  var vueProps = vueOptions.props;
  if (!vueProps) {
    vueOptions.props = vueProps = [];
  }
  var behaviors = [];
  if (Array.isArray(vueBehaviors)) {
    vueBehaviors.forEach(function (behavior) {
      behaviors.push(behavior.replace('uni://', "wx".concat("://")));
      if (behavior === 'uni://form-field') {
        if (Array.isArray(vueProps)) {
          vueProps.push('name');
          vueProps.push('value');
        } else {
          vueProps.name = {
            type: String,
            default: ''
          };
          vueProps.value = {
            type: [String, Number, Boolean, Array, Object, Date],
            default: ''
          };
        }
      }
    });
  }
  if (isPlainObject(vueExtends) && vueExtends.props) {
    behaviors.push(initBehavior({
      properties: initProperties(vueExtends.props, true)
    }));
  }
  if (Array.isArray(vueMixins)) {
    vueMixins.forEach(function (vueMixin) {
      if (isPlainObject(vueMixin) && vueMixin.props) {
        behaviors.push(initBehavior({
          properties: initProperties(vueMixin.props, true)
        }));
      }
    });
  }
  return behaviors;
}
function parsePropType(key, type, defaultValue, file) {
  // [String]=>String
  if (Array.isArray(type) && type.length === 1) {
    return type[0];
  }
  return type;
}
function initProperties(props) {
  var isBehavior = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
  var file = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : '';
  var options = arguments.length > 3 ? arguments[3] : undefined;
  var properties = {};
  if (!isBehavior) {
    properties.vueId = {
      type: String,
      value: ''
    };
    {
      if (options.virtualHost) {
        properties.virtualHostStyle = {
          type: null,
          value: ''
        };
        properties.virtualHostClass = {
          type: null,
          value: ''
        };
      }
    }
    // scopedSlotsCompiler auto
    properties.scopedSlotsCompiler = {
      type: String,
      value: ''
    };
    properties.vueSlots = {
      // 小程序不能直接定义 $slots 的 props，所以通过 vueSlots 转换到 $slots
      type: null,
      value: [],
      observer: function observer(newVal, oldVal) {
        var $slots = Object.create(null);
        newVal.forEach(function (slotName) {
          $slots[slotName] = true;
        });
        this.setData({
          $slots: $slots
        });
      }
    };
  }
  if (Array.isArray(props)) {
    // ['title']
    props.forEach(function (key) {
      properties[key] = {
        type: null,
        observer: createObserver(key)
      };
    });
  } else if (isPlainObject(props)) {
    // {title:{type:String,default:''},content:String}
    Object.keys(props).forEach(function (key) {
      var opts = props[key];
      if (isPlainObject(opts)) {
        // title:{type:String,default:''}
        var value = opts.default;
        if (isFn(value)) {
          value = value();
        }
        opts.type = parsePropType(key, opts.type);
        properties[key] = {
          type: PROP_TYPES.indexOf(opts.type) !== -1 ? opts.type : null,
          value: value,
          observer: createObserver(key)
        };
      } else {
        // content:String
        var type = parsePropType(key, opts);
        properties[key] = {
          type: PROP_TYPES.indexOf(type) !== -1 ? type : null,
          observer: createObserver(key)
        };
      }
    });
  }
  return properties;
}
function wrapper$1(event) {
  // TODO 又得兼容 mpvue 的 mp 对象
  try {
    event.mp = JSON.parse(JSON.stringify(event));
  } catch (e) {}
  event.stopPropagation = noop;
  event.preventDefault = noop;
  event.target = event.target || {};
  if (!hasOwn(event, 'detail')) {
    event.detail = {};
  }
  if (hasOwn(event, 'markerId')) {
    event.detail = (0, _typeof2.default)(event.detail) === 'object' ? event.detail : {};
    event.detail.markerId = event.markerId;
  }
  if (isPlainObject(event.detail)) {
    event.target = Object.assign({}, event.target, event.detail);
  }
  return event;
}
function getExtraValue(vm, dataPathsArray) {
  var context = vm;
  dataPathsArray.forEach(function (dataPathArray) {
    var dataPath = dataPathArray[0];
    var value = dataPathArray[2];
    if (dataPath || typeof value !== 'undefined') {
      // ['','',index,'disable']
      var propPath = dataPathArray[1];
      var valuePath = dataPathArray[3];
      var vFor;
      if (Number.isInteger(dataPath)) {
        vFor = dataPath;
      } else if (!dataPath) {
        vFor = context;
      } else if (typeof dataPath === 'string' && dataPath) {
        if (dataPath.indexOf('#s#') === 0) {
          vFor = dataPath.substr(3);
        } else {
          vFor = vm.__get_value(dataPath, context);
        }
      }
      if (Number.isInteger(vFor)) {
        context = value;
      } else if (!propPath) {
        context = vFor[value];
      } else {
        if (Array.isArray(vFor)) {
          context = vFor.find(function (vForItem) {
            return vm.__get_value(propPath, vForItem) === value;
          });
        } else if (isPlainObject(vFor)) {
          context = Object.keys(vFor).find(function (vForKey) {
            return vm.__get_value(propPath, vFor[vForKey]) === value;
          });
        } else {
          console.error('v-for 暂不支持循环数据：', vFor);
        }
      }
      if (valuePath) {
        context = vm.__get_value(valuePath, context);
      }
    }
  });
  return context;
}
function processEventExtra(vm, extra, event, __args__) {
  var extraObj = {};
  if (Array.isArray(extra) && extra.length) {
    /**
     *[
     *    ['data.items', 'data.id', item.data.id],
     *    ['metas', 'id', meta.id]
     *],
     *[
     *    ['data.items', 'data.id', item.data.id],
     *    ['metas', 'id', meta.id]
     *],
     *'test'
     */
    extra.forEach(function (dataPath, index) {
      if (typeof dataPath === 'string') {
        if (!dataPath) {
          // model,prop.sync
          extraObj['$' + index] = vm;
        } else {
          if (dataPath === '$event') {
            // $event
            extraObj['$' + index] = event;
          } else if (dataPath === 'arguments') {
            extraObj['$' + index] = event.detail ? event.detail.__args__ || __args__ : __args__;
          } else if (dataPath.indexOf('$event.') === 0) {
            // $event.target.value
            extraObj['$' + index] = vm.__get_value(dataPath.replace('$event.', ''), event);
          } else {
            extraObj['$' + index] = vm.__get_value(dataPath);
          }
        }
      } else {
        extraObj['$' + index] = getExtraValue(vm, dataPath);
      }
    });
  }
  return extraObj;
}
function getObjByArray(arr) {
  var obj = {};
  for (var i = 1; i < arr.length; i++) {
    var element = arr[i];
    obj[element[0]] = element[1];
  }
  return obj;
}
function processEventArgs(vm, event) {
  var args = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : [];
  var extra = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : [];
  var isCustom = arguments.length > 4 ? arguments[4] : undefined;
  var methodName = arguments.length > 5 ? arguments[5] : undefined;
  var isCustomMPEvent = false; // wxcomponent 组件，传递原始 event 对象

  // fixed 用户直接触发 mpInstance.triggerEvent
  var __args__ = isPlainObject(event.detail) ? event.detail.__args__ || [event.detail] : [event.detail];
  if (isCustom) {
    // 自定义事件
    isCustomMPEvent = event.currentTarget && event.currentTarget.dataset && event.currentTarget.dataset.comType === 'wx';
    if (!args.length) {
      // 无参数，直接传入 event 或 detail 数组
      if (isCustomMPEvent) {
        return [event];
      }
      return __args__;
    }
  }
  var extraObj = processEventExtra(vm, extra, event, __args__);
  var ret = [];
  args.forEach(function (arg) {
    if (arg === '$event') {
      if (methodName === '__set_model' && !isCustom) {
        // input v-model value
        ret.push(event.target.value);
      } else {
        if (isCustom && !isCustomMPEvent) {
          ret.push(__args__[0]);
        } else {
          // wxcomponent 组件或内置组件
          ret.push(event);
        }
      }
    } else {
      if (Array.isArray(arg) && arg[0] === 'o') {
        ret.push(getObjByArray(arg));
      } else if (typeof arg === 'string' && hasOwn(extraObj, arg)) {
        ret.push(extraObj[arg]);
      } else {
        ret.push(arg);
      }
    }
  });
  return ret;
}
var ONCE = '~';
var CUSTOM = '^';
function isMatchEventType(eventType, optType) {
  return eventType === optType || optType === 'regionchange' && (eventType === 'begin' || eventType === 'end');
}
function getContextVm(vm) {
  var $parent = vm.$parent;
  // 父组件是 scoped slots 或者其他自定义组件时继续查找
  while ($parent && $parent.$parent && ($parent.$options.generic || $parent.$parent.$options.generic || $parent.$scope._$vuePid)) {
    $parent = $parent.$parent;
  }
  return $parent && $parent.$parent;
}
function handleEvent(event) {
  var _this2 = this;
  event = wrapper$1(event);

  // [['tap',[['handle',[1,2,a]],['handle1',[1,2,a]]]]]
  var dataset = (event.currentTarget || event.target).dataset;
  if (!dataset) {
    return console.warn('事件信息不存在');
  }
  var eventOpts = dataset.eventOpts || dataset['event-opts']; // 支付宝 web-view 组件 dataset 非驼峰
  if (!eventOpts) {
    return console.warn('事件信息不存在');
  }

  // [['handle',[1,2,a]],['handle1',[1,2,a]]]
  var eventType = event.type;
  var ret = [];
  eventOpts.forEach(function (eventOpt) {
    var type = eventOpt[0];
    var eventsArray = eventOpt[1];
    var isCustom = type.charAt(0) === CUSTOM;
    type = isCustom ? type.slice(1) : type;
    var isOnce = type.charAt(0) === ONCE;
    type = isOnce ? type.slice(1) : type;
    if (eventsArray && isMatchEventType(eventType, type)) {
      eventsArray.forEach(function (eventArray) {
        var methodName = eventArray[0];
        if (methodName) {
          var handlerCtx = _this2.$vm;
          if (handlerCtx.$options.generic) {
            // mp-weixin,mp-toutiao 抽象节点模拟 scoped slots
            handlerCtx = getContextVm(handlerCtx) || handlerCtx;
          }
          if (methodName === '$emit') {
            handlerCtx.$emit.apply(handlerCtx, processEventArgs(_this2.$vm, event, eventArray[1], eventArray[2], isCustom, methodName));
            return;
          }
          var handler = handlerCtx[methodName];
          if (!isFn(handler)) {
            var _type = _this2.$vm.mpType === 'page' ? 'Page' : 'Component';
            var path = _this2.route || _this2.is;
            throw new Error("".concat(_type, " \"").concat(path, "\" does not have a method \"").concat(methodName, "\""));
          }
          if (isOnce) {
            if (handler.once) {
              return;
            }
            handler.once = true;
          }
          var params = processEventArgs(_this2.$vm, event, eventArray[1], eventArray[2], isCustom, methodName);
          params = Array.isArray(params) ? params : [];
          // 参数尾部增加原始事件对象用于复杂表达式内获取额外数据
          if (/=\s*\S+\.eventParams\s*\|\|\s*\S+\[['"]event-params['"]\]/.test(handler.toString())) {
            // eslint-disable-next-line no-sparse-arrays
            params = params.concat([,,,,,,,,,, event]);
          }
          ret.push(handler.apply(handlerCtx, params));
        }
      });
    }
  });
  if (eventType === 'input' && ret.length === 1 && typeof ret[0] !== 'undefined') {
    return ret[0];
  }
}
var eventChannels = {};
function getEventChannel(id) {
  var eventChannel = eventChannels[id];
  delete eventChannels[id];
  return eventChannel;
}
var hooks = ['onShow', 'onHide', 'onError', 'onPageNotFound', 'onThemeChange', 'onUnhandledRejection'];
function initEventChannel() {
  _vue.default.prototype.getOpenerEventChannel = function () {
    // 微信小程序使用自身getOpenerEventChannel
    {
      return this.$scope.getOpenerEventChannel();
    }
  };
  var callHook = _vue.default.prototype.__call_hook;
  _vue.default.prototype.__call_hook = function (hook, args) {
    if (hook === 'onLoad' && args && args.__id__) {
      this.__eventChannel__ = getEventChannel(args.__id__);
      delete args.__id__;
    }
    return callHook.call(this, hook, args);
  };
}
function initScopedSlotsParams() {
  var center = {};
  var parents = {};
  function currentId(fn) {
    var vueIds = this.$options.propsData.vueId;
    if (vueIds) {
      var vueId = vueIds.split(',')[0];
      fn(vueId);
    }
  }
  _vue.default.prototype.$hasSSP = function (vueId) {
    var slot = center[vueId];
    if (!slot) {
      parents[vueId] = this;
      this.$on('hook:destroyed', function () {
        delete parents[vueId];
      });
    }
    return slot;
  };
  _vue.default.prototype.$getSSP = function (vueId, name, needAll) {
    var slot = center[vueId];
    if (slot) {
      var params = slot[name] || [];
      if (needAll) {
        return params;
      }
      return params[0];
    }
  };
  _vue.default.prototype.$setSSP = function (name, value) {
    var index = 0;
    currentId.call(this, function (vueId) {
      var slot = center[vueId];
      var params = slot[name] = slot[name] || [];
      params.push(value);
      index = params.length - 1;
    });
    return index;
  };
  _vue.default.prototype.$initSSP = function () {
    currentId.call(this, function (vueId) {
      center[vueId] = {};
    });
  };
  _vue.default.prototype.$callSSP = function () {
    currentId.call(this, function (vueId) {
      if (parents[vueId]) {
        parents[vueId].$forceUpdate();
      }
    });
  };
  _vue.default.mixin({
    destroyed: function destroyed() {
      var propsData = this.$options.propsData;
      var vueId = propsData && propsData.vueId;
      if (vueId) {
        delete center[vueId];
        delete parents[vueId];
      }
    }
  });
}
function parseBaseApp(vm, _ref4) {
  var mocks = _ref4.mocks,
    initRefs = _ref4.initRefs;
  initEventChannel();
  {
    initScopedSlotsParams();
  }
  if (vm.$options.store) {
    _vue.default.prototype.$store = vm.$options.store;
  }
  uniIdMixin(_vue.default);
  _vue.default.prototype.mpHost = "mp-weixin";
  _vue.default.mixin({
    beforeCreate: function beforeCreate() {
      if (!this.$options.mpType) {
        return;
      }
      this.mpType = this.$options.mpType;
      this.$mp = (0, _defineProperty2.default)({
        data: {}
      }, this.mpType, this.$options.mpInstance);
      this.$scope = this.$options.mpInstance;
      delete this.$options.mpType;
      delete this.$options.mpInstance;
      if (this.mpType === 'page' && typeof getApp === 'function') {
        // hack vue-i18n
        var app = getApp();
        if (app.$vm && app.$vm.$i18n) {
          this._i18n = app.$vm.$i18n;
        }
      }
      if (this.mpType !== 'app') {
        initRefs(this);
        initMocks(this, mocks);
      }
    }
  });
  var appOptions = {
    onLaunch: function onLaunch(args) {
      if (this.$vm) {
        // 已经初始化过了，主要是为了百度，百度 onShow 在 onLaunch 之前
        return;
      }
      {
        if (wx.canIUse && !wx.canIUse('nextTick')) {
          // 事实 上2.2.3 即可，简单使用 2.3.0 的 nextTick 判断
          console.error('当前微信基础库版本过低，请将 微信开发者工具-详情-项目设置-调试基础库版本 更换为`2.3.0`以上');
        }
      }
      this.$vm = vm;
      this.$vm.$mp = {
        app: this
      };
      this.$vm.$scope = this;
      // vm 上也挂载 globalData
      this.$vm.globalData = this.globalData;
      this.$vm._isMounted = true;
      this.$vm.__call_hook('mounted', args);
      this.$vm.__call_hook('onLaunch', args);
    }
  };

  // 兼容旧版本 globalData
  appOptions.globalData = vm.$options.globalData || {};
  // 将 methods 中的方法挂在 getApp() 中
  var methods = vm.$options.methods;
  if (methods) {
    Object.keys(methods).forEach(function (name) {
      appOptions[name] = methods[name];
    });
  }
  initAppLocale(_vue.default, vm, getLocaleLanguage$1());
  initHooks(appOptions, hooks);
  initUnknownHooks(appOptions, vm.$options);
  return appOptions;
}
function getLocaleLanguage$1() {
  var localeLanguage = '';
  {
    var appBaseInfo = wx.getAppBaseInfo();
    var language = appBaseInfo && appBaseInfo.language ? appBaseInfo.language : LOCALE_EN;
    localeLanguage = normalizeLocale(language) || LOCALE_EN;
  }
  return localeLanguage;
}
function parseApp(vm) {
  return parseBaseApp(vm, {
    mocks: mocks,
    initRefs: initRefs
  });
}
function createApp(vm) {
  App(parseApp(vm));
  return vm;
}
var encodeReserveRE = /[!'()*]/g;
var encodeReserveReplacer = function encodeReserveReplacer(c) {
  return '%' + c.charCodeAt(0).toString(16);
};
var commaRE = /%2C/g;

// fixed encodeURIComponent which is more conformant to RFC3986:
// - escapes [!'()*]
// - preserve commas
var encode = function encode(str) {
  return encodeURIComponent(str).replace(encodeReserveRE, encodeReserveReplacer).replace(commaRE, ',');
};
function stringifyQuery(obj) {
  var encodeStr = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : encode;
  var res = obj ? Object.keys(obj).map(function (key) {
    var val = obj[key];
    if (val === undefined) {
      return '';
    }
    if (val === null) {
      return encodeStr(key);
    }
    if (Array.isArray(val)) {
      var result = [];
      val.forEach(function (val2) {
        if (val2 === undefined) {
          return;
        }
        if (val2 === null) {
          result.push(encodeStr(key));
        } else {
          result.push(encodeStr(key) + '=' + encodeStr(val2));
        }
      });
      return result.join('&');
    }
    return encodeStr(key) + '=' + encodeStr(val);
  }).filter(function (x) {
    return x.length > 0;
  }).join('&') : null;
  return res ? "?".concat(res) : '';
}
function parseBaseComponent(vueComponentOptions) {
  var _ref5 = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {},
    isPage = _ref5.isPage,
    initRelation = _ref5.initRelation;
  var needVueOptions = arguments.length > 2 ? arguments[2] : undefined;
  var _initVueComponent = initVueComponent(_vue.default, vueComponentOptions),
    _initVueComponent2 = (0, _slicedToArray2.default)(_initVueComponent, 2),
    VueComponent = _initVueComponent2[0],
    vueOptions = _initVueComponent2[1];
  var options = _objectSpread({
    multipleSlots: true,
    // styleIsolation: 'apply-shared',
    addGlobalClass: true
  }, vueOptions.options || {});
  {
    // 微信 multipleSlots 部分情况有 bug，导致内容顺序错乱 如 u-list，提供覆盖选项
    if (vueOptions['mp-weixin'] && vueOptions['mp-weixin'].options) {
      Object.assign(options, vueOptions['mp-weixin'].options);
    }
  }
  var componentOptions = {
    options: options,
    data: initData(vueOptions, _vue.default.prototype),
    behaviors: initBehaviors(vueOptions, initBehavior),
    properties: initProperties(vueOptions.props, false, vueOptions.__file, options),
    lifetimes: {
      attached: function attached() {
        var properties = this.properties;
        var options = {
          mpType: isPage.call(this) ? 'page' : 'component',
          mpInstance: this,
          propsData: properties
        };
        initVueIds(properties.vueId, this);

        // 处理父子关系
        initRelation.call(this, {
          vuePid: this._$vuePid,
          vueOptions: options
        });

        // 初始化 vue 实例
        this.$vm = new VueComponent(options);

        // 处理$slots,$scopedSlots（暂不支持动态变化$slots）
        initSlots(this.$vm, properties.vueSlots);

        // 触发首次 setData
        this.$vm.$mount();
      },
      ready: function ready() {
        // 当组件 props 默认值为 true，初始化时传入 false 会导致 created,ready 触发, 但 attached 不触发
        // https://developers.weixin.qq.com/community/develop/doc/00066ae2844cc0f8eb883e2a557800
        if (this.$vm) {
          this.$vm._isMounted = true;
          this.$vm.__call_hook('mounted');
          this.$vm.__call_hook('onReady');
        }
      },
      detached: function detached() {
        this.$vm && this.$vm.$destroy();
      }
    },
    pageLifetimes: {
      show: function show(args) {
        this.$vm && this.$vm.__call_hook('onPageShow', args);
      },
      hide: function hide() {
        this.$vm && this.$vm.__call_hook('onPageHide');
      },
      resize: function resize(size) {
        this.$vm && this.$vm.__call_hook('onPageResize', size);
      }
    },
    methods: {
      __l: handleLink,
      __e: handleEvent
    }
  };
  // externalClasses
  if (vueOptions.externalClasses) {
    componentOptions.externalClasses = vueOptions.externalClasses;
  }
  if (Array.isArray(vueOptions.wxsCallMethods)) {
    vueOptions.wxsCallMethods.forEach(function (callMethod) {
      componentOptions.methods[callMethod] = function (args) {
        return this.$vm[callMethod](args);
      };
    });
  }
  if (needVueOptions) {
    return [componentOptions, vueOptions, VueComponent];
  }
  if (isPage) {
    return componentOptions;
  }
  return [componentOptions, VueComponent];
}
function parseComponent(vueComponentOptions, needVueOptions) {
  return parseBaseComponent(vueComponentOptions, {
    isPage: isPage,
    initRelation: initRelation
  }, needVueOptions);
}
var hooks$1 = ['onShow', 'onHide', 'onUnload'];
hooks$1.push.apply(hooks$1, PAGE_EVENT_HOOKS);
function parseBasePage(vuePageOptions) {
  var _parseComponent = parseComponent(vuePageOptions, true),
    _parseComponent2 = (0, _slicedToArray2.default)(_parseComponent, 2),
    pageOptions = _parseComponent2[0],
    vueOptions = _parseComponent2[1];
  initHooks(pageOptions.methods, hooks$1, vueOptions);
  pageOptions.methods.onLoad = function (query) {
    this.options = query;
    var copyQuery = Object.assign({}, query);
    delete copyQuery.__id__;
    this.$page = {
      fullPath: '/' + (this.route || this.is) + stringifyQuery(copyQuery)
    };
    this.$vm.$mp.query = query; // 兼容 mpvue
    this.$vm.__call_hook('onLoad', query);
  };
  {
    initUnknownHooks(pageOptions.methods, vuePageOptions, ['onReady']);
  }
  {
    initWorkletMethods(pageOptions.methods, vueOptions.methods);
  }
  return pageOptions;
}
function parsePage(vuePageOptions) {
  return parseBasePage(vuePageOptions);
}
function createPage(vuePageOptions) {
  {
    return Component(parsePage(vuePageOptions));
  }
}
function createComponent(vueOptions) {
  {
    return Component(parseComponent(vueOptions));
  }
}
function createSubpackageApp(vm) {
  var appOptions = parseApp(vm);
  var app = getApp({
    allowDefault: true
  });
  vm.$scope = app;
  var globalData = app.globalData;
  if (globalData) {
    Object.keys(appOptions.globalData).forEach(function (name) {
      if (!hasOwn(globalData, name)) {
        globalData[name] = appOptions.globalData[name];
      }
    });
  }
  Object.keys(appOptions).forEach(function (name) {
    if (!hasOwn(app, name)) {
      app[name] = appOptions[name];
    }
  });
  if (isFn(appOptions.onShow) && wx.onAppShow) {
    wx.onAppShow(function () {
      for (var _len6 = arguments.length, args = new Array(_len6), _key6 = 0; _key6 < _len6; _key6++) {
        args[_key6] = arguments[_key6];
      }
      vm.__call_hook('onShow', args);
    });
  }
  if (isFn(appOptions.onHide) && wx.onAppHide) {
    wx.onAppHide(function () {
      for (var _len7 = arguments.length, args = new Array(_len7), _key7 = 0; _key7 < _len7; _key7++) {
        args[_key7] = arguments[_key7];
      }
      vm.__call_hook('onHide', args);
    });
  }
  if (isFn(appOptions.onLaunch)) {
    var args = wx.getLaunchOptionsSync && wx.getLaunchOptionsSync();
    vm.__call_hook('onLaunch', args);
  }
  return vm;
}
function createPlugin(vm) {
  var appOptions = parseApp(vm);
  if (isFn(appOptions.onShow) && wx.onAppShow) {
    wx.onAppShow(function () {
      for (var _len8 = arguments.length, args = new Array(_len8), _key8 = 0; _key8 < _len8; _key8++) {
        args[_key8] = arguments[_key8];
      }
      vm.__call_hook('onShow', args);
    });
  }
  if (isFn(appOptions.onHide) && wx.onAppHide) {
    wx.onAppHide(function () {
      for (var _len9 = arguments.length, args = new Array(_len9), _key9 = 0; _key9 < _len9; _key9++) {
        args[_key9] = arguments[_key9];
      }
      vm.__call_hook('onHide', args);
    });
  }
  if (isFn(appOptions.onLaunch)) {
    var args = wx.getLaunchOptionsSync && wx.getLaunchOptionsSync();
    vm.__call_hook('onLaunch', args);
  }
  return vm;
}
todos.forEach(function (todoApi) {
  protocols[todoApi] = false;
});
canIUses.forEach(function (canIUseApi) {
  var apiName = protocols[canIUseApi] && protocols[canIUseApi].name ? protocols[canIUseApi].name : canIUseApi;
  if (!wx.canIUse(apiName)) {
    protocols[canIUseApi] = false;
  }
});
var uni = {};
if (typeof Proxy !== 'undefined' && "mp-weixin" !== 'app-plus') {
  uni = new Proxy({}, {
    get: function get(target, name) {
      if (hasOwn(target, name)) {
        return target[name];
      }
      if (baseApi[name]) {
        return baseApi[name];
      }
      if (api[name]) {
        return promisify(name, api[name]);
      }
      {
        if (extraApi[name]) {
          return promisify(name, extraApi[name]);
        }
        if (todoApis[name]) {
          return promisify(name, todoApis[name]);
        }
      }
      if (eventApi[name]) {
        return eventApi[name];
      }
      return promisify(name, wrapper(name, wx[name]));
    },
    set: function set(target, name, value) {
      target[name] = value;
      return true;
    }
  });
} else {
  Object.keys(baseApi).forEach(function (name) {
    uni[name] = baseApi[name];
  });
  {
    Object.keys(todoApis).forEach(function (name) {
      uni[name] = promisify(name, todoApis[name]);
    });
    Object.keys(extraApi).forEach(function (name) {
      uni[name] = promisify(name, extraApi[name]);
    });
  }
  Object.keys(eventApi).forEach(function (name) {
    uni[name] = eventApi[name];
  });
  Object.keys(api).forEach(function (name) {
    uni[name] = promisify(name, api[name]);
  });
  Object.keys(wx).forEach(function (name) {
    if (hasOwn(wx, name) || hasOwn(protocols, name)) {
      uni[name] = promisify(name, wrapper(name, wx[name]));
    }
  });
}
wx.createApp = createApp;
wx.createPage = createPage;
wx.createComponent = createComponent;
wx.createSubpackageApp = createSubpackageApp;
wx.createPlugin = createPlugin;
var uni$1 = uni;
var _default = uni$1;
exports.default = _default;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)["default"], __webpack_require__(/*! ./../../../webpack/buildin/global.js */ 3)))

/***/ }),

/***/ 20:
/*!****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/iterableToArray.js ***!
  \****************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _iterableToArray(iter) {
  if (typeof Symbol !== "undefined" && iter[Symbol.iterator] != null || iter["@@iterator"] != null) return Array.from(iter);
}
module.exports = _iterableToArray, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 21:
/*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/nonIterableSpread.js ***!
  \******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _nonIterableSpread() {
  throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}
module.exports = _nonIterableSpread, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 22:
/*!*************************************************************!*\
  !*** ./node_modules/@dcloudio/uni-i18n/dist/uni-i18n.es.js ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni, global) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.LOCALE_ZH_HANT = exports.LOCALE_ZH_HANS = exports.LOCALE_FR = exports.LOCALE_ES = exports.LOCALE_EN = exports.I18n = exports.Formatter = void 0;
exports.compileI18nJsonStr = compileI18nJsonStr;
exports.hasI18nJson = hasI18nJson;
exports.initVueI18n = initVueI18n;
exports.isI18nStr = isI18nStr;
exports.isString = void 0;
exports.normalizeLocale = normalizeLocale;
exports.parseI18nJson = parseI18nJson;
exports.resolveLocale = resolveLocale;
var _slicedToArray2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/slicedToArray */ 5));
var _classCallCheck2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/classCallCheck */ 23));
var _createClass2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/createClass */ 24));
var _typeof2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/typeof */ 13));
var isObject = function isObject(val) {
  return val !== null && (0, _typeof2.default)(val) === 'object';
};
var defaultDelimiters = ['{', '}'];
var BaseFormatter = /*#__PURE__*/function () {
  function BaseFormatter() {
    (0, _classCallCheck2.default)(this, BaseFormatter);
    this._caches = Object.create(null);
  }
  (0, _createClass2.default)(BaseFormatter, [{
    key: "interpolate",
    value: function interpolate(message, values) {
      var delimiters = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : defaultDelimiters;
      if (!values) {
        return [message];
      }
      var tokens = this._caches[message];
      if (!tokens) {
        tokens = parse(message, delimiters);
        this._caches[message] = tokens;
      }
      return compile(tokens, values);
    }
  }]);
  return BaseFormatter;
}();
exports.Formatter = BaseFormatter;
var RE_TOKEN_LIST_VALUE = /^(?:\d)+/;
var RE_TOKEN_NAMED_VALUE = /^(?:\w)+/;
function parse(format, _ref) {
  var _ref2 = (0, _slicedToArray2.default)(_ref, 2),
    startDelimiter = _ref2[0],
    endDelimiter = _ref2[1];
  var tokens = [];
  var position = 0;
  var text = '';
  while (position < format.length) {
    var char = format[position++];
    if (char === startDelimiter) {
      if (text) {
        tokens.push({
          type: 'text',
          value: text
        });
      }
      text = '';
      var sub = '';
      char = format[position++];
      while (char !== undefined && char !== endDelimiter) {
        sub += char;
        char = format[position++];
      }
      var isClosed = char === endDelimiter;
      var type = RE_TOKEN_LIST_VALUE.test(sub) ? 'list' : isClosed && RE_TOKEN_NAMED_VALUE.test(sub) ? 'named' : 'unknown';
      tokens.push({
        value: sub,
        type: type
      });
    }
    //  else if (char === '%') {
    //   // when found rails i18n syntax, skip text capture
    //   if (format[position] !== '{') {
    //     text += char
    //   }
    // }
    else {
      text += char;
    }
  }
  text && tokens.push({
    type: 'text',
    value: text
  });
  return tokens;
}
function compile(tokens, values) {
  var compiled = [];
  var index = 0;
  var mode = Array.isArray(values) ? 'list' : isObject(values) ? 'named' : 'unknown';
  if (mode === 'unknown') {
    return compiled;
  }
  while (index < tokens.length) {
    var token = tokens[index];
    switch (token.type) {
      case 'text':
        compiled.push(token.value);
        break;
      case 'list':
        compiled.push(values[parseInt(token.value, 10)]);
        break;
      case 'named':
        if (mode === 'named') {
          compiled.push(values[token.value]);
        } else {
          if (true) {
            console.warn("Type of token '".concat(token.type, "' and format of value '").concat(mode, "' don't match!"));
          }
        }
        break;
      case 'unknown':
        if (true) {
          console.warn("Detect 'unknown' type of token!");
        }
        break;
    }
    index++;
  }
  return compiled;
}
var LOCALE_ZH_HANS = 'zh-Hans';
exports.LOCALE_ZH_HANS = LOCALE_ZH_HANS;
var LOCALE_ZH_HANT = 'zh-Hant';
exports.LOCALE_ZH_HANT = LOCALE_ZH_HANT;
var LOCALE_EN = 'en';
exports.LOCALE_EN = LOCALE_EN;
var LOCALE_FR = 'fr';
exports.LOCALE_FR = LOCALE_FR;
var LOCALE_ES = 'es';
exports.LOCALE_ES = LOCALE_ES;
var hasOwnProperty = Object.prototype.hasOwnProperty;
var hasOwn = function hasOwn(val, key) {
  return hasOwnProperty.call(val, key);
};
var defaultFormatter = new BaseFormatter();
function include(str, parts) {
  return !!parts.find(function (part) {
    return str.indexOf(part) !== -1;
  });
}
function startsWith(str, parts) {
  return parts.find(function (part) {
    return str.indexOf(part) === 0;
  });
}
function normalizeLocale(locale, messages) {
  if (!locale) {
    return;
  }
  locale = locale.trim().replace(/_/g, '-');
  if (messages && messages[locale]) {
    return locale;
  }
  locale = locale.toLowerCase();
  if (locale === 'chinese') {
    // 支付宝
    return LOCALE_ZH_HANS;
  }
  if (locale.indexOf('zh') === 0) {
    if (locale.indexOf('-hans') > -1) {
      return LOCALE_ZH_HANS;
    }
    if (locale.indexOf('-hant') > -1) {
      return LOCALE_ZH_HANT;
    }
    if (include(locale, ['-tw', '-hk', '-mo', '-cht'])) {
      return LOCALE_ZH_HANT;
    }
    return LOCALE_ZH_HANS;
  }
  var locales = [LOCALE_EN, LOCALE_FR, LOCALE_ES];
  if (messages && Object.keys(messages).length > 0) {
    locales = Object.keys(messages);
  }
  var lang = startsWith(locale, locales);
  if (lang) {
    return lang;
  }
}
var I18n = /*#__PURE__*/function () {
  function I18n(_ref3) {
    var locale = _ref3.locale,
      fallbackLocale = _ref3.fallbackLocale,
      messages = _ref3.messages,
      watcher = _ref3.watcher,
      formater = _ref3.formater;
    (0, _classCallCheck2.default)(this, I18n);
    this.locale = LOCALE_EN;
    this.fallbackLocale = LOCALE_EN;
    this.message = {};
    this.messages = {};
    this.watchers = [];
    if (fallbackLocale) {
      this.fallbackLocale = fallbackLocale;
    }
    this.formater = formater || defaultFormatter;
    this.messages = messages || {};
    this.setLocale(locale || LOCALE_EN);
    if (watcher) {
      this.watchLocale(watcher);
    }
  }
  (0, _createClass2.default)(I18n, [{
    key: "setLocale",
    value: function setLocale(locale) {
      var _this = this;
      var oldLocale = this.locale;
      this.locale = normalizeLocale(locale, this.messages) || this.fallbackLocale;
      if (!this.messages[this.locale]) {
        // 可能初始化时不存在
        this.messages[this.locale] = {};
      }
      this.message = this.messages[this.locale];
      // 仅发生变化时，通知
      if (oldLocale !== this.locale) {
        this.watchers.forEach(function (watcher) {
          watcher(_this.locale, oldLocale);
        });
      }
    }
  }, {
    key: "getLocale",
    value: function getLocale() {
      return this.locale;
    }
  }, {
    key: "watchLocale",
    value: function watchLocale(fn) {
      var _this2 = this;
      var index = this.watchers.push(fn) - 1;
      return function () {
        _this2.watchers.splice(index, 1);
      };
    }
  }, {
    key: "add",
    value: function add(locale, message) {
      var override = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      var curMessages = this.messages[locale];
      if (curMessages) {
        if (override) {
          Object.assign(curMessages, message);
        } else {
          Object.keys(message).forEach(function (key) {
            if (!hasOwn(curMessages, key)) {
              curMessages[key] = message[key];
            }
          });
        }
      } else {
        this.messages[locale] = message;
      }
    }
  }, {
    key: "f",
    value: function f(message, values, delimiters) {
      return this.formater.interpolate(message, values, delimiters).join('');
    }
  }, {
    key: "t",
    value: function t(key, locale, values) {
      var message = this.message;
      if (typeof locale === 'string') {
        locale = normalizeLocale(locale, this.messages);
        locale && (message = this.messages[locale]);
      } else {
        values = locale;
      }
      if (!hasOwn(message, key)) {
        console.warn("Cannot translate the value of keypath ".concat(key, ". Use the value of keypath as default."));
        return key;
      }
      return this.formater.interpolate(message[key], values).join('');
    }
  }]);
  return I18n;
}();
exports.I18n = I18n;
function watchAppLocale(appVm, i18n) {
  // 需要保证 watch 的触发在组件渲染之前
  if (appVm.$watchLocale) {
    // vue2
    appVm.$watchLocale(function (newLocale) {
      i18n.setLocale(newLocale);
    });
  } else {
    appVm.$watch(function () {
      return appVm.$locale;
    }, function (newLocale) {
      i18n.setLocale(newLocale);
    });
  }
}
function getDefaultLocale() {
  if (typeof uni !== 'undefined' && uni.getLocale) {
    return uni.getLocale();
  }
  // 小程序平台，uni 和 uni-i18n 互相引用，导致访问不到 uni，故在 global 上挂了 getLocale
  if (typeof global !== 'undefined' && global.getLocale) {
    return global.getLocale();
  }
  return LOCALE_EN;
}
function initVueI18n(locale) {
  var messages = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
  var fallbackLocale = arguments.length > 2 ? arguments[2] : undefined;
  var watcher = arguments.length > 3 ? arguments[3] : undefined;
  // 兼容旧版本入参
  if (typeof locale !== 'string') {
    var _ref4 = [messages, locale];
    locale = _ref4[0];
    messages = _ref4[1];
  }
  if (typeof locale !== 'string') {
    // 因为小程序平台，uni-i18n 和 uni 互相引用，导致此时访问 uni 时，为 undefined
    locale = getDefaultLocale();
  }
  if (typeof fallbackLocale !== 'string') {
    fallbackLocale = typeof __uniConfig !== 'undefined' && __uniConfig.fallbackLocale || LOCALE_EN;
  }
  var i18n = new I18n({
    locale: locale,
    fallbackLocale: fallbackLocale,
    messages: messages,
    watcher: watcher
  });
  var _t = function t(key, values) {
    if (typeof getApp !== 'function') {
      // app view
      /* eslint-disable no-func-assign */
      _t = function t(key, values) {
        return i18n.t(key, values);
      };
    } else {
      var isWatchedAppLocale = false;
      _t = function t(key, values) {
        var appVm = getApp().$vm;
        // 可能$vm还不存在，比如在支付宝小程序中，组件定义较早，在props的default里使用了t()函数（如uni-goods-nav），此时app还未初始化
        // options: {
        // 	type: Array,
        // 	default () {
        // 		return [{
        // 			icon: 'shop',
        // 			text: t("uni-goods-nav.options.shop"),
        // 		}, {
        // 			icon: 'cart',
        // 			text: t("uni-goods-nav.options.cart")
        // 		}]
        // 	}
        // },
        if (appVm) {
          // 触发响应式
          appVm.$locale;
          if (!isWatchedAppLocale) {
            isWatchedAppLocale = true;
            watchAppLocale(appVm, i18n);
          }
        }
        return i18n.t(key, values);
      };
    }
    return _t(key, values);
  };
  return {
    i18n: i18n,
    f: function f(message, values, delimiters) {
      return i18n.f(message, values, delimiters);
    },
    t: function t(key, values) {
      return _t(key, values);
    },
    add: function add(locale, message) {
      var override = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      return i18n.add(locale, message, override);
    },
    watch: function watch(fn) {
      return i18n.watchLocale(fn);
    },
    getLocale: function getLocale() {
      return i18n.getLocale();
    },
    setLocale: function setLocale(newLocale) {
      return i18n.setLocale(newLocale);
    }
  };
}
var isString = function isString(val) {
  return typeof val === 'string';
};
exports.isString = isString;
var formater;
function hasI18nJson(jsonObj, delimiters) {
  if (!formater) {
    formater = new BaseFormatter();
  }
  return walkJsonObj(jsonObj, function (jsonObj, key) {
    var value = jsonObj[key];
    if (isString(value)) {
      if (isI18nStr(value, delimiters)) {
        return true;
      }
    } else {
      return hasI18nJson(value, delimiters);
    }
  });
}
function parseI18nJson(jsonObj, values, delimiters) {
  if (!formater) {
    formater = new BaseFormatter();
  }
  walkJsonObj(jsonObj, function (jsonObj, key) {
    var value = jsonObj[key];
    if (isString(value)) {
      if (isI18nStr(value, delimiters)) {
        jsonObj[key] = compileStr(value, values, delimiters);
      }
    } else {
      parseI18nJson(value, values, delimiters);
    }
  });
  return jsonObj;
}
function compileI18nJsonStr(jsonStr, _ref5) {
  var locale = _ref5.locale,
    locales = _ref5.locales,
    delimiters = _ref5.delimiters;
  if (!isI18nStr(jsonStr, delimiters)) {
    return jsonStr;
  }
  if (!formater) {
    formater = new BaseFormatter();
  }
  var localeValues = [];
  Object.keys(locales).forEach(function (name) {
    if (name !== locale) {
      localeValues.push({
        locale: name,
        values: locales[name]
      });
    }
  });
  localeValues.unshift({
    locale: locale,
    values: locales[locale]
  });
  try {
    return JSON.stringify(compileJsonObj(JSON.parse(jsonStr), localeValues, delimiters), null, 2);
  } catch (e) {}
  return jsonStr;
}
function isI18nStr(value, delimiters) {
  return value.indexOf(delimiters[0]) > -1;
}
function compileStr(value, values, delimiters) {
  return formater.interpolate(value, values, delimiters).join('');
}
function compileValue(jsonObj, key, localeValues, delimiters) {
  var value = jsonObj[key];
  if (isString(value)) {
    // 存在国际化
    if (isI18nStr(value, delimiters)) {
      jsonObj[key] = compileStr(value, localeValues[0].values, delimiters);
      if (localeValues.length > 1) {
        // 格式化国际化语言
        var valueLocales = jsonObj[key + 'Locales'] = {};
        localeValues.forEach(function (localValue) {
          valueLocales[localValue.locale] = compileStr(value, localValue.values, delimiters);
        });
      }
    }
  } else {
    compileJsonObj(value, localeValues, delimiters);
  }
}
function compileJsonObj(jsonObj, localeValues, delimiters) {
  walkJsonObj(jsonObj, function (jsonObj, key) {
    compileValue(jsonObj, key, localeValues, delimiters);
  });
  return jsonObj;
}
function walkJsonObj(jsonObj, walk) {
  if (Array.isArray(jsonObj)) {
    for (var i = 0; i < jsonObj.length; i++) {
      if (walk(jsonObj, i)) {
        return true;
      }
    }
  } else if (isObject(jsonObj)) {
    for (var key in jsonObj) {
      if (walk(jsonObj, key)) {
        return true;
      }
    }
  }
  return false;
}
function resolveLocale(locales) {
  return function (locale) {
    if (!locale) {
      return locale;
    }
    locale = normalizeLocale(locale) || locale;
    return resolveLocaleChain(locale).find(function (locale) {
      return locales.indexOf(locale) > -1;
    });
  };
}
function resolveLocaleChain(locale) {
  var chain = [];
  var tokens = locale.split('-');
  while (tokens.length) {
    chain.push(tokens.join('-'));
    tokens.pop();
  }
  return chain;
}
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"], __webpack_require__(/*! ./../../../webpack/buildin/global.js */ 3)))

/***/ }),

/***/ 23:
/*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/classCallCheck.js ***!
  \***************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _classCallCheck(instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError("Cannot call a class as a function");
  }
}
module.exports = _classCallCheck, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 24:
/*!************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/createClass.js ***!
  \************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var toPropertyKey = __webpack_require__(/*! ./toPropertyKey.js */ 12);
function _defineProperties(target, props) {
  for (var i = 0; i < props.length; i++) {
    var descriptor = props[i];
    descriptor.enumerable = descriptor.enumerable || false;
    descriptor.configurable = true;
    if ("value" in descriptor) descriptor.writable = true;
    Object.defineProperty(target, toPropertyKey(descriptor.key), descriptor);
  }
}
function _createClass(Constructor, protoProps, staticProps) {
  if (protoProps) _defineProperties(Constructor.prototype, protoProps);
  if (staticProps) _defineProperties(Constructor, staticProps);
  Object.defineProperty(Constructor, "prototype", {
    writable: false
  });
  return Constructor;
}
module.exports = _createClass, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 25:
/*!******************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/mp-vue/dist/mp.runtime.esm.js ***!
  \******************************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* WEBPACK VAR INJECTION */(function(global) {/*!
 * Vue.js v2.6.11
 * (c) 2014-2024 Evan You
 * Released under the MIT License.
 */
/*  */

var emptyObject = Object.freeze({});

// These helpers produce better VM code in JS engines due to their
// explicitness and function inlining.
function isUndef (v) {
  return v === undefined || v === null
}

function isDef (v) {
  return v !== undefined && v !== null
}

function isTrue (v) {
  return v === true
}

function isFalse (v) {
  return v === false
}

/**
 * Check if value is primitive.
 */
function isPrimitive (value) {
  return (
    typeof value === 'string' ||
    typeof value === 'number' ||
    // $flow-disable-line
    typeof value === 'symbol' ||
    typeof value === 'boolean'
  )
}

/**
 * Quick object check - this is primarily used to tell
 * Objects from primitive values when we know the value
 * is a JSON-compliant type.
 */
function isObject (obj) {
  return obj !== null && typeof obj === 'object'
}

/**
 * Get the raw type string of a value, e.g., [object Object].
 */
var _toString = Object.prototype.toString;

function toRawType (value) {
  return _toString.call(value).slice(8, -1)
}

/**
 * Strict object type check. Only returns true
 * for plain JavaScript objects.
 */
function isPlainObject (obj) {
  return _toString.call(obj) === '[object Object]'
}

function isRegExp (v) {
  return _toString.call(v) === '[object RegExp]'
}

/**
 * Check if val is a valid array index.
 */
function isValidArrayIndex (val) {
  var n = parseFloat(String(val));
  return n >= 0 && Math.floor(n) === n && isFinite(val)
}

function isPromise (val) {
  return (
    isDef(val) &&
    typeof val.then === 'function' &&
    typeof val.catch === 'function'
  )
}

/**
 * Convert a value to a string that is actually rendered.
 */
function toString (val) {
  return val == null
    ? ''
    : Array.isArray(val) || (isPlainObject(val) && val.toString === _toString)
      ? JSON.stringify(val, null, 2)
      : String(val)
}

/**
 * Convert an input value to a number for persistence.
 * If the conversion fails, return original string.
 */
function toNumber (val) {
  var n = parseFloat(val);
  return isNaN(n) ? val : n
}

/**
 * Make a map and return a function for checking if a key
 * is in that map.
 */
function makeMap (
  str,
  expectsLowerCase
) {
  var map = Object.create(null);
  var list = str.split(',');
  for (var i = 0; i < list.length; i++) {
    map[list[i]] = true;
  }
  return expectsLowerCase
    ? function (val) { return map[val.toLowerCase()]; }
    : function (val) { return map[val]; }
}

/**
 * Check if a tag is a built-in tag.
 */
var isBuiltInTag = makeMap('slot,component', true);

/**
 * Check if an attribute is a reserved attribute.
 */
var isReservedAttribute = makeMap('key,ref,slot,slot-scope,is');

/**
 * Remove an item from an array.
 */
function remove (arr, item) {
  if (arr.length) {
    var index = arr.indexOf(item);
    if (index > -1) {
      return arr.splice(index, 1)
    }
  }
}

/**
 * Check whether an object has the property.
 */
var hasOwnProperty = Object.prototype.hasOwnProperty;
function hasOwn (obj, key) {
  return hasOwnProperty.call(obj, key)
}

/**
 * Create a cached version of a pure function.
 */
function cached (fn) {
  var cache = Object.create(null);
  return (function cachedFn (str) {
    var hit = cache[str];
    return hit || (cache[str] = fn(str))
  })
}

/**
 * Camelize a hyphen-delimited string.
 */
var camelizeRE = /-(\w)/g;
var camelize = cached(function (str) {
  return str.replace(camelizeRE, function (_, c) { return c ? c.toUpperCase() : ''; })
});

/**
 * Capitalize a string.
 */
var capitalize = cached(function (str) {
  return str.charAt(0).toUpperCase() + str.slice(1)
});

/**
 * Hyphenate a camelCase string.
 */
var hyphenateRE = /\B([A-Z])/g;
var hyphenate = cached(function (str) {
  return str.replace(hyphenateRE, '-$1').toLowerCase()
});

/**
 * Simple bind polyfill for environments that do not support it,
 * e.g., PhantomJS 1.x. Technically, we don't need this anymore
 * since native bind is now performant enough in most browsers.
 * But removing it would mean breaking code that was able to run in
 * PhantomJS 1.x, so this must be kept for backward compatibility.
 */

/* istanbul ignore next */
function polyfillBind (fn, ctx) {
  function boundFn (a) {
    var l = arguments.length;
    return l
      ? l > 1
        ? fn.apply(ctx, arguments)
        : fn.call(ctx, a)
      : fn.call(ctx)
  }

  boundFn._length = fn.length;
  return boundFn
}

function nativeBind (fn, ctx) {
  return fn.bind(ctx)
}

var bind = Function.prototype.bind
  ? nativeBind
  : polyfillBind;

/**
 * Convert an Array-like object to a real Array.
 */
function toArray (list, start) {
  start = start || 0;
  var i = list.length - start;
  var ret = new Array(i);
  while (i--) {
    ret[i] = list[i + start];
  }
  return ret
}

/**
 * Mix properties into target object.
 */
function extend (to, _from) {
  for (var key in _from) {
    to[key] = _from[key];
  }
  return to
}

/**
 * Merge an Array of Objects into a single Object.
 */
function toObject (arr) {
  var res = {};
  for (var i = 0; i < arr.length; i++) {
    if (arr[i]) {
      extend(res, arr[i]);
    }
  }
  return res
}

/* eslint-disable no-unused-vars */

/**
 * Perform no operation.
 * Stubbing args to make Flow happy without leaving useless transpiled code
 * with ...rest (https://flow.org/blog/2017/05/07/Strict-Function-Call-Arity/).
 */
function noop (a, b, c) {}

/**
 * Always return false.
 */
var no = function (a, b, c) { return false; };

/* eslint-enable no-unused-vars */

/**
 * Return the same value.
 */
var identity = function (_) { return _; };

/**
 * Check if two values are loosely equal - that is,
 * if they are plain objects, do they have the same shape?
 */
function looseEqual (a, b) {
  if (a === b) { return true }
  var isObjectA = isObject(a);
  var isObjectB = isObject(b);
  if (isObjectA && isObjectB) {
    try {
      var isArrayA = Array.isArray(a);
      var isArrayB = Array.isArray(b);
      if (isArrayA && isArrayB) {
        return a.length === b.length && a.every(function (e, i) {
          return looseEqual(e, b[i])
        })
      } else if (a instanceof Date && b instanceof Date) {
        return a.getTime() === b.getTime()
      } else if (!isArrayA && !isArrayB) {
        var keysA = Object.keys(a);
        var keysB = Object.keys(b);
        return keysA.length === keysB.length && keysA.every(function (key) {
          return looseEqual(a[key], b[key])
        })
      } else {
        /* istanbul ignore next */
        return false
      }
    } catch (e) {
      /* istanbul ignore next */
      return false
    }
  } else if (!isObjectA && !isObjectB) {
    return String(a) === String(b)
  } else {
    return false
  }
}

/**
 * Return the first index at which a loosely equal value can be
 * found in the array (if value is a plain object, the array must
 * contain an object of the same shape), or -1 if it is not present.
 */
function looseIndexOf (arr, val) {
  for (var i = 0; i < arr.length; i++) {
    if (looseEqual(arr[i], val)) { return i }
  }
  return -1
}

/**
 * Ensure a function is called only once.
 */
function once (fn) {
  var called = false;
  return function () {
    if (!called) {
      called = true;
      fn.apply(this, arguments);
    }
  }
}

var ASSET_TYPES = [
  'component',
  'directive',
  'filter'
];

var LIFECYCLE_HOOKS = [
  'beforeCreate',
  'created',
  'beforeMount',
  'mounted',
  'beforeUpdate',
  'updated',
  'beforeDestroy',
  'destroyed',
  'activated',
  'deactivated',
  'errorCaptured',
  'serverPrefetch'
];

/*  */



var config = ({
  /**
   * Option merge strategies (used in core/util/options)
   */
  // $flow-disable-line
  optionMergeStrategies: Object.create(null),

  /**
   * Whether to suppress warnings.
   */
  silent: false,

  /**
   * Show production mode tip message on boot?
   */
  productionTip: "development" !== 'production',

  /**
   * Whether to enable devtools
   */
  devtools: "development" !== 'production',

  /**
   * Whether to record perf
   */
  performance: false,

  /**
   * Error handler for watcher errors
   */
  errorHandler: null,

  /**
   * Warn handler for watcher warns
   */
  warnHandler: null,

  /**
   * Ignore certain custom elements
   */
  ignoredElements: [],

  /**
   * Custom user key aliases for v-on
   */
  // $flow-disable-line
  keyCodes: Object.create(null),

  /**
   * Check if a tag is reserved so that it cannot be registered as a
   * component. This is platform-dependent and may be overwritten.
   */
  isReservedTag: no,

  /**
   * Check if an attribute is reserved so that it cannot be used as a component
   * prop. This is platform-dependent and may be overwritten.
   */
  isReservedAttr: no,

  /**
   * Check if a tag is an unknown element.
   * Platform-dependent.
   */
  isUnknownElement: no,

  /**
   * Get the namespace of an element
   */
  getTagNamespace: noop,

  /**
   * Parse the real tag name for the specific platform.
   */
  parsePlatformTagName: identity,

  /**
   * Check if an attribute must be bound using property, e.g. value
   * Platform-dependent.
   */
  mustUseProp: no,

  /**
   * Perform updates asynchronously. Intended to be used by Vue Test Utils
   * This will significantly reduce performance if set to false.
   */
  async: true,

  /**
   * Exposed for legacy reasons
   */
  _lifecycleHooks: LIFECYCLE_HOOKS
});

/*  */

/**
 * unicode letters used for parsing html tags, component names and property paths.
 * using https://www.w3.org/TR/html53/semantics-scripting.html#potentialcustomelementname
 * skipping \u10000-\uEFFFF due to it freezing up PhantomJS
 */
var unicodeRegExp = /a-zA-Z\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD/;

/**
 * Check if a string starts with $ or _
 */
function isReserved (str) {
  var c = (str + '').charCodeAt(0);
  return c === 0x24 || c === 0x5F
}

/**
 * Define a property.
 */
function def (obj, key, val, enumerable) {
  Object.defineProperty(obj, key, {
    value: val,
    enumerable: !!enumerable,
    writable: true,
    configurable: true
  });
}

/**
 * Parse simple path.
 */
var bailRE = new RegExp(("[^" + (unicodeRegExp.source) + ".$_\\d]"));
function parsePath (path) {
  if (bailRE.test(path)) {
    return
  }
  var segments = path.split('.');
  return function (obj) {
    for (var i = 0; i < segments.length; i++) {
      if (!obj) { return }
      obj = obj[segments[i]];
    }
    return obj
  }
}

/*  */

// can we use __proto__?
var hasProto = '__proto__' in {};

// Browser environment sniffing
var inBrowser = typeof window !== 'undefined';
var inWeex = typeof WXEnvironment !== 'undefined' && !!WXEnvironment.platform;
var weexPlatform = inWeex && WXEnvironment.platform.toLowerCase();
var UA = inBrowser && window.navigator && window.navigator.userAgent.toLowerCase();
var isIE = UA && /msie|trident/.test(UA);
var isIE9 = UA && UA.indexOf('msie 9.0') > 0;
var isEdge = UA && UA.indexOf('edge/') > 0;
var isAndroid = (UA && UA.indexOf('android') > 0) || (weexPlatform === 'android');
var isIOS = (UA && /iphone|ipad|ipod|ios/.test(UA)) || (weexPlatform === 'ios');
var isChrome = UA && /chrome\/\d+/.test(UA) && !isEdge;
var isPhantomJS = UA && /phantomjs/.test(UA);
var isFF = UA && UA.match(/firefox\/(\d+)/);

// Firefox has a "watch" function on Object.prototype...
var nativeWatch = ({}).watch;
if (inBrowser) {
  try {
    var opts = {};
    Object.defineProperty(opts, 'passive', ({
      get: function get () {
      }
    })); // https://github.com/facebook/flow/issues/285
    window.addEventListener('test-passive', null, opts);
  } catch (e) {}
}

// this needs to be lazy-evaled because vue may be required before
// vue-server-renderer can set VUE_ENV
var _isServer;
var isServerRendering = function () {
  if (_isServer === undefined) {
    /* istanbul ignore if */
    if (!inBrowser && !inWeex && typeof global !== 'undefined') {
      // detect presence of vue-server-renderer and avoid
      // Webpack shimming the process
      _isServer = global['process'] && global['process'].env.VUE_ENV === 'server';
    } else {
      _isServer = false;
    }
  }
  return _isServer
};

// detect devtools
var devtools = inBrowser && window.__VUE_DEVTOOLS_GLOBAL_HOOK__;

/* istanbul ignore next */
function isNative (Ctor) {
  return typeof Ctor === 'function' && /native code/.test(Ctor.toString())
}

var hasSymbol =
  typeof Symbol !== 'undefined' && isNative(Symbol) &&
  typeof Reflect !== 'undefined' && isNative(Reflect.ownKeys);

var _Set;
/* istanbul ignore if */ // $flow-disable-line
if (typeof Set !== 'undefined' && isNative(Set)) {
  // use native Set when available.
  _Set = Set;
} else {
  // a non-standard Set polyfill that only works with primitive keys.
  _Set = /*@__PURE__*/(function () {
    function Set () {
      this.set = Object.create(null);
    }
    Set.prototype.has = function has (key) {
      return this.set[key] === true
    };
    Set.prototype.add = function add (key) {
      this.set[key] = true;
    };
    Set.prototype.clear = function clear () {
      this.set = Object.create(null);
    };

    return Set;
  }());
}

/*  */

var warn = noop;
var tip = noop;
var generateComponentTrace = (noop); // work around flow check
var formatComponentName = (noop);

if (true) {
  var hasConsole = typeof console !== 'undefined';
  var classifyRE = /(?:^|[-_])(\w)/g;
  var classify = function (str) { return str
    .replace(classifyRE, function (c) { return c.toUpperCase(); })
    .replace(/[-_]/g, ''); };

  warn = function (msg, vm) {
    var trace = vm ? generateComponentTrace(vm) : '';

    if (config.warnHandler) {
      config.warnHandler.call(null, msg, vm, trace);
    } else if (hasConsole && (!config.silent)) {
      console.error(("[Vue warn]: " + msg + trace));
    }
  };

  tip = function (msg, vm) {
    if (hasConsole && (!config.silent)) {
      console.warn("[Vue tip]: " + msg + (
        vm ? generateComponentTrace(vm) : ''
      ));
    }
  };

  formatComponentName = function (vm, includeFile) {
    if (vm.$root === vm) {
      if (vm.$options && vm.$options.__file) { // fixed by xxxxxx
        return ('') + vm.$options.__file
      }
      return '<Root>'
    }
    var options = typeof vm === 'function' && vm.cid != null
      ? vm.options
      : vm._isVue
        ? vm.$options || vm.constructor.options
        : vm;
    var name = options.name || options._componentTag;
    var file = options.__file;
    if (!name && file) {
      var match = file.match(/([^/\\]+)\.vue$/);
      name = match && match[1];
    }

    return (
      (name ? ("<" + (classify(name)) + ">") : "<Anonymous>") +
      (file && includeFile !== false ? (" at " + file) : '')
    )
  };

  var repeat = function (str, n) {
    var res = '';
    while (n) {
      if (n % 2 === 1) { res += str; }
      if (n > 1) { str += str; }
      n >>= 1;
    }
    return res
  };

  generateComponentTrace = function (vm) {
    if (vm._isVue && vm.$parent) {
      var tree = [];
      var currentRecursiveSequence = 0;
      while (vm && vm.$options.name !== 'PageBody') {
        if (tree.length > 0) {
          var last = tree[tree.length - 1];
          if (last.constructor === vm.constructor) {
            currentRecursiveSequence++;
            vm = vm.$parent;
            continue
          } else if (currentRecursiveSequence > 0) {
            tree[tree.length - 1] = [last, currentRecursiveSequence];
            currentRecursiveSequence = 0;
          }
        }
        !vm.$options.isReserved && tree.push(vm);
        vm = vm.$parent;
      }
      return '\n\nfound in\n\n' + tree
        .map(function (vm, i) { return ("" + (i === 0 ? '---> ' : repeat(' ', 5 + i * 2)) + (Array.isArray(vm)
            ? ((formatComponentName(vm[0])) + "... (" + (vm[1]) + " recursive calls)")
            : formatComponentName(vm))); })
        .join('\n')
    } else {
      return ("\n\n(found in " + (formatComponentName(vm)) + ")")
    }
  };
}

/*  */

var uid = 0;

/**
 * A dep is an observable that can have multiple
 * directives subscribing to it.
 */
var Dep = function Dep () {
  this.id = uid++;
  this.subs = [];
};

Dep.prototype.addSub = function addSub (sub) {
  this.subs.push(sub);
};

Dep.prototype.removeSub = function removeSub (sub) {
  remove(this.subs, sub);
};

Dep.prototype.depend = function depend () {
  if (Dep.SharedObject.target) {
    Dep.SharedObject.target.addDep(this);
  }
};

Dep.prototype.notify = function notify () {
  // stabilize the subscriber list first
  var subs = this.subs.slice();
  if ( true && !config.async) {
    // subs aren't sorted in scheduler if not running async
    // we need to sort them now to make sure they fire in correct
    // order
    subs.sort(function (a, b) { return a.id - b.id; });
  }
  for (var i = 0, l = subs.length; i < l; i++) {
    subs[i].update();
  }
};

// The current target watcher being evaluated.
// This is globally unique because only one watcher
// can be evaluated at a time.
// fixed by xxxxxx (nvue shared vuex)
/* eslint-disable no-undef */
Dep.SharedObject = {};
Dep.SharedObject.target = null;
Dep.SharedObject.targetStack = [];

function pushTarget (target) {
  Dep.SharedObject.targetStack.push(target);
  Dep.SharedObject.target = target;
  Dep.target = target;
}

function popTarget () {
  Dep.SharedObject.targetStack.pop();
  Dep.SharedObject.target = Dep.SharedObject.targetStack[Dep.SharedObject.targetStack.length - 1];
  Dep.target = Dep.SharedObject.target;
}

/*  */

var VNode = function VNode (
  tag,
  data,
  children,
  text,
  elm,
  context,
  componentOptions,
  asyncFactory
) {
  this.tag = tag;
  this.data = data;
  this.children = children;
  this.text = text;
  this.elm = elm;
  this.ns = undefined;
  this.context = context;
  this.fnContext = undefined;
  this.fnOptions = undefined;
  this.fnScopeId = undefined;
  this.key = data && data.key;
  this.componentOptions = componentOptions;
  this.componentInstance = undefined;
  this.parent = undefined;
  this.raw = false;
  this.isStatic = false;
  this.isRootInsert = true;
  this.isComment = false;
  this.isCloned = false;
  this.isOnce = false;
  this.asyncFactory = asyncFactory;
  this.asyncMeta = undefined;
  this.isAsyncPlaceholder = false;
};

var prototypeAccessors = { child: { configurable: true } };

// DEPRECATED: alias for componentInstance for backwards compat.
/* istanbul ignore next */
prototypeAccessors.child.get = function () {
  return this.componentInstance
};

Object.defineProperties( VNode.prototype, prototypeAccessors );

var createEmptyVNode = function (text) {
  if ( text === void 0 ) text = '';

  var node = new VNode();
  node.text = text;
  node.isComment = true;
  return node
};

function createTextVNode (val) {
  return new VNode(undefined, undefined, undefined, String(val))
}

// optimized shallow clone
// used for static nodes and slot nodes because they may be reused across
// multiple renders, cloning them avoids errors when DOM manipulations rely
// on their elm reference.
function cloneVNode (vnode) {
  var cloned = new VNode(
    vnode.tag,
    vnode.data,
    // #7975
    // clone children array to avoid mutating original in case of cloning
    // a child.
    vnode.children && vnode.children.slice(),
    vnode.text,
    vnode.elm,
    vnode.context,
    vnode.componentOptions,
    vnode.asyncFactory
  );
  cloned.ns = vnode.ns;
  cloned.isStatic = vnode.isStatic;
  cloned.key = vnode.key;
  cloned.isComment = vnode.isComment;
  cloned.fnContext = vnode.fnContext;
  cloned.fnOptions = vnode.fnOptions;
  cloned.fnScopeId = vnode.fnScopeId;
  cloned.asyncMeta = vnode.asyncMeta;
  cloned.isCloned = true;
  return cloned
}

/*
 * not type checking this file because flow doesn't play well with
 * dynamically accessing methods on Array prototype
 */

var arrayProto = Array.prototype;
var arrayMethods = Object.create(arrayProto);

var methodsToPatch = [
  'push',
  'pop',
  'shift',
  'unshift',
  'splice',
  'sort',
  'reverse'
];

/**
 * Intercept mutating methods and emit events
 */
methodsToPatch.forEach(function (method) {
  // cache original method
  var original = arrayProto[method];
  def(arrayMethods, method, function mutator () {
    var args = [], len = arguments.length;
    while ( len-- ) args[ len ] = arguments[ len ];

    var result = original.apply(this, args);
    var ob = this.__ob__;
    var inserted;
    switch (method) {
      case 'push':
      case 'unshift':
        inserted = args;
        break
      case 'splice':
        inserted = args.slice(2);
        break
    }
    if (inserted) { ob.observeArray(inserted); }
    // notify change
    ob.dep.notify();
    return result
  });
});

/*  */

var arrayKeys = Object.getOwnPropertyNames(arrayMethods);

/**
 * In some cases we may want to disable observation inside a component's
 * update computation.
 */
var shouldObserve = true;

function toggleObserving (value) {
  shouldObserve = value;
}

/**
 * Observer class that is attached to each observed
 * object. Once attached, the observer converts the target
 * object's property keys into getter/setters that
 * collect dependencies and dispatch updates.
 */
var Observer = function Observer (value) {
  this.value = value;
  this.dep = new Dep();
  this.vmCount = 0;
  def(value, '__ob__', this);
  if (Array.isArray(value)) {
    if (hasProto) {
      {// fixed by xxxxxx 微信小程序使用 plugins 之后，数组方法被直接挂载到了数组对象上，需要执行 copyAugment 逻辑
        if(value.push !== value.__proto__.push){
          copyAugment(value, arrayMethods, arrayKeys);
        } else {
          protoAugment(value, arrayMethods);
        }
      }
    } else {
      copyAugment(value, arrayMethods, arrayKeys);
    }
    this.observeArray(value);
  } else {
    this.walk(value);
  }
};

/**
 * Walk through all properties and convert them into
 * getter/setters. This method should only be called when
 * value type is Object.
 */
Observer.prototype.walk = function walk (obj) {
  var keys = Object.keys(obj);
  for (var i = 0; i < keys.length; i++) {
    defineReactive$$1(obj, keys[i]);
  }
};

/**
 * Observe a list of Array items.
 */
Observer.prototype.observeArray = function observeArray (items) {
  for (var i = 0, l = items.length; i < l; i++) {
    observe(items[i]);
  }
};

// helpers

/**
 * Augment a target Object or Array by intercepting
 * the prototype chain using __proto__
 */
function protoAugment (target, src) {
  /* eslint-disable no-proto */
  target.__proto__ = src;
  /* eslint-enable no-proto */
}

/**
 * Augment a target Object or Array by defining
 * hidden properties.
 */
/* istanbul ignore next */
function copyAugment (target, src, keys) {
  for (var i = 0, l = keys.length; i < l; i++) {
    var key = keys[i];
    def(target, key, src[key]);
  }
}

/**
 * Attempt to create an observer instance for a value,
 * returns the new observer if successfully observed,
 * or the existing observer if the value already has one.
 */
function observe (value, asRootData) {
  if (!isObject(value) || value instanceof VNode) {
    return
  }
  var ob;
  if (hasOwn(value, '__ob__') && value.__ob__ instanceof Observer) {
    ob = value.__ob__;
  } else if (
    shouldObserve &&
    !isServerRendering() &&
    (Array.isArray(value) || isPlainObject(value)) &&
    Object.isExtensible(value) &&
    !value._isVue &&
    !value.__v_isMPComponent
  ) {
    ob = new Observer(value);
  }
  if (asRootData && ob) {
    ob.vmCount++;
  }
  return ob
}

/**
 * Define a reactive property on an Object.
 */
function defineReactive$$1 (
  obj,
  key,
  val,
  customSetter,
  shallow
) {
  var dep = new Dep();

  var property = Object.getOwnPropertyDescriptor(obj, key);
  if (property && property.configurable === false) {
    return
  }

  // cater for pre-defined getter/setters
  var getter = property && property.get;
  var setter = property && property.set;
  if ((!getter || setter) && arguments.length === 2) {
    val = obj[key];
  }

  var childOb = !shallow && observe(val);
  Object.defineProperty(obj, key, {
    enumerable: true,
    configurable: true,
    get: function reactiveGetter () {
      var value = getter ? getter.call(obj) : val;
      if (Dep.SharedObject.target) { // fixed by xxxxxx
        dep.depend();
        if (childOb) {
          childOb.dep.depend();
          if (Array.isArray(value)) {
            dependArray(value);
          }
        }
      }
      return value
    },
    set: function reactiveSetter (newVal) {
      var value = getter ? getter.call(obj) : val;
      /* eslint-disable no-self-compare */
      if (newVal === value || (newVal !== newVal && value !== value)) {
        return
      }
      /* eslint-enable no-self-compare */
      if ( true && customSetter) {
        customSetter();
      }
      // #7981: for accessor properties without setter
      if (getter && !setter) { return }
      if (setter) {
        setter.call(obj, newVal);
      } else {
        val = newVal;
      }
      childOb = !shallow && observe(newVal);
      dep.notify();
    }
  });
}

/**
 * Set a property on an object. Adds the new property and
 * triggers change notification if the property doesn't
 * already exist.
 */
function set (target, key, val) {
  if ( true &&
    (isUndef(target) || isPrimitive(target))
  ) {
    warn(("Cannot set reactive property on undefined, null, or primitive value: " + ((target))));
  }
  if (Array.isArray(target) && isValidArrayIndex(key)) {
    target.length = Math.max(target.length, key);
    target.splice(key, 1, val);
    return val
  }
  if (key in target && !(key in Object.prototype)) {
    target[key] = val;
    return val
  }
  var ob = (target).__ob__;
  if (target._isVue || (ob && ob.vmCount)) {
     true && warn(
      'Avoid adding reactive properties to a Vue instance or its root $data ' +
      'at runtime - declare it upfront in the data option.'
    );
    return val
  }
  if (!ob) {
    target[key] = val;
    return val
  }
  defineReactive$$1(ob.value, key, val);
  ob.dep.notify();
  return val
}

/**
 * Delete a property and trigger change if necessary.
 */
function del (target, key) {
  if ( true &&
    (isUndef(target) || isPrimitive(target))
  ) {
    warn(("Cannot delete reactive property on undefined, null, or primitive value: " + ((target))));
  }
  if (Array.isArray(target) && isValidArrayIndex(key)) {
    target.splice(key, 1);
    return
  }
  var ob = (target).__ob__;
  if (target._isVue || (ob && ob.vmCount)) {
     true && warn(
      'Avoid deleting properties on a Vue instance or its root $data ' +
      '- just set it to null.'
    );
    return
  }
  if (!hasOwn(target, key)) {
    return
  }
  delete target[key];
  if (!ob) {
    return
  }
  ob.dep.notify();
}

/**
 * Collect dependencies on array elements when the array is touched, since
 * we cannot intercept array element access like property getters.
 */
function dependArray (value) {
  for (var e = (void 0), i = 0, l = value.length; i < l; i++) {
    e = value[i];
    e && e.__ob__ && e.__ob__.dep.depend();
    if (Array.isArray(e)) {
      dependArray(e);
    }
  }
}

/*  */

/**
 * Option overwriting strategies are functions that handle
 * how to merge a parent option value and a child option
 * value into the final value.
 */
var strats = config.optionMergeStrategies;

/**
 * Options with restrictions
 */
if (true) {
  strats.el = strats.propsData = function (parent, child, vm, key) {
    if (!vm) {
      warn(
        "option \"" + key + "\" can only be used during instance " +
        'creation with the `new` keyword.'
      );
    }
    return defaultStrat(parent, child)
  };
}

/**
 * Helper that recursively merges two data objects together.
 */
function mergeData (to, from) {
  if (!from) { return to }
  var key, toVal, fromVal;

  var keys = hasSymbol
    ? Reflect.ownKeys(from)
    : Object.keys(from);

  for (var i = 0; i < keys.length; i++) {
    key = keys[i];
    // in case the object is already observed...
    if (key === '__ob__') { continue }
    toVal = to[key];
    fromVal = from[key];
    if (!hasOwn(to, key)) {
      set(to, key, fromVal);
    } else if (
      toVal !== fromVal &&
      isPlainObject(toVal) &&
      isPlainObject(fromVal)
    ) {
      mergeData(toVal, fromVal);
    }
  }
  return to
}

/**
 * Data
 */
function mergeDataOrFn (
  parentVal,
  childVal,
  vm
) {
  if (!vm) {
    // in a Vue.extend merge, both should be functions
    if (!childVal) {
      return parentVal
    }
    if (!parentVal) {
      return childVal
    }
    // when parentVal & childVal are both present,
    // we need to return a function that returns the
    // merged result of both functions... no need to
    // check if parentVal is a function here because
    // it has to be a function to pass previous merges.
    return function mergedDataFn () {
      return mergeData(
        typeof childVal === 'function' ? childVal.call(this, this) : childVal,
        typeof parentVal === 'function' ? parentVal.call(this, this) : parentVal
      )
    }
  } else {
    return function mergedInstanceDataFn () {
      // instance merge
      var instanceData = typeof childVal === 'function'
        ? childVal.call(vm, vm)
        : childVal;
      var defaultData = typeof parentVal === 'function'
        ? parentVal.call(vm, vm)
        : parentVal;
      if (instanceData) {
        return mergeData(instanceData, defaultData)
      } else {
        return defaultData
      }
    }
  }
}

strats.data = function (
  parentVal,
  childVal,
  vm
) {
  if (!vm) {
    if (childVal && typeof childVal !== 'function') {
       true && warn(
        'The "data" option should be a function ' +
        'that returns a per-instance value in component ' +
        'definitions.',
        vm
      );

      return parentVal
    }
    return mergeDataOrFn(parentVal, childVal)
  }

  return mergeDataOrFn(parentVal, childVal, vm)
};

/**
 * Hooks and props are merged as arrays.
 */
function mergeHook (
  parentVal,
  childVal
) {
  var res = childVal
    ? parentVal
      ? parentVal.concat(childVal)
      : Array.isArray(childVal)
        ? childVal
        : [childVal]
    : parentVal;
  return res
    ? dedupeHooks(res)
    : res
}

function dedupeHooks (hooks) {
  var res = [];
  for (var i = 0; i < hooks.length; i++) {
    if (res.indexOf(hooks[i]) === -1) {
      res.push(hooks[i]);
    }
  }
  return res
}

LIFECYCLE_HOOKS.forEach(function (hook) {
  strats[hook] = mergeHook;
});

/**
 * Assets
 *
 * When a vm is present (instance creation), we need to do
 * a three-way merge between constructor options, instance
 * options and parent options.
 */
function mergeAssets (
  parentVal,
  childVal,
  vm,
  key
) {
  var res = Object.create(parentVal || null);
  if (childVal) {
     true && assertObjectType(key, childVal, vm);
    return extend(res, childVal)
  } else {
    return res
  }
}

ASSET_TYPES.forEach(function (type) {
  strats[type + 's'] = mergeAssets;
});

/**
 * Watchers.
 *
 * Watchers hashes should not overwrite one
 * another, so we merge them as arrays.
 */
strats.watch = function (
  parentVal,
  childVal,
  vm,
  key
) {
  // work around Firefox's Object.prototype.watch...
  if (parentVal === nativeWatch) { parentVal = undefined; }
  if (childVal === nativeWatch) { childVal = undefined; }
  /* istanbul ignore if */
  if (!childVal) { return Object.create(parentVal || null) }
  if (true) {
    assertObjectType(key, childVal, vm);
  }
  if (!parentVal) { return childVal }
  var ret = {};
  extend(ret, parentVal);
  for (var key$1 in childVal) {
    var parent = ret[key$1];
    var child = childVal[key$1];
    if (parent && !Array.isArray(parent)) {
      parent = [parent];
    }
    ret[key$1] = parent
      ? parent.concat(child)
      : Array.isArray(child) ? child : [child];
  }
  return ret
};

/**
 * Other object hashes.
 */
strats.props =
strats.methods =
strats.inject =
strats.computed = function (
  parentVal,
  childVal,
  vm,
  key
) {
  if (childVal && "development" !== 'production') {
    assertObjectType(key, childVal, vm);
  }
  if (!parentVal) { return childVal }
  var ret = Object.create(null);
  extend(ret, parentVal);
  if (childVal) { extend(ret, childVal); }
  return ret
};
strats.provide = mergeDataOrFn;

/**
 * Default strategy.
 */
var defaultStrat = function (parentVal, childVal) {
  return childVal === undefined
    ? parentVal
    : childVal
};

/**
 * Validate component names
 */
function checkComponents (options) {
  for (var key in options.components) {
    validateComponentName(key);
  }
}

function validateComponentName (name) {
  if (!new RegExp(("^[a-zA-Z][\\-\\.0-9_" + (unicodeRegExp.source) + "]*$")).test(name)) {
    warn(
      'Invalid component name: "' + name + '". Component names ' +
      'should conform to valid custom element name in html5 specification.'
    );
  }
  if (isBuiltInTag(name) || config.isReservedTag(name)) {
    warn(
      'Do not use built-in or reserved HTML elements as component ' +
      'id: ' + name
    );
  }
}

/**
 * Ensure all props option syntax are normalized into the
 * Object-based format.
 */
function normalizeProps (options, vm) {
  var props = options.props;
  if (!props) { return }
  var res = {};
  var i, val, name;
  if (Array.isArray(props)) {
    i = props.length;
    while (i--) {
      val = props[i];
      if (typeof val === 'string') {
        name = camelize(val);
        res[name] = { type: null };
      } else if (true) {
        warn('props must be strings when using array syntax.');
      }
    }
  } else if (isPlainObject(props)) {
    for (var key in props) {
      val = props[key];
      name = camelize(key);
      res[name] = isPlainObject(val)
        ? val
        : { type: val };
    }
  } else if (true) {
    warn(
      "Invalid value for option \"props\": expected an Array or an Object, " +
      "but got " + (toRawType(props)) + ".",
      vm
    );
  }
  options.props = res;
}

/**
 * Normalize all injections into Object-based format
 */
function normalizeInject (options, vm) {
  var inject = options.inject;
  if (!inject) { return }
  var normalized = options.inject = {};
  if (Array.isArray(inject)) {
    for (var i = 0; i < inject.length; i++) {
      normalized[inject[i]] = { from: inject[i] };
    }
  } else if (isPlainObject(inject)) {
    for (var key in inject) {
      var val = inject[key];
      normalized[key] = isPlainObject(val)
        ? extend({ from: key }, val)
        : { from: val };
    }
  } else if (true) {
    warn(
      "Invalid value for option \"inject\": expected an Array or an Object, " +
      "but got " + (toRawType(inject)) + ".",
      vm
    );
  }
}

/**
 * Normalize raw function directives into object format.
 */
function normalizeDirectives (options) {
  var dirs = options.directives;
  if (dirs) {
    for (var key in dirs) {
      var def$$1 = dirs[key];
      if (typeof def$$1 === 'function') {
        dirs[key] = { bind: def$$1, update: def$$1 };
      }
    }
  }
}

function assertObjectType (name, value, vm) {
  if (!isPlainObject(value)) {
    warn(
      "Invalid value for option \"" + name + "\": expected an Object, " +
      "but got " + (toRawType(value)) + ".",
      vm
    );
  }
}

/**
 * Merge two option objects into a new one.
 * Core utility used in both instantiation and inheritance.
 */
function mergeOptions (
  parent,
  child,
  vm
) {
  if (true) {
    checkComponents(child);
  }

  if (typeof child === 'function') {
    child = child.options;
  }

  normalizeProps(child, vm);
  normalizeInject(child, vm);
  normalizeDirectives(child);

  // Apply extends and mixins on the child options,
  // but only if it is a raw options object that isn't
  // the result of another mergeOptions call.
  // Only merged options has the _base property.
  if (!child._base) {
    if (child.extends) {
      parent = mergeOptions(parent, child.extends, vm);
    }
    if (child.mixins) {
      for (var i = 0, l = child.mixins.length; i < l; i++) {
        parent = mergeOptions(parent, child.mixins[i], vm);
      }
    }
  }

  var options = {};
  var key;
  for (key in parent) {
    mergeField(key);
  }
  for (key in child) {
    if (!hasOwn(parent, key)) {
      mergeField(key);
    }
  }
  function mergeField (key) {
    var strat = strats[key] || defaultStrat;
    options[key] = strat(parent[key], child[key], vm, key);
  }
  return options
}

/**
 * Resolve an asset.
 * This function is used because child instances need access
 * to assets defined in its ancestor chain.
 */
function resolveAsset (
  options,
  type,
  id,
  warnMissing
) {
  /* istanbul ignore if */
  if (typeof id !== 'string') {
    return
  }
  var assets = options[type];
  // check local registration variations first
  if (hasOwn(assets, id)) { return assets[id] }
  var camelizedId = camelize(id);
  if (hasOwn(assets, camelizedId)) { return assets[camelizedId] }
  var PascalCaseId = capitalize(camelizedId);
  if (hasOwn(assets, PascalCaseId)) { return assets[PascalCaseId] }
  // fallback to prototype chain
  var res = assets[id] || assets[camelizedId] || assets[PascalCaseId];
  if ( true && warnMissing && !res) {
    warn(
      'Failed to resolve ' + type.slice(0, -1) + ': ' + id,
      options
    );
  }
  return res
}

/*  */



function validateProp (
  key,
  propOptions,
  propsData,
  vm
) {
  var prop = propOptions[key];
  var absent = !hasOwn(propsData, key);
  var value = propsData[key];
  // boolean casting
  var booleanIndex = getTypeIndex(Boolean, prop.type);
  if (booleanIndex > -1) {
    if (absent && !hasOwn(prop, 'default')) {
      value = false;
    } else if (value === '' || value === hyphenate(key)) {
      // only cast empty string / same name to boolean if
      // boolean has higher priority
      var stringIndex = getTypeIndex(String, prop.type);
      if (stringIndex < 0 || booleanIndex < stringIndex) {
        value = true;
      }
    }
  }
  // check default value
  if (value === undefined) {
    value = getPropDefaultValue(vm, prop, key);
    // since the default value is a fresh copy,
    // make sure to observe it.
    var prevShouldObserve = shouldObserve;
    toggleObserving(true);
    observe(value);
    toggleObserving(prevShouldObserve);
  }
  if (
    true
  ) {
    assertProp(prop, key, value, vm, absent);
  }
  return value
}

/**
 * Get the default value of a prop.
 */
function getPropDefaultValue (vm, prop, key) {
  // no default, return undefined
  if (!hasOwn(prop, 'default')) {
    return undefined
  }
  var def = prop.default;
  // warn against non-factory defaults for Object & Array
  if ( true && isObject(def)) {
    warn(
      'Invalid default value for prop "' + key + '": ' +
      'Props with type Object/Array must use a factory function ' +
      'to return the default value.',
      vm
    );
  }
  // the raw prop value was also undefined from previous render,
  // return previous default value to avoid unnecessary watcher trigger
  if (vm && vm.$options.propsData &&
    vm.$options.propsData[key] === undefined &&
    vm._props[key] !== undefined
  ) {
    return vm._props[key]
  }
  // call factory function for non-Function types
  // a value is Function if its prototype is function even across different execution context
  return typeof def === 'function' && getType(prop.type) !== 'Function'
    ? def.call(vm)
    : def
}

/**
 * Assert whether a prop is valid.
 */
function assertProp (
  prop,
  name,
  value,
  vm,
  absent
) {
  if (prop.required && absent) {
    warn(
      'Missing required prop: "' + name + '"',
      vm
    );
    return
  }
  if (value == null && !prop.required) {
    return
  }
  var type = prop.type;
  var valid = !type || type === true;
  var expectedTypes = [];
  if (type) {
    if (!Array.isArray(type)) {
      type = [type];
    }
    for (var i = 0; i < type.length && !valid; i++) {
      var assertedType = assertType(value, type[i]);
      expectedTypes.push(assertedType.expectedType || '');
      valid = assertedType.valid;
    }
  }

  if (!valid) {
    warn(
      getInvalidTypeMessage(name, value, expectedTypes),
      vm
    );
    return
  }
  var validator = prop.validator;
  if (validator) {
    if (!validator(value)) {
      warn(
        'Invalid prop: custom validator check failed for prop "' + name + '".',
        vm
      );
    }
  }
}

var simpleCheckRE = /^(String|Number|Boolean|Function|Symbol)$/;

function assertType (value, type) {
  var valid;
  var expectedType = getType(type);
  if (simpleCheckRE.test(expectedType)) {
    var t = typeof value;
    valid = t === expectedType.toLowerCase();
    // for primitive wrapper objects
    if (!valid && t === 'object') {
      valid = value instanceof type;
    }
  } else if (expectedType === 'Object') {
    valid = isPlainObject(value);
  } else if (expectedType === 'Array') {
    valid = Array.isArray(value);
  } else {
    valid = value instanceof type;
  }
  return {
    valid: valid,
    expectedType: expectedType
  }
}

/**
 * Use function string name to check built-in types,
 * because a simple equality check will fail when running
 * across different vms / iframes.
 */
function getType (fn) {
  var match = fn && fn.toString().match(/^\s*function (\w+)/);
  return match ? match[1] : ''
}

function isSameType (a, b) {
  return getType(a) === getType(b)
}

function getTypeIndex (type, expectedTypes) {
  if (!Array.isArray(expectedTypes)) {
    return isSameType(expectedTypes, type) ? 0 : -1
  }
  for (var i = 0, len = expectedTypes.length; i < len; i++) {
    if (isSameType(expectedTypes[i], type)) {
      return i
    }
  }
  return -1
}

function getInvalidTypeMessage (name, value, expectedTypes) {
  var message = "Invalid prop: type check failed for prop \"" + name + "\"." +
    " Expected " + (expectedTypes.map(capitalize).join(', '));
  var expectedType = expectedTypes[0];
  var receivedType = toRawType(value);
  var expectedValue = styleValue(value, expectedType);
  var receivedValue = styleValue(value, receivedType);
  // check if we need to specify expected value
  if (expectedTypes.length === 1 &&
      isExplicable(expectedType) &&
      !isBoolean(expectedType, receivedType)) {
    message += " with value " + expectedValue;
  }
  message += ", got " + receivedType + " ";
  // check if we need to specify received value
  if (isExplicable(receivedType)) {
    message += "with value " + receivedValue + ".";
  }
  return message
}

function styleValue (value, type) {
  if (type === 'String') {
    return ("\"" + value + "\"")
  } else if (type === 'Number') {
    return ("" + (Number(value)))
  } else {
    return ("" + value)
  }
}

function isExplicable (value) {
  var explicitTypes = ['string', 'number', 'boolean'];
  return explicitTypes.some(function (elem) { return value.toLowerCase() === elem; })
}

function isBoolean () {
  var args = [], len = arguments.length;
  while ( len-- ) args[ len ] = arguments[ len ];

  return args.some(function (elem) { return elem.toLowerCase() === 'boolean'; })
}

/*  */

function handleError (err, vm, info) {
  // Deactivate deps tracking while processing error handler to avoid possible infinite rendering.
  // See: https://github.com/vuejs/vuex/issues/1505
  pushTarget();
  try {
    if (vm) {
      var cur = vm;
      while ((cur = cur.$parent)) {
        var hooks = cur.$options.errorCaptured;
        if (hooks) {
          for (var i = 0; i < hooks.length; i++) {
            try {
              var capture = hooks[i].call(cur, err, vm, info) === false;
              if (capture) { return }
            } catch (e) {
              globalHandleError(e, cur, 'errorCaptured hook');
            }
          }
        }
      }
    }
    globalHandleError(err, vm, info);
  } finally {
    popTarget();
  }
}

function invokeWithErrorHandling (
  handler,
  context,
  args,
  vm,
  info
) {
  var res;
  try {
    res = args ? handler.apply(context, args) : handler.call(context);
    if (res && !res._isVue && isPromise(res) && !res._handled) {
      res.catch(function (e) { return handleError(e, vm, info + " (Promise/async)"); });
      // issue #9511
      // avoid catch triggering multiple times when nested calls
      res._handled = true;
    }
  } catch (e) {
    handleError(e, vm, info);
  }
  return res
}

function globalHandleError (err, vm, info) {
  if (config.errorHandler) {
    try {
      return config.errorHandler.call(null, err, vm, info)
    } catch (e) {
      // if the user intentionally throws the original error in the handler,
      // do not log it twice
      if (e !== err) {
        logError(e, null, 'config.errorHandler');
      }
    }
  }
  logError(err, vm, info);
}

function logError (err, vm, info) {
  if (true) {
    warn(("Error in " + info + ": \"" + (err.toString()) + "\""), vm);
  }
  /* istanbul ignore else */
  if ((inBrowser || inWeex) && typeof console !== 'undefined') {
    console.error(err);
  } else {
    throw err
  }
}

/*  */

var callbacks = [];
var pending = false;

function flushCallbacks () {
  pending = false;
  var copies = callbacks.slice(0);
  callbacks.length = 0;
  for (var i = 0; i < copies.length; i++) {
    copies[i]();
  }
}

// Here we have async deferring wrappers using microtasks.
// In 2.5 we used (macro) tasks (in combination with microtasks).
// However, it has subtle problems when state is changed right before repaint
// (e.g. #6813, out-in transitions).
// Also, using (macro) tasks in event handler would cause some weird behaviors
// that cannot be circumvented (e.g. #7109, #7153, #7546, #7834, #8109).
// So we now use microtasks everywhere, again.
// A major drawback of this tradeoff is that there are some scenarios
// where microtasks have too high a priority and fire in between supposedly
// sequential events (e.g. #4521, #6690, which have workarounds)
// or even between bubbling of the same event (#6566).
var timerFunc;

// The nextTick behavior leverages the microtask queue, which can be accessed
// via either native Promise.then or MutationObserver.
// MutationObserver has wider support, however it is seriously bugged in
// UIWebView in iOS >= 9.3.3 when triggered in touch event handlers. It
// completely stops working after triggering a few times... so, if native
// Promise is available, we will use it:
/* istanbul ignore next, $flow-disable-line */
if (typeof Promise !== 'undefined' && isNative(Promise)) {
  var p = Promise.resolve();
  timerFunc = function () {
    p.then(flushCallbacks);
    // In problematic UIWebViews, Promise.then doesn't completely break, but
    // it can get stuck in a weird state where callbacks are pushed into the
    // microtask queue but the queue isn't being flushed, until the browser
    // needs to do some other work, e.g. handle a timer. Therefore we can
    // "force" the microtask queue to be flushed by adding an empty timer.
    if (isIOS) { setTimeout(noop); }
  };
} else if (!isIE && typeof MutationObserver !== 'undefined' && (
  isNative(MutationObserver) ||
  // PhantomJS and iOS 7.x
  MutationObserver.toString() === '[object MutationObserverConstructor]'
)) {
  // Use MutationObserver where native Promise is not available,
  // e.g. PhantomJS, iOS7, Android 4.4
  // (#6466 MutationObserver is unreliable in IE11)
  var counter = 1;
  var observer = new MutationObserver(flushCallbacks);
  var textNode = document.createTextNode(String(counter));
  observer.observe(textNode, {
    characterData: true
  });
  timerFunc = function () {
    counter = (counter + 1) % 2;
    textNode.data = String(counter);
  };
} else if (typeof setImmediate !== 'undefined' && isNative(setImmediate)) {
  // Fallback to setImmediate.
  // Technically it leverages the (macro) task queue,
  // but it is still a better choice than setTimeout.
  timerFunc = function () {
    setImmediate(flushCallbacks);
  };
} else {
  // Fallback to setTimeout.
  timerFunc = function () {
    setTimeout(flushCallbacks, 0);
  };
}

function nextTick (cb, ctx) {
  var _resolve;
  callbacks.push(function () {
    if (cb) {
      try {
        cb.call(ctx);
      } catch (e) {
        handleError(e, ctx, 'nextTick');
      }
    } else if (_resolve) {
      _resolve(ctx);
    }
  });
  if (!pending) {
    pending = true;
    timerFunc();
  }
  // $flow-disable-line
  if (!cb && typeof Promise !== 'undefined') {
    return new Promise(function (resolve) {
      _resolve = resolve;
    })
  }
}

/*  */

/* not type checking this file because flow doesn't play well with Proxy */

var initProxy;

if (true) {
  var allowedGlobals = makeMap(
    'Infinity,undefined,NaN,isFinite,isNaN,' +
    'parseFloat,parseInt,decodeURI,decodeURIComponent,encodeURI,encodeURIComponent,' +
    'Math,Number,Date,Array,Object,Boolean,String,RegExp,Map,Set,JSON,Intl,' +
    'require' // for Webpack/Browserify
  );

  var warnNonPresent = function (target, key) {
    warn(
      "Property or method \"" + key + "\" is not defined on the instance but " +
      'referenced during render. Make sure that this property is reactive, ' +
      'either in the data option, or for class-based components, by ' +
      'initializing the property. ' +
      'See: https://vuejs.org/v2/guide/reactivity.html#Declaring-Reactive-Properties.',
      target
    );
  };

  var warnReservedPrefix = function (target, key) {
    warn(
      "Property \"" + key + "\" must be accessed with \"$data." + key + "\" because " +
      'properties starting with "$" or "_" are not proxied in the Vue instance to ' +
      'prevent conflicts with Vue internals. ' +
      'See: https://vuejs.org/v2/api/#data',
      target
    );
  };

  var hasProxy =
    typeof Proxy !== 'undefined' && isNative(Proxy);

  if (hasProxy) {
    var isBuiltInModifier = makeMap('stop,prevent,self,ctrl,shift,alt,meta,exact');
    config.keyCodes = new Proxy(config.keyCodes, {
      set: function set (target, key, value) {
        if (isBuiltInModifier(key)) {
          warn(("Avoid overwriting built-in modifier in config.keyCodes: ." + key));
          return false
        } else {
          target[key] = value;
          return true
        }
      }
    });
  }

  var hasHandler = {
    has: function has (target, key) {
      var has = key in target;
      var isAllowed = allowedGlobals(key) ||
        (typeof key === 'string' && key.charAt(0) === '_' && !(key in target.$data));
      if (!has && !isAllowed) {
        if (key in target.$data) { warnReservedPrefix(target, key); }
        else { warnNonPresent(target, key); }
      }
      return has || !isAllowed
    }
  };

  var getHandler = {
    get: function get (target, key) {
      if (typeof key === 'string' && !(key in target)) {
        if (key in target.$data) { warnReservedPrefix(target, key); }
        else { warnNonPresent(target, key); }
      }
      return target[key]
    }
  };

  initProxy = function initProxy (vm) {
    if (hasProxy) {
      // determine which proxy handler to use
      var options = vm.$options;
      var handlers = options.render && options.render._withStripped
        ? getHandler
        : hasHandler;
      vm._renderProxy = new Proxy(vm, handlers);
    } else {
      vm._renderProxy = vm;
    }
  };
}

/*  */

var seenObjects = new _Set();

/**
 * Recursively traverse an object to evoke all converted
 * getters, so that every nested property inside the object
 * is collected as a "deep" dependency.
 */
function traverse (val) {
  _traverse(val, seenObjects);
  seenObjects.clear();
}

function _traverse (val, seen) {
  var i, keys;
  var isA = Array.isArray(val);
  if ((!isA && !isObject(val)) || Object.isFrozen(val) || val instanceof VNode) {
    return
  }
  if (val.__ob__) {
    var depId = val.__ob__.dep.id;
    if (seen.has(depId)) {
      return
    }
    seen.add(depId);
  }
  if (isA) {
    i = val.length;
    while (i--) { _traverse(val[i], seen); }
  } else {
    keys = Object.keys(val);
    i = keys.length;
    while (i--) { _traverse(val[keys[i]], seen); }
  }
}

var mark;
var measure;

if (true) {
  var perf = inBrowser && window.performance;
  /* istanbul ignore if */
  if (
    perf &&
    perf.mark &&
    perf.measure &&
    perf.clearMarks &&
    perf.clearMeasures
  ) {
    mark = function (tag) { return perf.mark(tag); };
    measure = function (name, startTag, endTag) {
      perf.measure(name, startTag, endTag);
      perf.clearMarks(startTag);
      perf.clearMarks(endTag);
      // perf.clearMeasures(name)
    };
  }
}

/*  */

var normalizeEvent = cached(function (name) {
  var passive = name.charAt(0) === '&';
  name = passive ? name.slice(1) : name;
  var once$$1 = name.charAt(0) === '~'; // Prefixed last, checked first
  name = once$$1 ? name.slice(1) : name;
  var capture = name.charAt(0) === '!';
  name = capture ? name.slice(1) : name;
  return {
    name: name,
    once: once$$1,
    capture: capture,
    passive: passive
  }
});

function createFnInvoker (fns, vm) {
  function invoker () {
    var arguments$1 = arguments;

    var fns = invoker.fns;
    if (Array.isArray(fns)) {
      var cloned = fns.slice();
      for (var i = 0; i < cloned.length; i++) {
        invokeWithErrorHandling(cloned[i], null, arguments$1, vm, "v-on handler");
      }
    } else {
      // return handler return value for single handlers
      return invokeWithErrorHandling(fns, null, arguments, vm, "v-on handler")
    }
  }
  invoker.fns = fns;
  return invoker
}

function updateListeners (
  on,
  oldOn,
  add,
  remove$$1,
  createOnceHandler,
  vm
) {
  var name, def$$1, cur, old, event;
  for (name in on) {
    def$$1 = cur = on[name];
    old = oldOn[name];
    event = normalizeEvent(name);
    if (isUndef(cur)) {
       true && warn(
        "Invalid handler for event \"" + (event.name) + "\": got " + String(cur),
        vm
      );
    } else if (isUndef(old)) {
      if (isUndef(cur.fns)) {
        cur = on[name] = createFnInvoker(cur, vm);
      }
      if (isTrue(event.once)) {
        cur = on[name] = createOnceHandler(event.name, cur, event.capture);
      }
      add(event.name, cur, event.capture, event.passive, event.params);
    } else if (cur !== old) {
      old.fns = cur;
      on[name] = old;
    }
  }
  for (name in oldOn) {
    if (isUndef(on[name])) {
      event = normalizeEvent(name);
      remove$$1(event.name, oldOn[name], event.capture);
    }
  }
}

/*  */

/*  */

// fixed by xxxxxx (mp properties)
function extractPropertiesFromVNodeData(data, Ctor, res, context) {
  var propOptions = Ctor.options.mpOptions && Ctor.options.mpOptions.properties;
  if (isUndef(propOptions)) {
    return res
  }
  var externalClasses = Ctor.options.mpOptions.externalClasses || [];
  var attrs = data.attrs;
  var props = data.props;
  if (isDef(attrs) || isDef(props)) {
    for (var key in propOptions) {
      var altKey = hyphenate(key);
      var result = checkProp(res, props, key, altKey, true) ||
          checkProp(res, attrs, key, altKey, false);
      // externalClass
      if (
        result &&
        res[key] &&
        externalClasses.indexOf(altKey) !== -1 &&
        context[camelize(res[key])]
      ) {
        // 赋值 externalClass 真正的值(模板里 externalClass 的值可能是字符串)
        res[key] = context[camelize(res[key])];
      }
    }
  }
  return res
}

function extractPropsFromVNodeData (
  data,
  Ctor,
  tag,
  context// fixed by xxxxxx
) {
  // we are only extracting raw values here.
  // validation and default values are handled in the child
  // component itself.
  var propOptions = Ctor.options.props;
  if (isUndef(propOptions)) {
    // fixed by xxxxxx
    return extractPropertiesFromVNodeData(data, Ctor, {}, context)
  }
  var res = {};
  var attrs = data.attrs;
  var props = data.props;
  if (isDef(attrs) || isDef(props)) {
    for (var key in propOptions) {
      var altKey = hyphenate(key);
      if (true) {
        var keyInLowerCase = key.toLowerCase();
        if (
          key !== keyInLowerCase &&
          attrs && hasOwn(attrs, keyInLowerCase)
        ) {
          tip(
            "Prop \"" + keyInLowerCase + "\" is passed to component " +
            (formatComponentName(tag || Ctor)) + ", but the declared prop name is" +
            " \"" + key + "\". " +
            "Note that HTML attributes are case-insensitive and camelCased " +
            "props need to use their kebab-case equivalents when using in-DOM " +
            "templates. You should probably use \"" + altKey + "\" instead of \"" + key + "\"."
          );
        }
      }
      checkProp(res, props, key, altKey, true) ||
      checkProp(res, attrs, key, altKey, false);
    }
  }
  // fixed by xxxxxx
  return extractPropertiesFromVNodeData(data, Ctor, res, context)
}

function checkProp (
  res,
  hash,
  key,
  altKey,
  preserve
) {
  if (isDef(hash)) {
    if (hasOwn(hash, key)) {
      res[key] = hash[key];
      if (!preserve) {
        delete hash[key];
      }
      return true
    } else if (hasOwn(hash, altKey)) {
      res[key] = hash[altKey];
      if (!preserve) {
        delete hash[altKey];
      }
      return true
    }
  }
  return false
}

/*  */

// The template compiler attempts to minimize the need for normalization by
// statically analyzing the template at compile time.
//
// For plain HTML markup, normalization can be completely skipped because the
// generated render function is guaranteed to return Array<VNode>. There are
// two cases where extra normalization is needed:

// 1. When the children contains components - because a functional component
// may return an Array instead of a single root. In this case, just a simple
// normalization is needed - if any child is an Array, we flatten the whole
// thing with Array.prototype.concat. It is guaranteed to be only 1-level deep
// because functional components already normalize their own children.
function simpleNormalizeChildren (children) {
  for (var i = 0; i < children.length; i++) {
    if (Array.isArray(children[i])) {
      return Array.prototype.concat.apply([], children)
    }
  }
  return children
}

// 2. When the children contains constructs that always generated nested Arrays,
// e.g. <template>, <slot>, v-for, or when the children is provided by user
// with hand-written render functions / JSX. In such cases a full normalization
// is needed to cater to all possible types of children values.
function normalizeChildren (children) {
  return isPrimitive(children)
    ? [createTextVNode(children)]
    : Array.isArray(children)
      ? normalizeArrayChildren(children)
      : undefined
}

function isTextNode (node) {
  return isDef(node) && isDef(node.text) && isFalse(node.isComment)
}

function normalizeArrayChildren (children, nestedIndex) {
  var res = [];
  var i, c, lastIndex, last;
  for (i = 0; i < children.length; i++) {
    c = children[i];
    if (isUndef(c) || typeof c === 'boolean') { continue }
    lastIndex = res.length - 1;
    last = res[lastIndex];
    //  nested
    if (Array.isArray(c)) {
      if (c.length > 0) {
        c = normalizeArrayChildren(c, ((nestedIndex || '') + "_" + i));
        // merge adjacent text nodes
        if (isTextNode(c[0]) && isTextNode(last)) {
          res[lastIndex] = createTextVNode(last.text + (c[0]).text);
          c.shift();
        }
        res.push.apply(res, c);
      }
    } else if (isPrimitive(c)) {
      if (isTextNode(last)) {
        // merge adjacent text nodes
        // this is necessary for SSR hydration because text nodes are
        // essentially merged when rendered to HTML strings
        res[lastIndex] = createTextVNode(last.text + c);
      } else if (c !== '') {
        // convert primitive to vnode
        res.push(createTextVNode(c));
      }
    } else {
      if (isTextNode(c) && isTextNode(last)) {
        // merge adjacent text nodes
        res[lastIndex] = createTextVNode(last.text + c.text);
      } else {
        // default key for nested array children (likely generated by v-for)
        if (isTrue(children._isVList) &&
          isDef(c.tag) &&
          isUndef(c.key) &&
          isDef(nestedIndex)) {
          c.key = "__vlist" + nestedIndex + "_" + i + "__";
        }
        res.push(c);
      }
    }
  }
  return res
}

/*  */

function initProvide (vm) {
  var provide = vm.$options.provide;
  if (provide) {
    vm._provided = typeof provide === 'function'
      ? provide.call(vm)
      : provide;
  }
}

function initInjections (vm) {
  var result = resolveInject(vm.$options.inject, vm);
  if (result) {
    toggleObserving(false);
    Object.keys(result).forEach(function (key) {
      /* istanbul ignore else */
      if (true) {
        defineReactive$$1(vm, key, result[key], function () {
          warn(
            "Avoid mutating an injected value directly since the changes will be " +
            "overwritten whenever the provided component re-renders. " +
            "injection being mutated: \"" + key + "\"",
            vm
          );
        });
      } else {}
    });
    toggleObserving(true);
  }
}

function resolveInject (inject, vm) {
  if (inject) {
    // inject is :any because flow is not smart enough to figure out cached
    var result = Object.create(null);
    var keys = hasSymbol
      ? Reflect.ownKeys(inject)
      : Object.keys(inject);

    for (var i = 0; i < keys.length; i++) {
      var key = keys[i];
      // #6574 in case the inject object is observed...
      if (key === '__ob__') { continue }
      var provideKey = inject[key].from;
      var source = vm;
      while (source) {
        if (source._provided && hasOwn(source._provided, provideKey)) {
          result[key] = source._provided[provideKey];
          break
        }
        source = source.$parent;
      }
      if (!source) {
        if ('default' in inject[key]) {
          var provideDefault = inject[key].default;
          result[key] = typeof provideDefault === 'function'
            ? provideDefault.call(vm)
            : provideDefault;
        } else if (true) {
          warn(("Injection \"" + key + "\" not found"), vm);
        }
      }
    }
    return result
  }
}

/*  */



/**
 * Runtime helper for resolving raw children VNodes into a slot object.
 */
function resolveSlots (
  children,
  context
) {
  if (!children || !children.length) {
    return {}
  }
  var slots = {};
  for (var i = 0, l = children.length; i < l; i++) {
    var child = children[i];
    var data = child.data;
    // remove slot attribute if the node is resolved as a Vue slot node
    if (data && data.attrs && data.attrs.slot) {
      delete data.attrs.slot;
    }
    // named slots should only be respected if the vnode was rendered in the
    // same context.
    if ((child.context === context || child.fnContext === context) &&
      data && data.slot != null
    ) {
      var name = data.slot;
      var slot = (slots[name] || (slots[name] = []));
      if (child.tag === 'template') {
        slot.push.apply(slot, child.children || []);
      } else {
        slot.push(child);
      }
    } else {
      // fixed by xxxxxx 临时 hack 掉 uni-app 中的异步 name slot page
      if(child.asyncMeta && child.asyncMeta.data && child.asyncMeta.data.slot === 'page'){
        (slots['page'] || (slots['page'] = [])).push(child);
      }else{
        (slots.default || (slots.default = [])).push(child);
      }
    }
  }
  // ignore slots that contains only whitespace
  for (var name$1 in slots) {
    if (slots[name$1].every(isWhitespace)) {
      delete slots[name$1];
    }
  }
  return slots
}

function isWhitespace (node) {
  return (node.isComment && !node.asyncFactory) || node.text === ' '
}

/*  */

function normalizeScopedSlots (
  slots,
  normalSlots,
  prevSlots
) {
  var res;
  var hasNormalSlots = Object.keys(normalSlots).length > 0;
  var isStable = slots ? !!slots.$stable : !hasNormalSlots;
  var key = slots && slots.$key;
  if (!slots) {
    res = {};
  } else if (slots._normalized) {
    // fast path 1: child component re-render only, parent did not change
    return slots._normalized
  } else if (
    isStable &&
    prevSlots &&
    prevSlots !== emptyObject &&
    key === prevSlots.$key &&
    !hasNormalSlots &&
    !prevSlots.$hasNormal
  ) {
    // fast path 2: stable scoped slots w/ no normal slots to proxy,
    // only need to normalize once
    return prevSlots
  } else {
    res = {};
    for (var key$1 in slots) {
      if (slots[key$1] && key$1[0] !== '$') {
        res[key$1] = normalizeScopedSlot(normalSlots, key$1, slots[key$1]);
      }
    }
  }
  // expose normal slots on scopedSlots
  for (var key$2 in normalSlots) {
    if (!(key$2 in res)) {
      res[key$2] = proxyNormalSlot(normalSlots, key$2);
    }
  }
  // avoriaz seems to mock a non-extensible $scopedSlots object
  // and when that is passed down this would cause an error
  if (slots && Object.isExtensible(slots)) {
    (slots)._normalized = res;
  }
  def(res, '$stable', isStable);
  def(res, '$key', key);
  def(res, '$hasNormal', hasNormalSlots);
  return res
}

function normalizeScopedSlot(normalSlots, key, fn) {
  var normalized = function () {
    var res = arguments.length ? fn.apply(null, arguments) : fn({});
    res = res && typeof res === 'object' && !Array.isArray(res)
      ? [res] // single vnode
      : normalizeChildren(res);
    return res && (
      res.length === 0 ||
      (res.length === 1 && res[0].isComment) // #9658
    ) ? undefined
      : res
  };
  // this is a slot using the new v-slot syntax without scope. although it is
  // compiled as a scoped slot, render fn users would expect it to be present
  // on this.$slots because the usage is semantically a normal slot.
  if (fn.proxy) {
    Object.defineProperty(normalSlots, key, {
      get: normalized,
      enumerable: true,
      configurable: true
    });
  }
  return normalized
}

function proxyNormalSlot(slots, key) {
  return function () { return slots[key]; }
}

/*  */

/**
 * Runtime helper for rendering v-for lists.
 */
function renderList (
  val,
  render
) {
  var ret, i, l, keys, key;
  if (Array.isArray(val) || typeof val === 'string') {
    ret = new Array(val.length);
    for (i = 0, l = val.length; i < l; i++) {
      ret[i] = render(val[i], i, i, i); // fixed by xxxxxx
    }
  } else if (typeof val === 'number') {
    ret = new Array(val);
    for (i = 0; i < val; i++) {
      ret[i] = render(i + 1, i, i, i); // fixed by xxxxxx
    }
  } else if (isObject(val)) {
    if (hasSymbol && val[Symbol.iterator]) {
      ret = [];
      var iterator = val[Symbol.iterator]();
      var result = iterator.next();
      while (!result.done) {
        ret.push(render(result.value, ret.length, i, i++)); // fixed by xxxxxx
        result = iterator.next();
      }
    } else {
      keys = Object.keys(val);
      ret = new Array(keys.length);
      for (i = 0, l = keys.length; i < l; i++) {
        key = keys[i];
        ret[i] = render(val[key], key, i, i); // fixed by xxxxxx
      }
    }
  }
  if (!isDef(ret)) {
    ret = [];
  }
  (ret)._isVList = true;
  return ret
}

/*  */

/**
 * Runtime helper for rendering <slot>
 */
function renderSlot (
  name,
  fallback,
  props,
  bindObject
) {
  var scopedSlotFn = this.$scopedSlots[name];
  var nodes;
  if (scopedSlotFn) { // scoped slot
    props = props || {};
    if (bindObject) {
      if ( true && !isObject(bindObject)) {
        warn(
          'slot v-bind without argument expects an Object',
          this
        );
      }
      props = extend(extend({}, bindObject), props);
    }
    // fixed by xxxxxx app-plus scopedSlot
    nodes = scopedSlotFn(props, this, props._i) || fallback;
  } else {
    nodes = this.$slots[name] || fallback;
  }

  var target = props && props.slot;
  if (target) {
    return this.$createElement('template', { slot: target }, nodes)
  } else {
    return nodes
  }
}

/*  */

/**
 * Runtime helper for resolving filters
 */
function resolveFilter (id) {
  return resolveAsset(this.$options, 'filters', id, true) || identity
}

/*  */

function isKeyNotMatch (expect, actual) {
  if (Array.isArray(expect)) {
    return expect.indexOf(actual) === -1
  } else {
    return expect !== actual
  }
}

/**
 * Runtime helper for checking keyCodes from config.
 * exposed as Vue.prototype._k
 * passing in eventKeyName as last argument separately for backwards compat
 */
function checkKeyCodes (
  eventKeyCode,
  key,
  builtInKeyCode,
  eventKeyName,
  builtInKeyName
) {
  var mappedKeyCode = config.keyCodes[key] || builtInKeyCode;
  if (builtInKeyName && eventKeyName && !config.keyCodes[key]) {
    return isKeyNotMatch(builtInKeyName, eventKeyName)
  } else if (mappedKeyCode) {
    return isKeyNotMatch(mappedKeyCode, eventKeyCode)
  } else if (eventKeyName) {
    return hyphenate(eventKeyName) !== key
  }
}

/*  */

/**
 * Runtime helper for merging v-bind="object" into a VNode's data.
 */
function bindObjectProps (
  data,
  tag,
  value,
  asProp,
  isSync
) {
  if (value) {
    if (!isObject(value)) {
       true && warn(
        'v-bind without argument expects an Object or Array value',
        this
      );
    } else {
      if (Array.isArray(value)) {
        value = toObject(value);
      }
      var hash;
      var loop = function ( key ) {
        if (
          key === 'class' ||
          key === 'style' ||
          isReservedAttribute(key)
        ) {
          hash = data;
        } else {
          var type = data.attrs && data.attrs.type;
          hash = asProp || config.mustUseProp(tag, type, key)
            ? data.domProps || (data.domProps = {})
            : data.attrs || (data.attrs = {});
        }
        var camelizedKey = camelize(key);
        var hyphenatedKey = hyphenate(key);
        if (!(camelizedKey in hash) && !(hyphenatedKey in hash)) {
          hash[key] = value[key];

          if (isSync) {
            var on = data.on || (data.on = {});
            on[("update:" + key)] = function ($event) {
              value[key] = $event;
            };
          }
        }
      };

      for (var key in value) loop( key );
    }
  }
  return data
}

/*  */

/**
 * Runtime helper for rendering static trees.
 */
function renderStatic (
  index,
  isInFor
) {
  var cached = this._staticTrees || (this._staticTrees = []);
  var tree = cached[index];
  // if has already-rendered static tree and not inside v-for,
  // we can reuse the same tree.
  if (tree && !isInFor) {
    return tree
  }
  // otherwise, render a fresh tree.
  tree = cached[index] = this.$options.staticRenderFns[index].call(
    this._renderProxy,
    null,
    this // for render fns generated for functional component templates
  );
  markStatic(tree, ("__static__" + index), false);
  return tree
}

/**
 * Runtime helper for v-once.
 * Effectively it means marking the node as static with a unique key.
 */
function markOnce (
  tree,
  index,
  key
) {
  markStatic(tree, ("__once__" + index + (key ? ("_" + key) : "")), true);
  return tree
}

function markStatic (
  tree,
  key,
  isOnce
) {
  if (Array.isArray(tree)) {
    for (var i = 0; i < tree.length; i++) {
      if (tree[i] && typeof tree[i] !== 'string') {
        markStaticNode(tree[i], (key + "_" + i), isOnce);
      }
    }
  } else {
    markStaticNode(tree, key, isOnce);
  }
}

function markStaticNode (node, key, isOnce) {
  node.isStatic = true;
  node.key = key;
  node.isOnce = isOnce;
}

/*  */

function bindObjectListeners (data, value) {
  if (value) {
    if (!isPlainObject(value)) {
       true && warn(
        'v-on without argument expects an Object value',
        this
      );
    } else {
      var on = data.on = data.on ? extend({}, data.on) : {};
      for (var key in value) {
        var existing = on[key];
        var ours = value[key];
        on[key] = existing ? [].concat(existing, ours) : ours;
      }
    }
  }
  return data
}

/*  */

function resolveScopedSlots (
  fns, // see flow/vnode
  res,
  // the following are added in 2.6
  hasDynamicKeys,
  contentHashKey
) {
  res = res || { $stable: !hasDynamicKeys };
  for (var i = 0; i < fns.length; i++) {
    var slot = fns[i];
    if (Array.isArray(slot)) {
      resolveScopedSlots(slot, res, hasDynamicKeys);
    } else if (slot) {
      // marker for reverse proxying v-slot without scope on this.$slots
      if (slot.proxy) {
        slot.fn.proxy = true;
      }
      res[slot.key] = slot.fn;
    }
  }
  if (contentHashKey) {
    (res).$key = contentHashKey;
  }
  return res
}

/*  */

function bindDynamicKeys (baseObj, values) {
  for (var i = 0; i < values.length; i += 2) {
    var key = values[i];
    if (typeof key === 'string' && key) {
      baseObj[values[i]] = values[i + 1];
    } else if ( true && key !== '' && key !== null) {
      // null is a special value for explicitly removing a binding
      warn(
        ("Invalid value for dynamic directive argument (expected string or null): " + key),
        this
      );
    }
  }
  return baseObj
}

// helper to dynamically append modifier runtime markers to event names.
// ensure only append when value is already string, otherwise it will be cast
// to string and cause the type check to miss.
function prependModifier (value, symbol) {
  return typeof value === 'string' ? symbol + value : value
}

/*  */

function installRenderHelpers (target) {
  target._o = markOnce;
  target._n = toNumber;
  target._s = toString;
  target._l = renderList;
  target._t = renderSlot;
  target._q = looseEqual;
  target._i = looseIndexOf;
  target._m = renderStatic;
  target._f = resolveFilter;
  target._k = checkKeyCodes;
  target._b = bindObjectProps;
  target._v = createTextVNode;
  target._e = createEmptyVNode;
  target._u = resolveScopedSlots;
  target._g = bindObjectListeners;
  target._d = bindDynamicKeys;
  target._p = prependModifier;
}

/*  */

function FunctionalRenderContext (
  data,
  props,
  children,
  parent,
  Ctor
) {
  var this$1 = this;

  var options = Ctor.options;
  // ensure the createElement function in functional components
  // gets a unique context - this is necessary for correct named slot check
  var contextVm;
  if (hasOwn(parent, '_uid')) {
    contextVm = Object.create(parent);
    // $flow-disable-line
    contextVm._original = parent;
  } else {
    // the context vm passed in is a functional context as well.
    // in this case we want to make sure we are able to get a hold to the
    // real context instance.
    contextVm = parent;
    // $flow-disable-line
    parent = parent._original;
  }
  var isCompiled = isTrue(options._compiled);
  var needNormalization = !isCompiled;

  this.data = data;
  this.props = props;
  this.children = children;
  this.parent = parent;
  this.listeners = data.on || emptyObject;
  this.injections = resolveInject(options.inject, parent);
  this.slots = function () {
    if (!this$1.$slots) {
      normalizeScopedSlots(
        data.scopedSlots,
        this$1.$slots = resolveSlots(children, parent)
      );
    }
    return this$1.$slots
  };

  Object.defineProperty(this, 'scopedSlots', ({
    enumerable: true,
    get: function get () {
      return normalizeScopedSlots(data.scopedSlots, this.slots())
    }
  }));

  // support for compiled functional template
  if (isCompiled) {
    // exposing $options for renderStatic()
    this.$options = options;
    // pre-resolve slots for renderSlot()
    this.$slots = this.slots();
    this.$scopedSlots = normalizeScopedSlots(data.scopedSlots, this.$slots);
  }

  if (options._scopeId) {
    this._c = function (a, b, c, d) {
      var vnode = createElement(contextVm, a, b, c, d, needNormalization);
      if (vnode && !Array.isArray(vnode)) {
        vnode.fnScopeId = options._scopeId;
        vnode.fnContext = parent;
      }
      return vnode
    };
  } else {
    this._c = function (a, b, c, d) { return createElement(contextVm, a, b, c, d, needNormalization); };
  }
}

installRenderHelpers(FunctionalRenderContext.prototype);

function createFunctionalComponent (
  Ctor,
  propsData,
  data,
  contextVm,
  children
) {
  var options = Ctor.options;
  var props = {};
  var propOptions = options.props;
  if (isDef(propOptions)) {
    for (var key in propOptions) {
      props[key] = validateProp(key, propOptions, propsData || emptyObject);
    }
  } else {
    if (isDef(data.attrs)) { mergeProps(props, data.attrs); }
    if (isDef(data.props)) { mergeProps(props, data.props); }
  }

  var renderContext = new FunctionalRenderContext(
    data,
    props,
    children,
    contextVm,
    Ctor
  );

  var vnode = options.render.call(null, renderContext._c, renderContext);

  if (vnode instanceof VNode) {
    return cloneAndMarkFunctionalResult(vnode, data, renderContext.parent, options, renderContext)
  } else if (Array.isArray(vnode)) {
    var vnodes = normalizeChildren(vnode) || [];
    var res = new Array(vnodes.length);
    for (var i = 0; i < vnodes.length; i++) {
      res[i] = cloneAndMarkFunctionalResult(vnodes[i], data, renderContext.parent, options, renderContext);
    }
    return res
  }
}

function cloneAndMarkFunctionalResult (vnode, data, contextVm, options, renderContext) {
  // #7817 clone node before setting fnContext, otherwise if the node is reused
  // (e.g. it was from a cached normal slot) the fnContext causes named slots
  // that should not be matched to match.
  var clone = cloneVNode(vnode);
  clone.fnContext = contextVm;
  clone.fnOptions = options;
  if (true) {
    (clone.devtoolsMeta = clone.devtoolsMeta || {}).renderContext = renderContext;
  }
  if (data.slot) {
    (clone.data || (clone.data = {})).slot = data.slot;
  }
  return clone
}

function mergeProps (to, from) {
  for (var key in from) {
    to[camelize(key)] = from[key];
  }
}

/*  */

/*  */

/*  */

/*  */

// inline hooks to be invoked on component VNodes during patch
var componentVNodeHooks = {
  init: function init (vnode, hydrating) {
    if (
      vnode.componentInstance &&
      !vnode.componentInstance._isDestroyed &&
      vnode.data.keepAlive
    ) {
      // kept-alive components, treat as a patch
      var mountedNode = vnode; // work around flow
      componentVNodeHooks.prepatch(mountedNode, mountedNode);
    } else {
      var child = vnode.componentInstance = createComponentInstanceForVnode(
        vnode,
        activeInstance
      );
      child.$mount(hydrating ? vnode.elm : undefined, hydrating);
    }
  },

  prepatch: function prepatch (oldVnode, vnode) {
    var options = vnode.componentOptions;
    var child = vnode.componentInstance = oldVnode.componentInstance;
    updateChildComponent(
      child,
      options.propsData, // updated props
      options.listeners, // updated listeners
      vnode, // new parent vnode
      options.children // new children
    );
  },

  insert: function insert (vnode) {
    var context = vnode.context;
    var componentInstance = vnode.componentInstance;
    if (!componentInstance._isMounted) {
      callHook(componentInstance, 'onServiceCreated');
      callHook(componentInstance, 'onServiceAttached');
      componentInstance._isMounted = true;
      callHook(componentInstance, 'mounted');
    }
    if (vnode.data.keepAlive) {
      if (context._isMounted) {
        // vue-router#1212
        // During updates, a kept-alive component's child components may
        // change, so directly walking the tree here may call activated hooks
        // on incorrect children. Instead we push them into a queue which will
        // be processed after the whole patch process ended.
        queueActivatedComponent(componentInstance);
      } else {
        activateChildComponent(componentInstance, true /* direct */);
      }
    }
  },

  destroy: function destroy (vnode) {
    var componentInstance = vnode.componentInstance;
    if (!componentInstance._isDestroyed) {
      if (!vnode.data.keepAlive) {
        componentInstance.$destroy();
      } else {
        deactivateChildComponent(componentInstance, true /* direct */);
      }
    }
  }
};

var hooksToMerge = Object.keys(componentVNodeHooks);

function createComponent (
  Ctor,
  data,
  context,
  children,
  tag
) {
  if (isUndef(Ctor)) {
    return
  }

  var baseCtor = context.$options._base;

  // plain options object: turn it into a constructor
  if (isObject(Ctor)) {
    Ctor = baseCtor.extend(Ctor);
  }

  // if at this stage it's not a constructor or an async component factory,
  // reject.
  if (typeof Ctor !== 'function') {
    if (true) {
      warn(("Invalid Component definition: " + (String(Ctor))), context);
    }
    return
  }

  // async component
  var asyncFactory;
  if (isUndef(Ctor.cid)) {
    asyncFactory = Ctor;
    Ctor = resolveAsyncComponent(asyncFactory, baseCtor);
    if (Ctor === undefined) {
      // return a placeholder node for async component, which is rendered
      // as a comment node but preserves all the raw information for the node.
      // the information will be used for async server-rendering and hydration.
      return createAsyncPlaceholder(
        asyncFactory,
        data,
        context,
        children,
        tag
      )
    }
  }

  data = data || {};

  // resolve constructor options in case global mixins are applied after
  // component constructor creation
  resolveConstructorOptions(Ctor);

  // transform component v-model data into props & events
  if (isDef(data.model)) {
    transformModel(Ctor.options, data);
  }

  // extract props
  var propsData = extractPropsFromVNodeData(data, Ctor, tag, context); // fixed by xxxxxx

  // functional component
  if (isTrue(Ctor.options.functional)) {
    return createFunctionalComponent(Ctor, propsData, data, context, children)
  }

  // extract listeners, since these needs to be treated as
  // child component listeners instead of DOM listeners
  var listeners = data.on;
  // replace with listeners with .native modifier
  // so it gets processed during parent component patch.
  data.on = data.nativeOn;

  if (isTrue(Ctor.options.abstract)) {
    // abstract components do not keep anything
    // other than props & listeners & slot

    // work around flow
    var slot = data.slot;
    data = {};
    if (slot) {
      data.slot = slot;
    }
  }

  // install component management hooks onto the placeholder node
  installComponentHooks(data);

  // return a placeholder vnode
  var name = Ctor.options.name || tag;
  var vnode = new VNode(
    ("vue-component-" + (Ctor.cid) + (name ? ("-" + name) : '')),
    data, undefined, undefined, undefined, context,
    { Ctor: Ctor, propsData: propsData, listeners: listeners, tag: tag, children: children },
    asyncFactory
  );

  return vnode
}

function createComponentInstanceForVnode (
  vnode, // we know it's MountedComponentVNode but flow doesn't
  parent // activeInstance in lifecycle state
) {
  var options = {
    _isComponent: true,
    _parentVnode: vnode,
    parent: parent
  };
  // check inline-template render functions
  var inlineTemplate = vnode.data.inlineTemplate;
  if (isDef(inlineTemplate)) {
    options.render = inlineTemplate.render;
    options.staticRenderFns = inlineTemplate.staticRenderFns;
  }
  return new vnode.componentOptions.Ctor(options)
}

function installComponentHooks (data) {
  var hooks = data.hook || (data.hook = {});
  for (var i = 0; i < hooksToMerge.length; i++) {
    var key = hooksToMerge[i];
    var existing = hooks[key];
    var toMerge = componentVNodeHooks[key];
    if (existing !== toMerge && !(existing && existing._merged)) {
      hooks[key] = existing ? mergeHook$1(toMerge, existing) : toMerge;
    }
  }
}

function mergeHook$1 (f1, f2) {
  var merged = function (a, b) {
    // flow complains about extra args which is why we use any
    f1(a, b);
    f2(a, b);
  };
  merged._merged = true;
  return merged
}

// transform component v-model info (value and callback) into
// prop and event handler respectively.
function transformModel (options, data) {
  var prop = (options.model && options.model.prop) || 'value';
  var event = (options.model && options.model.event) || 'input'
  ;(data.attrs || (data.attrs = {}))[prop] = data.model.value;
  var on = data.on || (data.on = {});
  var existing = on[event];
  var callback = data.model.callback;
  if (isDef(existing)) {
    if (
      Array.isArray(existing)
        ? existing.indexOf(callback) === -1
        : existing !== callback
    ) {
      on[event] = [callback].concat(existing);
    }
  } else {
    on[event] = callback;
  }
}

/*  */

var SIMPLE_NORMALIZE = 1;
var ALWAYS_NORMALIZE = 2;

// wrapper function for providing a more flexible interface
// without getting yelled at by flow
function createElement (
  context,
  tag,
  data,
  children,
  normalizationType,
  alwaysNormalize
) {
  if (Array.isArray(data) || isPrimitive(data)) {
    normalizationType = children;
    children = data;
    data = undefined;
  }
  if (isTrue(alwaysNormalize)) {
    normalizationType = ALWAYS_NORMALIZE;
  }
  return _createElement(context, tag, data, children, normalizationType)
}

function _createElement (
  context,
  tag,
  data,
  children,
  normalizationType
) {
  if (isDef(data) && isDef((data).__ob__)) {
     true && warn(
      "Avoid using observed data object as vnode data: " + (JSON.stringify(data)) + "\n" +
      'Always create fresh vnode data objects in each render!',
      context
    );
    return createEmptyVNode()
  }
  // object syntax in v-bind
  if (isDef(data) && isDef(data.is)) {
    tag = data.is;
  }
  if (!tag) {
    // in case of component :is set to falsy value
    return createEmptyVNode()
  }
  // warn against non-primitive key
  if ( true &&
    isDef(data) && isDef(data.key) && !isPrimitive(data.key)
  ) {
    {
      warn(
        'Avoid using non-primitive value as key, ' +
        'use string/number value instead.',
        context
      );
    }
  }
  // support single function children as default scoped slot
  if (Array.isArray(children) &&
    typeof children[0] === 'function'
  ) {
    data = data || {};
    data.scopedSlots = { default: children[0] };
    children.length = 0;
  }
  if (normalizationType === ALWAYS_NORMALIZE) {
    children = normalizeChildren(children);
  } else if (normalizationType === SIMPLE_NORMALIZE) {
    children = simpleNormalizeChildren(children);
  }
  var vnode, ns;
  if (typeof tag === 'string') {
    var Ctor;
    ns = (context.$vnode && context.$vnode.ns) || config.getTagNamespace(tag);
    if (config.isReservedTag(tag)) {
      // platform built-in elements
      if ( true && isDef(data) && isDef(data.nativeOn)) {
        warn(
          ("The .native modifier for v-on is only valid on components but it was used on <" + tag + ">."),
          context
        );
      }
      vnode = new VNode(
        config.parsePlatformTagName(tag), data, children,
        undefined, undefined, context
      );
    } else if ((!data || !data.pre) && isDef(Ctor = resolveAsset(context.$options, 'components', tag))) {
      // component
      vnode = createComponent(Ctor, data, context, children, tag);
    } else {
      // unknown or unlisted namespaced elements
      // check at runtime because it may get assigned a namespace when its
      // parent normalizes children
      vnode = new VNode(
        tag, data, children,
        undefined, undefined, context
      );
    }
  } else {
    // direct component options / constructor
    vnode = createComponent(tag, data, context, children);
  }
  if (Array.isArray(vnode)) {
    return vnode
  } else if (isDef(vnode)) {
    if (isDef(ns)) { applyNS(vnode, ns); }
    if (isDef(data)) { registerDeepBindings(data); }
    return vnode
  } else {
    return createEmptyVNode()
  }
}

function applyNS (vnode, ns, force) {
  vnode.ns = ns;
  if (vnode.tag === 'foreignObject') {
    // use default namespace inside foreignObject
    ns = undefined;
    force = true;
  }
  if (isDef(vnode.children)) {
    for (var i = 0, l = vnode.children.length; i < l; i++) {
      var child = vnode.children[i];
      if (isDef(child.tag) && (
        isUndef(child.ns) || (isTrue(force) && child.tag !== 'svg'))) {
        applyNS(child, ns, force);
      }
    }
  }
}

// ref #5318
// necessary to ensure parent re-render when deep bindings like :style and
// :class are used on slot nodes
function registerDeepBindings (data) {
  if (isObject(data.style)) {
    traverse(data.style);
  }
  if (isObject(data.class)) {
    traverse(data.class);
  }
}

/*  */

function initRender (vm) {
  vm._vnode = null; // the root of the child tree
  vm._staticTrees = null; // v-once cached trees
  var options = vm.$options;
  var parentVnode = vm.$vnode = options._parentVnode; // the placeholder node in parent tree
  var renderContext = parentVnode && parentVnode.context;
  vm.$slots = resolveSlots(options._renderChildren, renderContext);
  vm.$scopedSlots = emptyObject;
  // bind the createElement fn to this instance
  // so that we get proper render context inside it.
  // args order: tag, data, children, normalizationType, alwaysNormalize
  // internal version is used by render functions compiled from templates
  vm._c = function (a, b, c, d) { return createElement(vm, a, b, c, d, false); };
  // normalization is always applied for the public version, used in
  // user-written render functions.
  vm.$createElement = function (a, b, c, d) { return createElement(vm, a, b, c, d, true); };

  // $attrs & $listeners are exposed for easier HOC creation.
  // they need to be reactive so that HOCs using them are always updated
  var parentData = parentVnode && parentVnode.data;

  /* istanbul ignore else */
  if (true) {
    defineReactive$$1(vm, '$attrs', parentData && parentData.attrs || emptyObject, function () {
      !isUpdatingChildComponent && warn("$attrs is readonly.", vm);
    }, true);
    defineReactive$$1(vm, '$listeners', options._parentListeners || emptyObject, function () {
      !isUpdatingChildComponent && warn("$listeners is readonly.", vm);
    }, true);
  } else {}
}

var currentRenderingInstance = null;

function renderMixin (Vue) {
  // install runtime convenience helpers
  installRenderHelpers(Vue.prototype);

  Vue.prototype.$nextTick = function (fn) {
    return nextTick(fn, this)
  };

  Vue.prototype._render = function () {
    var vm = this;
    var ref = vm.$options;
    var render = ref.render;
    var _parentVnode = ref._parentVnode;

    if (_parentVnode) {
      vm.$scopedSlots = normalizeScopedSlots(
        _parentVnode.data.scopedSlots,
        vm.$slots,
        vm.$scopedSlots
      );
    }

    // set parent vnode. this allows render functions to have access
    // to the data on the placeholder node.
    vm.$vnode = _parentVnode;
    // render self
    var vnode;
    try {
      // There's no need to maintain a stack because all render fns are called
      // separately from one another. Nested component's render fns are called
      // when parent component is patched.
      currentRenderingInstance = vm;
      vnode = render.call(vm._renderProxy, vm.$createElement);
    } catch (e) {
      handleError(e, vm, "render");
      // return error render result,
      // or previous vnode to prevent render error causing blank component
      /* istanbul ignore else */
      if ( true && vm.$options.renderError) {
        try {
          vnode = vm.$options.renderError.call(vm._renderProxy, vm.$createElement, e);
        } catch (e) {
          handleError(e, vm, "renderError");
          vnode = vm._vnode;
        }
      } else {
        vnode = vm._vnode;
      }
    } finally {
      currentRenderingInstance = null;
    }
    // if the returned array contains only a single node, allow it
    if (Array.isArray(vnode) && vnode.length === 1) {
      vnode = vnode[0];
    }
    // return empty vnode in case the render function errored out
    if (!(vnode instanceof VNode)) {
      if ( true && Array.isArray(vnode)) {
        warn(
          'Multiple root nodes returned from render function. Render function ' +
          'should return a single root node.',
          vm
        );
      }
      vnode = createEmptyVNode();
    }
    // set parent
    vnode.parent = _parentVnode;
    return vnode
  };
}

/*  */

function ensureCtor (comp, base) {
  if (
    comp.__esModule ||
    (hasSymbol && comp[Symbol.toStringTag] === 'Module')
  ) {
    comp = comp.default;
  }
  return isObject(comp)
    ? base.extend(comp)
    : comp
}

function createAsyncPlaceholder (
  factory,
  data,
  context,
  children,
  tag
) {
  var node = createEmptyVNode();
  node.asyncFactory = factory;
  node.asyncMeta = { data: data, context: context, children: children, tag: tag };
  return node
}

function resolveAsyncComponent (
  factory,
  baseCtor
) {
  if (isTrue(factory.error) && isDef(factory.errorComp)) {
    return factory.errorComp
  }

  if (isDef(factory.resolved)) {
    return factory.resolved
  }

  var owner = currentRenderingInstance;
  if (owner && isDef(factory.owners) && factory.owners.indexOf(owner) === -1) {
    // already pending
    factory.owners.push(owner);
  }

  if (isTrue(factory.loading) && isDef(factory.loadingComp)) {
    return factory.loadingComp
  }

  if (owner && !isDef(factory.owners)) {
    var owners = factory.owners = [owner];
    var sync = true;
    var timerLoading = null;
    var timerTimeout = null

    ;(owner).$on('hook:destroyed', function () { return remove(owners, owner); });

    var forceRender = function (renderCompleted) {
      for (var i = 0, l = owners.length; i < l; i++) {
        (owners[i]).$forceUpdate();
      }

      if (renderCompleted) {
        owners.length = 0;
        if (timerLoading !== null) {
          clearTimeout(timerLoading);
          timerLoading = null;
        }
        if (timerTimeout !== null) {
          clearTimeout(timerTimeout);
          timerTimeout = null;
        }
      }
    };

    var resolve = once(function (res) {
      // cache resolved
      factory.resolved = ensureCtor(res, baseCtor);
      // invoke callbacks only if this is not a synchronous resolve
      // (async resolves are shimmed as synchronous during SSR)
      if (!sync) {
        forceRender(true);
      } else {
        owners.length = 0;
      }
    });

    var reject = once(function (reason) {
       true && warn(
        "Failed to resolve async component: " + (String(factory)) +
        (reason ? ("\nReason: " + reason) : '')
      );
      if (isDef(factory.errorComp)) {
        factory.error = true;
        forceRender(true);
      }
    });

    var res = factory(resolve, reject);

    if (isObject(res)) {
      if (isPromise(res)) {
        // () => Promise
        if (isUndef(factory.resolved)) {
          res.then(resolve, reject);
        }
      } else if (isPromise(res.component)) {
        res.component.then(resolve, reject);

        if (isDef(res.error)) {
          factory.errorComp = ensureCtor(res.error, baseCtor);
        }

        if (isDef(res.loading)) {
          factory.loadingComp = ensureCtor(res.loading, baseCtor);
          if (res.delay === 0) {
            factory.loading = true;
          } else {
            timerLoading = setTimeout(function () {
              timerLoading = null;
              if (isUndef(factory.resolved) && isUndef(factory.error)) {
                factory.loading = true;
                forceRender(false);
              }
            }, res.delay || 200);
          }
        }

        if (isDef(res.timeout)) {
          timerTimeout = setTimeout(function () {
            timerTimeout = null;
            if (isUndef(factory.resolved)) {
              reject(
                 true
                  ? ("timeout (" + (res.timeout) + "ms)")
                  : undefined
              );
            }
          }, res.timeout);
        }
      }
    }

    sync = false;
    // return in case resolved synchronously
    return factory.loading
      ? factory.loadingComp
      : factory.resolved
  }
}

/*  */

function isAsyncPlaceholder (node) {
  return node.isComment && node.asyncFactory
}

/*  */

function getFirstComponentChild (children) {
  if (Array.isArray(children)) {
    for (var i = 0; i < children.length; i++) {
      var c = children[i];
      if (isDef(c) && (isDef(c.componentOptions) || isAsyncPlaceholder(c))) {
        return c
      }
    }
  }
}

/*  */

/*  */

function initEvents (vm) {
  vm._events = Object.create(null);
  vm._hasHookEvent = false;
  // init parent attached events
  var listeners = vm.$options._parentListeners;
  if (listeners) {
    updateComponentListeners(vm, listeners);
  }
}

var target;

function add (event, fn) {
  target.$on(event, fn);
}

function remove$1 (event, fn) {
  target.$off(event, fn);
}

function createOnceHandler (event, fn) {
  var _target = target;
  return function onceHandler () {
    var res = fn.apply(null, arguments);
    if (res !== null) {
      _target.$off(event, onceHandler);
    }
  }
}

function updateComponentListeners (
  vm,
  listeners,
  oldListeners
) {
  target = vm;
  updateListeners(listeners, oldListeners || {}, add, remove$1, createOnceHandler, vm);
  target = undefined;
}

function eventsMixin (Vue) {
  var hookRE = /^hook:/;
  Vue.prototype.$on = function (event, fn) {
    var vm = this;
    if (Array.isArray(event)) {
      for (var i = 0, l = event.length; i < l; i++) {
        vm.$on(event[i], fn);
      }
    } else {
      (vm._events[event] || (vm._events[event] = [])).push(fn);
      // optimize hook:event cost by using a boolean flag marked at registration
      // instead of a hash lookup
      if (hookRE.test(event)) {
        vm._hasHookEvent = true;
      }
    }
    return vm
  };

  Vue.prototype.$once = function (event, fn) {
    var vm = this;
    function on () {
      vm.$off(event, on);
      fn.apply(vm, arguments);
    }
    on.fn = fn;
    vm.$on(event, on);
    return vm
  };

  Vue.prototype.$off = function (event, fn) {
    var vm = this;
    // all
    if (!arguments.length) {
      vm._events = Object.create(null);
      return vm
    }
    // array of events
    if (Array.isArray(event)) {
      for (var i$1 = 0, l = event.length; i$1 < l; i$1++) {
        vm.$off(event[i$1], fn);
      }
      return vm
    }
    // specific event
    var cbs = vm._events[event];
    if (!cbs) {
      return vm
    }
    if (!fn) {
      vm._events[event] = null;
      return vm
    }
    // specific handler
    var cb;
    var i = cbs.length;
    while (i--) {
      cb = cbs[i];
      if (cb === fn || cb.fn === fn) {
        cbs.splice(i, 1);
        break
      }
    }
    return vm
  };

  Vue.prototype.$emit = function (event) {
    var vm = this;
    if (true) {
      var lowerCaseEvent = event.toLowerCase();
      if (lowerCaseEvent !== event && vm._events[lowerCaseEvent]) {
        tip(
          "Event \"" + lowerCaseEvent + "\" is emitted in component " +
          (formatComponentName(vm)) + " but the handler is registered for \"" + event + "\". " +
          "Note that HTML attributes are case-insensitive and you cannot use " +
          "v-on to listen to camelCase events when using in-DOM templates. " +
          "You should probably use \"" + (hyphenate(event)) + "\" instead of \"" + event + "\"."
        );
      }
    }
    var cbs = vm._events[event];
    if (cbs) {
      cbs = cbs.length > 1 ? toArray(cbs) : cbs;
      var args = toArray(arguments, 1);
      var info = "event handler for \"" + event + "\"";
      for (var i = 0, l = cbs.length; i < l; i++) {
        invokeWithErrorHandling(cbs[i], vm, args, vm, info);
      }
    }
    return vm
  };
}

/*  */

var activeInstance = null;
var isUpdatingChildComponent = false;

function setActiveInstance(vm) {
  var prevActiveInstance = activeInstance;
  activeInstance = vm;
  return function () {
    activeInstance = prevActiveInstance;
  }
}

function initLifecycle (vm) {
  var options = vm.$options;

  // locate first non-abstract parent
  var parent = options.parent;
  if (parent && !options.abstract) {
    while (parent.$options.abstract && parent.$parent) {
      parent = parent.$parent;
    }
    parent.$children.push(vm);
  }

  vm.$parent = parent;
  vm.$root = parent ? parent.$root : vm;

  vm.$children = [];
  vm.$refs = {};

  vm._watcher = null;
  vm._inactive = null;
  vm._directInactive = false;
  vm._isMounted = false;
  vm._isDestroyed = false;
  vm._isBeingDestroyed = false;
}

function lifecycleMixin (Vue) {
  Vue.prototype._update = function (vnode, hydrating) {
    var vm = this;
    var prevEl = vm.$el;
    var prevVnode = vm._vnode;
    var restoreActiveInstance = setActiveInstance(vm);
    vm._vnode = vnode;
    // Vue.prototype.__patch__ is injected in entry points
    // based on the rendering backend used.
    if (!prevVnode) {
      // initial render
      vm.$el = vm.__patch__(vm.$el, vnode, hydrating, false /* removeOnly */);
    } else {
      // updates
      vm.$el = vm.__patch__(prevVnode, vnode);
    }
    restoreActiveInstance();
    // update __vue__ reference
    if (prevEl) {
      prevEl.__vue__ = null;
    }
    if (vm.$el) {
      vm.$el.__vue__ = vm;
    }
    // if parent is an HOC, update its $el as well
    if (vm.$vnode && vm.$parent && vm.$vnode === vm.$parent._vnode) {
      vm.$parent.$el = vm.$el;
    }
    // updated hook is called by the scheduler to ensure that children are
    // updated in a parent's updated hook.
  };

  Vue.prototype.$forceUpdate = function () {
    var vm = this;
    if (vm._watcher) {
      vm._watcher.update();
    }
  };

  Vue.prototype.$destroy = function () {
    var vm = this;
    if (vm._isBeingDestroyed) {
      return
    }
    callHook(vm, 'beforeDestroy');
    vm._isBeingDestroyed = true;
    // remove self from parent
    var parent = vm.$parent;
    if (parent && !parent._isBeingDestroyed && !vm.$options.abstract) {
      remove(parent.$children, vm);
    }
    // teardown watchers
    if (vm._watcher) {
      vm._watcher.teardown();
    }
    var i = vm._watchers.length;
    while (i--) {
      vm._watchers[i].teardown();
    }
    // remove reference from data ob
    // frozen object may not have observer.
    if (vm._data.__ob__) {
      vm._data.__ob__.vmCount--;
    }
    // call the last hook...
    vm._isDestroyed = true;
    // invoke destroy hooks on current rendered tree
    vm.__patch__(vm._vnode, null);
    // fire destroyed hook
    callHook(vm, 'destroyed');
    // turn off all instance listeners.
    vm.$off();
    // remove __vue__ reference
    if (vm.$el) {
      vm.$el.__vue__ = null;
    }
    // release circular reference (#6759)
    if (vm.$vnode) {
      vm.$vnode.parent = null;
    }
  };
}

function updateChildComponent (
  vm,
  propsData,
  listeners,
  parentVnode,
  renderChildren
) {
  if (true) {
    isUpdatingChildComponent = true;
  }

  // determine whether component has slot children
  // we need to do this before overwriting $options._renderChildren.

  // check if there are dynamic scopedSlots (hand-written or compiled but with
  // dynamic slot names). Static scoped slots compiled from template has the
  // "$stable" marker.
  var newScopedSlots = parentVnode.data.scopedSlots;
  var oldScopedSlots = vm.$scopedSlots;
  var hasDynamicScopedSlot = !!(
    (newScopedSlots && !newScopedSlots.$stable) ||
    (oldScopedSlots !== emptyObject && !oldScopedSlots.$stable) ||
    (newScopedSlots && vm.$scopedSlots.$key !== newScopedSlots.$key)
  );

  // Any static slot children from the parent may have changed during parent's
  // update. Dynamic scoped slots may also have changed. In such cases, a forced
  // update is necessary to ensure correctness.
  var needsForceUpdate = !!(
    renderChildren ||               // has new static slots
    vm.$options._renderChildren ||  // has old static slots
    hasDynamicScopedSlot
  );

  vm.$options._parentVnode = parentVnode;
  vm.$vnode = parentVnode; // update vm's placeholder node without re-render

  if (vm._vnode) { // update child tree's parent
    vm._vnode.parent = parentVnode;
  }
  vm.$options._renderChildren = renderChildren;

  // update $attrs and $listeners hash
  // these are also reactive so they may trigger child update if the child
  // used them during render
  vm.$attrs = parentVnode.data.attrs || emptyObject;
  vm.$listeners = listeners || emptyObject;

  // update props
  if (propsData && vm.$options.props) {
    toggleObserving(false);
    var props = vm._props;
    var propKeys = vm.$options._propKeys || [];
    for (var i = 0; i < propKeys.length; i++) {
      var key = propKeys[i];
      var propOptions = vm.$options.props; // wtf flow?
      props[key] = validateProp(key, propOptions, propsData, vm);
    }
    toggleObserving(true);
    // keep a copy of raw propsData
    vm.$options.propsData = propsData;
  }
  
  // fixed by xxxxxx update properties(mp runtime)
  vm._$updateProperties && vm._$updateProperties(vm);
  
  // update listeners
  listeners = listeners || emptyObject;
  var oldListeners = vm.$options._parentListeners;
  vm.$options._parentListeners = listeners;
  updateComponentListeners(vm, listeners, oldListeners);

  // resolve slots + force update if has children
  if (needsForceUpdate) {
    vm.$slots = resolveSlots(renderChildren, parentVnode.context);
    vm.$forceUpdate();
  }

  if (true) {
    isUpdatingChildComponent = false;
  }
}

function isInInactiveTree (vm) {
  while (vm && (vm = vm.$parent)) {
    if (vm._inactive) { return true }
  }
  return false
}

function activateChildComponent (vm, direct) {
  if (direct) {
    vm._directInactive = false;
    if (isInInactiveTree(vm)) {
      return
    }
  } else if (vm._directInactive) {
    return
  }
  if (vm._inactive || vm._inactive === null) {
    vm._inactive = false;
    for (var i = 0; i < vm.$children.length; i++) {
      activateChildComponent(vm.$children[i]);
    }
    callHook(vm, 'activated');
  }
}

function deactivateChildComponent (vm, direct) {
  if (direct) {
    vm._directInactive = true;
    if (isInInactiveTree(vm)) {
      return
    }
  }
  if (!vm._inactive) {
    vm._inactive = true;
    for (var i = 0; i < vm.$children.length; i++) {
      deactivateChildComponent(vm.$children[i]);
    }
    callHook(vm, 'deactivated');
  }
}

function callHook (vm, hook) {
  // #7573 disable dep collection when invoking lifecycle hooks
  pushTarget();
  var handlers = vm.$options[hook];
  var info = hook + " hook";
  if (handlers) {
    for (var i = 0, j = handlers.length; i < j; i++) {
      invokeWithErrorHandling(handlers[i], vm, null, vm, info);
    }
  }
  if (vm._hasHookEvent) {
    vm.$emit('hook:' + hook);
  }
  popTarget();
}

/*  */

var MAX_UPDATE_COUNT = 100;

var queue = [];
var activatedChildren = [];
var has = {};
var circular = {};
var waiting = false;
var flushing = false;
var index = 0;

/**
 * Reset the scheduler's state.
 */
function resetSchedulerState () {
  index = queue.length = activatedChildren.length = 0;
  has = {};
  if (true) {
    circular = {};
  }
  waiting = flushing = false;
}

// Async edge case #6566 requires saving the timestamp when event listeners are
// attached. However, calling performance.now() has a perf overhead especially
// if the page has thousands of event listeners. Instead, we take a timestamp
// every time the scheduler flushes and use that for all event listeners
// attached during that flush.
var currentFlushTimestamp = 0;

// Async edge case fix requires storing an event listener's attach timestamp.
var getNow = Date.now;

// Determine what event timestamp the browser is using. Annoyingly, the
// timestamp can either be hi-res (relative to page load) or low-res
// (relative to UNIX epoch), so in order to compare time we have to use the
// same timestamp type when saving the flush timestamp.
// All IE versions use low-res event timestamps, and have problematic clock
// implementations (#9632)
if (inBrowser && !isIE) {
  var performance = window.performance;
  if (
    performance &&
    typeof performance.now === 'function' &&
    getNow() > document.createEvent('Event').timeStamp
  ) {
    // if the event timestamp, although evaluated AFTER the Date.now(), is
    // smaller than it, it means the event is using a hi-res timestamp,
    // and we need to use the hi-res version for event listener timestamps as
    // well.
    getNow = function () { return performance.now(); };
  }
}

/**
 * Flush both queues and run the watchers.
 */
function flushSchedulerQueue () {
  currentFlushTimestamp = getNow();
  flushing = true;
  var watcher, id;

  // Sort queue before flush.
  // This ensures that:
  // 1. Components are updated from parent to child. (because parent is always
  //    created before the child)
  // 2. A component's user watchers are run before its render watcher (because
  //    user watchers are created before the render watcher)
  // 3. If a component is destroyed during a parent component's watcher run,
  //    its watchers can be skipped.
  queue.sort(function (a, b) { return a.id - b.id; });

  // do not cache length because more watchers might be pushed
  // as we run existing watchers
  for (index = 0; index < queue.length; index++) {
    watcher = queue[index];
    if (watcher.before) {
      watcher.before();
    }
    id = watcher.id;
    has[id] = null;
    watcher.run();
    // in dev build, check and stop circular updates.
    if ( true && has[id] != null) {
      circular[id] = (circular[id] || 0) + 1;
      if (circular[id] > MAX_UPDATE_COUNT) {
        warn(
          'You may have an infinite update loop ' + (
            watcher.user
              ? ("in watcher with expression \"" + (watcher.expression) + "\"")
              : "in a component render function."
          ),
          watcher.vm
        );
        break
      }
    }
  }

  // keep copies of post queues before resetting state
  var activatedQueue = activatedChildren.slice();
  var updatedQueue = queue.slice();

  resetSchedulerState();

  // call component updated and activated hooks
  callActivatedHooks(activatedQueue);
  callUpdatedHooks(updatedQueue);

  // devtool hook
  /* istanbul ignore if */
  if (devtools && config.devtools) {
    devtools.emit('flush');
  }
}

function callUpdatedHooks (queue) {
  var i = queue.length;
  while (i--) {
    var watcher = queue[i];
    var vm = watcher.vm;
    if (vm._watcher === watcher && vm._isMounted && !vm._isDestroyed) {
      callHook(vm, 'updated');
    }
  }
}

/**
 * Queue a kept-alive component that was activated during patch.
 * The queue will be processed after the entire tree has been patched.
 */
function queueActivatedComponent (vm) {
  // setting _inactive to false here so that a render function can
  // rely on checking whether it's in an inactive tree (e.g. router-view)
  vm._inactive = false;
  activatedChildren.push(vm);
}

function callActivatedHooks (queue) {
  for (var i = 0; i < queue.length; i++) {
    queue[i]._inactive = true;
    activateChildComponent(queue[i], true /* true */);
  }
}

/**
 * Push a watcher into the watcher queue.
 * Jobs with duplicate IDs will be skipped unless it's
 * pushed when the queue is being flushed.
 */
function queueWatcher (watcher) {
  var id = watcher.id;
  if (has[id] == null) {
    has[id] = true;
    if (!flushing) {
      queue.push(watcher);
    } else {
      // if already flushing, splice the watcher based on its id
      // if already past its id, it will be run next immediately.
      var i = queue.length - 1;
      while (i > index && queue[i].id > watcher.id) {
        i--;
      }
      queue.splice(i + 1, 0, watcher);
    }
    // queue the flush
    if (!waiting) {
      waiting = true;

      if ( true && !config.async) {
        flushSchedulerQueue();
        return
      }
      nextTick(flushSchedulerQueue);
    }
  }
}

/*  */



var uid$2 = 0;

/**
 * A watcher parses an expression, collects dependencies,
 * and fires callback when the expression value changes.
 * This is used for both the $watch() api and directives.
 */
var Watcher = function Watcher (
  vm,
  expOrFn,
  cb,
  options,
  isRenderWatcher
) {
  this.vm = vm;
  if (isRenderWatcher) {
    vm._watcher = this;
  }
  vm._watchers.push(this);
  // options
  if (options) {
    this.deep = !!options.deep;
    this.user = !!options.user;
    this.lazy = !!options.lazy;
    this.sync = !!options.sync;
    this.before = options.before;
  } else {
    this.deep = this.user = this.lazy = this.sync = false;
  }
  this.cb = cb;
  this.id = ++uid$2; // uid for batching
  this.active = true;
  this.dirty = this.lazy; // for lazy watchers
  this.deps = [];
  this.newDeps = [];
  this.depIds = new _Set();
  this.newDepIds = new _Set();
  this.expression =  true
    ? expOrFn.toString()
    : undefined;
  // parse expression for getter
  if (typeof expOrFn === 'function') {
    this.getter = expOrFn;
  } else {
    this.getter = parsePath(expOrFn);
    if (!this.getter) {
      this.getter = noop;
       true && warn(
        "Failed watching path: \"" + expOrFn + "\" " +
        'Watcher only accepts simple dot-delimited paths. ' +
        'For full control, use a function instead.',
        vm
      );
    }
  }
  this.value = this.lazy
    ? undefined
    : this.get();
};

/**
 * Evaluate the getter, and re-collect dependencies.
 */
Watcher.prototype.get = function get () {
  pushTarget(this);
  var value;
  var vm = this.vm;
  try {
    value = this.getter.call(vm, vm);
  } catch (e) {
    if (this.user) {
      handleError(e, vm, ("getter for watcher \"" + (this.expression) + "\""));
    } else {
      throw e
    }
  } finally {
    // "touch" every property so they are all tracked as
    // dependencies for deep watching
    if (this.deep) {
      traverse(value);
    }
    popTarget();
    this.cleanupDeps();
  }
  return value
};

/**
 * Add a dependency to this directive.
 */
Watcher.prototype.addDep = function addDep (dep) {
  var id = dep.id;
  if (!this.newDepIds.has(id)) {
    this.newDepIds.add(id);
    this.newDeps.push(dep);
    if (!this.depIds.has(id)) {
      dep.addSub(this);
    }
  }
};

/**
 * Clean up for dependency collection.
 */
Watcher.prototype.cleanupDeps = function cleanupDeps () {
  var i = this.deps.length;
  while (i--) {
    var dep = this.deps[i];
    if (!this.newDepIds.has(dep.id)) {
      dep.removeSub(this);
    }
  }
  var tmp = this.depIds;
  this.depIds = this.newDepIds;
  this.newDepIds = tmp;
  this.newDepIds.clear();
  tmp = this.deps;
  this.deps = this.newDeps;
  this.newDeps = tmp;
  this.newDeps.length = 0;
};

/**
 * Subscriber interface.
 * Will be called when a dependency changes.
 */
Watcher.prototype.update = function update () {
  /* istanbul ignore else */
  if (this.lazy) {
    this.dirty = true;
  } else if (this.sync) {
    this.run();
  } else {
    queueWatcher(this);
  }
};

/**
 * Scheduler job interface.
 * Will be called by the scheduler.
 */
Watcher.prototype.run = function run () {
  if (this.active) {
    var value = this.get();
    if (
      value !== this.value ||
      // Deep watchers and watchers on Object/Arrays should fire even
      // when the value is the same, because the value may
      // have mutated.
      isObject(value) ||
      this.deep
    ) {
      // set new value
      var oldValue = this.value;
      this.value = value;
      if (this.user) {
        try {
          this.cb.call(this.vm, value, oldValue);
        } catch (e) {
          handleError(e, this.vm, ("callback for watcher \"" + (this.expression) + "\""));
        }
      } else {
        this.cb.call(this.vm, value, oldValue);
      }
    }
  }
};

/**
 * Evaluate the value of the watcher.
 * This only gets called for lazy watchers.
 */
Watcher.prototype.evaluate = function evaluate () {
  this.value = this.get();
  this.dirty = false;
};

/**
 * Depend on all deps collected by this watcher.
 */
Watcher.prototype.depend = function depend () {
  var i = this.deps.length;
  while (i--) {
    this.deps[i].depend();
  }
};

/**
 * Remove self from all dependencies' subscriber list.
 */
Watcher.prototype.teardown = function teardown () {
  if (this.active) {
    // remove self from vm's watcher list
    // this is a somewhat expensive operation so we skip it
    // if the vm is being destroyed.
    if (!this.vm._isBeingDestroyed) {
      remove(this.vm._watchers, this);
    }
    var i = this.deps.length;
    while (i--) {
      this.deps[i].removeSub(this);
    }
    this.active = false;
  }
};

/*  */

var sharedPropertyDefinition = {
  enumerable: true,
  configurable: true,
  get: noop,
  set: noop
};

function proxy (target, sourceKey, key) {
  sharedPropertyDefinition.get = function proxyGetter () {
    return this[sourceKey][key]
  };
  sharedPropertyDefinition.set = function proxySetter (val) {
    this[sourceKey][key] = val;
  };
  Object.defineProperty(target, key, sharedPropertyDefinition);
}

function initState (vm) {
  vm._watchers = [];
  var opts = vm.$options;
  if (opts.props) { initProps(vm, opts.props); }
  if (opts.methods) { initMethods(vm, opts.methods); }
  if (opts.data) {
    initData(vm);
  } else {
    observe(vm._data = {}, true /* asRootData */);
  }
  if (opts.computed) { initComputed(vm, opts.computed); }
  if (opts.watch && opts.watch !== nativeWatch) {
    initWatch(vm, opts.watch);
  }
}

function initProps (vm, propsOptions) {
  var propsData = vm.$options.propsData || {};
  var props = vm._props = {};
  // cache prop keys so that future props updates can iterate using Array
  // instead of dynamic object key enumeration.
  var keys = vm.$options._propKeys = [];
  var isRoot = !vm.$parent;
  // root instance props should be converted
  if (!isRoot) {
    toggleObserving(false);
  }
  var loop = function ( key ) {
    keys.push(key);
    var value = validateProp(key, propsOptions, propsData, vm);
    /* istanbul ignore else */
    if (true) {
      var hyphenatedKey = hyphenate(key);
      if (isReservedAttribute(hyphenatedKey) ||
          config.isReservedAttr(hyphenatedKey)) {
        warn(
          ("\"" + hyphenatedKey + "\" is a reserved attribute and cannot be used as component prop."),
          vm
        );
      }
      defineReactive$$1(props, key, value, function () {
        if (!isRoot && !isUpdatingChildComponent) {
          {
            if(vm.mpHost === 'mp-baidu' || vm.mpHost === 'mp-kuaishou' || vm.mpHost === 'mp-xhs'){//百度、快手、小红书 observer 在 setData callback 之后触发，直接忽略该 warn
                return
            }
            //fixed by xxxxxx __next_tick_pending,uni://form-field 时不告警
            if(
                key === 'value' && 
                Array.isArray(vm.$options.behaviors) &&
                vm.$options.behaviors.indexOf('uni://form-field') !== -1
              ){
              return
            }
            if(vm._getFormData){
              return
            }
            var $parent = vm.$parent;
            while($parent){
              if($parent.__next_tick_pending){
                return  
              }
              $parent = $parent.$parent;
            }
          }
          warn(
            "Avoid mutating a prop directly since the value will be " +
            "overwritten whenever the parent component re-renders. " +
            "Instead, use a data or computed property based on the prop's " +
            "value. Prop being mutated: \"" + key + "\"",
            vm
          );
        }
      });
    } else {}
    // static props are already proxied on the component's prototype
    // during Vue.extend(). We only need to proxy props defined at
    // instantiation here.
    if (!(key in vm)) {
      proxy(vm, "_props", key);
    }
  };

  for (var key in propsOptions) loop( key );
  toggleObserving(true);
}

function initData (vm) {
  var data = vm.$options.data;
  data = vm._data = typeof data === 'function'
    ? getData(data, vm)
    : data || {};
  if (!isPlainObject(data)) {
    data = {};
     true && warn(
      'data functions should return an object:\n' +
      'https://vuejs.org/v2/guide/components.html#data-Must-Be-a-Function',
      vm
    );
  }
  // proxy data on instance
  var keys = Object.keys(data);
  var props = vm.$options.props;
  var methods = vm.$options.methods;
  var i = keys.length;
  while (i--) {
    var key = keys[i];
    if (true) {
      if (methods && hasOwn(methods, key)) {
        warn(
          ("Method \"" + key + "\" has already been defined as a data property."),
          vm
        );
      }
    }
    if (props && hasOwn(props, key)) {
       true && warn(
        "The data property \"" + key + "\" is already declared as a prop. " +
        "Use prop default value instead.",
        vm
      );
    } else if (!isReserved(key)) {
      proxy(vm, "_data", key);
    }
  }
  // observe data
  observe(data, true /* asRootData */);
}

function getData (data, vm) {
  // #7573 disable dep collection when invoking data getters
  pushTarget();
  try {
    return data.call(vm, vm)
  } catch (e) {
    handleError(e, vm, "data()");
    return {}
  } finally {
    popTarget();
  }
}

var computedWatcherOptions = { lazy: true };

function initComputed (vm, computed) {
  // $flow-disable-line
  var watchers = vm._computedWatchers = Object.create(null);
  // computed properties are just getters during SSR
  var isSSR = isServerRendering();

  for (var key in computed) {
    var userDef = computed[key];
    var getter = typeof userDef === 'function' ? userDef : userDef.get;
    if ( true && getter == null) {
      warn(
        ("Getter is missing for computed property \"" + key + "\"."),
        vm
      );
    }

    if (!isSSR) {
      // create internal watcher for the computed property.
      watchers[key] = new Watcher(
        vm,
        getter || noop,
        noop,
        computedWatcherOptions
      );
    }

    // component-defined computed properties are already defined on the
    // component prototype. We only need to define computed properties defined
    // at instantiation here.
    if (!(key in vm)) {
      defineComputed(vm, key, userDef);
    } else if (true) {
      if (key in vm.$data) {
        warn(("The computed property \"" + key + "\" is already defined in data."), vm);
      } else if (vm.$options.props && key in vm.$options.props) {
        warn(("The computed property \"" + key + "\" is already defined as a prop."), vm);
      }
    }
  }
}

function defineComputed (
  target,
  key,
  userDef
) {
  var shouldCache = !isServerRendering();
  if (typeof userDef === 'function') {
    sharedPropertyDefinition.get = shouldCache
      ? createComputedGetter(key)
      : createGetterInvoker(userDef);
    sharedPropertyDefinition.set = noop;
  } else {
    sharedPropertyDefinition.get = userDef.get
      ? shouldCache && userDef.cache !== false
        ? createComputedGetter(key)
        : createGetterInvoker(userDef.get)
      : noop;
    sharedPropertyDefinition.set = userDef.set || noop;
  }
  if ( true &&
      sharedPropertyDefinition.set === noop) {
    sharedPropertyDefinition.set = function () {
      warn(
        ("Computed property \"" + key + "\" was assigned to but it has no setter."),
        this
      );
    };
  }
  Object.defineProperty(target, key, sharedPropertyDefinition);
}

function createComputedGetter (key) {
  return function computedGetter () {
    var watcher = this._computedWatchers && this._computedWatchers[key];
    if (watcher) {
      if (watcher.dirty) {
        watcher.evaluate();
      }
      if (Dep.SharedObject.target) {// fixed by xxxxxx
        watcher.depend();
      }
      return watcher.value
    }
  }
}

function createGetterInvoker(fn) {
  return function computedGetter () {
    return fn.call(this, this)
  }
}

function initMethods (vm, methods) {
  var props = vm.$options.props;
  for (var key in methods) {
    if (true) {
      if (typeof methods[key] !== 'function') {
        warn(
          "Method \"" + key + "\" has type \"" + (typeof methods[key]) + "\" in the component definition. " +
          "Did you reference the function correctly?",
          vm
        );
      }
      if (props && hasOwn(props, key)) {
        warn(
          ("Method \"" + key + "\" has already been defined as a prop."),
          vm
        );
      }
      if ((key in vm) && isReserved(key)) {
        warn(
          "Method \"" + key + "\" conflicts with an existing Vue instance method. " +
          "Avoid defining component methods that start with _ or $."
        );
      }
    }
    vm[key] = typeof methods[key] !== 'function' ? noop : bind(methods[key], vm);
  }
}

function initWatch (vm, watch) {
  for (var key in watch) {
    var handler = watch[key];
    if (Array.isArray(handler)) {
      for (var i = 0; i < handler.length; i++) {
        createWatcher(vm, key, handler[i]);
      }
    } else {
      createWatcher(vm, key, handler);
    }
  }
}

function createWatcher (
  vm,
  expOrFn,
  handler,
  options
) {
  if (isPlainObject(handler)) {
    options = handler;
    handler = handler.handler;
  }
  if (typeof handler === 'string') {
    handler = vm[handler];
  }
  return vm.$watch(expOrFn, handler, options)
}

function stateMixin (Vue) {
  // flow somehow has problems with directly declared definition object
  // when using Object.defineProperty, so we have to procedurally build up
  // the object here.
  var dataDef = {};
  dataDef.get = function () { return this._data };
  var propsDef = {};
  propsDef.get = function () { return this._props };
  if (true) {
    dataDef.set = function () {
      warn(
        'Avoid replacing instance root $data. ' +
        'Use nested data properties instead.',
        this
      );
    };
    propsDef.set = function () {
      warn("$props is readonly.", this);
    };
  }
  Object.defineProperty(Vue.prototype, '$data', dataDef);
  Object.defineProperty(Vue.prototype, '$props', propsDef);

  Vue.prototype.$set = set;
  Vue.prototype.$delete = del;

  Vue.prototype.$watch = function (
    expOrFn,
    cb,
    options
  ) {
    var vm = this;
    if (isPlainObject(cb)) {
      return createWatcher(vm, expOrFn, cb, options)
    }
    options = options || {};
    options.user = true;
    var watcher = new Watcher(vm, expOrFn, cb, options);
    if (options.immediate) {
      try {
        cb.call(vm, watcher.value);
      } catch (error) {
        handleError(error, vm, ("callback for immediate watcher \"" + (watcher.expression) + "\""));
      }
    }
    return function unwatchFn () {
      watcher.teardown();
    }
  };
}

/*  */

var uid$3 = 0;

function initMixin (Vue) {
  Vue.prototype._init = function (options) {
    var vm = this;
    // a uid
    vm._uid = uid$3++;

    var startTag, endTag;
    /* istanbul ignore if */
    if ( true && config.performance && mark) {
      startTag = "vue-perf-start:" + (vm._uid);
      endTag = "vue-perf-end:" + (vm._uid);
      mark(startTag);
    }

    // a flag to avoid this being observed
    vm._isVue = true;
    // merge options
    if (options && options._isComponent) {
      // optimize internal component instantiation
      // since dynamic options merging is pretty slow, and none of the
      // internal component options needs special treatment.
      initInternalComponent(vm, options);
    } else {
      vm.$options = mergeOptions(
        resolveConstructorOptions(vm.constructor),
        options || {},
        vm
      );
    }
    /* istanbul ignore else */
    if (true) {
      initProxy(vm);
    } else {}
    // expose real self
    vm._self = vm;
    initLifecycle(vm);
    initEvents(vm);
    initRender(vm);
    callHook(vm, 'beforeCreate');
    !vm._$fallback && initInjections(vm); // resolve injections before data/props  
    initState(vm);
    !vm._$fallback && initProvide(vm); // resolve provide after data/props
    !vm._$fallback && callHook(vm, 'created');      

    /* istanbul ignore if */
    if ( true && config.performance && mark) {
      vm._name = formatComponentName(vm, false);
      mark(endTag);
      measure(("vue " + (vm._name) + " init"), startTag, endTag);
    }

    if (vm.$options.el) {
      vm.$mount(vm.$options.el);
    }
  };
}

function initInternalComponent (vm, options) {
  var opts = vm.$options = Object.create(vm.constructor.options);
  // doing this because it's faster than dynamic enumeration.
  var parentVnode = options._parentVnode;
  opts.parent = options.parent;
  opts._parentVnode = parentVnode;

  var vnodeComponentOptions = parentVnode.componentOptions;
  opts.propsData = vnodeComponentOptions.propsData;
  opts._parentListeners = vnodeComponentOptions.listeners;
  opts._renderChildren = vnodeComponentOptions.children;
  opts._componentTag = vnodeComponentOptions.tag;

  if (options.render) {
    opts.render = options.render;
    opts.staticRenderFns = options.staticRenderFns;
  }
}

function resolveConstructorOptions (Ctor) {
  var options = Ctor.options;
  if (Ctor.super) {
    var superOptions = resolveConstructorOptions(Ctor.super);
    var cachedSuperOptions = Ctor.superOptions;
    if (superOptions !== cachedSuperOptions) {
      // super option changed,
      // need to resolve new options.
      Ctor.superOptions = superOptions;
      // check if there are any late-modified/attached options (#4976)
      var modifiedOptions = resolveModifiedOptions(Ctor);
      // update base extend options
      if (modifiedOptions) {
        extend(Ctor.extendOptions, modifiedOptions);
      }
      options = Ctor.options = mergeOptions(superOptions, Ctor.extendOptions);
      if (options.name) {
        options.components[options.name] = Ctor;
      }
    }
  }
  return options
}

function resolveModifiedOptions (Ctor) {
  var modified;
  var latest = Ctor.options;
  var sealed = Ctor.sealedOptions;
  for (var key in latest) {
    if (latest[key] !== sealed[key]) {
      if (!modified) { modified = {}; }
      modified[key] = latest[key];
    }
  }
  return modified
}

function Vue (options) {
  if ( true &&
    !(this instanceof Vue)
  ) {
    warn('Vue is a constructor and should be called with the `new` keyword');
  }
  this._init(options);
}

initMixin(Vue);
stateMixin(Vue);
eventsMixin(Vue);
lifecycleMixin(Vue);
renderMixin(Vue);

/*  */

function initUse (Vue) {
  Vue.use = function (plugin) {
    var installedPlugins = (this._installedPlugins || (this._installedPlugins = []));
    if (installedPlugins.indexOf(plugin) > -1) {
      return this
    }

    // additional parameters
    var args = toArray(arguments, 1);
    args.unshift(this);
    if (typeof plugin.install === 'function') {
      plugin.install.apply(plugin, args);
    } else if (typeof plugin === 'function') {
      plugin.apply(null, args);
    }
    installedPlugins.push(plugin);
    return this
  };
}

/*  */

function initMixin$1 (Vue) {
  Vue.mixin = function (mixin) {
    this.options = mergeOptions(this.options, mixin);
    return this
  };
}

/*  */

function initExtend (Vue) {
  /**
   * Each instance constructor, including Vue, has a unique
   * cid. This enables us to create wrapped "child
   * constructors" for prototypal inheritance and cache them.
   */
  Vue.cid = 0;
  var cid = 1;

  /**
   * Class inheritance
   */
  Vue.extend = function (extendOptions) {
    extendOptions = extendOptions || {};
    var Super = this;
    var SuperId = Super.cid;
    var cachedCtors = extendOptions._Ctor || (extendOptions._Ctor = {});
    if (cachedCtors[SuperId]) {
      return cachedCtors[SuperId]
    }

    var name = extendOptions.name || Super.options.name;
    if ( true && name) {
      validateComponentName(name);
    }

    var Sub = function VueComponent (options) {
      this._init(options);
    };
    Sub.prototype = Object.create(Super.prototype);
    Sub.prototype.constructor = Sub;
    Sub.cid = cid++;
    Sub.options = mergeOptions(
      Super.options,
      extendOptions
    );
    Sub['super'] = Super;

    // For props and computed properties, we define the proxy getters on
    // the Vue instances at extension time, on the extended prototype. This
    // avoids Object.defineProperty calls for each instance created.
    if (Sub.options.props) {
      initProps$1(Sub);
    }
    if (Sub.options.computed) {
      initComputed$1(Sub);
    }

    // allow further extension/mixin/plugin usage
    Sub.extend = Super.extend;
    Sub.mixin = Super.mixin;
    Sub.use = Super.use;

    // create asset registers, so extended classes
    // can have their private assets too.
    ASSET_TYPES.forEach(function (type) {
      Sub[type] = Super[type];
    });
    // enable recursive self-lookup
    if (name) {
      Sub.options.components[name] = Sub;
    }

    // keep a reference to the super options at extension time.
    // later at instantiation we can check if Super's options have
    // been updated.
    Sub.superOptions = Super.options;
    Sub.extendOptions = extendOptions;
    Sub.sealedOptions = extend({}, Sub.options);

    // cache constructor
    cachedCtors[SuperId] = Sub;
    return Sub
  };
}

function initProps$1 (Comp) {
  var props = Comp.options.props;
  for (var key in props) {
    proxy(Comp.prototype, "_props", key);
  }
}

function initComputed$1 (Comp) {
  var computed = Comp.options.computed;
  for (var key in computed) {
    defineComputed(Comp.prototype, key, computed[key]);
  }
}

/*  */

function initAssetRegisters (Vue) {
  /**
   * Create asset registration methods.
   */
  ASSET_TYPES.forEach(function (type) {
    Vue[type] = function (
      id,
      definition
    ) {
      if (!definition) {
        return this.options[type + 's'][id]
      } else {
        /* istanbul ignore if */
        if ( true && type === 'component') {
          validateComponentName(id);
        }
        if (type === 'component' && isPlainObject(definition)) {
          definition.name = definition.name || id;
          definition = this.options._base.extend(definition);
        }
        if (type === 'directive' && typeof definition === 'function') {
          definition = { bind: definition, update: definition };
        }
        this.options[type + 's'][id] = definition;
        return definition
      }
    };
  });
}

/*  */



function getComponentName (opts) {
  return opts && (opts.Ctor.options.name || opts.tag)
}

function matches (pattern, name) {
  if (Array.isArray(pattern)) {
    return pattern.indexOf(name) > -1
  } else if (typeof pattern === 'string') {
    return pattern.split(',').indexOf(name) > -1
  } else if (isRegExp(pattern)) {
    return pattern.test(name)
  }
  /* istanbul ignore next */
  return false
}

function pruneCache (keepAliveInstance, filter) {
  var cache = keepAliveInstance.cache;
  var keys = keepAliveInstance.keys;
  var _vnode = keepAliveInstance._vnode;
  for (var key in cache) {
    var cachedNode = cache[key];
    if (cachedNode) {
      var name = getComponentName(cachedNode.componentOptions);
      if (name && !filter(name)) {
        pruneCacheEntry(cache, key, keys, _vnode);
      }
    }
  }
}

function pruneCacheEntry (
  cache,
  key,
  keys,
  current
) {
  var cached$$1 = cache[key];
  if (cached$$1 && (!current || cached$$1.tag !== current.tag)) {
    cached$$1.componentInstance.$destroy();
  }
  cache[key] = null;
  remove(keys, key);
}

var patternTypes = [String, RegExp, Array];

var KeepAlive = {
  name: 'keep-alive',
  abstract: true,

  props: {
    include: patternTypes,
    exclude: patternTypes,
    max: [String, Number]
  },

  created: function created () {
    this.cache = Object.create(null);
    this.keys = [];
  },

  destroyed: function destroyed () {
    for (var key in this.cache) {
      pruneCacheEntry(this.cache, key, this.keys);
    }
  },

  mounted: function mounted () {
    var this$1 = this;

    this.$watch('include', function (val) {
      pruneCache(this$1, function (name) { return matches(val, name); });
    });
    this.$watch('exclude', function (val) {
      pruneCache(this$1, function (name) { return !matches(val, name); });
    });
  },

  render: function render () {
    var slot = this.$slots.default;
    var vnode = getFirstComponentChild(slot);
    var componentOptions = vnode && vnode.componentOptions;
    if (componentOptions) {
      // check pattern
      var name = getComponentName(componentOptions);
      var ref = this;
      var include = ref.include;
      var exclude = ref.exclude;
      if (
        // not included
        (include && (!name || !matches(include, name))) ||
        // excluded
        (exclude && name && matches(exclude, name))
      ) {
        return vnode
      }

      var ref$1 = this;
      var cache = ref$1.cache;
      var keys = ref$1.keys;
      var key = vnode.key == null
        // same constructor may get registered as different local components
        // so cid alone is not enough (#3269)
        ? componentOptions.Ctor.cid + (componentOptions.tag ? ("::" + (componentOptions.tag)) : '')
        : vnode.key;
      if (cache[key]) {
        vnode.componentInstance = cache[key].componentInstance;
        // make current key freshest
        remove(keys, key);
        keys.push(key);
      } else {
        cache[key] = vnode;
        keys.push(key);
        // prune oldest entry
        if (this.max && keys.length > parseInt(this.max)) {
          pruneCacheEntry(cache, keys[0], keys, this._vnode);
        }
      }

      vnode.data.keepAlive = true;
    }
    return vnode || (slot && slot[0])
  }
};

var builtInComponents = {
  KeepAlive: KeepAlive
};

/*  */

function initGlobalAPI (Vue) {
  // config
  var configDef = {};
  configDef.get = function () { return config; };
  if (true) {
    configDef.set = function () {
      warn(
        'Do not replace the Vue.config object, set individual fields instead.'
      );
    };
  }
  Object.defineProperty(Vue, 'config', configDef);

  // exposed util methods.
  // NOTE: these are not considered part of the public API - avoid relying on
  // them unless you are aware of the risk.
  Vue.util = {
    warn: warn,
    extend: extend,
    mergeOptions: mergeOptions,
    defineReactive: defineReactive$$1
  };

  Vue.set = set;
  Vue.delete = del;
  Vue.nextTick = nextTick;

  // 2.6 explicit observable API
  Vue.observable = function (obj) {
    observe(obj);
    return obj
  };

  Vue.options = Object.create(null);
  ASSET_TYPES.forEach(function (type) {
    Vue.options[type + 's'] = Object.create(null);
  });

  // this is used to identify the "base" constructor to extend all plain-object
  // components with in Weex's multi-instance scenarios.
  Vue.options._base = Vue;

  extend(Vue.options.components, builtInComponents);

  initUse(Vue);
  initMixin$1(Vue);
  initExtend(Vue);
  initAssetRegisters(Vue);
}

initGlobalAPI(Vue);

Object.defineProperty(Vue.prototype, '$isServer', {
  get: isServerRendering
});

Object.defineProperty(Vue.prototype, '$ssrContext', {
  get: function get () {
    /* istanbul ignore next */
    return this.$vnode && this.$vnode.ssrContext
  }
});

// expose FunctionalRenderContext for ssr runtime helper installation
Object.defineProperty(Vue, 'FunctionalRenderContext', {
  value: FunctionalRenderContext
});

Vue.version = '2.6.11';

/**
 * https://raw.githubusercontent.com/Tencent/westore/master/packages/westore/utils/diff.js
 */
var ARRAYTYPE = '[object Array]';
var OBJECTTYPE = '[object Object]';
var NULLTYPE = '[object Null]';
var UNDEFINEDTYPE = '[object Undefined]';
// const FUNCTIONTYPE = '[object Function]'

function diff(current, pre) {
    var result = {};
    syncKeys(current, pre);
    _diff(current, pre, '', result);
    return result
}

function syncKeys(current, pre) {
    if (current === pre) { return }
    var rootCurrentType = type(current);
    var rootPreType = type(pre);
    if (rootCurrentType == OBJECTTYPE && rootPreType == OBJECTTYPE) {
        if(Object.keys(current).length >= Object.keys(pre).length){
            for (var key in pre) {
                var currentValue = current[key];
                if (currentValue === undefined) {
                    current[key] = null;
                } else {
                    syncKeys(currentValue, pre[key]);
                }
            }
        }
    } else if (rootCurrentType == ARRAYTYPE && rootPreType == ARRAYTYPE) {
        if (current.length >= pre.length) {
            pre.forEach(function (item, index) {
                syncKeys(current[index], item);
            });
        }
    }
}

function nullOrUndefined(currentType, preType) {
    if(
        (currentType === NULLTYPE || currentType === UNDEFINEDTYPE) && 
        (preType === NULLTYPE || preType === UNDEFINEDTYPE)
    ) {
        return false
    }
    return true
}

function _diff(current, pre, path, result) {
    if (current === pre) { return }
    var rootCurrentType = type(current);
    var rootPreType = type(pre);
    if (rootCurrentType == OBJECTTYPE) {
        if (rootPreType != OBJECTTYPE || Object.keys(current).length < Object.keys(pre).length) {
            setResult(result, path, current);
        } else {
            var loop = function ( key ) {
                var currentValue = current[key];
                var preValue = pre[key];
                var currentType = type(currentValue);
                var preType = type(preValue);
                if (currentType != ARRAYTYPE && currentType != OBJECTTYPE) {
                    if (currentValue !== pre[key] && nullOrUndefined(currentType, preType)) {
                        setResult(result, (path == '' ? '' : path + ".") + key, currentValue);
                    }
                } else if (currentType == ARRAYTYPE) {
                    if (preType != ARRAYTYPE) {
                        setResult(result, (path == '' ? '' : path + ".") + key, currentValue);
                    } else {
                        if (currentValue.length < preValue.length) {
                            setResult(result, (path == '' ? '' : path + ".") + key, currentValue);
                        } else {
                            currentValue.forEach(function (item, index) {
                                _diff(item, preValue[index], (path == '' ? '' : path + ".") + key + '[' + index + ']', result);
                            });
                        }
                    }
                } else if (currentType == OBJECTTYPE) {
                    if (preType != OBJECTTYPE || Object.keys(currentValue).length < Object.keys(preValue).length) {
                        setResult(result, (path == '' ? '' : path + ".") + key, currentValue);
                    } else {
                        for (var subKey in currentValue) {
                            _diff(currentValue[subKey], preValue[subKey], (path == '' ? '' : path + ".") + key + '.' + subKey, result);
                        }
                    }
                }
            };

            for (var key in current) loop( key );
        }
    } else if (rootCurrentType == ARRAYTYPE) {
        if (rootPreType != ARRAYTYPE) {
            setResult(result, path, current);
        } else {
            if (current.length < pre.length) {
                setResult(result, path, current);
            } else {
                current.forEach(function (item, index) {
                    _diff(item, pre[index], path + '[' + index + ']', result);
                });
            }
        }
    } else {
        setResult(result, path, current);
    }
}

function setResult(result, k, v) {
    // if (type(v) != FUNCTIONTYPE) {
        result[k] = v;
    // }
}

function type(obj) {
    return Object.prototype.toString.call(obj)
}

/*  */

function flushCallbacks$1(vm) {
    if (vm.__next_tick_callbacks && vm.__next_tick_callbacks.length) {
        if (Object({"NODE_ENV":"development","VUE_APP_DARK_MODE":"false","VUE_APP_NAME":"宠物之家","VUE_APP_PLATFORM":"mp-weixin","BASE_URL":"/"}).VUE_APP_DEBUG) {
            var mpInstance = vm.$scope;
            console.log('[' + (+new Date) + '][' + (mpInstance.is || mpInstance.route) + '][' + vm._uid +
                ']:flushCallbacks[' + vm.__next_tick_callbacks.length + ']');
        }
        var copies = vm.__next_tick_callbacks.slice(0);
        vm.__next_tick_callbacks.length = 0;
        for (var i = 0; i < copies.length; i++) {
            copies[i]();
        }
    }
}

function hasRenderWatcher(vm) {
    return queue.find(function (watcher) { return vm._watcher === watcher; })
}

function nextTick$1(vm, cb) {
    //1.nextTick 之前 已 setData 且 setData 还未回调完成
    //2.nextTick 之前存在 render watcher
    if (!vm.__next_tick_pending && !hasRenderWatcher(vm)) {
        if(Object({"NODE_ENV":"development","VUE_APP_DARK_MODE":"false","VUE_APP_NAME":"宠物之家","VUE_APP_PLATFORM":"mp-weixin","BASE_URL":"/"}).VUE_APP_DEBUG){
            var mpInstance = vm.$scope;
            console.log('[' + (+new Date) + '][' + (mpInstance.is || mpInstance.route) + '][' + vm._uid +
                ']:nextVueTick');
        }
        return nextTick(cb, vm)
    }else{
        if(Object({"NODE_ENV":"development","VUE_APP_DARK_MODE":"false","VUE_APP_NAME":"宠物之家","VUE_APP_PLATFORM":"mp-weixin","BASE_URL":"/"}).VUE_APP_DEBUG){
            var mpInstance$1 = vm.$scope;
            console.log('[' + (+new Date) + '][' + (mpInstance$1.is || mpInstance$1.route) + '][' + vm._uid +
                ']:nextMPTick');
        }
    }
    var _resolve;
    if (!vm.__next_tick_callbacks) {
        vm.__next_tick_callbacks = [];
    }
    vm.__next_tick_callbacks.push(function () {
        if (cb) {
            try {
                cb.call(vm);
            } catch (e) {
                handleError(e, vm, 'nextTick');
            }
        } else if (_resolve) {
            _resolve(vm);
        }
    });
    // $flow-disable-line
    if (!cb && typeof Promise !== 'undefined') {
        return new Promise(function (resolve) {
            _resolve = resolve;
        })
    }
}

/*  */

function clearInstance(key, value) {
  // 简易去除 Vue 和小程序组件实例
  if (value) {
    if (value._isVue || value.__v_isMPComponent) {
      return {}
    }
  }
  return value
}

function cloneWithData(vm) {
  // 确保当前 vm 所有数据被同步
  var ret = Object.create(null);
  var dataKeys = [].concat(
    Object.keys(vm._data || {}),
    Object.keys(vm._computedWatchers || {}));

  dataKeys.reduce(function(ret, key) {
    ret[key] = vm[key];
    return ret
  }, ret);

  // vue-composition-api
  var compositionApiState = vm.__composition_api_state__ || vm.__secret_vfa_state__;
  var rawBindings = compositionApiState && compositionApiState.rawBindings;
  if (rawBindings) {
    Object.keys(rawBindings).forEach(function (key) {
      ret[key] = vm[key];
    });
  }

  //TODO 需要把无用数据处理掉，比如 list=>l0 则 list 需要移除，否则多传输一份数据
  Object.assign(ret, vm.$mp.data || {});
  if (
    Array.isArray(vm.$options.behaviors) &&
    vm.$options.behaviors.indexOf('uni://form-field') !== -1
  ) { //form-field
    ret['name'] = vm.name;
    ret['value'] = vm.value;
  }

  return JSON.parse(JSON.stringify(ret, clearInstance))
}

var patch = function(oldVnode, vnode) {
  var this$1 = this;

  if (vnode === null) { //destroy
    return
  }
  if (this.mpType === 'page' || this.mpType === 'component') {
    var mpInstance = this.$scope;
    var data = Object.create(null);
    try {
      data = cloneWithData(this);
    } catch (err) {
      console.error(err);
    }
    data.__webviewId__ = mpInstance.data.__webviewId__;
    var mpData = Object.create(null);
    Object.keys(data).forEach(function (key) { //仅同步 data 中有的数据
      mpData[key] = mpInstance.data[key];
    });
    var diffData = this.$shouldDiffData === false ? data : diff(data, mpData);
    if (Object.keys(diffData).length) {
      if (Object({"NODE_ENV":"development","VUE_APP_DARK_MODE":"false","VUE_APP_NAME":"宠物之家","VUE_APP_PLATFORM":"mp-weixin","BASE_URL":"/"}).VUE_APP_DEBUG) {
        console.log('[' + (+new Date) + '][' + (mpInstance.is || mpInstance.route) + '][' + this._uid +
          ']差量更新',
          JSON.stringify(diffData));
      }
      this.__next_tick_pending = true;
      mpInstance.setData(diffData, function () {
        this$1.__next_tick_pending = false;
        flushCallbacks$1(this$1);
      });
    } else {
      flushCallbacks$1(this);
    }
  }
};

/*  */

function createEmptyRender() {

}

function mountComponent$1(
  vm,
  el,
  hydrating
) {
  if (!vm.mpType) {//main.js 中的 new Vue
    return vm
  }
  if (vm.mpType === 'app') {
    vm.$options.render = createEmptyRender;
  }
  if (!vm.$options.render) {
    vm.$options.render = createEmptyRender;
    if (true) {
      /* istanbul ignore if */
      if ((vm.$options.template && vm.$options.template.charAt(0) !== '#') ||
        vm.$options.el || el) {
        warn(
          'You are using the runtime-only build of Vue where the template ' +
          'compiler is not available. Either pre-compile the templates into ' +
          'render functions, or use the compiler-included build.',
          vm
        );
      } else {
        warn(
          'Failed to mount component: template or render function not defined.',
          vm
        );
      }
    }
  }
  
  !vm._$fallback && callHook(vm, 'beforeMount');

  var updateComponent = function () {
    vm._update(vm._render(), hydrating);
  };

  // we set this to vm._watcher inside the watcher's constructor
  // since the watcher's initial patch may call $forceUpdate (e.g. inside child
  // component's mounted hook), which relies on vm._watcher being already defined
  new Watcher(vm, updateComponent, noop, {
    before: function before() {
      if (vm._isMounted && !vm._isDestroyed) {
        callHook(vm, 'beforeUpdate');
      }
    }
  }, true /* isRenderWatcher */);
  hydrating = false;
  return vm
}

/*  */

function renderClass (
  staticClass,
  dynamicClass
) {
  if (isDef(staticClass) || isDef(dynamicClass)) {
    return concat(staticClass, stringifyClass(dynamicClass))
  }
  /* istanbul ignore next */
  return ''
}

function concat (a, b) {
  return a ? b ? (a + ' ' + b) : a : (b || '')
}

function stringifyClass (value) {
  if (Array.isArray(value)) {
    return stringifyArray(value)
  }
  if (isObject(value)) {
    return stringifyObject(value)
  }
  if (typeof value === 'string') {
    return value
  }
  /* istanbul ignore next */
  return ''
}

function stringifyArray (value) {
  var res = '';
  var stringified;
  for (var i = 0, l = value.length; i < l; i++) {
    if (isDef(stringified = stringifyClass(value[i])) && stringified !== '') {
      if (res) { res += ' '; }
      res += stringified;
    }
  }
  return res
}

function stringifyObject (value) {
  var res = '';
  for (var key in value) {
    if (value[key]) {
      if (res) { res += ' '; }
      res += key;
    }
  }
  return res
}

/*  */

var parseStyleText = cached(function (cssText) {
  var res = {};
  var listDelimiter = /;(?![^(]*\))/g;
  var propertyDelimiter = /:(.+)/;
  cssText.split(listDelimiter).forEach(function (item) {
    if (item) {
      var tmp = item.split(propertyDelimiter);
      tmp.length > 1 && (res[tmp[0].trim()] = tmp[1].trim());
    }
  });
  return res
});

// normalize possible array / string values into Object
function normalizeStyleBinding (bindingStyle) {
  if (Array.isArray(bindingStyle)) {
    return toObject(bindingStyle)
  }
  if (typeof bindingStyle === 'string') {
    return parseStyleText(bindingStyle)
  }
  return bindingStyle
}

/*  */

var MP_METHODS = ['createSelectorQuery', 'createIntersectionObserver', 'selectAllComponents', 'selectComponent'];

function getTarget(obj, path) {
  var parts = path.split('.');
  var key = parts[0];
  if (key.indexOf('__$n') === 0) { //number index
    key = parseInt(key.replace('__$n', ''));
  }
  if (parts.length === 1) {
    return obj[key]
  }
  return getTarget(obj[key], parts.slice(1).join('.'))
}

function internalMixin(Vue) {

  Vue.config.errorHandler = function(err, vm, info) {
    Vue.util.warn(("Error in " + info + ": \"" + (err.toString()) + "\""), vm);
    console.error(err);
    /* eslint-disable no-undef */
    var app = typeof getApp === 'function' && getApp();
    if (app && app.onError) {
      app.onError(err);
    }
  };

  var oldEmit = Vue.prototype.$emit;

  Vue.prototype.$emit = function(event) {
    if (this.$scope && event) {
      var triggerEvent = this.$scope['_triggerEvent'] || this.$scope['triggerEvent'];
      if (triggerEvent) {
        try {
          triggerEvent.call(this.$scope, event, {
            __args__: toArray(arguments, 1)
          });
        } catch (error) {

        }
      }
    }
    return oldEmit.apply(this, arguments)
  };

  Vue.prototype.$nextTick = function(fn) {
    return nextTick$1(this, fn)
  };

  MP_METHODS.forEach(function (method) {
    Vue.prototype[method] = function(args) {
      if (this.$scope && this.$scope[method]) {
        return this.$scope[method](args)
      }
      // mp-alipay
      if (typeof my === 'undefined') {
        return
      }
      if (method === 'createSelectorQuery') {
        /* eslint-disable no-undef */
        return my.createSelectorQuery(args)
      } else if (method === 'createIntersectionObserver') {
        /* eslint-disable no-undef */
        return my.createIntersectionObserver(args)
      }
      // TODO mp-alipay 暂不支持 selectAllComponents,selectComponent
    };
  });

  Vue.prototype.__init_provide = initProvide;

  Vue.prototype.__init_injections = initInjections;

  Vue.prototype.__call_hook = function(hook, args) {
    var vm = this;
    // #7573 disable dep collection when invoking lifecycle hooks
    pushTarget();
    var handlers = vm.$options[hook];
    var info = hook + " hook";
    var ret;
    if (handlers) {
      for (var i = 0, j = handlers.length; i < j; i++) {
        ret = invokeWithErrorHandling(handlers[i], vm, args ? [args] : null, vm, info);
      }
    }
    if (vm._hasHookEvent) {
      vm.$emit('hook:' + hook, args);
    }
    popTarget();
    return ret
  };

  Vue.prototype.__set_model = function(target, key, value, modifiers) {
    if (Array.isArray(modifiers)) {
      if (modifiers.indexOf('trim') !== -1) {
        value = value.trim();
      }
      if (modifiers.indexOf('number') !== -1) {
        value = this._n(value);
      }
    }
    if (!target) {
      target = this;
    }
    // 解决动态属性添加
    Vue.set(target, key, value);
  };

  Vue.prototype.__set_sync = function(target, key, value) {
    if (!target) {
      target = this;
    }
    // 解决动态属性添加
    Vue.set(target, key, value);
  };

  Vue.prototype.__get_orig = function(item) {
    if (isPlainObject(item)) {
      return item['$orig'] || item
    }
    return item
  };

  Vue.prototype.__get_value = function(dataPath, target) {
    return getTarget(target || this, dataPath)
  };


  Vue.prototype.__get_class = function(dynamicClass, staticClass) {
    return renderClass(staticClass, dynamicClass)
  };

  Vue.prototype.__get_style = function(dynamicStyle, staticStyle) {
    if (!dynamicStyle && !staticStyle) {
      return ''
    }
    var dynamicStyleObj = normalizeStyleBinding(dynamicStyle);
    var styleObj = staticStyle ? extend(staticStyle, dynamicStyleObj) : dynamicStyleObj;
    return Object.keys(styleObj).map(function (name) { return ((hyphenate(name)) + ":" + (styleObj[name])); }).join(';')
  };

  Vue.prototype.__map = function(val, iteratee) {
    //TODO 暂不考虑 string
    var ret, i, l, keys, key;
    if (Array.isArray(val)) {
      ret = new Array(val.length);
      for (i = 0, l = val.length; i < l; i++) {
        ret[i] = iteratee(val[i], i);
      }
      return ret
    } else if (isObject(val)) {
      keys = Object.keys(val);
      ret = Object.create(null);
      for (i = 0, l = keys.length; i < l; i++) {
        key = keys[i];
        ret[key] = iteratee(val[key], key, i);
      }
      return ret
    } else if (typeof val === 'number') {
      ret = new Array(val);
      for (i = 0, l = val; i < l; i++) {
        // 第一个参数暂时仍和小程序一致
        ret[i] = iteratee(i, i);
      }
      return ret
    }
    return []
  };

}

/*  */

var LIFECYCLE_HOOKS$1 = [
    //App
    'onLaunch',
    'onShow',
    'onHide',
    'onUniNViewMessage',
    'onPageNotFound',
    'onThemeChange',
    'onError',
    'onUnhandledRejection',
    //Page
    'onInit',
    'onLoad',
    // 'onShow',
    'onReady',
    // 'onHide',
    'onUnload',
    'onPullDownRefresh',
    'onReachBottom',
    'onTabItemTap',
    'onAddToFavorites',
    'onShareTimeline',
    'onShareAppMessage',
    'onResize',
    'onPageScroll',
    'onNavigationBarButtonTap',
    'onBackPress',
    'onNavigationBarSearchInputChanged',
    'onNavigationBarSearchInputConfirmed',
    'onNavigationBarSearchInputClicked',
    'onUploadDouyinVideo',
    'onNFCReadMessage',
    //Component
    // 'onReady', // 兼容旧版本，应该移除该事件
    'onPageShow',
    'onPageHide',
    'onPageResize'
];
function lifecycleMixin$1(Vue) {

    //fixed vue-class-component
    var oldExtend = Vue.extend;
    Vue.extend = function(extendOptions) {
        extendOptions = extendOptions || {};

        var methods = extendOptions.methods;
        if (methods) {
            Object.keys(methods).forEach(function (methodName) {
                if (LIFECYCLE_HOOKS$1.indexOf(methodName)!==-1) {
                    extendOptions[methodName] = methods[methodName];
                    delete methods[methodName];
                }
            });
        }

        return oldExtend.call(this, extendOptions)
    };

    var strategies = Vue.config.optionMergeStrategies;
    var mergeHook = strategies.created;
    LIFECYCLE_HOOKS$1.forEach(function (hook) {
        strategies[hook] = mergeHook;
    });

    Vue.prototype.__lifecycle_hooks__ = LIFECYCLE_HOOKS$1;
}

/*  */

// install platform patch function
Vue.prototype.__patch__ = patch;

// public mount method
Vue.prototype.$mount = function(
    el ,
    hydrating 
) {
    return mountComponent$1(this, el, hydrating)
};

lifecycleMixin$1(Vue);
internalMixin(Vue);

/*  */

/* harmony default export */ __webpack_exports__["default"] = (Vue);

/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./../../../../../webpack/buildin/global.js */ 3)))

/***/ }),

/***/ 26:
/*!***************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages.json ***!
  \***************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {



/***/ }),

/***/ 3:
/*!***********************************!*\
  !*** (webpack)/buildin/global.js ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports) {

var g;

// This works in non-strict mode
g = (function() {
	return this;
})();

try {
	// This works if eval is allowed (see CSP)
	g = g || new Function("return this")();
} catch (e) {
	// This works if the window reference is available
	if (typeof window === "object") g = window;
}

// g can still be undefined, but nothing to do about it...
// We return undefined, instead of nothing here, so it's
// easier to handle this case. if(!global) { ...}

module.exports = g;


/***/ }),

/***/ 34:
/*!**********************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js ***!
  \**********************************************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "default", function() { return normalizeComponent; });
/* globals __VUE_SSR_CONTEXT__ */

// IMPORTANT: Do NOT use ES2015 features in this file (except for modules).
// This module is a runtime utility for cleaner component module output and will
// be included in the final webpack user bundle.

function normalizeComponent (
  scriptExports,
  render,
  staticRenderFns,
  functionalTemplate,
  injectStyles,
  scopeId,
  moduleIdentifier, /* server only */
  shadowMode, /* vue-cli only */
  components, // fixed by xxxxxx auto components
  renderjs // fixed by xxxxxx renderjs
) {
  // Vue.extend constructor export interop
  var options = typeof scriptExports === 'function'
    ? scriptExports.options
    : scriptExports

  // fixed by xxxxxx auto components
  if (components) {
    if (!options.components) {
      options.components = {}
    }
    var hasOwn = Object.prototype.hasOwnProperty
    for (var name in components) {
      if (hasOwn.call(components, name) && !hasOwn.call(options.components, name)) {
        options.components[name] = components[name]
      }
    }
  }
  // fixed by xxxxxx renderjs
  if (renderjs) {
    if(typeof renderjs.beforeCreate === 'function'){
			renderjs.beforeCreate = [renderjs.beforeCreate]
		}
    (renderjs.beforeCreate || (renderjs.beforeCreate = [])).unshift(function() {
      this[renderjs.__module] = this
    });
    (options.mixins || (options.mixins = [])).push(renderjs)
  }

  // render functions
  if (render) {
    options.render = render
    options.staticRenderFns = staticRenderFns
    options._compiled = true
  }

  // functional template
  if (functionalTemplate) {
    options.functional = true
  }

  // scopedId
  if (scopeId) {
    options._scopeId = 'data-v-' + scopeId
  }

  var hook
  if (moduleIdentifier) { // server build
    hook = function (context) {
      // 2.3 injection
      context =
        context || // cached call
        (this.$vnode && this.$vnode.ssrContext) || // stateful
        (this.parent && this.parent.$vnode && this.parent.$vnode.ssrContext) // functional
      // 2.2 with runInNewContext: true
      if (!context && typeof __VUE_SSR_CONTEXT__ !== 'undefined') {
        context = __VUE_SSR_CONTEXT__
      }
      // inject component styles
      if (injectStyles) {
        injectStyles.call(this, context)
      }
      // register component module identifier for async chunk inferrence
      if (context && context._registeredComponents) {
        context._registeredComponents.add(moduleIdentifier)
      }
    }
    // used by ssr in case component is cached and beforeCreate
    // never gets called
    options._ssrRegister = hook
  } else if (injectStyles) {
    hook = shadowMode
      ? function () { injectStyles.call(this, this.$root.$options.shadowRoot) }
      : injectStyles
  }

  if (hook) {
    if (options.functional) {
      // for template-only hot-reload because in that case the render fn doesn't
      // go through the normalizer
      options._injectStyles = hook
      // register for functioal component in vue file
      var originalRender = options.render
      options.render = function renderWithStyleInjection (h, context) {
        hook.call(context)
        return originalRender(h, context)
      }
    } else {
      // inject component registration as beforeCreate hook
      var existing = options.beforeCreate
      options.beforeCreate = existing
        ? [].concat(existing, hook)
        : [hook]
    }
  }

  return {
    exports: scriptExports,
    options: options
  }
}


/***/ }),

/***/ 35:
/*!*****************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/uni.promisify.adaptor.js ***!
  \*****************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
// uni.promisify.adaptor.js
// 为uni-app API添加Promise支持

var promisify = function promisify(api) {
  return function (options) {
    for (var _len = arguments.length, params = new Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      params[_key - 1] = arguments[_key];
    }
    return new Promise(function (resolve, reject) {
      api.apply(void 0, [Object.assign({}, options, {
        success: resolve,
        fail: reject
      })].concat(params));
    });
  };
};

// 为常用的uni-app API添加Promise支持
var uniAPI = ['request', 'uploadFile', 'downloadFile', 'connectSocket', 'onSocketOpen', 'onSocketError', 'sendSocketMessage', 'onSocketMessage', 'closeSocket', 'onSocketClose', 'login', 'getUserInfo', 'getLocation', 'chooseLocation', 'openLocation', 'chooseImage', 'previewImage', 'getImageInfo', 'saveImageToPhotosAlbum', 'chooseVideo', 'saveVideoToPhotosAlbum', 'getBackgroundAudioPlayerState', 'playBackgroundAudio', 'pauseBackgroundAudio', 'seekBackgroundAudio', 'stopBackgroundAudio', 'playVoice', 'pauseVoice', 'stopVoice', 'getAvailableAudioSources', 'startRecord', 'stopRecord', 'playVoice', 'setInnerAudioOption', 'onBackgroundAudioPlay', 'onBackgroundAudioPause', 'onBackgroundAudioStop', 'getBackgroundAudioManager', 'createInnerAudioContext', 'createAudioContext', 'createVideoContext', 'createMapContext', 'createCameraContext', 'navigateTo', 'redirectTo', 'switchTab', 'navigateBack', 'reLaunch', 'showToast', 'showLoading', 'showModal', 'showActionSheet', 'showNavigationBarLoading', 'hideNavigationBarLoading', 'setNavigationBarTitle', 'setNavigationBarColor', 'setTabBarBadge', 'removeTabBarBadge', 'showTabBarRedDot', 'hideTabBarRedDot', 'setTabBarStyle', 'setTabBarItem', 'showTabBar', 'hideTabBar', 'saveFile', 'getFileInfo', 'getSavedFileList', 'getSavedFileInfo', 'removeSavedFile', 'openDocument', 'setClipboardData', 'getClipboardData', 'getNetworkType', 'getSystemInfo', 'getSystemInfoSync', 'vibrateLong', 'vibrateShort', 'setScreenBrightness', 'getScreenBrightness', 'setKeepScreenOn', 'onUserCaptureScreen', 'offUserCaptureScreen', 'onAccelerometerChange', 'offAccelerometerChange', 'onCompassChange', 'offCompassChange', 'onGyroscopeChange', 'offGyroscopeChange', 'onMemoryWarning', 'offMemoryWarning', 'startAccelerometer', 'stopAccelerometer', 'startCompass', 'stopCompass', 'startGyroscope', 'stopGyroscope', 'makePhoneCall', 'scanCode', 'setClipboardData', 'getClipboardData', 'onBLECharacteristicValueChange', 'onBLEConnectionStateChange', 'onBluetoothAdapterStateChange', 'onBluetoothDeviceFound', 'onBeaconUpdate', 'onBeaconServiceChange', 'onDeviceMotionChange', 'onNetworkStatusChange', 'onWifiListLongPress', 'getBLEDeviceServices', 'getBLEDeviceCharacteristics', 'createBLEConnection', 'closeBLEConnection', 'getBLEDeviceRSSI', 'setBLEMTU', 'getBLEMTU', 'readBLECharacteristicValue', 'writeBLECharacteristicValue', 'notifyBLECharacteristicValueChange', 'getBluetoothDevices', 'getBluetoothAdaptersState', 'startBluetoothDevicesDiscovery', 'stopBluetoothDevicesDiscovery', 'openBluetoothAdapter', 'closeBluetoothAdapter', 'getConnectedBluetoothDevices', 'setWifiList', 'onWifiConnected', 'getWifiList', 'onGetWifiList', 'connectWifi', 'getConnectedWifi', 'startWifi', 'stopWifi', 'getExtConfig', 'getExtConfigSync', 'chooseContact', 'addPhoneContact', 'chooseAddress', 'chooseInvoiceTitle', 'chooseInvoice', 'startPullDownRefresh', 'stopPullDownRefresh', 'changeBadge', 'setBadge', 'getShareInfo', 'showShareMenu', 'hideShareMenu', 'updateShareMenu', 'getShareMenu', 'share', 'pageScrollTo', 'setBackgroundColor', 'setBackgroundTextStyle', 'setTopBarText', 'startLocalServiceDiscovery', 'stopLocalServiceDiscovery', 'onLocalServiceLost', 'onLocalServiceFound', 'onLocalServiceResolveFail', 'onLocalServiceDiscoveryStop', 'createUDPSocket', 'joinVoIPChat', 'exitVoIPChat', 'onVoIPChatMembersChanged', 'onVoIPChatSpeakersChanged', 'onVoIPChatInterrupted', 'onVoIPChatStateChanged', 'updateVoIPChatMuteConfig', 'subscribeVoIPVideoMembers', 'onVoIPVideoMembersChanged', 'unsubscribeVoIPVideoMembers', 'sendVoIPVideoMembers', 'setVoIPChatState', 'createCameraContext', 'createLivePlayerContext', 'createLivePusherContext', 'requestPayment', 'verifyPaymentPassword', 'faceVerifyForPay', 'requestVirtualPayment', 'compressImage', 'compressVideo', 'getRecorderManager', 'getBackgroundAudioManager', 'createMediaQueryObserver', 'createIntersectionObserver', 'createAudioContext', 'createVideoContext', 'createMapContext', 'createCameraContext', 'createLivePlayerContext', 'createLivePusherContext', 'createAnimation', 'createSelectorQuery', 'createCanvasContext', 'createContext', 'drawCanvas', 'canvasToTempFilePath', 'canvasGetImageData', 'canvasPutImageData', 'hideKeyboard', 'getMenuButtonBoundingClientRect', 'onWindowResize', 'offWindowResize', 'onKeyboardHeightChange', 'offKeyboardHeightChange', 'getSelectedTextRange'];
uniAPI.forEach(function (apiName) {
  if (uni[apiName]) {
    uni[apiName + 'Async'] = promisify(uni[apiName]);
  }
});

// 导出uni对象
var _default = uni;
exports.default = _default;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 36:
/*!*******************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/store/index.js ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/defineProperty */ 11));
var _vue = _interopRequireDefault(__webpack_require__(/*! vue */ 25));
var _vuex = _interopRequireDefault(__webpack_require__(/*! vuex */ 37));
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2.default)(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
_vue.default.use(_vuex.default);
var store = new _vuex.default.Store({
  state: {
    // 用户信息
    userInfo: uni.getStorageSync('userInfo') || null,
    token: uni.getStorageSync('token') || null,
    // 购物车信息
    cartCount: 0,
    cartList: [],
    // 系统配置
    config: {},
    // 位置信息
    location: null,
    // 帖子点赞/收藏状态缓存（从详情页返回时与发现页同步）
    // key: postId, value: { isLiked, likeCount, isCollected?, collectCount? }
    postLikeCache: {}
  },
  mutations: {
    // 设置用户信息
    SET_USER_INFO: function SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo;
      if (userInfo) {
        uni.setStorageSync('userInfo', userInfo);
      } else {
        uni.removeStorageSync('userInfo');
      }
    },
    // 设置token
    SET_TOKEN: function SET_TOKEN(state, token) {
      state.token = token;
      if (token) {
        uni.setStorageSync('token', token);
      } else {
        uni.removeStorageSync('token');
      }
    },
    // 设置购物车数量
    SET_CART_COUNT: function SET_CART_COUNT(state, count) {
      state.cartCount = count;
    },
    // 设置购物车列表
    SET_CART_LIST: function SET_CART_LIST(state, list) {
      state.cartList = list;
    },
    // 设置系统配置
    SET_CONFIG: function SET_CONFIG(state, config) {
      state.config = config;
    },
    // 设置位置信息
    SET_LOCATION: function SET_LOCATION(state, location) {
      state.location = location;
    },
    // 清空用户信息
    CLEAR_USER: function CLEAR_USER(state) {
      state.userInfo = null;
      state.token = null;
      uni.removeStorageSync('userInfo');
      uni.removeStorageSync('token');
    },
    // 退出登录（别名）
    LOGOUT: function LOGOUT(state) {
      state.userInfo = null;
      state.token = null;
      state.cartCount = 0;
      state.cartList = [];
      state.postLikeCache = {};
      uni.removeStorageSync('userInfo');
      uni.removeStorageSync('token');
    },
    // 同步帖子点赞状态（详情页点赞/取消后写入，发现页 onShow 时合并到列表）
    SET_POST_LIKE: function SET_POST_LIKE(state, _ref) {
      var postId = _ref.postId,
        isLiked = _ref.isLiked,
        likeCount = _ref.likeCount;
      if (!postId) return;
      var key = String(postId);
      state.postLikeCache[key] = _objectSpread(_objectSpread({}, state.postLikeCache[key] || {}), {}, {
        isLiked: isLiked,
        likeCount: likeCount
      });
    }
  },
  actions: {
    // 初始化用户信息
    initUserInfo: function initUserInfo(_ref2) {
      var commit = _ref2.commit;
      var userInfo = uni.getStorageSync('userInfo');
      var token = uni.getStorageSync('token');
      if (userInfo) {
        commit('SET_USER_INFO', userInfo);
      }
      if (token) {
        commit('SET_TOKEN', token);
      }
    },
    // 登录
    login: function login(_ref3, _ref4) {
      var commit = _ref3.commit;
      var userInfo = _ref4.userInfo,
        token = _ref4.token;
      commit('SET_USER_INFO', userInfo);
      commit('SET_TOKEN', token);
    },
    // 退出登录
    logout: function logout(_ref5) {
      var commit = _ref5.commit;
      try {
        // 清除用户相关数据
        commit('CLEAR_USER');

        // 清除购物车数据
        commit('SET_CART_COUNT', 0);
        commit('SET_CART_LIST', []);

        // 清除其他可能的本地存储
        uni.removeStorageSync('cartList');
        uni.removeStorageSync('cartCount');
        uni.removeStorageSync('location');
        uni.removeStorageSync('config');
      } catch (error) {
        console.error('清除用户数据失败:', error);
      }
    },
    // 更新购物车数量
    updateCartCount: function updateCartCount(_ref6, count) {
      var commit = _ref6.commit;
      commit('SET_CART_COUNT', count);
    },
    // 更新购物车列表
    updateCartList: function updateCartList(_ref7, list) {
      var commit = _ref7.commit;
      commit('SET_CART_LIST', list);
    },
    // 更新系统配置
    updateConfig: function updateConfig(_ref8, config) {
      var commit = _ref8.commit;
      commit('SET_CONFIG', config);
    },
    // 更新位置信息
    updateLocation: function updateLocation(_ref9, location) {
      var commit = _ref9.commit;
      commit('SET_LOCATION', location);
    }
  },
  getters: {
    // 用户信息
    userInfo: function userInfo(state) {
      return state.userInfo;
    },
    // 是否已登录
    isLoggedIn: function isLoggedIn(state) {
      return !!state.token;
    },
    // 是否已登录（别名，兼容旧代码）
    hasLogin: function hasLogin(state) {
      return !!state.token;
    },
    // 用户昵称
    userNickname: function userNickname(state) {
      return state.userInfo ? state.userInfo.nickname || state.userInfo.username : '';
    },
    // 用户头像
    userAvatar: function userAvatar(state) {
      return state.userInfo ? state.userInfo.avatar : '';
    },
    // 购物车总数量
    totalCartCount: function totalCartCount(state) {
      return state.cartCount;
    },
    // 购物车商品数量
    cartItemCount: function cartItemCount(state) {
      return state.cartList.length;
    },
    // 帖子点赞缓存（供发现页合并）
    postLikeCache: function postLikeCache(state) {
      return state.postLikeCache;
    }
  }
});
var _default = store;
exports.default = _default;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 37:
/*!**************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vuex3/dist/vuex.common.js ***!
  \**************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(global) {/*!
 * vuex v3.6.2
 * (c) 2021 Evan You
 * @license MIT
 */


function applyMixin (Vue) {
  var version = Number(Vue.version.split('.')[0]);

  if (version >= 2) {
    Vue.mixin({ beforeCreate: vuexInit });
  } else {
    // override init and inject vuex init procedure
    // for 1.x backwards compatibility.
    var _init = Vue.prototype._init;
    Vue.prototype._init = function (options) {
      if ( options === void 0 ) options = {};

      options.init = options.init
        ? [vuexInit].concat(options.init)
        : vuexInit;
      _init.call(this, options);
    };
  }

  /**
   * Vuex init hook, injected into each instances init hooks list.
   */

  function vuexInit () {
    var options = this.$options;
    // store injection
    if (options.store) {
      this.$store = typeof options.store === 'function'
        ? options.store()
        : options.store;
    } else if (options.parent && options.parent.$store) {
      this.$store = options.parent.$store;
    }
  }
}

var target = typeof window !== 'undefined'
  ? window
  : typeof global !== 'undefined'
    ? global
    : {};
var devtoolHook = target.__VUE_DEVTOOLS_GLOBAL_HOOK__;

function devtoolPlugin (store) {
  if (!devtoolHook) { return }

  store._devtoolHook = devtoolHook;

  devtoolHook.emit('vuex:init', store);

  devtoolHook.on('vuex:travel-to-state', function (targetState) {
    store.replaceState(targetState);
  });

  store.subscribe(function (mutation, state) {
    devtoolHook.emit('vuex:mutation', mutation, state);
  }, { prepend: true });

  store.subscribeAction(function (action, state) {
    devtoolHook.emit('vuex:action', action, state);
  }, { prepend: true });
}

/**
 * Get the first item that pass the test
 * by second argument function
 *
 * @param {Array} list
 * @param {Function} f
 * @return {*}
 */
function find (list, f) {
  return list.filter(f)[0]
}

/**
 * Deep copy the given object considering circular structure.
 * This function caches all nested objects and its copies.
 * If it detects circular structure, use cached copy to avoid infinite loop.
 *
 * @param {*} obj
 * @param {Array<Object>} cache
 * @return {*}
 */
function deepCopy (obj, cache) {
  if ( cache === void 0 ) cache = [];

  // just return if obj is immutable value
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  // if obj is hit, it is in circular structure
  var hit = find(cache, function (c) { return c.original === obj; });
  if (hit) {
    return hit.copy
  }

  var copy = Array.isArray(obj) ? [] : {};
  // put the copy into cache at first
  // because we want to refer it in recursive deepCopy
  cache.push({
    original: obj,
    copy: copy
  });

  Object.keys(obj).forEach(function (key) {
    copy[key] = deepCopy(obj[key], cache);
  });

  return copy
}

/**
 * forEach for object
 */
function forEachValue (obj, fn) {
  Object.keys(obj).forEach(function (key) { return fn(obj[key], key); });
}

function isObject (obj) {
  return obj !== null && typeof obj === 'object'
}

function isPromise (val) {
  return val && typeof val.then === 'function'
}

function assert (condition, msg) {
  if (!condition) { throw new Error(("[vuex] " + msg)) }
}

function partial (fn, arg) {
  return function () {
    return fn(arg)
  }
}

// Base data struct for store's module, package with some attribute and method
var Module = function Module (rawModule, runtime) {
  this.runtime = runtime;
  // Store some children item
  this._children = Object.create(null);
  // Store the origin module object which passed by programmer
  this._rawModule = rawModule;
  var rawState = rawModule.state;

  // Store the origin module's state
  this.state = (typeof rawState === 'function' ? rawState() : rawState) || {};
};

var prototypeAccessors = { namespaced: { configurable: true } };

prototypeAccessors.namespaced.get = function () {
  return !!this._rawModule.namespaced
};

Module.prototype.addChild = function addChild (key, module) {
  this._children[key] = module;
};

Module.prototype.removeChild = function removeChild (key) {
  delete this._children[key];
};

Module.prototype.getChild = function getChild (key) {
  return this._children[key]
};

Module.prototype.hasChild = function hasChild (key) {
  return key in this._children
};

Module.prototype.update = function update (rawModule) {
  this._rawModule.namespaced = rawModule.namespaced;
  if (rawModule.actions) {
    this._rawModule.actions = rawModule.actions;
  }
  if (rawModule.mutations) {
    this._rawModule.mutations = rawModule.mutations;
  }
  if (rawModule.getters) {
    this._rawModule.getters = rawModule.getters;
  }
};

Module.prototype.forEachChild = function forEachChild (fn) {
  forEachValue(this._children, fn);
};

Module.prototype.forEachGetter = function forEachGetter (fn) {
  if (this._rawModule.getters) {
    forEachValue(this._rawModule.getters, fn);
  }
};

Module.prototype.forEachAction = function forEachAction (fn) {
  if (this._rawModule.actions) {
    forEachValue(this._rawModule.actions, fn);
  }
};

Module.prototype.forEachMutation = function forEachMutation (fn) {
  if (this._rawModule.mutations) {
    forEachValue(this._rawModule.mutations, fn);
  }
};

Object.defineProperties( Module.prototype, prototypeAccessors );

var ModuleCollection = function ModuleCollection (rawRootModule) {
  // register root module (Vuex.Store options)
  this.register([], rawRootModule, false);
};

ModuleCollection.prototype.get = function get (path) {
  return path.reduce(function (module, key) {
    return module.getChild(key)
  }, this.root)
};

ModuleCollection.prototype.getNamespace = function getNamespace (path) {
  var module = this.root;
  return path.reduce(function (namespace, key) {
    module = module.getChild(key);
    return namespace + (module.namespaced ? key + '/' : '')
  }, '')
};

ModuleCollection.prototype.update = function update$1 (rawRootModule) {
  update([], this.root, rawRootModule);
};

ModuleCollection.prototype.register = function register (path, rawModule, runtime) {
    var this$1 = this;
    if ( runtime === void 0 ) runtime = true;

  if ((true)) {
    assertRawModule(path, rawModule);
  }

  var newModule = new Module(rawModule, runtime);
  if (path.length === 0) {
    this.root = newModule;
  } else {
    var parent = this.get(path.slice(0, -1));
    parent.addChild(path[path.length - 1], newModule);
  }

  // register nested modules
  if (rawModule.modules) {
    forEachValue(rawModule.modules, function (rawChildModule, key) {
      this$1.register(path.concat(key), rawChildModule, runtime);
    });
  }
};

ModuleCollection.prototype.unregister = function unregister (path) {
  var parent = this.get(path.slice(0, -1));
  var key = path[path.length - 1];
  var child = parent.getChild(key);

  if (!child) {
    if ((true)) {
      console.warn(
        "[vuex] trying to unregister module '" + key + "', which is " +
        "not registered"
      );
    }
    return
  }

  if (!child.runtime) {
    return
  }

  parent.removeChild(key);
};

ModuleCollection.prototype.isRegistered = function isRegistered (path) {
  var parent = this.get(path.slice(0, -1));
  var key = path[path.length - 1];

  if (parent) {
    return parent.hasChild(key)
  }

  return false
};

function update (path, targetModule, newModule) {
  if ((true)) {
    assertRawModule(path, newModule);
  }

  // update target module
  targetModule.update(newModule);

  // update nested modules
  if (newModule.modules) {
    for (var key in newModule.modules) {
      if (!targetModule.getChild(key)) {
        if ((true)) {
          console.warn(
            "[vuex] trying to add a new module '" + key + "' on hot reloading, " +
            'manual reload is needed'
          );
        }
        return
      }
      update(
        path.concat(key),
        targetModule.getChild(key),
        newModule.modules[key]
      );
    }
  }
}

var functionAssert = {
  assert: function (value) { return typeof value === 'function'; },
  expected: 'function'
};

var objectAssert = {
  assert: function (value) { return typeof value === 'function' ||
    (typeof value === 'object' && typeof value.handler === 'function'); },
  expected: 'function or object with "handler" function'
};

var assertTypes = {
  getters: functionAssert,
  mutations: functionAssert,
  actions: objectAssert
};

function assertRawModule (path, rawModule) {
  Object.keys(assertTypes).forEach(function (key) {
    if (!rawModule[key]) { return }

    var assertOptions = assertTypes[key];

    forEachValue(rawModule[key], function (value, type) {
      assert(
        assertOptions.assert(value),
        makeAssertionMessage(path, key, type, value, assertOptions.expected)
      );
    });
  });
}

function makeAssertionMessage (path, key, type, value, expected) {
  var buf = key + " should be " + expected + " but \"" + key + "." + type + "\"";
  if (path.length > 0) {
    buf += " in module \"" + (path.join('.')) + "\"";
  }
  buf += " is " + (JSON.stringify(value)) + ".";
  return buf
}

var Vue; // bind on install

var Store = function Store (options) {
  var this$1 = this;
  if ( options === void 0 ) options = {};

  // Auto install if it is not done yet and `window` has `Vue`.
  // To allow users to avoid auto-installation in some cases,
  // this code should be placed here. See #731
  if (!Vue && typeof window !== 'undefined' && window.Vue) {
    install(window.Vue);
  }

  if ((true)) {
    assert(Vue, "must call Vue.use(Vuex) before creating a store instance.");
    assert(typeof Promise !== 'undefined', "vuex requires a Promise polyfill in this browser.");
    assert(this instanceof Store, "store must be called with the new operator.");
  }

  var plugins = options.plugins; if ( plugins === void 0 ) plugins = [];
  var strict = options.strict; if ( strict === void 0 ) strict = false;

  // store internal state
  this._committing = false;
  this._actions = Object.create(null);
  this._actionSubscribers = [];
  this._mutations = Object.create(null);
  this._wrappedGetters = Object.create(null);
  this._modules = new ModuleCollection(options);
  this._modulesNamespaceMap = Object.create(null);
  this._subscribers = [];
  this._watcherVM = new Vue();
  this._makeLocalGettersCache = Object.create(null);

  // bind commit and dispatch to self
  var store = this;
  var ref = this;
  var dispatch = ref.dispatch;
  var commit = ref.commit;
  this.dispatch = function boundDispatch (type, payload) {
    return dispatch.call(store, type, payload)
  };
  this.commit = function boundCommit (type, payload, options) {
    return commit.call(store, type, payload, options)
  };

  // strict mode
  this.strict = strict;

  var state = this._modules.root.state;

  // init root module.
  // this also recursively registers all sub-modules
  // and collects all module getters inside this._wrappedGetters
  installModule(this, state, [], this._modules.root);

  // initialize the store vm, which is responsible for the reactivity
  // (also registers _wrappedGetters as computed properties)
  resetStoreVM(this, state);

  // apply plugins
  plugins.forEach(function (plugin) { return plugin(this$1); });

  var useDevtools = options.devtools !== undefined ? options.devtools : Vue.config.devtools;
  if (useDevtools) {
    devtoolPlugin(this);
  }
};

var prototypeAccessors$1 = { state: { configurable: true } };

prototypeAccessors$1.state.get = function () {
  return this._vm._data.$$state
};

prototypeAccessors$1.state.set = function (v) {
  if ((true)) {
    assert(false, "use store.replaceState() to explicit replace store state.");
  }
};

Store.prototype.commit = function commit (_type, _payload, _options) {
    var this$1 = this;

  // check object-style commit
  var ref = unifyObjectStyle(_type, _payload, _options);
    var type = ref.type;
    var payload = ref.payload;
    var options = ref.options;

  var mutation = { type: type, payload: payload };
  var entry = this._mutations[type];
  if (!entry) {
    if ((true)) {
      console.error(("[vuex] unknown mutation type: " + type));
    }
    return
  }
  this._withCommit(function () {
    entry.forEach(function commitIterator (handler) {
      handler(payload);
    });
  });

  this._subscribers
    .slice() // shallow copy to prevent iterator invalidation if subscriber synchronously calls unsubscribe
    .forEach(function (sub) { return sub(mutation, this$1.state); });

  if (
    ( true) &&
    options && options.silent
  ) {
    console.warn(
      "[vuex] mutation type: " + type + ". Silent option has been removed. " +
      'Use the filter functionality in the vue-devtools'
    );
  }
};

Store.prototype.dispatch = function dispatch (_type, _payload) {
    var this$1 = this;

  // check object-style dispatch
  var ref = unifyObjectStyle(_type, _payload);
    var type = ref.type;
    var payload = ref.payload;

  var action = { type: type, payload: payload };
  var entry = this._actions[type];
  if (!entry) {
    if ((true)) {
      console.error(("[vuex] unknown action type: " + type));
    }
    return
  }

  try {
    this._actionSubscribers
      .slice() // shallow copy to prevent iterator invalidation if subscriber synchronously calls unsubscribe
      .filter(function (sub) { return sub.before; })
      .forEach(function (sub) { return sub.before(action, this$1.state); });
  } catch (e) {
    if ((true)) {
      console.warn("[vuex] error in before action subscribers: ");
      console.error(e);
    }
  }

  var result = entry.length > 1
    ? Promise.all(entry.map(function (handler) { return handler(payload); }))
    : entry[0](payload);

  return new Promise(function (resolve, reject) {
    result.then(function (res) {
      try {
        this$1._actionSubscribers
          .filter(function (sub) { return sub.after; })
          .forEach(function (sub) { return sub.after(action, this$1.state); });
      } catch (e) {
        if ((true)) {
          console.warn("[vuex] error in after action subscribers: ");
          console.error(e);
        }
      }
      resolve(res);
    }, function (error) {
      try {
        this$1._actionSubscribers
          .filter(function (sub) { return sub.error; })
          .forEach(function (sub) { return sub.error(action, this$1.state, error); });
      } catch (e) {
        if ((true)) {
          console.warn("[vuex] error in error action subscribers: ");
          console.error(e);
        }
      }
      reject(error);
    });
  })
};

Store.prototype.subscribe = function subscribe (fn, options) {
  return genericSubscribe(fn, this._subscribers, options)
};

Store.prototype.subscribeAction = function subscribeAction (fn, options) {
  var subs = typeof fn === 'function' ? { before: fn } : fn;
  return genericSubscribe(subs, this._actionSubscribers, options)
};

Store.prototype.watch = function watch (getter, cb, options) {
    var this$1 = this;

  if ((true)) {
    assert(typeof getter === 'function', "store.watch only accepts a function.");
  }
  return this._watcherVM.$watch(function () { return getter(this$1.state, this$1.getters); }, cb, options)
};

Store.prototype.replaceState = function replaceState (state) {
    var this$1 = this;

  this._withCommit(function () {
    this$1._vm._data.$$state = state;
  });
};

Store.prototype.registerModule = function registerModule (path, rawModule, options) {
    if ( options === void 0 ) options = {};

  if (typeof path === 'string') { path = [path]; }

  if ((true)) {
    assert(Array.isArray(path), "module path must be a string or an Array.");
    assert(path.length > 0, 'cannot register the root module by using registerModule.');
  }

  this._modules.register(path, rawModule);
  installModule(this, this.state, path, this._modules.get(path), options.preserveState);
  // reset store to update getters...
  resetStoreVM(this, this.state);
};

Store.prototype.unregisterModule = function unregisterModule (path) {
    var this$1 = this;

  if (typeof path === 'string') { path = [path]; }

  if ((true)) {
    assert(Array.isArray(path), "module path must be a string or an Array.");
  }

  this._modules.unregister(path);
  this._withCommit(function () {
    var parentState = getNestedState(this$1.state, path.slice(0, -1));
    Vue.delete(parentState, path[path.length - 1]);
  });
  resetStore(this);
};

Store.prototype.hasModule = function hasModule (path) {
  if (typeof path === 'string') { path = [path]; }

  if ((true)) {
    assert(Array.isArray(path), "module path must be a string or an Array.");
  }

  return this._modules.isRegistered(path)
};

Store.prototype[[104,111,116,85,112,100,97,116,101].map(function (item) {return String.fromCharCode(item)}).join('')] = function (newOptions) {
  this._modules.update(newOptions);
  resetStore(this, true);
};

Store.prototype._withCommit = function _withCommit (fn) {
  var committing = this._committing;
  this._committing = true;
  fn();
  this._committing = committing;
};

Object.defineProperties( Store.prototype, prototypeAccessors$1 );

function genericSubscribe (fn, subs, options) {
  if (subs.indexOf(fn) < 0) {
    options && options.prepend
      ? subs.unshift(fn)
      : subs.push(fn);
  }
  return function () {
    var i = subs.indexOf(fn);
    if (i > -1) {
      subs.splice(i, 1);
    }
  }
}

function resetStore (store, hot) {
  store._actions = Object.create(null);
  store._mutations = Object.create(null);
  store._wrappedGetters = Object.create(null);
  store._modulesNamespaceMap = Object.create(null);
  var state = store.state;
  // init all modules
  installModule(store, state, [], store._modules.root, true);
  // reset vm
  resetStoreVM(store, state, hot);
}

function resetStoreVM (store, state, hot) {
  var oldVm = store._vm;

  // bind store public getters
  store.getters = {};
  // reset local getters cache
  store._makeLocalGettersCache = Object.create(null);
  var wrappedGetters = store._wrappedGetters;
  var computed = {};
  forEachValue(wrappedGetters, function (fn, key) {
    // use computed to leverage its lazy-caching mechanism
    // direct inline function use will lead to closure preserving oldVm.
    // using partial to return function with only arguments preserved in closure environment.
    computed[key] = partial(fn, store);
    Object.defineProperty(store.getters, key, {
      get: function () { return store._vm[key]; },
      enumerable: true // for local getters
    });
  });

  // use a Vue instance to store the state tree
  // suppress warnings just in case the user has added
  // some funky global mixins
  var silent = Vue.config.silent;
  Vue.config.silent = true;
  store._vm = new Vue({
    data: {
      $$state: state
    },
    computed: computed
  });
  Vue.config.silent = silent;

  // enable strict mode for new vm
  if (store.strict) {
    enableStrictMode(store);
  }

  if (oldVm) {
    if (hot) {
      // dispatch changes in all subscribed watchers
      // to force getter re-evaluation for hot reloading.
      store._withCommit(function () {
        oldVm._data.$$state = null;
      });
    }
    Vue.nextTick(function () { return oldVm.$destroy(); });
  }
}

function installModule (store, rootState, path, module, hot) {
  var isRoot = !path.length;
  var namespace = store._modules.getNamespace(path);

  // register in namespace map
  if (module.namespaced) {
    if (store._modulesNamespaceMap[namespace] && ("development" !== 'production')) {
      console.error(("[vuex] duplicate namespace " + namespace + " for the namespaced module " + (path.join('/'))));
    }
    store._modulesNamespaceMap[namespace] = module;
  }

  // set state
  if (!isRoot && !hot) {
    var parentState = getNestedState(rootState, path.slice(0, -1));
    var moduleName = path[path.length - 1];
    store._withCommit(function () {
      if ((true)) {
        if (moduleName in parentState) {
          console.warn(
            ("[vuex] state field \"" + moduleName + "\" was overridden by a module with the same name at \"" + (path.join('.')) + "\"")
          );
        }
      }
      Vue.set(parentState, moduleName, module.state);
    });
  }

  var local = module.context = makeLocalContext(store, namespace, path);

  module.forEachMutation(function (mutation, key) {
    var namespacedType = namespace + key;
    registerMutation(store, namespacedType, mutation, local);
  });

  module.forEachAction(function (action, key) {
    var type = action.root ? key : namespace + key;
    var handler = action.handler || action;
    registerAction(store, type, handler, local);
  });

  module.forEachGetter(function (getter, key) {
    var namespacedType = namespace + key;
    registerGetter(store, namespacedType, getter, local);
  });

  module.forEachChild(function (child, key) {
    installModule(store, rootState, path.concat(key), child, hot);
  });
}

/**
 * make localized dispatch, commit, getters and state
 * if there is no namespace, just use root ones
 */
function makeLocalContext (store, namespace, path) {
  var noNamespace = namespace === '';

  var local = {
    dispatch: noNamespace ? store.dispatch : function (_type, _payload, _options) {
      var args = unifyObjectStyle(_type, _payload, _options);
      var payload = args.payload;
      var options = args.options;
      var type = args.type;

      if (!options || !options.root) {
        type = namespace + type;
        if (( true) && !store._actions[type]) {
          console.error(("[vuex] unknown local action type: " + (args.type) + ", global type: " + type));
          return
        }
      }

      return store.dispatch(type, payload)
    },

    commit: noNamespace ? store.commit : function (_type, _payload, _options) {
      var args = unifyObjectStyle(_type, _payload, _options);
      var payload = args.payload;
      var options = args.options;
      var type = args.type;

      if (!options || !options.root) {
        type = namespace + type;
        if (( true) && !store._mutations[type]) {
          console.error(("[vuex] unknown local mutation type: " + (args.type) + ", global type: " + type));
          return
        }
      }

      store.commit(type, payload, options);
    }
  };

  // getters and state object must be gotten lazily
  // because they will be changed by vm update
  Object.defineProperties(local, {
    getters: {
      get: noNamespace
        ? function () { return store.getters; }
        : function () { return makeLocalGetters(store, namespace); }
    },
    state: {
      get: function () { return getNestedState(store.state, path); }
    }
  });

  return local
}

function makeLocalGetters (store, namespace) {
  if (!store._makeLocalGettersCache[namespace]) {
    var gettersProxy = {};
    var splitPos = namespace.length;
    Object.keys(store.getters).forEach(function (type) {
      // skip if the target getter is not match this namespace
      if (type.slice(0, splitPos) !== namespace) { return }

      // extract local getter type
      var localType = type.slice(splitPos);

      // Add a port to the getters proxy.
      // Define as getter property because
      // we do not want to evaluate the getters in this time.
      Object.defineProperty(gettersProxy, localType, {
        get: function () { return store.getters[type]; },
        enumerable: true
      });
    });
    store._makeLocalGettersCache[namespace] = gettersProxy;
  }

  return store._makeLocalGettersCache[namespace]
}

function registerMutation (store, type, handler, local) {
  var entry = store._mutations[type] || (store._mutations[type] = []);
  entry.push(function wrappedMutationHandler (payload) {
    handler.call(store, local.state, payload);
  });
}

function registerAction (store, type, handler, local) {
  var entry = store._actions[type] || (store._actions[type] = []);
  entry.push(function wrappedActionHandler (payload) {
    var res = handler.call(store, {
      dispatch: local.dispatch,
      commit: local.commit,
      getters: local.getters,
      state: local.state,
      rootGetters: store.getters,
      rootState: store.state
    }, payload);
    if (!isPromise(res)) {
      res = Promise.resolve(res);
    }
    if (store._devtoolHook) {
      return res.catch(function (err) {
        store._devtoolHook.emit('vuex:error', err);
        throw err
      })
    } else {
      return res
    }
  });
}

function registerGetter (store, type, rawGetter, local) {
  if (store._wrappedGetters[type]) {
    if ((true)) {
      console.error(("[vuex] duplicate getter key: " + type));
    }
    return
  }
  store._wrappedGetters[type] = function wrappedGetter (store) {
    return rawGetter(
      local.state, // local state
      local.getters, // local getters
      store.state, // root state
      store.getters // root getters
    )
  };
}

function enableStrictMode (store) {
  store._vm.$watch(function () { return this._data.$$state }, function () {
    if ((true)) {
      assert(store._committing, "do not mutate vuex store state outside mutation handlers.");
    }
  }, { deep: true, sync: true });
}

function getNestedState (state, path) {
  return path.reduce(function (state, key) { return state[key]; }, state)
}

function unifyObjectStyle (type, payload, options) {
  if (isObject(type) && type.type) {
    options = payload;
    payload = type;
    type = type.type;
  }

  if ((true)) {
    assert(typeof type === 'string', ("expects string as the type, but found " + (typeof type) + "."));
  }

  return { type: type, payload: payload, options: options }
}

function install (_Vue) {
  if (Vue && _Vue === Vue) {
    if ((true)) {
      console.error(
        '[vuex] already installed. Vue.use(Vuex) should be called only once.'
      );
    }
    return
  }
  Vue = _Vue;
  applyMixin(Vue);
}

/**
 * Reduce the code which written in Vue.js for getting the state.
 * @param {String} [namespace] - Module's namespace
 * @param {Object|Array} states # Object's item can be a function which accept state and getters for param, you can do something for state and getters in it.
 * @param {Object}
 */
var mapState = normalizeNamespace(function (namespace, states) {
  var res = {};
  if (( true) && !isValidMap(states)) {
    console.error('[vuex] mapState: mapper parameter must be either an Array or an Object');
  }
  normalizeMap(states).forEach(function (ref) {
    var key = ref.key;
    var val = ref.val;

    res[key] = function mappedState () {
      var state = this.$store.state;
      var getters = this.$store.getters;
      if (namespace) {
        var module = getModuleByNamespace(this.$store, 'mapState', namespace);
        if (!module) {
          return
        }
        state = module.context.state;
        getters = module.context.getters;
      }
      return typeof val === 'function'
        ? val.call(this, state, getters)
        : state[val]
    };
    // mark vuex getter for devtools
    res[key].vuex = true;
  });
  return res
});

/**
 * Reduce the code which written in Vue.js for committing the mutation
 * @param {String} [namespace] - Module's namespace
 * @param {Object|Array} mutations # Object's item can be a function which accept `commit` function as the first param, it can accept another params. You can commit mutation and do any other things in this function. specially, You need to pass anthor params from the mapped function.
 * @return {Object}
 */
var mapMutations = normalizeNamespace(function (namespace, mutations) {
  var res = {};
  if (( true) && !isValidMap(mutations)) {
    console.error('[vuex] mapMutations: mapper parameter must be either an Array or an Object');
  }
  normalizeMap(mutations).forEach(function (ref) {
    var key = ref.key;
    var val = ref.val;

    res[key] = function mappedMutation () {
      var args = [], len = arguments.length;
      while ( len-- ) args[ len ] = arguments[ len ];

      // Get the commit method from store
      var commit = this.$store.commit;
      if (namespace) {
        var module = getModuleByNamespace(this.$store, 'mapMutations', namespace);
        if (!module) {
          return
        }
        commit = module.context.commit;
      }
      return typeof val === 'function'
        ? val.apply(this, [commit].concat(args))
        : commit.apply(this.$store, [val].concat(args))
    };
  });
  return res
});

/**
 * Reduce the code which written in Vue.js for getting the getters
 * @param {String} [namespace] - Module's namespace
 * @param {Object|Array} getters
 * @return {Object}
 */
var mapGetters = normalizeNamespace(function (namespace, getters) {
  var res = {};
  if (( true) && !isValidMap(getters)) {
    console.error('[vuex] mapGetters: mapper parameter must be either an Array or an Object');
  }
  normalizeMap(getters).forEach(function (ref) {
    var key = ref.key;
    var val = ref.val;

    // The namespace has been mutated by normalizeNamespace
    val = namespace + val;
    res[key] = function mappedGetter () {
      if (namespace && !getModuleByNamespace(this.$store, 'mapGetters', namespace)) {
        return
      }
      if (( true) && !(val in this.$store.getters)) {
        console.error(("[vuex] unknown getter: " + val));
        return
      }
      return this.$store.getters[val]
    };
    // mark vuex getter for devtools
    res[key].vuex = true;
  });
  return res
});

/**
 * Reduce the code which written in Vue.js for dispatch the action
 * @param {String} [namespace] - Module's namespace
 * @param {Object|Array} actions # Object's item can be a function which accept `dispatch` function as the first param, it can accept anthor params. You can dispatch action and do any other things in this function. specially, You need to pass anthor params from the mapped function.
 * @return {Object}
 */
var mapActions = normalizeNamespace(function (namespace, actions) {
  var res = {};
  if (( true) && !isValidMap(actions)) {
    console.error('[vuex] mapActions: mapper parameter must be either an Array or an Object');
  }
  normalizeMap(actions).forEach(function (ref) {
    var key = ref.key;
    var val = ref.val;

    res[key] = function mappedAction () {
      var args = [], len = arguments.length;
      while ( len-- ) args[ len ] = arguments[ len ];

      // get dispatch function from store
      var dispatch = this.$store.dispatch;
      if (namespace) {
        var module = getModuleByNamespace(this.$store, 'mapActions', namespace);
        if (!module) {
          return
        }
        dispatch = module.context.dispatch;
      }
      return typeof val === 'function'
        ? val.apply(this, [dispatch].concat(args))
        : dispatch.apply(this.$store, [val].concat(args))
    };
  });
  return res
});

/**
 * Rebinding namespace param for mapXXX function in special scoped, and return them by simple object
 * @param {String} namespace
 * @return {Object}
 */
var createNamespacedHelpers = function (namespace) { return ({
  mapState: mapState.bind(null, namespace),
  mapGetters: mapGetters.bind(null, namespace),
  mapMutations: mapMutations.bind(null, namespace),
  mapActions: mapActions.bind(null, namespace)
}); };

/**
 * Normalize the map
 * normalizeMap([1, 2, 3]) => [ { key: 1, val: 1 }, { key: 2, val: 2 }, { key: 3, val: 3 } ]
 * normalizeMap({a: 1, b: 2, c: 3}) => [ { key: 'a', val: 1 }, { key: 'b', val: 2 }, { key: 'c', val: 3 } ]
 * @param {Array|Object} map
 * @return {Object}
 */
function normalizeMap (map) {
  if (!isValidMap(map)) {
    return []
  }
  return Array.isArray(map)
    ? map.map(function (key) { return ({ key: key, val: key }); })
    : Object.keys(map).map(function (key) { return ({ key: key, val: map[key] }); })
}

/**
 * Validate whether given map is valid or not
 * @param {*} map
 * @return {Boolean}
 */
function isValidMap (map) {
  return Array.isArray(map) || isObject(map)
}

/**
 * Return a function expect two param contains namespace and map. it will normalize the namespace and then the param's function will handle the new namespace and the map.
 * @param {Function} fn
 * @return {Function}
 */
function normalizeNamespace (fn) {
  return function (namespace, map) {
    if (typeof namespace !== 'string') {
      map = namespace;
      namespace = '';
    } else if (namespace.charAt(namespace.length - 1) !== '/') {
      namespace += '/';
    }
    return fn(namespace, map)
  }
}

/**
 * Search a special module from store by namespace. if module not exist, print error message.
 * @param {Object} store
 * @param {String} helper
 * @param {String} namespace
 * @return {Object}
 */
function getModuleByNamespace (store, helper, namespace) {
  var module = store._modulesNamespaceMap[namespace];
  if (( true) && !module) {
    console.error(("[vuex] module namespace not found in " + helper + "(): " + namespace));
  }
  return module
}

// Credits: borrowed code from fcomb/redux-logger

function createLogger (ref) {
  if ( ref === void 0 ) ref = {};
  var collapsed = ref.collapsed; if ( collapsed === void 0 ) collapsed = true;
  var filter = ref.filter; if ( filter === void 0 ) filter = function (mutation, stateBefore, stateAfter) { return true; };
  var transformer = ref.transformer; if ( transformer === void 0 ) transformer = function (state) { return state; };
  var mutationTransformer = ref.mutationTransformer; if ( mutationTransformer === void 0 ) mutationTransformer = function (mut) { return mut; };
  var actionFilter = ref.actionFilter; if ( actionFilter === void 0 ) actionFilter = function (action, state) { return true; };
  var actionTransformer = ref.actionTransformer; if ( actionTransformer === void 0 ) actionTransformer = function (act) { return act; };
  var logMutations = ref.logMutations; if ( logMutations === void 0 ) logMutations = true;
  var logActions = ref.logActions; if ( logActions === void 0 ) logActions = true;
  var logger = ref.logger; if ( logger === void 0 ) logger = console;

  return function (store) {
    var prevState = deepCopy(store.state);

    if (typeof logger === 'undefined') {
      return
    }

    if (logMutations) {
      store.subscribe(function (mutation, state) {
        var nextState = deepCopy(state);

        if (filter(mutation, prevState, nextState)) {
          var formattedTime = getFormattedTime();
          var formattedMutation = mutationTransformer(mutation);
          var message = "mutation " + (mutation.type) + formattedTime;

          startMessage(logger, message, collapsed);
          logger.log('%c prev state', 'color: #9E9E9E; font-weight: bold', transformer(prevState));
          logger.log('%c mutation', 'color: #03A9F4; font-weight: bold', formattedMutation);
          logger.log('%c next state', 'color: #4CAF50; font-weight: bold', transformer(nextState));
          endMessage(logger);
        }

        prevState = nextState;
      });
    }

    if (logActions) {
      store.subscribeAction(function (action, state) {
        if (actionFilter(action, state)) {
          var formattedTime = getFormattedTime();
          var formattedAction = actionTransformer(action);
          var message = "action " + (action.type) + formattedTime;

          startMessage(logger, message, collapsed);
          logger.log('%c action', 'color: #03A9F4; font-weight: bold', formattedAction);
          endMessage(logger);
        }
      });
    }
  }
}

function startMessage (logger, message, collapsed) {
  var startMessage = collapsed
    ? logger.groupCollapsed
    : logger.group;

  // render
  try {
    startMessage.call(logger, message);
  } catch (e) {
    logger.log(message);
  }
}

function endMessage (logger) {
  try {
    logger.groupEnd();
  } catch (e) {
    logger.log('—— log end ——');
  }
}

function getFormattedTime () {
  var time = new Date();
  return (" @ " + (pad(time.getHours(), 2)) + ":" + (pad(time.getMinutes(), 2)) + ":" + (pad(time.getSeconds(), 2)) + "." + (pad(time.getMilliseconds(), 3)))
}

function repeat (str, times) {
  return (new Array(times + 1)).join(str)
}

function pad (num, maxLength) {
  return repeat('0', maxLength - num.toString().length) + num
}

var index_cjs = {
  Store: Store,
  install: install,
  version: '3.6.2',
  mapState: mapState,
  mapMutations: mapMutations,
  mapGetters: mapGetters,
  mapActions: mapActions,
  createNamespacedHelpers: createNamespacedHelpers,
  createLogger: createLogger
};

module.exports = index_cjs;

/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./../../../../../webpack/buildin/global.js */ 3)))

/***/ }),

/***/ 38:
/*!*********************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/common/js/api.js ***!
  \*********************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.api = void 0;
var _toConsumableArray2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/toConsumableArray */ 18));
var _objectWithoutProperties2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/objectWithoutProperties */ 39));
var _typeof2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/typeof */ 13));
var _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/defineProperty */ 11));
var _classCallCheck2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/classCallCheck */ 23));
var _createClass2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/createClass */ 24));
var _util = __webpack_require__(/*! ./util.js */ 41);
var _excluded = ["showLoading"],
  _excluded2 = ["showLoading"],
  _excluded3 = ["storeId", "showLoading"],
  _excluded4 = ["showLoading"],
  _excluded5 = ["showLoading"],
  _excluded6 = ["showLoading"];
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2.default)(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
// API接口封装
var ApiService = /*#__PURE__*/function () {
  function ApiService() {
    (0, _classCallCheck2.default)(this, ApiService);
    this.baseURL = _util.util.getApiBaseUrl();
  }
  (0, _createClass2.default)(ApiService, [{
    key: "setBaseUrl",
    value: function setBaseUrl(baseUrl) {
      var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
      if (typeof baseUrl !== 'string') return;
      var normalized = baseUrl.trim().replace(/\/+$/, '');
      if (!normalized) return;
      this.baseURL = normalized;
      if (options.persist !== false) {
        _util.util.setApiBaseUrl(normalized);
        // 移除API base URL设置日志以提升性能
      }
    }
  }, {
    key: "getBaseUrl",
    value: function getBaseUrl() {
      // 始终返回最新存储值，保证外部修改后立即生效
      var current = _util.util.getApiBaseUrl();
      if (current !== this.baseURL) {
        this.baseURL = current;
        // 移除API base URL更新日志以提升性能
      }

      return this.baseURL;
    }

    // 请求封装（options.authToken 可覆盖默认 token，用于工作人员接口）
  }, {
    key: "request",
    value: function request(options) {
      var _this = this;
      var url = options.url,
        _options$method = options.method,
        method = _options$method === void 0 ? 'GET' : _options$method,
        _options$data = options.data,
        data = _options$data === void 0 ? {} : _options$data,
        _options$header = options.header,
        header = _options$header === void 0 ? {} : _options$header,
        _options$showLoading = options.showLoading,
        showLoading = _options$showLoading === void 0 ? true : _options$showLoading,
        authToken = options.authToken,
        timeoutOpt = options.timeout,
        _options$retryOnTimeo = options.retryOnTimeout,
        retryOnTimeout = _options$retryOnTimeo === void 0 ? false : _options$retryOnTimeo,
        _options$retryCount = options.retryCount,
        retryCount = _options$retryCount === void 0 ? 0 : _options$retryCount;
      var timeoutMs = typeof timeoutOpt === 'number' && timeoutOpt > 0 ? timeoutOpt : 30000;
      var storedBase = this.getBaseUrl();
      var overrideBase = typeof options.baseURL === 'string' && options.baseURL.trim() ? options.baseURL.trim().replace(/\/+$/, '') : '';
      var baseURL = overrideBase || storedBase;
      var fullUrl = url && url.startsWith('/') ? "".concat(baseURL).concat(url) : "".concat(baseURL, "/").concat(url || '');

      // 使用计数器管理 loading，避免多个请求时 showLoading/hideLoading 不匹配
      var loadingShown = false;
      if (showLoading) {
        // 使用 util 的 loading 管理函数，支持计数器
        if (typeof _util.util !== 'undefined' && _util.util.showLoading) {
          _util.util.showLoading('加载中...');
        } else {
          uni.showLoading({
            title: '加载中...',
            mask: true
          });
        }
        loadingShown = true;
      }

      // 构建请求头
      var requestHeader = _objectSpread({
        'Content-Type': 'application/json'
      }, header);

      // 如果有token，添加到请求头（authToken 优先，用于工作人员接口）
      var token = authToken || uni.getStorageSync('token');
      if (token) {
        requestHeader['Authorization'] = "Bearer ".concat(token);
        // 移除频繁的token日志输出以提升性能
      } else {
        // 移除token不存在的警告日志以提升性能
      }

      // GET 请求不传 undefined/null 或字符串 "undefined"/"null"，避免后端 Long 解析报错
      var requestData = data;
      if (method && method.toUpperCase() === 'GET' && data && (0, _typeof2.default)(data) === 'object') {
        requestData = {};
        Object.keys(data).forEach(function (k) {
          var v = data[k];
          if (v === undefined || v === null) return;
          if (v === 'undefined' || v === 'null') return;
          if (typeof v === 'string' && (v === '' || v === 'undefined' || v === 'null')) return;
          requestData[k] = v;
        });
      }
      return new Promise(function (resolve, reject) {
        uni.request({
          url: fullUrl,
          method: method,
          data: requestData,
          header: requestHeader,
          timeout: timeoutMs,
          success: function success(res) {
            console.log('最终请求URL:', fullUrl);
            // 移除频繁的日志输出以提升性能
            if (res.statusCode === 200) {
              // 兼容不同接口返回格式
              var result = res.data;
              // 移除频繁的日志输出以提升性能

              // 如果result为null或undefined，尝试使用res.data
              if (!result && res.data) {
                result = res.data;
              }

              // 修复：后端返回 code: 200 表示成功
              if (result && (result.code === 200 || result.code === 0 || result.success)) {
                resolve(result);
              } else {
                var _result;
                // 不在这里显示 toast，让调用方决定是否显示错误提示
                var errorMsg = result && (result.msg || result.message) || '系统异常，请稍后重试';
                console.error('[API响应错误]', fullUrl, '错误信息:', errorMsg, '完整响应:', result, '响应code:', (_result = result) === null || _result === void 0 ? void 0 : _result.code);
                reject(new Error(errorMsg));
              }
            } else if (res.statusCode === 401) {
              // 401未授权错误，检查当前页面，避免在登录/注册页面清除存储
              try {
                var pages = getCurrentPages();
                var currentPage = pages && pages.length > 0 ? pages[pages.length - 1] : null;
                var currentRoute = currentPage ? currentPage.route : '';

                // 如果当前在登录或注册页面，不清除存储（可能是刚登录）
                if (currentRoute && (currentRoute.includes('login') || currentRoute.includes('register'))) {
                  reject(new Error('未授权，请重新登录'));
                  return;
                }

                // 检查是否是刚登录后的请求（5分钟内）
                var loginTime = uni.getStorageSync('loginTime');
                var _token = uni.getStorageSync('token');
                var userId = uni.getStorageSync('userId');
                var now = Date.now();
                var isRecentLogin = loginTime && now - loginTime < 5 * 60 * 1000; // 5分钟内

                if (_token && userId && isRecentLogin) {
                  // 不清除存储，只返回错误，让调用方处理
                  reject(new Error('认证失败，请稍后重试'));
                  return;
                }

                // 检查是否是用户主页且token存在，可能是token还未完全生效
                if (currentRoute && currentRoute.includes('main/index')) {
                  if (_token && userId) {
                    // 不清除存储，只返回错误，让调用方处理
                    reject(new Error('认证失败，请稍后重试'));
                    return;
                  }
                }
              } catch (e) {
                // 静默处理错误
              }

              // 401未授权错误，清除token并跳转到登录页
              uni.removeStorageSync('token');
              uni.removeStorageSync('userId');
              uni.removeStorageSync('userInfo');
              uni.removeStorageSync('username');
              uni.removeStorageSync('loginTime'); // 清除登录时间戳
              uni.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none',
                duration: 2000
              });
              setTimeout(function () {
                uni.reLaunch({
                  url: '/pages-auth/login'
                });
              }, 1500);
              reject(new Error('未授权，请重新登录'));
            } else {
              // 502等服务器错误，记录详细错误信息
              console.error("[API\u8BF7\u6C42\u5931\u8D25] ".concat(method, " ").concat(fullUrl, " - \u72B6\u6001\u7801: ").concat(res.statusCode), res);
              var _errorMsg = res.statusCode === 502 ? '服务器网关错误，请检查后端服务是否正常运行' : res.statusCode === 503 ? '服务暂时不可用' : res.statusCode === 504 ? '请求超时' : '网络错误';
              reject(new Error(_errorMsg));
            }
          },
          fail: function fail(err) {
            // 网络请求失败，记录详细错误信息
            console.error("[API\u8BF7\u6C42\u5931\u8D25] ".concat(method, " ").concat(fullUrl), err);
            var errMsg = err && err.errMsg ? String(err.errMsg) : '';
            var isTimeout = errMsg.toLowerCase().includes('timeout');
            if (isTimeout && retryOnTimeout && retryCount < 1) {
              // 超时自动重试一次（隐藏重复 loading，避免闪烁）
              _this.request(_objectSpread(_objectSpread({}, options), {}, {
                showLoading: false,
                retryOnTimeout: true,
                retryCount: retryCount + 1,
                timeout: Math.max(timeoutMs, 90000)
              })).then(resolve).catch(reject);
              return;
            }
            var errorMsg = err.errMsg || '网络请求失败';
            reject(new Error(errorMsg));
          },
          complete: function complete() {
            // 隐藏加载提示，使用 util 的 hideLoading 支持计数器
            if (loadingShown) {
              if (typeof _util.util !== 'undefined' && _util.util.hideLoading) {
                _util.util.hideLoading();
              } else {
                uni.hideLoading();
              }
            }
          }
        });
      });
    }

    // 获取配置信息
  }, {
    key: "getConfig",
    value: function getConfig(keys) {
      return this.request({
        url: '/api/config/values',
        method: 'GET',
        data: {
          keys: keys
        }
      });
    }

    // 获取轮播图列表（仅启用的）
  }, {
    key: "getBannerList",
    value: function getBannerList() {
      var showLoading = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : true;
      return this.request({
        url: '/api/banner/active',
        method: 'GET',
        showLoading: showLoading,
        timeout: 90000,
        retryOnTimeout: true
      });
    }

    // 获取轮播图/专题Banner详情
  }, {
    key: "getBannerDetail",
    value: function getBannerDetail(id) {
      return this.request({
        url: "/api/banner/".concat(id),
        method: 'GET'
      });
    }

    // 获取每日专题数据（含 banners、topics 等）
  }, {
    key: "getDailyTopics",
    value: function getDailyTopics() {
      return this.request({
        url: '/api/daily-topics',
        method: 'GET'
      });
    }

    // 获取专题主题分类列表（与后台「主题分类管理」同步，用于宠物专题 Tab）
  }, {
    key: "getDailyTopicThemes",
    value: function getDailyTopicThemes() {
      return this.request({
        url: '/api/daily-topics/themes',
        method: 'GET'
      });
    }

    // 获取专题详情（管理员后台创建的官方专题，可选 userId 用于点赞/收藏状态）
    // showLoading 默认 false：详情页自带 loading 态，避免与 uni.showLoading 叠加重影/难关闭
  }, {
    key: "getDailyTopicDetail",
    value: function getDailyTopicDetail(id) {
      var userId = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      var url = "/api/daily-topics/".concat(id);
      if (userId) url += "?userId=".concat(userId);
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 点赞专题
  }, {
    key: "likeTopic",
    value: function likeTopic(topicId, userId) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/like?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消点赞专题
  }, {
    key: "unlikeTopic",
    value: function unlikeTopic(topicId, userId) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/like?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 收藏专题（收藏到「我的收藏-文章」）
  }, {
    key: "collectTopic",
    value: function collectTopic(topicId, userId) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/collect?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消收藏专题
  }, {
    key: "uncollectTopic",
    value: function uncollectTopic(topicId, userId) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/collect?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 检查专题收藏状态
  }, {
    key: "checkTopicCollectStatus",
    value: function checkTopicCollectStatus(topicId, userId) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/collect/status?userId=").concat(userId),
        method: 'GET'
      });
    }

    // 获取用户收藏的专题列表（用于「我的收藏-文章」）
  }, {
    key: "getCollectedTopics",
    value: function getCollectedTopics() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var _params$showLoading = params.showLoading,
        showLoading = _params$showLoading === void 0 ? true : _params$showLoading,
        rest = (0, _objectWithoutProperties2.default)(params, _excluded);
      return this.request({
        url: '/api/daily-topics/collected',
        method: 'GET',
        data: {
          userId: rest.userId,
          page: rest.page || 1,
          size: rest.size || 20
        },
        showLoading: showLoading
      });
    }

    // 获取专题评论列表（详情页建议 showLoading=false，由页面自行控制加载态）
  }, {
    key: "getTopicComments",
    value: function getTopicComments(topicId) {
      var userId = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      var url = "/api/daily-topics/".concat(topicId, "/comments");
      if (userId) url += "?userId=".concat(userId);
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 添加专题评论
  }, {
    key: "addTopicComment",
    value: function addTopicComment(topicId, commentData) {
      return this.request({
        url: "/api/daily-topics/".concat(topicId, "/comments"),
        method: 'POST',
        data: commentData
      });
    }

    // 获取铲屎服务列表
  }, {
    key: "getLitterServicePage",
    value: function getLitterServicePage(params) {
      return this.request({
        url: '/api/litter-services/page',
        method: 'GET',
        data: params
      });
    }

    // 根据ID获取铲屎服务详情
  }, {
    key: "getLitterServiceById",
    value: function getLitterServiceById(id) {
      return this.request({
        url: "/api/litter-services/".concat(id),
        method: 'GET'
      });
    }

    // 获取宠物医院服务列表
  }, {
    key: "getHospitalServicePage",
    value: function getHospitalServicePage(params) {
      return this.request({
        url: '/api/hospital-services/page',
        method: 'GET',
        data: params
      });
    }

    // 根据ID获取宠物医院服务详情
  }, {
    key: "getHospitalServiceById",
    value: function getHospitalServiceById(id) {
      return this.request({
        url: "/api/hospital-services/".concat(id),
        method: 'GET'
      });
    }

    // 获取宠物医院服务列表
  }, {
    key: "getHospitalServiceList",
    value: function getHospitalServiceList() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/hospital-services/list',
        method: 'GET',
        data: params
      });
    }

    // 获取宠物医院展示图
  }, {
    key: "getHospitalBanner",
    value: function getHospitalBanner() {
      var position = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'hospital-page-top';
      return this.request({
        url: "/api/hospital-banners/position/".concat(position),
        method: 'GET'
      });
    }

    // 获取医疗服务列表
  }, {
    key: "getMedicalServicePage",
    value: function getMedicalServicePage(params) {
      return this.request({
        url: '/api/medical-services/page',
        method: 'GET',
        data: params
      });
    }

    // 获取洗护服务列表
  }, {
    key: "getGroomingServicePage",
    value: function getGroomingServicePage(params) {
      return this.request({
        url: '/api/grooming-services/page',
        method: 'GET',
        data: params
      });
    }

    // 获取启用的洗护服务列表
  }, {
    key: "getGroomingServiceList",
    value: function getGroomingServiceList() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/grooming-services/list',
        method: 'GET',
        data: params
      });
    }

    // 根据ID获取洗护服务详情
  }, {
    key: "getGroomingServiceById",
    value: function getGroomingServiceById(id) {
      return this.request({
        url: "/api/grooming-services/".concat(id),
        method: 'GET'
      });
    }

    // 获取领养服务列表
  }, {
    key: "getAdoptionServicePage",
    value: function getAdoptionServicePage(params) {
      return this.request({
        url: '/api/adoption-services/page',
        method: 'GET',
        data: params
      });
    }

    // 获取可用时间段
  }, {
    key: "getAvailableTimeSlots",
    value:
    // ==================== 服务预约时间与智能分配相关接口 ====================

    // 获取可预约时间段（storeId 可选，传入时仅返回该门店的排班）
    function getAvailableTimeSlots(serviceType, date, storeId) {
      var showLoading = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : true;
      var params = {
        serviceType: serviceType,
        date: date
      };
      if (storeId != null && storeId !== '' && !isNaN(Number(storeId))) {
        params.storeId = Number(storeId);
      }
      return this.request({
        url: '/api/schedule/available',
        method: 'GET',
        data: params,
        showLoading: showLoading
      });
    }

    // 获取服务人员列表
  }, {
    key: "createBooking",
    value:
    // 创建预约
    function createBooking(data) {
      return this.request({
        url: '/api/bookings/create',
        method: 'POST',
        data: data
      });
    }

    // 获取我的预约列表
  }, {
    key: "getMyBookings",
    value: function getMyBookings(params) {
      return this.request({
        url: '/api/bookings/my-list',
        method: 'GET',
        data: params
      });
    }

    // 取消预约
  }, {
    key: "cancelBooking",
    value: function cancelBooking(id) {
      return this.request({
        url: "/api/bookings/".concat(id, "/cancel"),
        method: 'PUT'
      });
    }

    // 微信小程序授权
  }, {
    key: "wxappAuthorize",
    value: function wxappAuthorize(params) {
      return this.request({
        url: '/api/user/wxapp/authorize',
        method: 'POST',
        data: params
      });
    }

    // 手机号密码登录
  }, {
    key: "loginByPhone",
    value: function loginByPhone(phone, password) {
      return this.request({
        url: '/api/user/login',
        method: 'POST',
        data: {
          phone: phone,
          password: password
        }
      });
    }

    // 微信登录
  }, {
    key: "loginByWechat",
    value: function loginByWechat(code) {
      return this.request({
        url: '/api/user/wxapp/login',
        method: 'POST',
        data: {
          code: code
        }
      });
    }

    // 用户注册（smsCode 在有短信服务时必填；勿传字符串 "undefined"）
  }, {
    key: "register",
    value: function register(phone, password, nickname, smsCode) {
      var data = {
        phone: String(phone),
        password: String(password),
        nickname: String(nickname || '')
      };
      if (smsCode != null && smsCode !== '' && String(smsCode) !== 'undefined') {
        data.smsCode = String(smsCode).trim();
      }
      return this.request({
        url: '/api/user/register',
        method: 'POST',
        data: data
      });
    }

    // 忘记密码：通过手机号+验证码重置密码
  }, {
    key: "resetPassword",
    value: function resetPassword(phone, smsCode, newPassword) {
      return this.request({
        url: '/api/user/forgot-password',
        method: 'POST',
        data: {
          phone: String(phone),
          smsCode: String(smsCode),
          newPassword: String(newPassword)
        }
      });
    }

    // 修改密码（登录后，需携带 token）
  }, {
    key: "changePassword",
    value: function changePassword(oldPassword, newPassword) {
      return this.request({
        url: '/api/user/change-password',
        method: 'POST',
        data: {
          oldPassword: String(oldPassword),
          newPassword: String(newPassword)
        }
      });
    }

    // 绑定手机号（使用其它号码时：需先发验证码，再传 phone + smsCode）
  }, {
    key: "bindPhone",
    value: function bindPhone(phone, smsCode) {
      return this.request({
        url: '/api/user/bind-phone',
        method: 'POST',
        data: {
          phone: String(phone),
          smsCode: String(smsCode)
        }
      });
    }

    // 解绑手机号（需登录）。解绑后该号码可被其他账号绑定，当前账号可再绑定其他号码
  }, {
    key: "unbindPhone",
    value: function unbindPhone() {
      return this.request({
        url: '/api/user/unbind-phone',
        method: 'POST'
      });
    }

    // 提交投诉举报（可不登录）
  }, {
    key: "submitComplaint",
    value: function submitComplaint(type, content, contactInfo, images) {
      return this.request({
        url: '/api/complaint/submit',
        method: 'POST',
        data: {
          type: type || '其他',
          content: String(content),
          contactInfo: contactInfo ? String(contactInfo) : '',
          images: Array.isArray(images) && images.length ? images : null
        },
        showLoading: true
      });
    }

    // 注销账号
  }, {
    key: "deleteAccount",
    value: function deleteAccount() {
      return this.request({
        url: '/api/user/delete',
        method: 'POST'
      });
    }

    // 获取用户详情
  }, {
    key: "getUserDetail",
    value: function getUserDetail(token) {
      return this.request({
        url: '/api/user/detail',
        method: 'GET',
        data: {
          token: token
        }
      });
    }

    // 获取用户资产
  }, {
    key: "getUserAmount",
    value: function getUserAmount(token) {
      return this.request({
        url: '/api/user/amount',
        method: 'GET',
        data: {
          token: token
        }
      });
    }

    // 获取最新公告
  }, {
    key: "getLastNotice",
    value: function getLastNotice(type) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: '/api/notice/last-one',
        method: 'GET',
        data: {
          type: type
        },
        showLoading: showLoading
      });
    }
    // 获取公告列表（系统公告页用，status=1 已发布）
  }, {
    key: "getNoticeList",
    value: function getNoticeList(type) {
      var status = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;
      var limit = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 50;
      var showLoading = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : false;
      return this.request({
        url: '/api/notice/list',
        method: 'GET',
        data: {
          type: type,
          status: status,
          limit: limit
        },
        showLoading: showLoading
      });
    }

    // ========== 通知中心（预约提醒、订单状态、系统公告、社区互动） ==========
  }, {
    key: "getMyNotifications",
    value: function getMyNotifications() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 20;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      return this.request({
        url: '/api/notifications/my',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize
        },
        showLoading: showLoading
      });
    }
  }, {
    key: "getNotificationUnreadCount",
    value: function getNotificationUnreadCount() {
      var showLoading = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
      return this.request({
        url: '/api/notifications/unread-count',
        method: 'GET',
        showLoading: showLoading
      });
    }
  }, {
    key: "markNotificationRead",
    value: function markNotificationRead(id) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
      return this.request({
        url: "/api/notifications/".concat(id, "/read"),
        method: 'POST',
        showLoading: showLoading
      });
    }
  }, {
    key: "markAllNotificationsRead",
    value: function markAllNotificationsRead() {
      var showLoading = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
      return this.request({
        url: '/api/notifications/read-all',
        method: 'POST',
        showLoading: showLoading
      });
    }
  }, {
    key: "getUnreadNotices",
    value: function getUnreadNotices() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 20;
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
      return this.request({
        url: '/api/notifications/notices-unread',
        method: 'GET',
        data: {
          limit: limit
        },
        showLoading: showLoading
      });
    }
  }, {
    key: "markNoticeAsRead",
    value: function markNoticeAsRead(noticeId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
      return this.request({
        url: "/api/notifications/notice/".concat(noticeId, "/read"),
        method: 'POST',
        showLoading: showLoading
      });
    }

    // 获取所有商品分类
  }, {
    key: "getAllCategories",
    value: function getAllCategories() {
      return this.request({
        url: '/api/categories/all',
        method: 'GET'
      });
    }

    // 获取商品列表V2
  }, {
    key: "getGoodsList",
    value: function getGoodsList(params) {
      return this.request({
        url: '/api/product/list',
        method: 'GET',
        data: params
      });
    }

    // 获取商品详情
  }, {
    key: "getGoodsDetail",
    value: function getGoodsDetail(id) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: "/api/product/".concat(id),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 获取商品评价
  }, {
    key: "getProductReviews",
    value: function getProductReviews(productId) {
      return this.request({
        url: "/api/product/".concat(productId, "/reviews"),
        method: 'GET'
      });
    }

    // 提交商品评价
  }, {
    key: "submitReview",
    value: function submitReview(reviewData) {
      return this.request({
        url: '/api/reviews',
        method: 'POST',
        data: reviewData
      });
    }

    // 获取我的卡券
  }, {
    key: "getMyCards",
    value: function getMyCards(token) {
      return this.request({
        url: '/api/card/my',
        method: 'GET',
        data: {
          token: token
        }
      });
    }

    // 获取购物车信息
  }, {
    key: "getShoppingCartInfo",
    value: function getShoppingCartInfo(token, type) {
      return this.request({
        url: '/api/shopping-cart/info',
        method: 'GET',
        data: {
          token: token,
          type: type
        }
      });
    }

    // ==================== 用户相关接口 ====================

    // 发送短信验证码
  }, {
    key: "sendSmsCode",
    value: function sendSmsCode(phone) {
      return this.request({
        url: '/api/sms/send',
        method: 'POST',
        data: {
          phone: phone
        }
      });
    }

    // 用户登录
  }, {
    key: "login",
    value: function login(loginData) {
      return this.request({
        url: '/api/user/login',
        method: 'POST',
        data: loginData
      });
    }

    // 获取当前用户信息（params.showLoading 为 false 时不显示全局加载中，用于「我的」页静默刷新）
  }, {
    key: "getCurrentUser",
    value: function getCurrentUser() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var _params$showLoading2 = params.showLoading,
        showLoading = _params$showLoading2 === void 0 ? true : _params$showLoading2,
        rest = (0, _objectWithoutProperties2.default)(params, _excluded2);
      var url = '/api/user/current';
      var queryParams = [];
      if (rest.userId != null && rest.userId !== undefined && rest.userId !== '') {
        var userId = typeof rest.userId === 'string' ? parseInt(rest.userId) : rest.userId;
        if (!isNaN(userId) && userId > 0) {
          queryParams.push("userId=".concat(userId));
        }
      }
      if (rest.username) {
        queryParams.push("username=".concat(encodeURIComponent(rest.username)));
      }
      if (queryParams.length > 0) {
        url += '?' + queryParams.join('&');
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 更新用户信息
  }, {
    key: "updateUser",
    value: function updateUser(userData) {
      return this.request({
        url: '/api/user/update',
        method: 'POST',
        data: userData
      });
    }

    // 更新用户资料（个人资料页面使用）
  }, {
    key: "updateUserProfile",
    value: function updateUserProfile(userData) {
      return this.request({
        url: '/api/user/update',
        method: 'POST',
        data: userData
      });
    }

    // 修改账号（格式：字母或下划线开头，6-20位，仅字母数字下划线减号）
  }, {
    key: "changeUsername",
    value: function changeUsername(newUsername) {
      return this.request({
        url: '/api/user/change-username',
        method: 'POST',
        data: {
          newUsername: newUsername
        }
      });
    }

    // 检查当前账号是否已设置密码（微信登录可能无密码）
  }, {
    key: "hasPassword",
    value: function hasPassword() {
      return this.request({
        url: '/api/user/has-password',
        method: 'GET'
      });
    }

    // 验证当前账号登录密码（修改账号前）
  }, {
    key: "verifyPassword",
    value: function verifyPassword(password) {
      return this.request({
        url: '/api/user/verify-password',
        method: 'POST',
        data: {
          password: password
        }
      });
    }

    // 获取微信手机号（通过code换取）
  }, {
    key: "getPhoneNumber",
    value: function getPhoneNumber(data) {
      return this.request({
        url: '/api/user/getPhoneNumber',
        method: 'POST',
        data: data
      });
    }

    // 获取用户统计信息（关注、粉丝、动态、获赞）
  }, {
    key: "getUserStats",
    value: function getUserStats() {
      var userId = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      var url = '/api/user/stats';
      if (userId) {
        url += "?userId=".concat(userId);
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 获取用户宠物列表
  }, {
    key: "getPetList",
    value: function getPetList() {
      var userId = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      var params = {
        pageNo: 1,
        pageSize: 100
      };
      if (userId) {
        params.userId = userId;
      }
      return this.request({
        url: '/api/pets/page',
        method: 'GET',
        data: params,
        showLoading: showLoading
      });
    }

    // ==================== 商品相关接口 ====================

    // 分页查询商品（可选 storeId 筛选店铺商品；options.showLoading 控制 loading）
  }, {
    key: "getProductPage",
    value: function getProductPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
      var _ref = options && (0, _typeof2.default)(options) === 'object' ? options : {},
        storeId = _ref.storeId,
        _ref$showLoading = _ref.showLoading,
        showLoading = _ref$showLoading === void 0 ? true : _ref$showLoading,
        extra = (0, _objectWithoutProperties2.default)(_ref, _excluded3);
      var data = _objectSpread({
        pageNo: pageNo,
        pageSize: pageSize
      }, extra);
      if (storeId != null && storeId !== '') {
        data.storeId = storeId;
      }
      return this.request({
        url: '/api/product/page',
        method: 'GET',
        data: data,
        showLoading: showLoading
      });
    }

    /** C 端：店铺基本信息（公开接口） */
  }, {
    key: "getStoreInfo",
    value: function getStoreInfo(storeId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: '/api/store/info',
        method: 'GET',
        data: {
          storeId: storeId
        },
        showLoading: showLoading
      });
    }

    // 获取热门商品
  }, {
    key: "getHotProducts",
    value: function getHotProducts() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 10;
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: '/api/product/hot',
        method: 'GET',
        data: {
          limit: limit
        },
        showLoading: showLoading,
        timeout: 90000,
        retryOnTimeout: true
      });
    }

    // 获取推荐商品
  }, {
    key: "getRecommendProducts",
    value: function getRecommendProducts() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 4;
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: '/api/product/recommend',
        method: 'GET',
        data: {
          limit: limit
        },
        showLoading: showLoading
      });
    }

    // 获取商品详情
  }, {
    key: "getProductDetail",
    value: function getProductDetail(id) {
      return this.request({
        url: "/api/product/".concat(id),
        method: 'GET'
      });
    }

    // ==================== 购物车相关接口 ====================

    // 分页查询购物车
  }, {
    key: "getCartPage",
    value: function getCartPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      var userId = arguments.length > 2 ? arguments[2] : undefined;
      return this.request({
        url: '/api/cart/page',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize,
          userId: userId
        }
      });
    }

    // 添加到购物车
  }, {
    key: "addToCart",
    value: function addToCart(cartData) {
      return this.request({
        url: '/api/cart/add',
        method: 'POST',
        data: cartData
      });
    }

    // 更新购物车商品
  }, {
    key: "updateCartItem",
    value: function updateCartItem(cartData) {
      return this.request({
        url: '/api/cart/update',
        method: 'PUT',
        data: cartData
      });
    }

    // 从购物车删除商品
  }, {
    key: "removeFromCart",
    value: function removeFromCart(id) {
      return this.request({
        url: "/api/cart/".concat(id),
        method: 'DELETE'
      });
    }

    // 获取购物车商品数量
  }, {
    key: "getCartCount",
    value: function getCartCount(userId) {
      return this.request({
        url: "/api/cart/count?userId=".concat(userId),
        method: 'GET'
      });
    }

    // ==================== 宠物相关接口 ====================

    // 分页查询宠物
  }, {
    key: "getPetPage",
    value: function getPetPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      var userId = arguments.length > 2 ? arguments[2] : undefined;
      var showLoading = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : true;
      return this.request({
        url: '/api/pets/page',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize,
          userId: userId
        },
        showLoading: showLoading
      });
    }

    // 创建宠物
  }, {
    key: "createPet",
    value: function createPet(petData) {
      return this.request({
        url: '/api/pets/create',
        method: 'POST',
        data: petData
      });
    }

    // 更新宠物
  }, {
    key: "updatePet",
    value: function updatePet(petData) {
      return this.request({
        url: '/api/pets/update',
        method: 'PUT',
        data: petData
      });
    }

    // 删除宠物
  }, {
    key: "deletePet",
    value: function deletePet(id) {
      return this.request({
        url: "/api/pets/".concat(id),
        method: 'DELETE'
      });
    }

    // 获取宠物详情
  }, {
    key: "getPetDetail",
    value: function getPetDetail(id) {
      return this.request({
        url: "/api/pets/".concat(id),
        method: 'GET'
      });
    }

    // 备注：疫苗记录/健康状况/就医记录（宠物医疗模块）已移除

    // ==================== 预约相关接口 ====================

    // 分页查询预约
  }, {
    key: "getAppointmentPage",
    value: function getAppointmentPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      return this.request({
        url: '/api/door-cleaning/page',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize
        }
      });
    }

    // 创建预约
  }, {
    key: "createAppointment",
    value: function createAppointment(appointmentData) {
      return this.request({
        url: '/api/door-cleaning/create',
        method: 'POST',
        data: appointmentData
      });
    }

    // 更新预约状态
  }, {
    key: "updateAppointmentStatus",
    value: function updateAppointmentStatus(id, status) {
      // 移除更新预约状态API日志以提升性能
      return this.request({
        url: "/api/door-cleaning/".concat(id, "/status?status=").concat(status),
        method: 'PUT'
      });
    }

    // 获取用户预约列表（上门铲屎）
  }, {
    key: "getUserAppointments",
    value: function getUserAppointments(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: "/api/door-cleaning/user/list/".concat(userId),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 获取用户全部预约（统一接口：上门铲屎+医院+洗护，一次请求，避免三个接口都失败导致整页加载失败）
  }, {
    key: "getUserAppointmentsUnified",
    value: function getUserAppointmentsUnified(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: "/api/user-appointments/list/".concat(userId),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 获取宠物详情
  }, {
    key: "getPetById",
    value: function getPetById(id) {
      return this.request({
        url: "/api/pets/".concat(id),
        method: 'GET'
      });
    }

    // 获取预约详情
  }, {
    key: "getAppointmentDetail",
    value: function getAppointmentDetail(id) {
      return this.request({
        url: "/api/door-cleaning/".concat(id),
        method: 'GET'
      });
    }

    // 提交服务评价（对已完成订单的服务人员评分）
  }, {
    key: "submitAppointmentRating",
    value: function submitAppointmentRating(data) {
      return this.request({
        url: '/api/appointment/rating',
        method: 'POST',
        data: data
      });
    }

    // 检查是否已评价
  }, {
    key: "checkAppointmentRated",
    value: function checkAppointmentRated(appointmentType, appointmentId) {
      return this.request({
        url: "/api/appointment/rating/check?appointmentType=".concat(encodeURIComponent(appointmentType), "&appointmentId=").concat(appointmentId),
        method: 'GET',
        showLoading: false
      });
    }
  }, {
    key: "getServiceMembers",
    value: function getServiceMembers(serviceType) {
      return this.request({
        url: '/api/service-member/list',
        method: 'GET',
        data: {
          serviceType: serviceType
        }
      });
    }

    // 自动分配服务人员
  }, {
    key: "autoAssignMember",
    value: function autoAssignMember(serviceType, date, timeSlot) {
      return this.request({
        url: '/api/service-member/auto-assign',
        method: 'POST',
        data: {
          serviceType: serviceType,
          date: date,
          timeSlot: timeSlot
        }
      });
    }

    // 获取服务人员工作量
  }, {
    key: "getMemberWorkload",
    value: function getMemberWorkload(memberId, date) {
      return this.request({
        url: "/api/service-member/workload/".concat(memberId),
        method: 'GET',
        data: {
          date: date
        }
      });
    }

    // 获取服务人员日程
  }, {
    key: "getMemberSchedule",
    value: function getMemberSchedule(memberId, date) {
      return this.request({
        url: "/api/schedule/member/".concat(memberId),
        method: 'GET',
        data: {
          date: date
        }
      });
    }

    // ==================== 宠物医院预约相关接口 ====================

    // 创建宠物医院预约
  }, {
    key: "createHospitalAppointment",
    value: function createHospitalAppointment(appointmentData) {
      return this.request({
        url: '/api/hospital-appointments/create',
        method: 'POST',
        data: appointmentData
      });
    }

    // 获取宠物医院预约列表
  }, {
    key: "getHospitalAppointmentPage",
    value: function getHospitalAppointmentPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      return this.request({
        url: '/api/hospital-appointments/page',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize
        }
      });
    }

    // 获取用户宠物医院预约列表
  }, {
    key: "getUserHospitalAppointments",
    value: function getUserHospitalAppointments(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: "/api/hospital-appointments/user/list/".concat(userId),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 更新宠物医院预约状态
  }, {
    key: "updateHospitalAppointmentStatus",
    value: function updateHospitalAppointmentStatus(id, status) {
      return this.request({
        url: "/api/hospital-appointments/".concat(id, "/status?status=").concat(status),
        method: 'PUT'
      });
    }

    // 获取宠物医院预约详情
  }, {
    key: "getHospitalAppointmentDetail",
    value: function getHospitalAppointmentDetail(id) {
      return this.request({
        url: "/api/hospital-appointments/".concat(id),
        method: 'GET'
      });
    }

    // ==================== 宠物洗护预约相关 API ====================

    // 创建宠物洗护预约
  }, {
    key: "createGroomingAppointment",
    value: function createGroomingAppointment(appointmentData) {
      return this.request({
        url: '/api/grooming-appointments/create',
        method: 'POST',
        data: appointmentData
      });
    }

    // 获取宠物洗护预约列表
  }, {
    key: "getGroomingAppointmentPage",
    value: function getGroomingAppointmentPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      return this.request({
        url: '/api/grooming-appointments/page',
        method: 'GET',
        data: {
          current: pageNo,
          size: pageSize
        }
      });
    }

    // 获取用户宠物洗护预约列表
  }, {
    key: "getUserGroomingAppointments",
    value: function getUserGroomingAppointments(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: "/api/grooming-appointments/user/list/".concat(userId),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 更新宠物洗护预约状态
  }, {
    key: "updateGroomingAppointmentStatus",
    value: function updateGroomingAppointmentStatus(id, status) {
      return this.request({
        url: "/api/grooming-appointments/".concat(id, "/status?status=").concat(status),
        method: 'PUT'
      });
    }

    // 获取宠物洗护预约详情
  }, {
    key: "getGroomingAppointmentDetail",
    value: function getGroomingAppointmentDetail(id) {
      return this.request({
        url: "/api/grooming-appointments/".concat(id),
        method: 'GET'
      });
    }

    // 获取洗护服务展示图
  }, {
    key: "getGroomingBanner",
    value: function getGroomingBanner() {
      var position = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'grooming-page-top';
      return this.request({
        url: "/api/grooming-banners/position/".concat(position),
        method: 'GET'
      });
    }

    // 预约变更申请：用户提交变更（提交后预约状态为「变更待确认」，工作人员同意后生效）
  }, {
    key: "submitAppointmentChangeRequest",
    value: function submitAppointmentChangeRequest(data) {
      return this.request({
        url: '/api/appointment-change-request/submit',
        method: 'POST',
        data: data
      });
    }

    // 按预约查询待处理变更（用于展示「变更待确认」）
  }, {
    key: "getPendingChangeByAppointment",
    value:
    // 按预约查询待处理变更请求
    function getPendingChangeByAppointment(appointmentType, appointmentId) {
      var q = "appointmentType=".concat(encodeURIComponent(appointmentType || ''), "&appointmentId=").concat(appointmentId || '');
      return this.request({
        url: "/api/appointment-change-request/pending-by-appointment?".concat(q),
        method: 'GET',
        showLoading: false
      });
    }

    // 按预约查询待处理取消请求
  }, {
    key: "hasAnyAppointmentChangeRequest",
    value:
    // 查询某预约是否已有任意变更记录（用于隐藏「申请变更」按钮）
    function hasAnyAppointmentChangeRequest(appointmentType, appointmentId) {
      var q = "appointmentType=".concat(encodeURIComponent(appointmentType || ''), "&appointmentId=").concat(appointmentId || '');
      return this.request({
        url: "/api/appointment-change-request/has-any?".concat(q),
        method: 'GET',
        showLoading: false
      });
    }

    // 用户取消变更申请（仅工作人员未确认时可取消）
  }, {
    key: "cancelAppointmentChangeRequest",
    value: function cancelAppointmentChangeRequest(data) {
      return this.request({
        url: '/api/appointment-change-request/cancel',
        method: 'POST',
        data: data
      });
    }

    // 取消预约申请：已确认订单填写原因提交，工作人员同意后取消（含 0-2 小时违约金）
  }, {
    key: "submitCancellationRequest",
    value: function submitCancellationRequest(data) {
      return this.request({
        url: '/api/appointment-cancellation-request/submit',
        method: 'POST',
        data: data
      });
    }
  }, {
    key: "getPendingCancellationByAppointment",
    value: function getPendingCancellationByAppointment(appointmentType, appointmentId) {
      var q = "appointmentType=".concat(encodeURIComponent(appointmentType || ''), "&appointmentId=").concat(appointmentId || '');
      return this.request({
        url: "/api/appointment-cancellation-request/pending-by-appointment?".concat(q),
        method: 'GET',
        showLoading: false
      });
    }

    // 工作人员：获取我的工单列表（与管理员后台规则一致，仅自己的工单）
  }, {
    key: "createAdoptionAppointment",
    value:
    // ==================== 宠物领养预约相关 API ====================

    // 创建宠物领养预约
    function createAdoptionAppointment(appointmentData) {
      return this.request({
        url: '/api/adoption-appointments/create',
        method: 'POST',
        data: appointmentData
      });
    }

    // 获取宠物领养预约列表
  }, {
    key: "getAdoptionAppointmentPage",
    value: function getAdoptionAppointmentPage() {
      var pageNo = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var pageSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      return this.request({
        url: '/api/adoption-appointments/page',
        method: 'GET',
        data: {
          pageNo: pageNo,
          pageSize: pageSize
        }
      });
    }

    // 获取用户宠物领养预约列表
  }, {
    key: "getUserAdoptionAppointments",
    value: function getUserAdoptionAppointments(userId) {
      return this.request({
        url: "/api/adoption-appointments/user/list/".concat(userId),
        method: 'GET'
      });
    }

    // 更新宠物领养预约状态
  }, {
    key: "updateAdoptionAppointmentStatus",
    value: function updateAdoptionAppointmentStatus(id, status) {
      return this.request({
        url: "/api/adoption-appointments/".concat(id, "/status?status=").concat(status),
        method: 'PUT'
      });
    }

    // 获取宠物领养预约详情
  }, {
    key: "getAdoptionAppointmentDetail",
    value: function getAdoptionAppointmentDetail(id) {
      return this.request({
        url: "/api/adoption-appointments/".concat(id),
        method: 'GET'
      });
    }

    // ==================== 待领养宠物相关 API ====================

    // 获取待领养宠物列表
  }, {
    key: "getAdoptionPets",
    value: function getAdoptionPets() {
      return this.request({
        url: '/api/pet-adoption/list',
        method: 'GET'
      });
    }

    // 获取待领养宠物详情
  }, {
    key: "getAdoptionPetDetail",
    value: function getAdoptionPetDetail(id) {
      return this.request({
        url: "/api/pet-adoption/".concat(id),
        method: 'GET'
      });
    }

    // 获取领养服务背景图
  }, {
    key: "getAdoptionBackground",
    value: function getAdoptionBackground() {
      return this.request({
        url: '/api/adoption-background/position/adoption-page-background',
        method: 'GET'
      });
    }

    // ==================== 订单相关接口 ====================

    // 获取订单列表（已合并到下面的方法，保留此方法以兼容旧代码）
    // getOrderList(status) {
    //   return this.request({
    //     url: '/api/orders/list',
    //     method: 'GET',
    //     data: { status }
    //   })
    // }

    // 获取订单详情
  }, {
    key: "getOrderDetail",
    value:
    // 获取订单详情
    function getOrderDetail(orderId) {
      return this.request({
        url: "/api/orders/".concat(orderId),
        method: 'GET',
        timeout: 90000,
        retryOnTimeout: true
      });
    }

    // 创建订单
  }, {
    key: "createOrder",
    value: function createOrder(orderData) {
      return this.request({
        url: '/api/orders/create',
        method: 'POST',
        data: orderData
      });
    }

    // 更新订单状态
  }, {
    key: "updateOrderStatus",
    value: function updateOrderStatus(orderId, status) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/status?status=").concat(status),
        method: 'PUT'
      });
    }

    // 删除订单（软删除：服务端设置为 deleted，跨设备生效）
  }, {
    key: "deleteOrder",
    value: function deleteOrder(orderId) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/status?status=deleted"),
        method: 'PUT'
      });
    }

    // 获取订单物流信息
  }, {
    key: "getOrderLogistics",
    value: function getOrderLogistics(orderId) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/logistics"),
        method: 'GET',
        timeout: 90000,
        retryOnTimeout: true
      });
    }

    // 更新订单物流信息（管理员用）
  }, {
    key: "updateOrderLogistics",
    value: function updateOrderLogistics(orderId, logisticsData) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/logistics"),
        method: 'PUT',
        data: logisticsData
      });
    }

    // 申请退款
  }, {
    key: "getGroomingAppointments",
    value:
    // ==================== 美容服务预约订单 ====================

    // 获取美容服务预约订单列表
    function getGroomingAppointments(status) {
      return this.request({
        url: '/api/orders/grooming-appointments',
        method: 'GET',
        data: {
          status: status
        }
      });
    }

    // ==================== 服务配置相关接口 ====================

    // 获取所有服务配置
  }, {
    key: "getAllServiceConfigs",
    value: function getAllServiceConfigs() {
      return this.request({
        url: '/api/service-config/all',
        method: 'GET'
      });
    }

    // 根据服务类型获取配置
  }, {
    key: "getServiceConfigByType",
    value: function getServiceConfigByType(serviceType) {
      return this.request({
        url: "/api/service-config/type/".concat(serviceType),
        method: 'GET'
      });
    }

    // ==================== 工作人员相关接口 ====================

    // 工作人员登录（使用 Admin 表，username + password）
  }, {
    key: "staffLogin",
    value: function staffLogin(username, password) {
      return this.request({
        url: '/api/admin/login',
        method: 'POST',
        data: {
          username: username,
          password: password
        },
        showLoading: true
      });
    }

    // 核销验证（使用工作人员 token，用于小程序端工作人员核销）
  }, {
    key: "verifyCodeForStaff",
    value: function verifyCodeForStaff(verifyCode) {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: '/api/verify/verify-code',
        method: 'POST',
        data: {
          verifyCode: verifyCode
        },
        authToken: staffToken
      });
    }

    // 工作人员：同意预约变更请求
  }, {
    key: "approveChangeRequest",
    value: function approveChangeRequest(changeRequestId) {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: "/api/appointment-change-request/".concat(changeRequestId, "/approve"),
        method: 'POST',
        authToken: staffToken
      });
    }

    // 工作人员：拒绝预约变更请求
  }, {
    key: "rejectChangeRequest",
    value: function rejectChangeRequest(changeRequestId) {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: "/api/appointment-change-request/".concat(changeRequestId, "/reject"),
        method: 'POST',
        authToken: staffToken
      });
    }

    // 工作人员：同意取消预约请求
  }, {
    key: "approveCancellationRequest",
    value: function approveCancellationRequest(cancellationRequestId) {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: "/api/appointment-cancellation-request/".concat(cancellationRequestId, "/approve"),
        method: 'POST',
        authToken: staffToken
      });
    }

    // 工作人员：拒绝取消预约请求
  }, {
    key: "rejectCancellationRequest",
    value: function rejectCancellationRequest(cancellationRequestId, rejectReason) {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: "/api/appointment-cancellation-request/".concat(cancellationRequestId, "/reject"),
        method: 'POST',
        data: {
          rejectReason: rejectReason
        },
        authToken: staffToken
      });
    }
  }, {
    key: "getStaffWorkOrders",
    value: function getStaffWorkOrders() {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: '/api/admin/self/work-orders',
        method: 'GET',
        authToken: staffToken,
        showLoading: false
      });
    }

    // 工作人员：获取我的真实评价列表（来自后端评价表）
  }, {
    key: "getStaffMyReviews",
    value: function getStaffMyReviews() {
      var staffToken = uni.getStorageSync('staffToken');
      return this.request({
        url: '/api/admin/self/reviews',
        method: 'GET',
        authToken: staffToken,
        showLoading: false
      });
    }

    // 工作人员：更新预约状态（确认/完成/拒绝；拒绝时可传 rejectReason，用户端会展示）
  }, {
    key: "updateAppointmentStatusForStaff",
    value: function updateAppointmentStatusForStaff(serviceType, id, status, rejectReason) {
      var staffToken = uni.getStorageSync('staffToken');
      var data = {
        id: id,
        serviceType: serviceType,
        status: status
      };
      if (rejectReason != null && String(rejectReason).trim() !== '') {
        data.rejectReason = String(rejectReason).trim();
      }
      return this.request({
        url: '/api/admin/self/work-orders/status',
        method: 'POST',
        data: data,
        authToken: staffToken
      });
    }

    // ==================== 核销相关接口 ====================

    // 核销验证（通用，使用当前 token）
  }, {
    key: "verifyCode",
    value: function verifyCode(_verifyCode) {
      return this.request({
        url: '/api/verify/verify-code',
        method: 'POST',
        data: {
          verifyCode: _verifyCode
        }
      });
    }

    // 检查核销码状态
  }, {
    key: "checkVerifyCode",
    value: function checkVerifyCode(verifyCode) {
      return this.request({
        url: "/api/verify/check-code/".concat(verifyCode),
        method: 'GET'
      });
    }

    // ==================== 服务门店相关接口 ====================

    // 获取所有营业中的门店
  }, {
    key: "getAllStores",
    value: function getAllStores() {
      return this.request({
        url: '/api/stores/all',
        method: 'GET'
      });
    }

    // 根据服务类型获取门店列表
  }, {
    key: "getStoresByService",
    value: function getStoresByService(serviceType) {
      return this.request({
        url: "/api/stores/by-service/".concat(serviceType),
        method: 'GET'
      });
    }

    // 获取默认门店
  }, {
    key: "getDefaultStore",
    value: function getDefaultStore() {
      return this.request({
        url: '/api/stores/default',
        method: 'GET'
      });
    }

    // ==================== 地区选择相关接口 ====================

    // 获取所有省份
  }, {
    key: "getProvinces",
    value: function getProvinces() {
      return this.request({
        url: '/api/regions/provinces',
        method: 'GET'
      });
    }

    // 根据省份代码获取城市
  }, {
    key: "getCities",
    value: function getCities(parentCode) {
      return this.request({
        url: '/api/regions/cities',
        method: 'GET',
        data: {
          parentCode: parentCode
        }
      });
    }

    // 根据城市代码获取区县
  }, {
    key: "getDistricts",
    value: function getDistricts(parentCode) {
      return this.request({
        url: '/api/regions/districts',
        method: 'GET',
        data: {
          parentCode: parentCode
        }
      });
    }

    // 根据父级代码获取子级地区
  }, {
    key: "getChildren",
    value: function getChildren(parentCode) {
      return this.request({
        url: '/api/regions/children',
        method: 'GET',
        data: {
          parentCode: parentCode
        }
      });
    }

    // 根据代码获取地区信息
  }, {
    key: "getRegionByCode",
    value: function getRegionByCode(code) {
      return this.request({
        url: "/api/regions/".concat(code),
        method: 'GET'
      });
    }

    // 获取订单列表（extra.timeout 可加长超时，避免弱网/慢接口被误判为「无订单」）
  }, {
    key: "getOrderList",
    value: function getOrderList() {
      var status = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
      var userId = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      var extra = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};
      var url = '/api/orders/list';
      var params = [];

      // 如果传入的是字符串且是状态值，则作为status处理
      if (status && typeof status === 'string' && ['all', 'pending', 'paid', 'shipped', 'completed', 'cancelled'].includes(status)) {
        if (status !== 'all') {
          params.push("status=".concat(status));
        }
      } else if (status && typeof status === 'number') {
        // 如果传入的是数字，可能是userId（兼容旧代码）
        params.push("userId=".concat(status));
      }

      // 如果明确传入了userId，使用userId
      if (userId) {
        // 移除可能存在的userId参数，使用新的userId
        var filteredParams = params.filter(function (p) {
          return !p.startsWith('userId=');
        });
        filteredParams.push("userId=".concat(userId));
        params.length = 0;
        params.push.apply(params, (0, _toConsumableArray2.default)(filteredParams));
      }
      if (params.length > 0) {
        url += '?' + params.join('&');
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading,
        timeout: extra.timeout != null ? extra.timeout : 60000,
        header: {
          'Cache-Control': 'no-store'
        }
      });
    }
  }, {
    key: "requestRefund",
    value: function requestRefund(orderId, refundData) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/refund"),
        method: 'POST',
        data: refundData
      });
    }

    // 取消退款申请（用户侧）
  }, {
    key: "cancelRefund",
    value: function cancelRefund(orderId) {
      return this.request({
        url: "/api/orders/".concat(orderId, "/refund/cancel"),
        method: 'PUT'
      });
    }

    // ==================== 签到任务相关接口 ====================

    // 获取签到数据（余额、连续签到天数、签到日历）
  }, {
    key: "getSignInData",
    value: function getSignInData() {
      return this.request({
        url: '/api/signin/data',
        method: 'GET'
      });
    }

    // 执行签到
  }, {
    key: "doSignIn",
    value: function doSignIn() {
      return this.request({
        url: '/api/signin/do',
        method: 'POST'
      });
    }

    // 获取每日任务列表
  }, {
    key: "getDailyTasks",
    value: function getDailyTasks() {
      return this.request({
        url: '/api/tasks/daily',
        method: 'GET'
      });
    }

    // 领取任务奖励
  }, {
    key: "claimTaskReward",
    value: function claimTaskReward(taskId) {
      return this.request({
        url: '/api/tasks/claim',
        method: 'POST',
        data: {
          taskId: taskId
        }
      });
    }

    // 获取任务进度
  }, {
    key: "getTaskProgress",
    value: function getTaskProgress() {
      return this.request({
        url: '/api/tasks/progress',
        method: 'GET'
      });
    }

    // ==================== 积分商城相关接口 ====================

    // 获取积分商城产品列表
  }, {
    key: "getPointsMallProducts",
    value: function getPointsMallProducts() {
      var extra = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/points/products',
        method: 'GET',
        showLoading: false,
        // 由页面自己控制loading
        timeout: extra.timeout
      });
    }

    // 兑换积分商品
  }, {
    key: "exchangeProduct",
    value: function exchangeProduct(data) {
      // 兼容旧版本：如果传入的是数字，转换为对象
      if (typeof data === 'number' || typeof data === 'string' && /^\d+$/.test(data)) {
        data = {
          productId: data
        };
      }
      return this.request({
        url: '/api/points/exchange',
        method: 'POST',
        data: data
      });
    }

    // 获取兑换记录（可选 userId：与订单列表一致；extra.timeout 可选）
  }, {
    key: "getExchangeHistory",
    value: function getExchangeHistory() {
      var page = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var size = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 20;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      var userId = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : null;
      var extra = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : {};
      var url = "/api/points/exchange/history?page=".concat(page, "&size=").concat(size);
      if (userId != null && userId !== '' && userId !== 'undefined' && userId !== 'null') {
        url += "&userId=".concat(userId);
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: showLoading,
        timeout: extra.timeout
      });
    }

    // 获取积分记录
  }, {
    key: "getPointsRecords",
    value: function getPointsRecords() {
      var userId = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
      var type = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'earn';
      var url = '/api/user/points/records';
      // GET 请求参数拼接到 URL
      var params = [];
      if (type) {
        params.push("type=".concat(type));
      }
      if (userId) {
        params.push("userId=".concat(userId));
      }
      if (params.length > 0) {
        url += '?' + params.join('&');
      }
      return this.request({
        url: url,
        method: 'GET'
      });
    }

    // 获取积分统计
  }, {
    key: "getPointsStatistics",
    value: function getPointsStatistics() {
      var userId = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
      var url = '/api/user/points/statistics';
      // GET 请求参数拼接到 URL
      if (userId) {
        url += "?userId=".concat(userId);
      }
      return this.request({
        url: url,
        method: 'GET'
      });
    }

    // ==================== 社区相关接口 ====================

    // 获取帖子列表
  }, {
    key: "getPostList",
    value: function getPostList() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/community/posts',
        method: 'GET',
        data: params
      });
    }

    // 获取帖子详情（默认不弹全局 loading，由详情页自行展示，避免与 uni.showLoading 叠加导致难关闭）
  }, {
    key: "getPostDetail",
    value: function getPostDetail(postId) {
      var userId = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      var params = userId ? {
        userId: userId
      } : {};
      return this.request({
        url: "/api/community/posts/".concat(postId),
        method: 'GET',
        data: params,
        showLoading: showLoading
      });
    }

    // 发布帖子
  }, {
    key: "publishPost",
    value: function publishPost(postData) {
      return this.request({
        url: '/api/community/posts',
        method: 'POST',
        data: postData,
        // 发布流程可能包含服务端审核/转码等耗时，避免默认 30s 误判超时
        timeout: 120000
      });
    }

    // ==================== 标签相关接口 ====================

    // 获取热门标签
  }, {
    key: "getHotTags",
    value: function getHotTags() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 10;
      return this.request({
        url: "/api/tag/hot?limit=".concat(limit),
        method: 'GET',
        showLoading: false // 这个接口不需要loading，因为不是关键操作
      });
    }

    // 根据标签查询帖子
  }, {
    key: "getPostsByTag",
    value: function getPostsByTag(tag) {
      var page = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;
      var size = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 20;
      return this.request({
        url: "/api/tag/posts?tag=".concat(encodeURIComponent(tag), "&page=").concat(page, "&size=").concat(size),
        method: 'GET',
        showLoading: false
      });
    }

    // 获取推荐帖子
  }, {
    key: "getRecommendPosts",
    value: function getRecommendPosts() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 10;
      return this.request({
        url: "/api/tag/recommend?limit=".concat(limit),
        method: 'GET',
        showLoading: false
      });
    }

    // 更新帖子
  }, {
    key: "updatePost",
    value: function updatePost(postId, postData, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId),
        method: 'PUT',
        data: _objectSpread(_objectSpread({}, postData), {}, {
          userId: userId
        })
      });
    }

    // 删除帖子
  }, {
    key: "deletePost",
    value: function deletePost(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 点赞帖子
  }, {
    key: "likePost",
    value: function likePost(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/like?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消点赞帖子
  }, {
    key: "unlikePost",
    value: function unlikePost(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/like?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 获取热门帖子
  }, {
    key: "getHotPosts",
    value: function getHotPosts() {
      var limit = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 10;
      return this.request({
        url: '/api/community/posts/hot',
        method: 'GET',
        data: {
          limit: limit
        }
      });
    }

    // 视频推荐流（类抖音）：登录用户走个性化，未登录走热度分页
  }, {
    key: "getVideoFeed",
    value: function getVideoFeed() {
      var page = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
      var size = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 10;
      var userId = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : null;
      var data = {
        page: page,
        size: size
      };
      if (userId !== null && userId !== undefined && userId !== '') {
        data.userId = userId;
      }
      return this.request({
        url: '/api/community/posts/video-feed',
        method: 'GET',
        data: data,
        showLoading: false
      });
    }

    // 获取关注用户的帖子
  }, {
    key: "getFollowingPosts",
    value: function getFollowingPosts(userId) {
      var params = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
      if (!userId || userId === null || userId === undefined) {
        return Promise.reject(new Error('用户ID不能为空'));
      }
      return this.request({
        url: '/api/community/posts/following',
        method: 'GET',
        data: _objectSpread({
          userId: userId
        }, params)
      });
    }

    // 获取帖子评论列表（showLoading 可选，列表页/详情页批量请求时传 false 避免长时间 loading）
  }, {
    key: "getPostComments",
    value: function getPostComments(postId, userId) {
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      var url = "/api/community/posts/".concat(postId, "/comments");
      if (userId) {
        url += "?userId=".concat(userId);
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: !!showLoading
      });
    }

    // 获取评论的回复列表（showLoading 可选，详情页按评论逐条请求时传 false 避免长时间 loading）
  }, {
    key: "getCommentReplies",
    value: function getCommentReplies(commentId, userId) {
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
      var url = "/api/community/comments/".concat(commentId, "/replies");
      if (userId) {
        url += "?userId=".concat(userId);
      }
      return this.request({
        url: url,
        method: 'GET',
        showLoading: !!showLoading
      });
    }

    // 获取@提及用户列表（抖音式：输入@后弹出选择）
  }, {
    key: "getMentionUsers",
    value: function getMentionUsers(postId, userId) {
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      return this.request({
        url: "/api/community/posts/".concat(postId, "/mention-users"),
        method: 'GET',
        data: {
          userId: userId
        },
        showLoading: !!showLoading
      });
    }

    // 添加评论
  }, {
    key: "addComment",
    value: function addComment(postId, commentData) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/comments"),
        method: 'POST',
        data: commentData
      });
    }

    // 删除评论
  }, {
    key: "deleteComment",
    value: function deleteComment(commentId, userId) {
      return this.request({
        url: "/api/community/comments/".concat(commentId, "?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 点赞评论
  }, {
    key: "likeComment",
    value: function likeComment(commentId, userId) {
      return this.request({
        url: "/api/community/comments/".concat(commentId, "/like?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消点赞评论
  }, {
    key: "unlikeComment",
    value: function unlikeComment(commentId, userId) {
      return this.request({
        url: "/api/community/comments/".concat(commentId, "/like?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 关注用户
  }, {
    key: "followUser",
    value: function followUser(followingId, followerId) {
      return this.request({
        url: "/api/community/follow?followingId=".concat(followingId, "&followerId=").concat(followerId),
        method: 'POST'
      });
    }

    // 取消关注用户
  }, {
    key: "unfollowUser",
    value: function unfollowUser(followingId, followerId) {
      return this.request({
        url: "/api/community/follow?followingId=".concat(followingId, "&followerId=").concat(followerId),
        method: 'DELETE'
      });
    }

    // 获取用户关注列表
  }, {
    key: "getFollowingList",
    value: function getFollowingList(userId) {
      return this.request({
        url: "/api/community/users/".concat(userId, "/following"),
        method: 'GET'
      });
    }

    // 获取用户粉丝列表
  }, {
    key: "getFollowerList",
    value: function getFollowerList(userId) {
      return this.request({
        url: "/api/community/users/".concat(userId, "/followers"),
        method: 'GET'
      });
    }

    // 检查关注状态
  }, {
    key: "checkFollowStatus",
    value: function checkFollowStatus(followerId, followingId) {
      return this.request({
        url: '/api/community/follow/status',
        method: 'GET',
        data: {
          followerId: followerId,
          followingId: followingId
        }
      });
    }

    // 获取用户关注的用户列表
  }, {
    key: "getFollowedUsers",
    value: function getFollowedUsers(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      if (!userId || userId === null || userId === undefined) {
        return Promise.reject(new Error('用户ID不能为空'));
      }
      return this.request({
        url: "/api/community/users/".concat(userId, "/followed"),
        method: 'GET',
        showLoading: showLoading
      });
    }

    // 获取推荐用户列表（社区关注页）
  }, {
    key: "getRecommendedUsers",
    value:
    // 获取推荐用户（params.userId 必填，否则后端会报 NumberFormatException）
    function getRecommendedUsers() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var _params$showLoading3 = params.showLoading,
        showLoading = _params$showLoading3 === void 0 ? true : _params$showLoading3,
        rest = (0, _objectWithoutProperties2.default)(params, _excluded4);
      var data = {
        limit: rest.limit || 10
      };
      if (rest.userId != null && rest.userId !== '') data.userId = rest.userId;
      return this.request({
        url: '/api/messages/recommended-users',
        method: 'GET',
        data: data,
        showLoading: showLoading
      });
    }
  }, {
    key: "getPostFeed",
    value:
    // 获取帖子Feed（置顶+热门+最新+关注区+发现区）
    function getPostFeed() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/community/posts/feed',
        method: 'GET',
        data: params
      });
    }

    /**
     * 发现页规则型推荐（小红书风格）
     * 服务端按 score = 兴趣匹配(+5) + 点赞×2 + 收藏×3 + 评论×4 + 浏览×0.2 + 新帖24h(+20) 排序
     */
  }, {
    key: "getDiscoverRecommend",
    value: function getDiscoverRecommend() {
      var _params$page, _params$size;
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var data = {
        page: (_params$page = params.page) !== null && _params$page !== void 0 ? _params$page : 1,
        size: (_params$size = params.size) !== null && _params$size !== void 0 ? _params$size : 20
      };
      if (params.userId != null && params.userId !== '') {
        data.userId = params.userId;
      }
      if (params.tag != null && params.tag !== '') {
        data.tag = params.tag;
      }
      return this.request({
        url: '/api/community/posts/recommend',
        method: 'GET',
        data: data
      });
    }

    // 获取发现页标签
  }, {
    key: "getDiscoverTags",
    value: function getDiscoverTags() {
      return this.request({
        url: '/api/tag/all',
        method: 'GET',
        showLoading: false
      });
    }

    // 协同过滤推荐（预留接口）
  }, {
    key: "getCollaborativeFiltering",
    value: function getCollaborativeFiltering(userId) {
      var limit = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 20;
      return this.request({
        url: '/api/recommendation/collaborative-filtering',
        method: 'GET',
        data: {
          userId: userId,
          limit: limit
        }
      });
    }

    // 获取用户兴趣标签
  }, {
    key: "getUserInterests",
    value: function getUserInterests(userId) {
      return this.request({
        url: '/api/recommendation/user-interests',
        method: 'GET',
        data: {
          userId: userId
        }
      });
    }

    // 获取最新帖子（分页）
  }, {
    key: "getLatestPosts",
    value: function getLatestPosts() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/community/posts',
        method: 'GET',
        data: {
          page: params.page || 1,
          size: params.size || 10,
          category: params.category || null
        }
      });
    }

    // 首页搜索 - 商品和服务
  }, {
    key: "searchHome",
    value: function searchHome(keyword) {
      return this.request({
        url: '/api/search/home',
        method: 'GET',
        data: {
          keyword: keyword
        }
      });
    }

    // 社区搜索 - 帖子
  }, {
    key: "searchCommunity",
    value: function searchCommunity(keyword) {
      return this.request({
        url: '/api/search/community',
        method: 'GET',
        data: {
          keyword: keyword
        }
      });
    }

    // 获取热搜关键词
  }, {
    key: "getHotKeywords",
    value: function getHotKeywords() {
      return this.request({
        url: '/api/search/hot',
        method: 'GET'
      });
    }

    // 收藏帖子
  }, {
    key: "collectPost",
    value: function collectPost(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/collect?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消收藏帖子
  }, {
    key: "uncollectPost",
    value: function uncollectPost(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/collect?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 检查收藏状态
  }, {
    key: "checkCollectStatus",
    value: function checkCollectStatus(postId, userId) {
      return this.request({
        url: "/api/community/posts/".concat(postId, "/collect/status?userId=").concat(userId),
        method: 'GET'
      });
    }

    // 获取收藏的帖子列表
  }, {
    key: "getCollectedPosts",
    value: function getCollectedPosts() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var _params$showLoading4 = params.showLoading,
        showLoading = _params$showLoading4 === void 0 ? true : _params$showLoading4,
        rest = (0, _objectWithoutProperties2.default)(params, _excluded5);
      return this.request({
        url: '/api/community/posts/collected',
        method: 'GET',
        data: {
          userId: rest.userId,
          page: rest.page || 1,
          size: rest.size || 10,
          type: rest.type || null
        },
        showLoading: showLoading
      });
    }

    // ========== 商品收藏相关接口 ==========

    // 收藏商品
  }, {
    key: "collectProduct",
    value: function collectProduct(productId, userId) {
      return this.request({
        url: "/api/product/".concat(productId, "/collect?userId=").concat(userId),
        method: 'POST'
      });
    }

    // 取消收藏商品
  }, {
    key: "uncollectProduct",
    value: function uncollectProduct(productId, userId) {
      return this.request({
        url: "/api/product/".concat(productId, "/collect?userId=").concat(userId),
        method: 'DELETE'
      });
    }

    // 检查商品收藏状态
  }, {
    key: "checkProductCollectStatus",
    value: function checkProductCollectStatus(productId, userId) {
      return this.request({
        url: "/api/product/".concat(productId, "/collect/status?userId=").concat(userId),
        method: 'GET'
      });
    }

    // 获取收藏的商品列表
  }, {
    key: "getCollectedProducts",
    value: function getCollectedProducts() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var _params$showLoading5 = params.showLoading,
        showLoading = _params$showLoading5 === void 0 ? true : _params$showLoading5,
        rest = (0, _objectWithoutProperties2.default)(params, _excluded6);
      return this.request({
        url: '/api/product/collected',
        method: 'GET',
        data: {
          userId: rest.userId,
          page: rest.page || 1,
          size: rest.size || 10
        },
        showLoading: showLoading
      });
    }

    // ========== 私信相关接口 ==========

    // 获取私信列表
  }, {
    key: "getPrivateMessages",
    value: function getPrivateMessages() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var queryParams = {
        userId: params.userId,
        page: params.page || 1,
        size: params.size || 20
      };

      // 如果提供了conversationId，使用它（用于客服消息）
      if (params.conversationId) {
        queryParams.conversationId = params.conversationId;
      } else if (params.targetUserId) {
        queryParams.targetUserId = params.targetUserId;
      }
      return this.request({
        url: '/api/messages/private',
        method: 'GET',
        data: queryParams,
        showLoading: params.showLoading !== false // 默认显示loading，轮询时可传false
      });
    }

    // 发送私信
  }, {
    key: "sendPrivateMessage",
    value: function sendPrivateMessage(data) {
      return this.request({
        url: '/api/messages/private',
        method: 'POST',
        data: {
          senderId: data.senderId,
          receiverId: data.receiverId,
          content: data.content,
          conversationId: data.conversationId // 支持传递会话ID（用于客服会话）
        }
      });
    }

    // 发送客服消息
  }, {
    key: "sendServiceMessage",
    value: function sendServiceMessage(data) {
      return this.request({
        url: '/api/messages/service',
        method: 'POST',
        data: {
          userId: data.userId,
          storeId: data.storeId,
          content: data.content
        }
      });
    }

    // 获取会话列表
  }, {
    key: "getConversationList",
    value: function getConversationList(userId) {
      var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
      return this.request({
        url: '/api/messages/conversations',
        method: 'GET',
        data: {
          userId: userId
        },
        showLoading: showLoading
      });
    }

    /** 服务端持久化隐藏会话（清缓存后不再出现），conversationId 与列表一致，医师会话为 doctor_{id} */
  }, {
    key: "hideConversation",
    value: function hideConversation(userId, conversationId) {
      var showLoading = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
      return this.request({
        url: '/api/messages/conversations/hide',
        method: 'POST',
        data: {
          userId: userId,
          conversationId: conversationId
        },
        showLoading: showLoading
      });
    }

    // 标记消息为已读
  }, {
    key: "markMessagesAsRead",
    value: function markMessagesAsRead(data) {
      return this.request({
        url: '/api/messages/read',
        method: 'POST',
        data: {
          userId: data.userId,
          conversationId: data.conversationId
        }
      });
    }

    // ========== 互动消息相关接口 ==========

    // 获取点赞和收藏列表
  }, {
    key: "getLikesAndCollections",
    value: function getLikesAndCollections() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/messages/likes',
        method: 'GET',
        data: {
          userId: params.userId,
          page: params.page || 1,
          size: params.size || 20
        }
      });
    }

    // 获取新增关注列表
  }, {
    key: "getNewFollows",
    value: function getNewFollows() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/messages/follows',
        method: 'GET',
        data: {
          userId: params.userId,
          page: params.page || 1,
          size: params.size || 20
        }
      });
    }

    // 获取评论和@列表
  }, {
    key: "getCommentsAndMentions",
    value: function getCommentsAndMentions() {
      var params = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      return this.request({
        url: '/api/messages/comments',
        method: 'GET',
        data: {
          userId: params.userId,
          page: params.page || 1,
          size: params.size || 20
        }
      });
    }
  }]);
  return ApiService;
}(); // 创建API实例
var api = new ApiService();
exports.api = api;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 39:
/*!************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/objectWithoutProperties.js ***!
  \************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var objectWithoutPropertiesLoose = __webpack_require__(/*! ./objectWithoutPropertiesLoose.js */ 40);
function _objectWithoutProperties(source, excluded) {
  if (source == null) return {};
  var target = objectWithoutPropertiesLoose(source, excluded);
  var key, i;
  if (Object.getOwnPropertySymbols) {
    var sourceSymbolKeys = Object.getOwnPropertySymbols(source);
    for (i = 0; i < sourceSymbolKeys.length; i++) {
      key = sourceSymbolKeys[i];
      if (excluded.indexOf(key) >= 0) continue;
      if (!Object.prototype.propertyIsEnumerable.call(source, key)) continue;
      target[key] = source[key];
    }
  }
  return target;
}
module.exports = _objectWithoutProperties, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 4:
/*!**********************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/interopRequireDefault.js ***!
  \**********************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : {
    "default": obj
  };
}
module.exports = _interopRequireDefault, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 40:
/*!*****************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/objectWithoutPropertiesLoose.js ***!
  \*****************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _objectWithoutPropertiesLoose(source, excluded) {
  if (source == null) return {};
  var target = {};
  var sourceKeys = Object.keys(source);
  var key, i;
  for (i = 0; i < sourceKeys.length; i++) {
    key = sourceKeys[i];
    if (excluded.indexOf(key) >= 0) continue;
    target[key] = source[key];
  }
  return target;
}
module.exports = _objectWithoutPropertiesLoose, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 41:
/*!**********************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/common/js/util.js ***!
  \**********************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.util = void 0;
var _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/defineProperty */ 11));
var _typeof2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/typeof */ 13));
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2.default)(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
// common/js/util.js 工具函数

var API_BASE_STORAGE_KEY = 'PETHOME_API_BASE_URL';
var INTERNAL_IP_STORAGE_KEY = 'PETHOME_INTERNAL_IP'; // 存储原始内网IP，用于图片URL转换
// 微信开发者工具在 Windows 上可能无法访问内网 IP，使用 localhost
var DEFAULT_HTTP_BASE = 'http://localhost:8080';
var DEFAULT_HTTPS_BASE = 'https://localhost';
var DEFAULT_BACKEND_PORT = '8080';
// 图片 URL 中 localhost 替换兜底（与 main.js 中 DEV_API_BASE 保持一致，避免子包/时序导致未替换）
var FALLBACK_DEV_IMAGE_ORIGIN = 'http://10.43.238.18:8080';
// 线上图片域名兜底：接口/数据库返回 localhost 时，若未替换则用此域名，避免封面黑块（与 main.js API_BASE 一致）
var PRODUCTION_IMAGE_ORIGIN = 'https://situationship.icu';

// 解析URL的辅助函数（替代URL构造函数，兼容小程序环境）
function parseUrl(url) {
  if (typeof url !== 'string' || !url) {
    return null;
  }
  var urlStr = url.trim();
  if (!urlStr) return null;

  // 匹配协议、主机名、端口、路径
  // 格式: http://hostname:port/path 或 https://hostname:port/path
  var match = urlStr.match(/^(https?):\/\/([^\/:]+)(:(\d+))?(\/.*)?$/);
  if (!match) {
    // 如果没有协议，尝试匹配 hostname:port/path
    var matchNoProtocol = urlStr.match(/^([^\/:]+)(:(\d+))?(\/.*)?$/);
    if (matchNoProtocol) {
      return {
        protocol: 'http',
        hostname: matchNoProtocol[1] || '',
        port: matchNoProtocol[3] || '',
        pathname: matchNoProtocol[4] || '/',
        origin: "http://".concat(matchNoProtocol[1]).concat(matchNoProtocol[3] ? ":".concat(matchNoProtocol[3]) : '')
      };
    }
    return null;
  }
  return {
    protocol: match[1] || 'http',
    hostname: match[2] || '',
    port: match[4] || '',
    pathname: match[5] || '/',
    origin: "".concat(match[1], "://").concat(match[2]).concat(match[4] ? ":".concat(match[4]) : '')
  };
}
function normalizeBaseUrl(url) {
  if (typeof url !== 'string') return '';
  var normalized = url.trim();
  if (!normalized) return '';
  // 去掉末尾的斜杠，保留协议
  normalized = normalized.replace(/\/+$/, '');
  return normalized;
}
function safeGetFromStorage(key) {
  try {
    if (typeof uni !== 'undefined' && uni.getStorageSync) {
      return uni.getStorageSync(key) || '';
    }
  } catch (err) {}
  return '';
}
function safeSetToStorage(key, value) {
  try {
    if (typeof uni !== 'undefined' && uni.setStorageSync) {
      uni.setStorageSync(key, value);
    }
  } catch (err) {}
}
function resolveBaseFromWindow() {
  if (typeof window === 'undefined' || !window.location) return '';
  var _window$location = window.location,
    protocol = _window$location.protocol,
    hostname = _window$location.hostname;
  if (!hostname) return '';
  var targetPort = (window.__PETHOME_BACKEND_PORT__ || DEFAULT_BACKEND_PORT || '').toString();
  var needPort = targetPort && targetPort !== '80' && targetPort !== '443';
  return "".concat(protocol, "//").concat(hostname).concat(needPort ? ":".concat(targetPort) : '');
}
function resolveBaseFromLaunchOptions() {
  if (typeof uni === 'undefined' || !uni.getLaunchOptionsSync) return '';
  try {
    var options = uni.getLaunchOptionsSync();
    if (!options) return '';
    if (options.query && options.query.apiBaseUrl) {
      return decodeURIComponent(options.query.apiBaseUrl);
    }
    if (options.referrerInfo && options.referrerInfo.extraData && options.referrerInfo.extraData.apiBaseUrl) {
      return options.referrerInfo.extraData.apiBaseUrl;
    }
  } catch (err) {}
  return '';
}
function sanitizeBaseUrl(url) {
  var normalized = normalizeBaseUrl(url);
  if (!normalized) return '';
  if (/^https?:\/\//i.test(normalized)) {
    return normalized;
  }
  return "http://".concat(normalized);
}

// 检测是否是真机环境（使用新的API避免废弃警告）
function isRealDevice() {
  try {
    if (typeof uni !== 'undefined') {
      // 优先使用新的API
      if (uni.getDeviceInfo) {
        try {
          var deviceInfo = uni.getDeviceInfo();
          return deviceInfo.platform !== 'devtools';
        } catch (e) {
          // 如果新API失败，回退到旧API
        }
      }
      // 回退到旧API（兼容性）
      if (uni.getSystemInfoSync) {
        var systemInfo = uni.getSystemInfoSync();
        // 真机环境：platform 不是 'devtools'
        return systemInfo.platform !== 'devtools';
      }
    }
  } catch (err) {
    // 静默处理错误
  }
  return false;
}
function getApiBaseUrl() {
  var cached = normalizeBaseUrl(safeGetFromStorage(API_BASE_STORAGE_KEY));
  var isReal = isRealDevice();

  // 开发环境：如果缓存的是错误的虚拟网卡IP（172.27.x.x），忽略它
  // 因为这种IP通常是Clash/TUN等虚拟网卡，手机无法访问
  if (cached && /^https?:\/\/172\.27\./.test(cached)) {
    // 清除错误的缓存
    safeSetToStorage(API_BASE_STORAGE_KEY, '');
    // 继续执行，使用默认值或启动参数
  }

  if (cached && !/^https?:\/\/172\.27\./.test(cached)) {
    // 如果缓存的是内网 IP 地址（10.x.x.x, 172.16-31.x.x, 192.168.x.x）
    var isInternalIP = /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(cached);
    if (isInternalIP) {
      // 保存原始内网IP，用于图片URL转换
      try {
        var parsed = parseUrl(cached);
        if (parsed && parsed.hostname) {
          safeSetToStorage(INTERNAL_IP_STORAGE_KEY, parsed.hostname);
          // 移除内网IP保存日志以提升性能
        }
      } catch (err) {
        // 静默处理错误
      }

      // 真机环境：使用内网 IP（手机可以访问）
      // 开发者工具：使用 localhost（工具可以访问）
      if (isReal) {
        // 移除真机环境日志以提升性能
        return cached;
      } else {
        // 移除开发者工具环境切换日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, DEFAULT_HTTP_BASE);
        return DEFAULT_HTTP_BASE;
      }
    }
    if (cached.includes('localhost') || cached.includes('127.0.0.1')) {
      return cached;
    }
    return cached;
  }
  var launchBase = normalizeBaseUrl(resolveBaseFromLaunchOptions());
  if (launchBase) {
    // 检查启动参数中的地址是否是内网 IP
    var _isInternalIP = /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(launchBase);
    if (_isInternalIP) {
      // 保存原始内网IP，用于图片URL转换
      try {
        var _parsed = parseUrl(launchBase);
        if (_parsed && _parsed.hostname) {
          safeSetToStorage(INTERNAL_IP_STORAGE_KEY, _parsed.hostname);
          // 移除内网IP保存日志以提升性能
        }
      } catch (err) {
        // 静默处理错误
      }

      // 真机环境：使用内网 IP（手机可以访问）
      // 开发者工具：使用 localhost（工具可以访问）
      if (isReal) {
        // 移除启动参数内网IP日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, launchBase);
        return launchBase;
      } else {
        // 移除开发者工具环境切换日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, DEFAULT_HTTP_BASE);
        return DEFAULT_HTTP_BASE;
      }
    }
    safeSetToStorage(API_BASE_STORAGE_KEY, launchBase);
    return launchBase;
  }
  var windowBase = normalizeBaseUrl(resolveBaseFromWindow());
  if (windowBase) {
    safeSetToStorage(API_BASE_STORAGE_KEY, windowBase);
    return windowBase;
  }
  return DEFAULT_HTTP_BASE;
}
function setApiBaseUrl(url) {
  var sanitized = sanitizeBaseUrl(url);
  if (!sanitized) return;
  safeSetToStorage(API_BASE_STORAGE_KEY, sanitized);
}
function getHttpBase() {
  return getApiBaseUrl() || DEFAULT_HTTP_BASE;
}
function getHttpsBase() {
  var httpBase = getHttpBase();
  try {
    var parsed = parseUrl(httpBase);
    if (!parsed || !parsed.hostname) {
      return DEFAULT_HTTPS_BASE;
    }
    if (parsed.protocol === 'https') {
      return parsed.origin;
    }

    // 针对内网 / 开发环境，保留端口号（如果存在且不是80/443）
    var hostname = parsed.hostname;
    var portPart = '';

    // 开发环境：如果原端口是8080，转换为8443（HTTPS端口）
    if (parsed.port === '8080') {
      portPart = ':8443';
    } else if (parsed.port && parsed.port !== '80' && parsed.port !== '443') {
      // 其他端口保留
      portPart = ":".concat(parsed.port);
    }
    return "https://".concat(hostname).concat(portPart);
  } catch (err) {
    return DEFAULT_HTTPS_BASE;
  }
}

/**
 * 处理图片URL，解决小程序HTTP协议限制问题
 * @param {string} imageUrl 原始图片URL
 * @returns {string} 处理后的图片URL
 */
function getImageUrl(imageUrl) {
  var HTTP_BASE = getHttpBase();
  var HTTPS_BASE = getHttpsBase();
  var isReal = isRealDevice(); // 判断是否是真机环境

  // 调试日志：输出环境信息（减少日志输出，只在关键转换时输出）
  // if (imageUrl && (imageUrl.includes('upload') || imageUrl.includes('images'))) {
  //   console.log(`[图片URL处理] 环境: ${isReal ? '真机' : '开发者工具'}, HTTP_BASE: ${HTTP_BASE}, HTTPS_BASE: ${HTTPS_BASE}, 原始URL: ${imageUrl}`)
  // }

  // 判断是否是内网/本地地址（需要保持HTTP）
  var isLocalOrInternal = function isLocalOrInternal(url) {
    if (!url) return false;
    // localhost 和 127.0.0.1
    if (url.includes('localhost') || url.includes('127.0.0.1')) {
      return true;
    }
    // 内网IP地址：10.x.x.x, 172.16-31.x.x, 192.168.x.x
    var internalIpPattern = /(^|\/)(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/;
    if (internalIpPattern.test(url)) {
      return true;
    }
    return false;
  };

  // 判断是否是IP地址（包括公网IP和内网IP）
  var isIpAddress = function isIpAddress(url) {
    if (!url) return false;
    // 匹配IP地址格式：xxx.xxx.xxx.xxx
    var ipPattern = /\b(\d{1,3}\.){3}\d{1,3}\b/;
    return ipPattern.test(url);
  };

  // 判断是否是合法域名（不是IP地址）
  var isLegalDomain = function isLegalDomain(url) {
    if (!url) return false;
    // 如果包含域名特征（如 .com, .cn, .io, .net 等），且不是IP地址
    var domainPattern = /^https?:\/\/[^\/]+/;
    var match = url.match(domainPattern);
    if (match) {
      var host = match[0].replace(/^https?:\/\//, '');
      // 如果是IP地址，返回false
      if (isIpAddress(host)) {
        return false;
      }
      // 如果包含域名特征，返回true
      return /\./.test(host) && !host.startsWith('localhost');
    }
    return false;
  };

  // 默认图片直接使用静态资源路径（/static/目录下的资源不需要转换为HTTP URL）
  var defaultImage = '/static/images/garfield-default-avatar.png';
  if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined') {
    return defaultImage;
  }

  // 转换为字符串并去除首尾空格
  imageUrl = String(imageUrl).trim();

  // 如果是空字符串，返回默认图片
  if (imageUrl === '') {
    return defaultImage;
  }

  // 兜底：后端或数据里可能误把完整 URL 存成 "/https://..." 或 "/http://..."，导致被当成相对路径拼出错误地址，先去掉前导 /
  if (imageUrl.startsWith('/https://') || imageUrl.startsWith('/http://')) {
    imageUrl = imageUrl.slice(1);
  }

  // 只要图片地址是 localhost/127.0.0.1（任意端口），就统一换成可用的域名，避免封面黑屏、ERR_CONNECTION_REFUSED
  var apiBaseRaw = getApiBaseUrl();
  var imageOrigin = (apiBaseRaw || '').trim().replace(/\/api\/?$/, '').replace(/\/+$/, '');
  var isProductionDomain = imageOrigin && /^https?:\/\//.test(imageOrigin) && !/localhost|127\.0\.0\.1/.test(imageOrigin);
  var isLocalhostImage = (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) && (imageUrl.startsWith('http://') || imageUrl.startsWith('https://'));
  if (isLocalhostImage) {
    var pathMatch = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/);
    if (pathMatch && pathMatch[1]) {
      var path = pathMatch[1].trim();
      var isApiLocalhost = imageOrigin && (imageOrigin.includes('localhost') || imageOrigin.includes('127.0.0.1'));
      if (isProductionDomain) {
        imageUrl = imageOrigin + path;
      } else if (isApiLocalhost) {
        // 本地开发：API 和图片都在 localhost，保持原样，避免替换成线上域名导致新上传图片 404
        // imageUrl 已是 localhost 地址，无需修改
      } else {
        // 配置未设置或仍是 localhost 时，用线上域名兜底，确保社区/发现流封面能加载
        imageUrl = PRODUCTION_IMAGE_ORIGIN + path;
      }
    }
  }
  // 若后端返回了带 :8443 的完整 URL，且上面未替换（pathMatch 未命中），再改为使用与接口同源的 base（图片用无 /api 的 origin）
  if ((imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) && imageUrl.includes(':8443')) {
    var _pathMatch = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/);
    if (_pathMatch) {
      var _path = _pathMatch[1];
      var base = (apiBaseRaw || '').replace(/\/api\/?$/, '').replace(/\/+$/, '');
      if (base) imageUrl = base + _path;
    }
  }

  // 如果包含URL编码的引号（%22），说明可能是被错误编码的字符串，尝试解码
  if (imageUrl.includes('%22') || imageUrl.includes('%27')) {
    try {
      imageUrl = decodeURIComponent(imageUrl);
      // 如果解码后包含引号，去掉引号
      imageUrl = imageUrl.replace(/^["']|["']$/g, '').trim();
    } catch (e) {
      // 静默处理错误
    }
  }

  // 如果以 [ 开头和 ] 结尾，说明可能是数组格式的字符串，尝试提取URL
  if (imageUrl.startsWith('[') && imageUrl.endsWith(']')) {
    try {
      var parsed = JSON.parse(imageUrl);
      if (Array.isArray(parsed) && parsed.length > 0) {
        imageUrl = String(parsed[0]).trim();
      } else {
        return defaultImage;
      }
    } catch (e) {
      // 如果不是有效的JSON，尝试提取引号内的内容
      var match = imageUrl.match(/["']([^"']+)["']/);
      if (match && match[1]) {
        imageUrl = match[1].trim();
      } else {
        return defaultImage;
      }
    }
  }

  // 优先处理临时路径（tmp或__tmp__），必须在HTTP检查之前
  // 匹配 /tmp/、/__tmp__/、http://tmp/xxx、http://__tmp__/xxx、tmp/xxx、__tmp__/xxx 等格式
  if (imageUrl.startsWith('/tmp/') || imageUrl.startsWith('/__tmp__/') || imageUrl.startsWith('http://tmp/') || imageUrl.startsWith('http://__tmp__/') || imageUrl.startsWith('https://tmp/') || imageUrl.startsWith('https://__tmp__/') || imageUrl.startsWith('tmp/') && !imageUrl.includes('://') || imageUrl.startsWith('__tmp__/') && !imageUrl.includes('://')) {
    return defaultImage;
  }

  // 尽早将 localhost/127.0.0.1 替换为当前配置的后端地址，避免个人页/商品/店铺等图片在真机或模拟器加载失败
  if ((imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) && (imageUrl.startsWith('http://') || imageUrl.startsWith('https://'))) {
    // 开发者工具下：若配置已是线上域名，上面已替换；否则用当前配置的 host:port（避免强制 8443 导致 REFUSED）
    if (!isReal) {
      var devBase = getApiBaseUrl();
      var devParsed = parseUrl(devBase);
      var isDevLocalhost = devParsed && devParsed.hostname && /localhost|127\.0\.0\.1/.test(devParsed.hostname);
      var devOrigin;
      if (isDevLocalhost) {
        devOrigin = "http://".concat(devParsed.hostname).concat(devParsed.port ? ':' + devParsed.port : ':8080');
      } else if (devParsed && devParsed.hostname && !/localhost|127\.0\.0\.1/.test(devParsed.hostname)) {
        devOrigin = devParsed.protocol === 'https' ? devParsed.origin : "https://".concat(devParsed.hostname).concat(devParsed.port ? ':' + devParsed.port : '');
      } else {
        devOrigin = devParsed && devParsed.hostname ? "https://".concat(devParsed.hostname).concat(devParsed.port ? ':' + devParsed.port : ':8443') : 'https://localhost:8443';
      }
      var _pathMatch2 = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/);
      imageUrl = _pathMatch2 && _pathMatch2[1] ? devOrigin.replace(/\/+$/, '') + _pathMatch2[1] : imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1)(:\d+)?/, 'http://localhost:8080');
    } else {
      var originToUse = '';
      try {
        var _base = getApiBaseUrl();
        var _parsed2 = parseUrl(_base);
        if (_parsed2 && _parsed2.hostname && !/localhost|127\.0\.0\.1/.test(_parsed2.hostname)) {
          originToUse = "http://".concat(_parsed2.hostname).concat(_parsed2.port ? ':' + _parsed2.port : ':8080');
        }
      } catch (e) {}
      if (!originToUse) {
        originToUse = FALLBACK_DEV_IMAGE_ORIGIN;
      }
      imageUrl = imageUrl.replace(/https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/, originToUse);
    }
  }
  var replaceToHttps = function replaceToHttps(url) {
    if (!url) return url;
    // 处理各种 localhost 格式
    if (url.startsWith('http://localhost:8080/')) {
      return url.replace('http://localhost:8080', HTTPS_BASE);
    }
    if (url.startsWith('http://localhost:8443/')) {
      return url.replace('http://localhost:8443', HTTPS_BASE);
    }
    if (url.startsWith('http://localhost/')) {
      return url.replace('http://localhost', HTTPS_BASE);
    }
    if (url.startsWith('http://127.0.0.1:8080/')) {
      return url.replace('http://127.0.0.1:8080', HTTPS_BASE);
    }
    if (url.startsWith('http://127.0.0.1:8443/')) {
      return url.replace('http://127.0.0.1:8443', HTTPS_BASE);
    }
    if (url.startsWith('http://127.0.0.1/')) {
      return url.replace('http://127.0.0.1', HTTPS_BASE);
    }
    // 处理 HTTP_BASE
    if (url.startsWith("".concat(HTTP_BASE, "/"))) {
      return url.replace(HTTP_BASE, HTTPS_BASE);
    }
    // 通用HTTP转HTTPS
    if (url.startsWith('http://')) {
      return url.replace(/^http:/, 'https:');
    }
    return url;
  };

  // 如果是本地静态资源（/static/），直接返回，不转换为HTTP URL
  // 在uniapp中，/static/目录下的资源在编译时会被打包到小程序中，不需要从服务器加载
  if (imageUrl.startsWith('/static/')) {
    // 移除静态资源日志以提升性能（频繁调用）
    return imageUrl;
  }

  // 如果是相对路径 /upload/... 或 /images/...
  // 开发环境（localhost/127.0.0.1）统一用 HTTP 8080，避免 8443 证书导致商品图/轮播图不显示
  if (imageUrl.startsWith('/upload/') || imageUrl.startsWith('/images/')) {
    var apiBaseIsDomain = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE);
    var finalUrl;
    if (apiBaseIsDomain) {
      finalUrl = "".concat(HTTPS_BASE).concat(imageUrl);
    } else {
      // 本地/内网环境：用 HTTP_BASE（8080）拼图，保证「不校验合法域名」时能加载
      finalUrl = "".concat(HTTP_BASE).concat(imageUrl);
    }
    // 若拼接后仍是 172.27（虚拟网卡），在开发者工具中改为 localhost 避免超时
    if (!isReal && /^https?:\/\/172\.27\.\d+\.\d+/.test(finalUrl)) {
      finalUrl = finalUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, 'http://localhost:8080');
    }
    return finalUrl;
  }

  // 如果是以 / 开头的其他路径（如 /upload/product/xxx）
  if (imageUrl.startsWith('/')) {
    var _apiBaseIsDomain = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE);
    if (_apiBaseIsDomain) {
      return "".concat(HTTPS_BASE).concat(imageUrl);
    }
    // 本地/内网：用 HTTP 8080 拼图，保证商品图等能加载
    return "".concat(HTTP_BASE).concat(imageUrl);
  }

  // 相对路径无前导 /（如 product/product-xxx.jpg、upload/xxx），数据库商品图常见格式，拼成 /upload/... 再走 base
  if (!imageUrl.startsWith('http') && imageUrl.length > 0) {
    var withLeadingSlash = imageUrl.startsWith('upload/') || imageUrl.startsWith('upload\\') ? '/' + imageUrl.replace(/\\/g, '/') : '/upload/' + imageUrl.replace(/\\/g, '/');
    var _apiBaseIsDomain2 = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE);
    var _finalUrl = _apiBaseIsDomain2 ? "".concat(HTTPS_BASE).concat(withLeadingSlash) : "".concat(HTTP_BASE).concat(withLeadingSlash);
    if (!isReal && /^https?:\/\/172\.27\.\d+\.\d+/.test(_finalUrl)) {
      _finalUrl = _finalUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, 'http://localhost:8080');
    }
    return _finalUrl;
  }

  // 开发者工具/本机无法访问 172.27 虚拟网卡（如 Clash TUN），导致 ERR_CONNECTION_TIMED_OUT，改为 localhost
  if ((imageUrl.startsWith('https://') || imageUrl.startsWith('http://')) && /^https?:\/\/172\.27\.\d+\.\d+/.test(imageUrl) && !isReal) {
    var useHttps = imageUrl.startsWith('https://');
    var originReplace = useHttps ? 'https://localhost:8443' : 'http://localhost:8080';
    return imageUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, originReplace);
  }

  // 最终兜底：任何仍含 localhost/127.0.0.1 的完整 URL 都改为线上域名，避免社区发现流封面黑块
  if ((imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) && (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1'))) {
    var m = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/);
    if (m && m[1]) {
      imageUrl = PRODUCTION_IMAGE_ORIGIN + m[1].trim();
    }
  }

  // 如果是HTTPS协议，直接返回
  if (imageUrl.startsWith('https://')) {
    return imageUrl;
  }

  // 如果是HTTP协议
  if (imageUrl.startsWith('http://')) {
    // 检查是否是开发环境（通过API base URL判断）
    var currentApiBase = getApiBaseUrl();
    var isDevelopment = currentApiBase.includes('localhost') || currentApiBase.includes('127.0.0.1') || /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(currentApiBase);

    // 开发环境：将 localhost 替换为内网 IP
    if (isDevelopment && (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1'))) {
      var portMatch = imageUrl.match(/https?:\/\/(localhost|127\.0\.0\.1):(\d+)/);
      if (portMatch) {
        var port = portMatch[2];
        // 获取内网 IP 地址
        var internalIp = null;

        // 优先从当前 API base 获取
        if (currentApiBase && !currentApiBase.includes('localhost') && !currentApiBase.includes('127.0.0.1')) {
          try {
            var _parsed3 = parseUrl(currentApiBase);
            if (_parsed3 && _parsed3.hostname) {
              var ipAddress = _parsed3.hostname;
              if (/^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(ipAddress)) {
                internalIp = ipAddress;
              }
            }
          } catch (err) {}
        }

        // 如果没找到，从存储的内网 IP 获取
        if (!internalIp) {
          var storedInternalIp = safeGetFromStorage(INTERNAL_IP_STORAGE_KEY);
          if (storedInternalIp && storedInternalIp.trim() && /^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(storedInternalIp.trim())) {
            internalIp = storedInternalIp.trim();
          }
        }

        // 如果还没找到，从存储的 API base 获取
        if (!internalIp) {
          var storedBase = safeGetFromStorage(API_BASE_STORAGE_KEY);
          if (storedBase && !storedBase.includes('localhost') && !storedBase.includes('127.0.0.1')) {
            try {
              var _parsed4 = parseUrl(storedBase);
              if (_parsed4 && _parsed4.hostname) {
                var storedIp = _parsed4.hostname;
                if (/^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(storedIp)) {
                  safeSetToStorage(INTERNAL_IP_STORAGE_KEY, storedIp);
                  internalIp = storedIp;
                }
              }
            } catch (err) {}
          }
        }

        // 如果找到了内网 IP，替换 localhost
        // 开发环境：统一使用HTTP（8080端口）
        // 真机环境下，<image>标签的HTTP图片会被阻止，需要在图片加载失败时使用downloadFile下载
        if (internalIp) {
          // 开发环境：使用HTTP（统一使用8080端口）
          var replacedUrl = imageUrl.replace(/https?:\/\/(localhost|127\.0\.0\.1):\d+/, "http://".concat(internalIp, ":8080"));
          // 移除日志输出以提升性能
          return replacedUrl;
        } else {
          // 如果没找到内网 IP
          // 特殊处理：如果端口是8443但协议是HTTP，改为8080端口的HTTP（开发环境）
          if (port === '8443' && imageUrl.startsWith('http://')) {
            var httpUrl = imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1):8443/, 'http://localhost:8080');
            // 移除日志输出以提升性能
            return httpUrl;
          }

          // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
          // 无论是开发者工具还是真机，开发环境都使用HTTP
          if (port === '8080') {
            // console.log('未找到内网 IP，保持原URL (开发环境HTTP):', imageUrl)
            return imageUrl;
          } else {
            // 其他端口，改为8080端口的HTTP（开发环境）
            var _httpUrl = imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1):\d+/, 'http://localhost:8080');
            // 移除端口转换日志以提升性能
            return _httpUrl;
          }
        }
      }
    }

    // 如果已经是完整URL（http:// 或 https://）
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      // 检查是否是合法域名
      if (isLegalDomain(imageUrl)) {
        // 合法域名：如果是HTTP，转换为HTTPS
        if (imageUrl.startsWith('http://')) {
          var httpsUrl = imageUrl.replace(/^http:/, 'https:');
          // 移除HTTPS转换日志以提升性能
          return httpsUrl;
        }
        // 已经是HTTPS，直接返回
        // 移除HTTPS返回日志以提升性能
        return imageUrl;
      }

      // 使用非捕获组来正确匹配IP和端口
      var internalIpMatch = imageUrl.match(/https?:\/\/((?:10\.\d+\.\d+\.\d+|172\.(?:1[6-9]|2[0-9]|3[01])\.\d+\.\d+|192\.168\.\d+\.\d+|localhost|127\.0\.0\.1))(?::(\d+))?/);
      if (internalIpMatch) {
        var host = internalIpMatch[1]; // 主机名或IP地址
        var _port = internalIpMatch[2] || '8080'; // 端口号，默认8080

        // 开发环境（内网IP）：保持HTTP不变，需要配置"不校验合法域名"
        // 生产环境：转换为HTTPS
        var _isDevEnv = /^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(host) || host === 'localhost' || host === '127.0.0.1';
        if (_isDevEnv) {
          // 开发者工具中微信基础库不再支持 HTTP 图片，必须用 HTTPS，否则报「不再支持 HTTP 协议」且 ERR_PROXY_CONNECTION_FAILED
          if (!isReal && _port === '8080' && imageUrl.startsWith('http://')) {
            var _httpsUrl = imageUrl.replace(/^http:\/\/([^\/]+)/, function (_, hostPort) {
              return 'https://' + hostPort.replace(':8080', ':8443');
            });
            return _httpsUrl;
          }
          if (_port === '8443' && imageUrl.startsWith('http://')) {
            var _httpUrl2 = imageUrl.replace(/http:\/\/[^\/]+/, "http://".concat(host, ":8080"));
            return _httpUrl2;
          } else if (_port === '8080' && imageUrl.startsWith('http://')) {
            return imageUrl;
          } else if (imageUrl.startsWith('https://')) {
            var _httpUrl3 = imageUrl.replace(/https:\/\/[^\/]+/, "http://".concat(host, ":8080"));
            return _httpUrl3;
          } else {
            var _httpUrl4 = imageUrl.replace(/http:\/\/[^\/]+/, "http://".concat(host, ":8080"));
            return _httpUrl4;
          }
        } else {
          // 生产环境：转换为HTTPS
          var httpsPort = _port === '8080' ? '8443' : _port === '80' ? '443' : _port;
          var _httpsUrl2 = imageUrl.replace(/http:\/\/[^\/]+/, "https://".concat(host, ":").concat(httpsPort));
          // 移除HTTP转HTTPS日志以提升性能
          return _httpsUrl2;
        }
      }

      // 对于其他HTTP地址（非内网）
      // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
      // 生产环境：转换为HTTPS
      var isDevEnv = imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1') || /https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(imageUrl);
      if (isDevEnv) {
        // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
        // 无论是开发者工具还是真机，开发环境都使用HTTP
        // 移除非内网HTTP地址日志以提升性能
        return imageUrl;
      } else {
        // 生产环境：转换为HTTPS
        var _httpsUrl3 = replaceToHttps(imageUrl);
        if (_httpsUrl3 !== imageUrl) {
          // 移除HTTP转HTTPS日志以提升性能
          return _httpsUrl3;
        }
        // 如果无法转换，至少尝试将协议改为HTTPS
        var fallbackHttps = imageUrl.replace(/^http:/, 'https:');
        return fallbackHttps;
      }
    }
  }

  // 检查是否是微信小程序的临时文件标识（通常是短字符串，不包含路径分隔符）
  // 这些标识符通常不包含 /、\、:、. 等路径字符
  if (imageUrl.length < 50 && !imageUrl.includes('/') && !imageUrl.includes('\\') && !imageUrl.includes(':') && !imageUrl.includes('.')) {
    return defaultImage;
  }

  // 其他情况，使用默认图片
  return defaultImage;
}

/**
 * 格式化价格，保留两位小数
 */
function formatPrice(price) {
  if (typeof price !== 'number') {
    price = parseFloat(price) || 0;
  }
  return '¥' + price.toFixed(2);
}

/**
 * 解析日期字符串为Date对象（iOS兼容）
 * 将 "2025-10-20 22:53:23" 格式转换为 "2025-10-20T22:53:23"
 */
function parseDate(dateString) {
  if (!dateString) return null;
  if (dateString instanceof Date) {
    return dateString;
  }
  if (typeof dateString === 'string') {
    // 将空格替换为T，使其符合ISO 8601格式，兼容iOS
    var isoDateString = dateString.replace(/ /g, 'T');
    return new Date(isoDateString);
  }
  return null;
}

/**
 * 格式化日期
 */
function formatDate(date) {
  var format = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'YYYY-MM-DD';
  if (!date) return '';
  if (typeof date === 'string') {
    date = parseDate(date);
  }
  if (!(date instanceof Date) || isNaN(date)) {
    return '';
  }
  var year = date.getFullYear();
  var month = date.getMonth() + 1;
  var day = date.getDate();
  var hour = date.getHours();
  var minute = date.getMinutes();
  var second = date.getSeconds();
  var formatMap = {
    'YYYY': year,
    'MM': formatNumber(month),
    'DD': formatNumber(day),
    'HH': formatNumber(hour),
    'mm': formatNumber(minute),
    'ss': formatNumber(second)
  };
  return format.replace(/YYYY|MM|DD|HH|mm|ss/g, function (matched) {
    return formatMap[matched];
  });
}

/**
 * 格式化数字，不足两位补零
 */
function formatNumber(n) {
  n = n.toString();
  return n[1] ? n : '0' + n;
}

/**
 * 计算时间差
 */
function timeAgo(date) {
  if (typeof date === 'string') {
    date = parseDate(date);
  }
  var now = new Date();
  var diff = now.getTime() - date.getTime();
  var minute = 60 * 1000;
  var hour = 60 * minute;
  var day = 24 * hour;
  var week = 7 * day;
  var month = 30 * day;
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  } else if (diff < week) {
    return Math.floor(diff / day) + '天前';
  } else if (diff < month) {
    return Math.floor(diff / week) + '周前';
  } else {
    return formatDate(date, 'YYYY-MM-DD');
  }
}

/**
 * 微信式时间戳格式化
 * 根据消息发送时间相对于当前时间的跨度，采用不同的显示格式
 * @param {Date|string|number} timestamp - 消息时间戳
 * @returns {string} 格式化后的时间字符串
 */
function formatWeChatTimestamp(timestamp) {
  if (!timestamp) return '';
  var date = null;
  if (timestamp instanceof Date) {
    date = timestamp;
  } else if (typeof timestamp === 'string') {
    date = parseDate(timestamp);
  } else if (typeof timestamp === 'number') {
    date = new Date(timestamp);
  } else {
    return '';
  }
  if (!date || isNaN(date.getTime())) {
    return '';
  }
  var now = new Date();
  var diff = now.getTime() - date.getTime();
  var minute = 60 * 1000;
  var hour = 60 * minute;
  var dayMs = 24 * hour;
  var weekMs = 7 * dayMs;

  // 星期数组
  var weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

  // 1. 当天消息（0-24小时内）：显示 "时:分"
  if (diff < dayMs) {
    var _hours = formatNumber(date.getHours());
    var _minutes = formatNumber(date.getMinutes());
    return "".concat(_hours, ":").concat(_minutes);
  }

  // 2. 超过1天但小于1周（1-7天）：显示 "星期几 时:分"
  if (diff < weekMs) {
    var weekday = weekdays[date.getDay()];
    var _hours2 = formatNumber(date.getHours());
    var _minutes2 = formatNumber(date.getMinutes());
    return "".concat(weekday, " ").concat(_hours2, ":").concat(_minutes2);
  }

  // 3. 超过1周（7天以上）：显示 "年月日 时:分"
  var year = date.getFullYear();
  var month = formatNumber(date.getMonth() + 1);
  var day = formatNumber(date.getDate());
  var hours = formatNumber(date.getHours());
  var minutes = formatNumber(date.getMinutes());
  return "".concat(year, "\u5E74").concat(month, "\u6708").concat(day, "\u65E5 ").concat(hours, ":").concat(minutes);
}

/**
 * 判断是否应该显示时间戳（基于5分钟规则）
 * @param {Date|string|number} currentTimestamp - 当前消息的时间戳
 * @param {Date|string|number} previousTimestamp - 上一条消息的时间戳（如果有）
 * @returns {boolean} 是否应该显示时间戳
 */
function shouldShowTimestamp(currentTimestamp, previousTimestamp) {
  if (!currentTimestamp) return false;

  // 如果没有上一条消息，总是显示时间戳
  if (!previousTimestamp) return true;
  var currentDate = null;
  var previousDate = null;

  // 解析当前消息时间
  if (currentTimestamp instanceof Date) {
    currentDate = currentTimestamp;
  } else if (typeof currentTimestamp === 'string') {
    currentDate = parseDate(currentTimestamp);
  } else if (typeof currentTimestamp === 'number') {
    currentDate = new Date(currentTimestamp);
  } else {
    return true; // 无法解析，显示时间戳
  }

  // 解析上一条消息时间
  if (previousTimestamp instanceof Date) {
    previousDate = previousTimestamp;
  } else if (typeof previousTimestamp === 'string') {
    previousDate = parseDate(previousTimestamp);
  } else if (typeof previousTimestamp === 'number') {
    previousDate = new Date(previousTimestamp);
  } else {
    return true; // 无法解析上一条，显示时间戳
  }

  if (!currentDate || isNaN(currentDate.getTime()) || !previousDate || isNaN(previousDate.getTime())) {
    return true;
  }

  // 计算时间差（毫秒）
  var diff = currentDate.getTime() - previousDate.getTime();

  // 5分钟 = 5 * 60 * 1000 毫秒
  var fiveMinutes = 5 * 60 * 1000;

  // 如果间隔超过5分钟，显示时间戳
  return diff > fiveMinutes;
}

/**
 * 防抖函数
 */
function debounce(func, wait, immediate) {
  var timeout;
  return function executedFunction() {
    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }
    var later = function later() {
      timeout = null;
      if (!immediate) func.apply(void 0, args);
    };
    var callNow = immediate && !timeout;
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
    if (callNow) func.apply(void 0, args);
  };
}

/**
 * 节流函数
 */
function throttle(func, wait) {
  var timeout;
  return function executedFunction() {
    for (var _len2 = arguments.length, args = new Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
      args[_key2] = arguments[_key2];
    }
    if (!timeout) {
      timeout = setTimeout(function () {
        timeout = null;
        func.apply(void 0, args);
      }, wait);
    }
  };
}

/**
 * 深拷贝
 */
function deepClone(obj) {
  if (obj === null || (0, _typeof2.default)(obj) !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime());
  if (obj instanceof Array) return obj.map(deepClone);
  if ((0, _typeof2.default)(obj) === 'object') {
    var clonedObj = {};
    Object.keys(obj).forEach(function (key) {
      clonedObj[key] = deepClone(obj[key]);
    });
    return clonedObj;
  }
}

/**
 * 生成随机ID
 */
function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

/**
 * 校验手机号
 */
function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone);
}

/**
 * 校验身份证号
 */
function isValidIdCard(idCard) {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard);
}

// Loading 状态管理
var loadingCount = 0;

/**
 * 显示加载提示
 */
function showLoading() {
  var title = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '加载中...';
  loadingCount++;
  uni.showLoading({
    title: title,
    mask: true
  });
}

/**
 * 隐藏加载提示
 */
function hideLoading() {
  loadingCount--;
  if (loadingCount <= 0) {
    loadingCount = 0;
    uni.hideLoading();
  }
}

/**
 * 获取当前 loading 状态
 */
function getLoadingCount() {
  return loadingCount;
}

/**
 * 重置 loading 状态（用于紧急情况）
 */
function resetLoading() {
  loadingCount = 0;
  uni.hideLoading();
}

/**
 * 显示提示消息
 */
function showToast(title) {
  var icon = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'none';
  var duration = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 2000;
  uni.showToast({
    title: title,
    icon: icon,
    duration: duration
  });
}

/**
 * 显示模态框
 */
function showModal(title, content) {
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  return new Promise(function (resolve, reject) {
    uni.showModal(_objectSpread(_objectSpread({
      title: title,
      content: content
    }, options), {}, {
      success: resolve,
      fail: reject
    }));
  });
}

/**
 * 跳转页面
 */
function navigateTo(url) {
  uni.navigateTo({
    url: url,
    fail: function fail() {
      showToast('页面跳转失败');
    }
  });
}

/**
 * 跳转到tabBar页面
 */
function switchTab(url) {
  uni.switchTab({
    url: url,
    fail: function fail() {
      showToast('页面跳转失败');
    }
  });
}

/**
 * 返回上一页（带页面栈检查，减轻「routeDone with a webviewId is not found」等 DevTools/竞态报错）
 */
function navigateBack() {
  var delta = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
  try {
    var pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    var n = pages && pages.length || 0;
    if (n <= delta) {
      if (typeof uni !== 'undefined' && uni.switchTab) {
        uni.switchTab({
          url: '/pages/main/index',
          fail: function fail() {}
        });
      }
      return;
    }
    uni.navigateBack({
      delta: delta,
      fail: function fail() {
        try {
          if (typeof uni !== 'undefined' && uni.switchTab) {
            uni.switchTab({
              url: '/pages/main/index',
              fail: function fail() {}
            });
          }
        } catch (e) {}
      }
    });
  } catch (e) {
    try {
      if (typeof uni !== 'undefined' && uni.switchTab) {
        uni.switchTab({
          url: '/pages/main/index',
          fail: function fail() {}
        });
      }
    } catch (e2) {}
  }
}

/** 工作人员跳转「我的」时避免与其它 tab 的 onShow 重复 switchTab，减轻 routeDone/webviewId 类报错 */
var staffSwitchToMineLock = false;

/**
 * 工作人员账号仅允许使用「我的」：在其它 tab 页的 onShow 首行调用。
 * 使用异步 switchTab，避免与当前 webview 卸载竞态。
 * @returns {boolean} true 表示已拦截（调用方应 return），false 表示非工作人员可继续 onShow
 */
function redirectStaffToMineIfNeeded() {
  try {
    if (typeof uni === 'undefined' || !uni.getStorageSync('staffToken')) return false;
    if (staffSwitchToMineLock) return true;
    staffSwitchToMineLock = true;
    setTimeout(function () {
      uni.switchTab({
        url: '/pages/main/index',
        complete: function complete() {
          staffSwitchToMineLock = false;
        },
        fail: function fail() {
          staffSwitchToMineLock = false;
        }
      });
      uni.showToast({
        title: '工作人员只能使用「我的」页面',
        icon: 'none'
      });
    }, 0);
    return true;
  } catch (e) {
    staffSwitchToMineLock = false;
    return false;
  }
}

/**
 * 预览图片
 */
function previewImage(urls) {
  var current = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';
  uni.previewImage({
    urls: urls,
    current: current,
    fail: function fail() {
      showToast('预览图片失败');
    }
  });
}

/**
 * 选择图片（默认优先压缩图，减轻上传与加载）
 */
function chooseImage() {
  var count = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
  var sizeType = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : ['compressed'];
  var sourceType = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : ['album', 'camera'];
  return new Promise(function (resolve, reject) {
    uni.chooseImage({
      count: count,
      sizeType: sizeType,
      sourceType: sourceType,
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 获取位置信息
 */
function getLocation() {
  var type = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'wgs84';
  return new Promise(function (resolve, reject) {
    uni.getLocation({
      type: type,
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 打电话
 */
function makePhoneCall(phoneNumber) {
  uni.makePhoneCall({
    phoneNumber: phoneNumber,
    fail: function fail() {
      showToast('拨打电话失败');
    }
  });
}

/**
 * 设置剪贴板内容
 */
function setClipboardData(data) {
  uni.setClipboardData({
    data: data,
    success: function success() {
      showToast('已复制到剪贴板', 'success');
    },
    fail: function fail() {
      showToast('复制失败');
    }
  });
}

/**
 * 获取剪贴板内容
 */
function getClipboardData() {
  return new Promise(function (resolve, reject) {
    uni.getClipboardData({
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 获取存储空间信息
 */
function getStorageInfo() {
  return new Promise(function (resolve, reject) {
    uni.getStorageInfo({
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 清空存储空间
 */
function clearStorage() {
  return new Promise(function (resolve, reject) {
    uni.clearStorage({
      success: resolve,
      fail: reject
    });
  });
}

/** 上传前压缩：与主流社交应用相当，质量 78、长边最大 1280，加载更快 */
var UPLOAD_COMPRESS_QUALITY = 78;
var UPLOAD_COMPRESS_MAX = 1280;
function compressImageForUpload(src) {
  return new Promise(function (resolve) {
    if (!src || typeof uni.compressImage !== 'function') {
      resolve(src);
      return;
    }
    uni.compressImage({
      src: src,
      quality: UPLOAD_COMPRESS_QUALITY,
      compressedWidth: UPLOAD_COMPRESS_MAX,
      compressedHeight: UPLOAD_COMPRESS_MAX,
      success: function success(res) {
        resolve(res.tempFilePath || src);
      },
      fail: function fail() {
        resolve(src);
      }
    });
  });
}

/**
 * 上传图片（先压缩再上传，用户/管理员上传的图会自动变小）
 */
function uploadImage(filePath) {
  var token = uni.getStorageSync('token');
  var apiBase = getApiBaseUrl();
  var uploadBase = typeof apiBase === 'string' && apiBase.startsWith('https://') ? getHttpsBase() : getHttpBase();
  var uploadUrl = "".concat(uploadBase, "/api/upload/image");
  var doUpload = function doUpload(path) {
    return new Promise(function (resolve, reject) {
      uni.uploadFile({
        url: uploadUrl,
        filePath: path,
        name: 'file',
        header: {
          'Authorization': "Bearer ".concat(token)
        },
        success: function success(res) {
          if (res.statusCode === 200) {
            try {
              var data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
              if (data.code === 0 || data.code === 200) {
                resolve(data.data);
              } else {
                reject(new Error(data.msg || '上传失败'));
              }
            } catch (e) {
              reject(new Error('上传失败'));
            }
          } else {
            reject(new Error('上传失败'));
          }
        },
        fail: function fail(err) {
          return reject(err);
        }
      });
    });
  };
  return compressImageForUpload(filePath).then(doUpload);
}
var util = {
  getImageUrl: getImageUrl,
  formatPrice: formatPrice,
  parseDate: parseDate,
  formatDate: formatDate,
  formatNumber: formatNumber,
  timeAgo: timeAgo,
  formatWeChatTimestamp: formatWeChatTimestamp,
  shouldShowTimestamp: shouldShowTimestamp,
  debounce: debounce,
  throttle: throttle,
  deepClone: deepClone,
  generateId: generateId,
  isValidPhone: isValidPhone,
  isValidIdCard: isValidIdCard,
  showLoading: showLoading,
  hideLoading: hideLoading,
  getLoadingCount: getLoadingCount,
  resetLoading: resetLoading,
  showToast: showToast,
  showModal: showModal,
  navigateTo: navigateTo,
  switchTab: switchTab,
  navigateBack: navigateBack,
  redirectStaffToMineIfNeeded: redirectStaffToMineIfNeeded,
  previewImage: previewImage,
  chooseImage: chooseImage,
  getLocation: getLocation,
  makePhoneCall: makePhoneCall,
  setClipboardData: setClipboardData,
  getClipboardData: getClipboardData,
  getStorageInfo: getStorageInfo,
  clearStorage: clearStorage,
  uploadImage: uploadImage,
  compressImageForUpload: compressImageForUpload,
  getApiBaseUrl: getApiBaseUrl,
  setApiBaseUrl: setApiBaseUrl
};
exports.util = util;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 5:
/*!**************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/slicedToArray.js ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var arrayWithHoles = __webpack_require__(/*! ./arrayWithHoles.js */ 6);
var iterableToArrayLimit = __webpack_require__(/*! ./iterableToArrayLimit.js */ 7);
var unsupportedIterableToArray = __webpack_require__(/*! ./unsupportedIterableToArray.js */ 8);
var nonIterableRest = __webpack_require__(/*! ./nonIterableRest.js */ 10);
function _slicedToArray(arr, i) {
  return arrayWithHoles(arr) || iterableToArrayLimit(arr, i) || unsupportedIterableToArray(arr, i) || nonIterableRest();
}
module.exports = _slicedToArray, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 501:
/*!*****************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/common/js/api/address.js ***!
  \*****************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/defineProperty */ 11));
var _typeof2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/typeof */ 13));
var _api = __webpack_require__(/*! ../api.js */ 38);
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2.default)(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
/** 与收藏/预约页一致：优先独立 userId，再从 userInfo 取 id/uid，避免只登录未写 userId 时列表永远失败 */
function resolveStoredUserId() {
  try {
    var raw = uni.getStorageSync('userId');
    if (raw != null && raw !== '' && String(raw) !== 'undefined') {
      var n = Number(raw);
      if (Number.isFinite(n) && n > 0) return n;
    }
    var u = uni.getStorageSync('userInfo');
    if (u && (0, _typeof2.default)(u) === 'object') {
      var id = u.id != null ? u.id : u.uid;
      if (id != null && id !== '' && String(id) !== 'undefined') {
        var _n = Number(id);
        if (Number.isFinite(_n) && _n > 0) return _n;
      }
    }
  } catch (e) {}
  return null;
}
var addressApi = {
  // 获取当前用户所有地址（showLoading 为 false 时不显示全局加载中，用于 onShow 静默刷新）
  getAddressList: function getAddressList() {
    var showLoading = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : true;
    var token = uni.getStorageSync('token');
    var uid = resolveStoredUserId();
    // 有 token 时务必请求后端：由 JWT 解析用户；query userId 仅作兜底（与 AddressController 一致）
    if (!token && uid == null) {
      return Promise.resolve({
        code: 200,
        data: [],
        msg: '未登录'
      });
    }
    var data = uid != null ? {
      userId: uid
    } : {};
    return _api.api.request({
      url: '/api/addresses/list',
      method: 'GET',
      data: data,
      showLoading: showLoading
    });
  },
  // 创建地址
  createAddress: function createAddress(addressData) {
    // 后端优先从 token 解析 userId，解析不到时使用 body 中的 userId
    var validUserId = resolveStoredUserId();
    var requestData = {
      name: addressData.name,
      phone: addressData.phone,
      province: addressData.province,
      city: addressData.city,
      district: addressData.district,
      detail: addressData.detail,
      isDefault: addressData.isDefault || false
    };
    if (validUserId != null) {
      requestData.userId = validUserId;
    }

    // 同时传 query 参数 userId 作为兜底（token 解析失败时后端可回退使用）
    var url = validUserId != null ? "/api/addresses/create?userId=".concat(validUserId) : '/api/addresses/create';
    return _api.api.request({
      url: url,
      method: 'POST',
      data: requestData
    });
  },
  // 更新地址
  updateAddress: function updateAddress(addressData) {
    var validUserId = resolveStoredUserId();
    var payload = _objectSpread({}, addressData);
    if (validUserId != null) {
      payload.userId = validUserId;
    }
    return _api.api.request({
      url: '/api/addresses/update',
      method: 'PUT',
      data: payload
    });
  },
  // 删除地址（页面已自行 showLoading「删除中」，此处关闭全局 loading 避免双 loading / hide 竞态）
  deleteAddress: function deleteAddress(addressId) {
    var validUserId = resolveStoredUserId();
    var url = validUserId != null ? "/api/addresses/".concat(addressId, "?userId=").concat(validUserId) : "/api/addresses/".concat(addressId);
    return _api.api.request({
      url: url,
      method: 'DELETE',
      showLoading: false
    });
  },
  // 设置默认地址
  setDefaultAddress: function setDefaultAddress(addressId) {
    return _api.api.request({
      url: "/api/addresses/set-default/".concat(addressId),
      method: 'PUT'
    });
  },
  // 获取默认地址（showLoading 为 false 时不显示全局加载中，用于 onShow 静默刷新）
  getDefaultAddress: function getDefaultAddress(userId) {
    var showLoading = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
    // 该接口后端会：优先从token解析userId；解析不到时才使用参数 userId
    // 但前端很多地方会传 null/undefined，导致 userId 参数为空，从而触发“未登录或用户ID无效”
    var finalUserId = userId;
    if (finalUserId === undefined || finalUserId === null || finalUserId === '' || finalUserId === 'undefined') {
      var resolved = resolveStoredUserId();
      finalUserId = resolved != null ? resolved : null;
    } else {
      var n = Number(finalUserId);
      finalUserId = Number.isFinite(n) && n > 0 ? n : null;
    }

    // 未登录则直接返回空地址；调用方已有兜底逻辑（不会影响下单）
    if (!finalUserId) {
      return Promise.resolve({
        code: 200,
        data: null,
        msg: '未登录，跳过获取默认地址'
      });
    }
    return _api.api.request({
      url: '/api/addresses/default',
      method: 'GET',
      data: {
        userId: finalUserId
      },
      showLoading: showLoading
    });
  }
};
var _default = addressApi;
exports.default = _default;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 6:
/*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayWithHoles.js ***!
  \***************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _arrayWithHoles(arr) {
  if (Array.isArray(arr)) return arr;
}
module.exports = _arrayWithHoles, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 64:
/*!************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/@babel/runtime/regenerator/index.js ***!
  \************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// TODO(Babel 8): Remove this file.

var runtime = __webpack_require__(/*! @babel/runtime/helpers/regeneratorRuntime */ 65)();
module.exports = runtime;

/***/ }),

/***/ 65:
/*!*******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/regeneratorRuntime.js ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var _typeof = __webpack_require__(/*! ./typeof.js */ 13)["default"];
function _regeneratorRuntime() {
  "use strict";

  /*! regenerator-runtime -- Copyright (c) 2014-present, Facebook, Inc. -- license (MIT): https://github.com/facebook/regenerator/blob/main/LICENSE */
  module.exports = _regeneratorRuntime = function _regeneratorRuntime() {
    return e;
  }, module.exports.__esModule = true, module.exports["default"] = module.exports;
  var t,
    e = {},
    r = Object.prototype,
    n = r.hasOwnProperty,
    o = Object.defineProperty || function (t, e, r) {
      t[e] = r.value;
    },
    i = "function" == typeof Symbol ? Symbol : {},
    a = i.iterator || "@@iterator",
    c = i.asyncIterator || "@@asyncIterator",
    u = i.toStringTag || "@@toStringTag";
  function define(t, e, r) {
    return Object.defineProperty(t, e, {
      value: r,
      enumerable: !0,
      configurable: !0,
      writable: !0
    }), t[e];
  }
  try {
    define({}, "");
  } catch (t) {
    define = function define(t, e, r) {
      return t[e] = r;
    };
  }
  function wrap(t, e, r, n) {
    var i = e && e.prototype instanceof Generator ? e : Generator,
      a = Object.create(i.prototype),
      c = new Context(n || []);
    return o(a, "_invoke", {
      value: makeInvokeMethod(t, r, c)
    }), a;
  }
  function tryCatch(t, e, r) {
    try {
      return {
        type: "normal",
        arg: t.call(e, r)
      };
    } catch (t) {
      return {
        type: "throw",
        arg: t
      };
    }
  }
  e.wrap = wrap;
  var h = "suspendedStart",
    l = "suspendedYield",
    f = "executing",
    s = "completed",
    y = {};
  function Generator() {}
  function GeneratorFunction() {}
  function GeneratorFunctionPrototype() {}
  var p = {};
  define(p, a, function () {
    return this;
  });
  var d = Object.getPrototypeOf,
    v = d && d(d(values([])));
  v && v !== r && n.call(v, a) && (p = v);
  var g = GeneratorFunctionPrototype.prototype = Generator.prototype = Object.create(p);
  function defineIteratorMethods(t) {
    ["next", "throw", "return"].forEach(function (e) {
      define(t, e, function (t) {
        return this._invoke(e, t);
      });
    });
  }
  function AsyncIterator(t, e) {
    function invoke(r, o, i, a) {
      var c = tryCatch(t[r], t, o);
      if ("throw" !== c.type) {
        var u = c.arg,
          h = u.value;
        return h && "object" == _typeof(h) && n.call(h, "__await") ? e.resolve(h.__await).then(function (t) {
          invoke("next", t, i, a);
        }, function (t) {
          invoke("throw", t, i, a);
        }) : e.resolve(h).then(function (t) {
          u.value = t, i(u);
        }, function (t) {
          return invoke("throw", t, i, a);
        });
      }
      a(c.arg);
    }
    var r;
    o(this, "_invoke", {
      value: function value(t, n) {
        function callInvokeWithMethodAndArg() {
          return new e(function (e, r) {
            invoke(t, n, e, r);
          });
        }
        return r = r ? r.then(callInvokeWithMethodAndArg, callInvokeWithMethodAndArg) : callInvokeWithMethodAndArg();
      }
    });
  }
  function makeInvokeMethod(e, r, n) {
    var o = h;
    return function (i, a) {
      if (o === f) throw Error("Generator is already running");
      if (o === s) {
        if ("throw" === i) throw a;
        return {
          value: t,
          done: !0
        };
      }
      for (n.method = i, n.arg = a;;) {
        var c = n.delegate;
        if (c) {
          var u = maybeInvokeDelegate(c, n);
          if (u) {
            if (u === y) continue;
            return u;
          }
        }
        if ("next" === n.method) n.sent = n._sent = n.arg;else if ("throw" === n.method) {
          if (o === h) throw o = s, n.arg;
          n.dispatchException(n.arg);
        } else "return" === n.method && n.abrupt("return", n.arg);
        o = f;
        var p = tryCatch(e, r, n);
        if ("normal" === p.type) {
          if (o = n.done ? s : l, p.arg === y) continue;
          return {
            value: p.arg,
            done: n.done
          };
        }
        "throw" === p.type && (o = s, n.method = "throw", n.arg = p.arg);
      }
    };
  }
  function maybeInvokeDelegate(e, r) {
    var n = r.method,
      o = e.iterator[n];
    if (o === t) return r.delegate = null, "throw" === n && e.iterator["return"] && (r.method = "return", r.arg = t, maybeInvokeDelegate(e, r), "throw" === r.method) || "return" !== n && (r.method = "throw", r.arg = new TypeError("The iterator does not provide a '" + n + "' method")), y;
    var i = tryCatch(o, e.iterator, r.arg);
    if ("throw" === i.type) return r.method = "throw", r.arg = i.arg, r.delegate = null, y;
    var a = i.arg;
    return a ? a.done ? (r[e.resultName] = a.value, r.next = e.nextLoc, "return" !== r.method && (r.method = "next", r.arg = t), r.delegate = null, y) : a : (r.method = "throw", r.arg = new TypeError("iterator result is not an object"), r.delegate = null, y);
  }
  function pushTryEntry(t) {
    var e = {
      tryLoc: t[0]
    };
    1 in t && (e.catchLoc = t[1]), 2 in t && (e.finallyLoc = t[2], e.afterLoc = t[3]), this.tryEntries.push(e);
  }
  function resetTryEntry(t) {
    var e = t.completion || {};
    e.type = "normal", delete e.arg, t.completion = e;
  }
  function Context(t) {
    this.tryEntries = [{
      tryLoc: "root"
    }], t.forEach(pushTryEntry, this), this.reset(!0);
  }
  function values(e) {
    if (e || "" === e) {
      var r = e[a];
      if (r) return r.call(e);
      if ("function" == typeof e.next) return e;
      if (!isNaN(e.length)) {
        var o = -1,
          i = function next() {
            for (; ++o < e.length;) {
              if (n.call(e, o)) return next.value = e[o], next.done = !1, next;
            }
            return next.value = t, next.done = !0, next;
          };
        return i.next = i;
      }
    }
    throw new TypeError(_typeof(e) + " is not iterable");
  }
  return GeneratorFunction.prototype = GeneratorFunctionPrototype, o(g, "constructor", {
    value: GeneratorFunctionPrototype,
    configurable: !0
  }), o(GeneratorFunctionPrototype, "constructor", {
    value: GeneratorFunction,
    configurable: !0
  }), GeneratorFunction.displayName = define(GeneratorFunctionPrototype, u, "GeneratorFunction"), e.isGeneratorFunction = function (t) {
    var e = "function" == typeof t && t.constructor;
    return !!e && (e === GeneratorFunction || "GeneratorFunction" === (e.displayName || e.name));
  }, e.mark = function (t) {
    return Object.setPrototypeOf ? Object.setPrototypeOf(t, GeneratorFunctionPrototype) : (t.__proto__ = GeneratorFunctionPrototype, define(t, u, "GeneratorFunction")), t.prototype = Object.create(g), t;
  }, e.awrap = function (t) {
    return {
      __await: t
    };
  }, defineIteratorMethods(AsyncIterator.prototype), define(AsyncIterator.prototype, c, function () {
    return this;
  }), e.AsyncIterator = AsyncIterator, e.async = function (t, r, n, o, i) {
    void 0 === i && (i = Promise);
    var a = new AsyncIterator(wrap(t, r, n, o), i);
    return e.isGeneratorFunction(r) ? a : a.next().then(function (t) {
      return t.done ? t.value : a.next();
    });
  }, defineIteratorMethods(g), define(g, u, "Generator"), define(g, a, function () {
    return this;
  }), define(g, "toString", function () {
    return "[object Generator]";
  }), e.keys = function (t) {
    var e = Object(t),
      r = [];
    for (var n in e) {
      r.push(n);
    }
    return r.reverse(), function next() {
      for (; r.length;) {
        var t = r.pop();
        if (t in e) return next.value = t, next.done = !1, next;
      }
      return next.done = !0, next;
    };
  }, e.values = values, Context.prototype = {
    constructor: Context,
    reset: function reset(e) {
      if (this.prev = 0, this.next = 0, this.sent = this._sent = t, this.done = !1, this.delegate = null, this.method = "next", this.arg = t, this.tryEntries.forEach(resetTryEntry), !e) for (var r in this) {
        "t" === r.charAt(0) && n.call(this, r) && !isNaN(+r.slice(1)) && (this[r] = t);
      }
    },
    stop: function stop() {
      this.done = !0;
      var t = this.tryEntries[0].completion;
      if ("throw" === t.type) throw t.arg;
      return this.rval;
    },
    dispatchException: function dispatchException(e) {
      if (this.done) throw e;
      var r = this;
      function handle(n, o) {
        return a.type = "throw", a.arg = e, r.next = n, o && (r.method = "next", r.arg = t), !!o;
      }
      for (var o = this.tryEntries.length - 1; o >= 0; --o) {
        var i = this.tryEntries[o],
          a = i.completion;
        if ("root" === i.tryLoc) return handle("end");
        if (i.tryLoc <= this.prev) {
          var c = n.call(i, "catchLoc"),
            u = n.call(i, "finallyLoc");
          if (c && u) {
            if (this.prev < i.catchLoc) return handle(i.catchLoc, !0);
            if (this.prev < i.finallyLoc) return handle(i.finallyLoc);
          } else if (c) {
            if (this.prev < i.catchLoc) return handle(i.catchLoc, !0);
          } else {
            if (!u) throw Error("try statement without catch or finally");
            if (this.prev < i.finallyLoc) return handle(i.finallyLoc);
          }
        }
      }
    },
    abrupt: function abrupt(t, e) {
      for (var r = this.tryEntries.length - 1; r >= 0; --r) {
        var o = this.tryEntries[r];
        if (o.tryLoc <= this.prev && n.call(o, "finallyLoc") && this.prev < o.finallyLoc) {
          var i = o;
          break;
        }
      }
      i && ("break" === t || "continue" === t) && i.tryLoc <= e && e <= i.finallyLoc && (i = null);
      var a = i ? i.completion : {};
      return a.type = t, a.arg = e, i ? (this.method = "next", this.next = i.finallyLoc, y) : this.complete(a);
    },
    complete: function complete(t, e) {
      if ("throw" === t.type) throw t.arg;
      return "break" === t.type || "continue" === t.type ? this.next = t.arg : "return" === t.type ? (this.rval = this.arg = t.arg, this.method = "return", this.next = "end") : "normal" === t.type && e && (this.next = e), y;
    },
    finish: function finish(t) {
      for (var e = this.tryEntries.length - 1; e >= 0; --e) {
        var r = this.tryEntries[e];
        if (r.finallyLoc === t) return this.complete(r.completion, r.afterLoc), resetTryEntry(r), y;
      }
    },
    "catch": function _catch(t) {
      for (var e = this.tryEntries.length - 1; e >= 0; --e) {
        var r = this.tryEntries[e];
        if (r.tryLoc === t) {
          var n = r.completion;
          if ("throw" === n.type) {
            var o = n.arg;
            resetTryEntry(r);
          }
          return o;
        }
      }
      throw Error("illegal catch attempt");
    },
    delegateYield: function delegateYield(e, r, n) {
      return this.delegate = {
        iterator: values(e),
        resultName: r,
        nextLoc: n
      }, "next" === this.method && (this.arg = t), y;
    }
  }, e;
}
module.exports = _regeneratorRuntime, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 66:
/*!*****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/asyncToGenerator.js ***!
  \*****************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function asyncGeneratorStep(gen, resolve, reject, _next, _throw, key, arg) {
  try {
    var info = gen[key](arg);
    var value = info.value;
  } catch (error) {
    reject(error);
    return;
  }
  if (info.done) {
    resolve(value);
  } else {
    Promise.resolve(value).then(_next, _throw);
  }
}
function _asyncToGenerator(fn) {
  return function () {
    var self = this,
      args = arguments;
    return new Promise(function (resolve, reject) {
      var gen = fn.apply(self, args);
      function _next(value) {
        asyncGeneratorStep(gen, resolve, reject, _next, _throw, "next", value);
      }
      function _throw(err) {
        asyncGeneratorStep(gen, resolve, reject, _next, _throw, "throw", err);
      }
      _next(undefined);
    });
  };
}
module.exports = _asyncToGenerator, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 7:
/*!*********************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/iterableToArrayLimit.js ***!
  \*********************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _iterableToArrayLimit(r, l) {
  var t = null == r ? null : "undefined" != typeof Symbol && r[Symbol.iterator] || r["@@iterator"];
  if (null != t) {
    var e,
      n,
      i,
      u,
      a = [],
      f = !0,
      o = !1;
    try {
      if (i = (t = t.call(r)).next, 0 === l) {
        if (Object(t) !== t) return;
        f = !1;
      } else for (; !(f = (e = i.call(t)).done) && (a.push(e.value), a.length !== l); f = !0) {
        ;
      }
    } catch (r) {
      o = !0, n = r;
    } finally {
      try {
        if (!f && null != t["return"] && (u = t["return"](), Object(u) !== u)) return;
      } finally {
        if (o) throw n;
      }
    }
    return a;
  }
}
module.exports = _iterableToArrayLimit, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 8:
/*!***************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/unsupportedIterableToArray.js ***!
  \***************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var arrayLikeToArray = __webpack_require__(/*! ./arrayLikeToArray.js */ 9);
function _unsupportedIterableToArray(o, minLen) {
  if (!o) return;
  if (typeof o === "string") return arrayLikeToArray(o, minLen);
  var n = Object.prototype.toString.call(o).slice(8, -1);
  if (n === "Object" && o.constructor) n = o.constructor.name;
  if (n === "Map" || n === "Set") return Array.from(o);
  if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return arrayLikeToArray(o, minLen);
}
module.exports = _unsupportedIterableToArray, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ }),

/***/ 9:
/*!*****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayLikeToArray.js ***!
  \*****************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function _arrayLikeToArray(arr, len) {
  if (len == null || len > arr.length) len = arr.length;
  for (var i = 0, arr2 = new Array(len); i < len; i++) {
    arr2[i] = arr[i];
  }
  return arr2;
}
module.exports = _arrayLikeToArray, module.exports.__esModule = true, module.exports["default"] = module.exports;

/***/ })

}]);
//# sourceMappingURL=../../.sourcemap/mp-weixin/common/vendor.js.map
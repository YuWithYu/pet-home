// store/index.js
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
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
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      if (userInfo) {
        uni.setStorageSync('userInfo', userInfo)
      } else {
        uni.removeStorageSync('userInfo')
      }
    },

    // 设置token
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        uni.setStorageSync('token', token)
      } else {
        uni.removeStorageSync('token')
      }
    },

    // 设置购物车数量
    SET_CART_COUNT(state, count) {
      state.cartCount = count
    },

    // 设置购物车列表
    SET_CART_LIST(state, list) {
      state.cartList = list
    },

    // 设置系统配置
    SET_CONFIG(state, config) {
      state.config = config
    },

    // 设置位置信息
    SET_LOCATION(state, location) {
      state.location = location
    },

    // 清空用户信息
    CLEAR_USER(state) {
      state.userInfo = null
      state.token = null
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('token')
    },

    // 退出登录（别名）
    LOGOUT(state) {
      state.userInfo = null
      state.token = null
      state.cartCount = 0
      state.cartList = []
      state.postLikeCache = {}
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('token')
    },

    // 同步帖子点赞状态（详情页点赞/取消后写入，发现页 onShow 时合并到列表）
    SET_POST_LIKE(state, { postId, isLiked, likeCount }) {
      if (!postId) return
      const key = String(postId)
      state.postLikeCache[key] = { ...(state.postLikeCache[key] || {}), isLiked, likeCount }
    }
  },

  actions: {
    // 初始化用户信息
    initUserInfo({ commit }) {
      const userInfo = uni.getStorageSync('userInfo')
      const token = uni.getStorageSync('token')

      if (userInfo) {
        commit('SET_USER_INFO', userInfo)
      }

      if (token) {
        commit('SET_TOKEN', token)
      }
    },

    // 登录
    login({ commit }, { userInfo, token }) {
      commit('SET_USER_INFO', userInfo)
      commit('SET_TOKEN', token)
    },

    // 退出登录
    logout({ commit }) {
      try {
        // 清除用户相关数据
        commit('CLEAR_USER')
        
        // 清除购物车数据
        commit('SET_CART_COUNT', 0)
        commit('SET_CART_LIST', [])
        
        // 清除其他可能的本地存储
        uni.removeStorageSync('cartList')
        uni.removeStorageSync('cartCount')
        uni.removeStorageSync('location')
        uni.removeStorageSync('config')
        
      } catch (error) {
        console.error('清除用户数据失败:', error)
      }
    },

    // 更新购物车数量
    updateCartCount({ commit }, count) {
      commit('SET_CART_COUNT', count)
    },

    // 更新购物车列表
    updateCartList({ commit }, list) {
      commit('SET_CART_LIST', list)
    },

    // 更新系统配置
    updateConfig({ commit }, config) {
      commit('SET_CONFIG', config)
    },

    // 更新位置信息
    updateLocation({ commit }, location) {
      commit('SET_LOCATION', location)
    }
  },

  getters: {
    // 用户信息
    userInfo: state => state.userInfo,

    // 是否已登录
    isLoggedIn: state => !!state.token,
    
    // 是否已登录（别名，兼容旧代码）
    hasLogin: state => !!state.token,

    // 用户昵称
    userNickname: state => state.userInfo ? state.userInfo.nickname || state.userInfo.username : '',

    // 用户头像
    userAvatar: state => state.userInfo ? state.userInfo.avatar : '',

    // 购物车总数量
    totalCartCount: state => state.cartCount,

    // 购物车商品数量
    cartItemCount: state => state.cartList.length,

    // 帖子点赞缓存（供发现页合并）
    postLikeCache: state => state.postLikeCache
  }
})

export default store

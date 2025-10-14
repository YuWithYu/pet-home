// store/index.js
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    // 用户信息
    userInfo: null,
    token: null,

    // 购物车信息
    cartCount: 0,
    cartList: [],

    // 系统配置
    config: {},

    // 位置信息
    location: null
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
      commit('CLEAR_USER')
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

    // 用户昵称
    userNickname: state => state.userInfo ? state.userInfo.nickname || state.userInfo.username : '',

    // 用户头像
    userAvatar: state => state.userInfo ? state.userInfo.avatar : '',

    // 购物车总数量
    totalCartCount: state => state.cartCount,

    // 购物车商品数量
    cartItemCount: state => state.cartList.length
  }
})

export default store

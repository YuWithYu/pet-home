<template>
    <view class="container">
      <!-- Tab 切换 -->
      <view class="tab-bar">
        <view 
          v-for="(tab,index) in tabs" 
          :key="tab.key" 
          :class="['tab-item', {active: activeTab===tab.key}]" 
          @tap="switchTab(tab.key)">
          {{ tab.label }}
        </view>
      </view>
  
      <!-- 搜索框（只在关注和粉丝tab显示） -->
      <view v-if="activeTab !== 'posts'" class="search-box">
        <image class="search-icon" src="/static/images/search.svg" mode="aspectFit"/>
        <input 
          class="search-input" 
          type="text" 
          :placeholder="searchPlaceholder" 
          v-model="searchKeyword"
          @input="onSearchInput"
          @confirm="onSearchConfirm"
        />
      </view>
  
      <!-- Tab 内容 -->
      <view class="tab-content">
        <!-- 关注Tab -->
        <view v-if="activeTab==='following'" class="tab-panel">
          <!-- 关注列表 -->
          <scroll-view class="list" scroll-y="true" @scrolltolower="loadMore" :lower-threshold="50">
            <view v-if="filteredFollowingList && filteredFollowingList.length > 0">
              <view 
                v-for="(item,index) in filteredFollowingList" 
                :key="item.id || item.followingId || index"
                class="following-item">
                <image class="avatar" :src="getImageUrl(item.avatarUrl) || getImageUrl('/static/images/garfield-default-avatar.png')" @error="handleAvatarError" @tap="goToUserProfile(item.id || item.followingId || item.userId)"/>
                <view class="info">
                  <text class="username">{{ item.username }}</text>
                  <text class="fans-count">{{ item.fansCount || 0 }} 粉丝</text>
                </view>
                <button 
                  class="recommend-btn followed" 
                  @tap="toggleFollow(item.id || item.followingId || item.userId, index, 'following')">
                  已关注
                </button>
              </view>
            </view>
            
            <!-- 推荐用户 -->
            <view v-if="recommendedList.length > 0" class="recommend-section">
              <view class="recommend-divider">------为你推荐------</view>
              <view class="recommend-grid">
                <view 
                  v-for="(user,index) in recommendedList" 
                  :key="user.id"
                  class="recommend-item">
                  <image 
                    class="recommend-avatar" 
                    :src="getRecommendAvatarUrl(user)" 
                    mode="aspectFill"
                    @error="handleRecommendAvatarError" 
                    @tap="goToUserProfile(user.id)"/>
                  <text class="recommend-name">{{ user.username }}</text>
                  <button 
                    class="recommend-btn" 
                    :class="user.isFollowing ? 'followed':'unfollow'"
                    @tap="toggleFollow(user.id, -1, 'following')">
                    {{ user.isFollowing ? '已关注':'+关注' }}
                  </button>
                </view>
              </view>
              <view class="change-batch-btn" @tap="changeBatch">
                <image class="refresh-icon" src="/static/images/刷新.png" mode="aspectFit"/>
                <text class="btn-text">换一批</text>
              </view>
            </view>
            
            <!-- 空状态 -->
            <view v-if="(!filteredFollowingList || filteredFollowingList.length===0) && (!recommendedList || recommendedList.length===0) && !loadingMore.following" class="empty-state">
              <text class="empty-text">您还没有关注任何人~</text>
              <text class="empty-subtext">去发现更多有趣的人吧</text>
            </view>
          </scroll-view>
        </view>
        
        <!-- 粉丝Tab -->
        <view v-if="activeTab==='fans'" class="tab-panel">
          <scroll-view class="list" scroll-y="true" @scrolltolower="loadMore" :lower-threshold="50">
            <view v-if="filteredFansList && filteredFansList.length > 0">
              <view 
                v-for="(item,index) in filteredFansList" 
                :key="item.id || item.followerId || index"
                class="following-item">
                <image class="avatar" :src="getImageUrl(item.avatarUrl) || getImageUrl('/static/images/garfield-default-avatar.png')" @error="handleAvatarError" @tap="goToUserProfile(item.id || item.followerId || item.userId)"/>
                <view class="info">
                  <text class="username">{{ item.username }}</text>
                  <text class="fans-count">{{ item.fansCount || 0 }} 粉丝</text>
                </view>
              <button 
                class="recommend-btn" 
                :class="(item.isMutualFollow || item.isFollowing) ? 'followed' : 'unfollow'"
                @tap="toggleFollow(item.id || item.followerId || item.userId, index, 'fans')">
                {{ item.isMutualFollow ? '互相关注' : (item.isFollowing ? '已关注' : '回关') }}
              </button>
              </view>
            </view>
            
            <!-- 空状态 -->
            <view v-if="(!filteredFansList || filteredFansList.length===0) && !loadingMore.fans" class="empty-state">
              <text class="empty-text">您还没有粉丝~</text>
              <text class="empty-subtext">别太低调嘛,多发布动态能增加曝光哟</text>
            </view>
          </scroll-view>
        </view>
        
      </view>
      
      <!-- 加载状态 -->
      <view v-if="loadingMore[activeTab]" class="loading">加载中...</view>
    </view>
  </template>
  
  <script>
  import { api } from '@/common/js/api.js'
  import { util } from '@/common/js/util.js'
  
  export default {
    data(){
      return {
        ownerId: null, // 从URL参数获取
        ownerName: '', // 从URL参数获取
        tabs:[
          {label:'关注', key:'following'},
          {label:'粉丝', key:'fans'}
        ],
        activeTab:'following',
        activeIndex:0,
        lists:{following:[], fans:[], posts:[]},
        recommendedList:[],  // 推荐用户列表
        page:{following:1, fans:1, posts:1},
        pageSize:20,
        hasMore:{following:true, fans:true, posts:true},
        loadingMore:{following:false, fans:false, posts:false},
        searchKeyword:'',
        searchPlaceholder:'搜索全部关注'
      }
    },
    computed:{
      currentList(){ return this.lists[this.activeTab] || [] },
      // 过滤后的关注列表
      filteredFollowingList(){
        // 确保始终返回数组
        if (!this.lists || !this.lists.following) {
          return [];
        }
        const list = this.lists.following;
        if(!this.searchKeyword || !this.searchKeyword.trim()) {
          return list;
        }
        const keyword = this.searchKeyword.toLowerCase().trim();
        return list.filter(item => {
          if (!item) return false;
          const username = item.username || item.nickname || item.name || '';
          return username && username.toLowerCase().includes(keyword);
        });
      },
      // 过滤后的粉丝列表
      filteredFansList(){
        // 确保始终返回数组
        if (!this.lists || !this.lists.fans) {
          return [];
        }
        const list = this.lists.fans;
        if(!this.searchKeyword || !this.searchKeyword.trim()) {
          return list;
        }
        const keyword = this.searchKeyword.toLowerCase().trim();
        return list.filter(item => {
          if (!item) return false;
          const username = item.username || item.nickname || item.name || '';
          return username && username.toLowerCase().includes(keyword);
        });
      }
    },
    onLoad(options){
      // 从URL参数中获取ownerId和ownerName
      if (options && options.ownerId) {
        this.ownerId = parseInt(options.ownerId) || null
      } else {
        // 如果没有传入ownerId，使用当前登录用户ID
        this.ownerId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || null
      }
      
      if (options && options.ownerName) {
        this.ownerName = decodeURIComponent(options.ownerName)
      }
      
      // 设置初始导航栏标题
      if (this.ownerName && this.ownerName !== '用户') {
        uni.setNavigationBarTitle({ title: `${this.ownerName}的关注` });
      } else {
        uni.setNavigationBarTitle({ title: '我的关注' });
      }
      
      // 如果有传入defaultTab参数，切换到对应的tab
      if(options && options.defaultTab){
        const targetTab = options.defaultTab;
        if(['following', 'fans'].includes(targetTab)){
          this.switchTab(targetTab);
        } else {
          // 如果传入的是posts，默认显示关注
          this.loadList('following');
          this.loadRecommendedUsers();
        }
      } else {
        // 加载数据
        this.loadList('following');
        this.loadRecommendedUsers();
      }
    },
      watch: {
      activeTab(newTab) {
        // 切换tab时更新搜索框placeholder和导航栏标题
        const titlePrefix = this.ownerName && this.ownerName !== '用户' ? this.ownerName : '我的'
        if(newTab === 'following') {
          this.searchPlaceholder = '搜索全部关注';
          uni.setNavigationBarTitle({ title: `${titlePrefix}的关注` });
        } else if(newTab === 'fans') {
          this.searchPlaceholder = '搜索全部粉丝';
          uni.setNavigationBarTitle({ title: `${titlePrefix}的粉丝` });
        }
        // 清空搜索关键词
        this.searchKeyword = '';
      }
    },
    methods:{
      // 获取图片URL（处理本地路径和服务器路径）
      getImageUrl(imageUrl) {
        if (!imageUrl) {
          return util.getImageUrl('/static/images/garfield-default-avatar.png')
        }
        return util.getImageUrl(imageUrl)
      },
      // 获取推荐用户头像URL
      getRecommendAvatarUrl(user) {
        if (!user) {
          return util.getImageUrl('/static/images/login-dog.png')
        }
        // 优先使用avatarUrl，其次avatar，最后使用默认头像
        let avatarUrl = user.avatarUrl || user.avatar
        if (!avatarUrl || avatarUrl === '/static/images/garfield-default-avatar.png') {
          avatarUrl = '/static/images/login-dog.png'
        }
        return util.getImageUrl(avatarUrl)
      },
      // 处理头像加载错误
      handleAvatarError(e) {
        // 头像加载失败时，使用默认头像
        e.target.src = util.getImageUrl('/static/images/login-dog.png')
      },
      // 处理推荐用户头像加载错误
      handleRecommendAvatarError(e) {
        // 推荐用户头像加载失败时，使用默认头像
        e.target.src = util.getImageUrl('/static/images/login-dog.png')
      },
      // 跳转到用户个人资料页面
      goToUserProfile(userId) {
        if (!userId && userId !== 0) {
          uni.showToast({
            title: '用户ID为空',
            icon: 'none'
          })
          return
        }
        
        // 确保userId是数字类型
        let targetUserId = userId
        if (typeof userId === 'string') {
          targetUserId = parseInt(userId)
          if (isNaN(targetUserId)) {
            uni.showToast({
              title: '无效的用户ID',
              icon: 'none'
            })
            return
          }
        }
        
        if (targetUserId <= 0) {
          uni.showToast({
            title: '无效的用户ID',
            icon: 'none'
          })
          return
        }
        
        uni.navigateTo({
          url: `/user/profile?userId=${targetUserId}`,
          success: () => {
          },
          fail: (err) => {
            console.error('跳转失败:', err)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      },
      switchTab(tabKey){
        this.activeTab = tabKey;
        this.activeIndex = this.tabs.findIndex(t=>t.key===tabKey);
        if(this.lists[tabKey].length===0){
          this.loadList(tabKey);
        }
      },
      // 搜索输入
      onSearchInput(e){
        this.searchKeyword = e.detail.value;
      },
      // 搜索确认
      onSearchConfirm(e){
        this.searchKeyword = e.detail.value;
      },
      loadList(tabKey){
        if(!this.hasMore[tabKey]) return;
        
        // 确保ownerId存在
        if (!this.ownerId) {
          // 静默处理警告
          this.loadingMore[tabKey] = false
          return
        }
        
        this.loadingMore[tabKey] = true;
  
        // 获取当前用户ID（用于检查关注状态等）
        const currentUserId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || null;
        
        // 使用 api.request 以确保携带 token
        api.request({
          url: `/api/user/${this.ownerId}/${tabKey}`,
          method: 'GET',
          data: {
            page: this.page[tabKey],
            size: this.pageSize,
            currentUserId: currentUserId
          },
          showLoading: false // 不显示全局loading，使用局部loading
        })
        .then(res => {
          if(res.code===200){
            const data=res.data.list || [];
            // 确保头像URL正确，并转换localhost
            data.forEach((item, index) => {
              // 确保id字段存在且有效
              if (!item.id && item.followingId) {
                item.id = item.followingId
              }
              
              // 确保username字段存在（用于显示和搜索）
              if (!item.username && item.nickname) {
                item.username = item.nickname
              }
              if (!item.username && item.name) {
                item.username = item.name
              }
              if (!item.username) {
                item.username = `用户${item.id || '未知'}`
              }
              
              // 优先使用avatarUrl，如果没有则使用avatar字段
              let avatarUrl = item.avatarUrl || item.avatar || item.followerAvatar || item.followingAvatar
              if (avatarUrl) {
                // 使用util.getImageUrl处理头像URL（转换localhost等）
                item.avatarUrl = util.getImageUrl(avatarUrl)
              } else {
                item.avatarUrl = util.getImageUrl('/static/images/login-dog.png')
              }
              // 确保isFollowing字段存在（粉丝列表需要检查是否已关注）
              if (tabKey === 'fans' && item.isFollowing === undefined) {
                // 如果后端没有返回，默认为false
                item.isFollowing = false
              }
              // 确保isMutualFollow字段存在（粉丝列表需要检查是否互相关注）
              if (tabKey === 'fans' && item.isMutualFollow === undefined) {
                // 如果后端没有返回，默认为false
                item.isMutualFollow = false
              }
              // 确保fansCount字段存在
              if (item.fansCount === undefined || item.fansCount === null) {
                item.fansCount = 0
              }
            })
            // 确保数据正确赋值（使用Vue.set确保响应式）
            // 再次检查并确保每条数据都有id字段
            data.forEach((item, idx) => {
              if (!item.id) {
                // 尝试从其他可能的字段获取id
                if (item.followingId) {
                  item.id = item.followingId
                } else if (item.userId) {
                  item.id = item.userId
                }
              }
            })
            
            if (this.page[tabKey] === 1) {
              this.$set(this.lists, tabKey, data)
            } else {
              this.$set(this.lists, tabKey, this.lists[tabKey].concat(data))
            }
            this.hasMore[tabKey] = data.length===this.pageSize;
            this.page[tabKey]++;
            
            // 如果是关注列表，加载推荐用户（首次加载完成后）
            if(tabKey === 'following' && this.page[tabKey] === 2){
              this.loadRecommendedUsers();
            }
            
            // 每次加载关注列表后，更新推荐列表（过滤掉新关注的用户）
            if(tabKey === 'following' && this.recommendedList.length > 0){
              const followingIds = (this.lists.following || []).map(u => u.id);
              this.recommendedList = this.recommendedList.filter(user => !followingIds.includes(user.id));
            }
          } else {
            console.error(`加载${tabKey}列表失败:`, res.msg || res.message)
          }
        })
        .catch(err => {
          console.error('加载列表失败:', err)
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          })
        })
        .finally(() => {
          this.loadingMore[tabKey] = false
        })
      },
      // 加载推荐用户（showLoading: false 避免与 hideLoading 不配对警告）
      loadRecommendedUsers(){
        const currentUserId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || 1;
        return api.request({
          url: `/api/community/users/${currentUserId}/recommended`,
          method: 'GET',
          showLoading: false
        })
        .then(res => {
          if(res.code === 200 && res.data){
            // 获取当前用户的关注列表，用于过滤
            const followingList = this.lists.following || []
            const followingIds = followingList.map(u => u.id)
            
            // 统一字段名，并过滤掉已关注的用户
            this.recommendedList = res.data
              .filter(user => {
                const isFollowing = followingIds.includes(user.id)
                return !isFollowing
              })
              .slice(0, 6)
              .map(user => {
                // 优先使用avatarUrl，其次avatar，最后使用默认头像
                let avatarUrl = user.avatarUrl || user.avatar
                if (!avatarUrl || avatarUrl === '/static/images/garfield-default-avatar.png') {
                  avatarUrl = '/static/images/login-dog.png'
                }
                // 使用util.getImageUrl处理头像URL（转换localhost等）
                avatarUrl = util.getImageUrl(avatarUrl)
                return {
                  id: user.id,
                  username: user.name || user.username || `用户${user.id}`,  // 后端返回的是name字段
                  avatarUrl: avatarUrl,
                  avatar: avatarUrl, // 同时保存avatar字段
                  isFollowing: false,
                  fansCount: user.fansCount || 0
                }
              });
          }
        })
        .catch(err => {
          console.error('加载推荐用户失败:', err)
          return Promise.reject(err)
        })
      },
      // 换一批：重新拉取推荐用户，完成后提示（不触发全局 loading，避免 showLoading/hideLoading 不配对）
      changeBatch(){
        this.loadRecommendedUsers()
          .then(() => {
            uni.showToast({ title: '已刷新', icon: 'success' })
          })
          .catch(() => {
            uni.showToast({ title: '刷新失败，请重试', icon: 'none' })
          })
      },
      loadMore(){
        this.loadList(this.activeTab);
      },
      toggleFollow(userId,index,tabKey){
        // 如果是推荐列表的用户
        let user;
        if(index === -1){
          user = this.recommendedList.find(u => u.id === userId);
        } else {
          const list = this.lists[tabKey];
          user = list[index];
        }
        
        if(!user) {
          return;
        }
        
        const prev = user.isFollowing;
        
        // 获取当前用户ID
        const currentUserId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || 1;
        
        if (!currentUserId) {
          uni.showToast({title: '请先登录', icon: 'none'});
          return;
        }

        // 使用 api.request 以确保携带 token
        const method = prev ? 'DELETE' : 'POST';
        const url = prev 
          ? `/api/community/follow?followingId=${userId}&followerId=${currentUserId}`
          : `/api/community/follow?followingId=${userId}&followerId=${currentUserId}`;
        
        api.request({
          url: url,
          method: method,
          showLoading: false
        })
        .then(res => {
          if (res.code === 200 || res.code === 0) {
            user.isFollowing = !prev;
            // 如果是粉丝列表，需要更新互相关注状态
            if (tabKey === 'fans') {
              // 如果当前用户关注了这个粉丝，检查是否互相关注
              // 由于这个粉丝已经在粉丝列表中（说明他关注了当前用户），所以如果当前用户也关注了他，就是互相关注
              if (user.isFollowing) {
                user.isMutualFollow = true
              } else {
                // 取消关注后，不再是互相关注
                user.isMutualFollow = false
              }
            }
            uni.showToast({
              title: user.isFollowing ? '关注成功' : '取消关注成功',
              icon: 'success'
            });
            
            // 如果关注成功，从推荐列表中移除
            if (user.isFollowing && index === -1) {
              const idx = this.recommendedList.findIndex(u => u.id === userId);
              if (idx > -1) {
                this.recommendedList.splice(idx, 1);
              }
            }
            
            // 如果取消关注，刷新推荐列表（可能可以重新推荐）
            if (!user.isFollowing) {
              // 重新加载推荐列表，因为取消关注后可以重新推荐
              setTimeout(() => {
                this.loadRecommendedUsers();
              }, 500);
            }
            
            // 重新加载关注列表，确保数据同步
            if (this.activeTab === 'following') {
              if (user.isFollowing) {
                // 如果关注成功，先立即将新关注的用户添加到关注列表（优化体验）
                const newFollowingUser = {
                  id: user.id,
                  username: user.username || user.name || `用户${user.id}`,
                  avatarUrl: user.avatarUrl || user.avatar || util.getImageUrl('/static/images/login-dog.png'),
                  fansCount: user.fansCount || 0,
                  isFollowing: true
                };
                // 确保lists.following是数组
                if (!this.lists.following) {
                  this.$set(this.lists, 'following', []);
                }
                // 检查是否已存在，避免重复添加
                const exists = this.lists.following.some(u => u.id === userId);
                if (!exists) {
                  this.lists.following.unshift(newFollowingUser);
                }
                
                // 然后重新加载完整列表以确保数据同步
                setTimeout(() => {
                  this.page.following = 1;
                  this.hasMore.following = true;
                  this.loadList('following');
                }, 500);
              } else {
                // 如果取消关注，立即从列表中移除该用户（确保响应式更新）
                if (this.lists.following && this.lists.following.length > 0) {
                  // 优先使用index（如果有效），否则使用findIndex查找
                  let idx = -1;
                  if (index >= 0 && index < this.lists.following.length) {
                    // 验证index对应的用户ID是否匹配
                    const userAtIndex = this.lists.following[index];
                    const userAtIndexId = userAtIndex.id || userAtIndex.followingId || userAtIndex.userId;
                    if (userAtIndexId && (userAtIndexId === userId || userAtIndexId.toString() === userId.toString())) {
                      idx = index;
                    }
                  }
                  
                  // 如果index无效，使用findIndex查找
                  if (idx === -1) {
                    idx = this.lists.following.findIndex(u => {
                      const uId = u.id || u.followingId || u.userId;
                      return uId && (uId === userId || uId.toString() === userId.toString());
                    });
                  }
                  
                  if (idx > -1) {
                    // 使用Vue.set确保响应式更新，或者直接使用splice（Vue 2.x会自动检测）
                    // 先创建一个新数组，确保Vue检测到变化
                    const newList = [...this.lists.following];
                    newList.splice(idx, 1);
                    this.$set(this.lists, 'following', newList);
                  }
                }
                
                // 保存已移除的用户ID，用于后续检查
                const removedUserId = userId;
                
                // 延迟重新加载完整列表以确保数据同步
                // 注意：重新加载时，服务器应该已经更新，不会包含已取消关注的用户
                setTimeout(() => {
                  this.page.following = 1;
                  this.hasMore.following = true;
                  this.loadList('following');
                  
                  // 在重新加载完成后，再次检查并移除（防止服务器延迟导致重新添加）
                  setTimeout(() => {
                    if (this.lists.following && this.lists.following.length > 0) {
                      const stillExists = this.lists.following.findIndex(u => {
                        const uId = u.id || u.followingId || u.userId;
                        return uId && (uId === removedUserId || uId.toString() === removedUserId.toString());
                      });
                      if (stillExists > -1) {
                        const newList = [...this.lists.following];
                        newList.splice(stillExists, 1);
                        this.$set(this.lists, 'following', newList);
                      }
                    }
                  }, 800); // 等待loadList完成
                }, 500);
              }
            }
          } else {
            user.isFollowing = prev; // 恢复原状态
            uni.showToast({
              title: res.msg || '操作失败',
              icon: 'none'
            });
          }
        })
        .catch(err => {
          user.isFollowing = prev; // 恢复原状态
          console.error('关注操作失败:', err);
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          });
        })
      },
      // 获取粉丝列表按钮的文本
      getFansButtonText(item) {
        if (item.isMutualFollow) {
          return '互相关注'
        } else if (item.isFollowing) {
          return '已关注'
        } else {
          return '回关'
        }
      },
      // 获取粉丝列表按钮的样式类
      getFansButtonClass(item) {
        if (item.isMutualFollow || item.isFollowing) {
          return 'followed' // 互相关注或已关注时使用灰色样式
        } else {
          return 'unfollow' // 回关时使用红色样式
        }
      },
      // 跳转到帖子详情
      gotoPostDetail(postId){
        uni.navigateTo({
          url:`/pages-community/post-detail-image?id=${postId}`
        })
      }
    }
  }
  </script>
  
  <style>
  .container{
    flex:1; 
    background:#fff;
    height: 100vh;
  }
  
  /* Tab 栏（整体缩小） */
  .tab-bar{
    display: flex;
    flex-direction: row;
    background:#fff;
    border-bottom: 1px solid #f0f0f0;
  }
  .tab-item{
    flex: 1;
    padding: 16rpx;
    text-align: center;
    font-size: 26rpx;
    color: #666;
  }
  .tab-item.active{
    color: #333;
    font-weight: bold;
    border-bottom: 2px solid #FFD700;
  }
  
  /* 搜索框（缩小 + 使用 search.svg 图标） */
  .search-box{
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 14rpx 16rpx;
    background: #f8f8f8;
    border-radius: 10rpx;
    margin: 14rpx 16rpx;
  }
  .search-icon{
    width: 28rpx;
    height: 28rpx;
    margin-right: 10rpx;
  }
  .search-input{
    flex: 1;
    font-size: 26rpx;
    color: #333;
  }
  
  /* Tab 内容 */
  .tab-content{
    flex: 1;
  }
  .tab-panel{
    flex: 1;
    height: calc(100vh - 200rpx);
    display: flex;
    flex-direction: column;
  }
  .list{
    flex: 1;
    height: 100%;
    width: 100%;
  }
  
  /* 关注/粉丝列表项（整体缩小） */
  .following-item{
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 16rpx;
    border-bottom: 1px solid #f0f0f0;
    background-color: #fff;
    width: 100%;
    box-sizing: border-box;
  }
  .avatar{
    width: 68rpx;
    height: 68rpx;
    border-radius: 50%;
    margin-right: 16rpx;
  }
  .info{
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .username{
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 8rpx;
  }
  .fans-count{
    font-size: 22rpx;
    color: #999;
  }
  
  /* 已关注按钮（关注列表使用） */
  .recommend-btn.followed[disabled] {
    background: #e0e0e0;
    color: #666;
    padding: 8rpx 20rpx;
    border-radius: 18rpx;
    font-size: 22rpx;
    border: none;
    opacity: 1;
  }
  
  /* 推荐区域（缩小） */
  .recommend-section{
    margin-top: 28rpx;
  }
  .recommend-divider{
    text-align: center;
    color: #999;
    font-size: 22rpx;
    margin: 16rpx 0;
  }
  .recommend-grid{
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
    padding: 0 16rpx;
  }
  .recommend-item{
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  /* 与关注列表一致：头像 68rpx、昵称 26rpx、按钮与「已关注」同大 */
  .recommend-avatar{
    width: 68rpx;
    height: 68rpx;
    border-radius: 50%;
    margin-bottom: 8rpx;
  }
  .recommend-name{
    font-size: 26rpx;
    color: #333;
    margin-bottom: 8rpx;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 100%;
  }
  .recommend-btn{
    padding: 6rpx 14rpx;
    border-radius: 14rpx;
    font-size: 20rpx;
    border: none;
  }
  .recommend-btn.unfollow{
    background: #ff2442;
    color: #fff;
  }
  .recommend-btn.followed{
    background: #f0f0f0;
    color: #666;
  }
  
  /* 换一批按钮（缩小） */
  .change-batch-btn{
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    margin: 28rpx 0;
  }
  .refresh-icon{
    width: 28rpx;
    height: 28rpx;
    margin-right: 8rpx;
  }
  .btn-text{
    font-size: 24rpx;
    color: #666;
  }
  
  /* 空状态（缩小） */
  .empty-state{
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80rpx 32rpx;
    margin-top: 120rpx;
  }
  .empty-icon{
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 28rpx;
  }
  .empty-text{
    font-size: 26rpx;
    color: #999;
    margin-bottom: 8rpx;
  }
  .empty-subtext{
    font-size: 22rpx;
    color: #ccc;
  }
  
  /* 动态项（缩小） */
  .post-item{
    padding: 16rpx;
    border-bottom: 1px solid #f0f0f0;
  }
  .post-content{
    font-size: 26rpx;
    color: #333;
    line-height: 1.6;
    margin-bottom: 16rpx;
  }
  .post-image{
    width: 100%;
    border-radius: 10rpx;
    max-height: 420rpx;
  }
  
  /* 加载状态（缩小） */
  .loading{
    text-align: center;
    color: #999;
    padding: 28rpx;
    font-size: 22rpx;
  }
  </style>
  
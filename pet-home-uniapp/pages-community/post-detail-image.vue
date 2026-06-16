<template>
  <view class="post-detail-page">
    <scroll-view scroll-y class="xhscroll">
      <view class="xh-header-user" @tap="goToUserProfile">
        <image class="xh-header-avatar" :src="getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
        <view class="xh-header-info">
          <text class="xh-header-name">{{ post.userName || txt.anonymous }}</text>
        </view>
        <view class="xh-header-follow" v-if="post.userId !== currentUserId" @click.stop="toggleFollow">
          <text class="xh-header-follow-txt">{{ isFollowing ? txt.followed : txt.followPlus }}</text>
        </view>
        <view class="xh-header-more" v-else-if="post.userId === currentUserId" @click.stop="showPostActionSheet">
          <text class="xh-header-more-icon">{{ txt.more }}</text>
        </view>
      </view>

      <view class="xh-card-wrap" v-if="postImages && postImages.length > 0">
        <swiper class="xh-card-swiper" :style="{ height: (imageHeights[currentImageIndex] || 750) + 'rpx' }" :indicator-dots="postImages.length > 1" indicator-color="rgba(0,0,0,0.2)" indicator-active-color="#ff2442" :circular="true" @change="onSwiperChange">
          <swiper-item v-for="(image, index) in postImages" :key="index" @click="previewImage(index)">
            <image class="xh-card-img" :src="getImageUrl(image)" mode="widthFix" @load="onCardImageLoad($event, index)"></image>
          </swiper-item>
        </swiper>
        <view class="xh-card-indicator" v-if="postImages.length > 1">
          <text class="xh-card-page">{{ currentImageIndex + 1 }}/{{ postImages.length }}</text>
        </view>
      </view>
      <view class="xh-card-wrap xh-empty" v-else>
        <text class="xh-empty-txt">{{ txt.noImage }}</text>
      </view>

      <view class="xh-title" v-if="post.title">
        <text class="xh-title-txt">{{ post.title }}</text>
      </view>

      <view class="xh-content" v-if="post.content">
        <text class="xh-content-txt">{{ post.content }}</text>
      </view>


      <view class="xh-bottom-actions">
        <view class="xh-reply-hint-row" v-if="replyingToUserName">
          <text class="xh-reply-hint-text">{{ txt.reply }} {{ replyingToUserName }}</text>
          <text class="xh-reply-cancel" @tap="cancelReply">{{ txt.cancel }}</text>
        </view>
        <view class="xh-bottom-row">
          <view class="xh-bottom-input-wrap">
            <input 
              class="xh-bottom-input" 
              type="text" 
              v-model="commentText" 
              :focus="isCommentFocused" 
              :placeholder="bottomCommentPlaceholder" 
              @input="onCommentInput"
              @focus="onCommentFocus" 
              @blur="onCommentBlur" 
              @confirm="submitComment" 
            />
            <view class="xh-bottom-input-tools">
              <text class="xh-bottom-tool-at" @tap.stop="mentionUser">@</text>
              <image class="xh-bottom-tool-icon" :src="imgPicture" mode="aspectFit" @tap.stop="chooseCommentImage" />
            </view>
          </view>
          <view class="xh-bottom-icons">
          <view class="xh-bottom-icon" @tap="toggleLike">
            <image class="xh-icon-img" :class="{ 'xh-icon-active': isLiked }" :src="isLiked ? imgLikeOn : imgLikeOff" mode="aspectFit"></image>
            <text class="xh-icon-num">{{ formatCount(likeCount) }}</text>
          </view>
          <view class="xh-bottom-icon" @tap="toggleCollect">
            <image class="xh-icon-img xh-collect-icon" :class="{ 'xh-icon-active': isCollected }" :src="isCollected ? imgCollectOn : imgCollectOff" mode="aspectFit"></image>
            <text class="xh-icon-num">{{ formatCount(collectCount) }}</text>
          </view>
          <view class="xh-bottom-icon" @tap="sharePost">
            <image class="xh-icon-img" :src="imgShare" mode="aspectFit"></image>
          </view>
        </view>
        </view>
        <view v-if="commentImages.length" class="xh-comment-images-preview">
          <view class="xh-comment-img-item" v-for="(img, imgIdx) in commentImages" :key="imgIdx">
            <image class="xh-comment-img-thumb" :src="img" mode="aspectFill" />
            <view class="xh-comment-img-del" @tap.stop="removeCommentImage(imgIdx)">{{ txt.del }}</view>
          </view>
        </view>
      </view>


      <view class="xh-comments-section">
        <view class="xh-comments-header">
          <view class="xh-comments-bar"></view>
          <text class="xh-comments-title">{{ commentsHeaderTitle }}</text>
        </view>
        <view class="xh-comments-list" v-if="comments.length > 0">
          <view class="xh-comment-item" v-for="c in comments" :key="c.id" @longpress="onCommentLongPress(c)">
            <view class="xh-comment-main-row">
              <image class="xh-comment-avatar" :src="getImageUrl(c.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
              <view class="xh-comment-content-wrapper">
                <view class="xh-comment-header">
                  <text class="xh-comment-name">{{ c.userName }}</text>
                </view>
                <view class="xh-comment-text">
                  <block v-for="(part, pidx) in parseCommentContent(c.content)" :key="pidx">
                    <text v-if="part.type === 'text'" class="comment-text-inline">{{ part.text }}</text>
                    <text v-else-if="part.type === 'mention'" class="comment-text-inline comment-mention">{{ part.text }}</text>
                    <image v-else-if="part.type === 'image'" class="comment-image-inline" :src="getImageUrl(part.url)" mode="widthFix" />
                  </block>
                </view>
                <view class="xh-comment-meta">
                  <text class="xh-comment-date-loc">{{ formatCommentDate(c.createTime) }}{{ c.location ? tLoc(c.location) : '' }}</text>
                  <text class="xh-comment-reply-btn" @click.stop="replyComment(c)">{{ txt.reply }}</text>
                </view>

                <view class="xh-comment-actions-right">
                  <view class="xh-comment-action-item" @click.stop="toggleCommentLike(c)">
                    <image class="xh-action-icon" :src="c.isLiked ? imgLikeOn : imgLikeOff" mode="aspectFit"></image>
                    <text class="xh-action-num" :class="{ 'xh-num-liked': c.isLiked }" v-if="c.likesCount > 0">{{ c.likesCount }}</text>
                  </view>
                  <view class="xh-comment-action-item" v-if="false">
                    <image class="xh-action-icon xh-emoji-icon" :src="imgEmoji" mode="aspectFit"></image>
                  </view>
                </view>
              </view>
            </view>


            <view class="xh-reply-level1" v-if="hasCommentReplies(c)">
              <template v-if="c.replies && c.replies.length > 0">
                <template v-for="(ent, idx) in getFlatReplies(c)">
                <view v-if="ent.type === 'reply'" :key="ent._key" class="xh-reply-item" @longpress="onCommentLongPress(ent.item)">
                  <image class="xh-reply-avatar" :src="getImageUrl(ent.item.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
                  <view class="xh-reply-content-wrapper">
                    <view class="xh-reply-header">
                      <text class="xh-reply-name">{{ ent.item.userName }}</text>
                      <text class="xh-reply-arrow" v-if="getReplyToUserName(ent)">{{ txt.reply }}</text>
                      <text class="xh-reply-to-name" v-if="getReplyToUserName(ent)">{{ getReplyToUserName(ent) }}</text>
                      <view class="xh-author-tag" v-if="ent.item.userId === post.userId">
                        <text class="xh-author-tag-text">{{ txt.author }}</text>
                      </view>
                    </view>
                    <text class="xh-reply-text">{{ ent.item.content }}</text>
                    <view class="xh-reply-meta">
                      <text class="xh-reply-date-loc">{{ formatCommentDate(ent.item.createTime) }}{{ ent.item.location ? tLoc(ent.item.location) : '' }}</text>
                      <text class="xh-reply-reply-btn" @click.stop="replyComment(ent.item)">{{ txt.reply }}</text>
                    </view>
                    <view class="xh-reply-actions-right">
                      <view class="xh-reply-action-item" @click.stop="toggleCommentLike(ent.item, ent.parent)">
                        <image class="xh-action-icon" :src="ent.item.isLiked ? imgLikeOn : imgLikeOff" mode="aspectFit"></image>
                        <text class="xh-action-num" :class="{ 'xh-num-liked': ent.item.isLiked }" v-if="(ent.item.likesCount || 0) > 0">{{ ent.item.likesCount }}</text>
                      </view>
                    </view>
                  </view>
                </view>
                <view v-else-if="ent.type === 'expand'" :key="'expand-' + ent._key" class="xh-expand-same" @click.stop="expandReplies(ent.key)">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text">{{ tExpandInline(ent.count) }}</text>
                  <text class="xh-expand-arrow">{{ txt.arrowR }}</text>
                </view>
                <view v-else-if="ent.type === 'expand_more_row'" :key="'expand_more_row-' + ent._key" class="xh-expand-same xh-expand-more-row">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text" @click.stop="expandMoreReplies(ent.commentId)">{{ txt.expandMore }}</text>
                  <text class="xh-expand-arrow" @click.stop="expandMoreReplies(ent.commentId)">{{ txt.arrowR }}</text>
                  <view class="xh-expand-gap"></view>
                  <text class="xh-expand-text xh-expand-collapse" @click.stop="collapseReplies(ent.commentId)">{{ txt.collapse }}</text>
                  <text class="xh-expand-arrow-up" @click.stop="collapseReplies(ent.commentId)">{{ txt.arrowL }}</text>
                </view>
                <view v-else-if="ent.type === 'collapse_row'" :key="'collapse_row-' + ent._key" class="xh-expand-same" @click.stop="collapseReplies(ent.commentId)">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text">{{ txt.collapse }}</text>
                  <text class="xh-expand-arrow-up">{{ txt.arrowL }}</text>
                </view>
              </template>
              </template>
              <view v-else class="xh-expand-same" @click.stop="expandReplies(c)">
                <view class="xh-expand-line"></view>
                <text class="xh-expand-text">{{ tLoadingExpand(c) }}</text>
                <text class="xh-expand-arrow">{{ txt.arrowR }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="xh-comments-empty" v-else>
          <text class="xh-comments-empty-txt">{{ txt.commentsEmpty }}</text>
        </view>
      </view>
      <view class="xh-bottom-pad"></view>
    </scroll-view>


    <view class="mention-picker-mask" v-if="showMentionPicker" @tap="showMentionPicker = false"></view>
    <view class="mention-picker" v-if="showMentionPicker">
      <view class="mention-picker-title">{{ txt.mentionTitle }}</view>
      <scroll-view scroll-y class="mention-picker-list">
        <view class="mention-picker-item" v-for="u in mentionUserList" :key="u.id" @tap.stop="selectMentionUser(u)">
          <image class="mention-picker-avatar" :src="getImageUrl(u.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" />
          <text class="mention-picker-name">{{ u.userName || txt.anonymous }}</text>
        </view>
        <view v-if="mentionUserList.length === 0" class="mention-picker-empty">{{ txt.mentionEmpty }}</view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      postId: '',
      post: {},
      isFollowing: false,
      isLiked: false,
      likeCount: 0,
      commentCount: 0,
      comments: [],
      commentText: '',
      replyingTo: null,
      replyingToUserName: '',
      currentUserId: null,
      postImages: [],
      postTags: [],
      currentImageIndex: 0,
      isCollected: false,
      collectCount: 0,
      isCommentFocused: false,
      userCans: 0,
      currentLocation: '',
      postVideos: [],
      currentVideoIndex: 0,
      currentVideoUrl: '',
      currentVideoCover: '',
      hasVideo: false,
      lastDetailLoadTime: 0,
      videoLoading: false,
      videoError: '',
      hasShownBlackScreenTip: false,
      videoKey: 0,
      expandedComments: {},
      expandedReplies: {},
      expandedVisibleCount: {},
      imageHeights: {},
      commentImages: [],
      showMentionPicker: false,
      mentionUserList: [],
      lastCommentText: '',
      imgLikeOn: '/static/images/community-like-on.svg',
      imgLikeOff: '/static/images/community-like-off.svg',
      imgCollectOn: '/static/images/community-collect-on.svg',
      imgCollectOff: '/static/images/community-collect-off.svg',
      imgShare: '/static/images/\u5206\u4eab.png',
      imgPicture: '/static/images/\u56fe\u7247.png',
      imgEmoji: '/static/images/\u8868\u60c5.png'
    }
  },

  computed: {
    bottomCommentPlaceholder() {
      const n = this.replyingToUserName
      return n ? '\u56de\u590d ' + n + '...' : '\u5584\u610f\u8bc4\u8bba\uff0c\u8bf4\u8bf4\u4f60\u7684\u60f3\u6cd5~'
    },
    panelCommentPlaceholder() {
      const n = this.replyingToUserName
      return n ? '\u56de\u590d' + n + '...' : '\u8bf4\u70b9\u4ec0\u4e48..'
    },

    txt() {
      return {
        anonymous: '\u533f\u540d\u7528\u6237',
        followed: '\u5df2\u5173\u6ce8',
        followPlus: '+ \u5173\u6ce8',
        more: '\u22ef',
        noImage: '\u6682\u65e0\u56fe\u7247',
        cancel: '\u53d6\u6d88',
        reply: '\u56de\u590d',
        author: '\u4f5c\u8005',
        expandMore: '\u5c55\u5f00\u66f4\u591a\u56de\u590d',
        collapse: '\u6536\u8d77',
        commentsEmpty: '\u8fd8\u6ca1\u6709\u8bc4\u8bba\uff0c\u5feb\u6765\u62a2\u6c99\u53d1~',
        close: '\u5173\u95ed',
        row: '\u6761',
        arrowR: '\u203a',
        arrowL: '\u2039',
        viewMore: '\u67e5\u770b\u66f4\u591a\u8bc4\u8bba',
        mentionTitle: '\u9009\u62e9\u8981 @ \u7684\u7528\u6237',
        mentionEmpty: '\u6682\u65e0\u53ef @ \u7684\u7528\u6237',
        send: '\u53d1\u9001',
        del: '\u00d7'
      }
    },
    commentsHeaderTitle() {
      return '\u5171 ' + this.commentCount + ' \u6761\u8bc4\u8bba'
    }
  },

  async onLoad(options) {
    let postId = options.id || options.postId

    if (!postId || postId === 'undefined' || postId === 'null' || postId === undefined || postId === null) {
      console.error('invalid post id', { options, postId })
      uni.showToast({
        title: '\u7f3a\u5c11\u5e16\u5b50 ID',
        icon: 'none',
        duration: 2000
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }

    this.postId = String(postId).trim()
    if (options.likeCount !== undefined && options.likeCount !== '' && !isNaN(Number(options.likeCount))) {
      this.likeCount = Number(options.likeCount)
    }
    if (options.isLiked !== undefined && options.isLiked !== '') {
      this.isLiked = options.isLiked === '1' || options.isLiked === true
    }
    this.loadCurrentUserId()
    this.getCurrentLocation()
    await this.loadPostDetail()
    this.loadComments()
  },

  onShow() {
    this.loadCurrentUserId()
    if (!this.postId) return
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastDetailLoadTime || 0) < throttleMs && (this.lastDetailLoadTime || 0) > 0) return
    this.loadPostDetail(true)
    this.loadComments(true)
    this.lastDetailLoadTime = Date.now()
  },

  onShareAppMessage() {
    return {
      title: (this.post && (this.post.title || this.post.content)) ? String(this.post.title || this.post.content).slice(0, 30) : '\u793e\u533a\u52a8\u6001',
      path: `/pages-community/post-detail-image?id=${this.postId || ''}`
    }
  },
  onShareTimeline() {
    return {
      title: (this.post && (this.post.title || this.post.content)) ? String(this.post.title || this.post.content).slice(0, 30) : '\u793e\u533a\u52a8\u6001',
      query: `id=${this.postId || ''}`
    }
  },

  methods: {
    tExpandInline(n) {
      return '\u5c55\u5f00 ' + n + ' \u6761'
    },
    tExpandRepliesMinus(comment) {
      return '\u5c55\u5f00 ' + (comment.replies.length - 2) + ' \u6761\u56de\u590d'
    },
    tLoadingExpand(c) {
      if (c.repliesLoading) return '\u52a0\u8f7d\u4e2d..'
      return '\u5c55\u5f00' + (c.replyCount || 0) + '\u6761\u56de\u590d'
    },
    tLoc(loc) {
      return loc ? (' \u00b7 ' + loc) : ''
    },
    loadCurrentUserId() {
      let userId = uni.getStorageSync('userId')

      if (!userId) {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          userId = userInfo.id || userInfo.uid || userInfo.userId
        }
      }

      if (userId) {
        this.currentUserId = Number(userId)
      } else {
        this.currentUserId = null
      }
    },

    getCurrentLocation(forceRefresh = false) {
      const cachedLocation = uni.getStorageSync('cached_location')
      const cacheTime = uni.getStorageSync('cached_location_time')
      const now = Date.now()

      if (!forceRefresh && cachedLocation && cacheTime && (now - cacheTime < 5 * 60 * 1000)) {
        this.currentLocation = cachedLocation
        return
      }

      if (forceRefresh) {
        uni.removeStorageSync('cached_location')
        uni.removeStorageSync('cached_location_time')
      }

      this.currentLocation = ''

      uni.request({
        url: 'http://ip-api.com/json/?lang=zh-CN&fields=status,message,regionName,country',
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.status === 'success' && res.data.regionName) {
            let province = res.data.regionName
            province = province.replace(/(\u7701|\u5e02|\u81ea\u6cbb\u533a|\u58ee\u65cf\u81ea\u6cbb\u533a|\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a|\u56de\u65cf\u81ea\u6cbb\u533a|\u7279\u522b\u884c\u653f\u533a)$/, '')
            this.currentLocation = province

            uni.setStorageSync('cached_location', province)
            uni.setStorageSync('cached_location_time', now)
          } else {
            this.currentLocation = ''
          }
        },
        fail: () => {
          this.currentLocation = ''
        }
      })
    },

    reverseGeocode(latitude, longitude) {
      if (!latitude || !longitude || latitude === 0 || longitude === 0) {
        this.currentLocation = ''
        return
      }

      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77&get_poi=0`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.status === 0 && res.data.result) {
            const address = res.data.result.address_component
            if (address && address.province) {
              let province = address.province
              province = province.replace(/(\u7701|\u5e02|\u81ea\u6cbb\u533a|\u7279\u522b\u884c\u653f\u533a)$/, '')
              this.currentLocation = province
            } else {
              this.currentLocation = ''
            }
          } else {
            this.currentLocation = ''
          }
        },
        fail: () => {
          this.currentLocation = ''
        }
      })
    },

    async loadPostDetail(silent = false) {
      try {
        if (!this.postId) return
        const showLoading = !silent
        const response = await api.getPostDetail(this.postId, this.currentUserId, showLoading)
        
        if (response.code === 200 || response.code === 0) {
          if (!response.data) {
            uni.showToast({
              title: '\u5e16\u5b50\u4e0d\u5b58\u5728\u6216\u5df2\u5220\u9664',
              icon: 'none'
            })
            return
          }
          this.post = response.data
          this.lastDetailLoadTime = Date.now()

          if (this.post.images) {
            if (typeof this.post.images === 'string') {
              try {
                this.postImages = JSON.parse(this.post.images)
              } catch (e) {
                this.postImages = [this.post.images]
              }
            } else if (Array.isArray(this.post.images)) {
              this.postImages = this.post.images
            }
            this.imageHeights = {}
          }
          
          this.parseVideos()


          if (this.hasVideo && this.postImages && this.postImages.length > 0) {
            const videoExtensions = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
            this.postImages = this.postImages.filter(img => {
              if (typeof img === 'string') {
                const url = img.toLowerCase()
                return !videoExtensions.some(ext => url.includes(ext))
              }
              return true
            })

          }
          

          this.parseTags()
          this.isLiked = this.post.isLiked === true || this.post.isLiked === 1
          this.likeCount = this.post.likesCount || 0
          this.commentCount = this.post.commentsCount || 0
          this.collectCount = this.post.collectCount || 0
          this.isCollected = this.post.isCollected === true || this.post.isCollected === 1 || 
                             this.post.collected === true || this.post.collected === 1
          
          if (this.post.userId) {
            this.loadUserInfo(this.post.userId)
          }

          this.checkLikeAndFollowStatus()

          if (this.currentUserId) {
            this.checkCollectStatus()
          }
        } else {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },

    parseVideos() {
      this.hasVideo = false
      this.postVideos = []
      this.currentVideoUrl = ''
      this.currentVideoCover = ''

      if (this.post.videos && this.post.videos !== null && this.post.videos !== 'null' && this.post.videos !== '') {
        try {
          let videosData = this.post.videos
          if (typeof videosData === 'string') {
            try {
              videosData = JSON.parse(videosData)
            } catch (parseError) {
              if (videosData.includes('.mp4') || videosData.includes('.mov') || videosData.includes('.avi')) {
                videosData = [{ url: videosData, thumb: '', cover: '' }]
              } else {
                videosData = null
              }
            }
          }

          if (Array.isArray(videosData) && videosData.length > 0) {
            const validVideos = videosData.filter(v => v && (v.url || typeof v === 'string'))
            if (validVideos.length > 0) {
              this.postVideos = validVideos
              this.hasVideo = true
              this.currentVideoIndex = 0
              this.setCurrentVideo(0)
              return
            }
          } else if (typeof videosData === 'object' && videosData && videosData.url) {
            this.postVideos = [videosData]
            this.hasVideo = true
            this.currentVideoIndex = 0
            this.setCurrentVideo(0)
            return
          }
        } catch (e) {
          console.error('加载视频失败:', e)
        }
      }

      if (!this.hasVideo && this.postImages && this.postImages.length > 0) {
        const videoExtensions = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
        const videoFiles = this.postImages.filter(img => {
          if (typeof img === 'string') {
            const url = img.toLowerCase()
            return videoExtensions.some(ext => url.includes(ext))
          }
          return false
        })

        if (videoFiles.length > 0 && (!this.post.videos || this.post.videos === null || this.post.videos === 'null' || this.post.videos === '')) {
          this.postVideos = videoFiles.map(url => ({ url: url, cover: '', thumb: '' }))
          this.hasVideo = true
          this.currentVideoIndex = 0
          this.setCurrentVideo(0)
        }
      }
    },
    

    setCurrentVideo(index) {
      if (!this.postVideos || this.postVideos.length <= index) return
      const video = this.postVideos[index]
      let rawUrl = typeof video === 'string' ? video : (video && video.url ? video.url : '')
      if (!rawUrl) return

      this.currentVideoUrl = rawUrl.startsWith('https://') ? rawUrl : rawUrl.replace('http://', 'https://')

      const thumb = video && (video.thumb || video.cover)
      if (thumb) {
        this.currentVideoCover = this.getImageUrl(thumb)
      } else if (this.post.coverImage) {
        const coverUrl = this.post.coverImage.toLowerCase()
        const isVideo = ['.mp4', '.mov', '.avi', '.m4v'].some(ext => coverUrl.includes(ext))
        this.currentVideoCover = isVideo ? '' : this.getImageUrl(this.post.coverImage)
      } else {
        this.currentVideoCover = ''
      }
    },
    
    onVideoPlay() {
      this.videoLoading = false
    },

    onVideoPause() {
    },

    onVideoEnded() {
    },

    onVideoError(e) {
      this.videoLoading = false
      const detail = e.detail || {}

      let errorMsg = '视频加载失败'
      if (detail.errMsg) {
        errorMsg = detail.errMsg
        this.videoError = detail.errMsg
      } else if (detail.errCode) {
        errorMsg = `播放错误 (错误码: ${detail.errCode})`
        this.videoError = `错误码: ${detail.errCode}`
      } else {
        if (this.currentVideoUrl && !errorMsg.includes('??') && !errorMsg.includes('URL')) {
          this.videoError = '??? MP4 + H.264 ??'
          if (!this.hasShownBlackScreenTip) {
            this.hasShownBlackScreenTip = true
            setTimeout(() => {
              uni.showModal({
                title: '提示',
                content: '视频格式不支持，请使用MP4 + H.264 + AAC',
                showCancel: false,
                confirmText: '知道了'
              })
            }, 500)
          }
        } else {
          this.videoError = '视频加载失败'
        }
      }

      uni.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 3000
      })
    },

    onVideoTimeUpdate(e) {
    },

    onVideoWaiting() {
      this.videoLoading = true
    },

    onVideoProgress(e) {
      this.videoLoading = false
    },

    onVideoLoadedMetadata(e) {
      this.videoLoading = false
      const detail = e.detail || {}

      if (detail.duration && detail.duration > 0) {
        if (!detail.width || !detail.height) {
          if (!this.hasShownBlackScreenTip) {
            this.hasShownBlackScreenTip = true
            uni.showModal({
              title: '提示',
              content: '视频尺寸异常，请使用 MP4/H.264 格式',
              showCancel: false,
              confirmText: '知道了',
              confirmColor: '#007AFF'
            })
          }
        }
        this.videoError = ''
      }
    },

    onVideoLoadStart(e) {
      this.videoLoading = true
      this.videoError = ''
    },

    formatCount(count) {
      if (!count) return '0'
      if (count < 1000) return String(count)
      if (count < 10000) return (count / 1000).toFixed(1) + 'k'
      return (count / 10000).toFixed(1) + 'w'
    },

    goBack() {
      uni.navigateBack()
    },

    showPostActionSheet() {
      uni.showActionSheet({
        itemList: ['\u5220\u9664\u5e16\u5b50'],
        success: (res) => {
          if (res.tapIndex === 0) this.handleDeletePost()
        }
      })
    },

    async handleDeletePost() {
      uni.showModal({
        title: '\u63d0\u793a',
        content: '\u786e\u5b9a\u5220\u9664\u8be5\u52a8\u6001\u5417\uff1f',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '\u5220\u9664\u4e2d...' })
            await api.deletePost(this.postId, this.currentUserId)
            uni.hideLoading()
            uni.setStorageSync('REFRESH_MY_STATS', '1')
            uni.showToast({ title: '\u5220\u9664\u6210\u529f', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 500)
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: e.msg || e.message || '\u5220\u9664\u5931\u8d25', icon: 'none' })
          }
        }
      })
    },

    // 解析标签
    parseTags() {
      const content = this.post.content || ''
      const tagRegex = /#([^#\s]+)/g
      const tags = []
      let match
      while ((match = tagRegex.exec(content)) !== null) {
        tags.push(match[1])
      }
      this.postTags = tags
    },

    formatPostDate(dateString) {
      if (!dateString) return ''
      let date
      if (dateString.includes(' ')) {
        date = new Date(dateString.replace(' ', 'T'))
      } else {
        date = new Date(dateString)
      }
      const m = date.getMonth() + 1
      const d = date.getDate()
      return `${m}-${d.toString().padStart(2, '0')}`
    },

    formatCommentTime(dateString) {
      if (!dateString) return ''
      let date
      if (dateString.includes(' ')) {
        date = new Date(dateString.replace(' ', 'T'))
      } else {
        date = new Date(dateString)
      }

      if (isNaN(date.getTime())) {
        return ''
      }

      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (minutes < 1) return '\u521a\u521a'
      if (minutes < 60) return `${minutes}\u5206\u949f\u524d`
      if (hours < 24) return `${hours}\u5c0f\u65f6\u524d`
      if (days < 7) return `${days}\u5929\u524d`

      const m = date.getMonth() + 1
      const d = date.getDate()
      return `${m}-${d.toString().padStart(2, '0')}`
    },

    formatCommentDate(createTime) {
      const time = this._commentTimeToMs(createTime)
      if (time == null || isNaN(time)) return ''
      const now = Date.now()
      const diff = now - time
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour
      if (diff < minute) return '\u521a\u521a'
      if (diff < hour) return Math.floor(diff / minute) + '\u5206\u949f\u524d'
      if (diff < day) return Math.floor(diff / hour) + '\u5c0f\u65f6\u524d'
      if (diff < 7 * day) return Math.floor(diff / day) + '\u5929\u524d'
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    _commentTimeToMs(v) {
      if (v == null) return null
      if (typeof v === 'number') return v < 1e12 ? v * 1000 : v
      const d = new Date(typeof v === 'string' && v.includes(' ') ? v.replace(' ', 'T') : v)
      return isNaN(d.getTime()) ? null : d.getTime()
    },

    previewImage(index) {
      uni.previewImage({
        urls: this.postImages,
        current: index
      })
    },

    onSwiperChange(e) {
      this.currentImageIndex = e.detail.current
    },
    onCardImageLoad(e, index) {
      const { width, height } = e.detail || {}
      if (!width || !height) return
      const heightRpx = (height / width) * 750
      const clamped = Math.min(Math.max(heightRpx, 300), 2000)
      this.$set(this.imageHeights, index, Math.round(clamped))
    },

    downloadImage() {
      if (!this.postImages || this.postImages.length === 0) {
        uni.showToast({
          title: '没有图片可下载',
          icon: 'none'
        })
        return
      }
      const currentImg = this.postImages[this.currentImageIndex] || this.postImages[0]
      const imgUrl = this.getImageUrl(currentImg)
      uni.downloadFile({
        url: imgUrl,
        success: (res) => {
          if (res.statusCode === 200) {
            uni.saveImageToPhotosAlbum({
              filePath: res.tempFilePath,
              success: () => {
                uni.showToast({
                  title: '保存成功',
                  icon: 'success'
                })
              },
              fail: () => {
                uni.showToast({
                  title: '保存失败',
                  icon: 'none'
                })
              }
            })
          }
        },
        fail: () => {
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
      })
    },

    findCommentById(comments, commentId) {
      for (const c of comments) {
        if (c.id === commentId) {
          return c
        }
        if (c.replies && Array.isArray(c.replies)) {
          const found = this.findCommentInReplies(c.replies, commentId)
          if (found) return found
        }
      }
      return null
    },
    
    findCommentInReplies(replies, commentId) {
      for (const reply of replies) {
        if (reply.id === commentId) {
          return reply
        }
        if (reply.replies && Array.isArray(reply.replies)) {
          const found = this.findCommentInReplies(reply.replies, commentId)
          if (found) return found
        }
      }
      return null
    },

    canDeleteComment(comment) {
      if (!comment || !this.currentUserId) return false
      const uid = Number(this.currentUserId)
      const commentUserId = comment.userId != null ? Number(comment.userId) : null
      const postUserId = this.post && this.post.userId != null ? Number(this.post.userId) : null
      return commentUserId === uid || postUserId === uid
    },

    onCommentLongPress(comment) {
      if (!this.canDeleteComment(comment)) return
      uni.showActionSheet({
        itemList: ['\u5220\u9664\u8bc4\u8bba'],
        success: (res) => {
          if (res.tapIndex === 0) this.handleDeleteComment(comment)
        }
      })
    },

    async handleDeleteComment(comment) {
      uni.showModal({
        title: '\u63d0\u793a',
        content: '\u786e\u5b9a\u5220\u9664\u8fd9\u6761\u8bc4\u8bba\u5417\uff1f',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '\u5220\u9664\u4e2d...' })
            await api.deleteComment(comment.id, this.currentUserId)
            uni.hideLoading()
            this.commentCount = Math.max(0, (this.commentCount || 0) - 1)
            await this.loadComments()
            uni.showToast({ title: '\u5220\u9664\u6210\u529f', icon: 'success' })
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: (e.msg || e.message) || '\u5220\u9664\u5931\u8d25', icon: 'none' })
          }
        }
      })
    },

    async toggleCommentLike(comment, parentComment) {
      this.loadCurrentUserId()
      if (!comment) {
        uni.showToast({ title: '参数错误', icon: 'none' })
        return
      }
      const commentId = comment.id != null ? Number(comment.id) : NaN
      if (!Number.isFinite(commentId)) {
        uni.showToast({ title: '评论ID无效', icon: 'none' })
        return
      }
      if (parentComment && parentComment.id != null && commentId === Number(parentComment.id)) {
        uni.showToast({ title: '不能回复自己', icon: 'none' })
        return
      }
      if (!this.currentUserId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const wasLiked = comment.isLiked
      const oldCount = comment.likesCount || comment.likeCount || 0
      const nextLiked = !wasLiked
      const nextCount = nextLiked ? oldCount + 1 : Math.max(0, oldCount - 1)

      const setLikeState = (target, liked, count) => {
        if (!target) return
        this.$set(target, 'isLiked', liked)
        this.$set(target, 'likesCount', count)
        this.$set(target, 'likeCount', count)
      }
      setLikeState(comment, nextLiked, nextCount)

      try {
        let res
        if (nextLiked) {
          res = await api.likeComment(commentId, this.currentUserId)
        } else {
          res = await api.unlikeComment(commentId, this.currentUserId)
        }

        if (res && res.code !== 200 && res.code !== 0) {
          setLikeState(comment, wasLiked, oldCount)
          uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
          return
        }
        const finalLiked = res && res.data === false ? (nextLiked ? true : false) : nextLiked
        const finalCount = res && res.data === false ? (nextLiked ? oldCount + 1 : Math.max(0, oldCount - 1)) : nextCount
        setLikeState(comment, finalLiked, finalCount)
        const found = this.findCommentOrReplyById(commentId)
        if (found) {
          setLikeState(found, finalLiked, finalCount)
        }
        this.$forceUpdate()
      } catch (error) {
        setLikeState(comment, wasLiked, oldCount)
        console.error('评论点赞失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    findCommentOrReplyById(commentId) {
      const id = Number(commentId)
      if (!Number.isFinite(id) || !this.comments || !this.comments.length) return null
      for (const c of this.comments) {
        if (c.id === id) return c
        const replies = c.replies || []
        for (const r of replies) {
          if (r.id === id) return r
          for (const n of (r.replies || [])) {
            if (n.id === id) return n
          }
        }
      }
      return null
    },

    async checkCollectStatus() {
      if (!this.postId || !this.currentUserId) return
      
      try {
        const res = await api.checkCollectStatus(this.postId, this.currentUserId)
        if (res.code === 200 || res.code === 0) {
          this.isCollected = res.data === true || res.data === 1
        }
      } catch (error) {
      }
    },
    
    async toggleCollect() {
      if (!this.currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }

      try {
        let res
        if (this.isCollected) {
          res = await api.uncollectPost(this.postId, this.currentUserId)
        } else {
          res = await api.collectPost(this.postId, this.currentUserId)
        }

        if (res.code === 200 || res.code === 0) {
          this.isCollected = !this.isCollected
          this.collectCount += this.isCollected ? 1 : -1

          if (this.post) {
            this.post.isCollected = this.isCollected
            this.post.collectCount = this.collectCount
          }

          this.$forceUpdate()

          uni.showToast({
            title: this.isCollected ? '收藏成功' : '已取消收藏',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: res.msg || '操作失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },

    onCommentFocus() {
      this.isCommentFocused = true
    },

    onCommentInput(e) {
      const newVal = (e && e.detail && e.detail.value !== undefined) ? e.detail.value : ''
      const oldVal = this.lastCommentText || ''
      this.commentText = newVal
      this.lastCommentText = newVal
      if (newVal.endsWith('@') && !oldVal.endsWith('@')) {
        this.loadMentionUsers()
        this.showMentionPicker = true
        this.$nextTick(() => {
          this.isCommentFocused = true
        })
      }
      if (!newVal.endsWith('@') && this.showMentionPicker) {
        this.showMentionPicker = false
      }
    },

    parseCommentContent(text) {
      if (!text || typeof text !== 'string') return [{ type: 'text', text: text }]
      const parts = []
      const imgRegex = /\[??\](https?:\/\/[^\s\[\]]+)/g
      let lastEnd = 0
      let m
      while ((m = imgRegex.exec(text)) !== null) {
        if (m.index > lastEnd) this._pushMentionParts(parts, text.substring(lastEnd, m.index))
        parts.push({ type: 'image', url: m[1] })
        lastEnd = m.index + m[0].length
      }
      if (lastEnd < text.length) this._pushMentionParts(parts, text.substring(lastEnd))
      return parts.length ? parts : [{ type: 'text', text: text }]
    },
    _pushMentionParts(parts, segment) {
      if (!segment) return
      const mentionRegex = /@[\u4e00-\u9fa5A-Za-z0-9_-]+/g
      let last = 0
      let m
      while ((m = mentionRegex.exec(segment)) !== null) {
        if (m.index > last) {
          parts.push({ type: 'text', text: segment.substring(last, m.index) })
        }
        parts.push({ type: 'mention', text: m[0] })
        last = m.index + m[0].length
      }
      if (last < segment.length) {
        parts.push({ type: 'text', text: segment.substring(last) })
      }
    },

    chooseCommentImage() {
      const remain = 9 - this.commentImages.length
      if (remain <= 0) {
        uni.showToast({ title: '\u6700\u591a 9 \u5f20\u56fe\u7247', icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: ['\u62cd\u7167', '\u4ece\u76f8\u518c\u9009\u62e9'],
        success: (sheetRes) => {
          const sourceType = sheetRes.tapIndex === 0 ? ['camera'] : ['album']
          uni.chooseMedia({
            count: remain,
            mediaType: ['image'],
            sourceType: sourceType,
            success: (res) => {
              (res.tempFiles || []).forEach(f => {
                if (f.tempFilePath && this.commentImages.length < 9) this.commentImages.push(f.tempFilePath)
              })
            }
          })
        }
      })
    },
    removeCommentImage(idx) {
      this.commentImages.splice(idx, 1)
    },

    uploadOneImage(tempPath) {
      return new Promise((resolve) => {
        uni.uploadFile({
          url: api.getBaseUrl() + '/api/upload/image',
          filePath: tempPath,
          name: 'file',
          formData: { type: 'comment' },
          success: (res) => {
            try {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if ((data.code === 0 || data.code === 200) && data.data) {
                const url = typeof data.data === 'string' ? data.data : (data.data.url || data.data)
                resolve(url)
              } else resolve('')
            } catch (e) { resolve('') }
          },
          fail: () => resolve('')
        })
      })
    },

    async mentionUser() {
      this.commentText = (this.commentText || '') + '@'
      this.lastCommentText = this.commentText
      this.$nextTick(() => { this.isCommentFocused = true })
      await this.loadMentionUsers()
      this.showMentionPicker = true
    },
    async loadMentionUsers() {
      if (!this.postId || !this.currentUserId) return
      try {
        const res = await api.getMentionUsers(this.postId, this.currentUserId, false)
        this.mentionUserList = (res && (res.code === 200 || res.code === 0) && res.data) ? res.data : []
      } catch (e) {
        this.mentionUserList = []
      }
    },
    selectMentionUser(user) {
      const name = user.userName || '\u7528\u6237'
      let t = this.commentText || ''
      if (t.endsWith('@')) t = t.slice(0, -1) + '@' + name + ' '
      else t = t + name + ' '
      this.commentText = t
      this.lastCommentText = t
      this.showMentionPicker = false
      this.mentionUserList = []
      this.$nextTick(() => { this.isCommentFocused = true })
    },

    onCommentBlur() {
      this.isCommentFocused = false
    },

    showCommentInput() {
      this.$nextTick(() => {
        this.isCommentFocused = true
      })
    },

    async loadComments() {
      try {
        if (!this.postId) {
          return
        }
        this.loadCurrentUserId()
        const response = await api.getPostComments(this.postId, this.currentUserId, false)
        if (!response || !response.data) return
        const list = response.data
        const normalizeReply = (reply) => {
          let replyIsLiked = false
          if (reply.isLiked !== undefined && reply.isLiked !== null) {
            replyIsLiked = reply.isLiked === true || reply.isLiked === 1
          } else if (reply.liked !== undefined && reply.liked !== null) {
            replyIsLiked = reply.liked === true || reply.liked === 1
          }
          return {
            ...reply,
            isLiked: replyIsLiked,
            likeCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
            likesCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
            replies: Array.isArray(reply.replies) ? reply.replies.map(normalizeReply) : []
          }
        }
        this.comments = list.map((comment) => {
          let isLiked = false
          if (comment.isLiked !== undefined && comment.isLiked !== null) {
            isLiked = comment.isLiked === true || comment.isLiked === 1
          } else if (comment.liked !== undefined && comment.liked !== null) {
            isLiked = comment.liked === true || comment.liked === 1
          }
          let replies = Array.isArray(comment.replies) ? comment.replies.map(normalizeReply) : []
          replies = replies.map((reply) => {
            const replyToUserName = reply.replyToUserName || (reply.parentId === comment.id ? comment.userName : (replies.find(r => r.id === reply.parentId) || {}).userName || comment.userName)
            return { ...reply, replyToUserName }
          })
          const replyCount = comment.replyCount ?? comment.reply_count
          const hasRepliesLoaded = !!(comment.replies && comment.replies.length > 0)
          const knownZeroReplies = replyCount !== undefined && replyCount !== null && Number(replyCount) === 0
          return {
            ...comment,
            isLiked,
            likesCount: comment.likeCount || comment.like_count || comment.likesCount || 0,
            likeCount: comment.likeCount || comment.like_count || comment.likesCount || 0,
            replies,
            replyCount: replyCount == null ? undefined : Number(replyCount),
            showAllReplies: false,
            repliesLoaded: hasRepliesLoaded || knownZeroReplies,
            authorLiked: comment.authorLiked || (this.post && this.post.userId === this.currentUserId && isLiked) || false
          }
        })
      } catch (error) {
      }
    },

    async toggleLike() {
      try {
        this.loadCurrentUserId()

        if (!this.currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 500)
          return
        }

        const wasLiked = this.isLiked
        if (wasLiked) {
          await api.unlikePost(this.postId, this.currentUserId)
          this.isLiked = false
          this.likeCount = Math.max(0, this.likeCount - 1)
          uni.showToast({
            title: '已取消点赞',
            icon: 'success'
          })
        } else {
          await api.likePost(this.postId, this.currentUserId)
          this.isLiked = true
          this.likeCount = this.likeCount + 1
          uni.showToast({
            title: '点赞成功',
            icon: 'success'
          })
        }

        if (this.post) {
          this.post.isLiked = this.isLiked
          this.post.likesCount = this.likeCount
        }
        if (this.$store) {
          this.$store.commit('SET_POST_LIKE', {
            postId: this.postId,
            isLiked: this.isLiked,
            likeCount: this.likeCount
          })
        }
        this.$forceUpdate()
      } catch (error) {
        console.error('点赞操作失败:', error)
        const errorMsg = error.message || error.errMsg || '操作失败'
        uni.showToast({
          title: errorMsg.includes('登录') ? '请先登录' : '操作失败',
          icon: 'none',
          duration: 2000
        })
      }
    },

    async toggleFollow() {
      this.loadCurrentUserId()

      if (!this.currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        uni.navigateTo({
          url: '/pages-auth/login'
        })
        return
      }

      if (this.post.userId === this.currentUserId) {
        uni.showToast({
          title: '不能关注自己',
          icon: 'none'
        })
        return
      }

      try {
        const result = await api.followUser(this.post.userId, this.currentUserId)
        if (result && (result.code === 200 || result.code === 0)) {
          this.isFollowing = !this.isFollowing
          uni.showToast({
            title: this.isFollowing ? '关注成功' : '已取消关注',
            icon: 'success'
          })
        }
      } catch (error) {
        console.error('关注操作失败:', error)
        let errorMessage = '操作失败'
        if (error.errMsg && error.errMsg.includes('request:fail')) {
          errorMessage = '网络异常'
        } else if (error.message && error.message.includes('登录')) {
          errorMessage = '请先登录'
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 1500)
        } else if (error.data && error.data.msg) {
          errorMessage = error.data.msg
        } else if (error.message) {
          errorMessage = error.message
        }
        uni.showToast({
          title: errorMessage,
          icon: 'none'
        })
      }
    },

    goToUserProfile() {
      if (!this.post || !this.post.userId) {
        console.error('用户ID无效')
        uni.showToast({
          title: '用户信息异常',
          icon: 'none'
        })
        return
      }

      const targetUserId = this.post.userId
      uni.navigateTo({
        url: `/user/profile?userId=${targetUserId}`
      })
    },

    goToCommentUserProfile(userId) {
      if (!userId) {
        console.error('用户ID不存在')
        uni.showToast({
          title: '用户信息异常',
          icon: 'none'
        })
        return
      }

      uni.navigateTo({
        url: `/user/profile?userId=${userId}`
      })
    },

    async submitComment() {
      if (!this.commentText.trim() && !this.commentImages.length) {
        uni.showToast({
          title: '请输入评论内容',
          icon: 'none'
        })
        return
      }

      try {
        this.currentLocation = ''
        this.getCurrentLocation(true)

        let content = this.commentText.trim()
        if (this.commentImages.length > 0) {
          uni.showLoading({ title: '上传中...', mask: true })
          const urls = []
          for (let i = 0; i < this.commentImages.length; i++) {
            const url = await this.uploadOneImage(this.commentImages[i])
            if (url) urls.push(url)
          }
          uni.hideLoading()
          if (urls.length) {
            const imgStr = urls.map(u => '[图片]' + u).join('\n')
            content = content ? content + '\n' + imgStr : imgStr
          }
        }

        const commentData = {
          content: content,
          userId: this.currentUserId
        }

        if (this.replyingTo) {
          commentData.parentId = this.replyingTo
        }

        if (this.currentLocation) {
          commentData.location = this.currentLocation
        }

        const response = await api.addComment(this.postId, commentData)

        if (response && (response.code === 200 || response.code === 0)) {
          const wasReplying = !!this.replyingTo

          this.commentText = ''
          this.commentImages = []
          this.replyingTo = null
          this.replyingToUserName = ''
          this.commentCount = this.commentCount + 1

          setTimeout(() => {
            this.loadComments()
          }, 500)

          uni.showToast({
            title: wasReplying ? '回复成功' : '评论成功',
            icon: 'success'
          })
        }
      } catch (error) {
        console.error('评论提交失败:', error)
        uni.showToast({
          title: '评论失败',
          icon: 'none'
        })
      }
    },

    // 回复评论
    replyComment(comment) {
      this.loadCurrentUserId()
      if (!this.currentUserId) {
        uni.showToast({
          title: '请先登录后回复',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages-auth/login'
          })
        }, 500)
        return
      }
      
      this.replyingTo = comment.id
      this.replyingToUserName = comment.userName
      
      this.commentText = ''
      
      this.$nextTick(() => {
        this.isCommentFocused = true
        setTimeout(() => {
          uni.pageScrollTo({
            scrollTop: 99999,
            duration: 300
          })
        }, 100)
      })
    },

    cancelReply() {
      this.replyingTo = null
      this.replyingToUserName = ''
      this.commentText = ''
    },

    async expandReplies(comment) {
      const expandId = typeof comment === 'object' ? comment.id : comment
      const commentIndex = this.comments.findIndex(c => c.id === expandId)
      
      if (commentIndex === -1) return
      
      const targetComment = this.comments[commentIndex]
      
      if (targetComment.replies && targetComment.replies.length > 0) {
        this.$set(this.expandedReplies, expandId, true)
        return
      }
      
      if (!targetComment.repliesLoading) {
        this.$set(targetComment, 'repliesLoading', true)
        try {
          const repliesResponse = await api.getCommentReplies(expandId, this.currentUserId, false)
          if (repliesResponse && repliesResponse.data) {
            const raw = repliesResponse.data.map((reply) => {
              let replyIsLiked = false
              if (reply.isLiked !== undefined && reply.isLiked !== null) {
                replyIsLiked = reply.isLiked === true || reply.isLiked === 1
              }
              return {
                ...reply,
                isLiked: replyIsLiked,
                likeCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
                likesCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
                replies: []
              }
            })
            const replies = raw.map((reply) => {
              const replyToUserName = reply.parentId === expandId
                ? targetComment.userName
                : (raw.find(r => r.id === reply.parentId) || {}).userName || targetComment.userName
              return { ...reply, replyToUserName }
            })
            const totalReplyCount = this.countAllReplies(replies)
            this.$set(targetComment, 'replies', replies)
            this.$set(targetComment, 'repliesLoading', false)
            this.$set(targetComment, 'repliesLoaded', true)
            this.$set(targetComment, 'replyCount', totalReplyCount)
            this.$set(this.expandedReplies, expandId, true)
          }
        } catch (err) {
          this.$set(targetComment, 'repliesLoading', false)
        }
      }
    },

    hasCommentReplies(c) {
      if (c.replyCount != null && Number(c.replyCount) === 0) return false
      return true
    },


    getReplyToUserName(ent) {
      if (!ent || !ent.item) return ''
      return ent.item.replyToUserName || (ent.parent && ent.parent.userName) || ''
    },

    countAllReplies(arr) {
      if (!Array.isArray(arr) || !arr.length) return 0
      let n = 0
      for (const r of arr) {
        n += 1
        n += this.countAllReplies(r.replies || [])
      }
      return n
    },

    getFlatReplies(c) {
      if (!c.replies || !c.replies.length) return []
      const expanded = this.expandedReplies[c.id]
      const out = []
      const flatList = []
      for (const r of c.replies) {
        flatList.push({ type: 'reply', item: r, parent: c, isNested: false, _key: 'r-' + (r.id || '') })
        const nest = r.replies || []
        for (const n of nest) {
          flatList.push({ type: 'reply', item: n, parent: r, isNested: true, _key: 'r-' + (n.id || '') })
        }
      }
      const totalReplies = flatList.length

      if (expanded) {
        const visibleCount = this.expandedVisibleCount[c.id] != null ? this.expandedVisibleCount[c.id] : 3
        const showCount = Math.min(visibleCount, totalReplies)
        for (let i = 0; i < showCount; i++) out.push(flatList[i])
        if (totalReplies > 3 && showCount < totalReplies) {
          out.push({ type: 'expand_more_row', commentId: c.id, totalReplies, visibleCount, _key: 'em-' + c.id })
        } else {
          out.push({ type: 'collapse_row', commentId: c.id, _key: 'co-' + c.id })
        }
      } else {
        const firstReply = c.replies[0]
        out.push({ type: 'reply', item: firstReply, parent: c, isNested: false, _key: 'r-' + (firstReply.id || '') })
        const hiddenCount = totalReplies - 1
        if (hiddenCount > 0) {
          out.push({ type: 'expand', key: c.id, count: hiddenCount, _key: 'e-' + c.id })
        }
      }
      return out
    },

    expandMoreReplies(commentId) {
      const c = this.comments.find(co => co.id === commentId)
      if (!c) return
      const total = this.countAllReplies(c.replies || [])
      this.$set(this.expandedVisibleCount, commentId, total)
    },

    collapseReplies(commentId) {
      this.$set(this.expandedReplies, commentId, false)
      this.$set(this.expandedVisibleCount, commentId, 3)
    },

    async likeComment(commentId) {
      try {
        this.loadCurrentUserId()

        if (!this.currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 500)
          return
        }

        let target = this.comments.find(c => c.id === commentId)

        if (!target) {
          for (let c of this.comments) {
            if (c && c.replies && Array.isArray(c.replies)) {
              const reply = c.replies.find(r => r && r.id === commentId)
              if (reply) {
                target = reply
                break
              }
            }
          }
        }

        if (!target) {
          return
        }

        const wasLiked = target.isLiked || false
        if (wasLiked) {
          await api.unlikeComment(commentId, this.currentUserId)
          target.isLiked = false
          target.likeCount = Math.max(0, (target.likeCount || 0) - 1)
          uni.showToast({
            title: '已取消点赞',
            icon: 'success'
          })
        } else {
          await api.likeComment(commentId, this.currentUserId)
          target.isLiked = true
          target.likeCount = (target.likeCount || 0) + 1
          uni.showToast({
            title: '点赞成功',
            icon: 'success'
          })
        }
      } catch (error) {
        const errorMsg = error.message || error.errMsg || '点赞失败'
        uni.showToast({
          title: '点赞失败',
          icon: 'none',
          duration: 2000
        })
      }
    },

    sharePost() {
      uni.showActionSheet({
        itemList: [
          '\u5206\u4eab\u7ed9\u5fae\u4fe1\u597d\u53cb',
          '\u5206\u4eab\u5230\u670b\u53cb\u5708',
          '\u590d\u5236\u94fe\u63a5'
        ],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.showToast({ title: '\u8bf7\u70b9\u53f3\u4e0a\u89d2\u00b7\u00b7\u00b7\u9009\u62e9\u5206\u4eab\u7ed9\u597d\u53cb', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 1) {
            uni.showToast({ title: '\u8bf7\u70b9\u53f3\u4e0a\u89d2\u00b7\u00b7\u00b7\u9009\u62e9\u5206\u4eab\u5230\u670b\u53cb\u5708', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 2) {
            const link = this.getShareLink()
            uni.setClipboardData({
              data: link,
              success: () => uni.showToast({ title: '\u94fe\u63a5\u5df2\u590d\u5236', icon: 'success' }),
              fail: () => uni.showToast({ title: '\u590d\u5236\u5931\u8d25', icon: 'none' })
            })
          }
        }
      })
    },
    getShareLink() {
      const path = `/pages-community/post-detail-image?id=${this.postId || ''}`
      try {
        const base = typeof util !== 'undefined' && util.getApiBaseUrl ? util.getApiBaseUrl().replace(/\/api.*$/, '') : ''
        if (base) return `${base}${path}`
      } catch (e) {}
      return path
    },

    loadMoreComments() {
    },

    async loadUserInfo(userId) {
      try {
        const response = await api.getCurrentUser({ userId: userId })
        if ((response.code === 200 || response.code === 0) && response.data) {
          this.userCans = response.data.points || 0
          if (this.post) {
            this.post.userLevel = response.data.level || response.data.memberLevel || 1
            this.post.userPoints = response.data.points || 0
          }
        }
      } catch (error) {
      }
    },

    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },

    async checkLikeAndFollowStatus() {
      if (!this.currentUserId || !this.postId) {
        return
      }

      if (!this.post || !this.post.userId) {
        return
      }

      try {
        if (this.post.isLiked !== undefined) {
          this.isLiked = this.post.isLiked === true || this.post.isLiked === 1
        }

        try {
          const followResponse = await api.checkFollowStatus(this.currentUserId, this.post.userId)
          if (followResponse && (followResponse.code === 200 || followResponse.code === 0)) {
            this.isFollowing = followResponse.data || false
          }
        } catch (error) {
        }
      } catch (error) {
      }
    }
  }
}
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  width: 100%;
  background-color: #fff;
  position: relative;
}

.xhscroll {
  width: 100%;
  height: 100vh;
  background-color: #fff;
}

.xh-header-user {
  display: flex;
  align-items: center;
  padding: 10rpx 24rpx;
  gap: 12rpx;
  background: #fff;
}

.xh-header-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.xh-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.xh-header-name {
  font-size: 26rpx;
  color: #333;
}

.xh-header-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.xh-tag-level {
  font-size: 22rpx;
  color: #333;
  background: #FFD700;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.xh-tag-divider {
  font-size: 24rpx;
  color: #ccc;
}

.xh-tag-points {
  font-size: 24rpx;
  color: #666;
}

.xh-header-follow {
  padding: 8rpx 20rpx;
  background: #FFD700;
  border-radius: 30rpx;
  flex-shrink: 0;
}

.xh-header-follow-txt {
  font-size: 24rpx;
  color: #333;
}

.xh-header-more {
  padding: 8rpx 16rpx;
  flex-shrink: 0;
}

.xh-header-more-icon {
  font-size: 36rpx;
  color: #333;
  line-height: 1;
}

.xh-card-wrap {
  width: 100%;
  margin: 0 0 20rpx 0;
  padding: 0;
  background: #fff;
  border-radius: 0;
  overflow: hidden;
  position: relative;
  box-shadow: none;
  box-sizing: border-box;
}

.xh-card-swiper {
  width: 100%;
  min-height: 300rpx;
  box-sizing: border-box;
}

.xh-card-img {
  width: 100%;
  display: block;
  vertical-align: top;
  background: #f5f5f5;
  box-sizing: border-box;
}

.xh-card-indicator {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background: rgba(0,0,0,0.5);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  z-index: 10;
}

.xh-card-page {
  font-size: 24rpx;
  color: #fff;
}

.xh-card-wrap.xh-empty {
  height: 400rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.xh-empty-txt {
  font-size: 28rpx;
  color: #999;
}

.xh-title {
  padding: 0 30rpx 20rpx;
  background: #fff;
}

.xh-title-txt {
  font-size: 36rpx;
  color: #333;
  line-height: 1.5;
}

.xh-content {
  padding: 0 30rpx 30rpx;
  background: #fff;
}

.xh-content-txt {
  font-size: 30rpx;
  color: #333;
  line-height: 1.8;
  word-break: break-word;
}

.xh-bottom-actions {
  position: fixed !important;
  bottom: 0 !important;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  padding: 14rpx 24rpx 18rpx 24rpx !important;
  margin: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8rpx;
  z-index: 100;
  box-sizing: border-box;
}

.xh-reply-hint-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4rpx;
}

.xh-reply-hint-text {
  font-size: 22rpx;
  color: #666;
}

.xh-reply-cancel {
  font-size: 22rpx;
  color: #999;
  padding: 6rpx 12rpx;
}

.xh-bottom-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.xh-bottom-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 64rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding-left: 22rpx;
  padding-right: 10rpx;
  box-sizing: border-box;
}

.xh-bottom-input-tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10rpx;
  flex-shrink: 0;
}

.xh-bottom-tool-icon {
  width: 36rpx;
  height: 36rpx;
  flex-shrink: 0;
}

.xh-bottom-tool-at {
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
  padding: 0 2rpx;
  line-height: 1;
}

.xh-comment-images-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 12rpx;
  padding: 0 4rpx;
}

.xh-comment-img-item {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
}

.xh-comment-img-thumb {
  width: 100%;
  height: 100%;
}

.xh-comment-img-del {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
}

.xh-bottom-input {
  flex: 1;
  min-width: 0;
  height: 64rpx;
  background: transparent;
  font-size: 26rpx;
  color: #333;
}

.xh-bottom-icons {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex-shrink: 0;
}

.xh-bottom-icon {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.xh-icon-img {
  width: 36rpx;
  height: 36rpx;
}

.xh-icon-img.xh-icon-active {
}

.xh-collect-icon.xh-icon-active {
  filter: none;
}

.xh-icon-num {
  font-size: 22rpx;
  color: #666;
}

.xh-comments-section {
  padding: 14rpx 0;
  background: #fff;
  width: 100%;
  box-sizing: border-box;
}

.xh-comments-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 14rpx;
}

.xh-comments-bar {
  width: 4rpx;
  height: 20rpx;
  background: #ff2442;
  border-radius: 2rpx;
}

.xh-comments-title {
  font-size: 26rpx;
  color: #333;
}

.xh-comments-list {
  display: block;
}

.xh-comment-item {
  margin-bottom: 20rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
}

.xh-comment-main-row {
  display: flex;
  gap: 12rpx;
  position: relative;
}

.xh-comment-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.xh-comment-content-wrapper {
  flex: 1;
  position: relative;
  padding-right: 96rpx;
}

.xh-comment-header {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 6rpx;
}

.xh-comment-name {
  font-size: 24rpx;
  color: #999;
}

.xh-comment-text {
  font-size: 24rpx;
  color: #333;
  line-height: 1.5;
  word-break: break-word;
  display: block;
  margin-bottom: 6rpx;
}

.xh-comment-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 6rpx;
  justify-content: flex-start;
}

.xh-comment-date-loc {
  font-size: 22rpx;
  color: #999;
}

.xh-comment-reply-btn {
  font-size: 22rpx;
  color: #999;
  cursor: pointer;
  padding: 4rpx 8rpx;
  user-select: none;
}

.xh-author-liked {
  text-align: center;
  margin: 6rpx 0;
}

.xh-author-liked-text {
  font-size: 20rpx;
  color: #999;
  text-decoration: underline;
}

.xh-comment-actions-right {
  position: absolute;
  right: 24rpx;
  bottom: 0;
  top: auto;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  min-height: 48rpx;
  z-index: 10;
}

.xh-comment-action-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  min-height: 48rpx;
  gap: 4rpx;
}

.xh-action-icon {
  width: 24rpx;
  height: 24rpx;
  transition: transform 0.2s ease;
}

.xh-comment-action-item:active .xh-action-icon {
  transform: scale(0.88);
}

.xh-action-icon.xh-action-liked {
  filter: invert(48%) sepia(79%) saturate(2476%) hue-rotate(325deg) brightness(100%) contrast(97%);
}

.xh-action-num.xh-num-liked {
  color: #ff6b81;
}

.xh-action-num {
  font-size: 22rpx;
  color: #999;
  margin-left: 0;
}

.xh-emoji-icon {
  width: 24rpx;
  height: 24rpx;
}

.xh-reply-level1 {
  margin-top: 12rpx;
  padding-left: 68rpx;
}

.xh-reply-item {
  display: flex;
  gap: 10rpx;
  margin-bottom: 14rpx;
  position: relative;
}

.xh-reply-avatar {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.xh-reply-content-wrapper {
  flex: 1;
  position: relative;
  padding-right: 80rpx;
}

.xh-reply-header {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 4rpx;
}

.xh-reply-name {
  font-size: 22rpx;
  color: #999;
}

.xh-reply-arrow {
  font-size: 20rpx;
  color: #999;
  margin: 0 4rpx;
  display: inline-block;
  transform: rotate(90deg);
}

.xh-reply-to-name {
  font-size: 22rpx;
  color: #999;
}

.xh-author-tag {
  background: #fff0f0;
  padding: 2rpx 10rpx;
  border-radius: 16rpx;
  margin-left: 6rpx;
}

.xh-author-tag-text {
  font-size: 20rpx;
  color: #ff6b81;
}

.xh-reply-text {
  font-size: 22rpx;
  color: #333;
  line-height: 1.45;
  word-break: break-word;
  display: block;
  margin-bottom: 4rpx;
}

.xh-reply-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 6rpx;
  justify-content: flex-start;
}

.xh-reply-date-loc {
  font-size: 20rpx;
  color: #999;
}

.xh-reply-reply-btn {
  font-size: 20rpx;
  color: #999;
  cursor: pointer;
}

.xh-reply-actions-right {
  position: absolute;
  right: 0;
  bottom: 0;
  top: 0;
  left: auto;
  width: 96rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  z-index: 20;
  pointer-events: auto;
}

.xh-reply-action-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  min-width: 96rpx;
  min-height: 64rpx;
  padding: 12rpx 0;
  gap: 4rpx;
  pointer-events: auto;
}

.xh-reply-action-item:active .xh-action-icon {
  transform: scale(0.88);
}

.xh-reply-actions-right .xh-action-icon {
  width: 22rpx;
  height: 22rpx;
}

.xh-reply-actions-right .xh-action-num {
  font-size: 20rpx;
  color: #999;
  margin-left: 0;
}

.xh-reply-level2 {
  margin-top: 8rpx;
  padding-left: 0;
}

.xh-nested-reply {
  display: flex;
  gap: 10rpx;
  margin-bottom: 12rpx;
  position: relative;
  padding-left: 52rpx;
}

.xh-nested-avatar {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.xh-nested-content {
  flex: 1;
  position: relative;
  padding-right: 80rpx;
}

.xh-nested-header {
  display: flex;
  align-items: center;
  gap: 5rpx;
  margin-bottom: 3rpx;
}

.xh-nested-name {
  font-size: 20rpx;
  color: #333;
}

.xh-nested-text {
  font-size: 22rpx;
  color: #333;
  line-height: 1.4;
  word-break: break-word;
  display: block;
  margin-bottom: 3rpx;
}

.xh-nested-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 4rpx;
  justify-content: flex-start;
}

.xh-nested-date-loc {
  font-size: 18rpx;
  color: #999;
}

.xh-nested-reply-btn {
  font-size: 18rpx;
  color: #999;
  cursor: pointer;
  padding: 4rpx 8rpx;
  user-select: none;
}

.xh-nested-actions-right {
  position: absolute;
  right: 0;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  width: 60rpx;
  z-index: 10;
}

.xh-nested-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rpx;
}

.xh-expand-same {
  margin-top: 8rpx;
  padding-left: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: nowrap;
}

.xh-expand-line {
  width: 48rpx;
  height: 2rpx;
  background: #e0e0e0;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.xh-expand-text {
  font-size: 22rpx;
  color: #999;
  letter-spacing: 1rpx;
}

.xh-expand-arrow {
  font-size: 20rpx;
  color: #999;
  margin-left: 4rpx;
}

.xh-expand-arrow-up {
  font-size: 20rpx;
  color: #999;
  margin-left: 4rpx;
}

.xh-expand-more-row {
  width: 100%;
  justify-content: flex-start;
}

.xh-expand-gap {
  flex: 1;
  min-width: 2em;
  max-width: 64rpx;
}

.xh-comments-empty {
  text-align: center;
  padding: 40rpx 0;
}

.xh-comments-empty-txt {
  font-size: 24rpx;
  color: #999;
}

.xh-bottom-pad {
  height: 140rpx;
}

.douyin-top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  background: linear-gradient(to bottom, rgba(0,0,0,0.5), transparent);
  z-index: 300;
}

.douyin-nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.douyin-back-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

.douyin-nav-title {
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
}

.douyin-nav-actions {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.douyin-nav-icon {
  font-size: 40rpx;
  color: #fff;
}

.douyin-video-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.douyin-video-player {
  width: 100%;
  height: 100%;
  background-color: #000;
  object-fit: contain;
  display: block;
}

.douyin-video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 30rpx 50rpx;
  border-radius: 20rpx;
}

.douyin-loading-text {
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
}

.douyin-video-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background-color: rgba(255, 0, 0, 0.85);
  padding: 30rpx 50rpx;
  border-radius: 20rpx;
  max-width: 85%;
  text-align: center;
}

.douyin-error-text {
  color: #fff;
  font-size: 26rpx;
  line-height: 1.5;
  font-weight: 500;
}

.douyin-video-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background-color: rgba(0, 0, 0, 0.75);
  padding: 50rpx 70rpx;
  border-radius: 24rpx;
  text-align: center;
}

.douyin-placeholder-text {
  color: #fff;
  font-size: 34rpx;
  display: block;
  margin-bottom: 24rpx;
  font-weight: 500;
}

.douyin-placeholder-tip {
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
  display: block;
}

.video-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 40rpx 60rpx;
  border-radius: 20rpx;
  text-align: center;
}

.placeholder-text {
  color: #fff;
  font-size: 32rpx;
  display: block;
  margin-bottom: 20rpx;
}

.placeholder-text-small {
  color: rgba(255, 255, 255, 0.7);
  font-size: 24rpx;
  display: block;
}

.ip-settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ip-settings-content {
  width: 80%;
  max-width: 600rpx;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.ip-settings-header {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  background-color: #f5f5f5;
  border-bottom: 1rpx solid #eee;
}

.ip-settings-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
}

.ip-settings-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ip-settings-body {
  padding: 30rpx;
}

.ip-tips {
  background-color: #f0f7ff;
  padding: 20rpx;
  border-radius: 10rpx;
  margin-bottom: 30rpx;
}

.tip-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  display: block;
  margin-bottom: 16rpx;
}

.tip-item {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
  display: block;
  margin-bottom: 12rpx;
}

.tip-highlight {
  color: #007AFF;
  font-weight: 600;
  background-color: #E6F3FF;
  padding: 12rpx;
  border-radius: 8rpx;
  margin-top: 8rpx;
}

.tip-important {
  color: #FF3B30;
  font-weight: 600;
  background-color: #FFEBEE;
  padding: 12rpx;
  border-radius: 8rpx;
  margin-top: 8rpx;
  margin-bottom: 8rpx;
}

.ip-input-group {
  margin-bottom: 20rpx;
}

.ip-label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.ip-input {
  width: 100%;
  height: 80rpx;
  background-color: #f5f5f5;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  border: 1rpx solid #ddd;
}

.ip-current {
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 10rpx;
  margin-bottom: 30rpx;
}

.ip-current-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 10rpx;
}

.ip-current-value {
  font-size: 28rpx;
  color: #007AFF;
  font-weight: 600;
}

.ip-buttons {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.ip-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.ip-btn-cancel {
  background-color: #f5f5f5;
  color: #666;
}

.ip-btn-save {
  background-color: #007AFF;
  color: #fff;
}

.ip-btn-test {
  background-color: #34C759;
  color: #fff;
}

.douyin-image-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  overflow: hidden;
}

.douyin-image-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  overflow: hidden;
}

.douyin-image-carousel {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.douyin-image-swiper {
  width: 100%;
  height: 100%;
}

.douyin-carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.douyin-empty-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
}

.douyin-empty-text {
  font-size: 32rpx;
  color: #999;
}

.douyin-right-bar {
  position: absolute;
  right: 20rpx;
  bottom: 300rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 50rpx;
  z-index: 200;
}

.douyin-user-avatar {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.douyin-avatar-img {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.4);
}

.douyin-follow-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background-color: #ff4757;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -24rpx;
  border: 4rpx solid #000;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.douyin-follow-icon {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

.douyin-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.douyin-action-icon {
  width: 64rpx;
  height: 64rpx;
  filter: brightness(0) invert(1);
  transition: all 0.3s;
}

.douyin-action-icon.douyin-action-active {
  filter: none;
}

.douyin-action-count {
  font-size: 24rpx;
  color: #fff;
  text-align: center;
  font-weight: 500;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.douyin-bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 140rpx;
  padding: 40rpx 30rpx 60rpx;
  background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.6) 50%, transparent 100%);
  z-index: 200;
}

.douyin-user-info {
  margin-bottom: 16rpx;
}

.douyin-username {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.6);
}

.douyin-content {
  margin-bottom: 16rpx;
  max-width: 100%;
}

.douyin-content-text {
  font-size: 28rpx;
  color: #fff;
  line-height: 1.6;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.6);
  word-break: break-word;
}

.douyin-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.douyin-date {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.douyin-comment-bar {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255,255,255,0.15);
}

.douyin-reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.douyin-reply-hint-text {
  font-size: 24rpx;
  color: rgba(255,255,255,0.9);
}

.douyin-cancel-reply {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  padding: 4rpx 12rpx;
}

.douyin-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.douyin-comment-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  color: #fff;
  background-color: rgba(255,255,255,0.2);
  border-radius: 36rpx;
  padding: 0 28rpx;
  border: none;
}

.douyin-comment-input::placeholder {
  color: rgba(255,255,255,0.6);
}

.douyin-send-btn {
  padding: 0 32rpx;
  height: 72rpx;
  line-height: 72rpx;
  background-color: #ff4757;
  border-radius: 36rpx;
  opacity: 0.6;
  flex-shrink: 0;
}

.douyin-send-btn.active {
  opacity: 1;
}

.douyin-send-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 500;
}

.post-text {
  font-size: 28rpx;
  color: #fff;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  overflow: hidden;
}

.post-meta-bottom {
  margin-top: 8rpx;
}

.post-date-bottom {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.user-level {
  font-size: 22rpx;
  color: #666;
}

.user-divider {
  font-size: 22rpx;
  color: #ddd;
}

.user-cans {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.can-icon {
  width: 24rpx;
  height: 24rpx;
  flex-shrink: 0;
}

.can-amount {
  font-size: 22rpx;
  color: #666;
}

.follow-btn-container {
  display: flex;
  align-items: center;
}

.follow-btn {
  background-color: #ffd700;
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
}

.follow-text {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

.comment-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
}

.comment-panel-content {
  width: 100%;
  height: 80%;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.comment-panel-header {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.comment-panel-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
}

.comment-panel-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-panel-scroll {
  flex: 1;
  overflow-y: auto;
}

.comment-input-panel {
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-top: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}
.comment-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.comment-input-inner {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 36rpx;
  padding: 12rpx 20rpx 12rpx 24rpx;
  min-height: 72rpx;
}
.comment-input-display {
  position: absolute;
  left: 24rpx;
  right: 100rpx;
  top: 12rpx;
  bottom: 12rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  overflow: hidden;
  pointer-events: none;
}
.comment-input-display-text, .comment-input-placeholder {
  font-size: 28rpx;
  color: #333;
}
.comment-input-placeholder { color: #999; }
.comment-input-inner .comment-input {
  flex: 1;
  font-size: 28rpx;
  height: 48rpx;
  min-width: 0;
  padding: 0;
  color: transparent;
  caret-color: #333;
}
.comment-input-real { position: relative; z-index: 1; }
.comment-input-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-left: 8rpx;
}
.input-action-icon {
  width: 40rpx;
  height: 40rpx;
  font-size: 32rpx;
  color: #999;
}
.comment-images-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;
}
.comment-image-item {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
}
.comment-image-thumb { width: 100%; height: 100%; }
.comment-image-del {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
}
.comment-text-inline { font-size: 26rpx; color: #333; }
.comment-text-inline.comment-mention { color: #1e6fff; }
.comment-image-inline {
  max-width: 280rpx;
  max-height: 280rpx;
  border-radius: 8rpx;
  margin-top: 8rpx;
  display: block;
}
.mention-picker-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3);
  z-index: 1998;
}
.mention-picker {
  position: fixed;
  left: 30rpx; right: 30rpx;
  bottom: 200rpx;
  max-height: 400rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.15);
  z-index: 1999;
  overflow: hidden;
}
.mention-picker-title {
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  color: #999;
  border-bottom: 1rpx solid #f0f0f0;
}
.mention-picker-list { flex: 1; max-height: 340rpx; padding: 12rpx 0; }
.mention-picker-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  gap: 20rpx;
}
.mention-picker-item:active { background: #f5f5f5; }
.mention-picker-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
}
.mention-picker-name { font-size: 28rpx; color: #333; }
.mention-picker-empty {
  padding: 40rpx;
  text-align: center;
  font-size: 26rpx;
  color: #999;
}



.comment-list {
  margin-bottom: 24rpx;
}

.comment-item {
  display: flex;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.comment-avatar {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  flex-shrink: 0;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: opacity 0.2s;
}

.comment-avatar:active {
  opacity: 0.7;
}

.comment-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.comment-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.comment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.comment-user-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 4rpx;
}

.comment-user {
  font-size: 24rpx;
  color: #999;
  font-weight: 400;
}

.author-tag {
  font-size: 18rpx;
  color: #007AFF;
  background-color: #E6F3FF;
  padding: 2rpx 6rpx;
  border-radius: 4rpx;
  line-height: 1.2;
}

.comment-text {
  font-size: 26rpx;
  line-height: 1.5;
  color: #333;
  margin-bottom: 6rpx;
  word-break: break-all;
}

.comment-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.comment-meta-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
}

.comment-location {
  font-size: 22rpx;
  color: #999;
}

.comment-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}

.comment-reply-btn {
  padding: 0;
}

.comment-reply-text {
  font-size: 22rpx;
  color: #999;
}

.comment-like {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}

.like-icon {
  width: 28rpx;
  height: 28rpx;
  font-size: 0;
}

.like-count {
  font-size: 18rpx;
  color: #999;
  line-height: 1;
}


.view-more-comments {
  text-align: center;
  padding: 20rpx 0;
}

.view-more-text {
  font-size: 28rpx;
  color: #007AFF;
}

.replies-section {
  margin-top: 16rpx;
  margin-left: 0;
  padding-left: 0;
}

.reply-item {
  display: flex;
  margin-bottom: 20rpx;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 10rpx;
  margin-left: 0;
  flex-shrink: 0;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: opacity 0.2s;
}

.reply-avatar:active {
  opacity: 0.7;
}

.reply-content-wrapper {
  flex: 1;
}

.reply-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.reply-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.reply-user-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 6rpx;
  flex-wrap: wrap;
}

.reply-to-indicator {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-left: 4rpx;
}

.reply-arrow {
  font-size: 20rpx;
  color: #999;
  transform: scale(0.8);
}

.reply-to-user {
  font-size: 22rpx;
  color: #999;
}

.reply-user {
  font-size: 22rpx;
  color: #999;
  font-weight: 400;
}

.reply-text {
  font-size: 24rpx;
  line-height: 1.5;
  color: #333;
  margin-bottom: 6rpx;
  word-break: break-all;
}

.reply-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6rpx;
}

.reply-meta-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.reply-time {
  font-size: 20rpx;
  color: #999;
}

.reply-location {
  font-size: 20rpx;
  color: #999;
}

.reply-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}

.reply-reply-btn {
  padding: 0;
}

.reply-like {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}

.expand-replies {
  margin-top: 12rpx;
  padding: 8rpx 0;
  padding-left: 60rpx;
}

.expand-text {
  font-size: 24rpx;
  color: #007AFF;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 16rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  margin-bottom: 8rpx;
}

.reply-hint-text {
  font-size: 22rpx;
  color: #007AFF;
}

.cancel-reply {
  font-size: 22rpx;
  color: #999;
  padding: 4rpx 8rpx;
}

.comment-input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.comment-input {
  flex: 1;
  height: 70rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #f5f5f5;
  border-radius: 40rpx;
  padding: 0 24rpx;
  border: none;
  outline: none;
}

.send-btn {
  background-color: #ff6b6b;
  color: #fff;
  padding: 12rpx 32rpx;
  border-radius: 40rpx;
  opacity: 0.5;
  transition: opacity 0.3s;
  flex-shrink: 0;
}

.send-btn.active {
  opacity: 1;
}

.send-text {
  font-size: 26rpx;
  color: #fff;
}
</style>
<template>
  <view class="post-detail-page">
    <!-- 全屏视频 + 所有UI叠在视频上（仅在有视频时显示） -->
    <view class="douyin-video-container" v-if="hasVideo">
      <!-- 简单的返回按钮：悬浮在视频左上角 -->
      <view class="douyin-back-button" @tap="goBack">
        <text class="douyin-back-icon">‹</text>
      </view>
      <!-- 自己的帖子显示更多（删除） -->
      <view class="douyin-more-button" v-if="post.userId === currentUserId" @tap="showPostActionSheet">
        <text class="douyin-more-icon">⋯</text>
      </view>

      <!-- 视频播放器：可滑动时用垂直 swiper（抖音式上下滑切换） -->
      <view class="douyin-video-wrapper" v-if="hasVideo">
        <swiper
          v-if="canSwipe"
          class="douyin-video-swiper"
          vertical
          :current="currentIndex"
          :duration="300"
          @change="onVideoSwiperChange"
        >
          <swiper-item v-for="(vid, i) in videoIds" :key="vid">
            <view class="douyin-swiper-page">
              <template v-if="i === currentIndex">
                <video
                  id="video-player"
                  class="douyin-video-player"
                  :key="videoKey"
                  :src="currentVideoUrl || ''"
                  :poster="currentVideoCover || ''"
                  v-if="currentVideoUrl"
                  :controls="false"
                  :show-center-play-btn="false"
                  :enable-play-gesture="false"
                  :autoplay="true"
                  :loop="false"
                  :muted="false"
                  :enable-progress-gesture="true"
                  :show-fullscreen-btn="false"
                  :show-play-btn="false"
                  :initial-time="0"
                  object-fit="contain"
                  :page-gesture="false"
                  :vslide-gesture="false"
                  :enable-progress="true"
                  :http-cache="true"
                  :play-strategy="0"
                  :enable-metadata="true"
                  @tap="onVideoSurfaceTap"
                  @play="onVideoPlay"
                  @pause="onVideoPause"
                  @ended="onVideoEnded"
                  @error="onVideoError"
                  @timeupdate="onVideoTimeUpdate"
                  @waiting="onVideoWaiting"
                  @progress="onVideoProgress"
                  @loadedmetadata="onVideoLoadedMetadata"
                  @loadstart="onVideoLoadStart"
                ></video>
                <view class="douyin-video-loading douyin-video-loading-mini" v-if="videoLoading && !currentVideoCover">
                  <text class="douyin-loading-text">加载中...</text>
                </view>
                <view class="douyin-video-loading douyin-video-loading-overlay" v-else-if="videoLoading">
                  <text class="douyin-loading-text douyin-loading-overlay-text">缓冲中...</text>
                </view>
                <view class="douyin-video-error" v-if="videoError">
                  <text class="douyin-error-text">{{ videoError }}</text>
                </view>
                <view class="douyin-video-placeholder" v-if="!currentVideoUrl && hasVideo && !videoLoading && !videoError">
                  <text class="douyin-placeholder-text">视频加载中...</text>
                </view>
                <view class="douyin-image-carousel" v-if="videoError && postImages && postImages.length > 0">
                  <swiper class="douyin-image-swiper" :indicator-dots="postImages.length > 1" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
                    <swiper-item v-for="(image, idx) in postImages" :key="idx" @click="previewImage(idx)">
                      <image class="douyin-carousel-image" :src="getImageUrl(image)" mode="aspectFill"></image>
                    </swiper-item>
                  </swiper>
                </view>
              </template>
              <view v-else class="douyin-swiper-placeholder">
                <text class="douyin-swiper-placeholder-txt">上滑下一个 · 下滑上一个</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
        <template v-else>
          <video
            id="video-player"
            class="douyin-video-player"
            :key="videoKey"
            :src="currentVideoUrl || ''"
            :poster="currentVideoCover || ''"
            v-if="currentVideoUrl"
            :controls="false"
            :show-center-play-btn="false"
            :enable-play-gesture="false"
            :autoplay="true"
            :loop="false"
            :muted="false"
            :enable-progress-gesture="true"
            :show-fullscreen-btn="false"
            :show-play-btn="false"
            :initial-time="0"
            object-fit="contain"
            :page-gesture="false"
            :vslide-gesture="false"
            :enable-progress="true"
            :http-cache="true"
            :play-strategy="0"
            :enable-metadata="true"
            @tap="onVideoSurfaceTap"
            @play="onVideoPlay"
            @pause="onVideoPause"
            @ended="onVideoEnded"
            @error="onVideoError"
            @timeupdate="onVideoTimeUpdate"
            @waiting="onVideoWaiting"
            @progress="onVideoProgress"
            @loadedmetadata="onVideoLoadedMetadata"
            @loadstart="onVideoLoadStart"
          ></video>
          <view class="douyin-video-loading douyin-video-loading-mini" v-if="videoLoading && !currentVideoCover">
            <text class="douyin-loading-text">加载中...</text>
          </view>
          <view class="douyin-video-loading douyin-video-loading-overlay" v-else-if="videoLoading">
            <text class="douyin-loading-text douyin-loading-overlay-text">缓冲中...</text>
          </view>
          <view class="douyin-video-error" v-if="videoError">
            <text class="douyin-error-text">{{ videoError }}</text>
          </view>
          <view class="douyin-video-placeholder" v-if="!currentVideoUrl && hasVideo && !videoLoading && !videoError">
            <text class="douyin-placeholder-text">视频加载中...</text>
          </view>
          <view class="douyin-image-carousel" v-if="videoError && postImages && postImages.length > 0">
            <swiper class="douyin-image-swiper" :indicator-dots="postImages.length > 1" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
              <swiper-item v-for="(image, index) in postImages" :key="index" @click="previewImage(index)">
                <image class="douyin-carousel-image" :src="getImageUrl(image)" mode="aspectFill"></image>
              </swiper-item>
            </swiper>
          </view>
        </template>
      </view>

      <!-- 单击播/停、暂停时中央三角、底部进度条（盖在视频区域，z-index 低于返回/右侧栏） -->
      <view v-if="hasVideo && currentVideoUrl && !videoError" class="douyin-video-custom-ui">
        <view v-if="videoPaused" class="douyin-custom-center-play" @tap.stop="resumeVideoPlay">
          <view class="douyin-custom-center-play-inner">
            <text class="douyin-custom-center-play-tri">▶</text>
          </view>
        </view>
        <view class="douyin-video-progress-row">
          <view class="douyin-video-progress-track">
            <view class="douyin-video-progress-fill" :style="{ width: videoProgressPercent + '%' }"></view>
          </view>
          <text class="douyin-video-progress-meta">{{ videoTimeLabel }}</text>
        </view>
      </view>
      
    <!-- 图片展示容器（如果没有视频，使用独立的图片容器） -->
    <view class="douyin-image-container" v-if="!hasVideo">
      <!-- 简单的返回按钮：悬浮在图片左上角 -->
      <view class="douyin-back-button" @tap="goBack">
        <text class="douyin-back-icon">‹</text>
      </view>
      <view class="douyin-more-button" v-if="post.userId === currentUserId" @tap="showPostActionSheet">
        <text class="douyin-more-icon">⋯</text>
      </view>

      <!-- 图片轮播 -->
      <view class="douyin-image-carousel" v-if="postImages && postImages.length > 0">
        <swiper class="douyin-image-swiper" :indicator-dots="postImages.length > 1" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
          <swiper-item v-for="(image, index) in postImages" :key="index" @click="previewImage(index)">
            <image class="douyin-carousel-image" :src="getImageUrl(image)" mode="aspectFill"></image>
          </swiper-item>
        </swiper>
      </view>
      <!-- 如果没有图片也没有视频，显示占位符 -->
      <view class="douyin-empty-content" v-else>
        <text class="douyin-empty-text">暂无内容</text>
      </view>
      
      <!-- 右侧互动按钮区域（抖音风格） -->
      <view class="douyin-right-bar">
        <!-- 用户头像和关注 -->
        <view class="douyin-user-avatar" @tap="goToUserProfile">
          <image class="douyin-avatar-img" :src="getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
          <view class="douyin-follow-btn" v-if="post.userId !== currentUserId" @click.stop="toggleFollow">
            <text class="douyin-follow-icon">{{ isFollowing ? '✓' : '+' }}</text>
          </view>
        </view>

        <!-- 点赞 -->
        <view class="douyin-action-item" @tap="toggleLike">
          <image 
            class="douyin-action-icon" 
            :class="{ 'douyin-action-active': isLiked }"
            :src="isLiked ? '/static/images/视频点赞后.png' : '/static/images/视频点赞前.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(likeCount) }}</text>
        </view>

        <!-- 评论 -->
        <view class="douyin-action-item" @tap="showCommentPanel">
          <image 
            class="douyin-action-icon" 
            :src="'/static/images/视频评论.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(commentCount) }}</text>
        </view>

        <!-- 收藏 -->
        <view class="douyin-action-item" @tap="toggleCollect">
          <image 
            class="douyin-action-icon" 
            :class="{ 'douyin-action-active': isCollected }"
            :src="isCollected ? '/static/images/视频已收藏-.png' : '/static/images/视频-收藏.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(collectCount) }}</text>
        </view>

        <!-- 分享 -->
        <view class="douyin-action-item" @tap="sharePost">
          <image 
            class="douyin-action-icon" 
            :src="'/static/images/视频分享.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">分享</text>
        </view>
      </view>

      <!-- 底部用户信息和标题（抖音风格） -->
      <view class="douyin-bottom-bar">
        <view class="douyin-user-row" @tap="goToUserProfile">
          <image class="douyin-user-row-avatar" :src="getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
          <text class="douyin-username">@{{ post.userName || '用户' }}</text>
        </view>
        <view class="douyin-content" v-if="post.content">
          <text class="douyin-content-text">{{ post.content }}</text>
        </view>
      </view>
    </view>

      <!-- 右侧互动按钮区域（抖音风格） -->
      <view class="douyin-right-bar">
        <!-- 用户头像和关注 -->
        <view class="douyin-user-avatar" @tap="goToUserProfile">
          <image class="douyin-avatar-img" :src="getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
          <view class="douyin-follow-btn" v-if="post.userId !== currentUserId" @click.stop="toggleFollow">
            <text class="douyin-follow-icon">{{ isFollowing ? '✓' : '+' }}</text>
          </view>
        </view>

        <!-- 点赞 -->
        <view class="douyin-action-item" @tap="toggleLike">
          <image 
            class="douyin-action-icon" 
            :class="{ 'douyin-action-active': isLiked }"
            :src="isLiked ? '/static/images/视频点赞后.png' : '/static/images/视频点赞前.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(likeCount) }}</text>
        </view>

        <!-- 评论 -->
        <view class="douyin-action-item" @tap="showCommentPanel">
          <image 
            class="douyin-action-icon" 
            :src="'/static/images/视频评论.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(commentCount) }}</text>
        </view>

        <!-- 收藏 -->
        <view class="douyin-action-item" @tap="toggleCollect">
          <image 
            class="douyin-action-icon" 
            :class="{ 'douyin-action-active': isCollected }"
            :src="isCollected ? '/static/images/视频已收藏-.png' : '/static/images/视频-收藏.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">{{ formatCount(collectCount) }}</text>
        </view>

        <!-- 分享 -->
        <view class="douyin-action-item" @tap="sharePost">
          <image 
            class="douyin-action-icon" 
            :src="'/static/images/视频分享.png'"
            mode="aspectFit"
          ></image>
          <text class="douyin-action-count">分享</text>
        </view>
      </view>

      <!-- 底部用户信息和标题（抖音风格）；上移避免与底部进度条重叠 -->
      <view class="douyin-bottom-bar douyin-bottom-bar--with-progress">
        <view class="douyin-user-row" @tap="goToUserProfile">
          <image class="douyin-user-row-avatar" :src="getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill"></image>
          <text class="douyin-username">@{{ post.userName || '用户' }}</text>
        </view>
        <view class="douyin-content" v-if="post.content">
          <text class="douyin-content-text">{{ post.content }}</text>
        </view>
      </view>
    </view>

    <!-- 评论区面板 -->
    <view class="comment-panel" v-if="showComments" @tap="hideCommentPanel">
      <view class="comment-panel-content" @tap.stop>
        <view class="comment-panel-header">
          <text class="comment-count-text">{{ commentCount }}条评论</text>
          <text class="comment-panel-close" @tap="hideCommentPanel">×</text>
        </view>
        
        <scroll-view scroll-y class="comment-panel-scroll" @scrolltolower="onScrollToLower" lower-threshold="100">
          <view class="comment-list">
            <view class="comment-item" v-for="comment in comments" :key="comment.id" @longpress="onCommentLongPress(comment)">
              <view class="comment-item-main">
                <image class="comment-avatar" :src="getImageUrl(comment.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" @click.stop="goToCommentUserProfile(comment.userId)"></image>
                <view class="comment-content-wrapper">
                  <view class="comment-content">
                    <view class="comment-info">
                      <view class="comment-user-row">
                        <text class="comment-user">{{ comment.userName }}</text>
                        <text class="author-tag" v-if="comment.userId === post.userId">作者</text>
                      </view>
                      <view class="comment-text">
                        <block v-for="(part, pidx) in parseCommentContent(comment.content)" :key="pidx">
                          <text
                            v-if="part.type === 'text'"
                            class="comment-text-inline"
                          >{{ part.text }}</text>
                          <text
                            v-else-if="part.type === 'mention'"
                            class="comment-text-inline comment-mention"
                          >{{ part.text }}</text>
                          <image v-else-if="part.type === 'emoji'" class="comment-emoji-inline" :src="part.path" mode="aspectFit" />
                          <image v-else-if="part.type === 'image'" class="comment-image-inline" :src="getImageUrl(part.url)" mode="widthFix" />
                        </block>
                      </view>
                      <view class="comment-meta-row">
                        <view class="comment-meta-left">
                          <text class="comment-time">{{ formatCommentTime(comment.createTime) }}</text>
                          <text class="comment-location" v-if="comment.location">·{{ comment.location }}</text>
                          <view class="comment-reply-btn" @click.stop="replyComment(comment)">
                            <text class="comment-reply-text">回复</text>
                          </view>
                        </view>
                        <view class="comment-like" @click.stop="toggleCommentLike(comment)">
                          <image 
                            class="like-icon" 
                            :class="{ 'liked': comment.isLiked }"
                            :src="comment.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                            mode="aspectFit"
                          ></image>
                          <text class="like-count" :class="{ 'liked': comment.isLiked }">{{ comment.likeCount || 0 }}</text>
                        </view>
                      </view>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 跟帖子详情页一致：没人回复不显示任何内容；有回复时显示「展开X条回复」或回复列表 -->
              <view class="xh-reply-level1" v-if="hasCommentReplies(comment)">
                <template v-if="comment.replies && comment.replies.length > 0">
                  <template v-for="(ent, idx) in getFlatReplies(comment)">
                    <view v-if="ent.type === 'reply'" :key="ent._key" class="xh-reply-item" @longpress="onCommentLongPress(ent.item)">
                      <image class="xh-reply-avatar" :src="getImageUrl(ent.item.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" @click.stop="goToCommentUserProfile(ent.item.userId)"></image>
                      <view class="xh-reply-content-wrapper">
                        <view class="xh-reply-header">
                          <text class="xh-reply-name">{{ ent.item.userName }}</text>
                          <text class="xh-reply-arrow" v-if="getReplyToUserName(ent)">▲</text>
                          <text class="xh-reply-to-name" v-if="getReplyToUserName(ent)">{{ getReplyToUserName(ent) }}</text>
                          <view class="xh-author-tag" v-if="ent.item.userId === post.userId">
                            <text class="xh-author-tag-text">作者</text>
                          </view>
                        </view>
                        <view class="xh-reply-text">
                          <block v-for="(part, pidx) in parseCommentContent(ent.item.content)" :key="pidx">
                            <text
                              v-if="part.type === 'text'"
                              class="comment-text-inline"
                            >{{ part.text }}</text>
                            <text
                              v-else-if="part.type === 'mention'"
                              class="comment-text-inline comment-mention"
                            >{{ part.text }}</text>
                            <image v-else-if="part.type === 'emoji'" class="comment-emoji-inline" :src="part.path" mode="aspectFit" />
                            <image v-else-if="part.type === 'image'" class="comment-image-inline" :src="getImageUrl(part.url)" mode="widthFix" />
                          </block>
                        </view>
                        <view class="xh-reply-meta">
                          <text class="xh-reply-date-loc">{{ formatCommentTime(ent.item.createTime) }} {{ ent.item.location || '' }}</text>
                          <text class="xh-reply-reply-btn" @click.stop="replyComment(ent.item)">回复</text>
                        </view>
                        <view class="xh-reply-actions-right">
                          <view class="xh-reply-action-item" @click.stop="toggleCommentLike(ent.item, ent.parent)">
                            <image class="xh-action-icon" :src="ent.item.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'" mode="aspectFit"></image>
                            <text class="xh-action-num" :class="{ 'xh-num-liked': ent.item.isLiked }" v-if="(ent.item.likesCount || 0) > 0">{{ ent.item.likesCount }}</text>
                          </view>
                        </view>
                      </view>
                    </view>
                    <view v-else-if="ent.type === 'expand'" :key="`expand-${ent._key}`" class="xh-expand-same" @click.stop="expandReplies(ent.key)">
                      <view class="xh-expand-line"></view>
                      <text class="xh-expand-text">展开{{ ent.count }}条回复</text>
                      <text class="xh-expand-arrow">▼</text>
                    </view>
                    <view v-else-if="ent.type === 'expand_more_row'" :key="`expand_more_row-${ent._key}`" class="xh-expand-same xh-expand-more-row">
                      <view class="xh-expand-line"></view>
                      <text class="xh-expand-text" @click.stop="expandMoreReplies(ent.commentId)">展开更多</text>
                      <text class="xh-expand-arrow" @click.stop="expandMoreReplies(ent.commentId)">▼</text>
                      <view class="xh-expand-gap"></view>
                      <text class="xh-expand-text xh-expand-collapse" @click.stop="collapseReplies(ent.commentId)">收起</text>
                      <text class="xh-expand-arrow-up" @click.stop="collapseReplies(ent.commentId)">▲</text>
                    </view>
                    <view v-else-if="ent.type === 'collapse_row'" :key="`collapse_row-${ent._key}`" class="xh-expand-same" @click.stop="collapseReplies(ent.commentId)">
                      <view class="xh-expand-line"></view>
                      <text class="xh-expand-text">收起</text>
                      <text class="xh-expand-arrow-up">▲</text>
                    </view>
                  </template>
                </template>
                <view v-else class="xh-expand-same" @click.stop="expandReplies(comment)">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text">{{ comment.repliesLoading ? '加载中...' : '展开' + (comment.replyCount || 0) + '条回复' }}</text>
                  <text class="xh-expand-arrow">▼</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <!-- @提及用户选择器（抖音式弹窗） -->
        <view class="mention-picker-mask" v-if="showMentionPicker" @tap="showMentionPicker = false"></view>
        <view class="mention-picker" v-if="showMentionPicker">
          <view class="mention-picker-title">选择要@的人</view>
          <scroll-view scroll-y class="mention-picker-list">
            <view 
              class="mention-picker-item" 
              v-for="u in mentionUserList" 
              :key="u.id"
              @tap.stop="selectMentionUser(u)"
            >
              <image class="mention-picker-avatar" :src="getImageUrl(u.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" />
              <text class="mention-picker-name">{{ u.userName || '用户' }}</text>
            </view>
            <view v-if="mentionUserList.length === 0" class="mention-picker-empty">暂无可@的用户</view>
          </scroll-view>
        </view>

        <!-- 底部评论输入框：textarea 仅纯文本+表情代码，图片用数组单独管理，用 <image /> 展示 -->
        <view class="comment-input-panel">
          <view class="comment-input-row">
            <view class="comment-input-inner">
              <view class="comment-input-display">
                <text v-if="commentText" class="comment-input-display-text">{{ commentText }}</text>
                <text v-else class="comment-input-placeholder">{{ replyingToUserName ? `回复${replyingToUserName}...` : '发条评论，和大家一起讨论' }}</text>
              </view>
              <input 
                class="comment-input comment-input-real"
                type="text"
                :value="commentText"
                :focus="commentInputFocus"
                :placeholder="replyingToUserName ? `回复${replyingToUserName}...` : '发条评论，和大家一起讨论'"
                @input="onCommentInput"
                @focus="onCommentFocus"
                @blur="onCommentBlur"
                @confirm="submitComment"
              />
              <view class="comment-input-actions">
                <image class="input-action-icon" src="/static/images/图片.png" mode="aspectFit" @click="chooseCommentImage"></image>
                <text class="input-action-icon" @click="mentionUser">@</text>
              </view>
            </view>
            <view v-if="commentText.trim() || commentImages.length" class="comment-send-btn" @click="submitComment">发送</view>
          </view>
          <view v-if="commentImages.length" class="comment-images-row">
            <view class="comment-image-item" v-for="(img, imgIdx) in commentImages" :key="imgIdx">
              <image class="comment-image-thumb" :src="img" mode="aspectFill" />
              <view class="comment-image-del" @click="removeCommentImage(imgIdx)">×</view>
            </view>
          </view>
        </view>
      </view>
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
      lastCommentText: '',    // 用于 @input 时检测删除的是否为占位符
      replyingTo: null,  // 正在回复的评论ID
      replyingToUserName: '',  // 正在回复的用户名
      currentUserId: null,  // 当前登录用户ID，从登录信息获取
      commentInputFocus: false,  // 输入框聚焦状态
      postImages: [],
      postTags: [],
      currentImageIndex: 0,
      isCollected: false,
      collectCount: 0,
      isCommentFocused: false,
      userCans: 0,  // 发帖人罐头数
      currentLocation: '',  // 当前位置信息
      // 视频相关
      postVideos: [],  // 视频列表
      currentVideoIndex: 0,  // 当前视频索引
      currentVideoUrl: '',  // 当前视频URL
      currentVideoCover: '',  // 当前视频封面
      hasVideo: false,  // 是否有视频
      showComments: false,  // 是否显示评论面板
      lastDetailLoadTime: 0,
      videoLoading: false,
      videoError: '',  // 视频错误信息
      videoKey: 0,  // 视频组件key，用于强制重新渲染
      isLoadingMoreComments: false,  // 是否正在加载更多评论
      videoIds: [],  // 视频列表ID（抖音式上下滑切换）
      currentIndex: 0,
      videoFeedPage: 1,      // 推荐流下一页
      hasMoreVideoFeed: true,
      loadingMore: false,
      /** 自定义抖音式控件：与 enable-play-gesture 解耦，单次点击即播/停 */
      invalidPostBackTimer: null,
      videoPaused: false,
      hasTriedPlay: false,
      videoCurrentTime: 0,
      videoDuration: 0,
      expandedReplies: {},   // 展开的评论ID集合 {commentId: true}
      expandedVisibleCount: {}, // 每条评论展开时显示的回复条数，默认3，点「展开更多」增加
      commentImages: [],     // 评论图片临时路径数组，单独管理，不用拼进 text
      showMentionPicker: false, // @提及用户选择器是否显示
      mentionUserList: [],   // @提及可选的用户列表
    }
  },

  computed: {
    canSwipe() {
      return this.videoIds && this.videoIds.length > 1
    },
    videoProgressPercent() {
      const d = this.videoDuration
      if (!d || d <= 0) return 0
      return Math.min(100, (this.videoCurrentTime / d) * 100)
    },
    videoTimeLabel() {
      return `${this.formatVideoClock(this.videoCurrentTime)} / ${this.formatVideoClock(this.videoDuration)}`
    }
  },

  async onLoad(options) {
    let postId = options.id || options.postId
    if (!postId || postId === 'undefined' || postId === 'null' || postId === undefined || postId === null) {
      console.error('帖子ID不存在或无效，无法加载详情', { options, postId })
      uni.showToast({ title: '帖子ID不存在', icon: 'none', duration: 2000 })
      this.invalidPostBackTimer = setTimeout(() => {
        this.invalidPostBackTimer = null
        util.navigateBack()
      }, 1500)
      return
    }
    postId = String(postId).trim()
    const anchorId = postId
    let ids = (options.ids || '').split(',').map(s => String(s).trim()).filter(Boolean)
    if (anchorId && !ids.includes(anchorId)) {
      ids = [anchorId, ...ids.filter(x => x !== anchorId)]
    }
    if (!ids.length) ids = [anchorId]
    let idx = ids.indexOf(anchorId)
    if (idx < 0) {
      const parsed = parseInt(options.index, 10)
      idx = !isNaN(parsed) && parsed >= 0 && parsed < ids.length ? parsed : 0
    }
    this.videoIds = ids
    this.currentIndex = idx
    this.postId = anchorId
    // 发现页传入的点赞数与状态，先用于首屏展示，与发现页一致；loadPostDetail 会用接口数据覆盖
    if (options.likeCount !== undefined && options.likeCount !== '' && !isNaN(Number(options.likeCount))) {
      this.likeCount = Number(options.likeCount)
    }
    if (options.isLiked !== undefined && options.isLiked !== '') {
      this.isLiked = options.isLiked === '1' || options.isLiked === true
    }
    // 从个人主页进入：只滑当前账号的视频，不加载推荐流
    const fromProfile = options.fromProfile === '1' || options.fromProfile === 'true'
    if (fromProfile) {
      this.hasMoreVideoFeed = false
    }
    this.loadCurrentUserId()
    this.getCurrentLocation()
    this.applyPendingVideo(this.postId)
    await this.loadPostDetail()
    this.loadComments()
    if (!fromProfile) {
      this.$nextTick(() => this.tryLoadMoreVideoFeed())
    }
  },

  onUnload() {
    if (this.invalidPostBackTimer) {
      clearTimeout(this.invalidPostBackTimer)
      this.invalidPostBackTimer = null
    }
  },

  onShow() {
    this.loadCurrentUserId()
    if (!this.postId) return
    // 静默刷新：节流 15 秒，不显示全局「加载中」，避免从其他页返回时反复弹 loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastDetailLoadTime || 0) < throttleMs && (this.lastDetailLoadTime || 0) > 0) return
    this.loadPostDetail(true)
    this.loadComments(true)
    this.lastDetailLoadTime = Date.now()
  },

  onShareAppMessage() {
    return {
      title: (this.post && (this.post.title || this.post.content)) ? String(this.post.title || this.post.content).slice(0, 30) : '帖子详情',
      path: `/pages-community/post-detail-video?id=${this.postId || ''}`
    }
  },
  onShareTimeline() {
    return {
      title: (this.post && (this.post.title || this.post.content)) ? String(this.post.title || this.post.content).slice(0, 30) : '帖子详情',
      query: `id=${this.postId || ''}`
    }
  },

  methods: {
    /** 去掉 # 及后续（如 devtools 注入的 #devtools_no_referrer），避免 video 组件请求失败 */
    sanitizeVideoUrl(u) {
      if (u == null || typeof u !== 'string') return u
      let s = u.trim()
      const i = s.indexOf('#')
      if (i >= 0) s = s.slice(0, i)
      return s
    },

    // 应用列表页预传的视频 URL/封面，进入即开始加载（一点就播）
    applyPendingVideo(postId) {
      if (!postId) return
      const app = getApp()
      const pending = app && app.globalData && app.globalData.pendingVideoDetail && app.globalData.pendingVideoDetail[postId]
      if (!pending || !pending.url) return
      let url = (pending.url || '').trim()
      const cover = (pending.cover || '').trim()
      if (!url) return
      if (!url.startsWith('http')) url = this.getImageUrl(url)
      else if (url.startsWith('http://')) url = url.replace('http://', 'https://')
      this.currentVideoUrl = this.sanitizeVideoUrl(url)
      this.currentVideoCover = cover ? this.getImageUrl(cover) : ''
      this.hasVideo = true
      this.postVideos = [{ url: pending.url, thumb: pending.cover, cover: pending.cover }]
      this.videoLoading = true
      this.videoError = ''
      delete app.globalData.pendingVideoDetail[postId]
    },

    // 获取当前登录用户ID（从多个地方尝试获取）
    loadCurrentUserId() {
      // 方法1: 直接从 userId 存储获取
      let userId = uni.getStorageSync('userId')
      
      // 方法2: 如果 userId 不存在，从 userInfo 中获取
      if (!userId) {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          userId = userInfo.id || userInfo.uid || userInfo.userId
        }
      }
      
      // 方法3: 如果还是没有，尝试从 token 中解析（如果有相关工具）
      // 这里先不实现，因为需要 JWT 解析库
      
      // 确保 userId 是数字类型
      if (userId) {
        this.currentUserId = Number(userId)
      } else {
        this.currentUserId = null
      }
    },

    // 获取当前位置信息（使用IP定位获取真实位置）
    getCurrentLocation(forceRefresh = false) {
      // 检查缓存，避免频繁请求
      const cachedLocation = uni.getStorageSync('cached_location')
      const cacheTime = uni.getStorageSync('cached_location_time')
      const now = Date.now()
      
      // 如果强制刷新，或者缓存不存在/已过期，才重新获取
      if (!forceRefresh && cachedLocation && cacheTime && (now - cacheTime < 5 * 60 * 1000)) {
        this.currentLocation = cachedLocation
        return
      }
      
      // 如果是强制刷新，清除缓存
      if (forceRefresh) {
        uni.removeStorageSync('cached_location')
        uni.removeStorageSync('cached_location_time')
      }
      
      // 每次调用时先清空之前的位置，确保是实时获取
      this.currentLocation = ''
      
      // 使用免费的IP定位API获取真实位置（基于网络IP地址）
      // 使用ip-api.com，免费且稳定
      uni.request({
        url: 'http://ip-api.com/json/?lang=zh-CN&fields=status,message,regionName,country',
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.status === 'success' && res.data.regionName) {
            let province = res.data.regionName
            // 去掉"省"、"市"、"自治区"等后缀
            province = province.replace(/省$|市$|自治区$|特别行政区$|壮族自治区$|维吾尔自治区$|回族自治区$/, '')
            this.currentLocation = province
            
            // 缓存位置信息（5分钟有效）
            uni.setStorageSync('cached_location', province)
            uni.setStorageSync('cached_location_time', now)
          } else {
            this.currentLocation = ''
          }
        },
        fail: (err) => {
          this.currentLocation = ''
        }
      })
    },
    
    // 逆地理编码：通过经纬度获取地址（只获取省份）
    reverseGeocode(latitude, longitude) {
      // 验证经纬度是否有效
      if (!latitude || !longitude || latitude === 0 || longitude === 0) {
        this.currentLocation = ''
        return
      }
      
      // 使用腾讯地图逆地理编码API
      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77&get_poi=0`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.status === 0 && res.data.result) {
            const address = res.data.result.address_component
            if (address && address.province) {
              // 只显示省份，去掉后缀
              let province = address.province
              province = province.replace(/省$|市$|自治区$|特别行政区$/, '')
              this.currentLocation = province
            } else {
              this.currentLocation = ''
            }
          } else {
            this.currentLocation = ''
          }
        },
        fail: (err) => {
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
            this.videoLoading = false
            return
          }
          this.post = response.data
          this.lastDetailLoadTime = Date.now()

          // 处理图片数据
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
          }
          
          // 处理视频数据（必须在图片处理之后）
          this.parseVideos()
          
          // 如果识别为视频，从postImages中移除视频URL，只保留图片
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
          
          // 解析标签
          this.parseTags()
          
          // 设置点赞状态和数量
          // 确保 isLiked 是布尔值，不能是 undefined
          this.isLiked = this.post.isLiked === true || this.post.isLiked === 1
          this.likeCount = this.post.likesCount || 0
          this.commentCount = this.post.commentsCount || 0
          this.collectCount = this.post.collectCount || 0
          
          // 设置收藏状态（支持多种字段名）
          this.isCollected = this.post.isCollected === true || this.post.isCollected === 1 || 
                             this.post.collected === true || this.post.collected === 1
          
          // 加载发帖人信息（等级和罐头数）
          if (this.post.userId) {
            this.loadUserInfo(this.post.userId)
          }
          
          // 帖子详情加载完成后，检查点赞和关注状态
          this.checkLikeAndFollowStatus()
          
          // 检查收藏状态
          if (this.currentUserId) {
            this.checkCollectStatus()
          }
        } else {
          console.error('加载帖子详情失败:', response.msg)
          this.videoLoading = false
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载帖子详情失败:', error)
        this.videoLoading = false
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 解析视频数据
    parseVideos() {
      this.hasVideo = false
      this.postVideos = []
      this.currentVideoUrl = ''
      this.currentVideoCover = ''
      
      // 首先检查videos字段（优先使用videos字段）
      if (this.post.videos && this.post.videos !== null && this.post.videos !== 'null' && this.post.videos !== '') {
        try {
          let videosData = this.post.videos
          if (typeof videosData === 'string') {
            // 尝试解析JSON字符串
            try {
              videosData = JSON.parse(videosData)
            } catch (parseError) {
              // 如果不是JSON，可能是单个URL字符串
              if (videosData.includes('.mp4') || videosData.includes('.mov') || videosData.includes('.avi')) {
                videosData = [{ url: videosData, thumb: '', cover: '' }]
              } else {
                videosData = null
              }
            }
          }
          
          if (Array.isArray(videosData) && videosData.length > 0) {
            const normOne = (v) => {
              if (typeof v === 'string') return { url: v, thumb: '', cover: '' }
              if (!v || typeof v !== 'object') return null
              const u = v.url || v.src || v.videoUrl || v.path
              if (!u || typeof u !== 'string') return null
              return { ...v, url: u }
            }
            const validVideos = videosData.map(normOne).filter(Boolean)
            if (validVideos.length > 0) {
              this.postVideos = validVideos
              this.hasVideo = true
              this.currentVideoIndex = 0
              this.setCurrentVideo(0)
              return
            }
          } else if (typeof videosData === 'object' && videosData !== null && !Array.isArray(videosData)) {
            const singleUrl = videosData.url || videosData.src || videosData.videoUrl || videosData.path
            if (singleUrl) {
              this.postVideos = [{ ...videosData, url: singleUrl }]
              this.hasVideo = true
              this.currentVideoIndex = 0
              this.setCurrentVideo(0)
              return
            }
          }
        } catch (e) {
          console.error('解析videos字段失败:', e)
        }
      }
      
      // 如果videos字段为空或null，检查images字段中是否有视频文件
      // 注意：只有当videos字段明确为空或null时，才检查images字段
      if (!this.hasVideo && this.postImages && this.postImages.length > 0) {
        const videoExtensions = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
        const videoFiles = this.postImages.filter(img => {
          if (typeof img === 'string') {
            const url = img.toLowerCase()
            return videoExtensions.some(ext => url.includes(ext))
          }
          return false
        })
        
        // 只有当images中确实有视频文件，且videos字段为空或null时，才使用images中的视频
        if (videoFiles.length > 0 && (!this.post.videos || this.post.videos === null || this.post.videos === 'null' || this.post.videos === '')) {
          this.postVideos = videoFiles.map(url => ({ url: url, cover: '', thumb: '' }))
          this.hasVideo = true
          this.currentVideoIndex = 0
          this.setCurrentVideo(0)
        }
      }
    },
    
    // 设置当前视频
    setCurrentVideo(index) {
      if (!this.postVideos || this.postVideos.length <= index) return
      const video = this.postVideos[index]
      let rawUrl = typeof video === 'string'
        ? video
        : (video && (video.url || video.src || video.videoUrl || video.path) ? (video.url || video.src || video.videoUrl || video.path) : '')
      rawUrl = rawUrl ? this.sanitizeVideoUrl(String(rawUrl)) : ''
      if (!rawUrl) return

      // 清除之前的错误状态
      this.videoError = ''
      this.videoLoading = false
      this.videoCurrentTime = 0
      this.videoDuration = 0
      // 初始视为暂停，保证首击总是触发播放
      this.videoPaused = true
      this.hasTriedPlay = false

      let finalUrl = rawUrl
      if (rawUrl.startsWith('http://')) {
        finalUrl = rawUrl.replace('http://', 'https://')
      } else if (!/^https?:\/\//i.test(rawUrl)) {
        finalUrl = this.getImageUrl(rawUrl)
      } else {
        finalUrl = rawUrl
      }
      this.videoKey = Date.now()
      this.currentVideoUrl = this.sanitizeVideoUrl(finalUrl)
      this.videoError = ''

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
    
    // 视频播放事件
    onVideoPlay() {
      this.videoLoading = false
      this.videoPaused = false
      this.hasTriedPlay = true
    },
    
    // 视频暂停事件
    onVideoPause() {
      this.videoPaused = true
    },
    
    // 视频结束事件
    onVideoEnded() {
      this.videoPaused = true
      if (this.videoDuration > 0) this.videoCurrentTime = this.videoDuration
    },

    /** 单击视频区域切换播放/暂停（已关闭微信「双击手势」，避免要点两下） */
    onVideoSurfaceTap() {
      try {
        const ctx = uni.createVideoContext('video-player', this)
        // 首次点击或当前暂停时，统一执行播放，避免“首击无效”
        if (this.videoPaused || !this.hasTriedPlay || this.videoCurrentTime <= 0) {
          ctx.play()
        } else {
          ctx.pause()
        }
      } catch (err) {
        console.error('video toggle', err)
      }
    },

    resumeVideoPlay() {
      try {
        uni.createVideoContext('video-player', this).play()
      } catch (e) {}
    },

    formatVideoClock(sec) {
      let s = typeof sec === 'number' && !isNaN(sec) ? sec : 0
      if (s < 0) s = 0
      const m = Math.floor(s / 60)
      const r = Math.floor(s % 60)
      return `${m}:${r < 10 ? '0' : ''}${r}`
    },
    
    // 视频错误事件
    onVideoError(e) {
      this.videoLoading = false
      const detail = e.detail || {}
      
      // 尝试获取更详细的错误信息
      if (detail.errMsg) {
        this.videoError = detail.errMsg
      } else if (detail.errCode) {
        this.videoError = `错误码: ${detail.errCode}`
      } else {
        // 默认错误信息
        this.videoError = '视频加载失败，请检查网络或视频文件'
      }
    },
    
    // 视频时间更新事件
    onVideoTimeUpdate(e) {
      const d = e.detail || {}
      if (typeof d.currentTime === 'number') this.videoCurrentTime = d.currentTime
      if (typeof d.duration === 'number' && d.duration > 0) this.videoDuration = d.duration
    },
    
    // 视频等待加载事件
    onVideoWaiting() {
      this.videoLoading = true
    },
    
    // 视频进度事件
    onVideoProgress(e) {
      // 视频加载进度
      this.videoLoading = false
    },
    
    // 视频元数据加载完成
    onVideoLoadedMetadata(e) {
      this.videoLoading = false
      const detail = e.detail || {}
      if (detail.duration && detail.duration > 0) {
        this.videoDuration = detail.duration
        this.videoError = ''
      }
    },
    
    // 视频开始加载
    onVideoLoadStart(e) {
      this.videoLoading = true
      this.videoError = ''
    },
    
    // 格式化数字（用于点赞数、评论数等）
    formatCount(count) {
      if (!count) return '0'
      if (count < 1000) return String(count)
      if (count < 10000) return (count / 1000).toFixed(1) + 'k'
      return (count / 10000).toFixed(1) + 'w'
    },
    
    // 显示评论面板
    showCommentPanel() {
      this.showComments = true
    },
    
    // 隐藏评论面板
    hideCommentPanel() {
      this.showComments = false
    },
    
    // 返回上一页
    goBack() {
      util.navigateBack()
    },

    showPostActionSheet() {
      uni.showActionSheet({
        itemList: ['删除帖子'],
        success: (res) => {
          if (res.tapIndex === 0) this.handleDeletePost()
        }
      })
    },

    async handleDeletePost() {
      uni.showModal({
        title: '提示',
        content: '确定要删除这条帖子吗？删除后无法恢复。',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '删除中...' })
            await api.deletePost(this.postId, this.currentUserId)
            uni.hideLoading()
            uni.setStorageSync('REFRESH_MY_STATS', '1') // 让「我的」页刷新动态数
            uni.showToast({ title: '已删除', icon: 'success' })
            setTimeout(() => {
              util.navigateBack()
            }, 500)
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: e.msg || e.message || '删除失败', icon: 'none' })
          }
        }
      })
    },

    // 抖音式上下滑切换视频
    async onVideoSwiperChange(e) {
      const next = e.detail.current
      if (next === this.currentIndex || next < 0 || next >= this.videoIds.length) return
      this.currentIndex = next
      this.postId = this.videoIds[next]
      this.currentVideoUrl = ''
      this.currentVideoCover = ''
      this.videoError = ''
      this.videoLoading = true
      this.videoKey = Date.now()
      this.videoCurrentTime = 0
      this.videoDuration = 0
      this.videoPaused = false
      await this.loadPostDetail()
      this.loadComments()
      this.tryLoadMoreVideoFeed()
    },

    // 推荐流加载更多：滑到倒数 1～2 条时拉取下一页，去重追加
    async tryLoadMoreVideoFeed() {
      if (!this.canSwipe || !this.hasMoreVideoFeed || this.loadingMore) return
      if (this.currentIndex < this.videoIds.length - 2) return
      this.loadingMore = true
      try {
        const res = await api.getVideoFeed(this.videoFeedPage, 10, this.currentUserId)
        if (!res || (res.code !== 200 && res.code !== 0) || !res.data) {
          this.hasMoreVideoFeed = false
          return
        }
        const d = res.data
        const nextIds = (d.postIds || []).map(id => String(id).trim()).filter(Boolean)
        const set = new Set(this.videoIds.map(id => String(id)))
        for (const id of nextIds) {
          if (!set.has(id)) {
            set.add(id)
            this.videoIds.push(id)
          }
        }
        this.videoFeedPage = d.nextPage != null ? d.nextPage : this.videoFeedPage + 1
        this.hasMoreVideoFeed = !!d.hasMore
      } catch (e) {
        this.hasMoreVideoFeed = false
      } finally {
        this.loadingMore = false
      }
    },
    
    // 检查环境（开发环境还是生产环境）
    
    
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

    // 格式化帖子日期
    formatPostDate(dateString) {
      if (!dateString) return ''
      // 修复iOS日期格式兼容性问题
      let date
      if (dateString.includes(' ')) {
        // 将 "yyyy-MM-dd HH:mm:ss" 格式转换为 "yyyy-MM-ddTHH:mm:ss" 格式
        date = new Date(dateString.replace(' ', 'T'))
      } else {
        date = new Date(dateString)
      }
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}-${day.toString().padStart(2, '0')}`
    },

    // 格式化评论时间
    formatCommentTime(dateString) {
      if (!dateString) return ''
      // 修复iOS日期格式兼容性问题
      let date
      if (dateString.includes(' ')) {
        // 将 "yyyy-MM-dd HH:mm:ss" 格式转换为 "yyyy-MM-ddTHH:mm:ss" 格式
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
      
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}-${day.toString().padStart(2, '0')}`
    },

    // 预览图片
    previewImage(index) {
      uni.previewImage({
        urls: this.postImages,
        current: index
      })
    },

    // 检查收藏状态
    async checkCollectStatus() {
      if (!this.postId || !this.currentUserId) return
      
      try {
        const res = await api.checkCollectStatus(this.postId, this.currentUserId)
        if (res.code === 200 || res.code === 0) {
          this.isCollected = res.data === true || res.data === 1
        }
      } catch (error) {
        console.error('检查收藏状态失败:', error)
      }
    },
    
    // 切换收藏状态
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
          // 取消收藏
          res = await api.uncollectPost(this.postId, this.currentUserId)
        } else {
          // 收藏
          res = await api.collectPost(this.postId, this.currentUserId)
        }
        
        if (res.code === 200 || res.code === 0) {
          this.isCollected = !this.isCollected
          this.collectCount += this.isCollected ? 1 : -1
          
          // 更新 post 对象中的收藏状态
          if (this.post) {
            this.post.isCollected = this.isCollected
            this.post.collectCount = this.collectCount
          }
          
          // 强制更新视图，确保样式生效
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
          title: '操作失败，请重试',
          icon: 'none'
        })
      }
    },

    // 选图：微信式先弹「拍照」「从相册选择」，再选图
    chooseCommentImage() {
      const remain = 9 - this.commentImages.length
      if (remain <= 0) {
        uni.showToast({ title: '最多上传9张', icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (sheetRes) => {
          const sourceType = sheetRes.tapIndex === 0 ? ['camera'] : ['album']
          uni.chooseMedia({
            count: remain,
            mediaType: ['image'],
            sourceType: sourceType,
            success: (res) => {
              const files = res.tempFiles || []
              files.forEach(f => {
                if (f.tempFilePath && this.commentImages.length < 9) {
                  this.commentImages.push(f.tempFilePath)
                }
              })
            }
          })
        }
      })
    },
    removeCommentImage(idx) {
      this.commentImages.splice(idx, 1)
    },

    // @：抖音式，弹出用户选择列表
    async mentionUser() {
      this.commentText = (this.commentText || '') + '@'
      this.lastCommentText = this.commentText
      this.$nextTick(() => { this.commentInputFocus = true })
      // 获取可@用户列表并显示选择器
      await this.loadMentionUsers()
      this.showMentionPicker = true
    },
    async loadMentionUsers() {
      if (!this.postId || !this.currentUserId) return
      try {
        const res = await api.getMentionUsers(this.postId, this.currentUserId, false)
        if (res && (res.code === 200 || res.code === 0) && res.data) {
          this.mentionUserList = res.data
        } else {
          this.mentionUserList = []
        }
      } catch (e) {
        this.mentionUserList = []
      }
    },
    selectMentionUser(user) {
      const name = user.userName || '用户'
      // 删除刚输入的 @，替换为 @用户名
      let t = this.commentText || ''
      if (t.endsWith('@')) {
        t = t.slice(0, -1) + '@' + name + ' '
      } else {
        t = t + name + ' '
      }
      this.commentText = t
      this.lastCommentText = t
      this.showMentionPicker = false
      this.mentionUserList = []
      this.$nextTick(() => { this.commentInputFocus = true })
    },

    // 解析评论内容：[图片]url 仍解析为图片，[xxx] 等按纯文本展示
    parseCommentContent(text) {
      if (!text || typeof text !== 'string') return [{ type: 'text', text: text }]
      const parts = []
      const imgRegex = /\[图片\](https?:\/\/[^\s\[\]]+)/g
      let lastEnd = 0
      let m
      while ((m = imgRegex.exec(text)) !== null) {
        if (m.index > lastEnd) {
          this._pushEmojiParts(parts, text.substring(lastEnd, m.index))
        }
        parts.push({ type: 'image', url: m[1] })
        lastEnd = m.index + m[0].length
      }
      if (lastEnd < text.length) {
        this._pushEmojiParts(parts, text.substring(lastEnd))
      }
      return parts.length ? parts : [{ type: 'text', text: text }]
    },
    _pushEmojiParts(parts, segment) {
      if (!segment) return
      // 在普通文本里拆出 @xxx 片段，单独作为 mention，高亮显示
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
    
    onCommentFocus() {
      this.isCommentFocused = true
    },

    // 输入框内容变化：同步 commentText
    onCommentInput(e) {
      const newVal = (e && e.detail && e.detail.value !== undefined) ? e.detail.value : ''
      const oldVal = this.lastCommentText || ''
      this.commentText = newVal
      this.lastCommentText = newVal
      // 检测输入 @ 后自动弹出用户选择器（抖音式）
      if (newVal.endsWith('@') && !oldVal.endsWith('@')) {
        this.loadMentionUsers()
        this.showMentionPicker = true
      }
      if (!newVal.endsWith('@') && this.showMentionPicker) {
        this.showMentionPicker = false
      }
    },

    // 评论输入框失焦（延迟关闭表情面板，避免点选表情时先关掉）
    onCommentBlur() {
      this.isCommentFocused = false
      this.commentInputFocus = false
    },

    // 显示评论输入
    showCommentInput() {
      // 直接聚焦到输入框，小程序会自动弹出输入法
      // 不需要滚动页面
      this.$nextTick(() => {
        this.commentInputFocus = true
      })
    },

    async loadComments() {
      try {
        if (!this.postId) {
          console.error('帖子ID不存在，无法加载评论')
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
          }
          return {
            ...reply,
            isLiked: replyIsLiked,
            likeCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
            likesCount: reply.likeCount || reply.like_count || reply.likesCount || 0,
            replies: []
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
          return {
            ...comment,
            isLiked,
            likeCount: comment.likeCount || comment.like_count || comment.likesCount || 0,
            replies,
            replyCount: replyCount == null ? undefined : Number(replyCount),
            repliesLoading: false
          }
        })
        if (response.total !== undefined) {
          this.commentCount = response.total
        } else if (response.count !== undefined) {
          this.commentCount = response.count
        } else if (list.length > 0) {
          this.commentCount = Math.max(this.commentCount, this.comments.length)
        }
      } catch (error) {
        console.error('加载评论失败:', error)
      }
    },

    async toggleLike() {
      try {
        // 先尝试重新获取用户ID（可能刚登录）
        this.loadCurrentUserId()
        
        // 检查用户是否登录
        if (!this.currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          // 跳转到登录页
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 500)
          return
        }
        
        // 根据当前状态执行操作
        const wasLiked = this.isLiked
        if (wasLiked) {
          // 当前已点赞，执行取消点赞
          await api.unlikePost(this.postId, this.currentUserId)
          this.isLiked = false
          this.likeCount = Math.max(0, this.likeCount - 1)
          uni.showToast({
            title: '已取消点赞',
            icon: 'success'
          })
        } else {
          // 当前未点赞，执行点赞
          await api.likePost(this.postId, this.currentUserId)
          this.isLiked = true
          this.likeCount = this.likeCount + 1
          uni.showToast({
            title: '点赞成功',
            icon: 'success'
          })
        }
        
        // 操作成功后，更新 post 对象中的 isLiked 状态，避免后续检查时被重置
        if (this.post) {
          this.post.isLiked = this.isLiked
          this.post.likesCount = this.likeCount
        }
        // 写入 Vuex，返回发现页时与列表同步
        if (this.$store) {
          this.$store.commit('SET_POST_LIKE', {
            postId: this.postId,
            isLiked: this.isLiked,
            likeCount: this.likeCount
          })
        }
        // 强制更新视图，确保样式生效
        this.$forceUpdate()
        
        // 不需要立即重新检查状态，因为我们已经更新了本地状态
        // 如果需要同步服务器状态，可以在页面重新显示时（onShow）检查
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
      // 再次尝试获取用户ID，防止缓存失效或页面onLoad时未获取到
      this.loadCurrentUserId()
      
      // 检查用户是否登录
      if (!this.currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        // 跳转到登录页
        uni.navigateTo({
          url: '/pages-auth/login'
        })
        return
      }
      
      // 防止自己关注自己
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
            title: this.isFollowing ? '关注成功' : '取消关注',
            icon: 'success'
          })
          
          // 不需要立即重新检查状态，因为我们已经更新了本地状态
          // 如果需要同步服务器状态，可以在页面重新显示时（onShow）检查
        }
      } catch (error) {
        console.error('关注操作失败:', error)
        let errorMessage = '系统异常，请稍后重试'
        if (error.errMsg && error.errMsg.includes('request:fail')) {
          errorMessage = '网络请求失败，请检查网络'
        } else if (error.message && error.message.includes('未授权')) {
          errorMessage = '登录已过期，请重新登录'
          // 跳转到登录页
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 1500)
        } else if (error.data && error.data.msg) {
          errorMessage = error.data.msg // 后端返回的业务错误信息
        } else if (error.message) {
          errorMessage = error.message
        }
        uni.showToast({
          title: errorMessage,
          icon: 'none'
        })
      }
    },

    // 跳转到用户主页（发帖人）
    goToUserProfile() {
      if (!this.post || !this.post.userId) {
        console.error('用户ID不存在，无法跳转到用户主页')
        uni.showToast({
          title: '用户信息不存在',
          icon: 'none'
        })
        return
      }
      
      const targetUserId = this.post.userId
      uni.navigateTo({
        url: `/user/profile?userId=${targetUserId}`
      })
    },

    // 跳转到评论用户的主页
    goToCommentUserProfile(userId) {
      if (!userId) {
        console.error('用户ID不存在，无法跳转到用户主页')
        uni.showToast({
          title: '用户信息不存在',
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
        uni.showToast({ title: '请输入内容或选择图片', icon: 'none' })
        return
      }

      try {
        this.currentLocation = ''
        this.getCurrentLocation(true)

        // 获取评论内容
        const content = (this.commentText || '').trim()
        if (this.commentImages.length > 0) {
          uni.showLoading({ title: '上传中...', mask: true })
          const urls = []
          for (let i = 0; i < this.commentImages.length; i++) {
            const tempPath = this.commentImages[i]
            const url = await this.uploadOneImage(tempPath)
            if (url) urls.push(url)
          }
          uni.hideLoading()
          if (urls.length) {
            content = content ? content + '\n' + urls.map(u => '[图片]' + u).join('\n') : urls.map(u => '[图片]' + u).join('\n')
          }
        }

        const commentData = {
          content: content,
          userId: this.currentUserId
        }
        if (this.replyingTo) commentData.parentId = this.replyingTo
        if (this.currentLocation) commentData.location = this.currentLocation

        const response = await api.addComment(this.postId, commentData)

        if (response && (response.code === 200 || response.code === 0)) {
          const wasReplying = !!this.replyingTo
          this.commentText = ''
          this.lastCommentText = ''
          this.commentImages = []
          this.replyingTo = null
          this.replyingToUserName = ''
          this.commentCount = this.commentCount + 1
          setTimeout(() => this.loadComments(), 500)
          uni.showToast({ title: wasReplying ? '回复成功' : '评论成功', icon: 'success' })
        }
      } catch (error) {
        uni.showToast({ title: '评论失败', icon: 'none' })
      }
    },

    uploadOneImage(tempPath) {
      return new Promise((resolve) => {
        uni.uploadFile({
          url: api.baseURL + '/api/upload/image',
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

    // 回复评论
    replyComment(comment) {
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
      
      // 设置正在回复的评论
      this.replyingTo = comment.id
      this.replyingToUserName = comment.userName
      
      // 不添加@符号，直接清空输入框
      this.commentText = ''
      this.lastCommentText = ''

      // 聚焦到输入框
      this.$nextTick(() => {
        this.commentInputFocus = true
        // 确保评论面板是打开的
        if (!this.showComments) {
          this.showComments = true
        }
        // 延迟一下再聚焦，确保面板已渲染
        setTimeout(() => {
          this.commentInputFocus = true
        }, 100)
      })
    },

    // 取消回复
    cancelReply() {
      this.replyingTo = null
      this.replyingToUserName = ''
      this.commentText = ''
      this.lastCommentText = ''
    },

    /** 跟帖子详情页一致：仅当接口明确返回回复数为 0 时不显示；否则都显示 */
    hasCommentReplies(c) {
      if (c.replyCount != null && Number(c.replyCount) === 0) return false
      return true
    },

    /** 回复行「用户名 ► 被回复人」：返回被回复人的昵称 */
    getReplyToUserName(ent) {
      if (!ent || !ent.item) return ''
      return ent.item.replyToUserName || (ent.parent && ent.parent.userName) || ''
    },

    /** 统计回复总数（含嵌套） */
    countAllReplies(arr) {
      if (!Array.isArray(arr) || !arr.length) return 0
      let n = 0
      for (const r of arr) {
        n += 1
        n += this.countAllReplies(r.replies || [])
      }
      return n
    },

    /** 扁平化回复列表：展开时最多先显示 3 条，可「展开更多」；底部横线+收起/展开更多（与帖子详情页一致） */
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

    /** 展开回复（懒加载，与帖子详情页一致） */
    async expandReplies(comment) {
      const commentId = typeof comment === 'object' ? comment.id : comment
      const commentIndex = this.comments.findIndex(c => c.id === commentId)
      if (commentIndex === -1) return
      const targetComment = this.comments[commentIndex]

      if (targetComment.replies && targetComment.replies.length > 0) {
        this.$set(this.expandedReplies, commentId, true)
        return
      }

      if (!targetComment.repliesLoading) {
        this.$set(targetComment, 'repliesLoading', true)
        try {
          const repliesResponse = await api.getCommentReplies(commentId, this.currentUserId, false)
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
              const replyToUserName = reply.parentId === commentId
                ? targetComment.userName
                : (raw.find(r => r.id === reply.parentId) || {}).userName || targetComment.userName
              return { ...reply, replyToUserName }
            })
            const totalReplyCount = this.countAllReplies(replies)
            this.$set(targetComment, 'replies', replies)
            this.$set(targetComment, 'repliesLoading', false)
            this.$set(targetComment, 'replyCount', totalReplyCount)
            this.$set(this.expandedReplies, commentId, true)
          }
        } catch (err) {
          this.$set(targetComment, 'repliesLoading', false)
        }
      }
    },

    /** 展开更多回复（单条评论） */
    expandMoreReplies(commentId) {
      const c = this.comments.find(co => co.id === commentId)
      if (!c) return
      const total = this.countAllReplies(c.replies || [])
      this.$set(this.expandedVisibleCount, commentId, total)
    },

    /** 收起回复 */
    collapseReplies(commentId) {
      this.$set(this.expandedReplies, commentId, false)
      this.$set(this.expandedVisibleCount, commentId, 3)
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
        itemList: ['删除'],
        success: (res) => {
          if (res.tapIndex === 0) this.handleDeleteComment(comment)
        }
      })
    },

    async handleDeleteComment(comment) {
      uni.showModal({
        title: '提示',
        content: '确定删除这条评论吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '删除中...' })
            await api.deleteComment(comment.id, this.currentUserId)
            uni.hideLoading()
            this.commentCount = Math.max(0, (this.commentCount || 0) - 1)
            await this.loadComments()
            uni.showToast({ title: '已删除', icon: 'success' })
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: (e.msg || e.message) || '删除失败', icon: 'none' })
          }
        }
      })
    },

    /** 点赞/取消点赞评论或回复（与帖子详情页一致，主评论与回复分开） */
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
        uni.showToast({ title: '请刷新后重试', icon: 'none' })
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
        uni.showToast({ title: '操作失败，请重试', icon: 'none' })
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

    async likeComment(commentId) {
      try {
        // 先尝试重新获取用户ID（可能刚登录）
        this.loadCurrentUserId()
        
        // 检查用户是否登录
        if (!this.currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          // 跳转到登录页
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 500)
          return
        }
        
        
        // 找到对应的评论对象，检查是否已点赞（可能在顶级评论或回复中）
        let comment = this.comments.find(c => c.id === commentId)
        let isReply = false
        
        // 如果不在顶级评论中，查找回复
        if (!comment) {
          for (let c of this.comments) {
            if (c && c.replies && Array.isArray(c.replies)) {
              const reply = c.replies.find(r => r && r.id === commentId)
              if (reply) {
                comment = reply
                isReply = true
                break
              }
            }
          }
        }
        
        if (!comment) {
          console.error('找不到对应的评论')
          return
        }
        
        // 根据当前状态执行操作
        const wasLiked = comment.isLiked || false
        if (wasLiked) {
          // 当前已点赞，执行取消点赞
          await api.unlikeComment(commentId, this.currentUserId)
          comment.isLiked = false
          comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
          uni.showToast({
            title: '已取消点赞',
            icon: 'success'
          })
        } else {
          // 当前未点赞，执行点赞
          await api.likeComment(commentId, this.currentUserId)
          comment.isLiked = true
          comment.likeCount = (comment.likeCount || 0) + 1
          uni.showToast({
            title: '点赞成功',
            icon: 'success'
          })
        }
        
        // 点赞操作后，强制重新加载评论以确保状态同步
        // 延迟一下，确保后端数据已更新
        setTimeout(() => {
          this.loadComments()
        }, 300)
      } catch (error) {
        console.error('点赞评论失败:', error)
        const errorMsg = error.message || error.errMsg || '操作失败'
        uni.showToast({
          title: errorMsg.includes('登录') ? '请先登录' : '操作失败',
          icon: 'none',
          duration: 2000
        })
      }
    },


    sharePost() {
      uni.showActionSheet({
        itemList: ['分享到微信', '分享到朋友圈', '复制链接'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.showToast({ title: '请点击右上角···选择「发送给朋友」', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 1) {
            uni.showToast({ title: '请点击右上角···选择「分享到朋友圈」', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 2) {
            const link = this.getShareLink()
            uni.setClipboardData({
              data: link,
              success: () => uni.showToast({ title: '链接已复制', icon: 'success' }),
              fail: () => uni.showToast({ title: '复制失败', icon: 'none' })
            })
          }
        }
      })
    },
    getShareLink() {
      const path = `/pages-community/post-detail-video?id=${this.postId || ''}`
      try {
        const base = typeof util !== 'undefined' && util.getApiBaseUrl ? util.getApiBaseUrl().replace(/\/api.*$/, '') : ''
        if (base) return `${base}${path}`
      } catch (e) {}
      return path
    },

    // 滚动到底部时自动加载更多评论
    onScrollToLower() {
      // 如果正在加载或没有更多评论，则不加载
      if (this.isLoadingMoreComments || this.commentCount <= this.comments.length) {
        return
      }
      this.loadMoreComments()
    },
    
    async loadMoreComments() {
      try {
        if (!this.postId || this.isLoadingMoreComments) {
          return
        }
        
        // 如果已经加载了所有评论，不继续加载
        if (this.commentCount > 0 && this.comments.length >= this.commentCount) {
          return
        }
        
        this.isLoadingMoreComments = true
        
        // 调用API加载更多评论（如果API不支持分页，则重新加载所有评论）（不显示 loading）
        const response = await api.getPostComments(this.postId, this.currentUserId, false)
        
        if (response && response.data && response.data.length > 0) {
          // 过滤掉已经加载的评论（通过ID判断）
          const existingIds = this.comments.map(c => c.id)
          const newCommentsData = response.data.filter(comment => !existingIds.includes(comment.id))
          
          if (newCommentsData.length === 0) {
            uni.hideLoading()
            uni.showToast({
              title: '没有更多评论了',
              icon: 'none'
            })
            return
          }
          
          // 处理新加载的评论
          const newComments = await Promise.all(newCommentsData.map(async (comment) => {
            let isLiked = false
            if (comment.isLiked !== undefined && comment.isLiked !== null) {
              isLiked = comment.isLiked === true || comment.isLiked === 1
            } else if (comment.liked !== undefined && comment.liked !== null) {
              isLiked = comment.liked === true || comment.liked === 1
            }
            
            // 加载该评论的回复列表（不显示 loading）
            let replies = []
            try {
              const repliesResponse = await api.getCommentReplies(comment.id, this.currentUserId, false)
              if (repliesResponse && repliesResponse.data) {
                replies = repliesResponse.data.map(reply => {
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
                replies = replies.map(reply => {
                  let replyToUserName = null
                  if (reply.parentId === comment.id) {
                    replyToUserName = comment.userName
                  } else {
                    const repliedReply = replies.find(r => r.id === reply.parentId)
                    if (repliedReply) {
                      replyToUserName = repliedReply.userName
                    } else {
                      replyToUserName = comment.userName
                    }
                  }
                  return {
                    ...reply,
                    replyToUserName: replyToUserName
                  }
                })
              }
            } catch (error) {
              console.error('加载回复失败:', error)
            }
            
            const replyCount = comment.replyCount ?? comment.reply_count
            return {
              ...comment,
              isLiked: isLiked,
              likeCount: comment.likeCount || comment.like_count || comment.likesCount || 0,
              replies: Array.isArray(replies) ? replies : [],
              replyCount: replyCount == null ? undefined : Number(replyCount),
              repliesLoading: false
            }
          }))
          
          // 追加新评论到现有列表
          this.comments = [...this.comments, ...newComments]
        }
        
        this.isLoadingMoreComments = false
      } catch (error) {
        console.error('加载更多评论失败:', error)
        this.isLoadingMoreComments = false
      }
    },

    // 加载发帖人信息
    async loadUserInfo(userId) {
      try {
        const response = await api.getCurrentUser({ userId: userId })
        if ((response.code === 200 || response.code === 0) && response.data) {
          this.userCans = response.data.points || 0
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },

    // 处理图片URL
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },

    // 检查点赞和关注状态
    async checkLikeAndFollowStatus() {
      // 只有在用户登录时才检查状态
      if (!this.currentUserId || !this.postId) {
        return
      }

      // 确保帖子数据已加载
      if (!this.post || !this.post.userId) {
        return
      }

      try {
        // 检查点赞状态（从帖子详情中获取，如果后端已返回）
        if (this.post.isLiked !== undefined) {
          // 确保 isLiked 是布尔值
          this.isLiked = this.post.isLiked === true || this.post.isLiked === 1
        }

        // 检查关注状态
        try {
          const followResponse = await api.checkFollowStatus(this.currentUserId, this.post.userId)
          if (followResponse && (followResponse.code === 200 || followResponse.code === 0)) {
            this.isFollowing = followResponse.data || false
          }
        } catch (error) {
          // 如果检查失败，不影响页面显示
        }
      } catch (error) {
        // 静默处理错误
      }
    }
  }
}
</script>

<style scoped>
/* 帖子详情页面样式 - 抖音风格 */
.post-detail-page {
  height: 100vh;
  width: 100%;
  background-color: #000;
  position: relative;
  overflow: hidden;
}

/* 抖音风格视频容器 - 全屏，所有UI叠在视频上 */
.douyin-video-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  overflow: hidden;
}

/* 返回按钮 - 悬浮在视频左上角 */
.douyin-back-button {
  position: absolute;
  top: 40rpx;
  left: 30rpx;
  width: 70rpx;
  height: 70rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  backdrop-filter: blur(10rpx);
}

.douyin-more-button {
  position: absolute;
  top: 40rpx;
  right: 30rpx;
  width: 70rpx;
  height: 70rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  backdrop-filter: blur(10rpx);
}

.douyin-more-icon {
  font-size: 44rpx;
  color: #fff;
  line-height: 1;
}

.douyin-back-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

/* 视频播放器 - 全屏背景 */
.douyin-video-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.douyin-video-swiper {
  width: 100%;
  height: 100%;
}

.douyin-swiper-page {
  width: 100%;
  height: 100%;
  position: relative;
  background: #000;
}

.douyin-swiper-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
}

.douyin-swiper-placeholder-txt {
  color: rgba(255, 255, 255, 0.7);
  font-size: 26rpx;
}

.douyin-video-player {
  width: 100%;
  height: 100%;
  background-color: #000;
  object-fit: contain;
  display: block;
  z-index: 1;
}

/* 自定义播控层：不挡竖滑切视频（本层 pointer-events: none，仅子元素可点） */
.douyin-video-custom-ui {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 90;
  pointer-events: none;
}

.douyin-custom-center-play {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  pointer-events: auto;
}

.douyin-custom-center-play-inner {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.35);
}

.douyin-custom-center-play-tri {
  font-size: 56rpx;
  color: #fff;
  margin-left: 8rpx;
  line-height: 1;
}

.douyin-video-progress-row {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 12rpx 24rpx calc(12rpx + env(safe-area-inset-bottom));
  z-index: 95;
  pointer-events: none;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.55), transparent);
}

.douyin-video-progress-track {
  height: 6rpx;
  border-radius: 3rpx;
  background: rgba(255, 255, 255, 0.25);
  overflow: hidden;
  margin-bottom: 8rpx;
}

.douyin-video-progress-fill {
  height: 100%;
  border-radius: 3rpx;
  background: #fff;
  transition: width 0.12s linear;
}

.douyin-video-progress-meta {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: right;
  display: block;
}

/* 抖音风格加载和错误提示 */
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

.douyin-video-loading-overlay {
  background: transparent;
  padding: 0;
}

.douyin-loading-dot {
  color: rgba(255, 255, 255, 0.9);
  font-size: 36rpx;
  animation: douyin-loading-pulse 0.8s ease-in-out infinite;
}

@keyframes douyin-loading-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
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

/* 图片容器（无视频时显示） */
.douyin-image-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  overflow: hidden;
}

/* 图片容器（无视频时显示） */
.douyin-image-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  overflow: hidden;
}

/* 图片轮播（无视频时显示） */
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

/* 右侧互动按钮区域 */
/* 抖音风格：整体偏下，用下面空间；按钮之间间隔加大 */
.douyin-right-bar {
  position: absolute;
  right: 20rpx;
  top: 70%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 44rpx;
  z-index: 200;
}

/* 抖音风格用户头像 */
.douyin-user-avatar {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.douyin-avatar-img {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.4);
}

.douyin-follow-btn {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background-color: #ff4757;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -12rpx;
  border: 2rpx solid #000;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.3);
}

.douyin-follow-icon {
  font-size: 24rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

/* 图标与数字横向排列；尺寸参考抖音约 30–35px（约 60–70rpx） */
.douyin-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.douyin-action-icon {
  width: 64rpx;
  height: 64rpx;
  /* 视频帖专用图：/static/images/视频*.png（由 pages-community/static 同步到根 static，勿与图文帖 community-*.svg 混用） */
  filter: brightness(0) invert(1);
  transition: all 0.3s;
}

.douyin-action-icon.douyin-action-active {
  filter: none;
}

.douyin-action-count {
  font-size: 22rpx;
  color: #fff;
  text-align: center;
  font-weight: 500;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

/* 抖音风格底部信息栏 - 渐变遮罩，用户框缩小 */
.douyin-bottom-bar {
  position: absolute;
  bottom: 20rpx;
  left: 0;
  right: 100rpx;
  padding: 0 20rpx 8rpx;
  background: transparent;
  z-index: 150;
  pointer-events: none;
}

/* 有视频时底部留出进度条+时间区域 */
.douyin-bottom-bar--with-progress {
  bottom: 120rpx;
}

.douyin-user-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 10rpx;
  pointer-events: auto;
}

.douyin-user-row-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.douyin-user-info {
  margin-bottom: 8rpx;
  pointer-events: auto;
}

.douyin-username {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.6);
}

.douyin-content {
  margin-bottom: 0;
  max-width: 100%;
  pointer-events: auto;
}

.douyin-content-text {
  font-size: 24rpx;
  color: #fff;
  line-height: 1.5;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.6);
  word-break: break-word;
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

/* 评论面板 */
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
  height: 70%;
  max-height: 75vh;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.comment-panel-header {
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.comment-panel-close {
  font-size: 48rpx;
  color: #333;
  line-height: 1;
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 300;
}

.comment-panel-scroll {
  flex: 1;
  overflow-y: auto;
}

/* 评论输入面板：输入框内为文字+发图/@/表情，发送按钮在输入框外 */
/* @提及用户选择器 */
.mention-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.3);
  z-index: 1998;
}
.mention-picker {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: 160rpx;
  max-height: 400rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.15);
  z-index: 1999;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.mention-picker-title {
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  color: #999;
  border-bottom: 1rpx solid #f0f0f0;
}
.mention-picker-list {
  flex: 1;
  max-height: 340rpx;
  padding: 12rpx 0;
}
.mention-picker-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  gap: 20rpx;
}
.mention-picker-item:active {
  background: #f5f5f5;
}
.mention-picker-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
}
.mention-picker-name {
  font-size: 28rpx;
  color: #333;
}
.mention-picker-empty {
  padding: 40rpx;
  text-align: center;
  font-size: 26rpx;
  color: #999;
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
  box-sizing: border-box;
}

.comment-input-display {
  position: absolute;
  left: 24rpx;
  right: 120rpx;
  top: 12rpx;
  bottom: 12rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  align-content: center;
  overflow: hidden;
  pointer-events: none;
  z-index: 2;
}

.comment-input-display-text {
  font-size: 28rpx;
  line-height: 1.4;
  color: #333;
  word-break: break-all;
}

/* 一格一字宽（28rpx）光标对齐，表情图 36rpx 与文字同大 */
.comment-input-display-emoji-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28rpx;
  flex-shrink: 0;
  vertical-align: middle;
  overflow: visible;
}

.comment-input-display-emoji {
  width: 36rpx;
  height: 36rpx;
}

.comment-input-placeholder {
  font-size: 28rpx;
  color: #999;
}

.comment-input-inner .comment-input {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.4;
  height: 48rpx;
  min-width: 0;
  padding-left: 0;
  padding-right: 0;
  box-sizing: content-box;
}

.comment-input-real {
  position: relative;
  z-index: 1;
  color: transparent !important;
  caret-color: #333;
  -webkit-text-fill-color: transparent;
  /* 无额外留白，指标刚好在文字/表情后面 */
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.comment-input-real::placeholder {
  color: transparent;
}

.comment-images-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 16rpx 0 0 0;
}

.comment-image-item {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
}

.comment-image-thumb {
  width: 100%;
  height: 100%;
}

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

.comment-input-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
  margin-left: 8rpx;
}

.input-action-icon {
  width: 40rpx;
  height: 40rpx;
  font-size: 32rpx;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-send-btn {
  flex-shrink: 0;
  padding: 16rpx 28rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
  line-height: 1.4;
}

.comment-send-btn:active {
  opacity: 0.9;
}

/* 评论内容中的表情/图片内联展示 */
.comment-text .comment-text-inline {
  font-size: 26rpx;
  line-height: 1.6;
  word-break: break-word;
}

.comment-text .comment-text-inline {
  color: #333;
}

.comment-text .comment-text-inline.comment-mention {
  color: #1e6fff;
}

.comment-emoji-inline {
  width: 36rpx;
  height: 36rpx;
  vertical-align: middle;
  display: inline-block;
}

.comment-image-inline {
  max-width: 280rpx;
  max-height: 280rpx;
  border-radius: 8rpx;
  margin-top: 8rpx;
  display: block;
}

.comment-list {
  padding: 20rpx 0 0 0;
}

.comment-count-header {
  padding: 20rpx 30rpx 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.comment-count-text {
  font-size: 24rpx;
  color: #999;
}

.comment-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 20rpx;
  padding: 0 30rpx 20rpx 30rpx;
}

.comment-item-main {
  display: flex;
  width: 100%;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 20rpx;
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
  min-width: 0;
}

.comment-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
  min-width: 0;
}

.comment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-right: 20rpx;
}

.comment-user-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 4rpx;
}

.comment-user {
  font-size: 26rpx;
  color: #999;
  font-weight: 500;
}

.author-tag {
  font-size: 20rpx;
  color: #ff4757;
  background-color: rgba(255, 71, 87, 0.1);
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  line-height: 1.2;
}

.comment-text {
  font-size: 26rpx;
  line-height: 1.6;
  color: #333;
  margin: 6rpx 0;
  word-break: break-all;
}

.comment-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.comment-meta-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
  flex-wrap: wrap;
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
  flex-shrink: 0;
  padding-left: 10rpx;
}

.like-icon {
  width: 32rpx;
  height: 32rpx;
  font-size: 0;
}

.like-icon.liked {
  filter: none;
}

.like-count {
  font-size: 22rpx;
  color: #999;
  line-height: 1;
  margin-left: 4rpx;
  white-space: nowrap;
}

.like-count.liked {
  color: #ff4757;
}



/* 回复相关样式 */
.replies-section {
  margin-top: 16rpx;
  margin-left: 84rpx;
  padding-left: 0;
  width: calc(100% - 84rpx);
}

.reply-item {
  display: flex;
  margin-bottom: 16rpx;
  padding-left: 0;
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
  margin-left: 8rpx;
}

.reply-arrow {
  font-size: 20rpx;
  color: #999;
  transform: scale(0.8);
}

.reply-to-user {
  font-size: 26rpx;
  color: #999;
}

.reply-user {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.reply-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
  margin: 8rpx 0;
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
  gap: 16rpx;
  flex: 1;
  min-width: 0;
  flex-wrap: wrap;
}

.reply-time {
  font-size: 24rpx;
  color: #999;
}

.reply-location {
  font-size: 24rpx;
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
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.reply-reply-btn:active {
  opacity: 0.7;
}

.reply-like {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}

.reply-like .like-icon {
  width: 32rpx;
  height: 32rpx;
}

.reply-like .like-count {
  font-size: 24rpx;
  color: #999;
  margin-left: 4rpx;
}

.reply-like .like-count.liked {
  color: #ff4757;
}

.expand-replies {
  margin-top: 12rpx;
  padding: 8rpx 0;
  padding-left: 84rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.expand-replies:active {
  opacity: 0.7;
}

.expand-text {
  font-size: 24rpx;
  color: #999;
}

.expand-arrow {
  font-size: 18rpx;
  color: #999;
}

/* 与帖子详情页一致的展开/收起回复样式 */
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
  font-size: 24rpx;
  color: #999;
}

.xh-reply-reply-btn {
  font-size: 24rpx;
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
  width: 32rpx;
  height: 32rpx;
}

.xh-reply-actions-right .xh-action-num {
  font-size: 24rpx;
  color: #999;
  margin-left: 0;
}

.xh-action-num.xh-num-liked {
  color: #ff6b81;
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

.xh-expand-collapse {
  margin-left: 0;
}

/* 回复提示样式 */
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
  height: 72rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #f5f5f5;
  border-radius: 36rpx;
  padding: 0 28rpx;
  border: none;
  outline: none;
}

.comment-input::placeholder {
  color: #999;
}
</style>
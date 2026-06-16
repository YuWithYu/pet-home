<template>
	<view class="container">
		<!-- 评价列表 -->
		<view class="reviews-section">
			<view class="reviews-list" v-if="reviewsList && reviewsList.length > 0">
				<view class="review-item" v-for="(review, index) in reviewsList" :key="index">
					<!-- 用户信息 -->
					<view class="review-header">
						<image 
							class="review-avatar" 
							:src="getReviewImageUrl(review.userAvatar)" 
							mode="aspectFill"
						></image>
						<view class="review-user-info">
							<text class="review-username">{{ review.userName || '匿名用户' }}</text>
							<text class="purchase-text" v-if="review.productName">购买了{{ review.productName }}</text>
						</view>
					</view>
					
					<!-- 评价内容 -->
					<view class="review-content">
						<text class="review-text" :class="{ 'expanded': review.expanded }">{{ review.comment || review.content || '暂无评价内容' }}</text>
						<text class="expand-btn" v-if="!review.expanded && shouldShowExpandBtn(review)" @click="expandReview(review)">展开√</text>
					</view>
					
					<!-- 评价图片和视频 -->
					<view class="review-images" v-if="getReviewImages(review).length > 0 || getReviewVideos(review).length > 0">
						<image 
							class="review-image" 
							v-for="(img, imgIndex) in getReviewImages(review).slice(0, 3)" 
							:key="imgIndex"
							:src="getReviewImageUrl(img)" 
							mode="aspectFill"
							@click="previewReviewImages(review, imgIndex)"
						></image>
						<view 
							class="review-video" 
							v-for="(video, videoIndex) in getReviewVideos(review).slice(0, 3)" 
							:key="videoIndex"
							@click="previewReviewVideo(review, videoIndex)"
						>
							<image 
								class="review-image" 
								:src="getReviewVideoThumbnail(video)" 
								mode="aspectFill"
							></image>
							<view class="video-play-icon">▶</view>
						</view>
						<view class="image-count" v-if="getReviewImages(review).length + getReviewVideos(review).length > 3">
							{{ getReviewImages(review).length + getReviewVideos(review).length }}图
						</view>
					</view>
					
					<!-- 追评提示 -->
					<view class="review-followup" v-if="review.followupComment">
						<text class="followup-label">用户1天内追评</text>
					</view>
					
					<!-- 评价时间和操作按钮 -->
					<view class="review-footer">
						<text class="review-time">{{ formatReviewTime(review.createTime) }}</text>
						<view class="review-actions">
							<view class="review-action-item" :class="{ 'liked': review.isLiked }" @tap.stop="() => toggleLikeReview(review)">
								<image class="action-icon-image" :src="review.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'" mode="aspectFit"></image>
								<text class="action-count">{{ review.likeCount || 0 }}</text>
							</view>
							<view class="review-action-item" @tap.stop="() => showReplyInput(review)">
								<image class="action-icon-image" src="/static/images/在线咨询.png" mode="aspectFit"></image>
								<text class="action-count">{{ review.replyCount || 0 }}</text>
							</view>
						</view>
					</view>
					
					<!-- 回复列表 -->
					<view class="review-replies" v-if="review.replies && review.replies.length > 0">
						<view class="reply-item" v-for="(reply, replyIndex) in review.replies" :key="replyIndex">
							<text class="reply-username">{{ reply.userName || '匿名用户' }}:</text>
							<text class="reply-content">{{ reply.content }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-reviews" v-else>
				<text class="empty-text">暂无评价</text>
			</view>
			
			<!-- 加载更多 -->
			<view class="load-more" v-if="reviewsList && reviewsList.length > 0 && reviewsHasMore">
				<text class="load-more-text" @click="loadMoreReviews">加载更多</text>
			</view>
			
			<!-- 回复输入框 -->
			<view class="reply-input-bar" v-if="replyingToReview">
				<input 
					class="reply-input" 
					v-model="replyText" 
					placeholder="热情回复,文明用语"
					@confirm="submitReply"
				/>
				<text class="reply-send-btn" @click="submitReply">发送</text>
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
			productId: null, // 商品ID
			productInfo: null, // 商品信息
			reviewsList: [], // 评价列表
			reviewsPage: 1, // 评价页码
			reviewsPageSize: 10, // 每页评价数量
			reviewsHasMore: true, // 是否还有更多评价
			reviewsLoading: false, // 是否正在加载评价
			replyingToReview: null, // 当前正在回复的评价
			replyText: '' // 回复内容
		}
	},
	onLoad(options) {
		if (options.productId) {
			this.productId = options.productId
			// 加载评价列表
			this.loadReviewsList()
		} else {
			uni.showToast({
				title: '商品ID缺失',
				icon: 'none'
			})
			setTimeout(() => {
				uni.navigateBack()
			}, 1500)
		}
	},
	onReachBottom() {
		// 上拉加载更多
		if (this.reviewsHasMore && !this.reviewsLoading) {
			this.loadMoreReviews()
		}
	},
	methods: {
		// 加载评价列表
		async loadReviewsList() {
			if (!this.productId) {
				this.reviewsList = []
				return
			}
			
			if (this.reviewsLoading || (!this.reviewsHasMore && this.reviewsPage > 1)) {
				return
			}
			
			this.reviewsLoading = true
			
		try {
			const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid')
			
			// 如果还没有获取商品信息，先获取商品信息
			if (!this.productInfo && this.reviewsPage === 1) {
				try {
					const productResponse = await api.request({
						url: `/api/product/${this.productId}`,
						method: 'GET',
						showLoading: false
					})
					if (productResponse && (productResponse.code === 200 || productResponse.code === 0) && productResponse.data) {
						this.productInfo = productResponse.data
					}
				} catch (error) {
					// 静默处理错误
				}
			}
			
			// 调用后端API获取商品评价
			const response = await api.request({
				url: `/api/product/${this.productId}/reviews`,
				method: 'GET',
				data: {
					page: this.reviewsPage,
					pageSize: this.reviewsPageSize,
					userId: userId || null // 传递userId以获取点赞状态
				},
				showLoading: this.reviewsPage === 1
			})
			
			if (response && (response.code === 200 || response.code === 0)) {
				let reviews = []
				if (Array.isArray(response.data)) {
					reviews = response.data
				} else if (response.data && Array.isArray(response.data.list)) {
					reviews = response.data.list
				} else if (response.data && Array.isArray(response.data.records)) {
					reviews = response.data.records
				}
				
				// 为每个评价添加商品信息和初始化属性
				reviews.forEach(review => {
					// 设置商品名称
					if (this.productInfo && this.productInfo.name) {
						review.productName = this.productInfo.name
						review.productImage = this.productInfo.image
					}
					// 初始化expanded属性
					if (!review.hasOwnProperty('expanded')) {
						this.$set(review, 'expanded', false)
					}
				})
					
					// 加载每个评价的回复列表
					for (const review of reviews) {
						if (review.id) {
							await this.loadReviewReplies(review)
						}
					}
					
					if (this.reviewsPage === 1) {
						this.reviewsList = reviews
					} else {
						this.reviewsList = [...this.reviewsList, ...reviews]
					}
					
					// 判断是否还有更多数据
					if (reviews.length < this.reviewsPageSize) {
						this.reviewsHasMore = false
					} else {
						this.reviewsPage++
					}
				} else {
					throw new Error(response.msg || '加载评价失败')
				}
			} catch (error) {
				console.error('加载评价列表失败:', error)
				uni.showToast({
					title: error.message || '加载评价失败',
					icon: 'none'
				})
			} finally {
				this.reviewsLoading = false
			}
		},
		// 加载更多评价
		loadMoreReviews() {
			if (this.reviewsHasMore && !this.reviewsLoading) {
				this.loadReviewsList()
			}
		},
		// 展开评价
		expandReview(review) {
			if (!review || typeof review !== 'object') {
				return
			}
			const index = this.reviewsList.findIndex(r => r.id === review.id || r === review)
			if (index !== -1) {
				this.$set(this.reviewsList[index], 'expanded', true)
			} else {
				this.$set(review, 'expanded', true)
			}
		},
		// 判断是否应该显示展开按钮
		shouldShowExpandBtn(review) {
			if (!review || (!review.comment && !review.content)) return false
			const text = review.comment || review.content || ''
			if (!text || text.length < 50) return false
			return text.length > 80
		},
		// 获取评价图片列表
		getReviewImages(review) {
			if (!review) return []
			
			if (!review.images || review.images === '' || review.images === 'null') return []
			
			if (Array.isArray(review.images)) {
				return review.images.filter(img => img && img.trim() !== '')
			}
			
			if (typeof review.images === 'string') {
				try {
					const parsed = JSON.parse(review.images)
					if (Array.isArray(parsed)) {
						return parsed.filter(img => img && img.trim() !== '')
					}
				} catch (e) {
					// JSON解析失败，当作普通字符串处理
				}
				
				if (review.images.includes(',')) {
					return review.images.split(',').map(img => img.trim()).filter(img => img && img !== '')
				}
				
				if (review.images.trim() !== '') {
					return [review.images.trim()]
				}
			}
			
			return []
		},
		// 获取评价视频列表
		getReviewVideos(review) {
			if (!review || !review.videos) return []
			if (Array.isArray(review.videos)) return review.videos
			if (typeof review.videos === 'string') {
				if (review.videos.includes(',')) {
					return review.videos.split(',').map(v => v.trim()).filter(v => v)
				}
				try {
					const parsed = JSON.parse(review.videos)
					if (Array.isArray(parsed)) return parsed
				} catch (e) {
					return [review.videos]
				}
			}
			return []
		},
		// 获取视频缩略图
		getReviewVideoThumbnail(videoUrl) {
			return '/static/images/video-placeholder.png'
		},
		// 预览视频
		previewReviewVideo(review, currentIndex) {
			uni.showToast({
				title: '视频播放功能开发中',
				icon: 'none'
			})
		},
		// 获取评价图片URL
		getReviewImageUrl(url) {
			if (!url) return '/static/images/garfield-default-avatar.png'
			return util.getImageUrl(url)
		},
		// 预览评价图片
		previewReviewImages(review, currentIndex) {
			const images = this.getReviewImages(review).map(img => this.getReviewImageUrl(img))
			uni.previewImage({
				urls: images,
				current: currentIndex
			})
		},
		// 格式化评价时间
		formatReviewTime(timeStr) {
			if (!timeStr) return ''
			const formattedTimeStr = timeStr.replace(/-/g, '/')
			const date = new Date(formattedTimeStr)
			const now = new Date()
			const diff = now.getTime() - date.getTime()
			const days = Math.floor(diff / (1000 * 60 * 60 * 24))
			
			if (days === 0) return '今天'
			if (days === 1) return '昨天'
			if (days < 7) return `${days}天前`
			
			const year = date.getFullYear()
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			const hours = String(date.getHours()).padStart(2, '0')
			const minutes = String(date.getMinutes()).padStart(2, '0')
			
			return `${year}-${month}-${day} ${hours}:${minutes}`
		},
		// 切换点赞状态
		async toggleLikeReview(review) {
			if (!review || !review.id) {
				uni.showToast({
					title: '评价信息错误',
					icon: 'none'
				})
				return
			}
			
			try {
				const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid')
				if (!userId) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				const response = await api.request({
					url: `/api/product/review/${review.id}/like`,
					method: 'POST',
					data: {
						userId: userId
					}
				})
				
				if (response.code === 200 && response.data) {
					const isLiked = response.data.isLiked
					const likeCount = response.data.likeCount || 0
					
					const index = this.reviewsList.findIndex(r => r.id === review.id)
					if (index !== -1) {
						this.$set(this.reviewsList[index], 'isLiked', isLiked)
						this.$set(this.reviewsList[index], 'likeCount', likeCount)
					}
				} else {
					throw new Error(response.msg || '操作失败')
				}
			} catch (error) {
				console.error('点赞失败:', error)
				uni.showToast({
					title: error.message || '操作失败，请稍后重试',
					icon: 'none'
				})
			}
		},
		// 显示回复输入框
		async showReplyInput(review) {
			if (!review || !review.id) {
				uni.showToast({
					title: '评价信息错误',
					icon: 'none'
				})
				return
			}
			
			if (!review.replies || review.replies.length === 0) {
				await this.loadReviewReplies(review)
			}
			
			this.replyingToReview = review
			this.replyText = ''
		},
		// 加载评论的回复列表
		async loadReviewReplies(review) {
			if (!review || !review.id) {
				return
			}
			
			try {
				const response = await api.request({
					url: `/api/product/review/${review.id}/replies`,
					method: 'GET',
					showLoading: false
				})
				
				if (response && (response.code === 200 || response.code === 0)) {
					let replies = []
					if (Array.isArray(response.data)) {
						replies = response.data
					} else if (response.data && Array.isArray(response.data.list)) {
						replies = response.data.list
					}
					
					const index = this.reviewsList.findIndex(r => r.id === review.id)
					if (index !== -1) {
						this.$set(this.reviewsList[index], 'replies', replies)
						this.$set(this.reviewsList[index], 'replyCount', replies.length)
					}
				}
			} catch (error) {
				// 静默处理错误
			}
		},
		// 提交回复
		async submitReply() {
			if (!this.replyingToReview || !this.replyText.trim()) {
				uni.showToast({
					title: '请输入回复内容',
					icon: 'none'
				})
				return
			}
			
			try {
				const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid')
				if (!userId) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				const response = await api.request({
					url: `/api/product/review/${this.replyingToReview.id}/reply`,
					method: 'POST',
					data: {
						userId: userId,
						content: this.replyText.trim()
					}
				})
				
				if (response && (response.code === 200 || response.code === 0)) {
					const newReply = response.data
					
					// 添加到回复列表
					const index = this.reviewsList.findIndex(r => r.id === this.replyingToReview.id)
					if (index !== -1) {
						if (!this.reviewsList[index].replies) {
							this.$set(this.reviewsList[index], 'replies', [])
						}
						this.reviewsList[index].replies.push(newReply)
						this.$set(this.reviewsList[index], 'replyCount', (this.reviewsList[index].replyCount || 0) + 1)
					}
					
					this.replyText = ''
					this.replyingToReview = null
					
					uni.showToast({
						title: '回复成功',
						icon: 'success'
					})
				} else {
					throw new Error(response.msg || '回复失败')
				}
			} catch (error) {
				console.error('提交回复失败:', error)
				uni.showToast({
					title: error.message || '回复失败，请稍后重试',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

.reviews-section {
	padding: 20rpx;
	background-color: #ffffff;
}

.reviews-list {
	.review-item {
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
		
		&:last-child {
			border-bottom: none;
		}
	}
}

.review-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.review-avatar {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.review-user-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.review-username {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	line-height: 1.4;
}

.purchase-text {
	font-size: 24rpx;
	color: #999;
	line-height: 1.4;
}

.review-content {
	margin-bottom: 16rpx;
}

.review-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.6;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 3;
	line-clamp: 3;
	overflow: hidden;
	word-break: break-all;
	
	&.expanded {
		-webkit-line-clamp: unset;
		line-clamp: unset;
		display: block;
	}
}

.expand-btn {
	font-size: 24rpx;
	color: #999;
	margin-left: 8rpx;
}

.review-images {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-bottom: 16rpx;
	position: relative;
}

.review-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 8rpx;
}

.review-video {
	position: relative;
	width: 200rpx;
	height: 200rpx;
	border-radius: 8rpx;
	overflow: hidden;
	
	.review-image {
		width: 100%;
		height: 100%;
	}
	
	.video-play-icon {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		color: #ffffff;
		font-size: 32rpx;
		padding-left: 4rpx;
	}
}

.image-count {
	position: absolute;
	bottom: 12rpx;
	right: 12rpx;
	background-color: rgba(0, 0, 0, 0.6);
	color: #ffffff;
	font-size: 20rpx;
	padding: 4rpx 12rpx;
	border-radius: 4rpx;
}

.review-followup {
	margin-bottom: 12rpx;
}

.followup-label {
	font-size: 24rpx;
	color: #999;
}

.review-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 20rpx;
}

.review-time {
	font-size: 24rpx;
	color: #999;
}

.review-actions {
	display: flex;
	align-items: center;
	gap: 30rpx;
}

.review-action-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	cursor: pointer;
	padding: 8rpx 12rpx;
	
	&.liked {
		.action-count {
			color: #ff6b00;
		}
	}
}

.action-icon-image {
	width: 32rpx;
	height: 32rpx;
}

.action-count {
	font-size: 24rpx;
	color: #999;
}

.review-replies {
	margin-top: 20rpx;
	padding: 20rpx;
	background-color: #f5f5f5;
	border-radius: 8rpx;
}

.reply-item {
	margin-bottom: 12rpx;
	font-size: 26rpx;
	line-height: 1.6;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.reply-username {
	color: #666;
	font-weight: 500;
	margin-right: 8rpx;
}

.reply-content {
	color: #333;
}

.reply-input-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: #fff;
	border-top: 1px solid #e5e5e5;
	z-index: 999;
}

.reply-input {
	flex: 1;
	padding: 20rpx 24rpx;
	background-color: #f5f5f5;
	border-radius: 40rpx;
	font-size: 28rpx;
	color: #333;
}

.reply-send-btn {
	padding: 20rpx 30rpx;
	margin-left: 20rpx;
	background-color: #ff6b00;
	color: #fff;
	border-radius: 40rpx;
	font-size: 28rpx;
}

.empty-reviews {
	padding: 100rpx 0;
	text-align: center;
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
}

.load-more {
	padding: 40rpx 0;
	text-align: center;
	
	.load-more-text {
		font-size: 28rpx;
		color: #666;
		padding: 16rpx 40rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
	}
}
</style>

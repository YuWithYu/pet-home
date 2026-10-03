<template>
	<view class="share-mask" :class="maskClass" @click="toggleMask">
		<view class="share-content" @click.stop="stopPrevent">
			<view class="share-title">分享到</view>
			<view class="share-list">
				<view 
					class="share-item" 
					v-for="(item, index) in shareList" 
					:key="index"
					@click="handleShare(item)"
				>
					<image v-if="item.icon" class="share-icon" :src="item.icon" mode="aspectFit"></image>
					<text v-else class="share-emoji">{{item.emoji || '📤'}}</text>
					<text class="share-text">{{item.text}}</text>
				</view>
			</view>
			<view class="share-cancel" @click="toggleMask">取消</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'Share',
	props: {
		contentHeight: {
			type: Number,
			default: 580
		},
		shareList: {
			type: Array,
			default: () => []
		}
	},
	data() {
		return {
			maskClass: 'none'
		}
	},
	methods: {
		toggleMask() {
			if (this.maskClass === 'show') {
				this.maskClass = 'hide';
				setTimeout(() => {
					this.maskClass = 'none';
				}, 250);
			} else if (this.maskClass === 'none') {
				this.maskClass = 'show';
			}
		},
		stopPrevent() {},
		handleShare(item) {
			uni.showToast({
				title: `分享到${item.text}`,
				icon: 'none'
			});
			this.toggleMask();
		}
	}
}
</script>

<style lang="scss" scoped>
.share-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 9999;
	
	&.none {
		display: none;
	}
	
	&.show {
		display: block;
		.share-content {
			transform: translateY(0);
		}
	}
	
	&.hide {
		.share-content {
			transform: translateY(100%);
		}
	}
}

.share-content {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	border-radius: 20upx 20upx 0 0;
	padding: 30upx;
	transform: translateY(100%);
	transition: transform 0.3s ease;
}

.share-title {
	text-align: center;
	font-size: 32upx;
	color: #333;
	margin-bottom: 30upx;
}

.share-list {
	display: flex;
	justify-content: space-around;
	padding: 20upx 0;
}

.share-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.share-icon {
	width: 80upx;
	height: 80upx;
	margin-bottom: 10upx;
}

.share-emoji {
	font-size: 60upx;
	margin-bottom: 10upx;
}

.share-text {
	font-size: 24upx;
	color: #666;
}

.share-cancel {
	text-align: center;
	font-size: 32upx;
	color: #333;
	padding: 30upx 0;
	border-top: 1px solid #f0f0f0;
	margin-top: 20upx;
}
</style>

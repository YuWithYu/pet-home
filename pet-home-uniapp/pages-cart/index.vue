<template>
	<view class="cart-container">
		<!-- 空白页面-->
		<view v-if="!isLoggedIn || empty === true" class="empty">
			<view v-if="isLoggedIn" class="empty-tips">
				空空如也
				<navigator class="navigator" url="/pages/index/index" open-type="switchTab">随便逛逛</navigator>
			</view>
			<view v-else class="empty-tips">
				空空如也
				<view class="navigator" @click="navToLogin">去登录</view>
			</view>
		</view>
		<view v-else>
			<!-- 按店铺分组的购物车列表-->
			<scroll-view class="cart-scroll" scroll-y>
				<view class="store-group" v-for="(group, gIdx) in groupedCart" :key="gIdx">
					<!-- 店铺头部 -->
					<view class="store-header">
						<view class="store-checkbox" :class="{ checked: group.storeChecked }" @click="toggleStoreCheck(gIdx)">
							<text v-if="group.storeChecked" class="checkmark">✓</text>
						</view>
						<view class="store-name-wrap" @click="goToStoreByGroup(group)">
							<text class="store-name">{{ group.storeName }}</text>
							<text class="store-arrow">></text>
						</view>
					</view>
					<!-- 店铺商品 -->
					<view class="product-card" v-for="(item, idx) in group.items" :key="item.id">
						<view class="product-main" @click="goToDetail(item)">
							<view class="product-checkbox" :class="{ checked: item.checked }" @click.stop="check('item', getFlatIndex(gIdx, idx))">
								<text v-if="item.checked" class="checkmark">✓</text>
							</view>
							<view class="product-image-wrap">
								<image :src="getCartItemImage(item)" mode="aspectFill" class="product-image"
									@error="onImageError(getFlatIndex(gIdx, idx))"></image>
								<view class="product-banner" v-if="item.banner">{{ item.banner }}</view>
							</view>
							<view class="product-info">
								<text class="product-name clamp-2">{{ item.productName || item.name }}</text>
								<view class="product-spec" v-if="item.spDataStr || item.attr">
									<text class="spec-tag">{{ item.spDataStr || item.attr }}</text>
								</view>
								<view class="product-price-row">
									<text class="price-current">¥{{ item.price }}</text>
									<text class="price-original" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
								</view>
								<view class="product-actions">
									<view class="quantity-box">
										<view class="qty-btn minus" @click.stop="changeQty(getFlatIndex(gIdx, idx), -1)"></view>
										<input class="qty-input" type="number" :value="item.quantity" disabled />
										<view class="qty-btn plus" @click.stop="changeQty(getFlatIndex(gIdx, idx), 1)"></view>
									</view>
									<text class="del-btn" @click.stop="handleDeleteCartItem(getFlatIndex(gIdx, idx))">删除</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>

			<!-- 底部结算按钮-->
			<view class="bottom-bar">
				<view class="bottom-left" @click="check('all')">
					<view class="bottom-checkbox" :class="{ checked: allChecked }">
						<text v-if="allChecked" class="checkmark">✓</text>
					</view>
					<text class="select-all">全选</text>
					<text class="total-info">已选{{ checkedCount }}件合计:<text class="total-price">¥{{ total }}</text></text>
				</view>
				<button class="checkout-btn" @click="createOrder">去结算</button>
			</view>
		</view>
	</view>
</template>

<script>
	import cartApi from '@/common/js/api/cart.js'
	import { util } from '@/common/js/util.js'

	export default {
		data() {
			return {
				total: 0,
				allChecked: false,
				empty: false,
				cartList: [],
				isLoggedIn: false,
			}
		},
		computed: {
			hasLogin() {
				const token = (this.$store && this.$store.state.token) || uni.getStorageSync('token')
				return !!token
			},
			checkedCount() {
				return this.cartList.filter(i => i.checked).reduce((sum, i) => sum + i.quantity, 0)
			},
			groupedCart() {
				const list = this.cartList
				const map = {}
				list.forEach(item => {
					const sid = item.storeId || (item.storeInfo && item.storeInfo.id) || 'default'
					const sname = item.storeName || (item.storeInfo && item.storeInfo.name) || '宠物家商品专卖店'
					const key = String(sid)
					if (!map[key]) {
						map[key] = { storeId: sid, storeName: sname, items: [] }
					}
					map[key].items.push(item)
				})
				return Object.values(map).map(g => ({
					...g,
					storeChecked: g.items.length > 0 && g.items.every(i => i.checked)
				}))
			}
		},
		onLoad() {
			this.$store.dispatch('initUserInfo')
			const token = uni.getStorageSync('token')
			this.isLoggedIn = !!token
		},
		onShow() {
			const token = uni.getStorageSync('token')
			this.isLoggedIn = !!token
			if (token && this.$store && !this.$store.state.token) {
				this.$store.commit('SET_TOKEN', token)
			}
			this.loadData()
		},
		watch: {
			cartList(e) {
				this.empty = e.length === 0
			}
		},
		methods: {
			getFlatIndex(gIdx, idx) {
				let i = 0
				for (let g = 0; g < gIdx; g++) i += this.groupedCart[g].items.length
				return i + idx
			},
			toggleStoreCheck(gIdx) {
				const group = this.groupedCart[gIdx]
				const target = !group.storeChecked
				group.items.forEach(item => { item.checked = target })
				this.calcTotal()
			},
			goToDetail(item) {
				if (item.productId) {
					uni.navigateTo({ url: `/pages-goods/detail?id=${item.productId}` })
				}
			},
			async loadData() {
				const token = uni.getStorageSync('token')
				if (!token) {
					this.empty = true
					return
				}
				let userId = null
				const storageUserInfo = uni.getStorageSync('userInfo')
				if (storageUserInfo) userId = storageUserInfo.id || storageUserInfo.uid
				if (!userId) {
					const staffInfo = uni.getStorageSync('staffInfo') || {}
					userId = staffInfo.adminId || null
				}
				if (!userId && this.$store && this.$store.state.userInfo) {
					userId = this.$store.state.userInfo.id || this.$store.state.userInfo.uid
				}
				if (!userId) {
					try {
						const userRes = await api.getCurrentUser()
						if (userRes && userRes.code === 200 && userRes.data) {
							userId = userRes.data.id || userRes.data.uid
							if (userId) uni.setStorageSync('userInfo', userRes.data)
						}
					} catch (e) {}
				}
				if (!userId) {
					this.empty = true
					this.cartList = []
					return
				}
				try {
					const response = await cartApi.fetchCartList(userId)
					if (response && (response.code === 200 || response.code === 0)) {
						let list = []
						if (Array.isArray(response.data)) list = response.data
						else if (response.data && Array.isArray(response.data.list)) list = response.data.list
						else if (response.data && Array.isArray(response.data.records)) list = response.data.records
						const cartList = list.map(item => {
							item.checked = true
							let spDataStr = ''
							if (item.productAttr) {
								try {
									const spDataArr = typeof item.productAttr === 'string' ? JSON.parse(item.productAttr) : item.productAttr
									if (Array.isArray(spDataArr)) {
										spDataArr.forEach(attr => {
											if (attr.key) spDataStr += attr.key + ':'
											if (attr.value) spDataStr += attr.value + ';'
										})
									}
								} catch (e) {}
							}
							item.spDataStr = spDataStr
							const pic = item.productPic || item.productImage || item.image
							item.productPic = pic ? (util.getImageUrl ? util.getImageUrl(pic) : pic) : '/static/images/default-product.svg'
							return item
						})
						this.cartList = cartList
						this.calcTotal()
					} else {
						this.empty = true
					}
				} catch (error) {
					console.error('加载购物车失败', error)
					this.empty = true
					uni.showToast({ title: '加载购物车失败', icon: 'none' })
				}
			},
			getCartItemImage(item) {
				const pic = item.productPic || item.productImage || item.image
				if (pic) return util.getImageUrl ? util.getImageUrl(pic) : pic
				return '/static/images/default-product.svg'
			},
			onImageError(flatIdx) {
				this.$set(this.cartList[flatIdx], 'productPic', '/static/images/default-product.svg')
			},
			navToLogin() {
				uni.navigateTo({ url: '/pages-auth/login' })
			},
			check(type, index) {
				if (type === 'item') {
					this.cartList[index].checked = !this.cartList[index].checked
				} else {
					const checked = !this.allChecked
					this.cartList.forEach(item => { item.checked = checked })
					this.allChecked = checked
				}
				this.calcTotal()
			},
			changeQty(flatIdx, delta) {
				const item = this.cartList[flatIdx]
				let qty = (item.quantity || 1) + delta
				qty = Math.max(1, Math.min(100, qty))
				this.doUpdateQty(item, qty, flatIdx)
			},
			async doUpdateQty(item, qty, flatIdx) {
				try {
					const res = await cartApi.updateQuantity({ id: item.id, quantity: qty })
					if (res && (res.code === 200 || res.code === 0)) {
						this.cartList[flatIdx].quantity = qty
						this.calcTotal()
					} else {
						uni.showToast({ title: res?.msg || '更新数量失败', icon: 'none' })
					}
				} catch (e) {
					uni.showToast({ title: '更新数量失败，请重试', icon: 'none' })
				}
			},
			async handleDeleteCartItem(flatIdx) {
				const row = this.cartList[flatIdx]
				uni.showModal({
					content: '确定要删除这个商品吗?',
					success: async (e) => {
						if (e.confirm) {
							try {
								const res = await cartApi.deleteCartItem({ ids: row.id })
								if (res && (res.code === 200 || res.code === 0)) {
									this.cartList.splice(flatIdx, 1)
									this.calcTotal()
									uni.showToast({ title: '删除成功', icon: 'success' })
								} else {
									uni.showToast({ title: res?.msg || '删除失败', icon: 'none' })
								}
							} catch (err) {
								uni.showToast({ title: '删除失败，请重试', icon: 'none' })
							}
						}
					}
				})
			},
			calcTotal() {
				const list = this.cartList
				if (list.length === 0) {
					this.empty = true
					return
				}
				let total = 0
				let checked = true
				list.forEach(item => {
					if (item.checked) total += (parseFloat(item.price) || 0) * (item.quantity || 1)
					else if (checked) checked = false
				})
				this.allChecked = checked
				this.total = Number(total.toFixed(2))
			},
			goToStoreByGroup(group) {
				if (group.items && group.items[0]) {
					this.goToStore(group.items[0])
				}
			},
			async goToStore(item) {
				let storeId = item.storeId || (item.storeInfo && item.storeInfo.id)
				const storeName = item.storeName || (item.storeInfo && item.storeInfo.name) || '未知店铺'
				if (!storeId && item.productId) {
					try {
						uni.showLoading({ title: '加载中...', })
						const res = await api.getProductDetail(item.productId)
						uni.hideLoading()
						if (res && res.code === 200 && res.data) storeId = res.data.storeId || res.data.store_id
					} catch (e) { uni.hideLoading() }
				}
				if (storeId) {
					uni.navigateTo({ url: `/pages-goods/list?storeId=${storeId}&storeName=${encodeURIComponent(storeName)}` })
				} else {
					uni.showToast({ title: '无法获取店铺信息', icon: 'none' })
				}
			},
			createOrder() {
				const cartIds = this.cartList.filter(i => i.checked).map(i => i.id)
				if (cartIds.length === 0) {
					uni.showToast({ title: '您还未选择要下单的商品', duration: 1000, icon: 'none' })
					return
				}
				uni.navigateTo({
					url: `/order/confirm?cartIds=${JSON.stringify(cartIds)}`,
					fail: () => uni.showToast({ title: '跳转失败，请重试', icon: 'none' })
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
.cart-container {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 120rpx;
}

.empty {
	position: fixed;
	left: 0;
	top: 0;
	width: 100%;
	height: 100vh;
	display: flex;
	justify-content: center;
	align-items: center;
	background: #fff;
	.empty-tips {
		font-size: 28rpx;
		color: #999;
		.navigator {
			color: #e4393c;
			margin-left: 16rpx;
		}
	}
}

/* 购物车列表*/
.cart-scroll {
	height: calc(100vh - 100rpx);
}

.store-group {
	background: #fff;
	margin-bottom: 12rpx;
	padding-bottom: 12rpx;
}

.store-header {
	display: flex;
	align-items: center;
	height: 64rpx;
	padding: 0 20rpx;
	border-bottom: 1rpx solid #f5f5f5;
	.store-checkbox {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #ddd;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 12rpx;
		&.checked {
			background: #e4393c;
			border-color: #e4393c;
			.checkmark {
				color: #fff;
				font-size: 22rpx;
				font-weight: bold;
				line-height: 1;
			}
		}
	}
	.store-name-wrap {
		flex: 1;
		display: flex;
		align-items: center;
	}
	.store-name {
		flex: 1;
		font-size: 24rpx;
		color: #333;
		font-weight: 500;
	}
	.store-arrow {
		font-size: 24rpx;
		color: #999;
	}
}

.product-card {
	padding: 12rpx 20rpx;
}

.product-main {
	display: flex;
	align-items: flex-start;
}

.product-checkbox {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	border: 2rpx solid #ddd;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 16rpx;
	flex-shrink: 0;
	margin-top: 44rpx;
	&.checked {
		background: #e4393c;
		border-color: #e4393c;
		.checkmark {
			color: #fff;
			font-size: 22rpx;
			font-weight: bold;
			line-height: 1;
		}
	}
}

.product-image-wrap {
	position: relative;
	width: 160rpx;
	height: 160rpx;
	flex-shrink: 0;
	border-radius: 8rpx;
	overflow: hidden;
	background: #f5f5f5;
	.product-image {
		width: 100%;
		height: 100%;
	}
	.product-banner {
		position: absolute;
		top: 0;
		right: 0;
		background: #e4393c;
		color: #fff;
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 0 0 0 8rpx;
	}
}

.product-info {
	flex: 1;
	min-width: 0;
	padding-left: 16rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.product-name {
	font-size: 24rpx;
	color: #333;
	line-height: 1.35;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.clamp-2 {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.product-spec {
	margin-top: 6rpx;
	.spec-tag {
		display: inline-block;
		background: #f5f5f5;
		color: #666;
		font-size: 20rpx;
		padding: 2rpx 10rpx;
		border-radius: 20rpx;
		max-width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

.product-price-row {
	margin-top: 8rpx;
	.price-current {
		font-size: 28rpx;
		color: #e4393c;
		font-weight: bold;
	}
	.price-original {
		font-size: 22rpx;
		color: #999;
		text-decoration: line-through;
		margin-left: 12rpx;
	}
}

.product-actions {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: 10rpx;
}

.quantity-box {
	display: flex;
	align-items: center;
	height: 44rpx;
	background: #f5f5f5;
	border-radius: 6rpx;
	overflow: hidden;
	/* 用等粗线条画 − / +，避免系统字体里 - 与 + 视觉粗细、大小不一致 */
	.qty-btn {
		position: relative;
		width: 44rpx;
		height: 44rpx;
		flex-shrink: 0;
		&.minus {
			border-right: 1rpx solid #eee;
			&::after {
				content: '';
				position: absolute;
				left: 50%;
				top: 50%;
				transform: translate(-50%, -50%);
				width: 18rpx;
				height: 4rpx;
				background-color: #333;
				border-radius: 2rpx;
			}
		}
		&.plus {
			border-left: 1rpx solid #eee;
			&::before {
				content: '';
				position: absolute;
				left: 50%;
				top: 50%;
				transform: translate(-50%, -50%);
				width: 18rpx;
				height: 4rpx;
				background-color: #333;
				border-radius: 2rpx;
			}
			&::after {
				content: '';
				position: absolute;
				left: 50%;
				top: 50%;
				transform: translate(-50%, -50%);
				width: 4rpx;
				height: 18rpx;
				background-color: #333;
				border-radius: 2rpx;
			}
		}
	}
	.qty-input {
		width: 50rpx;
		height: 44rpx;
		text-align: center;
		font-size: 24rpx;
		background: #f5f5f5;
		border: none;
	}
}

.del-btn {
	font-size: 22rpx;
	color: #999;
	padding: 6rpx 0;
}

/* 底部结算按钮区域*/
.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	height: 100rpx;
	padding-bottom: env(safe-area-inset-bottom);
	background: #fff;
	display: flex;
	align-items: center;
	padding: 0 24rpx;
	box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}

.bottom-left {
	flex: 1;
	display: flex;
	align-items: center;
	.bottom-checkbox {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #ddd;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		&.checked {
			background: #e4393c;
			border-color: #e4393c;
			.checkmark {
				color: #fff;
				font-size: 22rpx;
				font-weight: bold;
				line-height: 1;
			}
		}
	}
	.select-all {
		font-size: 24rpx;
		color: #333;
		margin-left: 12rpx;
		margin-right: 16rpx;
	}
	.total-info {
		font-size: 24rpx;
		color: #333;
		.total-price {
			font-size: 28rpx;
			color: #e4393c;
			font-weight: bold;
			margin-left: 4rpx;
		}
	}
}

.checkout-btn {
	width: 180rpx;
	height: 64rpx;
	line-height: 64rpx;
	background: #e4393c;
	color: #fff;
	font-size: 26rpx;
	font-weight: 500;
	border: none;
	border-radius: 32rpx;
	margin: 0;
	padding: 0;
}
</style>

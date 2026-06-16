<template>
	<view>
		<view class="status-section">
			<image :src="orderStatus.image" class="icon" v-if="orderStatus.image" />
			<text class="label-text">{{orderStatus.text}}</text>
		</view>
		
		<!-- 地址（普通订单和积分订单都显示，如果有地址信息）-->
		<view class="address-section" v-if="(order.receiverName || order.receiver_name || order.receiverProvince || order.receiver_province)">
			<view class="order-content">
				<text class="yticon icon-shouhuodizhi">📍</text>
				<view class="cen">
					<view class="top">
						<text class="name">{{order.receiverName || order.receiver_name || ''}}</text>
						<text class="mobile">{{order.receiverPhone || order.receiver_phone || ''}}</text>
					</view>
					<text class="address">{{order.receiverProvince || order.receiver_province || ''}} {{order.receiverCity || order.receiver_city || ''}} {{order.receiverRegion || order.receiver_region || ''}}
						{{order.receiverDetailAddress || order.receiver_detail_address || order.address || ''}}</text>
				</view>
			</view>

			<image class="a-bg" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAu4AAAAFCAYAAAAaAWmiAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6Rjk3RjkzMjM2NzMxMTFFOUI4RkU4OEZGMDcxQzgzOEYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6Rjk3RjkzMjQ2NzMxMTFFOUI4RkU4OEZGMDcxQzgzOEYiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpGOTdGOTMyMTY3MzExMUU5QjhGRTg4RkYwNzFDODM4RiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpGOTdGOTMyMjY3MzExMUU5QjhGRTg4RkYwNzFDODM4RiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PrEOZlQAAAiuSURBVHjazJp7bFvV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"></image>
		</view>

		<view class="goods-section">
			<view class="g-header b-b">
				<text class="name">商品信息</text>
			</view>
			<!-- 商品列表 -->
			<view class="g-item" v-for="(item, index) in orderItemList" :key="index">
				<image :src="getImageUrl(item.productPic || item.image || item.product_pic)"></image>
				<view class="right">
					<text class="title clamp">{{item.productName || item.name || item.product_name || '商品'}}</text>
					<text class="spec" v-if="item.productAttr || item.spec">{{formatProductAttr(item.productAttr || item.spec || item.product_attr)}}</text>
					<text class="promotion clamp" v-if="item.promotionName || item.promotion_name">{{item.promotionName || item.promotion_name}}</text>
					<view class="price-box">
						<text class="price" v-if="order.orderType === 'exchange'">{{item.points || order.points || 0}}g</text>
						<text class="price" v-else>￥{{item.productPrice || item.price || item.product_price || 0}}</text>
						<text class="number">x {{item.productQuantity || item.quantity || item.product_quantity || 1}}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 金额明细（仅普通订单显示） -->
		<view class="yt-list" v-if="order.orderType !== 'exchange'">
			<view class="yt-list-cell b-b">
				<text class="cell-tit clamp">商品合计</text>
				<text class="cell-tip">￥{{order.totalAmount || order.total_amount || 0}}</text>
			</view>
			<view class="yt-list-cell b-b">
				<text class="cell-tit clamp">运费</text>
				<text class="cell-tip">￥{{order.freightAmount || order.freight_amount || order.shippingFee || 0}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="order.promotionAmount || order.promotion_amount">
				<text class="cell-tit clamp">活动优惠</text>
				<text class="cell-tip red">-￥{{order.promotionAmount || order.promotion_amount || 0}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="order.couponAmount || order.coupon_amount">
				<text class="cell-tit clamp">优惠券</text>
				<text class="cell-tip red">-￥{{order.couponAmount || order.coupon_amount || 0}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="order.integrationAmount || order.integration_amount">
				<text class="cell-tit clamp">积分抵扣</text>
				<text class="cell-tip red">-￥{{order.integrationAmount || order.integration_amount || 0}}</text>
			</view>
			<view class="yt-list-cell desc-cell" v-if="order.note || order.remark">
				<text class="cell-tit clamp">备注</text>
				<text class="cell-tip">{{order.note || order.remark || '...'}}</text>
			</view>
		</view>

		<!-- 积分兑换订单信息 -->
		<view class="yt-list" v-if="order.orderType === 'exchange'">
			<view class="yt-list-cell b-b">
				<text class="cell-tit clamp">兑换积分</text>
				<text class="cell-tip">{{order.points || 0}}g</text>
			</view>
			<view class="yt-list-cell desc-cell" v-if="order.description || order.note">
				<text class="cell-tit clamp">备注</text>
				<text class="cell-tip">{{order.description || order.note || '...'}}</text>
			</view>
		</view>

		<!-- 订单明细 -->
		<view class="yt-list">
			<view class="yt-list-cell b-b">
				<text class="cell-tit clamp">订单编号</text>
				<text class="cell-tip">{{order.orderSn || order.orderId || order.order_id || order.orderNo || 'N/A'}}</text>
			</view>
			<view class="yt-list-cell b-b">
				<text class="cell-tit clamp">提交时间</text>
				<text class="cell-tip">{{formatDateTime(order.createTime || order.create_time)}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="order.orderType !== 'exchange'">
				<text class="cell-tit clamp">支付方式</text>
				<text class="cell-tip">{{formatPayType(order.payType || order.pay_type)}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="(order.status==1||order.status==2||order.status==3) && order.orderType !== 'exchange'">
				<text class="cell-tit clamp">实付金额</text>
				<text class="cell-tip">￥{{order.payAmount || order.pay_amount || order.finalAmount || order.final_amount || 0}}</text>
			</view>
			<view class="yt-list-cell b-b" v-if="(order.status==1||order.status==2||order.status==3) && order.orderType !== 'exchange' && (order.paymentTime || order.payment_time)">
				<text class="cell-tit clamp">付款时间</text>
				<text class="cell-tip">{{formatDateTime(order.paymentTime || order.payment_time)}}</text>
			</view>
		</view>

		<!-- 底部 -->
		<view class="footer" v-if="order.status==0||order.status==2||order.status==3">
			<view class="action-box b-t" v-if="order.status==0 && order.orderType !== 'exchange'">
				<button class="action-btn" @click="cancelOrder(order.id)">取消订单</button>
				<button class="action-btn recom" @click="payOrder(order)">立即付款</button>
			</view>
			<view class="action-box b-t" v-if="order.status == 2 && order.orderType !== 'exchange'">
				<button class="action-btn" @click="trackOrder">查看物流</button>
				<button class="action-btn recom" @click="receiveOrder(order.id)">确认收货</button>
			</view>
			<view class="action-box b-t" v-if="order.status == 3 && order.orderType !== 'exchange'">
				<button class="action-btn" @click="afterSale">申请售后</button>
				<button class="action-btn recom" @click="reviewOrder">评价商品</button>
			</view>
			<view class="price-content" v-if="order.status==0 && order.orderType !== 'exchange'">
				<text>应付金额</text>
				<text class="price-tip">￥</text>
				<text class="price">{{order.payAmount || order.pay_amount || order.finalAmount || order.final_amount || 0}}</text>
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
				orderId: null,
				order: {},
				orderStatus: {
					text: '加载中...',
					image: ''
				}
			}
		},
		computed: {
			orderItemList() {
				if (!this.order) return []
				
				// 如果是积分兑换订单
				if (this.order.orderType === 'exchange') {
					// 优先使用从后端获取的订单商品列表
					if (this.order.orderItemList && Array.isArray(this.order.orderItemList) && this.order.orderItemList.length > 0) {
						return this.order.orderItemList.map(item => ({
							...item,
							productPic: item.productPic || item.productImage || item.image || '/static/images/default-product.svg',
							points: this.order.points || item.points || 0
						}))
					}
					// 如果没有orderItemList，使用goods数据
					return [{
						productPic: this.order.goods?.image || '/static/images/default-product.svg',
						productName: this.order.goods?.name || '兑换商品',
						productPrice: 0,
						productQuantity: this.order.quantity || 1,
						points: this.order.points || 0
					}]
				}
				
				// 普通订单
				if (this.order.orderItemList && Array.isArray(this.order.orderItemList)) {
					return this.order.orderItemList
				}
				
				// 兼容其他数据格式
				if (this.order.products && Array.isArray(this.order.products)) {
					return this.order.products.map(p => ({
						productPic: p.image || p.productPic || '/static/images/default-product.svg',
						productName: p.name || p.productName || '商品',
						productPrice: p.price || p.productPrice || 0,
						productQuantity: p.quantity || p.productQuantity || 1,
						productAttr: p.spec || p.productAttr || null
					}))
				}
				
				// 如果只有一个商品
				if (this.order.goods) {
					return [{
						productPic: this.order.goods.image || '/static/images/default-product.svg',
						productName: this.order.goods.name || '商品',
						productPrice: this.order.selectedSpec?.price || this.order.goods.price || 0,
						productQuantity: this.order.quantity || 1,
						productAttr: this.order.selectedSpec?.name || null
					}]
				}
				
				return []
			}
		},
		onLoad(option) {
			// 获取订单ID
			this.orderId = option.orderId || option.id
			if (!this.orderId) {
				uni.showToast({
					title: '订单ID不存在',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
				return
			}
			
			this.loadData()
		},
		methods: {
			// 加载订单详情
			async loadData() {
				try {
					// 检查是否是积分兑换订单（订单号以EX开头）
					if (String(this.orderId).startsWith('EX') || String(this.orderId).includes('exchange')) {
						// 积分兑换订单，从兑换记录中获取
						const exchangeId = String(this.orderId).replace('EX', '')
						
						// 获取兑换记录
						const uid = uni.getStorageSync('userId')
						const response = await api.getExchangeHistory(1, 100, false, uid)
						if (response && (response.code === 200 || response.code === 0) && response.data) {
							const records = response.data.records || []
							const record = records.find(r => r.id == exchangeId || String(r.id) === exchangeId)
							
							if (record) {
								// 获取商品信息
								const productsRes = await api.getPointsMallProducts()
								let productInfo = null
								if (productsRes && (productsRes.code === 200 || productsRes.code === 0) && productsRes.data) {
									const products = productsRes.data.products || []
									// 从description中解析商品标题
									const descMatch = record.description?.match(/兑换商品[�?]\s*(.+)/)
									if (descMatch && descMatch[1]) {
										const searchTitle = descMatch[1].trim()
										productInfo = products.find(p => p.title === searchTitle || String(p.id) === searchTitle)
									}
								}
								
								// 尝试通过订单号获取订单详情（如果后端创建了Order记录）
								let orderDetail = null
								try {
									const orderResponse = await api.getOrderDetail(`EX${record.id}`)
									if (orderResponse && (orderResponse.code === 200 || orderResponse.code === 0) && orderResponse.data) {
										orderDetail = orderResponse.data
									}
								} catch (e) {
									// 静默处理错误
								}
								
								// 如果获取到了订单详情，使用订单数据（包含地址信息）
								if (orderDetail) {
									// 如果订单商品列表中的图片为空，尝试从商品信息中获取
									if (orderDetail.orderItemList && Array.isArray(orderDetail.orderItemList) && orderDetail.orderItemList.length > 0) {
										orderDetail.orderItemList.forEach(item => {
											if (!item.productPic && !item.productImage && !item.image) {
												// 如果商品图片为空，使用从积分商城获取的商品图片
												if (productInfo && productInfo.image) {
													item.productPic = productInfo.image
													item.productImage = productInfo.image
													item.image = productInfo.image
												}
											}
										})
									}
									
									this.order = {
										...orderDetail,
										orderType: 'exchange',
										points: record.points || 0,
										description: record.description,
										// 确保goods数据也存在，用于兼容
										goods: {
											id: productInfo?.id || record.id,
											name: productInfo?.title || orderDetail.orderItemList?.[0]?.productName || record.description || '兑换商品',
											image: productInfo?.image || orderDetail.orderItemList?.[0]?.productPic || orderDetail.orderItemList?.[0]?.productImage || '/static/images/default-product.svg'
										}
									}
									this.setOrderStatus(orderDetail.status || 1) // 使用订单状态（1=待发货）
								} else {
									// 否则使用兑换记录数据，并尝试获取默认地址
									let defaultAddress = null
									try {
										// 尝试获取用户的默认地址
										const addressApiModule = await import('@/common/js/api/address.js')
										const addressApi = addressApiModule.default
										const addressRes = await addressApi.getDefaultAddress()
										if (addressRes && addressRes.data) {
											defaultAddress = addressRes.data
										}
									} catch (e) {
										// 静默处理错误
									}
									
									// 构建订单数据
									this.order = {
										id: record.id,
										orderId: `EX${record.id}`,
										orderSn: `EX${record.id}`,
										orderType: 'exchange',
										goods: {
											id: productInfo?.id || record.id,
											name: productInfo?.title || record.description || '兑换商品',
											image: productInfo?.image || '/static/images/default-product.svg'
										},
										quantity: 1,
										points: record.points || 0,
										status: 1, // 待发货
										createTime: record.createTime,
										description: record.description
									}
									
									// 如果有默认地址，添加到订单数据
									if (defaultAddress) {
										this.order.receiverName = defaultAddress.contactName || defaultAddress.name || ''
										this.order.receiver_name = this.order.receiverName
										this.order.receiverPhone = defaultAddress.contactPhone || defaultAddress.phone || ''
										this.order.receiverProvince = defaultAddress.province || ''
										this.order.receiver_province = this.order.receiverProvince
										this.order.receiverCity = defaultAddress.city || ''
										this.order.receiverRegion = defaultAddress.district || ''
										this.order.receiverDetailAddress = defaultAddress.detail || ''
									}
									
									this.setOrderStatus(1) // 待发货
								}
								return
							}
						}
					}
					
					// 普通订单，调用订单详情API
					const response = await api.getOrderDetail(this.orderId)
					
					if (response && (response.code === 200 || response.code === 0) && response.data) {
						this.order = response.data
						this.setOrderStatus(this.order.status)
					} else {
						uni.showToast({
							title: response?.msg || '加载订单详情失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('加载订单详情失败:', error)
					uni.showToast({
						title: '加载订单详情失败',
						icon: 'none'
					})
				}
			},
			
			// 格式化商品属性
			formatProductAttr(attr) {
				if (!attr) return ''
				try {
					// 如果是字符串，尝试解析为JSON
					if (typeof attr === 'string') {
						try {
							const attrArr = JSON.parse(attr)
							if (Array.isArray(attrArr)) {
								let attrStr = ''
								for (let a of attrArr) {
									if (a.key && a.value) {
										attrStr += a.key + ':' + a.value + ';'
									} else if (typeof a === 'string') {
										attrStr += a + ';'
									}
								}
								return attrStr
							}
						} catch (e) {
							// 如果不是JSON，直接返回
							return attr
						}
					}
					return String(attr)
				} catch (e) {
					return String(attr || '')
				}
			},
			
			// 格式化日期时间
			formatDateTime(time) {
				if (time == null || time === '') {
					return 'N/A'
				}
				try {
					const date = util.parseDate(time)
					if (!date) return String(time)
					return util.formatDate(date, 'YYYY-MM-DD HH:mm:ss')
				} catch (e) {
					return String(time)
				}
			},
			
			// 格式化支付方式
			formatPayType(payType) {
				if (payType == null || payType === undefined) {
					return "未支付"
				}
				if (payType == 0) {
					return "未支付"
				} else if (payType == 1) {
					return "支付宝支付"
				} else if (payType == 2) {
					return "微信支付"
				}
				return "未知"
			},
			
			// 设置订单状态信息
			setOrderStatus(status) {
				// 使用 static/images 下已有 svg；勿用 /static/icon_*.png（不存在，小程序会 500）
				const statusMap = {
					0: {
						text: '等待付款',
						image: '/static/images/my-orders.svg'
					},
					1: {
						text: '等待发货',
						image: '/static/images/my-cans.svg'
					},
					2: {
						text: '等待收货',
						image: '/static/images/search.svg'
					},
					3: {
						text: '交易完成',
						image: '/static/images/my-favorites.svg'
					},
					4: {
						text: '交易关闭',
						image: '/static/images/report.svg'
					},
					'pending': {
						text: '等待付款',
						image: '/static/images/my-orders.svg'
					},
					'paid': {
						text: '等待发货',
						image: '/static/images/my-cans.svg'
					},
					'shipped': {
						text: '等待收货',
						image: '/static/images/search.svg'
					},
					'completed': {
						text: '交易完成',
						image: '/static/images/my-favorites.svg'
					},
					'cancelled': {
						text: '交易关闭',
						image: '/static/images/report.svg'
					}
				}
				
				this.orderStatus = statusMap[status] || {
					text: '未知状态',
					image: ''
				}
			},
			
			// 取消订单
			cancelOrder(orderId) {
				let superThis = this
				uni.showModal({
					title: '提示',
					content: '是否要取消该订单',
					success: function(res) {
						if (res.confirm) {
							uni.showLoading({
								title: '请稍后...',
							})
							superapi.updateOrderStatus(orderId, 'cancelled').then(response => {
								uni.hideLoading()
								if (response && (response.code === 200 || response.code === 0)) {
									uni.showToast({
										title: '订单已取消',
										icon: 'success'
									})
									superThis.loadData()
								} else {
									uni.showToast({
										title: response?.msg || '取消订单失败',
										icon: 'none'
									})
								}
							}).catch(error => {
								uni.hideLoading()
								console.error('取消订单失败:', error)
								uni.showToast({
									title: '取消订单失败',
									icon: 'none'
								})
							})
						}
					}
				})
			},
			
			// 支付订单 - 跳转到确认订单页（与立即购买流程一致）
			payOrder(order) {
				const orderId = String(order.orderId || order.orderNo || order.id || '')
				if (!orderId) {
					uni.showToast({ title: '订单号异常', icon: 'none' })
					return
				}
				uni.navigateTo({
					url: `/order/confirm?orderId=${encodeURIComponent(orderId)}`
				})
			},
			
			// 确认收货
			receiveOrder(orderId) {
				let superThis = this
				uni.showModal({
					title: '提示',
					content: '是否要确认收货？',
					success: function(res) {
						if (res.confirm) {
							uni.showLoading({
								title: '请稍后...',
							})
							superapi.updateOrderStatus(orderId, 'completed').then(response => {
								uni.hideLoading()
								if (response && (response.code === 200 || response.code === 0)) {
									uni.showToast({
										title: '确认收货成功',
										icon: 'success'
									})
									superThis.loadData()
								} else {
									uni.showToast({
										title: response?.msg || '确认收货失败',
										icon: 'none'
									})
								}
							}).catch(error => {
								uni.hideLoading()
								console.error('确认收货失败:', error)
								uni.showToast({
									title: '确认收货失败',
									icon: 'none'
								})
							})
						}
					}
				})
			},
			
			// 查看物流
			trackOrder() {
				const orderId = this.orderId || this.order.id || this.order.orderId
				if (!orderId) {
					uni.showToast({
						title: '订单ID不存在',
						icon: 'none'
					})
					return
				}
				uni.navigateTo({
					url: `/order/logistics?orderId=${orderId}`
				})
			},
			
			// 申请售后
			afterSale() {
				uni.showToast({
					title: '申请售后功能开发中',
					icon: 'none'
				})
			},
			
			// 评价商品
			reviewOrder() {
				if (this.orderItemList && this.orderItemList.length > 0) {
					const item = this.orderItemList[0]
					uni.navigateTo({
						url: `/order/review?orderId=${this.order.id}&productId=${item.productId || item.id}&productImage=${encodeURIComponent(item.productPic || '')}&productName=${encodeURIComponent(item.productName || '')}`
					})
				} else {
					uni.showToast({
						title: '无法获取商品信息',
						icon: 'none'
					})
				}
			},
			
			// 处理图片URL
			getImageUrl(imageUrl) {
				if (!imageUrl || imageUrl === '/static/images/default-product.svg') {
					return '/static/images/default-product.svg'
				}
				return util.getImageUrl(imageUrl)
			}
		}
	}
</script>

<style lang="scss">
	page {
		background: #f5f5f5;
		padding-bottom: 100upx;
	}

	.status-section {
		height: 80upx;
		background-color: #ff4444;
		display: flex;
		align-items: center;
		padding: 12upx 30upx;

		.icon {
			width: 32upx;
			height: 32upx;
			flex-shrink: 0;
		}

		.label-text {
			color: #fff;
			margin-left: 14upx;
			font-size: 24upx;
			font-weight: 600;
		}
	}

	.address-section {
		padding: 14upx 0;
		background: #fff;
		position: relative;
		margin-top: 16upx;

		.order-content {
			display: flex;
			align-items: center;
			padding: 0 30upx;
		}

		.icon-shouhuodizhi {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 60upx;
			color: #888;
			font-size: 28upx;
		}

		.cen {
			display: flex;
			flex-direction: column;
			flex: 1;
			font-size: 22upx;
			color: #333;
		}

		.top {
			display: flex;
			align-items: center;
			margin-bottom: 2upx;
		}

		.name {
			font-size: 26upx;
			margin-right: 14upx;
			color: #333;
			font-weight: normal;
		}

		.mobile {
			font-size: 20upx;
			color: #666;
		}

		.address {
			margin-top: 6upx;
			margin-right: 20upx;
			color: #999;
			line-height: 1.3;
		}

		.a-bg {
			position: absolute;
			left: 0;
			bottom: 0;
			display: block;
			width: 100%;
			height: 5upx;
		}
	}

	.goods-section {
		margin-top: 16upx;
		background: #fff;
		padding-bottom: 1px;

		.g-header {
			display: flex;
			align-items: center;
			height: 84upx;
			padding: 0 30upx;
			position: relative;
			border-bottom: 1rpx solid #f0f0f0;
		}

		.name {
			font-size: 30upx;
			color: #333;
			font-weight: 600;
		}

		.g-item {
			display: flex;
			margin: 20upx 30upx;
			padding-bottom: 20upx;
			border-bottom: 1rpx solid #f0f0f0;

			&:last-child {
				border-bottom: none;
			}

			image {
				flex-shrink: 0;
				display: block;
				width: 140upx;
				height: 140upx;
				border-radius: 4upx;
			}

			.right {
				flex: 1;
				padding-left: 24upx;
				overflow: hidden;
			}

			.title {
				font-size: 30upx;
				color: #333;
				margin-bottom: 8upx;
			}

			.spec {
				font-size: 26upx;
				color: #999;
				margin-bottom: 8upx;
			}

			.promotion {
				font-size: 24upx;
				color: #ff4444;
				margin-bottom: 8upx;
			}

			.price-box {
				display: flex;
				align-items: center;
				justify-content: space-between;
				font-size: 32upx;
				color: #333;
				padding-top: 10upx;

				.price {
					color: #ff4444;
					font-weight: 600;
				}

				.number {
					font-size: 26upx;
					color: #666;
				}
			}
		}
	}

	.yt-list {
		margin-top: 16upx;
		background: #fff;
	}

	.yt-list-cell {
		display: flex;
		align-items: center;
		padding: 20upx 30upx;
		line-height: 1.5;
		position: relative;
		border-bottom: 1rpx solid #f0f0f0;

		&:last-child {
			border-bottom: none;
		}

		.cell-tit {
			flex: 1;
			font-size: 26upx;
			color: #999;
			margin-right: 10upx;
		}

		.cell-tip {
			font-size: 26upx;
			color: #333;

			&.red {
				color: #ff4444;
			}
		}

		&.desc-cell {
			.cell-tit {
				max-width: 90upx;
			}
		}
	}

	.footer {
		position: fixed;
		flex-direction: row-reverse;
		left: 0;
		bottom: 0;
		z-index: 995;
		display: flex;
		align-items: center;
		width: 100%;
		height: 90upx;
		justify-content: space-between;
		font-size: 30upx;
		background-color: #fff;
		z-index: 998;
		color: #666;
		box-shadow: 0 -1px 5px rgba(0, 0, 0, .1);

		.price-content {
			padding-left: 30upx;
			display: flex;
			align-items: baseline;
		}

		.price-tip {
			color: #ff4444;
			margin-left: 8upx;
		}

		.price {
			font-size: 36upx;
			color: #ff4444;
			font-weight: 600;
		}
	}

	.action-box {
		display: flex;
		justify-content: flex-end;
		align-items: center;
		height: 100upx;
		position: relative;
		padding-right: 30upx;
	}

	.action-btn {
		width: 160upx;
		height: 60upx;
		margin: 0;
		margin-left: 24upx;
		padding: 0;
		text-align: center;
		line-height: 60upx;
		font-size: 24upx;
		color: #333;
		background: #fff;
		border: 1rpx solid #ddd;
		border-radius: 100px;

		&.recom {
			background: #fff9f9;
			color: #ff4444;
			border-color: #f7bcc8;
		}
	}
</style>

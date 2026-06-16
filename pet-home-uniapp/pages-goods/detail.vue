<template>
	<view class="container">
		<view class="carousel">
			<swiper indicator-dots circular :duration="400">
				<swiper-item class="swiper-item" v-for="(item,index) in imgList" :key="index">
					<view class="image-wrapper">
						<image :src="item.src" class="loaded" mode="aspectFill"></image>
					</view>
				</swiper-item>
			</swiper>
		</view>

		<view class="introduce-section">
			<text class="title">{{product.name}}</text>
			<view class="title2-wrapper" v-if="product.subTitle || (product.description && product.description.length < 100)">
				<text class="title2">{{product.subTitle || product.description}}</text>
			</view>
			<view class="price-box">
				<text class="price-tip">¥</text>
				<text class="price">{{product.price || 0}}</text>
				<text class="m-price" v-if="product.originalPrice && product.originalPrice > 0">¥{{product.originalPrice}}</text>
				<!-- <text class="coupon-tip">7?/text> -->
			</view>
			<view class="bot-row">
				<text>销量 {{product.sale || 0}}</text>
				<text>库存: {{product.stock || 0}}</text>
			</view>
		</view>

		<view class="c-list">
			<view class="c-row b-b">
				<view class="bz-list con">
					<text v-for="(item, index) in serviceList" :key="item">{{item}}<text v-if="index < serviceList.length - 1"> · </text></text>
				</view>
			</view>
		</view>

		<!-- 评价 -->
		<view class="eva-section">
			<view class="e-header" @click="goToReviews">
				<text class="tit">商品评价</text>
				<text>({{reviewCount}})</text>
				<text class="tip">查看更多</text>
				<text class="yticon icon-you"></text>
			</view>
			<view class="eva-box" v-if="latestReview">
				<image class="portrait" :src="latestReview.userAvatar || '/static/images/garfield-default-avatar.png'" mode="aspectFill"></image>
				<view class="right">
					<text class="name">{{latestReview.userName || '匿名用户'}}</text>
					<text class="con">{{latestReview.comment || latestReview.content || '暂无评价内容'}}</text>
					<view class="bot">
						<text class="attr" v-if="latestReview.specInfo">购买类型：{{latestReview.specInfo}}</text>
						<text class="time">{{formatDateTime(latestReview.createTime)}}</text>
					</view>
				</view>
			</view>
			<view class="eva-box" v-else>
				<view class="right">
					<text class="con" style="text-align: center; color: #999;">暂无评价</text>
				</view>
			</view>
		</view>

		<!-- 店铺信息 -->
		<view class="store-info">
			<view class="d-header">
				<text>店铺信息</text>
			</view>
			<view class="store-box" @click="navToStore()">
				<view class="store-left">
					<view class="store-logo">
						<image 
							v-if="storeInfo.avatar || storeInfo.logo" 
							:src="getStoreImageUrl(storeInfo.avatar || storeInfo.logo)" 
							class="loaded" 
							mode="aspectFill"
							@error="handleStoreImageError"
						></image>
						<view v-else class="store-logo-placeholder"></view>
					</view>
					<view class="store-details">
						<view class="store-name-row">
							<text class="store-name">{{storeInfo.name || '宠物家商品专卖店'}}</text>
							<text class="store-tag" v-if="storeInfo.tag">{{storeInfo.tag}}</text>
						</view>
						<view class="store-guarantee">
							<text class="guarantee-icon"></text>
							<text class="guarantee-text">{{storeInfo.guarantee || '店铺保障'}}</text>
							<text class="guarantee-desc">{{storeInfo.guaranteeDesc || '7天无理由退货·全场包邮'}}</text>
						</view>
					</view>
				</view>
				<view class="store-right">
					<view class="enter-store-btn">
						<text>进店</text>
						<text class="arrow">></text>
					</view>
				</view>
			</view>
		</view>

		<view class="detail-desc">
			<view class="d-header">
				<text>图文详情</text>
			</view>
			<rich-text :nodes="desc"></rich-text>
		</view>

		<!-- 底部操作菜单 -->
		<view class="page-bottom">
			<navigator url="/pages/index/index" open-type="switchTab" class="p-b-btn">
				<text class="yticon icon-xiatubiao--copy"></text>
				<text>首页</text>
			</navigator>
			<view class="p-b-btn" @click="goToCart">
				<text class="yticon icon-gouwuche"></text>
				<text>购物车</text>
			</view>
			<view class="p-b-btn" :class="{active: favorite}" @click="toFavorite">
				<text class="yticon icon-shoucang"></text>
				<text>收藏</text>
			</view>

			<view class="action-btn-group">
				<button type="primary" class=" action-btn no-border buy-now-btn" @click="buy">立即购买</button>
				<button type="primary" class=" action-btn no-border add-cart-btn" @click="addToCart">加入购物车</button>
			</view>
		</view>

		<!-- 规格选择框：用底部锚点容器保证整块贴屏幕底部，不贴顶 -->
		<view class="popup spec" :class="specClass" @touchmove.stop.prevent="stopPrevent" @click="toggleSpec">
			<view class="mask"></view>
			<view class="spec-bottom-anchor">
				<view class="layer spec-popup-content" @click.stop="stopPrevent">
				<view class="close-btn" @click="toggleSpec">
					<text class="close-icon">×</text>
				</view>
				<!-- 商品摘要：缩略图 + 价格 + 已选+ 数量 -->
				<view class="spec-product-row">
					<image class="spec-product-thumb" :src="getCurrentImage()" mode="aspectFill"></image>
					<view class="spec-product-right">
						<view class="spec-price-row">
							<text class="spec-price-current">¥{{ getCurrentPrice() || product.price || 0 }}</text>
							<text class="spec-price-original" v-if="product.originalPrice && Number(product.originalPrice) > Number(getCurrentPrice())">¥{{ product.originalPrice }}</text>
						</view>
						<view class="spec-selected-row">
							<text class="spec-selected-label">已选：</text>
							<text class="spec-selected-value" v-if="specSelected.length > 0">{{ specSelected.map(s => s.name).join(' ') }}</text>
							<text class="spec-selected-placeholder" v-else>请选择规格</text>
						</view>
						<view class="spec-quantity-row">
							<view class="spec-quantity-wrap">
								<view class="spec-qty-btn" @click="changeSpecQuantity(-1)"><text class="spec-qty-symbol">-</text></view>
								<text class="spec-qty-num">{{ specQuantity }}</text>
								<view class="spec-qty-btn" @click="changeSpecQuantity(1)"><text class="spec-qty-symbol">+</text></view>
							</view>
							<text class="spec-stock-tip" v-if="getCurrentStock() > 0 && getCurrentStock() <= 20">即将卖完</text>
						</view>
					</view>
				</view>
				<!-- 规格列表：仅标题 + 选项（flex 换行，一行能放多个就放多个） -->
				<scroll-view class="spec-list-scroll" scroll-y="true">
					<view v-for="(item, index) in specList" :key="index" class="spec-category">
						<text class="spec-category-title">{{ item.name }}</text>
						<view class="spec-option-wrap">
							<view
								v-for="(childItem, childIndex) in specChildList"
								v-if="childItem.pid === item.id"
								:key="childIndex"
								class="spec-option-chip"
								:class="{ selected: childItem.selected, disabled: isSpecDisabled(childItem) }"
								@click="selectSpec(childIndex, childItem.pid)"
							>
								<image v-if="childItem.image || childItem.pic" class="spec-option-thumb" :src="getSpecOptionImage(childItem)" mode="aspectFill"></image>
								<text class="spec-option-name">{{ childItem.name }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
					<!-- 底部：按钮-->
				<view class="spec-footer-wrap">
					<button class="spec-cta-btn buy-btn" @click="confirmSpecAndBuy" v-if="isBuying">立即购买</button>
					<button class="spec-cta-btn cart-btn" @click="confirmSpecAndAddToCart" v-else>加入购物车</button>
				</view>
			</view>
			</view>
		</view>
		<!-- 属性模态层弹窗 -->
		<view class="popup spec" :class="attrClass" @touchmove.stop.prevent="stopPrevent" @click="toggleAttr">
			<!-- 遮罩-->
			<view class="mask"></view>
			<view class="layer attr-content no-padding" @click.stop="stopPrevent">
				<view class="c-list">
					<view v-for="item in attrList" class="c-row b-b" :key="item.key">
						<text class="tit">{{item.key}}</text>
						<view class="con">
							<text class="con t-r">{{item.value}}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
		<!-- 分享 -->
		<share ref="share" :contentHeight="580" :shareList="shareList"></share>
	</view>
</template>

<script>
import share from '@/components/share.vue';
import { util } from '@/common/js/util.js';
import { api } from '@/common/js/api.js';
import { mapState } from 'vuex';
import cartApi from '@/common/js/api/cart.js';

const defaultServiceList = [{
	id: 1,
	name: "七天无理由退货"
}, {
	id: 2,
	name: "退货包运费"
}, {
	id: 3,
	name: "48小时内发货"
}, {
	id: 4,
	name: "免费包邮"
}];
const defaultShareList = [{
		type: 1,
		icon: '',
		text: '微信好友',
		emoji: '💬'
	},
	{
		type: 2,
		icon: '',
		text: '朋友',
		emoji: '📱'
	},
	{
		type: 3,
		icon: '',
		text: 'QQ好友',
		emoji: '💬'
	},
	{
		type: 4,
		icon: '',
		text: 'QQ空间',
		emoji: '🌐'
	}
]

export default {
	components: {
		share
	},
	data() {
		return {
			specClass: 'none',
			attrClass: 'none',
			specSelected: [],
			favorite: false,
			shareList: [],
			imgList: [],
			desc: '',
			specList: [],
			specChildList: [],
			product: {},
			brand: {},
			storeInfo: {
				id: null, // 店铺ID
				name: '宠物家商品专卖店',
				tag: '',
				logo: '',
				avatar: '',
				guarantee: '店铺保障',
				guaranteeDesc: '7天无理由退货·全场包邮'
			},
			serviceList: [],
			skuStockList: [],
			attrList: [],
			reviewCount: 0,
			goodRate: 0,
			latestReview: null,
			productId: null, 
			stockRefreshTimer: null, 
			isBuying: false, 
			specQuantity: 1
		};
	},
	async onLoad(options) {
		let id = options.id;
		this.productId = id; // 保存商品ID
		this.shareList = defaultShareList;
		await this.loadData(id);
		// 启动库存自动刷新（每30秒刷新一次）
		this.startStockRefresh();
	},
	onUnload() {
		// 页面卸载时清除定时器
		this.stopStockRefresh();
	},
	computed: {
		...mapState(['hasLogin', 'userInfo', 'isLoggedIn'])
	},
	methods: {
		formatDateTime(time) {
			if (time == null || time === '') {
				return 'N/A';
			}
			const iosTime = String(time).replace(' ', 'T');
			let date = new Date(iosTime);
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const hours = String(date.getHours()).padStart(2, '0');
			const minutes = String(date.getMinutes()).padStart(2, '0');
			const seconds = String(date.getSeconds()).padStart(2, '0');
			return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
		},
		async loadData(id) {
			try {
				const res = await api.getGoodsDetail(id);
				
				if ((res.code === 0 || res.code === 200) && res.data) {
					const goodsData = res.data;
					
					// 处理图片URL
					let imageUrl = goodsData.pic || goodsData.image || goodsData.imageUrl;
					if (imageUrl) {
						goodsData.pic = util.getImageUrl(imageUrl);
					}
					
					// 处理价格
					if (goodsData.price) {
						goodsData.price = goodsData.price.toString().replace(/¥/g, '').replace(/[^\d.]/g, '');
					}
					if (goodsData.originalPrice) {
						goodsData.originalPrice = goodsData.originalPrice.toString().replace(/¥/g, '').replace(/[^\d.]/g, '');
					}
					
					// 设置默认值，避免显示 undefined
					goodsData.sale = goodsData.sale || 0;
					goodsData.stock = goodsData.stock || 0;
					goodsData.viewCount = goodsData.viewCount || 0;
					goodsData.originalPrice = goodsData.originalPrice || 0;
					
					// 如果没有 subTitle，尝试从 description 中提取简短描述
					if (!goodsData.subTitle && goodsData.description) {
						// 如果 description 太长，只取前50个字符作�?subTitle
						if (goodsData.description.length > 50) {
							goodsData.subTitle = goodsData.description.substring(0, 50) + '...';
						} else {
							goodsData.subTitle = goodsData.description;
						}
					}
					
					// 解析详情图片（可能是JSON字符串）
					if (goodsData.detailImages) {
						try {
							if (typeof goodsData.detailImages === 'string') {
								// 如果是字符串，尝试解析为JSON
								const parsed = JSON.parse(goodsData.detailImages);
								goodsData.detailImages = parsed;
							}
							// 确保是数组
							if (!Array.isArray(goodsData.detailImages)) {
								goodsData.detailImages = [];
							}
						} catch (error) {
							goodsData.detailImages = [];
						}
					}
					
					// 处理品牌信息
					if (goodsData.brand && typeof goodsData.brand === 'object') {
						this.brand = goodsData.brand;
					} else if (goodsData.brand && typeof goodsData.brand === 'string') {
						this.brand = {
							name: goodsData.brand || '未知品牌',
							firstLetter: goodsData.brand ? goodsData.brand.charAt(0).toUpperCase() : 'U',
							logo: goodsData.brandLogo || ''
						};
					}
					
					// 处理店铺信息（如果有店铺头像等数据）
					if (goodsData.storeInfo) {
						this.storeInfo = {
							...this.storeInfo,
							...goodsData.storeInfo,
							id: goodsData.storeInfo.id || goodsData.storeInfo.storeId || this.storeInfo.id || null,
							avatar: goodsData.storeInfo.avatar || goodsData.storeInfo.logo || this.storeInfo.avatar || ''
						};
						const avatarUrl = goodsData.storeAvatar || goodsData.storeLogo || '';
						this.$set(this.storeInfo, 'avatar', avatarUrl);
						this.$set(this.storeInfo, 'logo', avatarUrl);
						// 尝试从其他字段获取storeId
						if (goodsData.storeId) {
							this.$set(this.storeInfo, 'id', goodsData.storeId);
						}
						
						// 如果storeInfo中没有id，尝试从商品数据中获取
						if (!this.storeInfo.id && (goodsData.storeId || goodsData.store_id)) {
							this.$set(this.storeInfo, 'id', goodsData.storeId || goodsData.store_id);
						}
						
						// 确保店铺头像和logo都有
						if (this.storeInfo.avatar && !this.storeInfo.logo) {
							this.$set(this.storeInfo, 'logo', this.storeInfo.avatar);
						} else if (this.storeInfo.logo && !this.storeInfo.avatar) {
							this.$set(this.storeInfo, 'avatar', this.storeInfo.logo);
						}
						// 不显示店铺标签（如年货节）
						this.$set(this.storeInfo, 'tag', '');
					}

					this.product = goodsData;
					this.skuStockList = goodsData.skuStockList || [];
					this.imgList = [];
					this.specList = [];
					this.specChildList = [];
					this.specSelected = [];
					this.attrList = [];
					this.serviceList = [];
					this.initImgList();
					this.initServiceList();
					this.initSpecList(goodsData);
					this.initAttrList(goodsData);
					this.initProductDesc();
					await this.initProductCollection();
					this.loadReviews(id);
				}
			} catch (error) {
				console.error('加载商品详情失败:', error);
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			}
		},
		//规格弹窗开关
		toggleSpec() {
			if (this.specClass === 'show') {
				this.specClass = 'hide';
				setTimeout(() => {
					this.specClass = 'none';
				}, 250);
			} else if (this.specClass === 'none') {
				this.specClass = 'show';
			}
		},
		//属性弹窗开关
		toggleAttr() {
			if (this.attrClass === 'show') {
				this.attrClass = 'hide';
				setTimeout(() => {
					this.attrClass = 'none';
				}, 250);
			} else if (this.attrClass === 'none') {
				this.attrClass = 'show';
			}
		},
		//优惠券弹窗开
		toggleCoupon(type) {
			// 暂时不实现优惠券功能，显示提示
			uni.showToast({
				title: "暂无可领优惠",
				icon: "none"
			});
			return;

		},
		//选择规格
		selectSpec(index, pid) {
			let list = this.specChildList;
			list.forEach(item => {
				if (item.pid === pid) {
					this.$set(item, 'selected', false);
				}
			})

			this.$set(list[index], 'selected', true);
			//存储已选择
			/**
			 * 修复选择规格存储错误
			 * 将这几行代码替换即可
			 * 选择的规格存放在specSelected?			 */
			this.specSelected = [];
			list.forEach(item => {
				if (item.selected === true) {
					this.specSelected.push(item);
				}
			})
			this.changeSpecInfo();
		},
		//分享
		share() {
			if (this.$refs.share) {
				this.$refs.share.toggleMask();
			} else {
				uni.showToast({
					title: '分享功能开发中',
					icon: 'none'
				});
			}
		},
		//收藏
		goToCart() {
			uni.navigateTo({
				url: '/cart/index',
				success: () => {
				},
				fail: (err) => {
					console.error('[商品详情] 跳转到购物车失败:', err)
					uni.showToast({
						title: '跳转失败，请重试',
						icon: 'none'
					})
				}
			})
		},
		
		async toFavorite() {
			if (!this.checkForLogin()) {
				return;
			}
			
			const userInfo = uni.getStorageSync('userInfo');
			const userId = userInfo.id || userInfo.uid;
			const productId = this.productId || this.product.id;
			
			if (!productId) {
				uni.showToast({
					title: '商品ID不存',
					icon: 'none'
				});
				return;
			}
			
			try {
				uni.showLoading({ title: '处理中...' });
				
				let res;
				if (this.favorite) {
					// 取消收藏
					res = await api.uncollectProduct(productId, userId);
				} else {
					// 收藏
					res = await api.collectProduct(productId, userId);
				}
				
				uni.hideLoading();
				
				if (res && (res.code === 200 || res.code === 0)) {
					this.favorite = !this.favorite;
					uni.showToast({
						title: this.favorite ? "收藏成功" : "取消收藏成功",
						icon: 'success'
					});
				} else {
					// 处理特殊错误情况
					const errorMsg = res?.msg || '';
					if (errorMsg.includes('已经收藏过了')) {
							// 如果已经收藏过了，更新状态为已收藏
						this.favorite = true;
						uni.showToast({
							title: '已经收藏过了',
							icon: 'none',
							duration: 1500
						});
					} else if (errorMsg.includes('没有收藏记录')) {
						// 如果没有收藏记录，更新状态为未收藏
						this.favorite = false;
						uni.showToast({
							title: '没有收藏记录',
							icon: 'none',
							duration: 1500
						});
					} else {
						uni.showToast({
							title: errorMsg || (this.favorite ? "取消收藏失败" : "收藏失败"),
							icon: 'none'
						});
					}
				}
			} catch (error) {
				uni.hideLoading();
				console.error('收藏/取消收藏失败:', error);
				// 处理错误信息
				const errorMsg = error.message || error.toString() || '';
				
				// 如果错误信息包含"已经收藏过了"，更新状态
				if (errorMsg.includes('已经收藏过了')) {
					this.favorite = true;
					uni.showToast({
						title: '已经收藏过了',
						icon: 'none',
						duration: 1500
					});
					return;
				}
				
				// 如果接口不存在，至少更新本地状态
				if (errorMsg.includes('404') || errorMsg.includes('NumberFormatException')) {
					// 接口不存在，只更新本地状态
					this.favorite = !this.favorite;
					uni.showToast({
						title: this.favorite ? "收藏成功！（仅本地）" : "取消收藏成功！（仅本地）",
						icon: 'none'
					});
				} else {
					uni.showToast({
						title: '操作失败，请重试',
						icon: 'none'
					});
				}
			}
		},
		// 有规格时一律清空预选，强制用户手动选择规格
		clearSpecSelectionWhenMultipleOptions() {
			const hasSpec = Array.isArray(this.specList) && this.specList.length > 0;
			if (hasSpec) {
				this.specSelected = [];
				this.specChildList.forEach(c => this.$set(c, 'selected', false));
			}
		},
		async buy() {
			if (!this.checkForLogin()) {
				return;
			}
			this.clearSpecSelectionWhenMultipleOptions();
			this.specQuantity = 1;
			this.isBuying = true;
			this.specClass = 'show';
		},
		// 确认规格并购买
		async confirmSpecAndBuy() {
			// 检查是否已选择规格
			if (this.specList.length > 0 && this.specSelected.length === 0) {
				uni.showToast({
					title: '请选择规格',
					icon: 'none'
				});
				return;
			}
			
			// 关闭规格选择弹窗
			this.toggleSpec();
			this.isBuying = false; // 重置标记
			
			// 等待弹窗关闭动画完成
			await new Promise(resolve => setTimeout(resolve, 300));
			
			// 直接执行购买逻辑，不再调用buy()方法（避免重复检查）
			if (!this.checkForLogin()) {
				return;
			}
			
			// 刷新库存确保数据最新
			await this.refreshStock();
			
			// 检查库存
			if (this.product.stock <= 0) {
				uni.showToast({
					title: '商品库存不足',
					icon: 'none'
				});
				return;
			}
			
			// 准备订单数据
			let productSkuStock = this.getSkuStock();
			
			// 如果有规格选择，即使没有SKU库存，也要保存规格信息
			let selectedSpecData = null;
			if (this.specSelected.length > 0) {
				// 有选择规格，保存规格信息
			selectedSpecData = {
					name: this.specSelected.map(s => s.name).join(','),
					price: productSkuStock ? (productSkuStock.promotionPrice || productSkuStock.price) : this.product.price,
					stock: productSkuStock ? productSkuStock.stock : this.product.stock
				};
			} else if (this.specList.length === 0) {
				// 没有规格的商品，selectedSpec 为null
				selectedSpecData = null;
			}
			
			const unitPrice = productSkuStock ? (productSkuStock.promotionPrice || productSkuStock.price) : this.product.price;
			const qty = Math.max(1, parseInt(this.specQuantity, 10) || 1);
			const orderData = {
				goods: {
					id: this.product.id,
					name: this.product.name,
					image: this.product.pic || this.product.image,
					price: unitPrice
				},
				selectedSpec: selectedSpecData,
				quantity: qty,
				totalAmount: Number(unitPrice) * qty,
				shippingFee: 0,
				finalAmount: Number(unitPrice) * qty
			};
			
			
			// 保存订单数据到本地存储
			uni.setStorageSync('orderData', orderData);
			
			// 跳转到订单确认页面
			uni.navigateTo({
				url: '/order/confirm',
				success: () => {
				},
				fail: (err) => {
					console.error('跳转到订单确认页面失败', err);
					uni.showToast({
						title: '跳转失败，请重试',
						icon: 'none'
					});
				}
			});
		},
		// 确认规格并加入购物车
		async confirmSpecAndAddToCart() {
			// 检查是否已选择规格
			if (this.specList.length > 0 && this.specSelected.length === 0) {
				uni.showToast({
					title: '请选择规格',
					icon: 'none'
				});
				return;
			}
			
			
			// 关闭规格选择弹窗
			this.toggleSpec();
			this.isBuying = false; // 重置标记
			
			// 等待弹窗关闭动画完成
			await new Promise(resolve => setTimeout(resolve, 300));
			
			// 直接执行加入购物车逻辑，不再次检查规格
			await this.addToCartDirectly();
		},
		stopPrevent() {},
		//设置头图信息
		initImgList() {
			const mainPic = this.product.pic || this.product.image || this.product.imageUrl;
			if (this.product.albumPics && typeof this.product.albumPics === 'string') {
				let tempPics = this.product.albumPics.split(',');
				if (mainPic) tempPics.unshift(mainPic);
				for (let item of tempPics) {
					if (item != null && item !== '') {
						this.imgList.push({
							src: util.getImageUrl ? util.getImageUrl(item) : item
						});
					}
				}
			}
			if (this.imgList.length === 0 && this.product.images && Array.isArray(this.product.images) && this.product.images.length > 0) {
				this.product.images.forEach(img => {
					this.imgList.push({
						src: util.getImageUrl ? util.getImageUrl(img) : img
					});
				});
			}
			if (this.imgList.length === 0 && mainPic) {
				this.imgList.push({
					src: util.getImageUrl ? util.getImageUrl(mainPic) : mainPic
				});
			}
			if (this.imgList.length === 0) {
				this.imgList.push({
					src: '/static/images/default-product.svg'
				});
			}
		},
		//设置服务信息
		initServiceList() {
			// 优先使用商品数据中的服务信息
			if (this.product.services) {
				try {
					let services = typeof this.product.services === 'string' 
						? JSON.parse(this.product.services) 
						: this.product.services;
					if (Array.isArray(services) && services.length > 0) {
						this.serviceList = services;
						return;
					}
				} catch (error) {
					console.error('解析服务信息失败:', error);
				}
			// 如果没有服务信息，使用默认值
					// 如果没有服务信息，使用默认值
			}
		},
		//设置商品规格
		initSpecList(data) {
			// 优先从商品数据中的specs字段获取（后台保存的购买类型）
			if (this.product.specs) {
				try {
					let specsData = typeof this.product.specs === 'string' 
						? JSON.parse(this.product.specs) 
						: this.product.specs;
					
					if (Array.isArray(specsData) && specsData.length > 0) {
						specsData.forEach((spec, index) => {
							const specId = spec.id || spec.name || `spec_${index}`;
							this.specList.push({
								id: specId,
								name: spec.name || ''
							});
							
							// 处理规格值：支持字符串数组、对象数组{name, price?, image?}、逗号分隔字符串
							if (spec.values) {
								let valuesArray = [];
								if (Array.isArray(spec.values)) {
									valuesArray = spec.values;
								} else if (typeof spec.values === 'string') {
									// 与后台录入一致：逗号、中文标点、空格均可分隔多个规格值
									valuesArray = spec.values.split(/[,，、\s]+/).map(v => v.trim()).filter(v => v);
								}
								if (valuesArray.length > 0) {
									valuesArray.forEach(value => {
										const name = (value && typeof value === 'object' && value.name != null) ? value.name : (value != null ? String(value) : '');
										const opt = { pid: specId, pname: spec.name || '', name };
										if (value && typeof value === 'object') {
											if (value.price != null) opt.price = value.price;
											if (value.image != null) opt.image = value.image;
											else if (value.pic != null) opt.image = value.pic;
										}
										this.specChildList.push(opt);
									});
								}
							}
						});
						return; // 如果从specs获取到数据，直接返回
					}
					// 后端可能返回 { skus: [{ spData, price, ... }] } 格式，无规格组数组；下面会由 skuStockList 反推
				} catch (error) {
					console.error('解析specs失败:', error);
				}
			}
			
			// 如果没有specs字段，尝试从productAttributeList获取
			if (data.productAttributeList && Array.isArray(data.productAttributeList)) {
				for (let i = 0; i < data.productAttributeList.length; i++) {
					let item = data.productAttributeList[i];
					if (item.type == 0) {
						this.specList.push({
							id: item.id,
							name: item.name
						});
						if (item.handAddStatus == 1) {
							//支持手动新增
						let valueList = data.productAttributeValueList || [];
							let filterValueList = valueList.filter(value => value.productAttributeId == item.id);
							if (filterValueList.length > 0) {
								let inputList = filterValueList[0].value.split(',');
								for (let j = 0; j < inputList.length; j++) {
									this.specChildList.push({
										pid: item.id,
										pname: item.name,
										name: inputList[j]
									});
								}
							}
						} else if (item.handAddStatus == 0) {
							//不支持手动新增的
							if (item.inputList) {
								let inputList = item.inputList.split(',');
								for (let j = 0; j < inputList.length; j++) {
									this.specChildList.push({
										pid: item.id,
										pname: item.name,
										name: inputList[j]
									});
								}
							}
						}
					}
				}
			}
			
			// 若仍无规格但有多个SKU（如后端只返回skuStockList、specs 为对象等），从skuStockList 反推规格，避免直接进确认
			if (this.specList.length === 0 && this.skuStockList.length > 1) {
				const keyOrder = [];
				const keyToValues = {};
				for (let i = 0; i < this.skuStockList.length; i++) {
					try {
						const spDataArr = JSON.parse(this.skuStockList[i].spData || '[]');
						for (let j = 0; j < spDataArr.length; j++) {
							const k = spDataArr[j].key;
							const v = spDataArr[j].value;
							if (k == null || v == null) continue;
							if (!keyToValues[k]) {
								keyOrder.push(k);
								keyToValues[k] = new Set();
							}
							keyToValues[k].add(String(v));
						}
					} catch (e) {
						console.error('skuStockList 解析 spData 失败:', e);
					}
				}
				keyOrder.forEach(specKey => {
					this.specList.push({ id: specKey, name: specKey });
					(keyToValues[specKey] || []).forEach(val => {
						this.specChildList.push({
							pid: specKey,
							pname: specKey,
							name: String(val)
						});
					});
				});
			}
			
			// 不再用「所有 spData 里的 value 集合」过滤 specChildList：
			// 多规格时若后端 sku 的 spData 与 specs 不完全一致，会把整组选项误删，弹窗只剩「请选择规格」空白区。
			// 规格选项以商品 specs / 属性配置为准；库存与价格在 getSkuStock / refreshStock 中再校验即可。
			// 不做任何默认选中：必须由用户手动点选规格后，才能购买/加购
			this.specSelected = [];
			this.specChildList.forEach(c => this.$set(c, 'selected', false));
		},
		//设置商品参数
		initAttrList(data) {
			// 优先使用商品数据中的params字段（JSON格式）
			if (this.product.params) {
					try {
					// 先检查是否是有效的JSON字符串
					let paramsStr = typeof this.product.params === 'string' 
						? this.product.params.trim() 
						: JSON.stringify(this.product.params);
					
					// 如果字符串为空，跳过
					if (!paramsStr || paramsStr === '') {
						// 继续使用其他方式获取参数
					} else if (paramsStr.startsWith('{') && paramsStr.endsWith('}')) {
						// 看起来是JSON格式，尝试解析
						let params = JSON.parse(paramsStr);
						if (params && typeof params === 'object' && !Array.isArray(params)) {
							Object.keys(params).forEach(key => {
								this.attrList.push({
									key: key,
									value: params[key]
								});
							});
							return;
						}
					} else {
						// 不是JSON格式，可能是普通文本，静默跳过（不显示警告）
						// 因为params字段可能存储的是其他格式的数据
					}
				} catch (error) {
					// 静默处理警告
					// 解析失败，继续使用其他方式获取参数
				}
			}
			
			// 如果没有params字段，尝试从productAttributeList获取
			if (data.productAttributeList && Array.isArray(data.productAttributeList)) {
				for (let item of data.productAttributeList) {
					if (item.type == 1) {
						let valueList = data.productAttributeValueList || [];
						let filterValueList = valueList.filter(value => value.productAttributeId == item.id);
						if (filterValueList.length > 0) {
							let value = filterValueList[0].value;
							this.attrList.push({
								key: item.name,
								value: value
							});
						}
					}
				}
			}
			
			// 如果没有参数数据，尝试从商品本身的attributes获取
			if (this.attrList.length === 0 && this.product.attributes && Array.isArray(this.product.attributes)) {
				this.product.attributes.forEach(attr => {
					this.attrList.push({
						key: attr.name || attr.key,
						value: attr.value
					});
				});
			}
		},
		// 加载评价数据
		async loadReviews(productId) {
			try {
				// 尝试调用评价API
				if (api.getProductReviews) {
					const res = await api.getProductReviews(productId);
					if ((res.code === 0 || res.code === 200) && res.data) {
						const reviews = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || []);
						this.reviewCount = reviews.length;
						
						// 计算好评率（假设评分>=4为好评）
						if (reviews.length > 0) {
							const goodCount = reviews.filter(r => (r.rating || 0) >= 4).length;
							this.goodRate = Math.round((goodCount / reviews.length) * 100);
							this.latestReview = reviews[0] || null;
						} else {
							this.goodRate = 0;
							this.latestReview = null;
						}
					}
				} else {
					// 如果没有评价API，设置默认值
					this.reviewCount = 0;
					this.goodRate = 0;
					this.latestReview = null;
				}
			} catch (error) {
				console.error('加载评价失败:', error);
				// 出错时设置默认值
				this.reviewCount = 0;
				this.goodRate = 0;
				this.latestReview = null;
			}
		},
		// 刷新库存（只刷新库存字段，不重新加载整个商品详情）
		async refreshStock() {
			if (!this.productId) {
				return;
			}
			try {
				const res = await api.getGoodsDetail(this.productId, false);
				if ((res.code === 0 || res.code === 200) && res.data) {
					const goodsData = res.data;
							// 只更新库存相关字段
					this.product.stock = goodsData.stock || 0;
					this.product.sale = goodsData.sale || 0;
					this.product.viewCount = goodsData.viewCount || 0;
					
					// 如果选择了规格，也需要更新SKU库存
					let productSkuStock = this.getSkuStock();
					if (productSkuStock && goodsData.skuStockList) {
						// 更新SKU库存列表
						this.skuStockList = goodsData.skuStockList || [];
						// 重新获取当前选中的SKU库存
						let currentSkuStock = this.getSkuStock();
						if (currentSkuStock) {
							this.product.stock = currentSkuStock.stock || 0;
						}
					}
					
				}
			} catch (error) {
				console.error('刷新库存失败:', error);
			}
		},
		// 启动库存自动刷新
		startStockRefresh() {
			// 清除旧的定时器
			this.stopStockRefresh();
			// 每30秒自动刷新一次库存
			this.stockRefreshTimer = setInterval(() => {
				this.refreshStock();
			}, 30000);
		},
		// 停止库存自动刷新
		stopStockRefresh() {
			if (this.stockRefreshTimer) {
				clearInterval(this.stockRefreshTimer);
				this.stockRefreshTimer = null;
			}
		},
		// 初始化商品详情信息
		initProductDesc() {
			// #ifdef MP
			// 小程序环境无法使用document对象，可以直接通过CSS控制样式
			if (this.product.detailMobileHtml) {
				this.desc = this.product.detailMobileHtml;
			} else if (this.product.detailImages && Array.isArray(this.product.detailImages) && this.product.detailImages.length > 0) {
				// 如果没有HTML，使用图片生成HTML
				let html = '';
				this.product.detailImages.forEach((img, index) => {
					// 处理图片路径，可能是相对路径或完整URL
					let imageUrl = img;
					if (typeof img === 'string' && !img.startsWith('http')) {
							// 如果product/xxx.jpg 格式，转换为 /upload/product/xxx.jpg
						if (img.startsWith('product/')) {
							imageUrl = '/upload/' + img;
						}
						imageUrl = util.getImageUrl(imageUrl);
					}
					html += `<img src="${imageUrl}" style="width:100%;height:auto;display:block;" />`;
				});
				this.desc = html;
			} else {
				this.desc = '<p>暂无商品详情</p>';
			}
			// #endif
			
			// #ifdef H5
			let rawhtml = this.product.detailMobileHtml || '';
			if (rawhtml) {
				let tempNode = document.createElement('div');
				tempNode.innerHTML = rawhtml;
				let imgs = tempNode.getElementsByTagName('img');
				for (let i = 0; i < imgs.length; i++) {
					imgs[i].style.width = '100%';
					imgs[i].style.height = 'auto';
					imgs[i].style.display = 'block';
				}
				this.desc = tempNode.innerHTML;
			} else if (this.product.detailImages && Array.isArray(this.product.detailImages) && this.product.detailImages.length > 0) {
				let html = '';
				this.product.detailImages.forEach((img, index) => {
					// 处理图片路径，可能是相对路径或完整URL
					let imageUrl = img;
					if (typeof img === 'string' && !img.startsWith('http')) {
						// 如果product/xxx.jpg 格式，转换为 /upload/product/xxx.jpg
						if (img.startsWith('product/')) {
							imageUrl = '/upload/' + img;
						}
						imageUrl = util.getImageUrl(imageUrl);
					}
					html += `<img src="${imageUrl}" style="width:100%;height:auto;display:block;" />`;
				});
				this.desc = html;
			} else {
				this.desc = '<p>暂无商品详情</p>';
			}
			// #endif
		},
		//处理创建浏览记录
		handleReadHistory() {
			// 暂时不实现浏览记录功能
		},
		//当商品规格改变时，修改商品信息
		changeSpecInfo() {
			let skuStock = this.getSkuStock();
			if (skuStock != null) {
				this.product.originalPrice = skuStock.price;
				if (this.product.promotionType == 1) {
					//单品优惠使用促销价格
					this.product.price = skuStock.promotionPrice;
				} else {
					this.product.price = skuStock.price;
				}
				this.product.stock = skuStock.stock;
			}
		},
		// 获取当前价格（用于弹窗显示）
		getCurrentPrice() {
			let skuStock = this.getSkuStock();
			if (skuStock) {
				return skuStock.promotionPrice || skuStock.price || this.product.price;
			}
			return this.product.price || 0;
		},
		// 获取当前库存（用于弹窗显示）
		getCurrentStock() {
			let skuStock = this.getSkuStock();
			if (skuStock) {
				return skuStock.stock || 0;
			}
			return this.product.stock || 0;
		},
		// 获取当前主图：选中规格对应 SKU 有图则用 SKU 图，否则用商品主图
		getCurrentImage() {
			let skuStock = this.getSkuStock();
			let pic = (skuStock && (skuStock.pic || skuStock.image)) || this.product.pic || this.product.image;
			return pic ? (util.getImageUrl ? util.getImageUrl(pic) : pic) : '/static/images/default-product.svg';
		},
		// 规格选项行用图：选项 image/pic 用选项图，否则用商品主图
		getSpecOptionImage(childItem) {
			let pic = (childItem && (childItem.image || childItem.pic)) || this.product.pic || this.product.image;
			return pic ? (util.getImageUrl ? util.getImageUrl(pic) : pic) : '/static/images/default-product.svg';
		},
		// 大图：可后续接相册或规格大图
		openSpecBigImage(item) {
			uni.showToast({ title: '大图功能待对接', icon: 'none' });
		},
		changeSpecQuantity(delta) {
			const max = Math.max(0, parseInt(this.getCurrentStock(), 10) || 0);
			let n = Math.max(1, (this.specQuantity || 1) + delta);
			if (max > 0 && n > max) n = max;
			this.specQuantity = n;
		},
		// 检查规格是否禁用（库存）
		isSpecDisabled(specItem) {
			// 这里可以根据库存情况判断规格是否可用
			// 暂时返回false，表示所有规格都可用
			return false;
		},
		//获取当前选中商品的SKU
		getSkuStock() {
			for (let i = 0; i < this.skuStockList.length; i++) {
				try {
					let spDataArr = JSON.parse(this.skuStockList[i].spData);
					let availAbleSpecSet = new Map();
					for (let j = 0; j < spDataArr.length; j++) {
						availAbleSpecSet.set(spDataArr[j].key, spDataArr[j].value);
					}
					let correctCount = 0;
					for (let item of this.specSelected) {
						let value = availAbleSpecSet.get(item.pname);
						if (value != null && value == item.name) {
							correctCount++;
						}
					}
					if (correctCount == this.specSelected.length) {
						return this.skuStockList[i];
					}
				} catch (e) {
					console.error('解析spData失败:', e);
				}
			}
			return null;
		},
		//将商品加入到购物车（直接执行，不检查规格）
		async addToCartDirectly() {
			if (!this.checkForLogin()) {
				return;
			}
			
			let productSkuStock = this.getSkuStock();
			
			// 检查库存
			if (!this.product || this.product.stock <= 0) {
				uni.showToast({
					title: '商品库存不足',
					icon: 'none'
				});
				return;
			}
			
			try {
				// 获取用户信息
				const userInfo = uni.getStorageSync('userInfo') || this.userInfo || {};
				const userId = userInfo.id || userInfo.uid;
				
				if (!userId) {
					uni.showToast({
						title: '用户信息获取失败，请重新登录',
						icon: 'none'
					});
					return;
				}
				
				if (!this.product || !this.product.id) {
					uni.showToast({
						title: '商品信息获取失败',
						icon: 'none'
					});
					return;
				}
				
				const qty = Math.max(1, parseInt(this.specQuantity, 10) || 1);
				const cartData = {
					userId: userId,
					productId: this.product.id,
					quantity: qty,
					skuId: productSkuStock ? productSkuStock.id : null
				};
				
				// 调用API添加到购物车
				const res = await cartApi.addToCart(cartData);
				
				if (res && (res.code === 0 || res.code === 200)) {
					uni.showToast({
						title: '已加入购物车',
						icon: 'success',
						duration: 800
					});
					
					// 刷新库存显示（虽然加入购物车不会减少库存，但可以确保显示最新数据）
					this.refreshStock();
					
					// 更新购物车数量（如果Vuex中有购物车计数）
					if (this.$store && this.$store.commit) {
						// 可以在这里更新购物车计数
					}
					
					// 加入成功后跳转到购物车页面
					setTimeout(() => {
						this.goToCart();
					}, 600);
				} else {
					const errorMsg = res?.msg || res?.message || '加入购物车失败';
					uni.showToast({
						title: errorMsg,
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '加入购物车失败，请重试',
					icon: 'none'
				});
			}
		},
		//将商品加入到购物车（会检查规格）
		async addToCart() {
			if (!this.checkForLogin()) {
				return;
			}

			// 和「立即购买」保持一致：先弹出规格数量选择框，再由弹窗内按钮确认加入购物车
			// - 有多规格时清空预选，强制用户选择
			// - 无规格商品也允许在弹窗内调整数量后加入购物车
			this.clearSpecSelectionWhenMultipleOptions();
			this.specQuantity = 1;
			this.isBuying = false;
			this.specClass = 'show';
		},
		checkForLogin() {
			const hasLogin = this.hasLogin || this.isLoggedIn || (this.userInfo && this.userInfo.id);
			if (!hasLogin) {
				uni.showModal({
					title: '提示',
					content: '你还没登录，是否要登录？',
					confirmText: '去登录',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) {
							uni.navigateTo({
								url: '/pages-auth/login'
							})
						}
					}
				});
				return false;
			} else {
				return true;
			}
		},
		async initProductCollection() {
			// 检查用户是否已登录
			if (!this.isLoggedIn) {
				this.favorite = false;
				return;
			}
			
			const userInfo = uni.getStorageSync('userInfo');
			const userId = userInfo?.id || userInfo?.uid;
			const productId = this.productId || this.product?.id;
			
			if (!userId || !productId) {
				this.favorite = false;
				return;
			}
			
			try {
							// 调用后端API检查收藏状态
				const res = await api.checkProductCollectStatus(productId, userId);
				
				if (res && (res.code === 200 || res.code === 0)) {
					this.favorite = res.data === true;
				} else {
					this.favorite = false;
				}
			} catch (error) {
				// 静默处理错误
				this.favorite = false;
			}
		},
		//跳转到品牌详情页
		navToBrandDetail(){
			if (this.brand && this.brand.id) {
				uni.showToast({
					title: '品牌详情功能开发中',
					icon: 'none'
				});
			}
		},
		navToStore() {
			const params = {
				storeName: this.storeInfo.name || '宠物家商品官方旗舰店'
			};
			if (this.storeInfo.id) {
				params.storeId = this.storeInfo.id;
			}
			const avatar = this.storeInfo.avatar || this.storeInfo.logo;
			if (avatar) {
				params.storeAvatar = avatar;
			}
			if (this.storeInfo.logo && this.storeInfo.logo !== avatar) {
				params.storeLogo = this.storeInfo.logo;
			}
			const queryString = Object.keys(params).map(key => {
				return `${key}=${encodeURIComponent(params[key])}`;
			}).join('&');
			uni.navigateTo({
				url: `/pages-goods/list?${queryString}`
			});
		},
	// 跳转到商品评价页面（单独页面，只显示当前商品的评价）
	goToReviews() {
		if (!this.productId) {
			uni.showToast({
				title: '商品信息不完整',
				icon: 'none'
			});
			return;
		}
		
		// 跳转到专门的商品评价页面
		uni.navigateTo({
			url: `/pages-goods/product-reviews?productId=${this.productId}`
		});
	},
	//获取商家头像URL
		getStoreImageUrl(avatar) {
			if (!avatar) {
				return '';
			}
			// 使用util.getImageUrl处理图片URL
			const url = util.getImageUrl(avatar);
			return url;
		},
		//商家头像加载失败处理
		handleStoreImageError(e) {
			console.error('商家头像加载失败:', e);
			this.$set(this.storeInfo, 'avatar', '');
			this.$set(this.storeInfo, 'logo', '');
		}
	}
}

</script>

<style lang='scss'>
page {
	background: #f5f5f5;
	padding-bottom: 160upx;
}

.icon-you {
	font-size: 30upx;
	color: #888;
}

.carousel {
	height: 722upx;
	position: relative;

	swiper {
		height: 100%;
	}

	.image-wrapper {
		width: 100%;
		height: 100%;
	}

	.swiper-item {
		display: flex;
		justify-content: center;
		align-content: center;
		height: 750upx;
		overflow: hidden;

		image {
			width: 100%;
			height: 100%;
		}
	}
}

/* 标题简介 */
.introduce-section {
	background: #fff;
	padding: 20upx 30upx;

	.title {
		font-size: 32upx;
		color: #333;
		height: 50upx;
		line-height: 50upx;
		display: block;
		margin-bottom: 10upx;
	}

	.title2-wrapper {
		margin-top: 10upx;
		margin-bottom: 10upx;
	}
	
	.title2 {
		font-size: 24upx;
		color: #999;
		line-height: 36upx;
		display: block;
	}

	.price-box {
		display: flex;
		align-items: baseline;
		height: 64upx;
		padding: 10upx 0;
		font-size: 26upx;
		color: #ff4444;
	}

	.price {
		font-size: 38upx;
	}

	.m-price {
		margin: 0 12upx;
		color: #999;
		text-decoration: line-through;
	}

	.coupon-tip {
		align-items: center;
		padding: 4upx 10upx;
		background: #ff4444;
		font-size: 24upx;
		color: #fff;
		border-radius: 6upx;
		line-height: 1;
		transform: translateY(-4upx);
	}

	.bot-row {
		display: flex;
		align-items: center;
		height: 50upx;
		font-size: 24upx;
		color: #999;

		text {
			flex: 1;
		}
	}
}

/* 分享 */
.share-section {
	display: flex;
	align-items: center;
	color: #666;
	background: linear-gradient(left, #fdf5f6, #fbebf6);
	padding: 12upx 30upx;

	.share-icon {
		display: flex;
		align-items: center;
		width: 70upx;
		height: 30upx;
		line-height: 1;
		border: 1px solid #ff4444;
		border-radius: 4upx;
		position: relative;
		overflow: hidden;
		font-size: 22upx;
		color: #ff4444;

		&:after {
			content: '';
			width: 50upx;
			height: 50upx;
			border-radius: 50%;
			left: -20upx;
			top: -12upx;
			position: absolute;
			background: #ff4444;
		}
	}

	.icon-xingxing {
		position: relative;
		z-index: 1;
		font-size: 24upx;
		margin-left: 2upx;
		margin-right: 10upx;
		color: #fff;
		line-height: 1;
	}

	.tit {
		font-size: 28upx;
		margin-left: 10upx;
	}

	.icon-bangzhu1 {
		padding: 10upx;
		font-size: 30upx;
		line-height: 1;
	}

	.share-btn {
		flex: 1;
		text-align: right;
		font-size: 24upx;
		color: #ff4444;
	}

	.icon-you {
		font-size: 24upx;
		margin-left: 4upx;
		color: #ff4444;
	}
}

.c-list {
	font-size: 28upx;
	color: #666;
	background: #fff;
	margin-top: 16upx;

	.c-row {
		display: flex;
		align-items: center;
		padding: 12upx 30upx;
		position: relative;
	}

	.tit {
		width: 140upx;
	}

	.con {
		flex: 1;
		color: #333;

		.selected-text {
			margin-right: 10upx;
		}
	}

	.bz-list {
		height: 32upx;
		font-size: 24upx;
		color: #4CAF50;
		font-weight: normal;
		white-space: nowrap;
		overflow: hidden;
		display: flex;
		flex-wrap: nowrap;
		align-items: center;
		line-height: 32upx;

		text {
			display: inline-block;
			margin-right: 12upx;
			flex-shrink: 0;
		}
	}

	.con-list {
		flex: 1;
		display: flex;
		flex-direction: column;
		color: #333;
		line-height: 40upx;
	}

	.red {
		color: #ff4444;
	}
}

/* 评价 */
.eva-section {
	display: flex;
	flex-direction: column;
	padding: 15upx 25upx;
	background: #fff;
	margin-top: 12upx;

	.e-header {
		display: flex;
		align-items: center;
		height: 56upx;
		font-size: 24upx;
		color: #999;

		.tit {
			font-size: 26upx;
			color: #333;
			margin-right: 4upx;
		}

		.tip {
			flex: 1;
			text-align: right;
			font-size: 22upx;
		}

		.icon-you {
			margin-left: 8upx;
			font-size: 22upx;
		}
	}
}

.eva-box {
	display: flex;
	padding: 12upx 0;

	.portrait {
		flex-shrink: 0;
		width: 60upx;
		height: 60upx;
		border-radius: 100px;
	}

	.right {
		flex: 1;
		display: flex;
		flex-direction: column;
		font-size: 24upx;
		color: #666;
		padding-left: 20upx;

		.name {
			font-size: 24upx;
			color: #333;
			margin-bottom: 6upx;
		}

		.con {
			font-size: 24upx;
			color: #333;
			padding: 8upx 0;
			line-height: 1.4;
		}

		.bot {
			display: flex;
			justify-content: space-between;
			font-size: 20upx;
			color: #999;
			margin-top: 4upx;
		}
	}
}

/*  详情 */
.detail-desc {
	background: #fff;
	margin-top: 16upx;

	.d-header {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 80upx;
		font-size: 30upx;
		color: #333;
		position: relative;

		text {
			padding: 0 20upx;
			background: #fff;
			position: relative;
			z-index: 1;
		}

		&:after {
			position: absolute;
			left: 50%;
			top: 50%;
			transform: translateX(-50%);
			width: 300upx;
			height: 0;
			content: '';
			border-bottom: 1px solid #ccc;
		}
	}
}

.detail-desc ::v-deep img {
	width: 100%;
	height: auto;
}

/* 规格选择弹窗 */
.attr-content {
	padding: 10upx 30upx;

	.a-t {
		display: flex;

		image {
			width: 170upx;
			height: 170upx;
			flex-shrink: 0;
			margin-top: -40upx;
			border-radius: 8upx;
		}

		.right {
			display: flex;
			flex-direction: column;
			padding-left: 24upx;
			font-size: 28upx;
			color: #666;
			line-height: 42upx;

			.price {
				font-size: 34upx;
				color: #ff4444;
				margin-bottom: 10upx;
			}

			.selected-text {
				margin-right: 10upx;
			}
		}
	}

	.attr-list {
		display: flex;
		flex-direction: column;
		font-size: 30upx;
		color: #666;
		padding-top: 30upx;
		padding-left: 10upx;
	}

	.item-list {
		padding: 20upx 0 0;
		display: flex;
		flex-wrap: wrap;

		text {
			display: flex;
			align-items: center;
			justify-content: center;
			background: #eee;
			margin-right: 20upx;
			margin-bottom: 20upx;
			border-radius: 100upx;
			min-width: 60upx;
			height: 60upx;
			padding: 0 20upx;
			font-size: 28upx;
			color: #333;
		}

		.selected {
			background: #fbebee;
			color: #ff4444;
		}
	}
}

.no-padding {
	padding: 0upx 0upx;
}

/*  弹出�?*/
.popup {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 99;

	&.show {
		display: block;

		.mask {
			animation: showPopup 0.2s linear both;
		}

		.layer {
			animation: showLayer 0.2s linear both;
		}
	}

	&.hide {
		.mask {
			animation: hidePopup 0.2s linear both;
		}

		.layer {
			animation: hideLayer 0.2s linear both;
		}
	}

	&.none {
		display: none;
	}

	.mask {
		position: fixed;
		top: 0;
		width: 100%;
		height: 100%;
		z-index: 1;
		background-color: rgba(0, 0, 0, 0.4);
	}

	.layer {
		position: fixed;
		z-index: 99;
		bottom: 0;
		width: 100%;
		min-height: 40vh;
		border-radius: 10upx 10upx 0 0;
		background-color: #fff;

		.btn {
			height: 66upx;
			line-height: 66upx;
			border-radius: 100upx;
			background: #ff4444;
			font-size: 30upx;
			color: #fff;
			margin: 30upx auto 20upx;
		}
	}
	
	/* 规格弹窗：底部锚点容器，整块白框一定在屏幕最底部（flex 压底�?*/
	.spec-bottom-anchor {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		z-index: 99;
		display: flex;
		flex-direction: column;
		justify-content: flex-end;
		pointer-events: none;
	}
	.spec-bottom-anchor .layer {
		pointer-events: auto;
		position: static !important;
		min-height: 0 !important;
		height: auto !important;
		flex-shrink: 0;
	}
	
	/* 规格选择框（与参考图一致）：高度随内容，内容少时不留空�?*/
	.spec-popup-content {
		padding: 0;
		display: flex;
		flex-direction: column;
		min-height: 0;
		height: auto;
		max-height: 75vh;
		position: relative;
		
		.close-btn {
			position: absolute;
			top: 16upx;
			right: 16upx;
			width: 48upx;
			height: 48upx;
			display: flex;
			align-items: center;
			justify-content: center;
			z-index: 10;
			.close-icon { font-size: 38upx; color: #999; line-height: 1; }
		}
		
		.spec-product-row {
			display: flex;
			align-items: flex-start;
			padding: 20upx 20upx 16upx;
			border-bottom: 1px solid #f0f0f0;
			.spec-product-thumb {
				width: 120upx;
				height: 120upx;
				border-radius: 8upx;
				margin-right: 20upx;
				flex-shrink: 0;
			}
			.spec-product-right { flex: 1; min-width: 0; }
			.spec-price-row {
				display: flex;
				align-items: baseline;
				margin-bottom: 8upx;
				.spec-price-current { font-size: 36upx; font-weight: bold; color: #ff4444; margin-right: 12upx; }
				.spec-price-original { font-size: 24upx; color: #999; text-decoration: line-through; }
			}
			.spec-selected-row {
				font-size: 24upx;
				color: #333;
				margin-bottom: 12upx;
				line-height: 1.4;
				.spec-selected-label { color: #666; }
				.spec-selected-value { color: #333; }
				.spec-selected-placeholder { color: #999; }
			}
			.spec-quantity-row {
				display: flex;
				align-items: center;
				.spec-quantity-wrap {
					display: flex;
					align-items: center;
					border: 1px solid #e0e0e0;
					border-radius: 6upx;
					overflow: hidden;
					.spec-qty-btn {
						width: 48upx;
						height: 44upx;
						display: flex;
						align-items: center;
						justify-content: center;
						background: #f8f8f8;
						.spec-qty-symbol {
							font-size: 28upx;
							color: #666;
							line-height: 1;
						}
					}
					.spec-qty-num {
						min-width: 56upx;
						text-align: center;
						font-size: 26upx;
						color: #333;
					}
				}
				.spec-stock-tip { font-size: 22upx; color: #ff6b6b; margin-left: 16upx; }
			}
		}
		
		.spec-list-scroll {
			flex: 0 0 auto; /* 不撑满，只占内容高度，规格少时弹窗整体变�?*/
			padding: 0 20upx;
			max-height: 320upx;
			.spec-category {
				padding: 16upx 0;
				border-bottom: 1px solid #f5f5f5;
				&:last-child { border-bottom: none; }
			}
			.spec-category-title {
				display: block;
				font-size: 26upx;
				color: #333;
				font-weight: 500;
				margin-bottom: 12upx;
			}
			/* 一行能放多个就放多个，不够才换行（与参考图口味/组合一致） */
			.spec-option-wrap {
				display: flex;
				flex-wrap: wrap;
				gap: 12upx;
			}
			.spec-option-chip {
				display: flex;
				align-items: center;
				flex: 0 0 auto;
				min-width: 240upx;
				max-width: 100%;
				padding: 10upx 14upx;
				border-radius: 8upx;
				border: 2px solid #e0e0e0;
				background: #fff;
				box-sizing: border-box;
				&.selected { border-color: #ff4444; background: #fff8f8; }
				&.disabled { opacity: 0.5; }
				.spec-option-thumb {
					width: 48upx;
					height: 48upx;
					border-radius: 6upx;
					margin-right: 12upx;
					flex-shrink: 0;
				}
				.spec-option-name { font-size: 24upx; color: #333; flex: 1; min-width: 0; }
			}
		}
		
		.spec-extra-row {
			padding: 10upx 20upx;
			font-size: 24upx;
			color: #666;
			.spec-pay-text { color: #07c160; }
		}
		
		.spec-footer-wrap {
			position: relative;
			padding: 16upx 20upx;
			padding-bottom: calc(16upx + env(safe-area-inset-bottom));
			border-top: 1px solid #f0f0f0;
			background: #fff;
			.spec-cta-btn {
				width: 100%;
				height: 64upx;
				line-height: 64upx;
				border-radius: 32upx;
				font-size: 28upx;
				font-weight: 500;
				border: none;
				&.buy-btn { background: #ffac30; color: #fff; }
				&.cart-btn { background: #ff9500; color: #fff; }
			}
		}
	}

	@keyframes showPopup {
		0% {
			opacity: 0;
		}

		100% {
			opacity: 1;
		}
	}

	@keyframes hidePopup {
		0% {
			opacity: 1;
		}

		100% {
			opacity: 0;
		}
	}

	@keyframes showLayer {
		0% {
			transform: translateY(120%);
		}

		100% {
			transform: translateY(0%);
		}
	}

	@keyframes hideLayer {
		0% {
			transform: translateY(0);
		}

		100% {
			transform: translateY(120%);
		}
	}
}

/* 底部操作菜单 */
.page-bottom {
	position: fixed;
	left: 30upx;
	bottom: 30upx;
	z-index: 95;
	display: flex;
	justify-content: center;
	align-items: center;
	width: 690upx;
	height: 100upx;
	background: rgba(255, 255, 255, .9);
	box-shadow: 0 0 20upx 0 rgba(0, 0, 0, .5);
	border-radius: 16upx;

	.p-b-btn {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		font-size: 24upx;
		color: #666;
		width: 96upx;
		height: 80upx;

		.yticon {
			font-size: 40upx;
			line-height: 48upx;
			color: #999;
		}

		&.active,
		&.active .yticon {
			color: #ff4444;
		}

		.icon-fenxiang2 {
			font-size: 42upx;
			transform: translateY(-2upx);
		}

		.icon-shoucang {
			font-size: 46upx;
		}
	}

	.action-btn-group {
		display: flex;
		height: 76upx;
		border-radius: 100px;
		overflow: hidden;
		box-shadow: 0 20upx 40upx -16upx #fa436a;
		box-shadow: 1px 2px 5px rgba(219, 63, 96, 0.4);
		background: linear-gradient(to right, #ffac30, #fa436a, #F56C6C);
		margin-left: 20upx;
		position: relative;

		&:after {
			content: '';
			position: absolute;
			top: 50%;
			right: 50%;
			transform: translateY(-50%);
			height: 28upx;
			width: 0;
			border-right: 1px solid rgba(255, 255, 255, .5);
		}

		.action-btn {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 180upx;
			height: 100%;
			font-size: 28upx;
			padding: 0;
			border-radius: 0;
			background: transparent;
			color: #fff;
		}
	}
}

/* 优惠券面�?*/
.mask {
	display: flex;
	align-items: flex-end;
	position: fixed;
	left: 0;
	top: 0;
	bottom: 0;
	width: 100%;
	background: rgba(0, 0, 0, 0);
	z-index: 9995;
	transition: .3s;

	.mask-content {
		width: 100%;
		min-height: 30vh;
		max-height: 70vh;
		background: #f3f3f3;
		transform: translateY(100%);
		transition: .3s;
		overflow-y: scroll;
	}

	&.none {
		display: none;
	}

	&.show {
		background: rgba(0, 0, 0, .4);

		.mask-content {
			transform: translateY(0);
		}
	}
}

/* 优惠券列�?*/
.coupon-item {
	display: flex;
	flex-direction: column;
	margin: 20upx 24upx;
	background: #fff;

	.con {
		display: flex;
		align-items: center;
		position: relative;
		height: 120upx;
		padding: 0 30upx;

		&:after {
			position: absolute;
			left: 0;
			bottom: 0;
			content: '';
			width: 100%;
			height: 0;
			border-bottom: 1px dashed #f3f3f3;
			transform: scaleY(50%);
		}
	}

	.left {
		display: flex;
		flex-direction: column;
		justify-content: center;
		flex: 1;
		overflow: hidden;
		height: 100upx;
	}

	.title {
		font-size: 32upx;
		color: #333;
		margin-bottom: 10upx;
	}

	.time {
		font-size: 24upx;
		color: #999;
	}

	.right {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		font-size: 26upx;
		color: #666;
		height: 100upx;
	}

	.price {
		font-size: 44upx;
		color: #ff4444;

		&:before {
			content: '¥';
			font-size: 34upx;
		}
	}

	.tips {
		font-size: 24upx;
		color: #999;
		line-height: 60upx;
		padding-left: 30upx;
	}

	.circle {
		position: absolute;
		left: -6upx;
		bottom: -10upx;
		z-index: 10;
		width: 20upx;
		height: 20upx;
		background: #f3f3f3;
		border-radius: 100px;

		&.r {
			left: auto;
			right: -6upx;
		}
	}
}

.store-info {
	margin-top: 16upx;
	background-color: #fff;
	display: flex;
	flex-direction: column;

	.d-header {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 80upx;
		font-size: 30upx;
		color: #333;
		position: relative;

		text {
			padding: 0 20upx;
			background: #fff;
			position: relative;
			z-index: 1;
		}

		&::before,
		&::after {
			content: '';
			position: absolute;
			left: 0;
			right: 0;
			height: 1px;
			background: #e5e5e5;
			top: 50%;
		}
	}

	.store-box {
		display: flex;
		align-items: flex-start;
		padding: 30upx;
		position: relative;

		.store-left {
			flex: 1;
			display: flex;
			align-items: flex-start;

			.store-logo {
				width: 80upx;
				height: 80upx;
				margin-right: 20upx;
				border-radius: 8upx;
				overflow: hidden;
				background-color: #f5f5f5;
				flex-shrink: 0;

				image {
					width: 100%;
					height: 100%;
				}
			}

			.store-details {
				flex: 1;
				display: flex;
				flex-direction: column;

				.store-name-row {
					display: flex;
					align-items: center;
					margin-bottom: 12upx;

					.store-name {
						font-size: 28upx;
						font-weight: normal;
						color: #333;
						margin-right: 12upx;
					}

					.store-tag {
						background-color: #ff4757;
						color: #fff;
						font-size: 20upx;
						padding: 4upx 12upx;
						border-radius: 4upx;
					}
				}

				.store-guarantee {
					display: flex;
					align-items: center;
					flex-wrap: wrap;

					.guarantee-icon {
						color: #4CAF50;
						font-size: 24upx;
						margin-right: 6upx;
					}

					.guarantee-text {
						font-size: 24upx;
						color: #333;
						margin-right: 8upx;
					}

					.guarantee-desc {
						font-size: 22upx;
						color: #999;
					}
				}
			}
		}

		.store-right {
			display: flex;
			align-items: center;
			margin-left: 20upx;

			.enter-store-btn {
				display: flex;
				align-items: center;
				padding: 12upx 24upx;
				background-color: #fff;
				border: 1px solid #ddd;
				border-radius: 8upx;
				font-size: 28upx;
				color: #333;

				.arrow {
					margin-left: 6upx;
					font-size: 24upx;
					color: #999;
				}
			}
		}
	}

	.d-header {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 80upx;
		font-size: 30upx;
		color: #333;
		position: relative;
	
		text {
			padding: 0 20upx;
			background: #fff;
			position: relative;
			z-index: 1;
		}
	
		&:after {
			position: absolute;
			left: 50%;
			top: 50%;
			transform: translateX(-50%);
			width: 300upx;
			height: 0;
			content: '';
			border-bottom: 1px solid #ccc;
		}
	}
}
</style>

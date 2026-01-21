<template>
	<view>
		<view class="coupon-item x-bc" @tap="selectCoupon" :class="(isShowCheck && item.bottomTip)? 'disableClass': ''">
			<view class="box-con" v-if="item.discountType == 1">
				<view class="box-text">减免券</view>
			</view>
			<view class="box-con" v-if="item.discountType == 2">
				<view class="box-text">折扣券</view>
			</view>
			<view class="coupon-money" :class="[{graybg:(status == 1 || status == 2)}]">
				<view class="layof" v-if="item.discountType == 1">
					<text style="font-size: 16px;">￥</text>
					<text style="font-size: 28px;">{{item.discountMoneyOrPercent}}</text>
				</view>
				<view class="layof" v-if="item.discountType == 2">
					<text style="font-size: 16px;margin-right: 8px;">折</text>
					<text style="font-size: 28px;">{{(item.discountMoneyOrPercent) / 10}}</text>
				</view>
				<view class="demand">满 {{item.discountCondition}} 可用</view>
			</view>
			<view class="coupon-detail" :class="[{graybgRight:(status == 1 || status == 2) }]">
				<view style="margin-bottom: 12rpx;"><text :class="[{graybg:(status == 1 || status == 2)}]"
						style="margin-right: 4px;background: linear-gradient(132deg, #e36452 0%, #e36452 100%);color: #fff;float: left;border-radius: 4px;font-size: 12px;padding: 2px 6px;">{{item.useType==1?'服务费': item.useType==2?'电费': '总费用'}}</text>
					<text style="font-size: 15px;color: #333333;" class="coupon-title">{{item.name}}
					</text>
				</view>
				<view class="x-bc" style="margin:0 20rpx 12rpx 0;">
					<text style="font-size: 12px;color: #666666;">{{item.periodEndTime}}&nbsp;到期</text>
					<view v-if="isShowCheck">
						<image class="selectimg" :src="
						        item.checked
						            ? selectStr
						            : unSelectStr
						    "></image>
					</view>
					<view v-else>
						<view v-if="status==0" @click.stop="gotoShop"
							style="width: 60px;height: 24px;line-height: 24px;background-color: #fa3534;color: #fff;text-align: center;border-radius: 12px;">
							去使用
						</view>
					</view>
				</view>
				<view v-if="!(isShowCheck && item.bottomTip)" style="margin-bottom: 12rpx;color: #999999;font-size: 12px;text-decoration: underline;"
					@click.stop="ruleShowOrClose()">使用规则</view>
				<view v-if="item.discountType == 2" style="margin-bottom: 12rpx;color: #999999;font-size: 12px;">最多抵扣
					{{item.deductionUpper}} 元
				</view>
				<view v-if="isShowCheck && item.bottomTip" style="margin-bottom: 12rpx;color: #C0191F;font-size: 12px;">
					此券暂不可和已勾选券叠加使用</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		components: {

		},
		data() {
			return {
				unSelectStr: '/static/images/un-select.png',
				selectStr: '/static/images/select.png',
				ruleShow: false
			}
		},
		props: {
			isShowCheck: {
				type: Boolean,
				default: false
			},
			status: {
				type: Number,
				default: 0
			},
			index: {
				type: Number,
				default: 0
			},
			item: {
				type: Object
			},
		},
		methods: {
			ruleShowOrClose() {
				this.$emit('showrule', this.item);
			},
			gotoShop() {
				if (this.status != 0) {
					return
				}
				if (this.$Route.path == '/pages/home/index') {
					this.$emit('closeModel');
				} else {
					uni.switchTab({
						url: '/pages/home/index'
					})
				}
			},
			selectCoupon() {
				let that = this;
				if (that.isShowCheck) {
					that.$bus.$emit('couponChecked', that.index);
				}
			}

		}
	}
</script>

<style lang='scss' scoped>
	@import '@/static/style/mixin/text-overflow.scss';

	.disableClass {
		pointer-events: none;
		background-color: #f5f5f5 !important;
		color: #999 !important;
	}

	.selectimg {
		width: 40rpx;
		height: 40rpx;
	}

	.graybgRight {
		background-color: #F5F4F5;
	}

	.graybg {
		background: linear-gradient(132deg, #D2D2D2 0%, #D2D2D2 100%) !important;
	}

	.coupon-item {
		width: 100%;
		height: 110px;
		border-radius: 10rpx;
		margin-top: 22rpx;
		border: 1px solid #FFFFFF;
		position: relative;
		background-color: #FFFFFF;

		.coupon-money {
			display: flex;
			justify-content: center;
			flex-direction: column;
			text-align: center;
			width: 232rpx;
			height: 100%;
			border-style: none dotted none none;
			border-color: #eeeeee;
			background: linear-gradient(132deg, #e36452 0%, #e36452 100%);

			.layof {
				width: 100%;
				font-weight: 400;
				color: #FFFFFF;
				margin-bottom: 6px;
			}

			.demand {
				font-size: 12px;
				color: #FFFFFF;
			}
		}

		.box-con {
			width: 85px;
			height: 88px;
			overflow: hidden;
			position: absolute;
			top: -3px;
			right: -3px;

			.box-text {
				font-size: 12px;
				color: white;
				text-align: center;
				-webkit-transform: rotate(45deg);
				position: relative;
				padding: 2px 0;
				left: 37px;
				top: 4px;
				width: 65px;
				background-color: #e36452;
				background-image: linear-gradient(top, #ff503e, #ff2f50);
				box-shadow: 0px 0px 3px #ff1b40;
			}
		}
	}

	.coupon-detail {
		display: flex;
		flex: 1;
		justify-content: center;
		flex-direction: column;
		text-align: left;
		padding-left: 20px;
		height: 100%;

		.coupon-title {
			@include text-overflow("", 2);
			font-weight: bold;
		}
	}

	.coupon-item:after {
		width: 40rpx;
		height: 20rpx;
		position: absolute;
		left: 212rpx;
		top: -1px;
		border-radius: 0 0 40rpx 40rpx;
		content: "";
		display: block;
		background: #F5F5F7;
		border: 1px solid #eeeeee;
		border-top: 0px;
	}

	.coupon-item:before {
		width: 40rpx;
		height: 20rpx;
		position: absolute;
		left: 212rpx;
		bottom: -1px;
		border-radius: 40rpx 40rpx 0 0;
		content: "";
		display: block;
		background: #F5F5F7;
		border: 1px solid #eeeeee;
		border-bottom: 0px;
	}
</style>

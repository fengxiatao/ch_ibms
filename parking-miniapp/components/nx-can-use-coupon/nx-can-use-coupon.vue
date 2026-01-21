<template>
	<view v-if="couponData.useList.length || (couponData.stopList.length && noCouponShow)" @touchmove.stop.prevent>
		<nx-coupon-rule ref="couponRule" v-if="couponShowRule" :itemData="itemData" :ruleShow="couponShowRule">
		</nx-coupon-rule>
		<view class="cu-form-group" @click="chooseClicked()">
			<view style="
			height: 84rpx;
			font-family: PingFangSC, PingFangSC-Regular;
			text-align: left;
			line-height: 84rpx;">

				<slot  v-if="discounts > 0 && getCheckCoupluon.length == 0"><text style="font-size: 34rpx;font-weight: 400;">会员折扣优惠</text></slot >
				<slot v-if="getCheckCoupluon.length > 0"><text style="font-size: 34rpx;font-weight: 400;">优惠券</text></slot>
			</view>

			<view>
				<view style="display: inline-block;font-size: 34rpx;">
					<view v-if="discounts>0" style="font-size: 34rpx;color: #e36452;">
						{{'- ￥'+ discounts}}
					</view>
					<view v-else>
						<view v-if="couponData.useList.length" style="
						background-color: #e36452;
						color: #fff;
						border-radius: 20rpx;
						font-size: 30rpx;
						padding: 4rpx 12rpx;">{{couponData.useList.length}}张可用</view>
						<view v-else-if="couponData.useList.length==0 && couponData.stopList.length && noCouponShow">无可用
						</view>
					</view>
				</view>
				<text class="cuIcon-right" style="color: #e36452;margin-left: 15rpx;"></text>
			</view>
		</view>
		<u-popup v-model="isShowList" mode="bottom" border-radius="10" height="auto">
			<view class="pop-content">
				<view style="text-align: center;
    font-size: 18px;
    font-family: PingFangSC-Semibold;
    color: #333333;
    position: fixed;
    background-color: #fff;
    z-index: 9999999;
	top: 0;
    width: 100%;
	padding: 10px 0;
}">
					<text>优惠券</text>
				</view>
				<view v-if="couponData.stopList.length" style="margin-top: 40px;"></view>
				<view v-else style="padding-top: 40px;">
				</view>
				<v-tabs v-if="couponData.stopList.length" v-model="current" :scroll="false" :tabs="tabs" :fixed="true"
					@change="changeTab" activeColor="#e36452" lineColor="#e36452">
				</v-tabs>
				<scroll-view v-if="isShowScrollView" scroll-y="true" class="autoheight"
					:style="[{color:couponList.length},{height:couponList.length<3?(couponList.length * 110 + 91+ 'px'):'409px'},{padding:couponList.length==0?'10px 10px 0 10px':'10px 10px 40px 10px'}]">

					<nx-coupon :isShowCheck="current==0?isShowCheck:false" :status="couponStatus"
						@closeModel="chooseClicked" @showrule="showrule" v-for="(item, index) in couponList"
						:key="index" :item="item" :index="index" />
				</scroll-view>
				<view class="x-c autoheight" v-if="couponList.length == 0 && current==0">
					<u-empty text="暂无数据" icon-size="180"></u-empty>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	import {
		mapGetters
	} from "vuex"
	export default {
		name: "can-use-coupon",
		components: {},
		watch: {
			'couponList.length': {
				handler(newValue, oldValue) {
					if (newValue !== oldValue) {
						console.log('watch');
					}
				}
			}
		},
		props: {
			noCouponShow: {
				type: Boolean,
				default: false
			},
			isShowCheck: {
				type: Boolean,
				default: false
			},
			discounts: {
				type: Number,
				default: 0
			},
			couponData: {
				type: Object,
				default: {
					stopList: [],
					useList: []
				}
			}

		},
		mounted() {
			console.log('勾选的getCheckCoupluon', this.getCheckCoupluon)
		},
		computed: {
			...mapGetters(['getCheckCoupluon'])
		},
		data() {
			return {
				couponStatus: 0,
				tabs: ['可用', '不可用'],
				current: 0,
				couponList: this.couponData.useList,
				isShowScrollView: true,
				isShowList: false,
				itemData: {},
				couponShowRule: false
			};
		},
		methods: {
			setStopList(array) {
				console.log('array')
				this.couponData.setStopList = array;
			},
			setUseData(array) {
				this.couponList = array;
				console.log('setUseData this.couponList', this.couponList)
			},
			submitCoupon() {
				this.$emit('submitCoupon');
			},
			okClick() {
				this.isShowList = !this.isShowList;
				if (this.isShowCheck && this.couponData.useList.length) {
					this.submitCoupon()
				}
			},
			changeTab(index) {
				console.log('index', index)
				this.couponStatus = index;
				this.current = index;
				if (index == 0) {
					this.couponStatus = index;
					this.couponList = this.couponData.useList;
				} else {
					this.couponStatus = 3;
					this.couponList = this.couponData.stopList;

				}
				this.isShowScrollView = false;
				this.$nextTick(() => {
					this.isShowScrollView = true;
				})
				console.log('couponList.length', this.couponList.length);
			},
			showrule(item) {
				let that = this;
				console.log('item:', item);
				this.couponShowRule = true;
				this.itemData = item;
				if (that.$refs.couponRule) {
					that.$refs.couponRule.ruleShowOrClose();
				}
			},
			chooseClicked() {
				console.log('chooseClicked')
				this.isShowList = !this.isShowList;
				this.current = 1;
				this.isShowScrollView = false;
				this.$nextTick(() => {
					this.isShowScrollView = true;
					this.current = 0;
				})
			},
		}
	}
</script>

<style lang="scss">
	.pop-content {
		position: relative;
		background-color: #F7F7F7;
		height: 100%;

		.autoheight {
			min-height: 190px;
		}

		.okisee {
			background: linear-gradient(132deg, #e36452 0%, #e36452 100%);
			width: 100%;
			border-radius: 48px;
			text-align: center;
			color: #FFFFFF;
			font-size: 32rpx;
			font-weight: bold;
			line-height: 76rpx;
		}
	}
</style>

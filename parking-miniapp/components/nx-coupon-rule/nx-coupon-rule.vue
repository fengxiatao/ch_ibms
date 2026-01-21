<template>
	<view>
		<view class="cu-modal" style="z-index: 9999999999;" :class="newRuleShow?'show':''">
			<view class="cu-dialog">
				<view class="cu-bar bg-white act-title">
					<view class="content" style="font-size: 18px;
	color: #333333;">使用规则</view>
				</view>
				<view class="bg-white" style="padding:20px;text-align: left;">
					<view>
						<view style="font-size: 14px;margin-bottom: 4px;font-weight: bold;">
							{{itemData.description}}
						</view>
					</view>
				</view>

				<view class="bg-white" style="display: flex;justify-content: center;">
					<view class="okisee" @click='ruleShowOrClose'>我知道了</view>
				</view>

			</view>
		</view>


	</view>
</template>

<script>
	export default {
		name: 'sn-coupon-rule',
		props: {
			ruleShow: {
				type: Boolean,
				default: false
			},
			itemData: {
				type: Object,
				default: {}
			}

		},
		data() {
			return {
				newRuleShow: this.ruleShow
			}
		},
		methods: {
			ruleShowOrClose() {
				this.newRuleShow = !this.newRuleShow;
			}

		}
	}
</script>

<style lang="scss" scoped>
	.okisee {
		margin: 0 20px 20px 20px;
		background: linear-gradient(132deg, #e36452 0%, #e36452 100%);
		opacity: 1;
		width: 670rpx;
		border-radius: 48px;
		text-align: center;
		color: #FFFFFF;
		font-size: 32rpx;
		font-weight: bold;
		line-height: 76rpx;
	}
</style>

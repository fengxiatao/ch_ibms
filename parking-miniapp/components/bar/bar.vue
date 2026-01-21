<template>
	<view>
		<u-tabbar :list="tabBar" active-color="#ed6c60"></u-tabbar>
	</view>
</template>

<script>
	export default {
		props: {
			tabBar: {
				type: Array,
				required: true
			},
		},
		onLoad() {},
		methods: {}
	}
</script>

<style>
</style>

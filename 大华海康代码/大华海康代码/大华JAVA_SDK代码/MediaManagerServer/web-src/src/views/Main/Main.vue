<template>
	<div class="main">
		<div class="left">
			<router-view></router-view>
		</div>
		<div class="right">
			<a @click="toHome" href="javascript:void(0)">预置位</a>
			<a @click="toStorage" href="javascript:void(0)">云端录像</a>
			<a @click="toPtzControl" href="javascript:void(0)">云台控制</a>
			<a @click="toPlay" href="javascript:void(0)">新播放器</a>
			<a @click="toHCNetPtzControl" href="javascript:void(0)">海康云台控制</a>
			<a @click="toDHNetPtzControl" href="javascript:void(0)">大华云台控制</a>
		</div>
	</div>
</template>
<style lang='less' scoped src='./Main.less'></style>
<script src='./Main.js'></script>

<template>
	<div class="container">
		<div class="up">
			<easy-player :video-url="videoUrl" live="true" stretch="true" autoplay>
			</easy-player>
		</div>
		<div class="down">
			<button @click="toPreset('1')">左</button>
			<button @click="toPreset('2')">右</button>
			<button @click="toPreset('3')">上</button>
			<button @click="toPreset('4')">下</button>
			<button @click="toPreset('5')">左上</button>
			<button @click="toPreset('6')">左下</button>
			<button @click="toPreset('7')">右上</button>
			<button @click="toPreset('8')">右下</button>
			<button @click="toPreset('9')">放大</button>
			<button @click="toPreset('10')">缩小</button>
		</div>

	</div>
</template>
<style lang='less' scoped src='./HCNetPtzControl.less'></style>
<script src='./HCNetPtzControl.js'></script>

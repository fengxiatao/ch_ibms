<template>
	<div class="container">
		<a class="item_text" v-for="item in list" @click="toFile(item)">
			{{item.stream}}
		</a>
	</div>

</template>
<!-- <style lang='less' scoped src='./Storage.less'></style> -->
<script src='./Storage.js'></script>

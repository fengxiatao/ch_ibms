<template>
	<div class="container">
		<div class="up">
			<easy-player :video-url="videoUrl" live="true" stretch="true" autoplay>
			</easy-player>
		</div>
			<div class="down">
						<!-- <button @click="toPreset('1')">左</button> -->
						<el-button class="elbutton" @click="toPreset('1')">左</el-button>
						<el-button class="elbutton" @click="toPreset('2')">右</el-button>
						<el-button class="elbutton" @click="toPreset('3')">上</el-button>
						<el-button class="elbutton" @click="toPreset('4')">下</el-button>
						<el-button class="elbutton" @click="toPreset('5')">左上</el-button>
						<el-button class="elbutton" @click="toPreset('6')">左下</el-button>
						<el-button class="elbutton" @click="toPreset('7')">右上</el-button>
						<el-button class="elbutton" @click="toPreset('8')">右下</el-button>
						<el-button class="elbutton" @click="toPreset('9')">变倍+</el-button>
						<el-button class="elbutton" @click="toPreset('10')">变倍-</el-button>
<!-- 						<button @click="toPreset('11')">变焦+</button>
						<button @click="toPreset('12')">变焦-</button> -->
						<el-button class="elbutton"  @click="Speak('0')">开启通话</el-button>
						<el-button class="elbutton"  @click="Speak('1')">关闭通话</el-button>
						<el-button class="elbutton" @click="controllLight('0')">开启灯光</el-button>
						<el-button class="elbutton" @click="controllLight('1')">关闭灯光</el-button>
							
			</div>
			<div class="downBottom" style="margin-left: 0rem !important">
				<el-form :model="searchForm" :rules="rules" label-width="149rem" ref="searchForm" class="demo-ruleForm" style="margin-left: -10rem;">
					<div style="display: flex;justify-content: flex-start;">
						<div style="margin-top: 10rem;">
							<el-form-item class="short" label="预置点编号：" prop="keyword">
								<el-input v-model="searchForm.keyword" placeholder="请输入预置点编号" ></el-input>
							</el-form-item>
						</div>
						<el-button class="elbutton"  @click="YZDController(0)">设置预置点</el-button>
						
						<div style="margin-top: 10rem;">
							<el-select @change="searchYZDState()" v-model="yzdState" placeholder="预置点">
							  <el-option
							      v-for="item in allYzd"
							      :key="item.id"
							      :label="item.name"
							      :value="item.id">
							  </el-option>
							</el-select>
						</div>
						
						<div>
							<el-button class="elbutton" @click="YZDController(1)">移动到预置点</el-button>
						</div>
						<div>
							<el-button class="elbutton" @click="YZDController(2)">删除预置点</el-button>
						</div>
						
						<div style="margin-top: 10rem;">
							<el-select @change="searchListFileState"  v-model="fileName" placeholder="报警音频">
							  <el-option
							      v-for="item in allFile"
							      :key="item.id"
							      :label="item.name"
							      :value="item.id">
							  </el-option>
							</el-select>
						</div>
						
						<div>
							<el-button class="elbutton" @click="SpeakData('0')">开启语音转发</el-button>
						</div>
						
						<div>
							<el-button class="elbutton" @click="SpeakData('1')">关闭语音转发</el-button>
						</div>
						
						<div>
							<div style="margin-top: 10rem;">
								<el-form-item class="short" label="修改预置点名称：" prop="keywordName">
									<el-input v-model="searchForm.keywordName" placeholder="请输入预置点名称" ></el-input>
								</el-form-item>
							</div>
						</div>
						
						<div>
							<el-button class="elbutton"  @click="UpdateYZDName()">修改名称</el-button>
						</div>
						
					</div>
				</el-form>
				
			</div>
		
	</div>
</template>
<style lang='less' scoped src='./DHNetPtzControl.less'></style>
<script src='./DHNetPtzControl.js'></script>

import apis from '@/common/Apis'
export default {
    name: 'HCNetPtzControl',
    components:{

    },
    data() {
        return {
            videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv",
			yzdState:"",
			allYzd:[],
			searchForm: {
			    keyword:" ",
				keywordName:"",
			},
			rules: {
			    keyword: [{required: true,  message: '请输入预置点编号',  trigger: 'blur'}], 
			},
        }
    },
    mounted(){
           this.getYZDList();
		   this.getToolPoint();
    },
    methods: {		
		getToolPoint:function(){
			let that=this;
			that.$http.post("/HCNet/getToolPoint",{
				UserName:"admin",
				Password:"CDtt@123",
			}).then(res=>{
			    console.log(JSON.stringify(res))
			})
		},
		getYZDList:function(){
			let that=this;
			that.$http.post("/HCNet/GetDVRConfig",{
				UserName:"admin",
				Password:"CDtt@123",
			}).then(res=>{
				that.allYzd=[];
				that.yzdState="";
			    console.log(JSON.stringify(res))
				let result=res.data.data;//预置点集合
				for (var i = 0; i < result.length; i++) {
					that.allYzd.push({
						id:result[i].id,
						name:result[i].presetName
					})
				}
			})
		},
		controllLight:function(type){
			let that=this;
			that.$http.post("/HCNet/setAlarmLight",{
				UserName:"admin",
				Password:"CDtt@123",
				type:type
			}).then(res=>{
			    console.log(JSON.stringify(res))
			})
		},
		UpdateYZDName:function(){
			let that=this;
	
			let yzdIDs=that.yzdState;
			let name=that.searchForm.keywordName;
			that.$http.post("/HCNet/UpdateYZDName",{
				UserName:"admin",
				Password:"CDtt@123",
				yzdIDs:yzdIDs,
				name:name
			}).then(res=>{
			    console.log(JSON.stringify(res))
				if(res.data.code==0){
					this.$message.success("设置预置点名称成功")
					that.getYZDList();
				}	
			})
		},
		YZDController:function(comm){
			let that=this;
			var command=comm;
			var yzdIDs="";
			if(command==0){
				command="8"
				yzdIDs=that.searchForm.keyword
			}
			else if(command==1){
				command="39"
				yzdIDs=that.yzdState
			}
			else if(command==2){
				command="9"
				yzdIDs=that.yzdState
			}
			that.$http.post("/HCNet/moveYZD",{
				UserName:"admin",
				Password:"CDtt@123",
				Command:command,
				yzdID:yzdIDs
			}).then(res=>{
			    console.log(JSON.stringify(res))
				if(command=="8"&&res.data.code==0){
					this.$message.success("设置预置点成功")
					that.getYZDList();
				}
				else if(command=="9"&&res.data.code==0){
					this.$message.success("删除预置点成功")
					that.getYZDList();
				}
				else if(command=="39"&&res.data.code==0){
					this.$message.success("移动预置点成功")
					this.getToolPoint();
				}
				
			})
		},
		Speak:function(types){
			let that=this;
			that.$http.post("/HCNet/openOrCloseSpeek",{
				UserName:"admin",
				Password:"CDtt@123",
				type:types,
			}).then(res=>{
				if(types=="0"&&res.data.code==0){
					this.$message.success("开启语音通话成功");
				}
				else if(types=="1"&&res.data.code==0){
					this.$message.success("关闭语音通话成功");
				}
			    console.log("sssssssssss"+JSON.stringify(res))
			})
		},
		SpeakData:function(types){
			let that=this;
			that.$http.post("/HCNet/StartSpeekData",{
				UserName:"admin",
				Password:"CDtt@123",
			}).then(res=>{
				if(res.data.code==0){
					this.$message.success("语音转发成功");
				}
			    console.log("sssssssssss"+JSON.stringify(res))
			})
		},
        toPreset(index){
			let command=""
			switch(index){
				case "1":
				command="23"
				break;
				case "2":
				command="24"
				break;
				case "3":
				command="21"
				break;
				case "4":
				command="22"
				break;
				case "5":
				command="25"
				break;
				case "6":
				command="27"
				break;
				case "7":
				command="26"
				break;
				case "8":
				command="28"
				break;
				case "9":
				command="11"
				break;
				case "10":
				command="12"
				break;
			}
            this.$http.post("/HCNet/panTiltControl",{
				UserName:"admin",
				Password:"CDtt@123",
				Command:command,
            }).then(res=>{
                console.log(JSON.stringify(res))
            })
        }
    }
}
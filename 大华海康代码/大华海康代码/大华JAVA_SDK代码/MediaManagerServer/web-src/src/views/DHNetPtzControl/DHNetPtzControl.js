import apis from '@/common/Apis'
export default {
    name: 'DHNetPtzControl',
    components:{

    },
    data() {
        return {
            videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv",
			yzdState:"",
			allYzd:[],
			allFile:[],
			fileName:"",
			FileUrls:"",
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
		this.getFileList();
		this.getToolPoint();
    },
    methods: {
		getToolPoint:function(){
			let that=this;
			that.$http.post("/DHNetController/getToolPoint",{
				UserName:"admin",
				Password:"jokeep2012",
			}).then(res=>{
			    console.log(JSON.stringify(res))
			})
		},
		searchListFileState(ev){
			let that=this;
			console.log(ev)
			that.FileUrls=ev;
		},
		SpeakData:function(types){
			let that=this;
			that.$http.post("/DHNetController/StartSpeekData",{
				UserName:"admin",
				Password:"jokeep2012",
				types:types,
				F_FileUrl:that.FileUrls
			}).then(res=>{
				if(res.data.code==0){
					this.$message.success("语音转发成功");
				}
			    console.log("sssssssssss"+JSON.stringify(res))
			})
		},
		getYZDList:function(){
			let that=this;
			that.$http.post("/DHNetController/getYZDList",{
				UserName:"admin",
				Password:"jokeep2012",
			}).then(res=>{
				that.allYzd=[];
				that.yzdState="";
			    console.log(JSON.stringify(res))
				let result=res.data.data.ListYZD;//预置点集合
				for (var i = 0; i < result.length; i++) {
					that.allYzd.push({
						id:result[i].YZDID,
						name:result[i].Name
					})
				}
			})
		},
		UpdateYZDName:function(){
			let that=this;
			let yzdIDs=that.yzdState;
			let name=that.searchForm.keywordName;
			that.$http.post("/DHNetController/UpdateYZDName",{
				UserName:"admin",
				Password:"jokeep2012",
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
		getFileList:function(){
			let that=this;
			that.$http.post("/DHNetController/getFileList",{
				UserName:"admin",
				Password:"jokeep2012",
			}).then(res=>{
				that.allFile=[];
				that.fileName="";
			    console.log(JSON.stringify(res))
				let result=res.data.data.LisFile;
				
				for (var i = 0; i < result.length; i++) {
					that.allFile.push({
						id:result[i].FilePath,
						name:result[i].FileName
					})
				}
				console.log(that.allFile)
			})
		},
		controllLight:function(type){
			let that=this;
			that.$http.post("/DHNetController/setAlarmLight",{
				UserName:"admin",
				Password:"jokeep2012",
				type:type
			}).then(res=>{
			    console.log(JSON.stringify(res))
			})
		},
		YZDController:function(comm){
			let that=this;
			var command=comm;
			var yzdIDs="";
			if(command==0){
				command="YZD_SET"
				yzdIDs=that.searchForm.keyword
			}
			else if(command==1){
				command="YZD_MOVE"
				yzdIDs=that.yzdState
			}
			else if(command==2){
				command="YZD_DEL"
				yzdIDs=that.yzdState
			}
			that.$http.post("/DHNetController/panTiltControl",{
				UserName:"admin",
				Password:"jokeep2012",
				Command:command,
				type:"2",
				yzdID:yzdIDs
			}).then(res=>{
			    console.log(JSON.stringify(res))
				if(command=="YZD_SET"){
					this.$message.success("设置预置点成功")
					that.getYZDList();
				}
				else if(command=="YZD_DEL"){
					this.$message.success("删除预置点成功")
					that.getYZDList();
				}
				else if(command=="YZD_MOVE"){
					this.$message.success("移动预置点成功")
				    that.getToolPoint();
				}
			})
		},
		Speak:function(types){
			let that=this;
			that.$http.post("/DHNetController/openOrCloseSpeek",{
				UserName:"admin",
				Password:"jokeep2012",
				type:types,
			}).then(res=>{
				debugger
			    console.log(JSON.stringify(res))
			})
		},
        toPreset(index){
			let that=this;
			let command=""
			switch(index){
				case "1":
				command="LEFT"
				break;
				case "2":
				command="RIGHT"
				break;
				case "3":
				command="UP"
				break;
				case "4":
				command="DOWN"
				break;
				case "5":
				command="LEFT_UP"
				break;
				case "6":
				command="LEFT_DOWN"
				break;
				case "7":
				command="RIGHT_UP"
				break;
				case "8":
				command="RIGHT_DOWN"
				break;
				case "9":
				command="ZOOM_IN"
				break;
				case "10":
				command="ZOOM_OUT"
				break;
				case "11":
				command="FOCUS_NEAR"
				break;
				case "12":
				command="FOCUS_FAR"
				break;
			}
            this.$http.post("/DHNetController/panTiltControl",{
				UserName:"admin",
				Password:"jokeep2012",
				Command:command,
				type:"1",
				yzdID:""
            }).then(res=>{
                console.log(JSON.stringify(res))
				debugger
				that.getToolPoint();
            })
        }
    }
}
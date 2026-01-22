import apis from '@/common/Apis'
export default {
    name: 'PtzControl',
    components:{

    },
    data() {
        return {
            videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv",
			presetIndex: 1,
			channelId:"34",
			deviceId:"340000",
        }
    },
    mounted(){

    },
    methods: {
        toPreset(index){
			let command=""
			switch(index){
				case "1":
				command="left"
				break;
				case "2":
				command="right"
				break;
				case "3":
				command="up"
				break;
				case "4":
				command="down"
				break;
				case "5":
				command="upleft"
				break;
				case "6":
				command="downleft"
				break;
				case "7":
				command="upright"
				break;
				case "8":
				command="downright"
				break;
				case "9":
				command="zoomin"
				break;
				case "10":
				command="zoomout"
				break;
				case "11":
				command="stop"
				break;
			}
            this.$http.post(apis.PTZ_CONTROL,{
				channelId: this.channelId,
				deviceId: this.deviceId,
				command:command,
            }).then(res=>{
                console.log(JSON.stringify(res))
            })
        },
		setPreset(presetIndex){
			this.$http.post(apis.FRONT_END_COMMAND,{
				channelId: this.channelId,
				deviceId: this.deviceId,
				parameter2:this.presetIndex,
				parameter1:0,
				cmdCode:129,
				combindCode2:0
			}).then(res=>{
				console.log((JSON.stringify(res)))
			})
		},
		delPreset(presetIndex){
			this.$http.post(apis.FRONT_END_COMMAND,{
				channelId: this.channelId,
				deviceId: this.deviceId,
				parameter2:this.presetIndex,
				parameter1:0,
				cmdCode:131,
				combindCode2:0
			}).then(res=>{
				console.log((JSON.stringify(res)))
			})
		}
    }
}
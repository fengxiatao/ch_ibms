import apis from '@/common/Apis'
export default {
    name: 'Home',
    components:{

    },
    data() {
        return {
            videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv",
            channelId:'34',
            deviceId:"340000",
            presets:[]
        }
    },
    mounted(){
        this.loadPreset()
    },
    methods: {
        toPreset(index){
            this.$http.post(apis.HOME_POSITION,{
                channelId:this.channelId,
                deviceId:this.deviceId,
                enabled:"1",
                presetIndex:index,
                resetTime:"2022-08-01 00:00:00"
            }).then(res=>{
                console.log(JSON.stringify(res))
            })
        },
        loadPreset(){
            let _this=this;
            this.$http.post(apis.PREST_QUERY,{
                channelId:this.channelId,
                deviceId:this.deviceId
            }).then(res=>{
                if(res.data.code==0){
                    _this.presets=res.data.data
                }
            })
        }
    }
}
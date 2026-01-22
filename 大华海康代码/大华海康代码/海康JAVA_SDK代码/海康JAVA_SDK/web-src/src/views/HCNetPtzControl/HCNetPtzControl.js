import apis from '@/common/Apis'
export default {
    name: 'HCNetPtzControl',
    components:{

    },
    data() {
        return {
            videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv"
        }
    },
    mounted(){

    },
    methods: {
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
				Password:"jokeep2012",
				Command:command,
            }).then(res=>{
                console.log(JSON.stringify(res))
            })
        }
    }
}
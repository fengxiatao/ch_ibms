import apis from '@/common/Apis'

export default {
	name: 'Storage',
	components: {

	},
	data() {
		return {
			videoUrl: "http://47.104.97.152:8090/rtp/340000_34.live.flv",
			list:[]
		}
	},
	mounted() {
		this.loadData()
	},

	methods: {
		loadData() {
			let _this=this
			this.$http.post(apis.RECORD_LIST, {
				page: 1,
				count: 10,
			}).then(res => {
				if (res.data.code == 0) {
					_this.list = res.data.data.data.list
					console.log('sdssss', _this.list)
				}

			})
		},
		toFile(res) {
			// this.$router.push("/main/home");
			console.log("1111",res)
			let _this=this
			this.$http.post(apis.RECORD_DATE_LIST, {
				app: res.app,
				stream: res.stream,
			}).then(res => {
				if (res.data.code == 0) {
					console.log('sdssss', res)
				}
			
			})
		},
	}
}

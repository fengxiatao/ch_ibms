export default {
	name: 'Main',
	components: {

	},
	data() {
		return {

		}
	},
	mounted() {

	},
	methods: {
		toHome() {
			this.$router.push("/main/home");
		},
		toStorage() {
			this.$router.push("/main/storage");
		},
		toPtzControl() {
			this.$router.push("/main/ptzControl");
		},
		toPlay(){
				this.$router.push("/main/live");
		},
		toHCNetPtzControl() {
			this.$router.push("/main/HCNetptzControl");

		}
	}
}

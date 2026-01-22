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
		toHCNetPtzControl() {
			this.$router.push("/main/HCNetptzControl");
		},
		toDHNetPtzControl() {
			this.$router.push("/main/DHNetptzControl");
		}
	},
	watch: {
	    '$route' (to, from) {
	        this.$router.go(0);
	    }
	},
}

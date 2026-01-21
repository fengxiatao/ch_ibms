export default rules = {
	// 姓名
	name: [
			// 对name字段进行长度验证
			{
				min: 5,
				message: '简介不能少于5个字',
				trigger: 'change'
			},
			// 对name字段进行必填验证
			{
				required: true,
				message: '请填写姓名',
				trigger: ['change','blur']
			},
		],
	//手机号码
	mobile: [
		{
			required: true, 
			message: '请输入手机号',
			trigger: ['change','blur'],
		},
		{
			// 自定义验证函数，见上说明
			validator: (rule, value, callback) => {
				// 上面有说，返回true表示校验通过，返回false表示不通过
				// this.$u.test.mobile()就是返回true或者false的
				return this.$u.test.mobile(value);
			},
			message: '手机号码不正确',
			// 触发器可以同时用blur和change
			trigger: ['change','blur'],
		}
	]
	
}

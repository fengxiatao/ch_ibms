function uniSwitchTab(url) {
	uni.switchTab({
		url: url
	});
}

function getFullYear() {
	let arr = []
	let start = 1970;
	let end = new Date().getFullYear();
	for (let i = start; i <= end; i++) {
		arr.unshift({
			label: i,
			value: i
		})
	}
	return arr
}

function getDay(day) {

	var today = new Date();

	var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;

	today.setTime(targetday_milliseconds); //注意，这行是关键代码

	var tYear = today.getFullYear();

	var tMonth = today.getMonth();

	var tDate = today.getDate();

	tMonth = doHandleMonth(tMonth + 1);

	tDate = doHandleMonth(tDate);

	return tYear + "-" + tMonth + "-" + tDate;

}

function doHandleMonth(month) {

	var m = month;

	if (month.toString().length == 1) {

		m = "0" + month;

	}

	return m;

}

// 将数字转成汉字格式
function formateDay(value) {
	switch (value) {
		case 1:
			return "周一";
		case 2:
			return "周二";
		case 3:
			return "周三";
		case 4:
			return "周四";
		case 5:
			return "周五";
		case 6:
			return "周六";
		case 0:
			return "周日";
		default:
			return null;
	}
}
// 格式化分钟为xx小时xx分钟
function formatMinutes(value) {

	let hours = Math.floor(value / 60)
	let minutes = value % 60

	return hours + '小时' + minutes + '分钟'
}

//格式化时间
function format(dat) {
	//获取年月日，时间
	var year = dat.getFullYear();
	var mon = (dat.getMonth() + 1) < 10 ? "0" + (dat.getMonth() + 1) : dat.getMonth() + 1;
	var data = dat.getDate() < 10 ? "0" + (dat.getDate()) : dat.getDate();
	var hour = dat.getHours() < 10 ? "0" + (dat.getHours()) : dat.getHours();
	var min = dat.getMinutes() < 10 ? "0" + (dat.getMinutes()) : dat.getMinutes();
	var seon = dat.getSeconds() < 10 ? "0" + (dat.getSeconds()) : dat.getSeconds();

	var newDate = year + "-" + mon + "-" + data + " " + hour + ":" + min + ":" + seon;
	return newDate;
}

function formatDate(dat) {
	//获取年月日
	var year = dat.getFullYear();
	var mon = (dat.getMonth() + 1) < 10 ? "0" + (dat.getMonth() + 1) : dat.getMonth() + 1;
	var data = dat.getDate() < 10 ? "0" + (dat.getDate()) : dat.getDate();
	var newDate = year + "-" + mon + "-" + data + " ";
	return newDate;
}
// 校验车牌号
function testCarNumber(value) {
	return /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$/.test(
		value
	)
}
export {
	uniSwitchTab,
	getFullYear,
	getDay,
	formateDay,
	formatMinutes,
	format,
	formatDate,
	testCarNumber
}

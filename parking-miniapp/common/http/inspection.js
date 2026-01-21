import store from "@/store/index.js"

let {apiPatrol} = store.state.apiHeader;

export default {
	// 小程序用户查询巡更记录
	findTbPatrolRecordByUserId:apiPatrol+"/patrolRecord/findTbPatrolRecordByUserId",
	// 小程序用户统计一个月每天巡查状态
	calendarDayToMonth:apiPatrol+"/patrolRecord/calendarDayToMonth",
	// 查询巡更记录任务详情
	patrolRecordDotSelect:apiPatrol+"/patrolRecordDot/select",
	// 小程序用户修改巡更记录（接单--1、完成--2）
	updateTbPatrolRecordByUserId:apiPatrol+"/patrolRecord/updateTbPatrolRecordByUserId",
	// 小程序查询计划中所有巡更人,转单时查询
	findUserByPlanId:apiPatrol+"/patrolRecord/findUserByPlanId",
	// 修改巡更记录任务详情（已巡更）
	patrolRecordDotUpdate:apiPatrol+"/patrolRecordDot/update",
	// 小程序用户转单
	transferTbPatrolRecordByUserId:apiPatrol+"/patrolRecord/transferTbPatrolRecordByUserId",
	// 小程序用户修改转单（同意或不同意）
	updateTransferByUserId:apiPatrol+"/patrolRecord/updateTransferByUserId",
}

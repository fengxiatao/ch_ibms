package com.jokeep.framework.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

public class BaseEntity implements Serializable {
    @ApiModelProperty("是否删除（0=否;1=是）")
    @TableField("F_IsDeleted")
    @TableLogic
    private Integer isDeleted;

    @ApiModelProperty("建立人ID")
    @TableField("F_CreateUserID")
    private String createUserID;

    @ApiModelProperty("建立人姓名")
    @TableField("F_CreateUserName")
    private String createUserName;

    @ApiModelProperty("建立人部门ID")
    @TableField("F_CreateDeptID")
    private String createDeptID;

    @ApiModelProperty("建立人部门名称")
    @TableField("F_CreateDeptName")
    private String createDeptName;

    @ApiModelProperty("建立人单位ID")
    @TableField("F_CreateUnitID")
    private String createUnitID;

    @ApiModelProperty("建立人单位名称")
    @TableField("F_CreateUintName")
    private String createUintName;

    @ApiModelProperty("建立时间")
    @TableField("F_CreateTime")
    private Date createTime;

    @ApiModelProperty("最后更新人ID")
    @TableField("F_UpdateUserID")
    private String updateUserID;

    @ApiModelProperty("最后更新姓名")
    @TableField("F_UpdateUserName")
    private String updateUserName;

    @ApiModelProperty("最后更新部门ID")
    @TableField("F_UpdateDeptID")
    private String updateDeptID;

    @ApiModelProperty("最后更新部门名称")
    @TableField("F_UpdateDeptName")
    private String updateDeptName;

    @ApiModelProperty("最后更新单位ID")
    @TableField("F_UpdateUnitID")
    private String updateUnitID;

    @ApiModelProperty("最后更新单位名称")
    @TableField("F_UpdateUnitName")
    private String updateUnitName;

    @ApiModelProperty("最后更新人时间")
    @TableField("F_UpdateTime")
    private Date updateTime;

    @ApiModelProperty("删除人ID")
    @TableField("F_DeleteUserID")
    private String deleteUserID;

    @ApiModelProperty("删除人部门ID")
    @TableField("F_DeleteDeptID")
    private String deleteDeptID;

    @ApiModelProperty("删除人单位ID")
    @TableField("F_DeleteUnitID")
    private String deleteUnitID;

    @ApiModelProperty("删除时间")
    @TableField("F_DeleteTime")
    private Date deleteTime;
}

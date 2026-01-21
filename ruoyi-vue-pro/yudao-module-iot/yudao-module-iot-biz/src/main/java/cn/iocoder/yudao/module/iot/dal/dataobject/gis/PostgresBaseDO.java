package cn.iocoder.yudao.module.iot.dal.dataobject.gis;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PostgreSQL 数据库专用基础实体对象
 * 
 * 与标准 BaseDO 的区别：
 * 1. deleted 字段使用 BooleanToIntTypeHandler，适配 PostgreSQL 的 SMALLINT 类型
 * 2. 用于 GIS 相关的实体（floor, building, campus 等）
 *
 * @author IBMS Team
 */
@Data
@JsonIgnoreProperties(value = "transMap")
public abstract class PostgresBaseDO implements Serializable, TransPojo {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;
    
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;
    
    /**
     * 是否删除
     * 
     * PostgreSQL 数据库中使用 SMALLINT 类型存储（0=未删除, 1=已删除）
     * MyBatis 会自动将 Boolean 映射到 SMALLINT
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 把 creator、createTime、updateTime、updater 都清空，避免前端直接传递 creator 之类的字段，直接就被更新了
     */
    public void clean(){
        this.creator = null;
        this.createTime = null;
        this.updater = null;
        this.updateTime = null;
    }

}


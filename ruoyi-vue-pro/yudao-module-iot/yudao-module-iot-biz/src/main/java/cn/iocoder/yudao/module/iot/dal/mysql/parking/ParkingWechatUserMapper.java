package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingWechatUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车场微信用户 Mapper
 *
 * @author changhui
 */
@Mapper
public interface ParkingWechatUserMapper extends BaseMapperX<ParkingWechatUserDO> {

    /**
     * 根据OpenID查询用户
     *
     * @param openid 微信OpenID
     * @return 用户信息
     */
    default ParkingWechatUserDO selectByOpenid(String openid) {
        return selectOne(ParkingWechatUserDO::getOpenid, openid);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    default ParkingWechatUserDO selectByUsername(String username) {
        return selectOne(ParkingWechatUserDO::getUsername, username);
    }

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    default ParkingWechatUserDO selectByMobile(String mobile) {
        return selectOne(ParkingWechatUserDO::getMobile, mobile);
    }
}

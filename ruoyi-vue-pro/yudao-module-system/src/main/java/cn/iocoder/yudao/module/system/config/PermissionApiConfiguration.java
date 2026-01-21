package cn.iocoder.yudao.module.system.config;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * 权限 API 配置类
 * 
 * 解决 PermissionCommonApi Bean 注入问题
 *
 * @author 长辉信息科技有限公司
 */
@Configuration
public class PermissionApiConfiguration {

    /**
     * 将 PermissionApi 也注册为 PermissionCommonApi 类型的 Bean
     * 因为 PermissionApi 继承了 PermissionCommonApi
     * 
     * 使用方法参数注入并添加 @Lazy 避免循环依赖
     */
    @Bean
    @ConditionalOnBean(PermissionApi.class)
    @Primary
    public PermissionCommonApi permissionCommonApi(@Lazy PermissionApi permissionApi) {
        return permissionApi;
    }

    @Bean
    @ConditionalOnMissingBean(PermissionCommonApi.class)
    public PermissionCommonApi fallbackPermissionCommonApi() {
        return new PermissionCommonApi() {
            @Override
            public boolean hasAnyPermissions(Long userId, String... permissions) {
                return false;
            }
            @Override
            public boolean hasAnyRoles(Long userId, String... roles) {
                return false;
            }
            @Override
            public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
                return new DeptDataPermissionRespDTO();
            }
        };
    }

}

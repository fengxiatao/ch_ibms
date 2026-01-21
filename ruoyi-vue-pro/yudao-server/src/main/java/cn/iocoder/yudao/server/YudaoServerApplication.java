package cn.iocoder.yudao.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
 *
 * @author 长辉信息科技有限公司
 */
@SpringBootApplication(
    scanBasePackages = {"cn.iocoder.yudao.server", "cn.iocoder.yudao.module"},
    exclude = {
        org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        // 排除默认的 UserDetailsService 配置
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
    }
)
public class YudaoServerApplication {

    public static void main(String[] args) {
        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章

        // 明确指定为 SERVLET 应用，避免 IDEA 调试时尝试加载 WebFlux
        SpringApplication app = new SpringApplication(YudaoServerApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);

        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.changhui-tech.com/quick-start/ 文章
    }

}

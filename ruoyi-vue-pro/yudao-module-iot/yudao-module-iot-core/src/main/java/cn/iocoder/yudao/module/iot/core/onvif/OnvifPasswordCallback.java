package cn.iocoder.yudao.module.iot.core.onvif;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * ONVIF WS-Security 密码回调处理器
 * 
 * @author 长辉信息科技有限公司
 */
public class OnvifPasswordCallback implements CallbackHandler {

    private final String username;
    private final String password;

    public OnvifPasswordCallback(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                if (username.equals(pc.getIdentifier())) {
                    pc.setPassword(password);
                }
            }
        }
    }
}

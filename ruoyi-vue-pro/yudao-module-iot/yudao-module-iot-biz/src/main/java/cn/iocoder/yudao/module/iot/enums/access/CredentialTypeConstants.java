package cn.iocoder.yudao.module.iot.enums.access;

/**
 * 凭证类型常量
 * <p>
 * 所有凭证类型比较应使用此类中的常量和方法，以确保大小写不敏感的比较。
 * </p>
 * 
 * @author Kiro
 */
public final class CredentialTypeConstants {

    /** 人脸凭证 */
    public static final String FACE = "FACE";

    /** 卡片凭证 */
    public static final String CARD = "CARD";

    /** 密码凭证 */
    public static final String PASSWORD = "PASSWORD";

    /** 指纹凭证 */
    public static final String FINGERPRINT = "FINGERPRINT";

    private CredentialTypeConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 判断是否为人脸凭证类型（大小写不敏感）
     *
     * @param type 凭证类型字符串
     * @return 如果是人脸凭证类型返回 true，否则返回 false
     */
    public static boolean isFace(String type) {
        return FACE.equalsIgnoreCase(type);
    }

    /**
     * 判断是否为卡片凭证类型（大小写不敏感）
     *
     * @param type 凭证类型字符串
     * @return 如果是卡片凭证类型返回 true，否则返回 false
     */
    public static boolean isCard(String type) {
        return CARD.equalsIgnoreCase(type);
    }

    /**
     * 判断是否为密码凭证类型（大小写不敏感）
     *
     * @param type 凭证类型字符串
     * @return 如果是密码凭证类型返回 true，否则返回 false
     */
    public static boolean isPassword(String type) {
        return PASSWORD.equalsIgnoreCase(type);
    }

    /**
     * 判断是否为指纹凭证类型（大小写不敏感）
     *
     * @param type 凭证类型字符串
     * @return 如果是指纹凭证类型返回 true，否则返回 false
     */
    public static boolean isFingerprint(String type) {
        return FINGERPRINT.equalsIgnoreCase(type);
    }

    /**
     * 标准化凭证类型为大写
     *
     * @param type 凭证类型字符串
     * @return 标准化后的大写凭证类型，如果输入为 null 则返回 null
     */
    public static String normalize(String type) {
        return type != null ? type.toUpperCase() : null;
    }
}

package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

/**
 * TEA (Tiny Encryption Algorithm) 加密解密工具类
 * 
 * <p>TEA 是一种简单高效的分组加密算法，使用 128 位密钥对 64 位数据块进行加密。</p>
 * 
 * @author IoT Gateway Team
 */
public class TeaCrypto {

    /**
     * TEA 加密轮数
     */
    private static final int ROUNDS = 32;

    /**
     * TEA 常量 delta
     */
    private static final int DELTA = 0x9E3779B9;

    /**
     * TEA 加密
     *
     * @param data 明文数据
     * @param key  TEA 密钥（4 个 32 位整数）
     * @return 密文数据
     */
    public static byte[] encrypt(byte[] data, int[] key) {
        if (data == null || key == null || key.length != 4) {
            return null;
        }

        // 填充数据到 8 字节的倍数
        int paddedLength = ((data.length + 7) / 8) * 8;
        if (paddedLength == 0) {
            paddedLength = 8;
        }
        byte[] padded = new byte[paddedLength];
        System.arraycopy(data, 0, padded, 0, data.length);

        // 加密每个 8 字节块
        byte[] result = new byte[paddedLength];
        for (int i = 0; i < paddedLength; i += 8) {
            int v0 = bytesToInt(padded, i);
            int v1 = bytesToInt(padded, i + 4);

            int sum = 0;
            for (int j = 0; j < ROUNDS; j++) {
                sum += DELTA;
                v0 += ((v1 << 4) + key[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + key[1]);
                v1 += ((v0 << 4) + key[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + key[3]);
            }

            intToBytes(v0, result, i);
            intToBytes(v1, result, i + 4);
        }

        return result;
    }

    /**
     * TEA 解密
     *
     * @param data 密文数据
     * @param key  TEA 密钥（4 个 32 位整数）
     * @return 明文数据
     */
    public static byte[] decrypt(byte[] data, int[] key) {
        if (data == null || key == null || key.length != 4 || data.length % 8 != 0) {
            return null;
        }

        byte[] result = new byte[data.length];

        // 解密每个 8 字节块
        for (int i = 0; i < data.length; i += 8) {
            int v0 = bytesToInt(data, i);
            int v1 = bytesToInt(data, i + 4);

            int sum = DELTA * ROUNDS;
            for (int j = 0; j < ROUNDS; j++) {
                v1 -= ((v0 << 4) + key[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + key[3]);
                v0 -= ((v1 << 4) + key[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + key[1]);
                sum -= DELTA;
            }

            intToBytes(v0, result, i);
            intToBytes(v1, result, i + 4);
        }

        return result;
    }

    /**
     * 字节数组转整数（大端序）
     */
    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    /**
     * 整数转字节数组（大端序）
     */
    private static void intToBytes(int value, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((value >>> 24) & 0xFF);
        bytes[offset + 1] = (byte) ((value >>> 16) & 0xFF);
        bytes[offset + 2] = (byte) ((value >>> 8) & 0xFF);
        bytes[offset + 3] = (byte) (value & 0xFF);
    }
}

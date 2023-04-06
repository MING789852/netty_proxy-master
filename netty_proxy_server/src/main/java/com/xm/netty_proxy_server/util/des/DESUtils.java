package com.xm.netty_proxy_server.util.des;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

/**
 * SRM邮箱密码
 */
public class DESUtils {

    /**
     * 加密/解密算法-工作模式-填充模式
     */
    private static final String CIPHER_ALGORITHM = "DES";


    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "DES";

    /**
     * 默认编码
     */
    private static final String CHARSET = "utf-8";


    /**
     * 生成key
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static Key generateKey(String key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key.getBytes(CHARSET));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(dks);
    }

    /**
     * 解密
     *
     * @param inStr     需要解密的内容
     * @param secretKey 密钥
     * @return 解密后的数据
     */

    public static String decrypt(String inStr, String secretKey) {
        Key deskey;

        Cipher cipher;
        String outStr = null;

        try {
            deskey = generateKey(secretKey);
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            outStr = new String(cipher.doFinal(hex2byte(inStr)));
        } catch (Exception e) {
            System.err.println("DES解密异常" + e.getMessage());
        }
//        System.out.println("DES解密后数据：" + outStr);

        return outStr;

    }

    /**
     * 十六进转二进制
     *
     * @param hexStr 待转换16进制字符串
     * @return 二进制字节组
     */
    public static byte[] hex2byte(String hexStr) {

        if (hexStr == null)

            return null;

        hexStr = hexStr.trim();

        int len = hexStr.length();

        if (len == 0 || len % 2 == 1)

            return null;

        byte[] digest = new byte[len / 2];

        try {

            for (int i = 0; i < hexStr.length(); i += 2) {

                digest[i / 2] = (byte) Integer.decode("0x" + hexStr.substring(i, i + 2)).intValue();

            }
            return digest;

        } catch (Exception e) {

            return null;

        }

    }

    public static void main(String[] args) {
        System.out.println(DESUtils.decrypt("bffc1b63bae708a9bc20f32dce1f9442","tissonco"));
    }
}

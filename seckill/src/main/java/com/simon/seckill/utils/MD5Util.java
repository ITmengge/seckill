package com.simon.seckill.utils;


import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5加密（随便写的盐）
 */
public class MD5Util {
    /**
     * 进行两次MD5加密：（都是为了保证安全，MD5不是很安全，加盐salt更加安全）
     * 用户注册时，系统用来和用户密码进行组合而生成的随机数值，称作salt值，通称为加盐值。
     * 第一次MD5加密是在客户端输入用户名密码之后传到后端
     * 第二次MD5加密是在将第一次MD5加密后的密码存到数据库中
     */

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    // 准备盐
    private static final String salt="1a2b3c4d";

    /**
     * 第一次加密（前端传到后端）
     */
    public static String inputPassToFromPass(String inputPass){
        String str = ""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 第二次加密（存到数据库中）
     */
    public static String formPassToDBPass(String formPass,String salt){
        String str = ""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     *  真正存到数据库的密码（进行了两次MD5加密）
     */
    public static String inputPassToDBPass(String inputPass,String salt){
        String fromPass = inputPassToFromPass(inputPass);
        String dbPass = formPassToDBPass(fromPass, salt);
        return dbPass;
    }

    // 测试
    public static void main(String[] args) {
        // d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToFromPass("123456"));
        // 测试加密是否成功
        System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }
}

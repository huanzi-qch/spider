package cn.huanzi.qch.commonspider.util;

import org.apache.commons.lang.RandomStringUtils;

/**
 * 随机数工具类
 */
public class RandomUtil {

    /**
     * 返回一个最小值-最大值的随机数
     */
    public static Integer randomNumber(Integer min, Integer max) {
        //(最小值+Math.randomNumber()*(最大值-最小值+1))
        return (int) (min + Math.random() * (max - min + 1));
    }

    /**
     * 返回一个随机指定长度的字符串
     */
    public static String randomString(Integer length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}

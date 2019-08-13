package cn.huanzi.qch.commonspider.vo;

import lombok.Data;

/**
 * 统一响应对象
 */
@Data
public class ResultVo<E> {

    private ResultVo(Integer statusCode, String statusMessage, E page) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.page = page;
    }

    //响应状态
    private Integer statusCode;

    //响应消息
    private String statusMessage;

    //响应对象
    private E page;

    /**
     * 通过静态方法获取实例
     */
    public static <E> ResultVo<E> of(Integer statusCode,String statusMessage,E page) {
        return new ResultVo<>(statusCode, statusMessage, page);
    }
}
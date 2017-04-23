package com.sorashiro.caculator.exception;

/**
 * @author Sora
 * @date 2017.4.23
 * <p>
 * Using to handle runtime exception of calculation process.
 * 封装好的处理计算过程发生的异常
 */

public class CustomerException extends RuntimeException {

    private String retCd;
    private String msgDes;

    public CustomerException() {
        super();
    }

    public CustomerException(String message) {
        super(message);
        msgDes = message;
    }

    public CustomerException(String retCd, String msgDes) {
        super();
        this.retCd = retCd;
        this.msgDes = msgDes;
    }

    public String getRetCd() {
        return retCd;
    }

    public String getMsgDes() {
        return msgDes;
    }
}
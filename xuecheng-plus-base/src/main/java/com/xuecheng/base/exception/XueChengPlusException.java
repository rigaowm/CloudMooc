package com.xuecheng.base.exception;

/**
 * @Author Rigao
 * @Title: XueChengPlusException
 * @Date: 2023/12/22 16:48
 * @Version 1.0
 * @Description:
 */

public class XueChengPlusException extends RuntimeException{
    private String errMssage;

    public XueChengPlusException() {
    }
    public XueChengPlusException(String errMssage) {
        super(errMssage);
        this.errMssage = errMssage;
    }
    public String getErrMessage() {
        return errMssage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

}

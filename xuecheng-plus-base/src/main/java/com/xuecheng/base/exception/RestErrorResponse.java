package com.xuecheng.base.exception;

/**
 * @Author Rigao
 * @Title: RestErrorResponse
 * @Date: 2023/12/22 16:47
 * @Version 1.0
 * @Description:
 */

public class RestErrorResponse {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

}

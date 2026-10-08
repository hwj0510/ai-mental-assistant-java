package com.heima.aimentalassistant.common.exception;

import com.heima.aimentalassistant.common.constant.Business;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private String code;
    private String message;
    private Object data;

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = Business.BUSINESS_ERROR;
        this.data = null;
    }
}

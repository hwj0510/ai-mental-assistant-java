package com.heima.aimentalassistant.common.results;

import com.heima.aimentalassistant.common.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T>  {

    private String code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> create() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
        return result;
    }
    public static <T> Result<T> success(T data) {
        Result<T> result = create();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(){
        Result<T> result = create();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMessage());
        return result;
    }

    public static <T> Result<T> error(String msg,String code,T data) {
        Result<T> result = create();
        result.setMsg(msg);
        result.setCode(code);
        result.setData(data);
        return result;
    }

}
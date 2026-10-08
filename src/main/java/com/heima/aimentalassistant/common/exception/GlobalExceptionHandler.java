package com.heima.aimentalassistant.common.exception;

import com.heima.aimentalassistant.common.enums.ResultCode;
import com.heima.aimentalassistant.common.results.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理方法参数校验异常
     * @param e 异常对象
     * @return 处理结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleException(MethodArgumentNotValidException e) {
        //异常数据的处理
        String message = e.getBindingResult().getFieldErrors().stream().
                map(FieldError::getDefaultMessage).
                collect(Collectors.joining(";"));
        return Result.error(ResultCode.PARAM_ERROR.getMessage() ,ResultCode.PARAM_ERROR.getCode(),message);
    }

    //处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        if(e.getData() == null){
            return Result.error(e.getMessage(),e.getCode(),null);
        }
        return Result.error(e.getMessage(),e.getCode(),null);
    }
}

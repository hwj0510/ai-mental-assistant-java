package com.heima.aimentalassistant.common.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.heima.aimentalassistant.common.enums.ResultCode;
import com.heima.aimentalassistant.common.results.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ResponseUtil {
    //过滤器中的异常处理
    public static void writeError(HttpServletResponse response, ResultCode resultCode){
        //根据不同的状态码返回不同的错误信息
        int statusCode = switch(resultCode){
            case UNAUTHORIZED , ACCESS_UNAUTHORIZED, TOKEN_INVALID,
                 TOKEN_EXPIRED, TOKEN_BLOCKED -> HttpStatus.UNAUTHORIZED.value();

            case TOKEN_ACCESS_FORBIDDEN ->  HttpStatus.FORBIDDEN.value();

            default -> HttpStatus.BAD_REQUEST.value();
        };

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try(PrintWriter writer=response.getWriter()){
            String jsonResponse = JSONUtil.toJsonStr(Result.error(resultCode.getMessage(), resultCode.getCode(),null));
            writer.print(jsonResponse);
            writer.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

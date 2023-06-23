package com.meteorcat.mix.gateway.api;

import com.meteorcat.mix.core.utils.DigestExtends;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 用于继承公用方法
 * @author MeteorCat
 */
public class BaseApi {


    /**
     * 生成参数Sign
     * @param key 密钥Key
     * @param data 数据列表
     * @return String
     */
    protected String createSign(String key,Map<String,Object> data){
        // 数据格式重新排序
        TreeMap<String,Object> sorted = new TreeMap<>(data);

        // 重新格式化数据
        StringBuffer buffer = new StringBuffer();
        sorted.forEach((k,v)-> buffer.append(v));

        // 追加Key
        buffer.append(key);
        String message = buffer.toString();

        // 返回MD5
        return DigestExtends.md5(message.getBytes()).orElse("");
    }



    /**
     * 响应客户端
     * @param status 状态
     * @param message 消息
     * @param data 数据
     * @return Map
     */
    protected Object response(int status,String message,Object data){
        return new HashMap<>(data == null ? 2 : 3){{
            put("status",status);
            put("message",message);
            if(data != null){
                put("data",data);
            }
        }};
    }


    /**
     * 返回成功数据
     * @param message 消息
     * @param data 数据
     * @return Map
     */
    protected Object success(String message,Object data){
        return response(HttpStatus.OK.value(), message,data);
    }


    /**
     * 返回成功数据
     * @param data 数据
     * @return Map
     */
    protected Object success(Object data){
        return response(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }


    /**
     * 返回成功数据
     * @return Map
     */
    protected Object success(){
        return response(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),null);
    }


    /**
     * 返回错误数据
     * @param message 消息
     * @param data 数据
     * @return Map
     */
    protected Object failed(String message,Object data){
        return response(HttpStatus.BAD_REQUEST.value(),message,data);
    }


    /**
     * 返回错误数据
     * @param message 消息
     * @return Map
     */
    protected Object failed(String message){
        return response(HttpStatus.BAD_REQUEST.value(), message,null);
    }


    /**
     * 返回错误数据
     * @param data 数据
     * @return Map
     */
    protected Object failed(Object data){
        return response(HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(), data);
    }


    /**
     * 返回错误数据
     * @return Map
     */
    protected Object failed(){
        return response(HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(),null);
    }

}

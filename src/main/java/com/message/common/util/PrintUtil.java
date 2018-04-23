package com.message.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by oneapm on 2018/4/19.
 */
public class PrintUtil {
    private static Logger log = LoggerFactory.getLogger(PrintUtil.class);

    public static void print(HttpServletResponse res, JSONObject obj){
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        try {
            res.getWriter().write(obj.toJSONString());
            res.getWriter().flush();
            res.getWriter().close();
        } catch (IOException e) {
            log.error("输出数据到web异常",e);
        }

    }
    public static void print(HttpServletResponse res, String msg,boolean status){
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        try {
            JSONObject obj = new JSONObject();
            obj.put("code", status ? 200 : 500);
            obj.put("message",msg);
            res.getWriter().write(obj.toJSONString());
            res.getWriter().flush();
            res.getWriter().close();
        } catch (IOException e) {
            log.error("输出数据到web异常",e);
        }

    }

    public static void print(HttpServletResponse res, JSONArray arr, boolean status){
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        try {
            JSONObject obj = new JSONObject();
            obj.put("code", status ? 200 : 500);
            obj.put("list",arr);
            res.getWriter().write(obj.toJSONString());
            res.getWriter().flush();
            res.getWriter().close();
        } catch (IOException e) {
            log.error("输出数据到web异常",e);
        }

    }
}

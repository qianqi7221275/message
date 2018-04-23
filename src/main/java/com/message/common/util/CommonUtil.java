package com.message.common.util;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {


    public static JSONObject getAccount(HttpServletRequest req){

        Object obj = req.getSession().getAttribute("account");
        return (JSONObject) obj;
    }
}

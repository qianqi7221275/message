package com.message.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.message.common.util.CommonUtil;
import com.message.common.util.JDBCUtil;
import com.message.common.util.PrintUtil;
import jdk.nashorn.internal.scripts.JD;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/MessageAction")
public class MessageAction {
    private static Logger log = LoggerFactory.getLogger(MessageAction.class);



    @RequestMapping("/updateMessageChat.do")
    public void updateMessageChat(HttpServletRequest req,HttpServletResponse res){

        String id = req.getParameter("id");
        String content = req.getParameter("content");

        String sql = "update message_comments set content =? where auto_id=?";
        try {
            JDBCUtil.getInstance().execute(sql,new Object[]{content,id});
            PrintUtil.print(res,"更新评论失败",true);
        } catch (SQLException e) {
            PrintUtil.print(res,"更新评论失败",false);
        }
    }

    @RequestMapping("/addFriend.do")
    public void addFriend(HttpServletRequest req,HttpServletResponse res) {

        String self = req.getParameter("self");
        String friend = req.getParameter("friend");
        String sql = "select count(*) from friend where self = ? and friend=?";
        try {
            int count = JDBCUtil.getInstance().count(sql,new Object[]{self,friend});
            if(count <= 0){
                sql = "insert into friend(auto_id,self,friend) values(?,?,?)";
                JDBCUtil.getInstance().execute(sql,new Object[]{
                        UUID.randomUUID().toString().replace("-",""),
                        self,friend
                });

            }
            PrintUtil.print(res,"添加朋友成功",true);


        } catch (SQLException e) {
            PrintUtil.print(res,"添加朋友失败",false);

        }
    }
    @RequestMapping("/queryAccountById.do")
    public void queryAccountById(HttpServletRequest req,HttpServletResponse res){
        String id = req.getParameter("id");
        String sql = "select * from account where auto_id=?";
        try {
            JSONArray query = JDBCUtil.getInstance().query(sql, new Object[]{id});
            JSONObject account = query.getJSONObject(0);
            JSONObject r = new JSONObject();
            r.put("account",account);
            r.put("code",200);
            PrintUtil.print(res,r);
        } catch (Exception e) {
            PrintUtil.print(res,"查询个人信息失败",false);
        }
    }

    @RequestMapping("/queryHistoryChat.do")
    public void queryHistoryChat(HttpServletRequest req,HttpServletResponse res){

        String sender = req.getParameter("sender");
        String recver = req.getParameter("recver");
        String sql = "select * from chat_message where (sender=? and recver=?) or (recver=? and sender=?) order by create_time";
        try {
            JSONArray query = JDBCUtil.getInstance().query(sql, new Object[]{sender, recver,sender,recver});
            PrintUtil.print(res,query,true);
        } catch (SQLException e) {
            PrintUtil.print(res,"查询历史数据失败",false);

        }
    }

    @RequestMapping("/queryFriends.do")
    public void queryFriends(HttpServletRequest req,HttpServletResponse res){


        String self = CommonUtil.getAccount(req).getString("auto_id");
        String sql = "select f.*,a.img_path as user_img from friend f left join account a on f.friend=a.auto_id  where self=?";
        try {
            JSONArray query = JDBCUtil.getInstance().query(sql, new Object[]{self});
            PrintUtil.print(res,query,true);
        } catch (SQLException e) {
            PrintUtil.print(res,"查询朋友列表失败",false);
        }
    }

    @RequestMapping("/delComment.do")
    public void delComment(HttpServletRequest req, HttpServletResponse res){

        String id = req.getParameter("id");
        boolean isMessage = Boolean.parseBoolean(req.getParameter("message"));
        String sql = "delete from message_comments where auto_id=?";
        if(isMessage){
            sql = "delete from message where auto_id=?";
        }
        try {
            JDBCUtil.getInstance().execute(sql,new Object[]{id});
            PrintUtil.print(res, "发布成功", true);
        } catch (SQLException e) {
            PrintUtil.print(res, "", false);
            log.error("删除失败",e);
        }
    }

    @RequestMapping("/queryAccountInfo.do")
    public void queryAccountInfo(HttpServletRequest req, HttpServletResponse res){


        JSONObject result = new JSONObject();

        result.put("info",CommonUtil.getAccount(req));
        result.put("code",200);
        PrintUtil.print(res, result);


    }

    @RequestMapping("/sendComment.do")
    public void sendComment(HttpServletRequest req, HttpServletResponse res){

        String msg = req.getParameter("message");
        String id = req.getParameter("id");
        String visibility = req.getParameter("visibility");
        String autoId = UUID.randomUUID().toString().replace("-","");
        String sql = "insert into message_comments(auto_id,content,account_id,message_id,visibility,create_time) values(?,?,?,?,?,?)";
        try {
            JDBCUtil.getInstance().execute(sql,new Object[]{
                    autoId,
                    msg,CommonUtil.getAccount(req).getString("auto_id"),
                    id,visibility,new Date()
            });
            PrintUtil.print(res, autoId, true);
        } catch (SQLException e) {
            log.error("发布评论失败",e);
            PrintUtil.print(res, "发布评论失败", true);
        }
    }

    @RequestMapping("/queryList.do")
    public void queryList(HttpServletRequest req, HttpServletResponse res){


        String sql = "select m.*,a.img_path as user_img from message m left join account a on a.auto_id=m.account_id order by create_time desc";
        try {
            JSONArray query = JDBCUtil.getInstance().query(sql, null);
            for (int i = 0; i < query.size(); i++) {
                JSONObject z = query.getJSONObject(i);
                z.put("comments",getComments(z.getString("auto_id")));
            }
            PrintUtil.print(res, query, true);
        } catch (SQLException e) {
            log.error("查询失败",e);
            PrintUtil.print(res, "查询失败", false);
        }
    }

    public JSONArray getComments(String id) throws SQLException {
        String sql = "select m.*,a.img_path as user_img from `message_comments` m left join account a on a.auto_id=m.account_id  where message_id = ? order by create_time";
        JSONArray query = JDBCUtil.getInstance().query(sql, new Object[]{id});
        return query;
    }

    private static String[] imgs = new String[]{
      "../img/7E145ED4-AB7F-458C-AAE3-582A41C2B540.png",
      "../img/avatar-4.jpg",
      "../img/CDB48AD9-3C85-49A2-8F5B-3C51CC0544C1.png",
      "../img/97A841DC-1371-4070-B7EC-BBB9236312E3.png",
      "../img/510827C5-003C-4826-B127-30C955C2DBAC.png",
      "../img/CCC8B1C7-F7B3-4A13-8A32-418093D5647A.png",
      "../img/CF9B6E90-DBCC-43DF-B462-FA2C75F950EF.png",
      "../img/F1DDE508-B95A-4A2F-B429-C2B9A5B5E118.png",
    };

    @RequestMapping("/deleteImgCache.do")
    public void deleteImgCache(HttpServletRequest req, HttpServletResponse res){

        String img = req.getParameter("img");
        String accountId = CommonUtil.getAccount(req).getString("auto_id");

        if(img_cache.containsKey(accountId)){
            JSONArray array = img_cache.get(accountId);
            array.remove(img);
        }
        PrintUtil.print(res, "删除成功", true);
    }

    @RequestMapping("/register.do")
    public void register(HttpServletRequest req, HttpServletResponse res) {

        String username = req.getParameter("username");


        String pwd = req.getParameter("pwd");

        try {

            String sl = "select count(*) from account where username=?";
            int count = JDBCUtil.getInstance().count(sl, new Object[]{username});
            if(count > 0 ){

                PrintUtil.print(res,"当前用户名已存在", 300);

                return ;
            }
            String sql = "insert into account(auto_id,username,password,img_path) values(?,?,?,?)";
            JDBCUtil.getInstance().execute(sql,new Object[]{
                    UUID.randomUUID().toString().replace("-",""),
                    username,pwd,
                    imgs[(int)(Math.round(Math.random()*(imgs.length - 1)))]
            });
            PrintUtil.print(res, "注册成功", true);
        } catch (SQLException e) {
            PrintUtil.print(res, "注册失败", false);
        }

    }
    @RequestMapping("/release.do")
    public void release(HttpServletRequest req, HttpServletResponse res) {

        String title = req.getParameter("title");
        String content = req.getParameter("content");


        String sql = "insert into message(auto_id,title,content,img_path,account_id,create_time) values(?,?,?,?,?,?)";

        try {
            String accountId = CommonUtil.getAccount(req).getString("auto_id");
            String img = StringUtils.join(img_cache.get(accountId),",");
            JDBCUtil.getInstance().execute(sql,new Object[]{
                    UUID.randomUUID().toString().replace("-",""),
                    title,content,img, CommonUtil.getAccount(req).getString("auto_id"),
                    new Date()
            });
            img_cache.put(accountId,new JSONArray());
            PrintUtil.print(res, "发布成功", true);
        } catch (SQLException e) {
            log.error("发布失败",e);
            PrintUtil.print(res, "发布失败", false);
        }
    }

    @RequestMapping("/login.do")
    public void login(HttpServletRequest req, HttpServletResponse res) {

        String username = req.getParameter("username");
        String pwd = req.getParameter("pwd");

        String sql = "select * from account where username = ? and password = ?";

        try {
            JSONArray query = JDBCUtil.getInstance().query(sql, new Object[]{username, pwd});
            if(query.size() > 0){
                JSONObject account = query.getJSONObject(0);
                req.getSession().setAttribute("account",account);
                PrintUtil.print(res, "登陆成功", true);
                return;
            }
        } catch (SQLException e) {
            PrintUtil.print(res, "登陆失败", false);
            return;
        }
        PrintUtil.print(res, "用户名或密码错误", false);

    }

    @RequestMapping("/loadTempFile.do")
    public void loadTempFile(HttpServletRequest request, HttpServletResponse response){
        String accountId = CommonUtil.getAccount(request).getString("auto_id");
        JSONArray arr = img_cache.get(accountId);
        PrintUtil.print(response, arr, true);
    }

    private static Map<String,JSONArray> img_cache = new HashMap<>();
    @RequestMapping("/uploadFile.do")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response) {
        try {

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            List<MultipartFile> multipartFiles = multipartRequest.getFiles("file");
            MultipartFile multipartFile = multipartFiles.get(0);
            String fname = multipartFile.getOriginalFilename();

            byte[] bytes = null;
            try {
                bytes = multipartFile.getBytes();
            } catch (IOException e2) {

            }
            String path=request.getServletContext().getRealPath("/upload");
            String uuid = UUID.randomUUID().toString();
            File file1 = new File(path+"/"+uuid+fname);
            FileUtils.writeByteArrayToFile(file1,bytes);
            int errorCode = 200;
            String msg = uuid+fname;
            String accountId = CommonUtil.getAccount(request).getString("auto_id");
            if(!img_cache.containsKey(accountId)){
                img_cache.put(accountId,new JSONArray());
            }
            img_cache.get(accountId).add("../upload/"+msg);
            String outStr = "{\"error\": false, \"errorCode\": " + errorCode + ", \"msg\": \"" + msg + "\"}";
            try {
                response.getWriter().write(outStr);
            } catch (IOException e1) {
                log.error("response输出信息失败：" + outStr);
            }
        } catch (Exception e) {
            log.error("上传文件失败",e);

        }
    }
}

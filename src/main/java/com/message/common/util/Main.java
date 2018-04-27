package com.message.common.util;

import com.message.action.MessageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;


public class Main {
    public static String myEmailAccount = "812427023@qq.com";
    public static String myEmailPassword = "lhmrkkehbxmcbccf";

    public static String myEmailSMTPHost = "smtp.qq.com";

    private static Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        sendMessage("571878793@qq.com","测试程序发送邮件","程序发送邮件正文内容");
    }

    public static void  sendMessage(String receiveMailAccount,String title,String content){
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", myEmailSMTPHost);   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

        final String smtpPort = "465";
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);

        Session session = Session.getInstance(props);
        session.setDebug(true);                                 // 设置为debug模式, 可以查看详细的发送 log

        MimeMessage message = null;
        try {
            message = createMimeMessage(session, myEmailAccount, receiveMailAccount,title,content);
        } catch (Exception e) {
            log.error("",e);
        }

        Transport transport = null;
        try {
            transport = session.getTransport();
        } catch (NoSuchProviderException e) {
            log.error("",e);
        }


        try {
            transport.connect(myEmailAccount, myEmailPassword);
        } catch (MessagingException e) {
            log.error("",e);
        }

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            log.error("",e);
        }

        // 7. 关闭连接
        try {
            transport.close();
        } catch (MessagingException e) {
            log.error("",e);
        }
    }

    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail,String title,String content) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sendMail, "天一时代", "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "XX用户", "UTF-8"));
        message.setSubject(title, "UTF-8");
        message.setContent(content, "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

}
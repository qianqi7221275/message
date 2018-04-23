package com.message.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;


public class JDBCUtil {
    private static Logger log = LoggerFactory.getLogger(JDBCUtil.class);


    private ComboPooledDataSource dataSource;
    private Properties prop = new Properties();

    private String c3p0Key = "cpool";
    private JDBCUtil(){

        try {
            initConfig();
            initDataSource();

        } catch (IOException e) {
            log.error("读取JDBC配置文件失败");
        }
    }

    private void initConfig() throws IOException {
        InputStream inStream = JDBCUtil.class
                .getResourceAsStream("/properties/jdbc-mysql.properties");
        prop.load(inStream);
    }

    private void initDataSource(){
        dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(prop.getProperty("jdbc.driverClassName"));
        } catch (PropertyVetoException var12) {
            log.debug("数据源加载出错, driver=" + prop.getProperty("jdbc.driverClassName"), var12);
        }
        dataSource.setJdbcUrl(prop.getProperty("jdbc.url"));
        dataSource.setUser(prop.getProperty("jdbc.username"));
        dataSource.setPassword(prop.getProperty("jdbc.password"));
        initDataSourceConfig(dataSource);
    }


    private void initDataSourceConfig(ComboPooledDataSource dataSource){
        dataSource.setInitialPoolSize(Integer.valueOf(prop.getProperty(c3p0Key + ".initPoolSize", "1")).intValue());
        dataSource.setMinPoolSize(Integer.valueOf(prop.getProperty(c3p0Key + ".minPoolSize", "1")).intValue());
        dataSource.setMaxPoolSize(Integer.valueOf(prop.getProperty(c3p0Key + ".maxPoolSize", "10")).intValue());
        dataSource.setMaxIdleTime(Integer.valueOf(prop.getProperty(c3p0Key + ".maxIdleTime", "1800")).intValue());
        dataSource.setMaxStatementsPerConnection(Integer.valueOf(prop.getProperty(c3p0Key + ".maxStatementsPerConnection", "50")).intValue());
        dataSource.setMaxStatements(Integer.valueOf(prop.getProperty(c3p0Key + ".maxStatements", "100")).intValue());
        dataSource.setNumHelperThreads(Integer.valueOf(prop.getProperty(c3p0Key + ".numHelperThreads", "17")).intValue());
        dataSource.setAcquireIncrement(Integer.valueOf(prop.getProperty(c3p0Key + ".acquireIncrement", "1")).intValue());
        dataSource.setAcquireRetryAttempts(Integer.valueOf(prop.getProperty(c3p0Key + ".acquireRetryAttempts", "3")).intValue());
        dataSource.setAcquireRetryDelay(Integer.valueOf(prop.getProperty(c3p0Key + ".acquireRetryDelay", "1000")).intValue());
        dataSource.setBreakAfterAcquireFailure(Boolean.valueOf(prop.getProperty(c3p0Key + ".breakAfterAcquireFailure", "false")).booleanValue());
        dataSource.setCheckoutTimeout(Integer.valueOf(prop.getProperty(c3p0Key + ".checkoutTimeout", "1800")).intValue());
        dataSource.setMaxIdleTimeExcessConnections(Integer.valueOf(prop.getProperty(c3p0Key + ".maxIdleTimeExcessConnections", "1800")).intValue());
        dataSource.setIdleConnectionTestPeriod(Integer.valueOf(prop.getProperty(c3p0Key + ".idleConnectionTestPeriod", "1800")).intValue());
        dataSource.setTestConnectionOnCheckout(Boolean.valueOf(prop.getProperty(c3p0Key + ".testConnectionOnCheckout", "true")).booleanValue());
        dataSource.setPreferredTestQuery(prop.getProperty(c3p0Key + ".preferredTestQuery", " SELECT 1 "));
    }

    private static JDBCUtil jdbcUtil;

    public static JDBCUtil getInstance(){
        if(jdbcUtil==null){
            synchronized (JDBCUtil.class){
                if(jdbcUtil == null){
                    jdbcUtil = new JDBCUtil();
                }
            }
        }
        return jdbcUtil;
    }



    public int count(String sql,Object[] params) throws SQLException {
        Connection connection=null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, sql, params);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                return resultSet.getInt(1);
            }

        }finally {
            close(connection,resultSet,preparedStatement);
        }
        return 0;
    }

    public boolean execute(String sql,Object[] params) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try{
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, sql, params);
            return preparedStatement.execute();
        }finally {
            close(connection,null,preparedStatement);
        }

    }


    public JSONArray query(String sql,Object[] params,Page page) throws SQLException {
        if(-1==page.getCount()){
            page.setCount(count("select count(*) from ("+sql+") as p", params));
        }
        int totalPage = (int) Math.ceil((double) page.getCount() / (double) page.getSize());
        page.setTotalPage(totalPage);
        if (page.getCurrent() > totalPage || page.getCurrent() < 0)
            page.setCurrent(totalPage);
        if (page.getCurrent() == 0)
            page.setCurrent(1);
        if (page.getTotalPage() == 0)
            page.setTotalPage(1);

        sql += " limit "+ ((page.getCurrent()-1) * page.getSize()) + ","+ page.getSize() +"";
        return query(sql, params);
    }



    public JSONArray query(String sql,Object[] params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        JSONArray result = new JSONArray();
        while(resultSet.next()){
            JSONObject obj = new JSONObject();

            for(int i = 1; i <= rsmd.getColumnCount(); ++i) {
                String columnLabel = rsmd.getColumnLabel(i);
                Object columnObj = resultSet.getObject(columnLabel);
                obj.put(columnLabel, columnObj == null?"":columnObj);
            }
            result.add(obj);
        }
        close(connection,resultSet,preparedStatement);
        return result;

    }

    private PreparedStatement getPreparedStatement(Connection connection,String sql,Object[] params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(params != null){
            for(int i = 0,size = params.length;i<size;i++){
                preparedStatement.setObject((i+1),params[i]);
            }
        }
        return preparedStatement;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    void close(Connection connection){
        try {
            if(connection != null) {
                connection.close();
                log.debug("conn close:" + connection);
                connection = null;
            }
        } catch (Exception e) {
            log.error("closeConnection Error", e);
        }
    }
    void close(ResultSet resultSet){
        try {
            if(resultSet != null) {
                resultSet.close();
                log.debug("ResultSet close:" + resultSet);
            }
        } catch (Exception e) {
            log.error("closeReultSet Error", e);
        }
    }
    void close(PreparedStatement statement){
        try {
            if(statement != null) {
                statement.close();
                log.debug("statement close:" + statement);
            }
        } catch (Exception e) {
            log.error("close statement Error", e);
        }
    }
    void close(Connection connection,ResultSet resultSet,PreparedStatement statement){
        close(resultSet);
        close(statement);
        close(connection);
    }

}

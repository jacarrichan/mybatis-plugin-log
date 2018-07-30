package com.jacarrichan.tools.mpl.interceptor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Sql执行时间记录拦截器
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})})
public class SqlLogInterceptor implements Interceptor {
    private Log log = LogFactory.getLog(SqlLogInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object returnVal = null;

        try {
            returnVal = invocation.proceed();
            return returnVal;
        } finally {
            try {
                logSql(invocation, startTime, returnVal);
            } catch (Exception ex) {
                log.error("error:" + ex.toString());
            }
        }
    }

    private void logSql(Invocation invocation, long startTime, Object returnVal) {
        long endTime = System.currentTimeMillis();
        long sqlCost = endTime - startTime;
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql();
        sql = beautifySql(sql);
        Object parameterObject = boundSql.getParameterObject();
        // 输入sql字符串空判断
        if (sql == null || sql.length() == 0) {
            return;
        }
        sql = sqlReplace(ms, boundSql, sql, parameterObject);
        String sqlId = ms.getId();
        if (sqlId.split("\\.").length > 2) {
            int methodIndex = sqlId.lastIndexOf(".");
            String methodName = sqlId.substring(methodIndex + 1);
            String fullClsName = sqlId.substring(0, methodIndex);
            String clsName = fullClsName.substring(fullClsName.lastIndexOf(".") + 1);
            sqlId = clsName + "." + methodName;
        }

        int returnRows = 0;

        if (null != returnVal) {
            if (returnVal instanceof ArrayList<?>) {
                List<?> returnList = (ArrayList<?>) returnVal;
                returnRows = returnList.size();
            } else if (returnVal instanceof Integer) {
                returnRows = ((Integer) returnVal);
            }
        }

        log.debug(String.format("执行耗时[%s]ms,method:[%s],SQL:[%s],count[%s]", sqlCost, sqlId, sql, returnRows));
    }

    private String sqlReplace(MappedStatement ms, BoundSql boundSql, String sql, Object parameterObject) {
        Configuration configuration = ms.getConfiguration();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null) {
            return sql;
        }
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }
            Object value;
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            String propertyValue = null;
            if (null != value) {
                if (value instanceof Date) {
                    //时间格式的内容看起来舒服
                    propertyValue = new Timestamp(((Date) value).getTime()).toString();
                } else {
                    propertyValue = value.toString();
                }
                Class javaType = parameterMapping.getJavaType();
                if (javaType.isAssignableFrom(String.class) ||
                        javaType.isAssignableFrom(Date.class) ||
                        javaType.isAssignableFrom(Timestamp.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }
            }
            sql = sql.replaceFirst("\\?", propertyValue);
        }
        return sql;
    }

    /**
     * 美化Sql
     */
    private String beautifySql(String sql) {
        sql = sql.replace("\n", "").replace("\t", "").replace("  ", " ").replace("( ", "(").replace(" )", ")").replace(" ,", ",");
        while (sql.contains("  ")) {
            sql = sql.replace("  ", " ");//两个空格替换为一个
        }
        return sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}

 

 

 

 
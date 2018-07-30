package com.jacarrichan.tools.mpl.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jacarrichan.tools.mpl.test.BaseDataTest.*;

public class DaoTest {
    private Log log = LogFactory.getLog(DaoTest.class);
    private static SqlSessionFactory sqlSessionFactory;
    private static Reader reader;

    static {
        try {
            reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getSession() {
        return sqlSessionFactory;
    }

    @Before
    public void setUp() throws Exception {
        log.warn("init datasource");
        DataSource ds = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        runScript(ds, JPETSTORE_DDL);
        runScript(ds, JPETSTORE_DATA);
    }

    @Test
    public void test() {
        SqlSession session = sqlSessionFactory.openSession();
        Map<String, Object> arg = new HashMap<>();
        arg.put("itemid", "EST-7");
        arg.put("qty", "100");
        List list = session.selectList("dao.IUserDao.selectInventory", arg);
        for (Object o : list) {
            log.debug("======>{}" + o);
        }
        arg.put("listprice", 125.50);
        list = session.selectList("dao.IUserDao.selectItem", arg);
        for (Object o : list) {
            log.debug("======>{}" + o);
        }
        session.close();
    }
}
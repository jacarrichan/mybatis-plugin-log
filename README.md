#SQL  美化


SQL和参数显示在一行。让日志中打印的SQL直接复制到SQL查询器里面就可以执行

注意看SqlLogInterceptor打印的日志:


```
 [DEBUG]-[dao.IUserDao.selectInventory] method:==>  Preparing: select * from inventory WHERE itemid=? or qty=?
 [DEBUG]-[dao.IUserDao.selectInventory] method:==> Parameters: EST-7(String), 100(String)
 [DEBUG]-[com.jacarrichan.tools.mpl.interceptor.SqlLogInterceptor] method:执行耗时[438]ms,method:[IUserDao.selectInventory],SQL:[select * from inventory WHERE itemid="EST-7" or qty="100"],count[2]
 [DEBUG]-[com.jacarrichan.tools.mpl.test.DaoTest] method:======>{}{QTY=100, ITEMID=EST-6}
 [DEBUG]-[com.jacarrichan.tools.mpl.test.DaoTest] method:======>{}{QTY=100, ITEMID=EST-7}
 [DEBUG]-[dao.IUserDao.selectItem] method:ooo Using Connection [org.hsqldb.jdbc.JDBCConnection@52f759d7]
 [DEBUG]-[dao.IUserDao.selectItem] method:==>  Preparing: select * from item WHERE itemid=? or listprice=?
 [DEBUG]-[dao.IUserDao.selectItem] method:==> Parameters: EST-7(String), 125.5(Double)
 [DEBUG]-[com.jacarrichan.tools.mpl.interceptor.SqlLogInterceptor] method:执行耗时[0]ms,method:[IUserDao.selectItem],SQL:[select * from item WHERE itemid="EST-7" or listprice="125.5"],count[2]
 [DEBUG]-[com.jacarrichan.tools.mpl.test.DaoTest] method:======>{}{STATUS=P, UNITCOST=92.00, LISTPRICE=125.50, ATTR1=Adult Male, ITEMID=EST-26, PRODUCTID=K9-CW-01, SUPPLIER=1}
 [DEBUG]-[com.jacarrichan.tools.mpl.test.DaoTest] method:======>{}{STATUS=P, UNITCOST=12.00, LISTPRICE=18.50, ATTR1=Female Puppy, ITEMID=EST-7, PRODUCTID=K9-BD-01, SUPPLIER=1}
```

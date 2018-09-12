package cw.frame.mybatisext.provider.mysql;

import cw.frame.mybatisext.provider.mysql.interceptorhandler.ResultSetHandlerInterceptor;
import cw.frame.mybatisext.provider.mysql.interceptorhandler.StatementHandlerInterceptor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "update", args = Statement.class),
        @Signature(type = ResultSetHandler.class, method="handleResultSets", args={Statement.class})
})
public class MySqlInterceptor implements Interceptor {
    private StatementHandlerInterceptor statementHandlerInterceptor;
    private ResultSetHandlerInterceptor resultSetHandlerInterceptor;

    @Override
    public Object intercept(Invocation invocation) throws Throwable{
        // query/update -> prepare -> setParameters -> handleResultSets

        Object target = invocation.getTarget();
        String methodName = invocation.getMethod().getName();

        if (target instanceof StatementHandler && methodName == "prepare"){
            return this.getStatementHandlerInterceptor().prepare(invocation);
        } else if (target instanceof StatementHandler && methodName == "update"){
            return this.getStatementHandlerInterceptor().update(invocation);
        } else if (target instanceof ResultSetHandler && methodName == "handleResultSets"){
            return this.getResultSetHandlerInterceptor().handleResultSets(invocation);
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private StatementHandlerInterceptor getStatementHandlerInterceptor() {
        if (this.statementHandlerInterceptor == null){
            this.statementHandlerInterceptor = new StatementHandlerInterceptor();
        }

        return this.statementHandlerInterceptor;
    }

    private ResultSetHandlerInterceptor getResultSetHandlerInterceptor() {
        if (this.resultSetHandlerInterceptor == null){
            this.resultSetHandlerInterceptor = new ResultSetHandlerInterceptor();
        }

        return this.resultSetHandlerInterceptor;
    }
}

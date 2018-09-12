package cw.frame.mybatisext.provider.mysql.interceptorhandler;

import cw.frame.mybatisext.base.BaseSqlStatement;

import java.util.List;

public abstract class BaseInterceptorHandler {

    protected boolean needInterceptor(String sql){
        if (sql.startsWith(BaseSqlStatement.OPERATE_TYPE_NAME_PRE)){
            return true;
        } else {
            return false;
        }
    }

    protected String joinKeys(List keys){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<keys.size(); i++){
            if (i > 0){
                sb.append(",");
            }
            sb.append(keys.get(i));
        }

        return sb.toString();
    }
}

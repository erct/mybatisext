package cw.frame.mybatisext.base;

import java.util.List;

public class FormatSqlWrapper {
    String sql;
    List<Object> parameters;

    public FormatSqlWrapper(String sql, List<Object> parameters){
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}

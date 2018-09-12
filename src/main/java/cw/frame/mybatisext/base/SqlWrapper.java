package cw.frame.mybatisext.base;

import java.util.HashMap;
import java.util.Map;

public class SqlWrapper {
    private String sql;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public SqlWrapper(){}

    public SqlWrapper(String sql){
        this.sql = sql;
    }

    public SqlWrapper(String sql, Map<String, Object> parameters){
        this.sql = sql;
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getSql() {
        return sql;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}

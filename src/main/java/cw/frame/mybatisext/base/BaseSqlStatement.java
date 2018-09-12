package cw.frame.mybatisext.base;

import cw.frame.mybatisext.ExpressionExplain;
import cw.frame.mybatisext.provider.mysql.statement.DefaultExpressionExplain;
import cw.frame.mybatisext.SqlStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseSqlStatement implements SqlStatement {

    public static final String OPERATE_TYPE_NAME_PRE = "mybatisext.";
    public static final String OPERATE_TYPE_ADD_ONE = OPERATE_TYPE_NAME_PRE + "addOne";
    public static final String OPERATE_TYPE_ADD_MANY = OPERATE_TYPE_NAME_PRE + "addMany";

    public static final String OPERATE_TYPE_REMOVE_BY_ID = OPERATE_TYPE_NAME_PRE + "removeById";
    public static final String OPERATE_TYPE_REMOVE_BY_IDS = OPERATE_TYPE_NAME_PRE + "removeByIds";
    public static final String OPERATE_TYPE_REMOVE = OPERATE_TYPE_NAME_PRE + "remove";

    public static final String OPERATE_TYPE_UPDAT_BY_ID = OPERATE_TYPE_NAME_PRE + "updateById";
    public static final String OPERATE_TYPE_UPDATE = OPERATE_TYPE_NAME_PRE + "update";

    public static final String OPERATE_TYPE_GET_BY_ID = OPERATE_TYPE_NAME_PRE + "findById";
    public static final String OPERATE_TYPE_GET_BY_IDS = OPERATE_TYPE_NAME_PRE + "findByIds";
    public static final String OPERATE_TYPE_GET_ONE = OPERATE_TYPE_NAME_PRE + "getOne";
    public static final String OPERATE_TYPE_GET_MANY = OPERATE_TYPE_NAME_PRE + "getMany";
    public static final String OPERATE_TYPE_GET_PAGE = OPERATE_TYPE_NAME_PRE + "getPage";

    public static final String OPERATE_PARAM_ENTITY = "entity";
    public static final String OPERATE_PARAM_ENTITIES = "entities";
    public static final String OPERATE_PARAM_PRIMARY_KEY = "id";
    public static final String OPERATE_PARAM_PRIMARY_KEYS = "ids";
    public static final String OPERATE_PARAM_SQLSTATEMANT = "sqlStatement";
    public static final String OPERATE_PARAM_PAGER = "pager";

    private SqlWrapper sqlWrapper;
    private ExpressionExplain expressionExplain = null;
    private ResultMap resultMap;
    private FormatSqlWrapper formatSqlWrapper = null;

    @Override
    public SqlWrapper getSqlWrapper(){
        if (this.sqlWrapper == null){
            this.prepare();
        }

        return this.sqlWrapper;
    }

    @Override
    public ResultMap getResultMap(){
        return this.resultMap;
    }

    @Override
    public FormatSqlWrapper getFormatSqlWrapper(){
        if (this.formatSqlWrapper == null){
            this.formatSql();
        }

        return this.formatSqlWrapper;
    }

    public void setResultMap(ResultMap resultMap){
        this.resultMap = resultMap;
    }

    public void setExpressionExplain(ExpressionExplain expressionExplain) {
        this.expressionExplain = expressionExplain;
    }

    public ExpressionExplain getExpressionExplain() {
        if (this.expressionExplain == null){
            this.expressionExplain = new DefaultExpressionExplain();
        }

        return this.expressionExplain;
    }

    public String getParameterName(String tableName, String propertyName){
        StringBuilder sb = new StringBuilder();

        sb.append("#{");
        if(tableName != null && !tableName.isEmpty()){
            sb.append(tableName).append(".");
        }
        sb.append(propertyName);
        sb.append("}");

        return sb.toString();
    }

    protected void setSqlWrapper(String sql) {
        this.sqlWrapper = new SqlWrapper(sql);
    }

    protected void setSqlWrapper(String sql, Map<String, Object> parameters) {
        this.sqlWrapper = new SqlWrapper(sql, parameters);
    }

    protected void setSqlWrapper(SqlWrapper sqlWrapper) {
        this.sqlWrapper = sqlWrapper;
    }

    protected void unsetSqlWrapper(){
        this.sqlWrapper = null;
    }

    protected SqlWrapper combineSqlWrappers(List<SqlStatement> sqlStatements, String joinStr){
        if (sqlStatements.size() == 0){
            return new SqlWrapper();
        }

        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<String, Object>();

        for(SqlStatement sqlStatement : sqlStatements){
            if (sb.length() > 0){
                sb.append(joinStr);
            }

            SqlWrapper sqlWrapper = sqlStatement.getSqlWrapper();
            sb.append(sqlWrapper.getSql());
            parameters.putAll(sqlWrapper.getParameters());
        }

        return new SqlWrapper(sb.toString(), parameters);
    }

    protected abstract void prepare();

    private void formatSql(){
        StringBuffer sbSql = new StringBuffer();
        List<Object> values = new ArrayList<Object>();
        SqlWrapper sqlWrapper = this.getSqlWrapper();

        Pattern pattern = Pattern.compile("#\\{\\S+?\\}");
        Matcher matcher = pattern.matcher(sqlWrapper.getSql());

        while (matcher.find()){
            matcher.appendReplacement(sbSql, this.getFormatSqlReplacement());
            values.add(sqlWrapper.getParameters().get(matcher.group()));
        }

        matcher.appendTail(sbSql);

        this.formatSqlWrapper = new FormatSqlWrapper(sbSql.toString(), values);
    }
}

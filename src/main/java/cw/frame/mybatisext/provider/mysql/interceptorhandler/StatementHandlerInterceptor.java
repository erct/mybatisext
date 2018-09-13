package cw.frame.mybatisext.provider.mysql.interceptorhandler;

import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.FormatSqlWrapper;
import cw.frame.mybatisext.base.Pager;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import cw.frame.mybatisext.base.entity.BaseExtEnum;
import cw.frame.mybatisext.base.entity.ColumnInfo;
import cw.frame.mybatisext.base.entity.TableInfo;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatementHandlerInterceptor extends BaseInterceptorHandler {
    private Logger logger= LoggerFactory.getLogger(getClass());

    public Object prepare(Invocation invocation) throws Throwable{
        Object target = invocation.getTarget();
        StatementHandler statementHandler = (StatementHandler)target;
        BoundSql boundSql = statementHandler.getBoundSql();

        if (!this.needInterceptor(boundSql.getSql())){
            return invocation.proceed();
        }

        MetaObjectHelper metaObjectHelper = new MetaObjectHelper(statementHandler);
        SqlCommandType sqlCommandType = metaObjectHelper.getSqlCommandType();
        Object result = null;

        if (sqlCommandType == SqlCommandType.INSERT){
            result = executeInsertOnPrepareMethod(metaObjectHelper, (Connection)invocation.getArgs()[0], boundSql.getSql());
        } else if (sqlCommandType == SqlCommandType.DELETE){
            result = executeDeleteOnPrepareMethod(metaObjectHelper, (Connection)invocation.getArgs()[0], boundSql.getSql());
        } else if (sqlCommandType == SqlCommandType.UPDATE){
            result = executeUpdateOnPrepareMethod(metaObjectHelper, (Connection)invocation.getArgs()[0], boundSql.getSql());
        } else if (sqlCommandType == SqlCommandType.SELECT){
            result = executeSelectOnPrepareMethod(metaObjectHelper, (Connection)invocation.getArgs()[0], boundSql.getSql());
        }

        if (result == null){
            return invocation.proceed();
        } else {
            return result;
        }
    }

    public Object update(Invocation invocation) throws Throwable{
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        StatementHandler statementHandler = (StatementHandler)target;
        BoundSql boundSql = statementHandler.getBoundSql();

        if (!this.needInterceptor(boundSql.getSql())){
            return invocation.proceed();
        }

        MetaObjectHelper metaObjectHelper = new MetaObjectHelper(statementHandler);
        Object result = null;

        if (metaObjectHelper.getSqlCommandType() == SqlCommandType.INSERT){
            String operateSql = boundSql.getSql();
            Object parameterObject = null;
            if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_ADD_ONE)){
                parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_ENTITY);
            } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_ADD_MANY)){
                parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_ENTITIES);
            }

            result = executeInsertOnUpdateMethod(parameterObject, (PreparedStatement)args[0], operateSql);
        }

        if (result == null){
            return invocation.proceed();
        } else {
            return result;
        }
    }

    private PreparedStatement executeSelectOnPrepareMethod(MetaObjectHelper metaObjectHelper, Connection connection, String operateSql) throws Throwable{
        if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_BY_ID)){
            Object parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEY);
            TableInfo tableInfo = TableInfo.getTableInfo(metaObjectHelper.getEntityClassType());
            ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();

            StringBuilder sb = new StringBuilder();
            sb.append("select * from ").append(tableInfo.getTableName()).append(" where `");
            sb.append(primaryKeyColumn.getColumnName()).append("`=?");

            PreparedStatement preparedStatement = this.getPreparedStatementBySql(sb.toString(), connection);
            this.setPreparedStatementValue(preparedStatement,1, parameterObject);

            return preparedStatement;
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_BY_IDS)){
            Object parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEYS);
            TableInfo tableInfo = TableInfo.getTableInfo(metaObjectHelper.getEntityClassType());
            ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();
            List keys = (List)parameterObject;

            StringBuilder sb = new StringBuilder();
            sb.append("select * from ").append(tableInfo.getTableName()).append(" where find_in_set(`");
            sb.append(primaryKeyColumn.getColumnName()).append("`,?)");

            PreparedStatement preparedStatement = this.getPreparedStatementBySql(sb.toString(), connection);
            this.setPreparedStatementValue(preparedStatement, 1, this.joinKeys(keys));

            return preparedStatement;
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_ONE)){
            SqlStatement sqlStatement = (SqlStatement) metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT);
            FormatSqlWrapper formatSqlWrapper = sqlStatement.getFormatSqlWrapper();
            String sql = formatSqlWrapper.getSql();
            int indexOfLimit = sql.indexOf("limit");
            if (indexOfLimit > 0){
                sql = "select * from (" + sql + ") a limit 1";
            } else {
                sql += " limit 1";
            }
            return getPreparedStatementBySqlStatement(sql, formatSqlWrapper.getParameters(), connection);
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_MANY)){
            return getPreparedStatementBySqlStatement(metaObjectHelper, connection);
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_PAGE)){
            SqlStatement sqlStatement = (SqlStatement) metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT);
            Pager pager = (Pager) metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_PAGER);

            FormatSqlWrapper formatSqlWrapper = sqlStatement.getFormatSqlWrapper();
            String pageSql = formatSqlWrapper.getSql();
            String sql = "select count(*) from (" + pageSql + ") a";

            PreparedStatement countStatement = getPreparedStatementBySqlStatement(sql, formatSqlWrapper.getParameters(), connection);
            ResultSet resultSet = countStatement.executeQuery();
            if (resultSet.next()){
                Object count = resultSet.getObject(1);
                pager.setRowCount(Integer.valueOf(count.toString()));
                int currentPage = pager.getCurrentPage();
                int pageSize = pager.getPageSize();
                int skip = pageSize * (currentPage - 1);

                StringBuilder sb = new StringBuilder();

                int indexOfLimit = pageSql.indexOf("limit");
                if (indexOfLimit > 0){
                    sb.append("select * from (").append(pageSql).append(") a");
                } else {
                    sb.append(pageSql);
                }
                sb.append(" limit ");
                if (skip > 0){
                    sb.append(skip).append(",");
                }
                sb.append(pageSize);
                return getPreparedStatementBySqlStatement(sb.toString(), formatSqlWrapper.getParameters(), connection);
            }

            return getPreparedStatementBySqlStatement(metaObjectHelper, connection);
        }

        return null;
    }

    private PreparedStatement executeUpdateOnPrepareMethod(MetaObjectHelper metaObjectHelper, Connection connection, String operateSql) throws Throwable{
        if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_UPDAT_BY_ID)){
            Object parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_ENTITY);
            TableInfo tableInfo = TableInfo.getTableInfo(parameterObject.getClass().getName());

            StringBuilder sbSetPart = new StringBuilder();
            ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();
            List<Object> values = new ArrayList<Object>();

            for (ColumnInfo columnInfo : tableInfo.getColumnns()){
                if (!columnInfo.isDbColumn()){
                    continue;
                }
                if (!columnInfo.getIsPrimaryKey()){
                    if (sbSetPart.length() > 0){
                        sbSetPart.append(",");
                    }
                    sbSetPart.append("`").append(columnInfo.getColumnName()).append("`").append("=?");
                    values.add(TableInfo.getFieldValue(parameterObject, columnInfo.getField()));
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("update ").append(tableInfo.getTableName()).append(" set ");
            sb.append(sbSetPart.toString());
            sb.append(" where `").append(primaryKeyColumn.getColumnName()).append("`=?");

            PreparedStatement preparedStatement = this.getPreparedStatementBySql(sb.toString(), connection);
            int i = 1;
            for (Object value : values){
                this.setPreparedStatementValue(preparedStatement, i, value);
                i++;
            }
            this.setPreparedStatementValue(preparedStatement, i, TableInfo.getFieldValue(parameterObject, primaryKeyColumn.getField()));

            return preparedStatement;

        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_UPDATE)){
            return getPreparedStatementBySqlStatement(metaObjectHelper, connection);
        }

        return null;
    }

    private PreparedStatement executeDeleteOnPrepareMethod(MetaObjectHelper metaObjectHelper, Connection connection, String operateSql) throws Throwable{
        if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_REMOVE_BY_ID)){
            Object parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEY);
            TableInfo tableInfo = TableInfo.getTableInfo(metaObjectHelper.getEntityClassType());
            ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();

            StringBuilder sb = new StringBuilder();
            sb.append("delete from ").append(tableInfo.getTableName()).append(" where ");
            sb.append(primaryKeyColumn.getColumnName()).append("=?");

            PreparedStatement preparedStatement = this.getPreparedStatementBySql(sb.toString(), connection);
            this.setPreparedStatementValue(preparedStatement,1, parameterObject);

            return preparedStatement;
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_REMOVE_BY_IDS)){
            Object parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEYS);
            TableInfo tableInfo = TableInfo.getTableInfo(metaObjectHelper.getEntityClassType());
            ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();

            List keys = (List)parameterObject;

            StringBuilder sb = new StringBuilder();
            sb.append("delete from ").append(tableInfo.getTableName()).append(" where find_in_set(`");
            sb.append(primaryKeyColumn.getColumnName()).append("`,?)");

            PreparedStatement preparedStatement = this.getPreparedStatementBySql(sb.toString(), connection);
            this.setPreparedStatementValue(preparedStatement,1, this.joinKeys(keys).toString());

            return preparedStatement;

        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_REMOVE)){
            return getPreparedStatementBySqlStatement(metaObjectHelper, connection);
        }

        return null;
    }

    private PreparedStatement executeInsertOnPrepareMethod(MetaObjectHelper metaObjectHelper, Connection connection, String operateSql) throws Throwable{
        TableInfo tableInfo;
        List<Object> entities = null;
        Object parameterObject;

        if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_ADD_ONE)){
            parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_ENTITY);
            tableInfo = TableInfo.getTableInfo(parameterObject.getClass().getName());
            entities = new ArrayList<Object>();
            entities.add(parameterObject);
        } else if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_ADD_MANY)){
            parameterObject = metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_ENTITIES);
            entities = (List)parameterObject;
            if (entities.size() == 0){
                throw new IllegalArgumentException();
            }
            tableInfo = TableInfo.getTableInfo(entities.get(0).getClass().getName());
        } else {
            return null;
        }

        List<List<Object>> allValues = new ArrayList<List<Object>>();
        String sql = getInsertSql(tableInfo, entities, allValues);
        ColumnInfo primaryKeyColumn = tableInfo.getPrimaryKeyColumn();

        PreparedStatement preparedStatement;
        if (primaryKeyColumn.getIsPrimaryKey() && primaryKeyColumn.isGeneratedKey() && allValues.size() == 1){
            preparedStatement = this.getPreparedStatementBySql(sql, connection, Statement.RETURN_GENERATED_KEYS);
        } else {
            preparedStatement = this.getPreparedStatementBySql(sql, connection);
        }

        for (List<Object> values : allValues){
            for (int i=0; i<values.size(); i++){
                Object paramValue = values.get(i);
                this.setPreparedStatementValue(preparedStatement,i+1, paramValue);
            }
            if (allValues.size() > 1){
                preparedStatement.addBatch();
            }
        }

        return preparedStatement;
    }

    private String getInsertSql(TableInfo tableInfo, List<Object> entities, List<List<Object>> allValues) throws Throwable{
        StringBuilder sb = new StringBuilder();

        sb.append("insert into ");
        sb.append(tableInfo.getTableName());
        sb.append("(");

        StringBuilder sbColumn = new StringBuilder();
        StringBuilder sbValue = new StringBuilder();

        for (int i=0; i<entities.size(); i++){
            List<Object> values = new ArrayList<Object>();
            for (ColumnInfo columnInfo : tableInfo.getColumnns()){
                if (!columnInfo.isDbColumn()){
                    continue;
                }
                if (columnInfo.getIsPrimaryKey() && columnInfo.isGeneratedKey()){
                    continue;
                }

                if (i == 0){
                    if (sbColumn.length() > 0){
                        sbColumn.append(",");
                        sbValue.append(",");
                    }

                    sbColumn.append("`").append(columnInfo.getColumnName()).append("`");
                    sbValue.append("?");
                }

                Object val = TableInfo.getFieldValue(entities.get(i), columnInfo.getField());
                values.add(val);
            }

            allValues.add(values);
        }

        sb.append(sbColumn.toString()).append(")");
        sb.append(" values(").append(sbValue.toString()).append(")");

        return sb.toString();
    }

    private Integer executeInsertOnUpdateMethod(Object parameterObject, PreparedStatement preparedStatement, String operateSql) throws Throwable{
        Object entity = null;
        if (parameterObject instanceof BaseExtEntity){
            entity = parameterObject;
        } else if (parameterObject instanceof List){
            List entities = (List)parameterObject;
            if (entities.size() == 1){
                entity = entities.get(0);
            }
        }

        if (entity != null){
            TableInfo tableInfo = TableInfo.getTableInfo(entity.getClass().getName());
            ColumnInfo keyColumnInfo = tableInfo.getPrimaryKeyColumn();

            int result = preparedStatement.executeUpdate();

            if (keyColumnInfo.isGeneratedKey()){
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()){
                    Object primaryKeyValue = rs.getObject(1);
                    TableInfo.setFieldValue(parameterObject, keyColumnInfo.getField(), primaryKeyValue);
                }
            }
            return result;
        } else {
            int[] results = preparedStatement.executeBatch();
            int resultNum = 0;
            for (int v : results){
                resultNum += v;
            }

            return resultNum;
        }
    }

    private PreparedStatement getPreparedStatementBySqlStatement(MetaObjectHelper metaObjectHelper, Connection connection) throws Throwable{
        SqlStatement sqlStatement = (SqlStatement) metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT);
        FormatSqlWrapper formatSqlWrapper = sqlStatement.getFormatSqlWrapper();

        return this.getPreparedStatementBySqlStatement(formatSqlWrapper.getSql(), formatSqlWrapper.getParameters(), connection);
    }

    private PreparedStatement getPreparedStatementBySqlStatement(String sql, List<Object> parameters, Connection connection) throws Throwable{
        PreparedStatement preparedStatement = this.getPreparedStatementBySql(sql, connection);
        for (int i=0; i<parameters.size(); i++){
            this.setPreparedStatementValue(preparedStatement,i + 1, parameters.get(i));
        }

        return preparedStatement;
    }

    private PreparedStatement getPreparedStatementBySql(String sql, Connection connection) throws Throwable{
        this.logger.debug(sql);

        return connection.prepareStatement(sql);
    }

    private PreparedStatement getPreparedStatementBySql(String sql, Connection connection, int autoGeneratedKeys) throws Throwable{
        this.logger.debug(sql);

        return connection.prepareStatement(sql, autoGeneratedKeys);
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int index, Object value) throws Throwable{
        if (value instanceof BaseExtEnum){
            preparedStatement.setObject(index, ((BaseExtEnum) value).getValue());
        } else {
            preparedStatement.setObject(index, value);
        }
    }
}

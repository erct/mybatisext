package cw.frame.mybatisext.provider.mysql;

import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.ResultMap;
import cw.frame.mybatisext.base.SqlWrapper;
import cw.frame.mybatisext.base.TableIndentityProvider;
import cw.frame.mybatisext.base.entity.RelationshipInfo;
import cw.frame.mybatisext.base.entity.TableInfo;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import cw.frame.mybatisext.enumeration.ConditionType;
import cw.frame.mybatisext.enumeration.JoinType;
import cw.frame.mybatisext.enumeration.OrderType;
import cw.frame.mybatisext.provider.mysql.statement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlStatement extends BaseSqlStatement {
    private Class<? extends BaseExtEntity> entityClass;
    private TableInfo tableInfo;

    private TableIndentityProvider tableIndentityProvider;
    private OperateType operateType;

    private SelectStatement selectStatement = null;
    private List<MySqlStatement> selectSubMySqlStatements = null;
    private MutiSqlStatement joinStatements = null;
    private MutiSqlStatement conditionStatemennts = null;
    private MutiSqlStatement orderStatemennts = null;
    private MutiSqlStatement groupStatemennts = null;
    private MutiSqlStatement havingStatemennts = null;
    private String limitString = "";

    private List<UpdateSetItem> updateSetItems = new ArrayList<UpdateSetItem>();

    public static MySqlStatement createSelectStatement(Class<? extends BaseExtEntity> entityClass){
        return new MySqlStatement(entityClass, OperateType.SELECT);
    }

    public static MySqlStatement createUpdateStatement(Class<? extends BaseExtEntity> entityClass){
        return new MySqlStatement(entityClass, OperateType.UPDATE);
    }

    public static MySqlStatement createDeleteStatement(Class<? extends BaseExtEntity> entityClass){
        return new MySqlStatement(entityClass, OperateType.DELETE);
    }

    public MySqlStatement(Class<? extends BaseExtEntity> entityClass, TableIndentityProvider tableIndentityProvider){
        this.init(entityClass, tableIndentityProvider);
        this.operateType = OperateType.SELECT;
    }

    private MySqlStatement(Class<? extends BaseExtEntity> entityClass, OperateType operateType){
        this.init(entityClass, new TableIndentityProvider());
        this.operateType = operateType;
    }

    public SelectStatement getSelectStatement() {
        return this.selectStatement;
    }

    public MySqlStatement select(String... propertyNames){
        this.selectStatement.select(propertyNames);

        return this;
    }

    public MySqlStatement selectAs(String propertyName, String resultPropertyName){
        this.selectStatement.selectAs(propertyName, resultPropertyName);
        return this;
    }

    public MySqlStatement selectAs(Map<String, String> propertyNameMap){
        this.selectStatement.selectAs(propertyNameMap);
        return this;
    }

    public MySqlStatement set(String propertyName, Object propertyValue){
        this.updateSetItems.add(new UpdateSetItem(propertyName, propertyValue));
        return this;
    }

    /**
     * set a.name = b.name
     * @param propertyName
     * @param subQuery
     * @param subPropertyName
     * @return
     */
    public MySqlStatement set(String propertyName, MySqlStatement subQuery, String subPropertyName){
        this.updateSetItems.add(new UpdateSetItem(propertyName, subQuery.getSelectStatement().getAliasTableName(), subPropertyName));
        return this;
    }

    public MySqlStatement set(Map<String, Object> propertyMap){
        for (String propertyName : propertyMap.keySet()){
            this.updateSetItems.add(new UpdateSetItem(propertyName, propertyMap.get(propertyName)));
        }
        return this;
    }

    public MySqlStatement join(Class<? extends BaseExtEntity> classType, JoinType joinType, String fromPropertyName, String toPropertyName){
        SingleJoinStatement singleJoinStatement = this.getSingleJoinStatement(classType, joinType);
        singleJoinStatement.setRelationship(fromPropertyName, toPropertyName);

        return this;
    }

    public MySqlStatement join(Class<? extends BaseExtEntity> classType, JoinType joinType, Map<String, String> relationshipMap){
        SingleJoinStatement singleJoinStatement = this.getSingleJoinStatement(classType, joinType);
        for (String fromPropertyName : relationshipMap.keySet()){
            singleJoinStatement.setRelationship(fromPropertyName, relationshipMap.get(fromPropertyName));
        }

        return this;
    }

    public MySqlStatement join(MySqlStatement subQuery, JoinType joinType, String fromPropertyName, String toPropertyName, boolean joinByQuery){
        if (joinByQuery){
            SingleJoinStatement statement = this.getSingleJoinStatement(subQuery, joinType);
            statement.setRelationship(fromPropertyName, toPropertyName);
        } else {
            SingleJoinStatement singleJoinStatement = this.getSingleJoinStatement(subQuery.getSelectStatement(), joinType);
            singleJoinStatement.setRelationship(fromPropertyName, toPropertyName);

        }

        return this;
    }

    public MySqlStatement join(MySqlStatement subQuery, JoinType joinType, Map<String, String> relationshipMap, boolean joinByQuery){
        SingleJoinStatement singleJoinStatement;

        if (joinByQuery){
            singleJoinStatement = this.getSingleJoinStatement(subQuery, joinType);

        } else {
            singleJoinStatement = this.getSingleJoinStatement(subQuery.getSelectStatement(), joinType);

        }

        for (String fromPropertyName : relationshipMap.keySet()){
            singleJoinStatement.setRelationship(fromPropertyName, relationshipMap.get(fromPropertyName));
        }

        return this;
    }

    public MySqlStatement where(String propertyName, ConditionType conditionType, Object value){
        this.setCondition(this.selectStatement, propertyName, conditionType, value);

        return this;
    }

    public MySqlStatement where(MySqlStatement subQuery, String propertyName, ConditionType conditionType, Object value){
        this.setCondition(subQuery.getSelectStatement(), propertyName, conditionType, value);

        return this;
    }

    public MySqlStatement orderBy(String propertyName, OrderType orderType){
        this.setOrderBy(this.selectStatement, propertyName, orderType);
        return this;
    }

    public MySqlStatement orderBy(MySqlStatement subQuery, String propertyName, OrderType orderType){
        this.setOrderBy(subQuery.getSelectStatement(), propertyName, orderType);
        return this;
    }

    public MySqlStatement groupBy(String propertyName){
        this.setGroupBy(this.selectStatement, propertyName);
        return this;
    }

    public MySqlStatement groupBy(MySqlStatement subQuery, String propertyName){
        this.setGroupBy(subQuery.getSelectStatement(), propertyName);
        return this;
    }

    public MySqlStatement having(String expression, ConditionType conditionType, Object value){
        this.setHaving(this.selectStatement, expression, conditionType, value);
        return this;
    }

    public MySqlStatement having(MySqlStatement subQuery, String expression, ConditionType conditionType, Object value){
        this.setHaving(subQuery.getSelectStatement(), expression, conditionType, value);
        return this;
    }

    public MySqlStatement limit(int limit){
        return this.limit(limit, 0);
    }

    public MySqlStatement limit(int limit, int skip){
        StringBuilder sb = new StringBuilder();
        sb.append("limit ");
        if (skip > 0){
            sb.append(skip).append(",").append(limit);
        } else {
            sb.append(limit);
        }
        this.limitString = sb.toString();

        return this;
    }

    /**
     * 根据实体类型创建子查询对象，不会有查询字段返回
     * @param entityClass
     * @return
     */
    public MySqlStatement createSubQuery(Class<? extends BaseExtEntity> entityClass){
        MySqlStatement operator = new MySqlStatement(entityClass, this.tableIndentityProvider);

        return operator;
    }

    /**
     * 创建子查询对象，结果存储于@OneOne or @OneMany定义的关系字段
     * @param relationshipPropertyName 关系字段名
     * @return
     * @throws
     */
    public MySqlStatement createSubQuery(String relationshipPropertyName){
        RelationshipInfo relationshipInfo = this.tableInfo.getOneManyMap().getOrDefault(relationshipPropertyName, null);
        if (relationshipInfo == null){
            relationshipInfo = this.tableInfo.getOneOneMap().getOrDefault(relationshipPropertyName, null);
        }

        if (relationshipInfo == null){
            throw new IllegalArgumentException("relationshipPropertyName error: " + relationshipPropertyName);
        }

        MySqlStatement operator = new MySqlStatement(relationshipInfo.getSubTable().getTableEntityClass(), this.tableIndentityProvider);

        if (this.selectSubMySqlStatements == null){
            this.selectSubMySqlStatements = new ArrayList<MySqlStatement>();
        }
        this.selectSubMySqlStatements.add(operator);
        this.getResultMap().addSubResultMap(operator.getResultMap(), relationshipPropertyName);

        this.join(operator, JoinType.INNER_JOIN, relationshipInfo.getPropertyKey(), relationshipInfo.getForeignKey(), false);

        return operator;
    }

    @Override
    protected void prepare(){
        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<String, Object>();

        switch (this.operateType){
            case SELECT:
                sb.append("select ");

                SqlWrapper selectSqlWrapper = this.selectStatement.getSqlWrapper();
                sb.append(selectSqlWrapper.getSql());
                parameters.putAll(selectSqlWrapper.getParameters());

                this.setSubSelectFieldsSql(this.selectSubMySqlStatements, sb, parameters);

                sb.append(" from ").append(this.selectStatement.getTableInfo().getTableName()).append(" ");
                sb.append(this.selectStatement.getAliasTableName());

                this.combineByWrapper(this.joinStatements, sb, parameters, " ");

                this.combineByWrapper(this.conditionStatemennts, sb, parameters, " where ");

                this.combineByWrapper(this.orderStatemennts, sb, parameters, " order by ");

                this.combineByWrapper(this.groupStatemennts, sb, parameters, " group by ");

                this.combineByWrapper(this.havingStatemennts, sb, parameters, " having ");

                if (this.limitString != null && !this.limitString.isEmpty()){
                    sb.append(" ").append(this.limitString);
                }

                break;

            case UPDATE:
                sb.append("update ").append(this.selectStatement.getTableInfo().getTableName()).append(" ");
                sb.append(this.selectStatement.getAliasTableName());

                this.combineByWrapper(this.joinStatements, sb, parameters, " ");

                sb.append(" set ");
                int i=0;
                for (UpdateSetItem updateSetItem : this.updateSetItems){
                    if (i > 0){
                        sb.append(",");
                    }
                    String parameterName = this.getParameterName(this.selectStatement.getAliasTableName(), updateSetItem.getPropertyName());
                    sb.append(this.selectStatement.getAliasTableName()).append(".`");
                    sb.append(updateSetItem.getPropertyName());
                    sb.append("`=");

                    if (updateSetItem.isUseTargetTableProperty()){
                        sb.append(updateSetItem.getTargetTableName()).append(".`");
                        sb.append(updateSetItem.targetPropertyName).append("`");
                    } else {
                        sb.append(parameterName);
                        parameters.put(parameterName, updateSetItem.getPropertyValue());
                    }
                    i++;
                }

                this.combineByWrapper(this.conditionStatemennts, sb, parameters, " where ");

                break;

            case DELETE:
                sb.append("delete ").append(this.selectStatement.getAliasTableName()).append(" from ").append(this.selectStatement.getTableInfo().getTableName()).append(" ");
                sb.append(this.selectStatement.getAliasTableName());

                this.combineByWrapper(this.joinStatements, sb, parameters, " ");
                this.combineByWrapper(this.conditionStatemennts, sb, parameters, " where ");

                break;
        }

        this.setSqlWrapper(sb.toString(), parameters);
    }

    public List<MySqlStatement> getSelectSubMySqlStatements(){
        return this.selectSubMySqlStatements;
    }

    @Override
    public ResultMap getResultMap(){
        return this.selectStatement.getResultMap();
    }

    private void setGroupBy(SelectStatement statement, String propertyName){
        String tableName = statement.getAliasTableName();
        String columnName = statement.getTableInfo().getColumnByPropertyName(propertyName).getColumnName();
        SingleGroupStatement groupStatement = new SingleGroupStatement(columnName, tableName);

        if (this.groupStatemennts == null){
            this.groupStatemennts = new MutiSqlStatement(",");
        }
        this.groupStatemennts.addStatement(groupStatement);
    }

    private void setSubSelectFieldsSql(List<MySqlStatement> subMySqlStatements, StringBuilder sb, Map<String, Object> parameters){
        if (subMySqlStatements != null){
            for (MySqlStatement operator : subMySqlStatements){
                SelectStatement statement = operator.getSelectStatement();
                if (statement.hasSelectFields()){
                    this.combineByWrapper(statement, sb, parameters, ",");
                }

                this.setSubSelectFieldsSql(operator.getSelectSubMySqlStatements(), sb, parameters);
            }
        }
    }

    private void setOrderBy(SelectStatement statement, String propertyName, OrderType orderType){
        String tableName = statement.getAliasTableName();
        String columnName = statement.getTableInfo().getColumnByPropertyName(propertyName).getColumnName();

        SingleOrderStatement singleOrderStatement = new SingleOrderStatement(columnName, orderType, tableName);

        if (this.orderStatemennts == null){
            this.orderStatemennts = new MutiSqlStatement(",");
        }

        this.orderStatemennts.addStatement(singleOrderStatement);
    }

    private void setCondition(SelectStatement statement, String propertyName, ConditionType conditionType, Object value){
        if (this.conditionStatemennts == null){
            this.conditionStatemennts = new MutiSqlStatement(" and ");
        }

        String tableName = statement.getAliasTableName();
        String columnName = statement.getTableInfo().getColumnByPropertyName(propertyName).getColumnName();

        SingleConditionStatement singleConditionStatement = new SingleConditionStatement(tableName, columnName, conditionType, value);
        this.conditionStatemennts.addStatement(singleConditionStatement);
    }

    private SingleJoinStatement getSingleJoinStatement(Class<? extends BaseExtEntity> classType, JoinType joinType){
        SelectStatement statement = new SelectStatement(classType, this.tableIndentityProvider.getNextTableAsName());

        return this.getSingleJoinStatement(statement, joinType);
    }

    private SingleJoinStatement getSingleJoinStatement(SelectStatement statement, JoinType joinType){
        this.initJoinStatements();

        SingleJoinStatement singleJoinStatement = new SingleJoinStatement(this.selectStatement, statement, joinType);

        this.joinStatements.addStatement(singleJoinStatement);

        return singleJoinStatement;

    }

    private SingleJoinStatement getSingleJoinStatement(MySqlStatement subQuery, JoinType joinType){
        this.initJoinStatements();

        SingleJoinStatement singleJoinStatement = new SingleJoinStatement(this.selectStatement, subQuery, joinType);

        this.joinStatements.addStatement(singleJoinStatement);

        return singleJoinStatement;
    }

    private void initJoinStatements(){
        if (this.joinStatements == null){
            this.joinStatements = new MutiSqlStatement(" ");
        }
    }

    private void setHaving(SelectStatement statement, String expression, ConditionType conditionType, Object value){
        if (this.havingStatemennts == null){
            this.havingStatemennts = new MutiSqlStatement(",");
        }
        SingleHavingStatement singleHavingStatement = new SingleHavingStatement(statement, expression, conditionType, value);

        this.havingStatemennts.addStatement(singleHavingStatement);
    }

    private void combineByWrapper(SqlStatement statement, StringBuilder sb, Map<String, Object> parameters, String preStr){
        if (statement != null){
            sb.append(preStr);
            SqlWrapper wrapper = statement.getSqlWrapper();
            sb.append(wrapper.getSql());
            parameters.putAll(wrapper.getParameters());
        }
    }

    private void init(Class<? extends BaseExtEntity> entityClass, TableIndentityProvider tableIndentityProvider){
        this.entityClass = entityClass;
        this.tableIndentityProvider = tableIndentityProvider;
        this.tableInfo = TableInfo.getTableInfo(this.entityClass);
        this.selectStatement = new SelectStatement(this.entityClass, this.tableIndentityProvider.getNextTableAsName());
    }

    private enum OperateType {
        SELECT,
        UPDATE,
        DELETE,
    }

    private class UpdateSetItem{
        private String propertyName;
        private Object propertyValue;
        private String targetTableName;
        private String targetPropertyName;
        private boolean useTargetTableProperty;

        public UpdateSetItem(String propertyName, Object propertyValue){
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
            this.useTargetTableProperty = false;
        }

        public UpdateSetItem(String propertyName, String targetTableName, String targetPropertyName){
            this.propertyName = propertyName;
            this.targetTableName = targetTableName;
            this.targetPropertyName = targetPropertyName;
            this.useTargetTableProperty = true;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Object getPropertyValue() {
            return propertyValue;
        }

        public String getTargetPropertyName() {
            return targetPropertyName;
        }

        public String getTargetTableName() {
            return targetTableName;
        }

        public boolean isUseTargetTableProperty() {
            return useTargetTableProperty;
        }
    }
}

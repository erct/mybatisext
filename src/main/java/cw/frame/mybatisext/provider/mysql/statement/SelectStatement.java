package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.ExpressionResult;
import cw.frame.mybatisext.base.ResultMap;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import cw.frame.mybatisext.base.entity.ColumnInfo;
import cw.frame.mybatisext.base.entity.TableInfo;

import java.util.*;

public class SelectStatement extends BaseSqlStatement {
    private Class<? extends BaseExtEntity> entityClassType;
    private Set<String> selectPropertyNames = new TreeSet<String >();
    private Map<String, String> selectPropertyNameMap = new HashMap<String, String>();
    private TableInfo tableInfo;
    private String aliasTableName;
    private Map<String, String> propertyResultMap = new HashMap<String, String>();
    private ResultMap resultMap;

    public SelectStatement(Class<? extends BaseExtEntity> entityClassType, String aliasTableName){
        this.entityClassType = entityClassType;
        this.aliasTableName = aliasTableName;
        this.tableInfo = TableInfo.getTableInfo(this.entityClassType);
        this.resultMap = new ResultMap(this.tableInfo);

        this.setResultMap(this.resultMap);
    }

    /**
     * 设置查询列
     * @param propertyNames propertyName, max(#{propertyName})等
     */
    public void select(String... propertyNames){
        for (String propertyName : propertyNames){
            if (propertyName.equals("*")){
                for (ColumnInfo columnInfo : this.tableInfo.getColumnns()){
                    this.selectPropertyNames.add(columnInfo.getPropertyName());
                }
                break;

            } else {
                this.selectPropertyNames.add(propertyName);
            }
        }

        this.unsetSqlWrapper();
    }

    /**
     * 设置查询列
     * @param propertyName propertyName, max(#{propertyName})等
     * @param resultPropertyName 结果返回存储属性字段
     */
    public void selectAs(String propertyName, String resultPropertyName){
        this.selectPropertyNameMap.put(propertyName, resultPropertyName);
    }

    /**
     * 设置查询列
     * @param propertyNameMap k:propertyName propertyName, max(#{propertyName})等,v:结果返回存储属性字段
     */
    public void selectAs(Map<String, String> propertyNameMap){
        this.selectPropertyNameMap.putAll(propertyNameMap);
    }

    public TableInfo getTableInfo(){
        return this.tableInfo;
    }

    public boolean hasSelectFields(){
        return this.selectPropertyNames.size() > 0;
    }

    public String getAliasTableName() {
        return aliasTableName;
    }

    public Map<String, String> getPropertyResultMap() {
        return propertyResultMap;
    }

    @Override
    protected void prepare(){
        StringBuilder sb = new StringBuilder();
        for (String propertyName : this.selectPropertyNames){
            this.buildSelectItemPartSql(sb, propertyName, null);
        }
        for (String propertyName : this.selectPropertyNameMap.keySet()){
            this.buildSelectItemPartSql(sb, propertyName, this.selectPropertyNameMap.get(propertyName));
        }

        // 查询所有字段
        if (sb.length() == 0){
            for (String propertyName : this.tableInfo.getPropertyNames()){
                this.buildSelectItemPartSql(sb, propertyName, null);
            }
        }

        String sql = sb.toString();

        this.setSqlWrapper(sql);
    }

    private void buildSelectItemPartSql(StringBuilder sb, String selectFieldString, String resultProperty){
        if (sb.length() != 0){
            sb.append(",");
        }

        ExpressionResult expressionResult = ExpressionExplain.explain(selectFieldString, this);
        ColumnInfo columnInfo = expressionResult.getColumn();

        if (expressionResult.isExpression()){
            sb.append(expressionResult.getResult());
        } else {
            sb.append(this.aliasTableName).append(".`");
            sb.append(columnInfo.getColumnName()).append("`");
        }

        String asName;
        String resultPropertyName;
        if (resultProperty == null || resultProperty.isEmpty()){
            asName = this.aliasTableName + "_" + columnInfo.getColumnName();
            resultPropertyName = columnInfo.getPropertyName();
        } else {
            ColumnInfo resultColumn = this.tableInfo.getColumnByPropertyName(resultProperty);
            resultPropertyName = resultProperty;
            if (resultColumn.isDbColumn()){
                asName = this.aliasTableName + "_" + resultColumn.getPropertyName();
            } else {
                asName = this.aliasTableName + "_" + resultPropertyName;
            }
        }

        sb.append(" ").append(asName);

        this.propertyResultMap.put(resultPropertyName, asName);
        this.resultMap.addResultMap(asName, resultPropertyName);
    }
}


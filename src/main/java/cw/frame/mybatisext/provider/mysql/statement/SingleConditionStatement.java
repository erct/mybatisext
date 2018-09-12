package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.SqlWrapper;
import cw.frame.mybatisext.base.entity.BaseExtEnum;
import cw.frame.mybatisext.enumeration.ConditionType;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class SingleConditionStatement extends BaseSqlStatement {
    private ConditionType conditionType;
    private String columnName;
    private Object propertyValue;
    private String tableName = "";

    public SingleConditionStatement(String tableName, String columnName, ConditionType conditionType, Object propertyValue){
        this.columnName = columnName;
        this.conditionType = conditionType;
        this.propertyValue = propertyValue;
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    @Override
    protected void prepare(){
        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder();
        String conditionColumnName = "`" + this.columnName + "`";

        if (!this.tableName.isEmpty()){
            conditionColumnName = this.tableName + "." + conditionColumnName;
        }

        switch (this.conditionType){
            case IN:
                sb.append(" find_in_set (").append(conditionColumnName).append(",");
                break;
            case NOT_IN:
                sb.append(" not find_in_set (").append(conditionColumnName).append(",");
                break;
            case EQUAL:
                sb.append(conditionColumnName);
                sb.append("=");
                break;
            case NOT_EQUAL:
                sb.append(conditionColumnName);
                sb.append("<>");
                break;
            case GREATER:
                sb.append(conditionColumnName);
                sb.append(">");
                break;
            case GREATER_AND_EQUAL:
                sb.append(conditionColumnName);
                sb.append(">=");
                break;
            case LESS:
                sb.append(conditionColumnName);
                sb.append("<");
                break;
            case LESS_AND_EQUAL:
                sb.append(conditionColumnName);
                sb.append("<=");
                break;
            case LIKE:
                sb.append(conditionColumnName);
                sb.append(" like ");
                break;
            case NOT_LIKE:
                sb.append(conditionColumnName);
                sb.append(" not like ");
                break;
        }

        if (this.propertyValue instanceof SqlStatement){
            SqlStatement sqlStatement = (SqlStatement) this.propertyValue;
            SqlWrapper sqlWrapper = sqlStatement.getSqlWrapper();
            String sql = sqlWrapper.getSql();
            sb.append(sql);
            parameters.putAll(sqlWrapper.getParameters());
        } else {
            String parameterName = this.getParameterName(this.tableName, this.columnName);
            sb.append(parameterName);
            if (this.conditionType == ConditionType.IN || this.conditionType == ConditionType.NOT_IN){
                if (this.propertyValue.getClass().isArray()){
                    StringBuilder sbValue = new StringBuilder();
                    for (int i=0; i<Array.getLength(this.propertyValue); i++){
                        if (sbValue.length() > 0){
                            sbValue.append(",");
                        }
                        sbValue.append(Array.get(this.propertyValue, i));
                    }
                    parameters.put(parameterName, this.getPropertyValue(this.propertyValue).toString());
                } else {
                    parameters.put(parameterName, this.getPropertyValue(this.propertyValue));
                }
            } else {
                parameters.put(parameterName, this.getPropertyValue(this.propertyValue));
            }
        }

        if (this.conditionType == ConditionType.IN || this.conditionType == ConditionType.NOT_IN){
            sb.append(")");
        }

        this.setSqlWrapper(new SqlWrapper(sb.toString(), parameters));
    }

    private Object getPropertyValue(Object propertyValue){
        if (propertyValue instanceof BaseExtEnum){
            return ((BaseExtEnum) propertyValue).getValue();
        } else {
            return propertyValue;
        }
    }
}

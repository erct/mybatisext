package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.enumeration.ConditionType;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class SingleHavingStatement extends BaseSqlStatement {

    private SelectStatement selectStatement;
    private ConditionType conditionType;
    private String expressionn;
    private Object value;

    public SingleHavingStatement(SelectStatement selectStatement, String expression, ConditionType conditionType, Object value){
        this.selectStatement = selectStatement;
        this.expressionn = expression;
        this.conditionType = conditionType;
        this.value = value;
    }

    @Override
    protected void prepare(){
        ExpressionResult expressionResult = ExpressionExplain.explain(this.expressionn, selectStatement);
        if (expressionResult.isExpression()){
            Map<String, Object> parameters = new HashMap<String, Object>();
            StringBuilder sb = new StringBuilder();
            sb.append(expressionResult.getResult());
            switch (this.conditionType){
                case EQUAL:
                    sb.append("=");
                    break;
                case GREATER:
                    sb.append(">");
                    break;
                case GREATER_AND_EQUAL:
                    sb.append(">=");
                    break;
                case LESS:
                    sb.append("<");
                    break;
                case LESS_AND_EQUAL:
                    sb.append("<=");
                    break;
                case NOT_EQUAL:
                    sb.append("<>");
                    break;
                case IN:
                    sb.append(" find_in_set (");
                    break;
                case NOT_IN:
                    sb.append(" not find_in_set (");
                    break;
            }
            sb.append(this.expressionn);
            if (this.conditionType == ConditionType.IN || this.conditionType == ConditionType.NOT_IN){
                if (this.value.getClass().isArray()){
                    StringBuilder sbValue = new StringBuilder();
                    for (int i = 0; i<Array.getLength(this.value); i++){
                        if (sbValue.length() > 0){
                            sbValue.append(",");
                        }
                        sbValue.append(Array.get(this.value, i));
                    }
                    parameters.put(this.expressionn, sbValue.toString());
                } else {
                    parameters.put(this.expressionn, this.value);
                }
                sb.append(")");
            } else {
                parameters.put(this.expressionn, this.value);
            }

            this.setSqlWrapper(sb.toString(), parameters);
        }
    }
}

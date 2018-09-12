package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.SqlWrapper;
import cw.frame.mybatisext.enumeration.JoinType;
import cw.frame.mybatisext.provider.mysql.MySqlStatement;

import java.util.HashMap;
import java.util.Map;

public class SingleJoinStatement extends BaseSqlStatement {

    private SelectStatement fromSelectStatement;
    private Object toStatement;
    private JoinType joinType;
    private Map<String, String> joinRelationshipMap = new HashMap<String, String>();

    public SingleJoinStatement(SelectStatement fromSelectStatement, SelectStatement toSelectStatement, JoinType joinType){
        this.fromSelectStatement = fromSelectStatement;
        this.toStatement = toSelectStatement;
        this.joinType = joinType;
    }

    public SingleJoinStatement(SelectStatement fromSelectStatement, MySqlStatement toMySqlStatement, JoinType joinType){
        this.fromSelectStatement = fromSelectStatement;
        this.toStatement = toMySqlStatement;
        this.joinType = joinType;
    }

    public void setRelationship(String fromPropertyName, String toPropertyName){
        this.joinRelationshipMap.put(fromPropertyName, toPropertyName);
    }

    @Override
    protected void prepare(){
        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<String, Object>();

        SelectStatement toSelectStatement = null;
        MySqlStatement toMySqlStatement = null;

        for (String fromPropertyName : this.joinRelationshipMap.keySet()){
            String toPropertyName = this.joinRelationshipMap.get(fromPropertyName);
            if (sb.length() == 0){
                sb.append(this.getJoinTypeSqlString(this.joinType)).append(" ");
                if (this.toStatement instanceof SelectStatement){
                    toSelectStatement = (SelectStatement)this.toStatement;

                    String toTableAsName = toSelectStatement.getAliasTableName();
                    sb.append("`").append(toSelectStatement.getTableInfo().getTableName()).append("`");
                    sb.append(" ").append(toTableAsName);
                } else {
                    // 连接子查询
                    toMySqlStatement = (MySqlStatement)this.toStatement;
                    String toTableAsName = toMySqlStatement.getSelectStatement().getAliasTableName();

                    SqlWrapper wrapper = toMySqlStatement.getSqlWrapper();
                    parameters.putAll(wrapper.getParameters());

                    sb.append("(").append(wrapper.getSql()).append(")");
                    sb.append(" ").append(toTableAsName);
                }
                sb.append(" on ");
            } else {
                sb.append(" and ");
            }

            sb.append(this.fromSelectStatement.getAliasTableName()).append(".`");
            sb.append(this.fromSelectStatement.getTableInfo().getColumnByPropertyName(fromPropertyName).getColumnName());
            sb.append("`=");

            if (toSelectStatement != null){
                sb.append(toSelectStatement.getAliasTableName()).append(".").append("`");
                sb.append(toSelectStatement.getTableInfo().getColumnByPropertyName(toPropertyName).getColumnName());
                sb.append("`");
            } else {
                sb.append(toMySqlStatement.getSelectStatement().getAliasTableName());
                sb.append(".`");
                sb.append(toMySqlStatement.getSelectStatement().getPropertyResultMap().get(toPropertyName));
                sb.append("`");
            }
        }


        this.setSqlWrapper(new SqlWrapper(sb.toString(), parameters));
    }

    private String getJoinTypeSqlString(JoinType joinType){
        String str;
        switch (joinType){
            case LEFT_JOIN:
                str = "left join";
                break;
            case INNER_JOIN:
                str = "inner join";
                break;
            case RIGHT_JOIN:
                str = "right join";
                break;
            default:
                str = "inner join";
                break;
        }

        return str;
    }
}

package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.SqlWrapper;
import cw.frame.mybatisext.enumeration.OrderType;

public class SingleOrderStatement extends BaseSqlStatement {
    private String columnName;
    private String tableName = "";
    private OrderType orderType = OrderType.DESCENDING;

    private SqlWrapper sqlWrapper;

    public SingleOrderStatement(String columnName){
        this.columnName = columnName;
    }

    public SingleOrderStatement(String columnName, String tableName){
        this.columnName = columnName;
        this.tableName = tableName;
    }

    public SingleOrderStatement(String columnName, OrderType orderType){
        this.columnName = columnName;
        this.orderType = orderType;
    }

    public SingleOrderStatement(String columnName, OrderType orderType, String tableName){
        this.columnName = columnName;
        this.orderType = orderType;
        this.tableName = tableName;
    }

    @Override
    protected void prepare(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.tableName).append(".`");
        sb.append(this.columnName).append("`");
        if (this.orderType == OrderType.DESCENDING){
            sb.append(" desc");
        }

        this.setSqlWrapper(new SqlWrapper(sb.toString()));
    }
}

package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.SqlWrapper;

public class SingleGroupStatement extends BaseSqlStatement {
    private String columnName;
    private String tableName = "";

    private SqlWrapper sqlWrapper;

    public SingleGroupStatement(String columnName){
        this.columnName = columnName;
    }

    public SingleGroupStatement(String columnName, String tableName){
        this.columnName = columnName;
        this.tableName = tableName;
    }

    @Override
    protected void prepare(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.tableName).append(".`");
        sb.append(this.columnName).append("`");

        this.setSqlWrapper(new SqlWrapper(sb.toString()));
    }
}

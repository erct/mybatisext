package cw.frame.mybatisext.provider.mysql;

import cw.frame.mybatisext.base.entity.ColumnInfo;

public class ExpressionResult {

    private ColumnInfo columnInfo;
    private String result;
    private boolean isExpression;

    public ExpressionResult(String result, ColumnInfo columnInfo, boolean isExpression){
        this.result = result;
        this.columnInfo = columnInfo;
        this.isExpression = isExpression;
    }

    public ColumnInfo getColumn() {
        return this.columnInfo;
    }

    public String getResult() {
        return this.result;
    }

    public boolean isExpression() {
        return this.isExpression;
    }
}

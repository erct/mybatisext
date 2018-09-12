package cw.frame.mybatisext;

import cw.frame.mybatisext.base.ExpressionResult;
import cw.frame.mybatisext.provider.mysql.statement.SelectStatement;

public interface ExpressionExplain {

    public ExpressionResult explain(String expression, SelectStatement selectStatement);
}

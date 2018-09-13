package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.ExpressionResult;
import cw.frame.mybatisext.base.entity.ColumnInfo;
import cw.frame.mybatisext.base.entity.TableInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionExplain {

    private static Pattern pattern = Pattern.compile("#\\{\\w+\\}");

    public static ExpressionResult explain(String expression, SelectStatement selectStatement) {
        String result;
        boolean isExpression;
        ColumnInfo columnInfo;

        Matcher matcher = pattern.matcher(expression);
        TableInfo tableInfo = selectStatement.getTableInfo();

        if (matcher.find()) {
            StringBuffer buffer = new StringBuffer();
            String param = matcher.group();

            String propertyName = matcher.group().substring(2, param.length() - 1);
            columnInfo = tableInfo.getColumnByPropertyName(propertyName);

            matcher.appendReplacement(buffer, selectStatement.getAliasTableName() + "." + columnInfo.getColumnName());

            matcher.appendTail(buffer);

            result = buffer.toString();
            isExpression = true;
        } else {
            columnInfo = tableInfo.getColumnByPropertyName(expression);
            result = expression;
            isExpression = false;
        }

        return new ExpressionResult(result, columnInfo, isExpression);
    }
}

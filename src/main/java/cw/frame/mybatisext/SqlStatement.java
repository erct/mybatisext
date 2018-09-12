package cw.frame.mybatisext;

import cw.frame.mybatisext.base.FormatSqlWrapper;
import cw.frame.mybatisext.base.ResultMap;
import cw.frame.mybatisext.base.SqlWrapper;

import java.util.List;

public interface SqlStatement {

    public SqlWrapper getSqlWrapper();

    public ResultMap getResultMap();

    public FormatSqlWrapper getFormatSqlWrapper();

    default String getFormatSqlReplacement(){
        return "?";
    }
}

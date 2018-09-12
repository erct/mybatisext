package cw.frame.mybatisext.provider.mysql.statement;

import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.SqlWrapper;

import java.util.ArrayList;
import java.util.List;

public class MutiSqlStatement extends BaseSqlStatement {

    private List<SqlStatement> statements;
    private String joinStr;

    public MutiSqlStatement(String joinStr){
        this.statements = new ArrayList<SqlStatement>();
        this.joinStr = joinStr;
    }

    public MutiSqlStatement(List<SqlStatement> statements, String joinStr){
        this.statements = statements;
        this.joinStr = joinStr;
    }

    public MutiSqlStatement addStatement(SqlStatement statement){
        this.statements.add(statement);
        this.unsetSqlWrapper();

        return this;
    }

    @Override
    protected void prepare(){
        SqlWrapper wrapper = this.combineSqlWrappers(this.statements, this.joinStr);

        this.setSqlWrapper(wrapper);
    }
}

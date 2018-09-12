package cw.frame.mybatisext.base;

public class TableIndentityProvider {
    private int tableIndentity = 0;

    public String getNextTableAsName(){
        this.tableIndentity++;
        return this.getCurrentTableAsName();
    }

    public String getCurrentTableAsName(){
        return "t" + this.tableIndentity;
    }
}

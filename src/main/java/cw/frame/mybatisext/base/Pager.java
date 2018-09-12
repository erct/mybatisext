package cw.frame.mybatisext.base;

public class Pager {
    private int pageSize;
    private int currentPage;
    private int rowCount;
    private int pageCount;

    public Pager(){}

    public Pager(int pageSize){
        this(pageSize, 1);
    }

    public Pager(int pageSize, int currentPage){
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;

        this.pageCount = (this.rowCount - 1) % this.pageSize + 1;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("rowCount=").append(this.rowCount).append(", ");
        sb.append("pageSize=").append(this.pageSize).append(", ");
        sb.append("currentPage=").append(this.currentPage).append(", ");
        sb.append("pageCount=").append(this.pageCount).append("}");

        return sb.toString();
    }
}

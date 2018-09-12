package cw.frame.mybatisext.base.entity;

import java.lang.reflect.Field;

public class ColumnInfo {
    private TableInfo table;
    private String propertyName;
    private String columnName;
    private boolean isPrimaryKey = false;
    private boolean isGeneratedKey = true;
    private boolean isDbColumn = true;
    private Field field;

    public static ColumnInfo createDbColumn(TableInfo table, String propertyName, String columnName, boolean isPrimaryKey, boolean isGeneratedKey, Field field){
        return new ColumnInfo(table, propertyName, columnName, isPrimaryKey, isGeneratedKey, field);
    }

    public static ColumnInfo createNormalColumn(TableInfo table, String propertyName, Field field){
        return new ColumnInfo(table, propertyName, field);
    }

    private ColumnInfo(TableInfo table, String propertyName, String columnName, boolean isPrimaryKey, boolean isGeneratedKey, Field field){
        this.table = table;
        this.propertyName = propertyName;
        this.columnName = columnName;
        this.isPrimaryKey = isPrimaryKey;
        this.isGeneratedKey = isGeneratedKey;
        this.isDbColumn = true;
        this.field = field;
    }

    private ColumnInfo(TableInfo table, String propertyName, Field field){
        this.table = table;
        this.propertyName = propertyName;
        this.isDbColumn = false;
        this.field = field;
    }

    public TableInfo getTable() {
        return table;
    }

    public void setTable(TableInfo table) {
        this.table = table;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean getIsPrimaryKey(){
        return this.isPrimaryKey;
    }

    public boolean isGeneratedKey(){
        return this.isGeneratedKey;
    }

    public boolean isDbColumn(){
        return this.isDbColumn;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

}

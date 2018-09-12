package cw.frame.mybatisext.base.entity;

import java.lang.reflect.Field;

public class RelationshipInfo {
    private TableInfo primaryTable;
    private Field field;
    private String foreignKey;
    private String propertyKey;
    private boolean oneOne;
    private Class<? extends BaseExtEntity> relationTableEntityClass;

    public static RelationshipInfo createOneOneRelationship(TableInfo primaryTable, Field field, Class<? extends BaseExtEntity> relationTableEntityClass, String propertyKey, String foreignKey){
        return new RelationshipInfo(primaryTable, field, relationTableEntityClass, propertyKey, foreignKey, true);
    }

    public static RelationshipInfo createOneManyRelationship(TableInfo primaryTable, Field field, Class<? extends BaseExtEntity> relationTableEntityClass, String propertyKey, String foreignKey){
        return new RelationshipInfo(primaryTable, field, relationTableEntityClass, propertyKey, foreignKey, false);
    }

    private RelationshipInfo(TableInfo primaryTable, Field field, Class<? extends BaseExtEntity> relationTableEntityClass, String propertyKey, String foreignKey, boolean oneOne){
        this.primaryTable = primaryTable;
        this.field = field;
        this.relationTableEntityClass = relationTableEntityClass;
        this.foreignKey = foreignKey;
        this.propertyKey = propertyKey;
        this.oneOne = oneOne;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public TableInfo getPrimaryTable() {
        return primaryTable;
    }

    public Field getField() {
        return field;
    }

    public TableInfo getSubTable() {
        return TableInfo.getTableInfo(this.relationTableEntityClass);
    }

    public boolean isOneOne() {
        return oneOne;
    }

    public boolean isOneMay(){
        return !oneOne;
    }
}

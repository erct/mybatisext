package cw.frame.mybatisext.base.entity;

import cw.frame.mybatisext.annotation.Column;
import cw.frame.mybatisext.annotation.OneMany;
import cw.frame.mybatisext.annotation.OneOne;
import cw.frame.mybatisext.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class TableInfo {
    private static Map<String, TableInfo> tableMap = new Hashtable<String, TableInfo>();

    private String tableName;
    private String entityFullName;
    private Class<? extends BaseExtEntity> entityClass;
    private ColumnInfo primaryKeyColumnInfo;

    private Map<String, ColumnInfo> propertyMap = new HashMap<String, ColumnInfo>();
    private Map<String, ColumnInfo> columnNameMap = new HashMap<String, ColumnInfo>();
    private Map<String, RelationshipInfo> oneOneMap = new HashMap<String, RelationshipInfo>();
    private Map<String, RelationshipInfo> oneManyMap = new HashMap<String, RelationshipInfo>();

    private TableInfo(Class<? extends BaseExtEntity> entityClass){
        this.entityClass = entityClass;
        this.tableName = this.getTableName(entityClass);
        this.entityFullName = entityClass.getName();

        this.initTableInfo();

        tableMap.put(this.entityFullName, this);
    }

    public static TableInfo getTableInfo(Class<? extends BaseExtEntity> entityClass){
        return getTableInfo(entityClass.getName());
    }

    public static TableInfo getTableInfo(String className){
        if (!tableMap.containsKey(className)){
            try{
                Class entityClass = Class.forName(className);
                if (BaseExtEntity.class.isAssignableFrom(entityClass)){
                    tableMap.put(className, new TableInfo(entityClass));
                } else {
                    return null;
                }
            } catch (ClassNotFoundException ex){
                return null;
            }
        }

        return tableMap.get(className);
    }

    public static void setFieldValue(Object entity, String propertyName, Object value) throws Throwable{
        TableInfo tableInfo = TableInfo.getTableInfo(entity.getClass().getName());
        Field field = tableInfo.getColumnByPropertyName(propertyName).getField();

        setFieldValue(entity, field, value);
    }

    public static void setFieldValue(Object entity, Field field, Object value) throws Throwable{
        field.setAccessible(true);
        String type = field.getGenericType().toString();

        if (type.equals("class java.lang.String")){
            field.set(entity, value.toString());
        } else if (type.equals("class java.lang.Integer")){
            field.set(entity, Integer.valueOf(value.toString()));
        } else if (type.equals("int")){
            field.set(entity, Integer.valueOf(value.toString()));
        } else if (type.equals("boolean")){
            field.set(entity, (boolean)value);
        } else if (type.equals("class java.lang.Boolean")){
            field.set(entity, (Boolean)value);
        } else if (type.equals("class java.util.Date")){
            field.set(entity, (Date)value);
        } else if (type.equals("class java.lang.Long")){
            field.set(entity, (Long)value);
        } else if (type.equals("class java.lang.Short")){
            field.set(entity, (Short)value);
        } else if (type.equals("class java.lang.Byte")){
            field.set(entity, (Byte)value);
        } else if (type.equals("class java.lang.Float")){
            field.set(entity, (Float)value);
        } else if (type.equals("class java.lang.Double")){
            field.set(entity, (Double)value);
        } else {
            Class clazz = Class.forName(field.getGenericType().getTypeName());
            if (clazz.isEnum() && BaseExtEnum.class.isAssignableFrom(clazz)){
                BaseExtEnum baseExtEnums[] = (BaseExtEnum[]) clazz.getMethod("values").invoke(null, null);
                String val = value.toString();
                for (BaseExtEnum baseExtEnum : baseExtEnums){
                    if (baseExtEnum.getValue().toString().equals(val)){
                        field.set(entity, baseExtEnum);
                        break;
                    }
                }
            }
        }

        field.setAccessible(false);
    }

    public static Object getFieldValue(Object entity, Field field) throws Throwable{
        field.setAccessible(true);
        Object val = field.get(entity);
        field.setAccessible(false);

        return val;
    }

    public static Object getFieldValue(Object entity, String propertyName) throws Throwable{
        TableInfo tableInfo = TableInfo.getTableInfo(entity.getClass().getName());
        Field field = tableInfo.getColumnByPropertyName(propertyName).getField();

        return getFieldValue(entity, field);
    }

    public String getTableName() {
        return tableName;
    }

    public Class<? extends BaseExtEntity> getTableEntityClass(){
        return this.entityClass;
    }

    public ColumnInfo getPrimaryKeyColumn(){
        return this.primaryKeyColumnInfo;
    }

    /**
     * 根据属性字段获取ColumnnInfo对象，包括非数据库字段
     * @param propertyName  属性字段名
     * @return  ColumnInfo
     */
    public ColumnInfo getColumnByPropertyName(String propertyName){
        return this.propertyMap.getOrDefault(propertyName, null);
    }

    public ColumnInfo getColumnByColumnName(String columnName){
        return this.columnNameMap.getOrDefault(columnName, null);
    }

    /**
     * 根据属性字段获取ColumnnInfo对象，包括非数据库字段
     * @param propertyNames 多个属性字段名
     * @return
     */
    public Collection<ColumnInfo> getColumnnsByPropertyNames(String[] propertyNames){
        return this.getColumnnsNames(propertyNames, this.propertyMap);
    }

    public Collection<ColumnInfo> getColumnnsByColumnyNames(String[] columnNames){
        return this.getColumnnsNames(columnNames, this.columnNameMap);
    }

    public Collection<ColumnInfo> getColumnns(){
        return this.propertyMap.values();
    }

    public Collection<String> getPropertyNames(){
        return this.propertyMap.keySet();
    }

    public Collection<String> getColumnNames(){
        return this.columnNameMap.keySet();
    }

    public Map<String, RelationshipInfo> getOneManyMap() {
        return oneManyMap;
    }

    public Map<String, RelationshipInfo> getOneOneMap() {
        return oneOneMap;
    }

    public boolean isOneOne(String propertyName){
        return this.oneOneMap.containsKey(propertyName);
    }

    public boolean isOneMany(String propertyName){
        return this.oneManyMap.containsKey(propertyName);
    }

    public RelationshipInfo getRelationshipInfo(String propertyName) {
        if (this.isOneOne(propertyName)){
            return this.getOneOneMap().get(propertyName);
        } else if (this.isOneMany(propertyName)){
            return this.getOneManyMap().get(propertyName);
        } else {
            throw new IllegalArgumentException("propertyName error: " + propertyName);
        }
    }

    private Collection<ColumnInfo> getColumnnsNames(String[] names, Map<String, ColumnInfo> map){
        if (names == null || names.length == 0){
            return this.getColumnns();
        }

        List<ColumnInfo> columnInfoList = new ArrayList<ColumnInfo>();
        for (String name : names){
            columnInfoList.add(map.get(name));
        }

        return columnInfoList;
    }

    private void initTableInfo(){
        this.propertyMap = new HashMap<String, ColumnInfo>();
        this.columnNameMap = new HashMap<String, ColumnInfo>();

        Class<?> classType = this.entityClass;
        while (true){
            for (Field field : classType.getDeclaredFields()){
                ColumnInfo columnInfo = null;

                if (!this.isEntityColumn(field)){
                    Type type = field.getGenericType();
                    String typeName = type.getTypeName();
                    if (typeName.equals("class java.lang.String")
                            || typeName.equals("class java.lang.Integer")
                            || typeName.equals("int")
                            || typeName.equals("boolean")
                            || typeName.equals("class java.lang.Boolean")
                            || typeName.equals("class java.util.Date")
                            || typeName.equals("class java.lang.Long")
                            || typeName.equals("class java.lang.Short")
                            || typeName.equals("class java.lang.Byte")
                            || typeName.equals("class java.lang.Float")
                            || typeName.equals("class java.lang.Double")
                            ) {
                        columnInfo = ColumnInfo.createNormalColumn(this, field.getName(), field);
                    } else {
                        OneOne oneOne = field.getAnnotation(OneOne.class);
                        OneMany oneMany = field.getAnnotation(OneMany.class);
                        if (oneOne != null){
                            RelationshipInfo relationshipInfo = RelationshipInfo.createOneOneRelationship(this, field, oneOne.type(), oneOne.propertyKey(), oneOne.foreignKey());
                            this.oneOneMap.put(field.getName(), relationshipInfo);
                        } else if (oneMany != null){
                            RelationshipInfo relationshipInfo = RelationshipInfo.createOneManyRelationship(this, field, oneMany.type(), oneMany.propertyKey(), oneMany.foreignKey());
                            this.oneManyMap.put(field.getName(), relationshipInfo);
                        }

                    }
                } else {
                    columnInfo = this.buildColumnInfo(field);
                    if (columnInfo.getIsPrimaryKey()){
                        this.primaryKeyColumnInfo = columnInfo;
                        this.columnNameMap.put(columnInfo.getColumnName(), columnInfo);
                    }
                }

                if (columnInfo != null){
                    this.propertyMap.put(columnInfo.getPropertyName(), columnInfo);
                }

            }

            if (classType.equals(BaseExtEntity.class)){
                break;
            }

            classType = classType.getSuperclass();
        }
    }

    private boolean isEntityColumn(Field field){
        boolean isColumn = true;

        Column column = field.getAnnotation(Column.class);
        if (column == null){
            isColumn = false;
        }

        return isColumn;
    }

    private ColumnInfo buildColumnInfo(Field field){
        Column column = field.getAnnotation(Column.class);

        String columnName = column.value();
        boolean isPrimaryKey = column.primaryKey();
        boolean isGeneratedKey = isPrimaryKey && column.generatedKey();
        String propertyName = field.getName();

        if (columnName == null || columnName.isEmpty()){
            columnName = propertyName;
        }

        return ColumnInfo.createDbColumn(this, propertyName, columnName, isPrimaryKey, isGeneratedKey, field);
    }

    private String getTableName(Class<?> entityClass){
        String tableName = entityClass.getSimpleName();

        Table table = entityClass.getAnnotation(Table.class);

        if (table != null){
            tableName = table.value();
            if (tableName == null || tableName.isEmpty()){
                tableName = entityClass.getSimpleName();
            }
        }

        return tableName;
    }
}

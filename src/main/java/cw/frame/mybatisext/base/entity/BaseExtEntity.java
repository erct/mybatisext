package cw.frame.mybatisext.base.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

public abstract class BaseExtEntity {

    private Logger logger= LoggerFactory.getLogger(getClass());

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        TableInfo tableInfo = TableInfo.getTableInfo(this.getClass());
        for (ColumnInfo columnInfo : tableInfo.getColumnns()){
            if (sb.length() > 0){
                sb.append(", ");
            }
            Field field = columnInfo.getField();
            sb.append(columnInfo.getPropertyName()).append("=");
            sb.append(this.getFieldValue(field));
        }

        int i = 0;
        for (RelationshipInfo relationshipInfo : tableInfo.getOneOneMap().values()){
            if (i > 0){
                sb.append(", ");
            }
            sb.append(relationshipInfo.getPropertyKey()).append("=");
            sb.append(this.getFieldValue(relationshipInfo.getField()));
        }

        i = 0;
        for (RelationshipInfo relationshipInfo : tableInfo.getOneManyMap().values()){
            if (i > 0){
                sb.append(",");
            }
            sb.append(relationshipInfo.getPropertyKey()).append("=");
            sb.append("[");

            Object obj = this.getFieldValue(relationshipInfo.getField());
            if (obj != null){
                List values = (List)obj;
                for (int j=0; j<values.size(); j++){
                    if (j > 0){
                        sb.append(", ");
                    }
                    sb.append(values.get(j));
                }
            }

            sb.append("]");
        }

        sb.insert(0, "{").append("}");

        return sb.toString();
    }

    private Object getFieldValue(Field field){
        Object val = null;
        field.setAccessible(true);
        try{
            val = field.get(this);
        }
        catch (Exception ex){
            this.logger.error(ex.getMessage());
        }

        return val;
    }
}

package cw.frame.mybatisext.provider.mysql.interceptorhandler;

import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MetaObjectHelper {
    private MetaObject metaObject;
    private String keyPre = "";

    public MetaObjectHelper(StatementHandler statementHandler){
        this.metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        this.keyPre = "delegate.";
    }

    public MetaObjectHelper(ResultSetHandler resultSetHandler){
        this.metaObject = MetaObject.forObject(resultSetHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        this.keyPre = "";
    }

    public ParameterHandler getParameterHandler(){
        return (ParameterHandler)this.metaObject.getValue(this.keyPre + "parameterHandler");
    }

    public SqlCommandType getSqlCommandType(){
        return this.getMappedStatement().getSqlCommandType();
    }

    public Object getParameterObject(){
        return this.getParameterHandler().getParameterObject();
    }

    public Object getParameterObject(String paramName){
        Object object = this.getParameterObject();
        if (object instanceof MapperMethod.ParamMap){
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)object;
            if (paramMap.keySet().contains(paramName)){
                return paramMap.get(paramName);
            }
        }

        return null;
    }

    public MappedStatement getMappedStatement(){
        return (MappedStatement)this.metaObject.getValue(this.keyPre + "mappedStatement");
    }

    public String getMapperId(){
        return this.getMappedStatement().getId();
    }

    public SqlStatement getSqlStatement(){
        Object parameterObject = this.getParameterObject();

        if (parameterObject instanceof MapperMethod.ParamMap){
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)parameterObject;
            if (paramMap.keySet().contains("sqlStatement")){
                return  (SqlStatement) paramMap.get("sqlStatement");
            }
        }
        return null;
    }

    public Class<? extends BaseExtEntity> getEntityClassType() throws Throwable{
        Class<? extends BaseExtEntity> entityClass = null;
        String mapperId = this.getMapperId();
        String className = mapperId.substring(0, mapperId.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);

        for (Type type : clazz.getGenericInterfaces()){
            ParameterizedType parameterizedType = (ParameterizedType)type;
            for (Type argumentsType : parameterizedType.getActualTypeArguments()){
                Class argumentClass = Class.forName(argumentsType.getTypeName());
                if (BaseExtEntity.class.isAssignableFrom(argumentClass)){
                    entityClass = argumentClass;
                    break;
                }
            }

            if (entityClass != null){
                break;
            }
        }

        return entityClass;
    }

    public void setSql(String sql){
        this.metaObject.setValue(this.keyPre + "boundSql.sql", sql);
    }
}

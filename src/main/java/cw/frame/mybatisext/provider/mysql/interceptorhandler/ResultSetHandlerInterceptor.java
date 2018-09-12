package cw.frame.mybatisext.provider.mysql.interceptorhandler;

import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.BaseSqlStatement;
import cw.frame.mybatisext.base.ResultMap;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import cw.frame.mybatisext.base.entity.ColumnInfo;
import cw.frame.mybatisext.base.entity.RelationshipInfo;
import cw.frame.mybatisext.base.entity.TableInfo;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class ResultSetHandlerInterceptor extends BaseInterceptorHandler {

    public Object handleResultSets(Invocation invocation) throws Throwable{
        Object target = invocation.getTarget();
        ResultSetHandler resultSetHandler = (ResultSetHandler) target;

        MetaObjectHelper metaObjectHelper = new MetaObjectHelper(resultSetHandler);
        String operateSql = metaObjectHelper.getMappedStatement().getSqlSource().getBoundSql(null).getSql();

        if (!this.needInterceptor(operateSql)){
            return invocation.proceed();
        }

        Statement stmt =  (Statement) invocation.getArgs()[0];
        ResultSet rs = stmt.getResultSet();

        if (operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_BY_ID) || operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_BY_IDS)){
            return this.getResults(rs, metaObjectHelper.getEntityClassType());
        } else if (
                operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_ONE)
                        || operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_MANY)
                        || operateSql.equals(BaseSqlStatement.OPERATE_TYPE_GET_PAGE)
                ) {
            SqlStatement sqlStatement = (SqlStatement) metaObjectHelper.getParameterObject(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT);
            return this.getResults(rs, sqlStatement.getResultMap(), metaObjectHelper.getEntityClassType());
        }

        return invocation.proceed();
    }

    private <Entity extends BaseExtEntity> List<Entity> getResults(ResultSet rs, Class<Entity> entityClass) throws Throwable{
        TableInfo tableInfo = TableInfo.getTableInfo(entityClass);
        ResultMap resultMap = this.buildDefualtResultMap(tableInfo);

        return this.getResults(rs, resultMap, entityClass);
    }

    private <Entity extends BaseExtEntity> List<Entity> getResults(ResultSet rs, ResultMap resultMap, Class<Entity> entityClass) throws Throwable{
        List<Entity> results = new ArrayList<Entity>();
        Entity lastEntity = null;

        while (rs.next()){
            Entity entity = this.getRowDataEntity(rs, lastEntity, resultMap, entityClass);

            if (entity != null){
                results.add(entity);
                lastEntity = entity;
            }
        }

        return results;
    }

    private <Entity extends BaseExtEntity> Entity getRowDataEntity(ResultSet rs, Object lastEntity, ResultMap resultMap, Class<Entity> entityClass) throws Throwable{
        Entity entity = entityClass.getConstructor().newInstance();

        TableInfo tableInfo = resultMap.getTableInfo();
        Map<String, String> map = resultMap.getResultMap();

        boolean sameAllValue = true;
        if (lastEntity == null){
            sameAllValue = false;
        }

        for (String resultColumnName : map.keySet()){
            String propertyName = map.get(resultColumnName);
            Object propertyValue = rs.getObject(resultColumnName);
            TableInfo.setFieldValue(entity, propertyName, propertyValue);

            if (lastEntity != null && sameAllValue){
                if (!TableInfo.getFieldValue(entity, propertyName).equals(TableInfo.getFieldValue(lastEntity, propertyName))){
                    sameAllValue = false;
                }
            }
        }

        if (sameAllValue){
            entity = null;
        } else {
            lastEntity = entity;
        }

        if (resultMap.hasSubResultMap()){
            Map<String, ResultMap> subResultMaps = resultMap.getSubResultMap();
            for (String relationshipPropertyKey : subResultMaps.keySet()){
                ResultMap subResultMap = subResultMaps.get(relationshipPropertyKey);
                TableInfo subTableInfo = subResultMap.getTableInfo();
                RelationshipInfo relationshipInfo = tableInfo.getRelationshipInfo(relationshipPropertyKey);
                Field relationshipField = relationshipInfo.getField();
                relationshipField.setAccessible(true);

                if (relationshipInfo.isOneOne()){
                    if (!sameAllValue){
                        relationshipField.set(entity, this.getRowDataEntity(rs, null, subResultMap, subTableInfo.getTableEntityClass()));
                    }
                }
                if (relationshipInfo.isOneMay()){
                    if (!sameAllValue){
                        List subEntities = new ArrayList();
                        Object subEntity = this.getRowDataEntity(rs, null, subResultMap, subTableInfo.getTableEntityClass());
                        subEntities.add(subEntity);
                        relationshipField.set(entity, subEntities);
                    } else {
                        List relationshipValue = (List)relationshipField.get(lastEntity);
                        Object lastSubEntity = relationshipValue.get(relationshipValue.size() - 1);
                        Object subEntity = this.getRowDataEntity(rs, lastSubEntity, subResultMap, subTableInfo.getTableEntityClass());
                        if (subEntity != null){
                            relationshipValue.add(subEntity);
                        }
                    }
                }
                relationshipField.setAccessible(false);
            }
        }

        return entity;
    }

    private ResultMap buildDefualtResultMap(TableInfo tableInfo){
        ResultMap resultMap = new ResultMap(tableInfo);
        for (ColumnInfo columnInfo : tableInfo.getColumnns()){
            resultMap.addResultMap(columnInfo.getColumnName(), columnInfo.getPropertyName());
        }

        return resultMap;
    }

}

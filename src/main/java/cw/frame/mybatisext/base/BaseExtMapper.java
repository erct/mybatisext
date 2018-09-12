package cw.frame.mybatisext.base;

import cw.frame.mybatisext.SqlStatement;
import cw.frame.mybatisext.base.entity.BaseExtEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BaseExtMapper<Entity extends BaseExtEntity> {

    @Insert(BaseSqlStatement.OPERATE_TYPE_ADD_ONE)
    public int addOne(@Param(BaseSqlStatement.OPERATE_PARAM_ENTITY) Entity entity);

    @Insert(BaseSqlStatement.OPERATE_TYPE_ADD_MANY)
    public int addMany(@Param(BaseSqlStatement.OPERATE_PARAM_ENTITIES) List<Entity> entities);

    @Delete(BaseSqlStatement.OPERATE_TYPE_REMOVE_BY_ID)
    public int removeById(@Param(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEY) Object id);

    @Delete(BaseSqlStatement.OPERATE_TYPE_REMOVE_BY_IDS)
    public int removeByIds(@Param(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEYS) List<Object> ids);

    @Delete(BaseSqlStatement.OPERATE_TYPE_REMOVE)
    public int remove(@Param(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT) SqlStatement sqlStatement);

    @Update(BaseSqlStatement.OPERATE_TYPE_UPDAT_BY_ID)
    public int updateById(@Param(BaseSqlStatement.OPERATE_PARAM_ENTITY) Entity entity);

    @Update(BaseSqlStatement.OPERATE_TYPE_UPDATE)
    public int update(@Param(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT) SqlStatement sqlStatement);

    @Select(BaseSqlStatement.OPERATE_TYPE_GET_BY_ID)
    public Entity getById(@Param(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEY) Object id);

    @Select(BaseSqlStatement.OPERATE_TYPE_GET_BY_IDS)
    public List<Entity> getByIds(@Param(BaseSqlStatement.OPERATE_PARAM_PRIMARY_KEYS) List<Object> ids);

    @Select(BaseSqlStatement.OPERATE_TYPE_GET_ONE)
    public Entity getOne(@Param(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT) SqlStatement sqlStatement);

    @Select(BaseSqlStatement.OPERATE_TYPE_GET_MANY)
    public List<Entity> getMany(@Param(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT) SqlStatement sqlStatement);

    @Select(BaseSqlStatement.OPERATE_TYPE_GET_PAGE)
    public List<Entity> getPage(@Param(BaseSqlStatement.OPERATE_PARAM_SQLSTATEMANT) SqlStatement sqlStatement, @Param(BaseSqlStatement.OPERATE_PARAM_PAGER) Pager pager);

}

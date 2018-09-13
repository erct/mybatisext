# mybatisext

利用mybatis拦截器进行扩展，实现基础mapper、关系映射、对象查询

实体关系映射

    数据表定义
    public @interface Table {
        String value() default "";
    }

    数据库字段定义
    public @interface Column {
        String value() default "";
        boolean primaryKey() default false;
        boolean generatedKey() default true;
    }
    
    一对一字段定义
    public @interface OneOne {

        Class<? extends BaseExtEntity> type();

        String foreignKey();

        String propertyKey();
    }
    
    一对多字段定义
    public @interface OneMany {
        Class<? extends BaseExtEntity> type();

        String foreignKey();

        String propertyKey();
    }
    
    枚举类型定义
    public enum CommonStatus implements BaseExtEnum {
        Enable(1, "正常"),
        Disable(0, "关闭");

        private int value;
        private String name;

        private CommonStatus(int value, String name){
            this.value = value;
            this.name = name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String getName() {
            return name;
        }
    }


基础mapper
    
    public interface BaseMapper<Entity extends BaseEntity> extends BaseExtMapper<Entity>{}
    
    public interface YourMapper extends BaseMapper<YourEntity>{}
    
    @Autowired
    private YourMapper yourMapper;
    
    yourMapper.addOne(entity);
    yourMapper.addMany(entities);
    yourMapper.removeById(id);
    yourMapper.removeByIds(ids);
    yourMapper.updateById(entity);
    yourMapper.getById(id);
    yourMapper.getByIds(id);


SQL查询对象

    更新
    MySqlStatement mySqlStatement = MySqlStatement.createUpdateStatement(YourEntity.class);
    mySqlStatement
            .set("propertyName", "propertyValue")
            .where("propertyName", ConditionType.EQUAL, "propertyValue")
    ;
    int result = mapper.update(mySqlStatement);
    
    删除
    MySqlStatement mySqlStatement = MySqlStatement.createDeleteStatement(YourEntity.class);
    mySqlStatement
            .where("propertyName", ConditionType.EQUAL, "propertyValue")
    ;
    int result = mapper.remove(mySqlStatement);
    
    单表查询
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity.class);
    mySqlStatement.select("id", "name");
    mySqlStatement.select("id", "name");
    YourEntity entity = mapper.getOne(mySqlStatement);
    
    mySqlStatement.orderBy("id", OrderType.DESCENDING).limit(10);
    List<YourEntity> entities = mapper.getMany(mySqlStatement);
    
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity.class);
    mySqlStatement.select("max(#{id})");
    YourEntity entity = mapper.getOne(mySqlStatement);
    int maxId = entity.getId();
    
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity.class);
    mySqlStatement.selectAs("count(#{id})", "propertyName");
    YourEntity entity = mapper.getOne(mySqlStatement);
    int countNum = entity.getPropertyName();

    表连接查询
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity1.class);
    MySqlStatement mySqlStatement1 = mySqlStatement.createSubQuery("relationshipProperty");
    MySqlStatement mySqlStatement2 = mySqlStatement.createSubQuery(YourEntity2.class);
    mySqlStatement.join(mySqlStatement2, JoinType.LEFT_JOIN, "fromPropertyName", "toPropertyName", false, "resultPropertyName");
    mySqlStatement1.select("id", "name");
    mySqlStatement2.select("id", "name");
    
    子查询
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity1.class);
    MySqlStatement mySqlStatement2 = mySqlStatement.createSubQuery(YourEntity2.class);
    mySqlStatement.join(mySqlStatement2, JoinType.LEFT_JOIN, "fromPropertyName", "toPropertyName", true, "resultPropertyName");
    
    分页查询
    Pager pager = new Pager();
    pager.setPageSize(10);
    pager.setCurrentPage(1);
        
    List<YourEntity> entities = getPage(mySqlStatement, pager);
    pager.getRowCount();
    pager.getCurrentPage();
    
    获取查询sql及参数
    FormatSqlWrapper formatSqlWrapper = mySqlStatement.getFormatSqlWrapper();
    formatSqlWrapper.getSql();
    formatSqlWrapper.getParameters();

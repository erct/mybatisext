# mybatisext

利用mybatis拦截器进行扩展，实现基础mapper、关系映射、对象查询

基础mapper
    
    public interface BaseMapper<Entity extends BaseEntity> extends BaseExtMapper<Entity>{}
    
    public interface CompanyMapper extends BaseMapper<CompanyEntity>{}
    
    @Autowired
    private CompanyMapper companyMapper;
    
    companyMapper.addOne(entity);
    companyMapper.addMany(entities);
    companyMapper.removeById(id);
    companyMapper.removeByIds(ids);
    companyMapper.updateById(entity);
    companyMapper.getById(id);
    companyMapper.getByIds(id);


实体关系映射

    @Table

    @Column
    
    @OneOne
    
    @OneMany
    
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


类SQL对象

    MySqlStatement mySqlStatement = MySqlStatement.createUpdateStatement(YourEntity.class);
    mySqlStatement
            .set("propertyName", "propertyValue")
            .where("propertyName", ConditionType.EQUAL, "propertyValue")
    ;
    int result = mapper.update(mySqlStatement);
    
    MySqlStatement mySqlStatement = MySqlStatement.createDeleteStatement(YourEntity.class);
    mySqlStatement
            .where("propertyName", ConditionType.EQUAL, "propertyValue")
    ;
    int result = mapper.remove(mySqlStatement);
    
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity.class);
    mySqlStatement.select("max(#{id})");
    YourEntity entity = mapper.getOne(mySqlStatement);
    int maxId = entity.getId();
    
    MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(YourEntity.class);
    mySqlStatement.selectAs("count(#{id})", "propertyName");
    YourEntity entity = mapper.getOne(mySqlStatement);
    int countNum = entity.getPropertyName();


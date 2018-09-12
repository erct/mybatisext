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

        public abstract class BaseEntity extends BaseExtEntity {
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
        }
        
        @Table("Company")
        public class CompanyEntity extends BaseEntity {

            @Column(primaryKey = true)
            private int id;

            @Column
            private String comName;

            @Column
            private Date registerDate;

            @Column("ComAddress")
            private String address;

            @Column
            private CommonStatus status;

            @OneMany(type = DepartmentEntity.class, foreignKey = "comId", propertyKey = "id")
            private List<DepartmentEntity> departments;

            private int countValue;

            public int getId() {
                return id;
            }

            public Date getRegisterDate() {
                return registerDate;
            }

            public String getAddress() {
                return address;
            }

            public String getComName() {
                return comName;
            }

            public CommonStatus getStatus() {
                return status;
            }

            public int getCountValue() {
                return countValue;
            }

            public List<DepartmentEntity> getDepartments() {
                return departments;
            }

            public void setId(int id) {
                this.id = id;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public void setComName(String comName) {
                this.comName = comName;
            }

            public void setRegisterDate(Date registerDate) {
                this.registerDate = registerDate;
            }

            public void setStatus(CommonStatus status) {
                this.status = status;
            }

            public void setCountValue(int countValue) {
                this.countValue = countValue;
            }

            public void setDepartments(List<DepartmentEntity> departments) {
                this.departments = departments;
            }
        }

        @Table("Department")
        public class DepartmentEntity extends BaseEntity {

            @Column(primaryKey = true)
            private int id;

            @Column
            private int comId;

            @Column
            private String departmentName;

            @OneOne(type = CompanyEntity.class, foreignKey = "id", propertyKey = "comId")
            private CompanyEntity company;

            @OneMany(type = EmployeeEntity.class, foreignKey = "departmentId", propertyKey = "id")
            private List<EmployeeEntity> employees;
        }

        @Table("Employee")
        public class EmployeeEntity extends BaseEntity {

            @Column(primaryKey = true, generatedKey = false)
            private String id;

            @Column
            private String name;

            @Column
            private Gender gender;

            @Column
            private int level;

            @Column
            private String title;

            @Column
            private int departmentId;

            @OneOne(type = DepartmentEntity.class, foreignKey = "id", propertyKey = "departmentId")
            private DepartmentEntity department;

            public enum Gender implements BaseExtEnum {
                Male(1, "男"),
                Female(2, "女");

                private int value;
                private String name;

                private Gender(int value, String name){
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
        }


查询对象关系映射

    @Test
    public void updateBySqlStatement(){
        CompanyEntity entity = new CompanyEntity();
        entity.setComName("company");
        entity.setAddress("china");
        entity.setRegisterDate(new Date());
        entity.setStatus(BaseEntity.CommonStatus.Enable);

        companyMapper.addOne(entity);

        MySqlStatement mySqlStatement = MySqlStatement.createUpdateStatement(CompanyEntity.class);
        mySqlStatement
                .set("comName", "unknow")
                .where("id", ConditionType.EQUAL, entity.getId())
        ;

        int result = companyMapper.update(mySqlStatement);
        CompanyEntity updatedEntity = companyMapper.getById(entity.getId());

        Assert.assertEquals(1, result);
        Assert.assertNotEquals(entity.getComName(), updatedEntity.getComName());
    }

    @Test
    public void selectBySqlStatement(){
        CompanyEntity entity = new CompanyEntity();
        entity.setComName("company");
        entity.setAddress("china");
        entity.setRegisterDate(new Date());
        entity.setStatus(BaseEntity.CommonStatus.Enable);

        companyMapper.addOne(entity);


        MySqlStatement mySqlStatement = MySqlStatement.createSelectStatement(CompanyEntity.class);
        mySqlStatement.select("max(#{id})");

        CompanyEntity resultEntity = companyMapper.getOne(mySqlStatement);
        Assert.assertEquals(entity.getId(), resultEntity.getId());


        mySqlStatement = MySqlStatement.createSelectStatement(CompanyEntity.class);
        mySqlStatement.selectAs("count(#{id})", "countValue");

        resultEntity = companyMapper.getOne(mySqlStatement);
        Assert.assertTrue(resultEntity.getCountValue() > 0);
    }

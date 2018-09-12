package cw.frame.mybatisext.test.entity;

import cw.frame.mybatisext.annotation.Column;
import cw.frame.mybatisext.annotation.OneOne;
import cw.frame.mybatisext.annotation.Table;
import cw.frame.mybatisext.base.entity.BaseExtEnum;

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

package cw.frame.mybatisext.test.entity;

import cw.frame.mybatisext.annotation.Column;
import cw.frame.mybatisext.annotation.OneMany;
import cw.frame.mybatisext.annotation.OneOne;
import cw.frame.mybatisext.annotation.Table;

import java.util.List;

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

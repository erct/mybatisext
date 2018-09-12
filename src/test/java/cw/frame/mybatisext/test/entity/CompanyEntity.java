package cw.frame.mybatisext.test.entity;

import cw.frame.mybatisext.annotation.Column;
import cw.frame.mybatisext.annotation.OneMany;
import cw.frame.mybatisext.annotation.Table;

import java.util.Date;
import java.util.List;

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

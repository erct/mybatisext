package cw.frame.mybatisext.test.tester;

import cw.frame.mybatisext.enumeration.ConditionType;
import cw.frame.mybatisext.provider.mysql.MySqlStatement;
import cw.frame.mybatisext.test.entity.BaseEntity;
import cw.frame.mybatisext.test.entity.CompanyEntity;
import cw.frame.mybatisext.test.mapper.CompanyMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EnableTransactionManagement
@ComponentScan({"cw.frame.mybatisext.test"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MapperTest.class)
public class MapperTest {

    @Autowired
    private CompanyMapper companyMapper;

    @Test
    public void addCompany(){
        CompanyEntity entity = new CompanyEntity();
        entity.setComName("alibaba");
        entity.setAddress("china");
        entity.setRegisterDate(new Date());
        entity.setStatus(BaseEntity.CommonStatus.Enable);

        int result = companyMapper.addOne(entity);

        Assert.assertEquals(1, result);
        Assert.assertTrue(entity.getId() > 0);
    }

    @Test
    public void addCompanies(){
        int num = 10;
        List<CompanyEntity> companyEntities = new ArrayList<CompanyEntity>();

        for (int i=0; i<num; i++){
            CompanyEntity entity = new CompanyEntity();
            entity.setComName("alibaba " + i);
            entity.setAddress("china");
            entity.setRegisterDate(new Date());
            entity.setStatus(BaseEntity.CommonStatus.Enable);
            companyEntities.add(entity);
        }

        int result = companyMapper.addMany(companyEntities);
        Assert.assertEquals(num, result);
    }

    @Test
    public void getCompanyById(){
        int id = 1;

        CompanyEntity entity = companyMapper.getById(id);

        Assert.assertNotNull(entity);
        Assert.assertEquals(id, entity.getId());
    }

    @Test
    public void updateById(){
        int id = 1;

        CompanyEntity entity = companyMapper.getById(id);
        if (entity.getStatus() == BaseEntity.CommonStatus.Enable){
            entity.setStatus(BaseEntity.CommonStatus.Disable);
        } else {
            entity.setStatus(BaseEntity.CommonStatus.Enable);
        }

        int result = companyMapper.updateById(entity);
        CompanyEntity updatedEntity = companyMapper.getById(id);

        Assert.assertEquals(1, result);
        Assert.assertEquals(entity.getStatus(), updatedEntity.getStatus());
    }

    @Test
    public void removeById(){
        CompanyEntity entity = new CompanyEntity();
        entity.setComName("company");
        entity.setAddress("china");
        entity.setRegisterDate(new Date());
        entity.setStatus(BaseEntity.CommonStatus.Enable);

        companyMapper.addOne(entity);

        int result = companyMapper.removeById(entity.getId());
        CompanyEntity removedEntity = companyMapper.getById(entity.getId());

        Assert.assertEquals(1, result);
        Assert.assertNull(removedEntity);
    }

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
}

package cw.frame.mybatisext.test.basic;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import cw.frame.mybatisext.provider.mysql.MySqlInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@MapperScan({"cw.frame.mybatisext.test.mapper"})
public class MyBatisConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private MySqlInterceptor mySqlInterceptor;

    @Bean
    public MySqlInterceptor mySqlInterceptor(){

        return new MySqlInterceptor();
    }
    @Bean
    public DataSource masterDataSource() throws Exception{
        return DruidDataSourceFactory.createDataSource(this.getDataSourceProperty("datasources.master."));
    }

    @Bean
    public DataSource slaveDataSource() throws Exception{
        return DruidDataSourceFactory.createDataSource(this.getDataSourceProperty("datasources.slave."));
    }

    /**
     * @Primary 该注解表示在同一个接口有多个实现类可以注入的时候，默认选择哪一个，而不是让@autowire注解报错
     * @Qualifier 根据名称进行注入，通常是在具有相同的多个类型的实例的一个注入（例如有多个DataSource类型的实例）
     * @DependsOn 解决循环依赖问题，在系统自动创建datasource前创建指定数据源
     * @param masterDataSource
     * @param slaveDataSource
     * @return
     */
    @Bean
    @Primary
    @DependsOn({ "masterDataSource", "slaveDataSource"})
    public DynamicDataSource dataSource(@Qualifier("masterDataSource") DataSource masterDataSource, @Qualifier("slaveDataSource") DataSource slaveDataSource){
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.Master, masterDataSource);
        dataSourceMap.put(DataSourceType.Slave, slaveDataSource);

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);

        return dynamicDataSource;
    }

    /**
     * 根据数据源创建SqlSessionFactory
     * mybatis拦截器需要手动设置一下 fb.setPlugins(new Interceptor[]{mySqlInterceptor});
     * @param dynamicDataSource
     * @return
     * @throws Exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DynamicDataSource dynamicDataSource) throws Exception{
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();

        // 因为手动创建了SqlSessionFactory，所以需要手动设置拦截器
        fb.setPlugins(new Interceptor[]{mySqlInterceptor});

        fb.setDataSource(dynamicDataSource);

        return fb.getObject();
    }

    /**
     * 配置事务管理器
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DynamicDataSource dataSource) throws Exception{
        return new DataSourceTransactionManager(dataSource);
    }

    private Properties getDataSourceProperty(String prefixKey){
        String[] keys = {
                "driverClassName", "url", "username", "password", "type", "filters", "maxActive", "initialSize",
                "maxWait", "minIdle", "timeBetweenEvictionRunsMillis", "minEvictableIdleTimeMillis",
                "validationQuery", "testWhileIdle", "testOnBorrow", "testOnReturn",
                "poolPreparedStatements", "maxOpenPreparedStatements"
        };

        Properties properties = new Properties();

        for (String key : keys){
            properties.put(key, environment.getProperty(prefixKey + key));
        }

        return properties;
    }
}

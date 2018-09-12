package cw.frame.mybatisext.test.basic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType dataSourceType){
        contextHolder.set(dataSourceType);
    }

    public static DataSourceType getDataSourceType() {
        return contextHolder.get();
    }

    public static void clear(){
        contextHolder.remove();
    }

    @Override
    protected Object determineCurrentLookupKey(){
        return DynamicDataSource.getDataSourceType();
    }
}

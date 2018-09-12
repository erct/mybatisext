package cw.frame.mybatisext.base;

import cw.frame.mybatisext.base.entity.TableInfo;

import java.util.*;

public class ResultMap {
    private TableInfo tableInfo;
    private Map<String, String> resultMap;
    private Map<String, ResultMap> subResultMap;

    public ResultMap(TableInfo tableInfo){
        this.tableInfo = tableInfo;
        this.resultMap = new HashMap<String, String>();
        this.subResultMap = null;
    }

    public void addResultMap(String resultName, String propertyName){
        this.resultMap.put(resultName, propertyName);
    }

    /**
     * 添加子结果映射
     * @param subResultMap 子结果map对象
     * @param resultPropertyName 结果存储字段
     */
    public void addSubResultMap(ResultMap subResultMap, String resultPropertyName){
        if (this.subResultMap == null){
            this.subResultMap = new HashMap<String, ResultMap>();
        }

        this.subResultMap.put(resultPropertyName, subResultMap);
    }

    public Map<String, String> getResultMap(){
        return this.resultMap;
    }

    public boolean hasSubResultMap(){
        if (this.subResultMap != null && this.subResultMap.size() > 0){
            return true;
        } else {
            return false;
        }
    }

    public Map<String, ResultMap> getSubResultMap() {
        return subResultMap;
    }

    public TableInfo getTableInfo(){
        return this.tableInfo;
    }
}

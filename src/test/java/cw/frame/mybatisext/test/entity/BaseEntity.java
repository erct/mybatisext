package cw.frame.mybatisext.test.entity;

import cw.frame.mybatisext.base.entity.BaseExtEntity;
import cw.frame.mybatisext.base.entity.BaseExtEnum;

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


package cw.frame.mybatisext.annotation;

import cw.frame.mybatisext.base.entity.BaseExtEntity;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneMany {
    Class<? extends BaseExtEntity> type();

    String foreignKey();

    String propertyKey();
}

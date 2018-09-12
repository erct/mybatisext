package cw.frame.mybatisext.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    String value() default "";
    boolean primaryKey() default false;
    boolean generatedKey() default true;
}

package cw.frame.mybatisext.test.basic;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DynamicDataSourceAspect {

    @Before("@annotation(DataSource)")
    public void beforeExecute(JoinPoint point){
    }

    @Around("@annotation(DataSource)")
    public Object aroundExecute(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        Class<?> classType = proceedingJoinPoint.getTarget().getClass();
        String methodName = proceedingJoinPoint.getSignature().getName();
        Class[] argClass = ((MethodSignature)proceedingJoinPoint.getSignature()).getParameterTypes();
        DataSourceType lastDataSourceType = null;
        boolean resetDataSourceType = false;

        Method method = classType.getMethod(methodName, argClass);
        if (method.isAnnotationPresent(DataSource.class)){
            DataSourceType dataSourceType = method.getAnnotation(DataSource.class).value();
            lastDataSourceType = DynamicDataSource.getDataSourceType();
            if (lastDataSourceType == null){
                DynamicDataSource.setDataSourceType(dataSourceType);
                resetDataSourceType = true;
            } else {
                switch (lastDataSourceType){
                    case Master:
                        break;
                    case Slave:
                        if (dataSourceType == DataSourceType.Master){
                            DynamicDataSource.setDataSourceType(dataSourceType);
                            resetDataSourceType = true;
                        }
                        break;
                }
            }
        }

        Object result = proceedingJoinPoint.proceed();

        if (resetDataSourceType){
            if (lastDataSourceType == null){
                DynamicDataSource.clear();
            } else {
                DynamicDataSource.setDataSourceType(lastDataSourceType);
            }
        }

        return result;
    }

    @After("@annotation(DataSource)")
    public void afterExecute(JoinPoint point){
    }
}

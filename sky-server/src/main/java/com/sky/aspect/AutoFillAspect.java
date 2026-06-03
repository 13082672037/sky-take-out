package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，用于实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点 表达式和特定注解才会被拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    @Before("autoFillPointCut()")
    /**
     * 前置通知，在方法执行前执行
     * @param joinPoint
     */
    public void autuFill(JoinPoint joinPoint){
        log.info("开始进行公共字段填充....");

        // 获取被拦截方法的签名信息（强转为MethodSignature以获取更多信息）
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 从方法签名中获取Method对象，再从Method对象上获取@AutoFill注解
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        // 获取@AutoFill注解上的value属性值（即INSERT或UPDATE）
        OperationType value = autoFill.value();

        // 获取方法参数，即实体对象
        Object[] args = joinPoint.getArgs(); // 获取所有方法参数
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0]; // 获取方法参数中的第一个参数，即实体对象（约定）
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        if (value == OperationType.INSERT) {
            try{
                Method createTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射赋值
                createTimeMethod.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }

        } else if (value == OperationType.UPDATE) {
            try{
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

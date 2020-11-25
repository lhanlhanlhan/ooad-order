package cn.edu.xmu.oomall.order.aspects;

import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.connector.UserConnector;
import cn.edu.xmu.oomall.order.connector.model.UserInfo;
import cn.edu.xmu.oomall.order.enums.Constants;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 「审查」的 Aspect 定义
 *
 * @author Han Li
 * Created at 25/11/2020 8:59 上午
 * Modified by Han Li at 25/11/2020 8:59 上午
 */
@Aspect
@Component
public class InspectAspect {

    // 审查日志组件
    private static final Logger logger = LoggerFactory.getLogger(InspectAspect.class);

    // 用户服务连接器组件
    @Autowired
    private UserConnector userConnector;

    // 定义此 Aspect 在 Controller 层切点为 @Inspect 注解
    @Pointcut("@annotation(cn.edu.xmu.oomall.order.aspects.Inspect)")
    public void inspectAspect() {
    }

    /**
     * 前置通知：在进入目标 Controller 前执行，不能改变方法执行过程
     *
     * @author Han Li
     * Created at 25/11/2020 09:04
     * Created by Han Li at 25/11/2020 09:04
     * @param joinPoint 在 Controller 层的切点
     */
    @Before("inspectAspect()")
    public void doBefore(JoinPoint joinPoint) {
    }

    /**
     * 环绕通知：在进入目标方法之前和之后都能执行
     * 可以拒绝 / 批准目标方法执行，可以改动方法参数，甚至可以改动目标方法返回值
     *
     * @author Han Li
     * Created at 25/11/2020 09:06
     * Created by Han Li at 25/11/2020 09:06
     * @param joinPoint 在 Controller 层的切点
     * @return java.lang.Object
     */
    @Around("inspectAspect()")
    public Object around(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("around: begin joinPoint = "+ joinPoint);
        }
        // 获取目标方法
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();
        // 目标方法是 Controller，这里获取它们的 HttpServletRequest/HttpServletResponse 以获取 token 及必要时返回错误消息
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        assert response != null;
        String token = request.getHeader(Constants.LOGIN_TOKEN_KEY);
        if (token == null) {
            // 未有附带 token，返回错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ResponseUtils.makeResponse(ResponseCode.NEED_LOGIN);
        }

        // 从 其他模块 获取用户资料
        UserInfo userInfo = userConnector.verifyTokenAndGetUserInfo(token);
        if (null == userInfo) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ResponseUtils.makeResponse(ResponseCode.INVALID_JWT);
        }
        Long userId = userInfo.getId();

        // 将获取到的用户资料注入进 Args 去
        Object[] args = joinPoint.getArgs();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            // 获取参数拥有的 annotation
            Annotation[] paramAnn = annotations[i];
            if (paramAnn.length == 0){
                continue;
            }
            // 检查参数拥有的每一个 Annotation
            for (Annotation annotation : paramAnn) {
                // LoginUser.class：注入用户 id
                if (annotation.annotationType().equals(LoginUser.class)) {
                    // 将 id 注入进去
                    args[i] = userId;
                }
            }
        }

        Object obj = null;
        try {
            obj = ((ProceedingJoinPoint) joinPoint).proceed(args);
        } catch (Throwable ignored) {
            // 不能 proceed？
        }
        return obj;
    }
}

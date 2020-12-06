package cn.edu.xmu.ooad.order.configurations;

import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理机
 *
 * @author Han Li
 * Created at 6/12/2020 12:08 下午
 * Modified by Han Li at 6/12/2020 12:08 下午
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获及处理【请求方式不支持】之错误
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    public Object handleException(HttpRequestMethodNotSupportedException e) {
        logger.info("尝试的请求不被支援：" + e.getMessage());
        return ResponseUtils.make(new APIReturnObject<>(HttpStatus.METHOD_NOT_ALLOWED, ResponseCode.REQUEST_NOT_ALLOWED));
    }

    /**
     * 捕获及处理【任何普通】错误
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) {
        logger.error("出现未定义的错误：" + e.getMessage());
        return ResponseUtils.make(
                new APIReturnObject<>(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCode.INTERNAL_SERVER_ERR,
                "服务器未能理解及处理刚发生的错误，已将错误提交管理员处理。为防止您的信息丢失，请勿重复尝试！"));
    }

    /**
     * 捕获及处理【由 Spring 校验器】触发之错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidException(MethodArgumentNotValidException e){
        FieldError fe = e.getBindingResult().getFieldError();
        if (fe == null) {
            logger.info("校验器发生未指明的错误：" + e.getMessage());
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST);
        }
        // 获取错误资料
        String defaultMessage = fe.getDefaultMessage();
        logger.info("校验器错误：" + defaultMessage);
        // 将错误信息返回给前台
        return ResponseUtils.make(
                new APIReturnObject<>(HttpStatus.BAD_REQUEST,
                ResponseCode.BAD_REQUEST,
                defaultMessage));
    }
}

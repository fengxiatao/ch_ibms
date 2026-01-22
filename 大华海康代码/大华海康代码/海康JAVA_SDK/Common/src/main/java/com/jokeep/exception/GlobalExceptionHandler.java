package com.jokeep.exception;


import com.jokeep.enhance.ResultVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     *
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
     *
     * @param model
     */
    @ModelAttribute
    public void addAttributes(Model model) {
        //model.addAttribute("author", "Magical Sam");
    }

    /**
     * 拦截捕捉自定义异常 AjaxException.class
     *  该方法可重载多个，传入不同类型的异常类型参数
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Throwable.class)
    public ResultVo errorHandler(Throwable ex) {
        String message = ex.getMessage();
        int code = 100;
        // 在这里判断异常，根据不同的异常返回错误。
        if (ex.getClass().equals(DataAccessException.class)) {
            message = "数据库操作失败！";
            code = 101;
        } else if (ex.getClass().toString().equals(NullPointerException.class.toString())) {
            message = "调用了未经初始化的对象或者是不存在的对象！";
            code = 102;
        } else if (ex.getClass().equals(IOException.class)) {
            message = "IO异常！";
            code = 103;
        } else if (ex.getClass().equals(ClassNotFoundException.class)) {
            message = "指定的类不存在！";
            code = 104;
        } else if (ex.getClass().equals(ArithmeticException.class)) {
            message = "数学运算异常！";
            code = 105;
        } else if (ex.getClass().equals(ArrayIndexOutOfBoundsException.class)) {
            message = "数组下标越界！";
            code = 106;
        } else if (ex.getClass().equals(IllegalArgumentException.class)) {
            message = "方法的参数错误！";
            code = 107;
        } else if (ex.getClass().equals(ClassCastException.class)) {
            message = "类型强制转换错误！";
            code = 108;
        } else if (ex.getClass().equals(SecurityException.class)) {
            message = "违背安全原则异常！";
            code = 109;
        } else if (ex.getClass().equals(SQLException.class)) {
            message = "操作数据库异常！";
            code = 110;
        } else if (ex.getClass().equals(NoSuchMethodError.class)) {
            message = "方法末找到异常！";
            code = 111;
        } else if (ex.getClass().equals(InternalError.class)) {
            message = "Java虚拟机发生了内部错误！";
            code = 112;
        } else if (ex.getClass().equals(CustomException.class)) {
            message = ex.getMessage();
            code = ((CustomException) ex).getCode();
        } else {
            message = "程序内部错误，操作失败！";
            code = 113;
        }
        log.error(ex.getMessage(), ex);
        ResultVo result=new ResultVo(code,message);
        return result;
    }
}
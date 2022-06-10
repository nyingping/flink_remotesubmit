package com.yp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author nyp
 * @version 1.0
 * @description: 全局异常处理
 * @date 2021/10/14 13:49
 */
@RestControllerAdvice
public class MyExceptionHandler {

    Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ModelAndView Handler(Exception e) {
        logger.error(e.getMessage(),e);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("info", e.getMessage());
        return mv;
    }

}

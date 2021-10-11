package com.yp.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ModelAndView Handler(Exception e) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("info", e.getMessage());
        return mv;
    }

}

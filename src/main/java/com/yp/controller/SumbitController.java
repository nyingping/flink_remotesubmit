package com.yp.controller;

import com.yp.model.DefaultSql;
import com.yp.model.SubmitModel;
import com.yp.service.SubmitService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
public class SumbitController {

    Logger logger = LoggerFactory.getLogger(SumbitController.class);

    @Autowired
    private SubmitService submitService;

    @Autowired
    private DefaultSql defaultSql;

    @GetMapping("/")
    public ModelAndView index(Model model) {
        return new ModelAndView("submit","defaultSql",defaultSql);
    }


    @PostMapping("/submit")
    public ModelAndView submit(SubmitModel model) throws Exception{
        if (model.getSubmitType() == 2 && StringUtils.isBlank(model.getJarName())) {
            Assert.isNull(model.getJarName(),String.format("当选择flink现有jar包的提交方式时，必须输入jar包名称【{}】","jarName"));
        }
        String url = submitService.service(model);
        return new ModelAndView("direct", "url", url);
    }

}

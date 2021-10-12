package com.yp.controller;

import com.google.gson.Gson;
import com.yp.model.SubmitModel;
import com.yp.service.SubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
public class SumbitController {

    @Autowired
    private SubmitService submitService;

    //    private Logger logger = LoggerFactory.getLogger(SumbitController.class);
    @GetMapping("/")
    public ModelAndView index(Model model) {
        return new ModelAndView("submit"); // 视图重定向 - 跳转
    }


    @PostMapping("/submit")
    public ModelAndView submit(SubmitModel model) throws Exception{
        Gson gson = new Gson();
        System.out.println(gson.toJson(model));
        String url = submitService.service(model);
        return new ModelAndView("direct", "url", url);
    }

}

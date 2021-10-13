package com.yp.service;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.yp.controller.SumbitController;
import com.yp.model.SubmitModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class SubmitService {

    Logger logger = LoggerFactory.getLogger(SubmitService.class);

    @Autowired
    private JarService jarService;

    @Value("${flinkurl}")
    private String flinkUrl;

    /**
     * 任务提交后，flink展示相应任务界面的地址
     */
    private String flinkMissionUiUrl;

    /**
     * flink任务打包成功后上传到flink集群的相应请示地址
     */
    private String flinkJarUploadUrl;

    private String submitMissionUrl;


    public String service(SubmitModel model) throws Exception {
        init();
        String flinkJarName = model.getJarName();
        if (model.getSubmitType() == 1) {
            //1.生成jar包
            String jarPath = jarService.sevice(model);
            //2.上传jar包
            flinkJarName = upload(jarPath);
            System.out.println(flinkJarName);
        }

        //3.提交任务
        String url = getParam(model, flinkJarName,1);
        String jobId = HttpUtil.post(url, "");
        if (jobId.contains("error")) {
            throw new Exception(jobId);
        }
        return flinkMissionUiUrl.replace("{jobId}", jobId);
    }

    private String getParam(SubmitModel model, String flinkJarName,Integer submitType) {
        StringBuilder param = new StringBuilder("?entry-class=");
        param.append(model.getMainClass());
        param.append("&parallelism=");
        param.append(model.getParallelism());
        if (submitType == 2) {
            param.append("&program-args=--- ");
            param.append(model.getSource().replaceAll("\r", "").replaceAll("\n", ""));
            param.append(" --- ");
            param.append(model.getTransformation().replaceAll("\r", "").replaceAll("\n", ""));
            param.append(" --- ");
            param.append(model.getSink().replaceAll("\r", "").replaceAll("\n", ""));
        }
        return submitMissionUrl.replace("{flinkJarName}", flinkJarName) +param.toString();
    }

    /**
     * 上传JAR包至flink集群
     * @param jarPath
     * @return
     * @throws Exception
     */
    public String upload (String jarPath) throws Exception{
        HttpClient restClient = new DefaultHttpClient();
        HttpPost uploadFile = new HttpPost(flinkJarUploadUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        File file = new File(jarPath);
        builder.addBinaryBody(
                "jarfile",
                new FileInputStream(jarPath),
                ContentType.create("application/x-java-archive"),
                file.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        HttpResponse response = restClient.execute(uploadFile);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            Gson gson = new Gson();
            HashMap<String, String> result = gson.fromJson(line, HashMap.class);
            if ("success".equals(result.get("status"))) {
                return result.get("filename").split("/")[4];
            }
        }
        return null;
    }

    public void init(){
        flinkMissionUiUrl=flinkUrl+"/#/job/{jobId}/overview";
        flinkJarUploadUrl=flinkUrl+"/jars/upload";
        submitMissionUrl=flinkUrl+"/jars/{flinkJarName}/run";
    }

}

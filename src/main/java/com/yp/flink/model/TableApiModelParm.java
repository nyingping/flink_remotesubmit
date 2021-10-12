package com.yp.flink.model;

import com.google.gson.Gson;
import com.yp.model.SubmitModel;
import org.apache.flink.api.java.utils.ParameterTool;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TableApiModelParm {

    public static void main(String[] args) throws Exception {

        System.out.println("args = " + args.length);

        for (int i = 0; i <args.length ; i++) {
            System.out.println("args"+i+"="+args[i]);
        }

        String paramPath = "C:\\Users\\nyp\\Desktop\\model.txt";

       String param = Files.lines(Paths.get(paramPath), Charset.defaultCharset())
                .flatMap(line -> Arrays.stream(line.split("\n")))
                .collect(Collectors.toList()).get(0);

        Gson gson = new Gson();
        SubmitModel model = gson.fromJson(param, SubmitModel.class);
        System.out.println(model);

        StringBuilder str = new StringBuilder();
        for (int i=0;i<args.length;i++) {
            str.append(args[i]+" ");
        }

        String[] p = str.toString().split("---");

        String sourceSql =p[1];
        String transformationSql =p[2];
        String sinkSql =p[3];
        try {
            final ParameterTool params = ParameterTool.fromPropertiesFile("C:\\Users\\nyp\\Desktop\\application.properties");
            sourceSql = params.get("sourceSql");
            transformationSql = params.get("transformationSql");
            sinkSql = params.get("sinkSql");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(
                    "No param specified. Please run 'TableApiModel "
                            + "--sourceSql <sourceSql> --transformationSql <transformationSql> --sinkSql <sinkSql>'");
            return;
        }
        System.out.println("========");

        /*EnvironmentSettings fsSettings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .useBlinkPlanner()
                .build();
        TableEnvironment te = TableEnvironment.create(fsSettings);
        te.executeSql("sourceSql");
        te.executeSql("sinkSql");
        te.executeSql("transformationSql");*/
    }

}


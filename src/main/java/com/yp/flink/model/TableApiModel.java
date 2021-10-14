package com.yp.flink.model;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

/**
 * @author nyp
 * @version 1.0
 * @description: flink table api 模板
 * @date 2021/10/14 13:49
 */
public class TableApiModel {

    public static void main(String[] args) throws Exception {
        service();
    }

    public static void service() {
        EnvironmentSettings fsSettings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .useBlinkPlanner()
                .build();
        TableEnvironment te = TableEnvironment.create(fsSettings);
        te.executeSql("${sourceSql}");
        te.executeSql("${sinkSql}");
        te.executeSql("${transformationSql}");
    }

}


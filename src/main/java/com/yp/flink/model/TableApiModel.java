package com.yp.flink.model;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class TableApiModel {

    public static void service(String sourceSql,String transformationSql,String sinkSql) {
        EnvironmentSettings fsSettings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .useBlinkPlanner()
                .build();
        TableEnvironment te = TableEnvironment.create(fsSettings);
        te.executeSql(sourceSql);
//        Table table = te.sqlQuery(transformationSql);
//        te.createTemporaryView("t", table);
        te.executeSql(sinkSql);
        te.executeSql(transformationSql);
    }

}


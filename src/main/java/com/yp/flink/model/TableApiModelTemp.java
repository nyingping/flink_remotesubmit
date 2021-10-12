
package com.yp.flink.model;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class TableApiModelTemp {

    public static void main(String[] args) throws Exception {
        service();
    }

    public static void service() {
        EnvironmentSettings fsSettings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .useBlinkPlanner()
                .build();
        TableEnvironment te = TableEnvironment.create(fsSettings);
        te.executeSql("sdf");
        te.executeSql("3w2");
        te.executeSql("sfd");
    }

}

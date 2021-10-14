package com.yp.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author nyp
 * @version 1.0
 * @description: 提交任务页面默认值
 * @date 2021/10/14 13:49
 */
@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "default")
public class DefaultSql {
    private String sourceSql;
    private String transformationSql;
    private String sinkSql;

    public String getSourceSql() {
        return sourceSql;
    }

    public void setSourceSql(String sourceSql) {
        this.sourceSql = sourceSql;
    }

    public String getTransformationSql() {
        return transformationSql;
    }

    public void setTransformationSql(String transformationSql) {
        this.transformationSql = transformationSql;
    }

    public String getSinkSql() {
        return sinkSql;
    }

    public void setSinkSql(String sinkSql) {
        this.sinkSql = sinkSql;
    }
}

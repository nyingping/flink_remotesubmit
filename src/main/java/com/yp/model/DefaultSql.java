package com.yp.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

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

package com.yp.constants;

/**
 * @author nyp
 * @version 1.0
 * @description: 常量类
 * @date 2021/10/14 13:49
 */
public class SubmitConstant {

    /**
     * 无编译
     */
    public static final Integer SUBMIT_TYPE_NOCOMPILE = 2;
    /**
     * 编译
     */
    public static final Integer SUBMIT_TYPE_COMPILE = 1;

    /**
     * flink提交任务失败返回值包含的关键词
     */
    public static final String FLINKSUBMIT_RESULT_ERR_KEY = "error";

    /**
     * flink提交任务成功返回值提取jobid key
     */
    public static final String FLINKSUBMIT_RESULT_SUCCESS_JOBID_KEY = "jobid";
}

package com.yp.service;

import com.yp.model.SubmitModel;
import com.yp.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
/**
 * @author nyp
 * @version 1.0
 * @description: 生成flink包
 * @date 2021/10/14 13:49
 */

@Component
public class JarService {

    Logger logger = LoggerFactory.getLogger(JarService.class);

    @Value("${rootpath}")
    private String rootPath;

    @Value("${dependmentpath}")
    private String dependmentPath;

    @Value("${mainpath}")
    private String mainpath;

    public String sevice(SubmitModel model) throws Exception {
        FileUtils fileUtils = new FileUtils();
        String key = UUID.randomUUID().toString();
        //将原java文件copy到临时目录，并将mainclass里的参数替换
        String newPath = rootPath + "\\temp\\" + key;
        File newPathFile = new File(newPath);
        if (!newPathFile.exists()) {
            newPathFile.mkdirs();
        }
        fileUtils.copyFolder(rootPath + "\\src", newPath);
        String mainPath = newPath + mainpath;

        replace(mainPath, model, fileUtils);

        String dependmentJars = fileUtils.getAllFileName(new File(dependmentPath), "jar");
        List<File> complierFiles = fileUtils.getAllFile(new File(newPath + "\\src\\main"), "java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(complierFiles);

        String targetDir = rootPath + "\\temp\\" + key + "\\class";
        File targetFile = new File(targetDir);
        if (!targetFile.exists()) targetFile.mkdirs();
        String sourceDir = rootPath;
        Iterable<String> options = Arrays.asList("-encoding", "UTF-8", "-classpath", dependmentJars, "-cp", dependmentJars, "-d", targetDir, "-sourcepath", sourceDir);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits);
        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()){}
        fileManager.close();
        if (logger.isInfoEnabled()) {
            if (success) {
                logger.info("编译成功");
            }
        } else {
            throw new Exception("jar包编译失败！");
        }

        String jarPath = fileUtils.createTempJar(targetDir, rootPath + "\\temp", "flink_" + model.getMissionName());
        boolean bool = fileUtils.deleteDir(new File(newPath));
        if (logger.isInfoEnabled()) {
            if (bool) {
                logger.info("临时目录" + targetDir + "删除");
                logger.info("打包完成后的路径 = " + jarPath);
            }
        }

        return jarPath;
    }

    public void replace(String mainPath, SubmitModel model, FileUtils fileUtils) throws Exception {
        List<String> list = fileUtils.readJavaFile(mainPath);
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (StringUtils.isNoneBlank()) {
                line = line.replace("${sourceSql}", model.getSource().replaceAll("\n", "").replaceAll("\r", ""));
                line = line.replace("${transformationSql}", model.getTransformation().replaceAll("\n", "").replaceAll("\r", ""));
                line = line.replace("${sinkSql}", model.getSink().replaceAll("\n", "").replaceAll("\r", ""));
            }
            list.remove(i);
            list.add(i, line);
        }
        //将原文件删除
        File old = new File(mainPath);
        old.delete();
        fileUtils.contentToJava(mainPath, list);
    }
}

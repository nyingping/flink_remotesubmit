package com.yp.utils;

import com.yp.service.CreateJar.CompilerService;
import org.apache.commons.lang3.StringUtils;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    public static void main(String[] args) {
        service("sdf", "sfd", "3w2");
    }

    public static void service(String sourceSql, String transformationSql, String sinkSql) {
        List<String> list = readJavaFile("D:\\ideaproject\\test\\flink_remotesubmit2\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModel.java");

        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (StringUtils.isNoneBlank()) {
                line = line.replace("${sourceSql}", sourceSql);
                line = line.replace("${transformationSql}", transformationSql);
                line = line.replace("${sinkSql}", sinkSql);
                line = line.replace("public class TableApiModel","public class TableApiModelTemp");
            }
            list.remove(i);
            list.add(i, line);
        }
        File file = new File("D:\\ideaproject\\test\\flink_remotesubmit2\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java");
        if (new File("D:\\ideaproject\\test\\flink_remotesubmit2\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java").exists()) {
            file.delete();
        }
        contentToJava("D:\\ideaproject\\test\\flink_remotesubmit2\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java", list);

        String fullQuanlifiedFileName = "D:\\ideaproject\\test\\flink_remotesubmit2\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);


        Iterable<? extends JavaFileObject> files =
                fileManager.getJavaFileObjectsFromStrings(
                        Arrays.asList(fullQuanlifiedFileName));
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, null, null, null, files);

        Boolean result = task.call();
        if (result == true) {
            System.out.println("Succeeded");
        }



       /* try {
            compiler();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public static void contentToJava(String filePath, List<String> listStr) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));
            listStr.forEach(str -> {
                try {
                    writer.newLine();
                    writer.write(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("文件写入完毕,文件路径:" + filePath);
    }


    public static List<String> readJavaFile(String file) {
        //读取文件
        List<String> lineLists = null;
        try {
            lineLists = Files.lines(Paths.get(file), Charset.defaultCharset())
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            //Your exception handling here
        }
        return lineLists;
    }

    public static void compiler() throws Exception {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        //该文件管理器实例的作用就是将我们需要动态编译的java源文件转换为getTask需要的编译单元
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        // 获取要编译原文件列表
        List<File> sourceFileList = new ArrayList<>();
        sourceFileList.add(new File("D:\\ideaproject\\test\\flink_remotesubmit\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModel.java"));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
        /**
         * 编译选项，在编译java文件时，编译程序会自动的去寻找java文件引用的其他的java源文件或者class。 -sourcepath选项就是定义java源文件的查找目录， -classpath选项就是定义class文件的查找目录，-d就是编译文件的输出目录。
         */
        //Iterable<String> options =Arrays.asList("-encoding",encoding,"-classpath",jars,"-d", targetDir, "-sourcepath", sourceDir);
        //-classpath 依赖jar
        //-d 编译后class存入目录
        String dependmentJars = "";
        dependmentJars = getJarFiles(new File("D:\\ideaproject\\test\\flink_remotesubmit\\lib"), dependmentJars);
        String targetDir = "D:\\ideaproject\\test\\flink_remotesubmit";
        String sourceDir = "D:\\ideaproject\\test\\flink_remotesubmit\\target";
        Iterable<String> options = Arrays.asList("-encoding", "UTF-8", "-classpath", dependmentJars, "-d", targetDir, "-sourcepath", sourceDir);
        /**
         * 第一个参数为文件输出，这里我们可以不指定，我们采用javac命令的-d参数来指定class文件的生成目录
         * 第二个参数为文件管理器实例  fileManager
         * 第三个参数DiagnosticCollector<JavaFileObject> diagnostics是在编译出错时，存放编译错误信息
         * 第四个参数为编译命令选项，就是javac命令的可选项，这里我们主要使用了-d和-sourcepath这两个选项
         * 第五个参数为类名称
         * 第六个参数为上面提到的编译单元，就是我们需要编译的java源文件
         */

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits);
        // 运行编译任务
        // 编译源程式
        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            System.out.printf(
                    "Code: %s%n" +
                            "Kind: %s%n" +
                            "Position: %s%n" +
                            "Start Position: %s%n" +
                            "End Position: %s%n" +
                            "Source: %s%n" +
                            "Message: %s%n",
                    diagnostic.getCode(), diagnostic.getKind(),
                    diagnostic.getPosition(), diagnostic.getStartPosition(),
                    diagnostic.getEndPosition(), diagnostic.getSource(),
                    diagnostic.getMessage(null));
        fileManager.close();
        System.out.println((success) ? "编译成功" : "编译失败");
    }

    /**
     * 查找该目录下的所有的jar文件
     */
    private static String getJarFiles(File sourceFile, String jars) throws Exception {
        if (!sourceFile.exists()) {
            // 文件或者目录必须存在
            throw new IOException("jar目录不存在");
        }
        if (!sourceFile.isDirectory()) {
            // 若file对象为目录
            throw new IOException("jar路径不为目录");
        }
        if (sourceFile.isDirectory()) {
            for (File file : sourceFile.listFiles()) {
                if (file.isDirectory()) {
                    getJarFiles(file, jars);
                } else {
                    jars = jars + file.getPath() + ";";
                }
            }
        } else {
            jars = jars + sourceFile.getPath() + ";";
        }
        return jars;
    }


}

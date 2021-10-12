package com.yp.service;

import com.yp.model.SubmitModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Component
public class JarService {

    @Value("${rootpath}")
    private String rootPath;

    @Value("${dependmentpath}")
    private String dependmentPath;
    
    @Value("${mainpath}")
    private String mainpath;

    public void sevice(SubmitModel model) throws Exception {
//        String rootPath = "D:\\ideaproject\\test\\flink_remotesubmit2";
        String key = UUID.randomUUID().toString();
        //将原java文件copy到临时目录，并将mainclass里的参数替换
        String newPath = rootPath + "\\temp\\" + key;
        File newPathFile = new File(newPath);
        if (!newPathFile.exists()) {
            newPathFile.mkdirs();
        }
        copyFolder(rootPath + "\\src", newPath);
        String mainPath = newPath + "//src//main//java//com//yp//flink//model//TableApiModel.java";

        replace(mainPath, model);

        String dependmentJars = getAllFileName(new File(dependmentPath), "jar");
        List<File> complierFiles = getAllFile(new File(newPath + "\\src\\main"), "java");

        System.out.println("dependmentJars = " + dependmentJars);
        System.out.println("complierFiles = " + complierFiles);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(complierFiles);

        String targetDir = rootPath + "\\temp\\" + key + "\\class";
        File targetFile = new File(targetDir);
        if (!targetFile.exists()) targetFile.mkdirs();
        String sourceDir = rootPath;
        Iterable<String> options = Arrays.asList("-encoding", "UTF-8", "-classpath", dependmentJars, "-d", targetDir, "-sourcepath", sourceDir);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits);
        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) ;
        fileManager.close();
        System.out.println((success) ? "编译成功" : "编译失败");

        String jarPath = createTempJar(targetDir, rootPath + "\\temp", "flink_" + key);
        boolean bool = deleteDir(new File(newPath));
        if (bool) {
            System.out.println("临时目录" + targetDir + "删除");
        }
        System.out.println("打包完成后的路径 = " + jarPath);
    }


    public void replace(String mainPath, SubmitModel model) {
        List<String> list = readJavaFile(mainPath);
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (StringUtils.isNoneBlank()) {
                line = line.replace("${sourceSql}", model.getSource());
                line = line.replace("${transformationSql}", model.getTransformation());
                line = line.replace("${sinkSql}", model.getSink());
//                line = line.replace("public class TableApiModel", "public class TableApiModelTemp");
            }
            list.remove(i);
            list.add(i, line);
        }
        //将原文件删除
        File old = new File(mainPath);
        old.delete();
        contentToJava(mainPath, list);
    }

    public void contentToJava(String filePath, List<String> listStr) {
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


    public List<String> readJavaFile(String file) {
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


    /**
     * @param rootPath    class文件根目录
     * @param targetPath  需要将jar存放的路径
     * @param jarFileName jar文件的名称
     * @Description: 根据class生成jar文件
     */
    public String createTempJar(String rootPath, String targetPath, String jarFileName) throws IOException {
        if (!new File(rootPath).exists()) {
            throw new IOException(String.format("%s路径不存在", rootPath));
        }
        if (StringUtils.isBlank(jarFileName)) {
            throw new NullPointerException("jarFileName为空");
        }
        //生成META-INF文件
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        //manifest.getMainAttributes().putValue("Main-Class", "Show");//指定Main Class
        //创建临时jar
        File jarFile = File.createTempFile("edwin-", ".jar", new File(System.getProperty("java.io.tmpdir")));
        JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        createTempJarInner(out, new File(rootPath), "");
        out.flush();
        out.close();
        //程序结束后，通过以下代码删除生成的jar文件
       /* Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                jarFile.delete();
            }
        });*/
        //生成目标路径
        File targetFile = new File(targetPath);
        if (!targetFile.exists()) targetFile.mkdirs();
        File targetJarFile = new File(targetPath + File.separator + jarFileName + ".jar");
        if (targetJarFile.exists() && targetJarFile.isFile()) targetJarFile.delete();
        FileUtils.moveFile(jarFile, targetJarFile);
        //jarFile.renameTo(new File(""));
        return targetJarFile.getAbsolutePath();
    }

    /**
     * @param out  文件输出流
     * @param f    文件临时File
     * @param base 文件基础包名
     * @return void
     * @Description: 生成jar文件
     */
    private void createTempJarInner(JarOutputStream out, File f,
                                    String base) throws IOException {

        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            if (base.length() > 0) {
                base = base + "/";
            }
            for (int i = 0; i < fl.length; i++) {
                createTempJarInner(out, fl[i], base + fl[i].getName());
            }
        } else {
            out.putNextEntry(new JarEntry(base));
            FileInputStream in = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int n = in.read(buffer);
            while (n != -1) {
                out.write(buffer, 0, n);
                n = in.read(buffer);
            }
            in.close();
        }
    }

    /**
     * 获取文件夹下所有文件，包括子目录下文件
     *
     * @return
     * @author hsj
     */
    public List<File> getAllFile(File rootFile, String end) {
        List<File> files = new ArrayList<>();
        if (rootFile == null) {
            return null;
        }
        if (rootFile.isDirectory()) {
            File[] fileArr = rootFile.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File file = fileArr[i];
                if (fileArr[i].isDirectory()) {
                    files.addAll(getAllFile(file, end));
                } else {
                    if (file.getPath().endsWith(end)) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    /**
     * 获取文件夹下所有文件，包括子目录下文件
     *
     * @return
     * @author hsj
     */
    public String getAllFileName(File rootFile, String end) {
        String files = "";
        if (rootFile == null) {
            return "";
        }
        if (rootFile.isDirectory()) {
            File[] fileArr = rootFile.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File file = fileArr[i];
                if (fileArr[i].isDirectory()) {
                    files += getAllFile(file, end);
                } else {
                    if (file.getPath().endsWith(end)) {
                        files += file + ";";
                    }
                }
            }
        }
        return files;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public void copyFolder(String resource, String target) throws Exception {

        File resourceFile = new File(resource);
        if (!resourceFile.exists()) {
            throw new Exception("源目标路径：[" + resource + "] 不存在...");
        }
        File targetFile = new File(target);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }

        // 获取源文件夹下的文件夹或文件
        File[] resourceFiles = resourceFile.listFiles();

        for (File file : resourceFiles) {

            File file1 = new File(targetFile.getAbsolutePath() + File.separator + resourceFile.getName());
            // 复制文件
            if (file.isFile()) {
                System.out.println("文件" + file.getName());
                // 在 目标文件夹（B） 中 新建 源文件夹（A），然后将文件复制到 A 中
                // 这样 在 B 中 就存在 A
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                File targetFile1 = new File(file1.getAbsolutePath() + File.separator + file.getName());
                copyFile(file, targetFile1);
            }
            // 复制文件夹
            if (file.isDirectory()) {// 复制源文件夹
                String dir1 = file.getAbsolutePath();
                // 目的文件夹
                String dir2 = file1.getAbsolutePath();
                copyFolder(dir1, dir2);
            }
        }

    }

    public void copyFile(File resource, File target) throws Exception {
        // 输入流 --> 从一个目标读取数据
        // 输出流 --> 向一个目标写入数据

        long start = System.currentTimeMillis();

        // 文件输入流并进行缓冲
        FileInputStream inputStream = new FileInputStream(resource);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        // 文件输出流并进行缓冲
        FileOutputStream outputStream = new FileOutputStream(target);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        // 缓冲数组
        // 大文件 可将 1024 * 2 改大一些，但是 并不是越大就越快
        byte[] bytes = new byte[1024 * 2];
        int len = 0;
        while ((len = inputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, len);
        }
        // 刷新输出缓冲流
        bufferedOutputStream.flush();
        //关闭流
        bufferedInputStream.close();
        bufferedOutputStream.close();
        inputStream.close();
        outputStream.close();

        long end = System.currentTimeMillis();

        System.out.println("耗时：" + (end - start) / 1000 + " s");

    }

}

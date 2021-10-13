package com.yp.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class FileUtils {
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
        //创建临时jar
        File jarFile = File.createTempFile("edwin-", ".jar", new File(System.getProperty("java.io.tmpdir")));
        JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        createTempJarInner(out, new File(rootPath), "");
        out.flush();
        out.close();
        //生成目标路径
        File targetFile = new File(targetPath);
        if (!targetFile.exists()) targetFile.mkdirs();
        File targetJarFile = new File(targetPath + File.separator + jarFileName + ".jar");
        if (targetJarFile.exists() && targetJarFile.isFile()) targetJarFile.delete();
        org.apache.commons.io.FileUtils.moveFile(jarFile, targetJarFile);
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

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
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
        File[] resourceFiles = resourceFile.listFiles();
        for (File file : resourceFiles) {
            File file1 = new File(targetFile.getAbsolutePath() + File.separator + resourceFile.getName());
            if (file.isFile()) {
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                File targetFile1 = new File(file1.getAbsolutePath() + File.separator + file.getName());
                copyFile(file, targetFile1);
            }
            if (file.isDirectory()) {// 复制源文件夹
                String dir1 = file.getAbsolutePath();
                String dir2 = file1.getAbsolutePath();
                copyFolder(dir1, dir2);
            }
        }
    }

    public void copyFile(File resource, File target) throws Exception {
        long start = System.currentTimeMillis();
        FileInputStream inputStream = new FileInputStream(resource);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        FileOutputStream outputStream = new FileOutputStream(target);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        byte[] bytes = new byte[1024 * 2];
        int len = 0;
        while ((len = inputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, len);
        }
        bufferedOutputStream.flush();
        bufferedInputStream.close();
        bufferedOutputStream.close();
        inputStream.close();
        outputStream.close();
        long end = System.currentTimeMillis();
    }
}

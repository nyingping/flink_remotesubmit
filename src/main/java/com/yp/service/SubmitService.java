package com.yp.service;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.yp.model.SubmitModel;
import com.yp.service.CreateJar.CreateJarService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmitService {

    @Autowired
    private CreateJarService createJarService;

    /**
     * 任务提交后，flink展示相应任务界面的地址
     */
    @Value("${flink_mission_ui_rul}")
    private String flinkMissionUiUrl;

    /**
     * flink任务打包成功后上传到flink集群的相应请示地址
     */
    @Value("${flink_jar_upload_url}")
    private String flinkJarUploadUrl;

    /**
     * flink任务打包后的路径
     */
    @Value("${flink_mission_jar_path}")
    private String flinkMissionJarPath;


    @Value("${submit_mission_url}")
    private String submitMissionUrl;


    public String service(SubmitModel model) throws Exception {
//        replace(model);
        //1.生成jar包
        String jarPath = createJarService.execute();
        //2.上传jar包
        String flinkJarName = UploadJar(jarPath);
        System.out.println(flinkJarName);
        //3.提交任务
        String param = "?paramentry-class=" + model.getMainClass() + "&parallelism=" + model.getParallelism();
        param += "&program-args=--- " + model.getSource()
                + " --- " + model.getTransformation()
                + " --- " + model.getSink();
//        param += "&program-args=--- 1"
//                + " --- 2"
//                + " --- 3";
        String url = submitMissionUrl.replace("{flinkJarName}", flinkJarName)
                + param.replaceAll("\r","").replaceAll("\n","");
        System.out.println("url = " + url);
        String jobId = HttpUtil.post(url, "");
        if (jobId.contains("error")) {
            throw new Exception(jobId);
        }
        return flinkMissionUiUrl.replace("{jobId}", jobId);
    }

    public void replace (SubmitModel model) {
        List<String> list = readJavaFile("D:\\ideaproject\\test\\flink_remotesubmit\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModel.java");

        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (StringUtils.isNoneBlank()) {
                line = line.replace("${sourceSql}", model.getSource().replaceAll("\r","").replaceAll("\n",""));
                line = line.replace("${transformationSql}", model.getTransformation().replaceAll("\r","").replaceAll("\n",""));
                line = line.replace("${sinkSql}", model.getSink().replaceAll("\r","").replaceAll("\n",""));
                line = line.replace("public class TableApiModel","public class TableApiModelTemp");
            }
            list.remove(i);
            list.add(i, line);
        }
        File file = new File("D:\\ideaproject\\test\\flink_remotesubmit\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java");
        if (new File("D:\\ideaproject\\test\\flink_remotesubmit\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java").exists()) {
            file.delete();
        }
        contentToJava("D:\\ideaproject\\test\\flink_remotesubmit\\src\\main\\java\\com\\yp\\flink\\model\\TableApiModelTemp.java", list);
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

    public String UploadJar(String fileName) {
        //设置file的name，路径
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("filename", fileName);
        String contentType = "";
        String ret = formUpload(flinkJarUploadUrl, fileMap, contentType);
        Gson gson = new Gson();
        HashMap<String, String> result = gson.fromJson(ret, HashMap.class);
        if ("success".equals(result.get("status"))) {
            return result.get("filename").split("/")[4];
        }
        return null;
    }

//    public void  s (){
//        HttpPost uploadFile = new HttpPost("/jars/upload");
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.addBinaryBody(
//                "jarfile",
//                new FileInputStream(f),
//                ContentType.create("application/x-java-archive"),
//                f.getName()
//        );
//
//        HttpEntity multipart = builder.build();
//        uploadFile.setEntity(multipart);
//        CloseableHttpResponse response = restClient.execute(uploadFile);
//        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//    }

    public String formUpload(String urlStr,
                             Map<String, String> fileMap, String contentType) {
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text

            // file
            if (fileMap != null) {
                Iterator iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();

                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    contentType = new MimetypesFileTypeMap().getContentType(file);
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            System.out.println("发送POST请求出错。" + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }

}

# flink_remotesubmit

**基于flink table api，传入相应sql,打包任务并提交到flink集群**  


1.**配置**
需要修改application的以下4个配置  
分别是  
- flink集群的访问地址，
- 布置WEB服务的根目录地址，
- 编译flink任务所依赖的jar包目录，
- flink任务main class，也就是flink任务模板的路径(不要写成包名的格式)
~~~
flinkurl=http://localhost:8081
rootpath=D://ideaproject//test//flink_remotesubmit
dependmentpath=D://ideaproject//test//flink_remotesubmit//lib
mainpath=//src//main//java//com//yp//flink//model//TableApiModel.java
~~~  
2.**界面如下**
![](https://img2020.cnblogs.com/blog/600147/202110/600147-20211014145616840-2104441371.png)  


![](https://img2020.cnblogs.com/blog/600147/202110/600147-20211014145526544-1081096030.png)  

提交成功后，请确保浏览器没有阻止弹窗，跳转到flink任务界面  
3.**环境**
基于
- flink 1.13.2
- spring boot 2.5

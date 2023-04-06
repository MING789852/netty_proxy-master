

# netty_proxy-master

基于netty实现内网穿透

# 使用方法

一、进入netty_proxy_common，执行

```
mvn clean
mvn install
```

二、进入netty_proxy_server，执行

```
mvn clean
mvn install
```

打包后进入target文件夹使用java -jar proxy.jar启动服务端

三、进入netty_proxy_client，执行

```
mvn clean
mvn install
```

打包后进入target文件夹使用java -jar client.jar启动客户端

四、进入proxy_web，执行

```
npm install
npm run dev
```

五、打开浏览器输入http://localhost:8080/#/login

登录界面

![](https://github.com/MING789852/-/blob/main/%E7%99%BB%E5%BD%95%E7%95%8C%E9%9D%A2.png)

实例

![](https://github.com/MING789852/-/blob/main/%E5%AE%9E%E4%BE%8B.png)

新建

![](https://github.com/MING789852/-/blob/main/%E6%96%B0%E5%BB%BA.png)

删除禁用

![](https://github.com/MING789852/-/blob/main/%E5%88%A0%E9%99%A4%E7%A6%81%E7%94%A8.png)

使用例子（远程桌面）

![](https://github.com/MING789852/-/blob/main/%E8%BF%9C%E7%A8%8B%E6%A1%8C%E9%9D%A2.png)

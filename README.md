>git地址 [https://github.com/jamienlu/dlsbookshelf](https://github.com/jamienlu/dlsbookshelf)
# 相关功能说明

```plain
io/github/jamienlu/dlsbookshelf/api  传参和返回结果对象封装
io/github/jamienlu/dlsbookshelf/cluster 集群相关 集群通信和快照生成等 jraft实现
io/github/jamienlu/dlsbookshelf/conf 配置 主要配置注册服务定义的参数和相关序列化
io/github/jamienlu/dlsbookshelf/model 模型 定义服务实例
io/github/jamienlu/dlsbookshelf/service 服务方法 注册订阅等
```
## 使用示例

```plain
<dependency>
  <groupId>io.github.jamienlu</groupId>
  <artifactId>dlsbookshelf</artifactId>
  <version>0.0.1-SNAPSHOT</version>
<dependency>


application.yml文件配置
registry:
  group-id: default
  port: 8710
  node-path: /opt/registry/node1
  server-list: 192.168.0.101:8710,192.168.0.101:8711,192.168.0.101:8712


registry:
  group-id: default
  port: 8711
  node-path: /opt/registry/node1
  server-list: 192.168.0.101:8710,192.168.0.101:8711,192.168.0.101:8712


registry:
  group-id: default
  port: 8712
  node-path: /opt/registry/node1
  server-list: 192.168.0.101:8710,192.168.0.101:8711,192.168.0.101:8712


```



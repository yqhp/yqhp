## yqhp

高代码自动化平台

## 最新版本

| 服务      | 描述               | 最新版本  | 依赖中间件                                 |
|---------|------------------|-------|---------------------------------------|
| gateway | 请求入口，统一认证授权与请求转发 | 0.0.1 | nacos                                 |
| auth    | 用户认证授权与相关信息管理    | 0.0.1 | nacos, mysql, redis                   |
| file    | 文件存储服务           | 0.0.1 | nacos, minio                          |
| console | 业务核心服务           | 0.0.1 | nacos, mysql, redis, kafka, zookeeper |
| agent   | 自动化执行引擎          | 0.0.1 | nacos, kafka, zookeeper               |

## agent plugins

| 渠道  | 插件     | 描述           | 最新版本  |  
|-----|--------|--------------|-------|
| 官方  | appium | 简化appium api | 0.0.1 |

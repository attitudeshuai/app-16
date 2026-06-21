# CommuteCarpool - 通勤拼车便签板

一句话介绍：公司园区/社区内部通勤拼车黑板，路线相近的人发布空座或寻找拼车，分摊油费、减少拥堵。

## 功能亮点

- 轻量拼车撮合，支持固定路线发布与订阅
- 拼车费用自动估算，按座位分摊
- 拼车小组打卡与评价体系
- 路线热度统计与数据看板
- JWT 鉴权保障数据安全

## 技术栈

Java 17 / Spring Boot 3.2 / Spring Data JPA / MySQL 8.0 / JWT / SpringDoc OpenAPI / Docker

## 目录结构

```
├── src/main/java/com/commutecarpool/
│   ├── config/          # 配置类（安全、JWT、OpenAPI）
│   ├── controller/      # REST 控制器
│   ├── dto/             # 数据传输对象
│   ├── entity/          # JPA 实体
│   ├── exception/       # 全局异常处理
│   ├── repository/      # 数据访问层
│   ├── security/        # JWT 认证过滤器
│   ├── service/         # 业务逻辑层
│   └── util/            # 工具类
├── src/main/resources/
│   └── application.yml  # 应用配置
├── src/test/            # 测试代码
├── docker-compose.yml   # 容器编排
├── Dockerfile           # 多阶段构建
├── init.sql             # 初始化数据
├── pom.xml              # Maven 依赖
└── postman_collection.json
```

## 快速启动

```bash
# 克隆项目
git clone <repo-url>
cd CommuteCarpool

# Docker 一键启动
docker-compose up --build -d

# 查看日志
docker-compose logs -f app

# 验证健康检查
curl http://localhost:8086/actuator/health
```

## API 文档

启动后访问：http://localhost:8086/swagger-ui.html

## 测试

```bash
# Maven 测试
mvn test

# 导入 Postman 集合
# 导入 postman_collection.json 文件，按顺序执行请求
```

## 停止服务

```bash
docker-compose down -v
```

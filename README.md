# 优选商城 - 后端服务 (e-commerce-backend)

基于 SpringBoot + MyBatis-Plus 的电商系统后端，包含后台管理接口与 C 端商城接口，配套离线 ETL 数据分析。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | SpringBoot 2.3.7 |
| ORM | MyBatis-Plus 3.x |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 接口文档 | Swagger 2 (springfox) |
| 构建工具 | Maven 3.6 |
| JDK | 1.8 |

## 功能模块

### 后台管理
- **商品体系**：分类管理（三级树）、品牌管理（含 logo 上传与分类关联）、属性分组、规格参数、销售属性、SPU/SKU 管理、商品发布（支持草稿与定时发布）
- **订单管理**：订单列表（多条件检索）、订单状态流转、发货（自动生成物流+站内消息）、退款流程、订单操作日志、批量操作
- **用户体系**：后台用户管理（支持多角色绑定）、角色管理、权限控制（按角色过滤菜单与快速入口）、部门管理（树形结构）
- **会员管理**：C 端会员列表、积分管理（含积分流水）、会员状态批量操作
- **营销体系**：促销活动管理、优惠券管理、广告管理（含点击量统计与定时发布）
- **交易管理**：支付记录、物流跟踪、发票管理（支持开票与 PDF 上传/预览）
- **客服系统**：会话列表、聊天工作台、会话质量统计、转化率分析
- **内容管理**：评论管理、消息通知、浏览历史、收藏管理

### C 端商城
- 商品浏览（分类筛选、搜索、促销价实时计算）
- 购物车、下单、支付模拟
- 优惠券领取与使用
- 收藏、浏览历史
- 会员注册/登录、个人中心
- 客服咨询入口

### 数据分析（阶段二）
- 用户全链路行为漏斗
- 订单数据分析（流失分析、客单价分布）
- 用户评价分析（好/中/差分类汇总）
- 客服服务质量分析
- 会员画像、地域分布、商品分析
- 搜索行为分析（热搜词、无结果占比）
- 清洗前后 A/B 对照、数据质量稽核（DQC）

## 项目结构

```
mall-sys/
├── src/main/java/com/gec/
│   ├── components/      # 通用组件（鉴权拦截器、文件上传模板、全局异常处理）
│   ├── config/          # 配置类（MyBatis、Swagger、Web、Redis）
│   ├── controller/      # 控制层（30+ 模块接口）
│   ├── dao/             # 数据访问层（Mapper）
│   ├── domain/          # 实体类 / VO / 搜索条件
│   ├── service/         # 业务逻辑层
│   ├── util/            # 工具类
│   └── MallAPP.java     # 启动类
├── src/main/resources/
│   ├── mapper/          # MyBatis XML 映射
│   ├── application.yml  # 应用配置
│   └── schema.sql       # 数据库 DDL
└── pom.xml
```

## 快速开始

### 1. 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 2. 初始化数据库
```bash
mysql -uroot -p < src/main/resources/schema.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`，配置数据库与 Redis 连接信息。

### 4. 启动
```bash
cd mall-sys
mvn spring-boot:run
```
服务启动后访问：
- 接口文档：http://localhost:8090/mall-sys/swagger-ui/index.html
- 后端端口：8090

## 鉴权说明
- 后台接口通过 `AuthInterceptor` 校验 Token，白名单内接口（登录、C 端商城、图片回显、Swagger）放行
- C 端商城接口对外开放，会员态由前端维护

## 配套前端
前端项目地址：[e-commerce-frontend](https://github.com/jinember/e-commerce-frontend)

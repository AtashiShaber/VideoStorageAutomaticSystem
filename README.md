# VideoStorageAutomaticSystem

轻量化个人视频整理与分类系统，适用于本地视频归档、分类、搜索和元数据管理。

## 项目整体体系

该项目是一个“本地文件整理 + 元数据索引”的轻量后端系统，整体架构分为四层：

- Controller：对外提供 REST API
- Service：负责业务逻辑，例如批量归档、搜索、保存元数据
- Mapper：通过 MyBatis 访问数据库
- Database：使用 H2 嵌入式文件数据库保存结构化信息

核心流转如下：

1. 前端或调用方选中某些视频文件
2. 程序读取 currentDirectory 中的文件
3. 根据 vType、vAuthor、vSeries、vSeason 生成归档目录
4. 将文件移动到 rootDirectory 下的最终存储位置
5. 将元数据写入 H2 数据库
6. 通过统一关键字搜索检索 type、author、series、tag 等字段

## 系统目标
- 适合单机、个人使用，无需独立数据库服务
- 不依赖 PostgreSQL 等外部数据库环境
- 归档目录清晰，便于本地浏览和管理
- 支持搜索框直接输入关键词进行模糊检索

## 运行方式

### 1. 环境要求
- Java 17
- Gradle

### 2. 启动项目
```bash
cd /workspaces/VideoStorageAutomaticSystem
export JAVA_HOME=/usr/local/sdkman/candidates/java/17.0.20-tem
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew bootRun
```

### 3. 访问应用
默认启动后，可以访问：

```text
http://localhost:8080
```

### 4. H2 控制台
数据库使用 H2 嵌入式文件数据库，配置如下：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/videostorage;AUTO_SERVER=TRUE;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
```

控制台入口：

```text
http://localhost:8080/h2-console
```

默认账户：
- 用户名：sa
- 密码：空

数据文件会保存在项目根目录下的：

```text
./data/videostorage
```

## 归档规则

文件会按以下路径结构组织：

```text
rootDirectory/
  vType/
    vAuthor/
      vSeries/
        vSeason/
          file.mp4
```

### 具体规则
- rootDirectory 是最终存储根目录
- vType 是一级分类，如 Anime、Movie
- vAuthor 是作者/公司/团队
- 若存在 vSeries，则继续放入 series 目录
- 若存在 vSeason，则继续放入 season 目录
- 若 vSeries 与 vSeason 都存在，文件名优先使用 vNumber，例如：05.mp4
- 若缺少 vSeries 或 vSeason，则使用 snowflake 随机名称
- tag 不参与目录层级，仅作为查询字段

## 目录结构示例
```text
/storage/
  Anime/
    Studio_A/
      One_Piece/
        S1/
          05.mp4
    Studio_B/
      film-name.mp4
```

## 搜索规则

搜索框中输入关键词时，系统会在多个字段中做模糊匹配：

- type
- author
- series
- tag
- name
- rank
- season

示例：

```http
GET /videos/search?keyword=anime
GET /videos/search?q=studio
GET /videos/search?keyword=piece
GET /videos/search?keyword=adventure
```

设计目标：
- 用户不需要选择某个具体字段
- 输入一个关键信息即可自动检索相关视频

## 数据库方案

本项目当前采用的是 H2 嵌入式数据库，而不是独立 PostgreSQL。这样可以做到：

- 无需安装数据库服务
- 打包后直接运行
- 数据保存在本地文件中
- 适合个人视频整理场景

## 业务流程概览

### 分类与存储流程
```text
选中文件 -> 读取 currentDirectory -> 读取类型/作者/系列/季 -> 生成目标目录 -> 移动文件 -> 保存元数据库 -> 返回结果
```

### 检索流程
```text
输入关键词 -> 统一模糊匹配 -> 返回符合条件的记录 -> 前端展示结果
```

## 主要 API

### 文件归档
```http
POST /videos/classify
```

### 批量归档并保存元数据
```http
POST /videos/batch
```

### 搜索
```http
GET /videos/search?keyword=anime
GET /videos/search?q=one piece
```

## 说明

该项目的定位是“轻量、个人、本地化”的视频整理工具，不强调大规模并发、分布式部署或复杂企业级数据库架构；它更注重：

- 本地文件归档的稳定性
- 关键字搜索的自然体验
- 无数据库安装成本
- 打包即用的便携性

## 已验证状态

当前项目已验证可通过针对性单测；相关验证命令已执行并成功，输出为：

```text
BUILD SUCCESSFUL
```

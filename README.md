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

## 环境要求

- Java 17，建议使用 Temurin 17
- Windows 10/11、Linux 或 macOS
- 运行用户对视频来源目录和存储目录具有读写权限
- 项目自带 Gradle Wrapper，不需要单独安装 Gradle

如果需要使用 Windows 资源管理器目录选择或系统默认播放器，Spring Boot 必须直接运行在有桌面的 Windows 本机上。Docker、WSL、远程 Linux 或无图形界面服务器无法打开 Windows 资源管理器和默认播放器。

检查 Java：

```bash
java -version
```

输出应为 Java 17。

## 运行方式

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

根目录的 `index.html` 是单文件前端，包含归档页和管理页。推荐复制到 Spring Boot 静态资源目录后访问：

```bash
mkdir -p src/main/resources/static
cp index.html src/main/resources/static/index.html
```

Windows PowerShell：

```powershell
New-Item -ItemType Directory -Force src/main/resources/static
Copy-Item index.html src/main/resources/static/index.html -Force
```

然后访问 `http://localhost:8080/`。如果使用 Live Server 等其他端口单独打开页面，首次设置中的 API 地址必须填写 `http://localhost:8080`，不能填写页面所在的端口。

后端未启动时页面仍可选择文件、编辑信息、保存路径和保存离线归档草稿；实际移动文件需后端上线后执行。

顶部“进入视频管理”按钮可以在归档页和管理页之间切换。管理页支持默认隐藏 18+、查看全部记录、编辑元数据、删除数据库记录和本地文件，以及请求运行后端的操作系统默认播放器打开视频。路径设置中的“从资源管理器选择”由 Spring Boot 进程在本机弹出目录选择器。

## Windows 启动

在 Windows 项目目录打开 PowerShell：

```powershell
./gradlew.bat bootRun
```

如果 Java 17 没有加入环境变量：

```powershell
$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-17"
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"
./gradlew.bat bootRun
```

后端启动成功后访问：

```text
http://localhost:8080/
```

## 前端使用

1. 在归档页选择视频文件或文件夹。
2. 设置 Video 存储根目录和当前来源目录。
3. 填写类型、评级、作者、标签、系列和季。
4. 在逐文件设置中填写名称或编号，二者不能同时填写。
5. 点击“批量归档并保存元数据”执行移动并写入数据库。
6. 点击“进入视频管理”查看、搜索、编辑、播放或删除视频。

管理页默认隐藏评级为 `18+` 的记录，可以切换为显示全部评级。删除默认同时删除数据库记录和本地文件。

后端未启动时，页面仍可选择文件、编辑信息、保存路径和保存离线草稿；后端启动后才能实际移动文件和保存数据库记录。

## 打包和发布

建议先把前端复制到 Spring Boot 静态资源目录：

```bash
mkdir -p src/main/resources/static
cp index.html src/main/resources/static/index.html
```

Windows PowerShell：

```powershell
New-Item -ItemType Directory -Force src/main/resources/static
Copy-Item index.html src/main/resources/static/index.html -Force
```

运行测试：

```bash
./gradlew test
```

Windows：

```powershell
.\gradlew test
```

构建可执行 JAR：

```bash
./gradlew bootJar
```

Windows：

```powershell
.\gradlew bootJar
```

生成文件：

```text
build\\libs\\VideoStorageAutomaticSystem-0.1.0.jar
```

运行打包文件：

```bash
java -jar build/libs/VideoStorageAutomaticSystem-0.1.0.jar
```

Windows PowerShell：

```powershell
java -jar .\\build\\libs\\VideoStorageAutomaticSystem-0.1.0.jar
```

建议在项目或 JAR 所在目录启动程序，H2 数据默认保存在当前目录的 `data\\videostorage`。迁移程序时请同时备份 `data` 目录和视频存储目录。

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
- 若用户传入 vName 且未设置 vSeries / vSeason / vNumber，则以用户指定的名称作为文件名
- 若用户同时设置了 vName 与 vSeries / vSeason / vNumber，则系统拒绝该配置并抛出异常
- tag 不参与目录层级，仅作为查询字段

### 用户覆盖规则
- vName：若不为空，且没有 vSeries / vSeason / vNumber，则使用该值作为文件名
- vType：若不为空，则按用户值生成目录和分类
- vSeries / vSeason / vNumber：若不为空，则按设置值指定路径和命名
- vTag：若不为空，则写入元数据，作为搜索字段
- 如果某字段为空，则回退到默认逻辑

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

### 管理
```http
GET /videos
GET /videos/{id}
PUT /videos/{id}
DELETE /videos/{id}?deleteLocalFile=true
GET /videos/{id}/file
POST /videos/{id}/open
GET /videos/choose-directory
```

## 常见问题

### 页面提示 `Unexpected token '<'`

这通常表示页面请求到了静态服务器返回的 HTML，而不是后端 JSON。打开“存储设置”，将后端 API 地址改为：

```text
http://localhost:8080
```

如果页面通过 Live Server 的 `5500` 端口打开，API 地址仍然应该是 `8080`。

### 目录选择器打不开

确认后端已经启动，并且 Spring Boot 运行在 Windows 桌面本机。运行在 Docker、WSL、Linux 服务器或无图形界面的环境中时，请手动填写 Windows 路径。

### 播放按钮没有打开播放器

播放按钮请求 `/videos/{id}/open`，由后端所在操作系统打开默认播放器。视频文件和后端必须位于同一台电脑，并且运行用户有权访问该文件。

### 修改 index.html 后页面没有变化

如果前端已经复制到 `src/main/resources/static`，需要重新复制并重新打包：

```bash
cp index.html src/main/resources/static/index.html
./gradlew bootJar
```

浏览器端可使用 `Ctrl + F5` 强制刷新。

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

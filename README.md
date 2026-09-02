# VideoStorageAutomaticSystem

轻量化个人视频整理与分类系统，目标是：
- 从指定目录选中视频文件
- 按 type、author、series、season 进行本地归档
- 使用标签作为查询/筛选字段，不参与目录层级
- 采用嵌入式本地数据库，打包后无需独立数据库环境即可运行

## 设计原则
- 以个人本地整理为核心，不依赖独立数据库服务
- 文件归档结构：rootDirectory -> type -> author -> [series -> season]
- 若 series + season 同时存在，则文件名使用 number，否则使用 snowflake 随机名称
- tag 仅用于搜索和过滤，不作为物理目录层级
- 打包部署时可直接运行，无需额外安装 PostgreSQL 等数据库

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

## 数据库方案
- 不再依赖独立 PostgreSQL
- 改为嵌入式数据库（如 H2 / SQLite / HSQLDB），用于本地元数据存储
- 便于单机、个人使用场景下直接打包发布和运行

## 运行目标
- 选中文件时，从 currentDirectory 读取
- 归档时，移动到 rootDirectory 的指定分类目录
- 元数据保存在本地嵌入数据库中
- 无需手动配置数据库环境

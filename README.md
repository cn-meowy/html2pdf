# html2pdf

基于 Chromium + Playwright 的 HTML 转 PDF 工具，支持动态渲染后的网页内容导出。

## 背景

针对动态加载的 HTML，需要等待网页渲染完毕后再执行导出，故整体思路围绕 Java 下如何获取渲染完毕的网页内容：

1. Chromium 加载并渲染网页
2. Playwright 操作 Chromium 进行导出

提供两种使用方式：**Web 服务**（HTTP API）与**命令行工具**。

## 功能特性

- 支持本地 HTML 文件与远程 URL 两种输入
- 支持截图导出（将页面截图嵌入 HTML 再转 PDF）
- Web 服务支持流式下载（`stream`）与 Base64 返回（`base64`）
- Web 服务支持静态资源上传与管理
- 内置中文字体（HarmonyOS Sans），解决中文乱码
- Docker 多架构镜像（`linux/amd64` + `linux/arm64`）
- GitHub Action 自动构建发布

## 快速开始

### Docker（推荐）

```bash
# ghcr.io
docker run -d -p 10240:10240 --name html2pdf ghcr.io/<owner>/html2pdf-web:latest

# Docker Hub
docker run -d -p 10240:10240 --name html2pdf <docker-username>/html2pdf-web:latest
```

服务启动后访问 `http://localhost:10240/api`。

### 本地构建运行

前置条件：JDK 17、Maven。

```bash
# 构建
mvn -B -ntp clean package -DskipTests

# 运行 Web 服务（需指定 Chrome 可执行文件路径）
java -jar html2pdf-web/target/html2pdf-web.jar \
     --html2pdf.executable.path=/path/to/chrome \
     --html2pdf.resource.path=/path/to/resources
```

### 命令行

```bash
java -jar html2pdf-command/target/html2pdf-command.jar \
     -P /path/to/chrome \
     -I /path/to/input.html \
     -O /path/to/output.pdf
```

| 参数 | 含义 | 示例 |
|---|---|---|
| `-P` | Chrome 可执行文件路径 | `-P D:\chrome\chrome.exe` |
| `-I` | 输入 HTML（本地路径或 `http` 开头的 URL） | `-I D:\input.html` |
| `-O` | 输出 PDF 路径 | `-O D:\output.pdf` |

## Web API

服务监听端口 `10240`，context-path 为 `/api`。

### 1. HTML 转 PDF

```
POST /api/cov
Content-Type: application/json
```

请求体（[`CovDto`](html2pdf-web/src/main/java/cn/meowy/html2pdfweb/dto/CovDto.java:10)）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `inputType` | string | 输入类型：`L` 本地文件、`R` 远程 URL |
| `input` | string | 输入内容（文件路径或 URL） |
| `outputType` | string | 输出类型：`stream` 流式下载、`base64` 返回 Base64 |
| `output` | string | 输出文件名（`stream` 时作下载文件名；`base64` 时可留空） |
| `screenshot` | boolean | 是否截图模式 |
| `option` | object | Playwright `Page.PdfOptions`，控制页面大小、边距等 |

示例（流式下载）：

```bash
curl -X POST http://localhost:10240/api/cov \
     -H "Content-Type: application/json" \
     -d '{
       "inputType": "R",
       "input": "https://example.com",
       "outputType": "stream",
       "output": "example",
       "screenshot": false,
       "option": {}
     }' --output example.pdf
```

### 2. 上传静态资源

```
POST /api/upload
Content-Type: multipart/form-data
```

| 参数 | 类型 | 说明 |
|---|---|---|
| `files` | file[] | 待上传文件 |
| `path` | string | 目标子目录（默认 `/`） |

```bash
curl -X POST http://localhost:10240/api/upload \
     -F "files=@style.css" \
     -F "path=/assets"
```

### 3. 获取资源列表

```
GET /api/getResource
```

返回资源目录的树形结构（目录为对象，文件为空字符串）。

## 配置项

通过 Spring Boot 启动参数或 `application.yml` 配置：

| 配置项 | 说明 | 默认值 |
|---|---|---|
| `html2pdf.executable.path` | Chrome 可执行文件路径 | 无 |
| `html2pdf.resource.path` | 静态资源目录 | 临时目录下 `resource` |

Docker 镜像中已内置配置：

- Chrome 路径：`/app/browser/chrome-headless-shell/chrome-headless-shell`
- 资源路径：`/app/resources`

## CI/CD

本项目通过 GitHub Action（[`.github/workflows/docker-release.yml`](.github/workflows/docker-release.yml)）自动构建多架构 Docker 镜像并发布。

### 触发方式

- **打 tag 触发**：推送 `v*` 开头的 tag
  ```bash
  git tag v0.0.1
  git push origin v0.0.1
  ```
- **手动触发**：GitHub 仓库 Actions 页 -> 选择 "Docker Release" -> Run workflow，可输入自定义版本号（留空则取仓库最新 tag）

### 镜像地址

| 仓库 | 地址 |
|---|---|
| GitHub Container Registry | `ghcr.io/<owner>/html2pdf-web:<version>` / `:latest` |
| Docker Hub | `<docker-username>/html2pdf-web:<version>` / `:latest` |

支持 `linux/amd64` 与 `linux/arm64` 双架构。版本号从 git tag 提取（去掉 `v` 前缀），通过 Maven `flatten-maven-plugin` + `-Drevision=` 注入。

### 前置配置（必需 Secret）

在 GitHub 仓库 Settings -> Secrets and variables -> Actions 添加：

| Secret | 用途 |
|---|---|
| `DOCKER_USERNAME` | Docker Hub 用户名（同时作为镜像 owner） |
| `DOCKER_TOKEN` | Docker Hub 访问令牌 |

ghcr.io 使用内置 `GITHUB_TOKEN`，无需额外配置。

### 本地多架构构建（可选）

```bash
# 单架构（amd64）
docker build -t html2pdf-web:local --build-arg TARGETARCH=amd64 .

# 多架构（需 buildx + QEMU）
docker buildx build --platform linux/amd64,linux/arm64 -t html2pdf-web:local --load .
```

## 项目结构

```
html2pdf
├── html2pdf-common      # 核心转换工具（Html2Pdf）
├── html2pdf-command     # 命令行入口
├── html2pdf-web         # Web 服务（Spring Boot）
├── browser/             # 浏览器内核（Docker 构建时下载）
├── fonts/               # 中文字体（HarmonyOS Sans）
├── www/                 # 前端静态资源
├── Dockerfile           # 多架构构建（amd64 + arm64）
└── .github/workflows/   # CI/CD
```

## 技术栈

- Java 17
- Spring Boot 4.0.3
- Playwright 1.58.0（操作 Chromium）
- Lombok
- Docker（多架构 buildx + QEMU）
- GitHub Actions

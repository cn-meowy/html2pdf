# HTML转PDF工具

## 说明
### 针对动态加载的HTML需要等待网页渲染完毕后再执行导出，故整体思路围绕JAVA下如何获取渲染完毕的网页内容。
*** 工具思路
1. chromium加载渲染网页
2. playwright操作chromium进行导出

## CI/CD

本项目通过 GitHub Action（[`.github/workflows/docker-release.yml`](.github/workflows/docker-release.yml)）自动构建多架构 Docker 镜像并发布。

### 触发方式

- **打 tag 触发**：推送 `v*` 开头的 tag（如 `git tag v0.0.1 && git push origin v0.0.1`）
- **手动触发**：在 GitHub 仓库 Actions 页选择 "Docker Release" -> Run workflow，可输入自定义版本号（留空则取仓库最新 tag）

### 镜像地址

- GitHub Container Registry：`ghcr.io/<owner>/html2pdf-web:<version>` 与 `:latest`
- Docker Hub：`<docker-username>/html2pdf-web:<version>` 与 `:latest`

支持 `linux/amd64` 与 `linux/arm64` 双架构。

### 前置配置（必需 Secret）

在 GitHub 仓库 -> Settings -> Secrets and variables -> Actions -> New repository secret，添加：

| Secret 名 | 用途 | 获取方式 |
|---|---|---|
| `DOCKER_USERNAME` | Docker Hub 用户名（同时作为镜像 owner） | Docker Hub 账号 |
| `DOCKER_TOKEN` | Docker Hub 访问令牌 | Docker Hub -> Account Settings -> Security -> New Access Token |

ghcr.io 使用内置 `GITHUB_TOKEN`，无需额外配置。

### 本地多架构构建（可选）

```bash
# 仅 amd64
docker build -t html2pdf-web:local --build-arg TARGETARCH=amd64 .

# 多架构（需 buildx + QEMU）
docker buildx build --platform linux/amd64,linux/arm64 -t html2pdf-web:local --load .
```

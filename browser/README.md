## 浏览器内核

镜像内通过 Google Chrome 官方 `.deb` 源安装稳定版，二进制统一位于 `/opt/google/chrome/chrome`，
同时支持 `amd64` 与 `arm64` 架构。

> 注意：Chrome for Testing 仅提供 `linux64`（x86_64）的 chrome-headless-shell 构建，
> 不提供 `linux-arm64` 构建，因此多架构镜像不再使用该渠道，改用官方 `.deb` 源。

### 下载地址

```http request
GET https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
GET https://dl.google.com/linux/direct/google-chrome-stable_current_arm64.deb
```

### 版本查询

如需查看最新稳定版版本号，可访问：

```http request
GET https://googlechromelabs.github.io/chrome-for-testing/latest-versions-per-milestone-with-downloads.json
```
```json
{
    "chrome-headless-shell": [
      {
        "platform": "linux64",
        "url": "https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip"
      },
      {
        "platform": "mac-arm64",
        "url": "https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/mac-arm64/chrome-headless-shell-mac-arm64.zip"
      },
      {
        "platform": "mac-x64",
        "url": "https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/mac-x64/chrome-headless-shell-mac-x64.zip"
      },
      {
        "platform": "win32",
        "url": "https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/win32/chrome-headless-shell-win32.zip"
      },
      {
        "platform": "win64",
        "url": "https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/win64/chrome-headless-shell-win64.zip"
      }
    ]
}
```
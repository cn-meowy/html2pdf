## docker build -t html2pdf-web:latest .
## 多架构构建：docker buildx build --platform linux/amd64,linux/arm64 -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
EXPOSE 10240

## buildx 自动注入的目标架构（amd64 / arm64）
ARG TARGETARCH

## 安装基础工具（curl 用于下载，unzip 用于解压字体）
## 说明：Chrome for Testing 不提供 linux-arm64 的 chrome-headless-shell 构建，
##       故改用 Google 官方 .deb 源（amd64 / arm64 均提供），二进制统一位于
##       /opt/google/chrome/chrome，与架构解耦。
RUN apt update \
    && apt install -y curl unzip \
    && case "$TARGETARCH" in \
         amd64) DEB_ARCH=amd64 ;; \
         arm64) DEB_ARCH=arm64 ;; \
         *) echo "不支持的架构: $TARGETARCH" >&2 && exit 1 ;; \
       esac \
    && curl -fsSL "https://dl.google.com/linux/direct/google-chrome-stable_current_${DEB_ARCH}.deb" -o /tmp/chrome.deb \
    && apt install -y /tmp/chrome.deb \
    && rm /tmp/chrome.deb \
    && rm -rf /var/lib/apt/lists/*

## 安装中文字体库
ADD fonts/HarmonyOS_Sans.zip /app/fonts/
RUN unzip -oj /app/fonts/HarmonyOS_Sans.zip -d /usr/local/share/fonts && rm /app/fonts/HarmonyOS_Sans.zip && fc-cache -fv

RUN groupadd -r appuser && useradd -r -g appuser appuser
ADD www/ /app/resources/
RUN mkdir -p /app && chown -R appuser:appuser /app && chmod -R 775 /app

USER appuser
WORKDIR /app
COPY ./html2pdf-web/target/html2pdf-web.jar /app

## Google Chrome .deb 安装后二进制统一位于 /opt/google/chrome/chrome，与架构解耦
CMD ["java", "-jar", "html2pdf-web.jar", \
     "--html2pdf.executable.path=/opt/google/chrome/chrome", \
     "--html2pdf.resource.path=/app/resources", \
     "--spring.profiles.active=dev"]

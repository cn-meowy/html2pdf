## docker build -t html2pdf-web:latest .
## 多架构构建：docker buildx build --platform linux/amd64,linux/arm64 -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
EXPOSE 10240

ARG CHROME_VERSION=147.0.7720.0
ARG TARGETARCH

## 安装解压工具 + Chrome 依赖
RUN apt update \
    && apt install -y unzip curl \
    && apt install -y libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libgtk-3-0 libgbm1 libasound2 \
    && rm -rf /var/lib/apt/lists/*

## 按架构下载 Chrome-headless-shell 并创建统一软链接
RUN case "$TARGETARCH" in \
      amd64) CHROME_ARCH=linux64;     CHROME_FILE=chrome-headless-shell-linux64 ;; \
      arm64) CHROME_ARCH=linux-arm64; CHROME_FILE=chrome-headless-shell-linux-arm64 ;; \
      *) echo "不支持的架构: $TARGETARCH" >&2 && exit 1 ;; \
    esac \
    && ADD_URL="https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/${CHROME_ARCH}/${CHROME_FILE}.zip" \
    && curl -fsSL "$ADD_URL" -o /tmp/chrome.zip \
    && mkdir -p /app/browser \
    && unzip /tmp/chrome.zip -d /app/browser/ \
    && rm /tmp/chrome.zip \
    && ln -s "/app/browser/${CHROME_FILE}" /app/browser/chrome-headless-shell

## 安装中文字体库
ADD fonts/HarmonyOS_Sans.zip /app/fonts/
RUN unzip -oj /app/fonts/HarmonyOS_Sans.zip -d /usr/local/share/fonts && rm /app/fonts/HarmonyOS_Sans.zip && fc-cache -fv

RUN groupadd -r appuser && useradd -r -g appuser appuser
ADD www/ /app/resources/
RUN mkdir -p /app && chown -R appuser:appuser /app && chmod -R 775 /app

USER appuser
WORKDIR /app
COPY ./html2pdf-web/target/html2pdf-web.jar /app

## 通过软链接统一可执行路径，与架构解耦
CMD ["java", "-jar", "html2pdf-web.jar", \
     "--html2pdf.executable.path=/app/browser/chrome-headless-shell/chrome-headless-shell", \
     "--html2pdf.resource.path=/app/resources", \
     "--spring.profiles.active=dev"]

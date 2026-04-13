## docker build -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
EXPOSE 10240
## 安装解压工具
RUN apt update && apt install -y unzip
## 安装 Chrome 依赖
RUN apt install -y libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libgtk-3-0 libgbm1 libasound2
## 添加浏览器内核
ADD https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip /app/browser/
RUN unzip /app/browser/chrome-headless-shell-linux64.zip -d /app/browser/ && rm /app/browser/chrome-headless-shell-linux64.zip
## 安装中文字体库
ADD fonts/HarmonyOS_Sans.zip /app/fonts/
RUN unzip -oj /app/fonts/HarmonyOS_Sans.zip -d /usr/local/share/fonts && rm /app/fonts/HarmonyOS_Sans.zip && fc-cache -fv
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN mkdir -p /app && chown appuser:appuser /app && chmod 775 /app
## 添加静态资源
ADD www/ /app/resources/
USER appuser
WORKDIR /app
COPY ./html2pdf-web/target/html2pdf-web.jar /app
CMD ["java", "-jar", "html2pdf-web.jar", "--html2pdf.executable.path=/app/browser/chrome-headless-shell-linux64/chrome-headless-shell", "--html2pdf.resource.path=/app/resources", "--spring.profiles.active=dev"]

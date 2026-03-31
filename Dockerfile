## docker build -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
EXPOSE 10240
RUN apt update && apt install -y unzip
## 安装 Chrome 依赖
RUN apt install -y libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libgtk-3-0 libgbm1 libasound2
ADD https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip /app/browser/
RUN unzip /app/browser/chrome-headless-shell-linux64.zip -d /app/browser/ && rm /app/browser/chrome-headless-shell-linux64.zip
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN mkdir -p /app && chown appuser:appuser /app && chmod 775 /app
USER appuser
WORKDIR /app
COPY ./html2pdf-web/target/html2pdf-web.jar /app
CMD ["java", "-jar", "html2pdf-web.jar", "--html2pdf.executable.path=/app/browser/chrome-headless-shell-linux64/chrome-headless-shell", "--spring.profiles.active=dev"]

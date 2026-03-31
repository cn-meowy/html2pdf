## docker build -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
RUN mkdir -p /app
WORKDIR /app
ADD https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip /app/browser/
COPY ./html2pdf-web/target/html2pdf-web.jar /app
EXPOSE 10240
CMD ["java", "-jar", "html2pdf-web.jar", "--html2pdf.executable.path=/app/browser/chrome-headless-shell-linux64/chrome-headless-shell"]

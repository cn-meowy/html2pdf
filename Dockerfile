## docker build -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
EXPOSE 10240
RUN mkdir -p /app
WORKDIR /app
RUN apt update && apt install -y unzip
RUN unzip /app/browser/chrome-headless-shell-linux64.zip -d /app/browser/ && rm /app/browser/chrome-headless-shell-linux64.zip
ADD https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip /app/browser/
COPY ./html2pdf-web/target/html2pdf-web.jar /app
CMD ["java", "-jar", "html2pdf-web.jar", "--html2pdf.executable.path=/app/browser/chrome-headless-shell-linux64/chrome-headless-shell"]

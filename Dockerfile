## docker build -t html2pdf-web:latest .
FROM azul/zulu-openjdk:17
COPY ./html2pdf-web/target/html2pdf-web.jar /app
#COPY ./browser/chrome-headless-shell-linux64.zip /app
WORKDIR /app
RUN wget https://storage.googleapis.com/chrome-for-testing-public/147.0.7720.0/linux64/chrome-headless-shell-linux64.zip
RUN unzip chrome-headless-shell-linux64.zip && rm chrome-headless-shell-linux64.zip
CMD ["java", "-jar", "html2pdf-web.jar", "--html2pdf.executable.path=/app/chrome-headless-shell-linux64/chrome-headless-shell"]

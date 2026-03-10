package cn.meowy.html2pdfweb.common;

import cn.meowy.html2pdf.utils.Html2Pdf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化
 *
 * @author Mr.Zou
 **/
@Configuration
public class Init {

    @Value("${html2pdf.executable.path:}")
    private String executablePath;

    @Bean
    public Html2Pdf html2Pdf() {
        return Html2Pdf.create(executablePath);
    }

}

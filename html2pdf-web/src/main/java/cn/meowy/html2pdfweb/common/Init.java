package cn.meowy.html2pdfweb.common;

import cn.meowy.html2pdf.utils.Html2Pdf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 初始化
 *
 * @author Mr.Zou
 **/
@Configuration
public class Init {

    @Value("${html2pdf.executable.path:}")
    private String executablePath;

    @Value("${html2pdf.resource.path:}")
    private String resourcePath;

    @Bean
    public Html2Pdf html2Pdf() {
        Html2Pdf html2Pdf = Html2Pdf.create(executablePath);
        if (Objects.nonNull(resourcePath) && !resourcePath.isBlank()) {
            try {
                String resource = Html2Pdf.getTempDir() + "/resource";
                new File(resource).deleteOnExit();
                Files.createSymbolicLink(Paths.get(resource), Paths.get(resourcePath));
            } catch (IOException e) {
                throw new RuntimeException("创建符号链接失败", e);
            }
        }
        return html2Pdf;
    }

}

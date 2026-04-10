package cn.meowy.html2pdfweb.api;

import cn.meowy.html2pdf.utils.Html2Pdf;
import cn.meowy.html2pdfweb.dto.CovDto;
import com.microsoft.playwright.Page;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * 接口
 *
 * @author Mr.Zou
 **/
@RestController
public class Api {

    @Resource
    private Html2Pdf html2Pdf;

    @PostMapping("/cov")
    public ResponseEntity<?> cov(@RequestBody CovDto request) {
        try {
            byte[] cov = html2Pdf.cov(request.inputType(), request.input(), request.output(), request.option());
            if ("stream".equalsIgnoreCase(request.outputType())) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (request.output() != null && !request.output().isBlank() ? (request.output().endsWith(".pdf") ? request.output() : request.output() + ".pdf") : "file.pdf") + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(cov);
            } else {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "Success",
                        "type", "base64".equalsIgnoreCase(request.outputType()) ? "data:application/pdf;base64," : request.outputType(),
                        "data", request.output() != null && !request.output().isBlank() ? request.output() : Base64.getEncoder().encodeToString(cov)
                ));
            }
        } catch (Throwable e) {
            return ResponseEntity.ok(Map.of(
                    "code", 500,
                    "msg", e.getLocalizedMessage()
            ));
        }
    }

}

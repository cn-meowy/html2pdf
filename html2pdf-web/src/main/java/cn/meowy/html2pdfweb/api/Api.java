package cn.meowy.html2pdfweb.api;

import cn.meowy.html2pdf.utils.Html2Pdf;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

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
    public ResponseEntity<?> cov(@RequestParam("inputType") String inputType, @RequestParam("input") String input, @RequestParam(value = "outputType", defaultValue = "base64") String outputType, @RequestParam(value = "output", required = false) String output) {
        try {
            byte[] cov = html2Pdf.cov(inputType, input, output);
            if ("stream".equalsIgnoreCase(outputType)) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (output != null && !output.isBlank() ? (output.endsWith(".pdf") ? output : output + ".pdf") : "file.pdf") + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(cov);
            } else {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "Success",
                        "type", "base64".equalsIgnoreCase(outputType) ? "data:application/pdf;base64," : outputType,
                        "data", output != null && !output.isBlank() ? output : Base64.getEncoder().encodeToString(cov)
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

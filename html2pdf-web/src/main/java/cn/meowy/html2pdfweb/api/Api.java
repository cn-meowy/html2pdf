package cn.meowy.html2pdfweb.api;

import cn.meowy.html2pdf.utils.Html2Pdf;
import jakarta.annotation.Resource;
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
    public Map<String, Object> cov(@RequestParam("inputType") String inputType, @RequestParam("input") String input, @RequestParam(value = "output", required = false) String output) {
        try {
            byte[] cov = html2Pdf.cov(inputType, input, output);
            return Map.of(
                    "code", 200,
                    "msg", "Success",
                    "data", output != null && !output.isBlank() ? output : Base64.getEncoder().encodeToString(cov)
            );
        } catch (Throwable e) {
            return Map.of(
                    "code", 500,
                    "msg", e.getLocalizedMessage()
            );
        }
    }

}

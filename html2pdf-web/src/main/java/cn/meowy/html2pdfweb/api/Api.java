package cn.meowy.html2pdfweb.api;

import cn.meowy.html2pdf.utils.Html2Pdf;
import cn.meowy.html2pdfweb.dto.CovDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * 接口
 *
 * @author Mr.Zou
 **/
@Slf4j
@RestController
public class Api {

    @Resource
    private Html2Pdf html2Pdf;

    @Value("${html2pdf.resource.path:}")
    private String resourcePath;


    @PostMapping("/cov")
    public ResponseEntity<?> cov(@RequestBody CovDto request) {
        try {
            byte[] cov = html2Pdf.cov(request.inputType(), request.input(), request.output(), request.option(), request.screenshot());
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
            log.error("PDF转换失败!", e);
            return ResponseEntity.ok(Map.of(
                    "code", 500,
                    "msg", "PDF转换失败!"
            ));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("files") MultipartFile[] files, @RequestParam(value = "path", required = false, defaultValue = "/") String path) {
        try {
            path = path.startsWith("/") ? path.substring(1) : path;
            path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            String dirPath = Objects.nonNull(resourcePath) && !resourcePath.isBlank() ? resourcePath : Html2Pdf.getTempDir() + "/resource";
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            } else if (dir.isFile() && dir.delete() && !dir.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            }
            String targetPath = dirPath + "/" + path;
            File targetDir = new File(targetPath);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            } else if (targetDir.isFile() && targetDir.delete() && !targetDir.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            }
            for (MultipartFile file : files) {
                String targetFilePath = targetPath + "/" + file.getOriginalFilename();
                new File(targetFilePath).deleteOnExit();
                file.transferTo(Paths.get(targetFilePath));
            }
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "Success"
            ));
        } catch (Throwable e) {
            log.error("文件上传失败!", e);
            return ResponseEntity.ok(Map.of(
                    "code", 500,
                    "msg", "文件上传失败!"
            ));
        }
    }

    @GetMapping("/getResource")
    public ResponseEntity<?> getResource() {
        String dirPath = Objects.nonNull(resourcePath) && !resourcePath.isBlank() ? resourcePath : Html2Pdf.getTempDir() + "/resource";
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "msg", "Success",
                "data", resourceList(Objects.requireNonNull(new File(dirPath).listFiles()))
        ));
    }

    private Map<String, Object> resourceList(File[] files) {
        Map<String, Object> result = new HashMap<>();
        for (File file : files) {
            if (file.isDirectory()) {
                result.put(file.getName(), resourceList(Objects.requireNonNull(file.listFiles())));
            } else {
                result.put(file.getName(), "");
            }
        }
        return result;
    }

}

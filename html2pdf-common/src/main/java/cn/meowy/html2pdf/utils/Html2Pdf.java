package cn.meowy.html2pdf.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotAnimations;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 转换工具
 *
 * @author Mr.Zou
 **/
@Slf4j
public class Html2Pdf {

    private static Html2Pdf INSTANCE;
    private static Playwright PLAYWRIGHT;
    private static Browser BROWSER;
    private static BrowserContext CONTEXT;
    private static String TEMP_DIR;

    private Html2Pdf(String executablePath) {
        PLAYWRIGHT = Playwright.create(new Playwright.CreateOptions().setEnv(Map.of(
                "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", Boolean.toString(executablePath != null && !executablePath.isBlank())                                                    // 跳过浏览器内核下载
        )));
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
        if (executablePath != null && !executablePath.isBlank()) {
            log.debug("加载浏览器内核[{}]", executablePath);
            options.setExecutablePath(Paths.get(executablePath));
        } else {
            log.debug("加载默认浏览器内核......");
        }
        options.setHeadless(true);
        BROWSER = PLAYWRIGHT.chromium().launch(options);
        CONTEXT = BROWSER.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080).setDeviceScaleFactor(3D));

    }

    public synchronized static Html2Pdf create(String executablePath) {
        if (INSTANCE == null) {
            INSTANCE = new Html2Pdf(executablePath);
            File html2pdf = null;
            try {
                html2pdf = File.createTempFile("html2pdf", ".html");
                TEMP_DIR = html2pdf.getParent() + "/html2pdf";
            } catch (IOException e) {
                log.error("获取临时目录失败", e);
                throw new RuntimeException("获取临时目录失败");
            } finally {
                if (Objects.nonNull(html2pdf) && html2pdf.exists() && !html2pdf.delete()) {
                    log.debug("删除临时文件失败[{}]", html2pdf);
                }
            }
            File dir = new File(TEMP_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建临时目录失败!");
                throw new RuntimeException("创建临时目录失败!");
            }
        }
        return INSTANCE;
    }

    public byte[] cov(String inputType, String input, String output, Page.PdfOptions pdfOptions, boolean screenshot) {
        List<String> tempFiles = new ArrayList<>();
        try (Page page = CONTEXT.newPage()) {
            if ("L".equalsIgnoreCase(inputType)) {          // local 本地
                page.navigate("file://" + input);
            } else if ("R".equalsIgnoreCase(inputType)) {   // remote 远程
                page.navigate(input);
            } else if ("S".equalsIgnoreCase(inputType)) {   // string 字符串
                page.setContent(input);
            } else {
                log.error("[{}]不支持的输入类型", inputType);
                throw new RuntimeException("不支持的输入类型");
            }
            log.debug("正在加载网页.....");
            page.keyboard().press("End");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            log.debug("网页加载完毕.....");
            if (screenshot) {
                log.debug("正在生成截图.....");
                String screenshotFilename = System.currentTimeMillis() + ".png";
                String screenshotFile = TEMP_DIR + "/" + screenshotFilename;
                tempFiles.add(screenshotFile);
                log.debug("截图: {}", screenshotFile);
                String htmlFile = TEMP_DIR + "/" + System.currentTimeMillis() + ".html";
                tempFiles.add(htmlFile);
                log.debug("HTML: {}", htmlFile);
                Files.writeString(Paths.get(htmlFile), getHtml(screenshotFilename));
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get(screenshotFile))
                        .setAnimations(ScreenshotAnimations.DISABLED)
                        .setFullPage(true)
                );
                try (Page temp = CONTEXT.newPage()) {
                    temp.navigate("file://" + htmlFile);
                    return temp.pdf(pdfOptions);
                }
            } else {
                pdfOptions = Objects.requireNonNullElseGet(pdfOptions, Page.PdfOptions::new);
                pdfOptions.setPath(output == null || output.isBlank() ? null : Paths.get(output));
                log.debug("开始生成PDF[{}].....", output);
                // 生成PDF
                return page.pdf(pdfOptions);
            }
        } catch (Throwable e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException(e);
        } finally {
            log.debug("PDF生成结束[{}].....", output);
            tempFiles.forEach(file -> {
                File f = new File(file);
                if (f.exists() && !f.delete()) {
                    log.debug("删除临时文件失败[{}]", file);
                }
            });
        }
    }

    public void close() {
        CONTEXT.close();
        BROWSER.close();
        PLAYWRIGHT.close();
    }

    private static String getHtml(String screenshotFilename) {
        return "<!DOCTYPE html>\n" +
                "            <html lang=\"en\">\n" +
                "            <head>\n" +
                "                <meta charset=\"UTF-8\">\n" +
                "                <title>Title</title>\n" +
                "                <style>\n" +
                "                    /* 屏幕样式 */\n" +
                "                    body {\n" +
                "                        margin: 0;\n" +
                "                        padding: 0;\n" +
                "                        background-color: #f5f5f5;\n" +
                "                    }\n" +
                "            \n" +
                "                    .container {\n" +
                "                        max-width: 1200px;\n" +
                "                        margin: 0 auto;\n" +
                "                    }\n" +
                "            \n" +
                "                    .image-wrapper {\n" +
                "                        background: white;\n" +
                "                        padding: 0;\n" +
                "                        border-radius: 8px;\n" +
                "                        box-shadow: 0 0 0 rgba(0,0,0,0.1);\n" +
                "                    }\n" +
                "            \n" +
                "                    img {\n" +
                "                        max-width: 100%;\n" +
                "                        height: auto;\n" +
                "                        display: block;\n" +
                "                        margin: 0 auto;\n" +
                "                    }\n" +
                "                </style>\n" +
                "            </head>\n" +
                "            <body>\n" +
                "            <div class=\"container\">\n" +
                "                <div class=\"image-wrapper\">\n" +
                "                    <img src=\"" + screenshotFilename+ "\" alt=\"要打印的图片\" id=\"printImage\">\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            </body>\n" +
                "            </html>";
    }

}

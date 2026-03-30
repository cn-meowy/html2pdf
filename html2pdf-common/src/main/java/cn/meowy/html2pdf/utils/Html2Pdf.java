package cn.meowy.html2pdf.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.Map;

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
        CONTEXT = BROWSER.newContext();
    }

    public synchronized static Html2Pdf create(String executablePath) {
        if (INSTANCE == null) {
            INSTANCE = new Html2Pdf(executablePath);
        }
        return INSTANCE;
    }

    public byte[] cov(String inputType, String input, String output) {
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
            page.waitForLoadState(LoadState.NETWORKIDLE);
            log.debug("网页加载完毕.....");
            Page.PdfOptions pdfOptions = new Page.PdfOptions()
                    .setPath(output == null || output.isBlank() ? null : Paths.get(output))
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true)
                    .setScale(1.0)
                    .setDisplayHeaderFooter(false);
            log.debug("开始生成PDF[{}].....", output);
            // 生成PDF
            return page.pdf(pdfOptions);
        } catch (Throwable e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException(e);
        } finally {
            log.debug("PDF生成结束[{}].....", output);
        }
    }

    public void close() {
        CONTEXT.close();
        BROWSER.close();
        PLAYWRIGHT.close();
    }

}

package cn.meowy.html2pdf;

import cn.meowy.html2pdf.utils.Html2Pdf;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Scanner;

/**
 * 命令行
 *
 * @author Mr.Zou
 **/
@Slf4j
public class Soft {

   static Scanner SCANNER = new Scanner(System.in);

   static Html2Pdf HTML2PDF = Html2Pdf.create(getExecutablePath());


    public static void main(String[] args) {
        log.info("浏览器内核路径为: {}", getExecutablePath());
        while (true) {
            try {
                log.info("请输入需要转换的网页地址(按Q/q退出):");
                String input = getInput();
                if (input != null && !input.isBlank()) {
                    log.info("请输入PDF文件输出路径(按Q/q退出): ");
                    String output = getInput();
                    if (output != null && !output.isBlank()) {
                        HTML2PDF.cov(input.startsWith("http") ? "R" : "L", input, output, new Page.PdfOptions(), false);
                    }
                }
            } catch (Throwable e) {
                log.error("生成PDF失败....", e);
            }
        }
    }

    private static String getInput() {
        String input = SCANNER.next();
        if ("q".equalsIgnoreCase(input)) {
            log.warn("程序即将退出.....");
            HTML2PDF.close();
            System.exit(0);
        }
        return input;
    }


    private static String getSystemName() {
        String name = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (name.contains("windows")) {
            return "win64";
        }
        if (name.contains("linux")) {
            if (arch.equals("aarch64")) {
                return "linux-arm64";
            } else {
                return "linux";
            }
        }
        if (name.contains("mac os x")) {
            if (arch.equals("aarch64")) {
                return "mac-arm64";
            } else {
                return "mac";
            }
        }
        throw new RuntimeException("Unexpected os.name value: " + name);
    }

    public static String getExecutablePath() {
        String path = Soft.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path).getParent() + "/browser/chrome-headless-shell-" + getSystemName() + "/" + "chrome-headless-shell";
    }
}

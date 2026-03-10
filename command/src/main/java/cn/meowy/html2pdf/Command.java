package cn.meowy.html2pdf;

import cn.meowy.html2pdf.utils.Html2Pdf;
import lombok.extern.slf4j.Slf4j;

/**
 * 命令行
 *
 * @author Mr.Zou
 **/
@Slf4j
public class Command {

    public static void main(String[] args) {
        try {
            if (args.length == 0 || args.length % 2 != 0) {
                help();
            } else {
                String executablePath = null;
                String inputHtml = null;
                String outputPdf = null;
                for (int i = 0; i < args.length; i++) {
                    String cmd = args[i];
                    String param = args[++i];
                    if (param == null || param.isBlank() || param.startsWith("-")) {
                        log.warn("指令错误,请检查后重新输入......");
                        help();
                        return;
                    }
                    if (cmd.startsWith("-P")) {
                        executablePath = param;
                    } else if (cmd.startsWith("-I")) {
                        inputHtml = param;
                    } else if (cmd.startsWith("-O")) {
                        outputPdf = param;
                    }
                }
                if (outputPdf == null || outputPdf.isBlank()) {
                    log.error("未指定PDF输出路径!");
                    help();
                    return;
                }
                Html2Pdf html2Pdf = Html2Pdf.create(executablePath);
                html2Pdf.cov(inputHtml != null && inputHtml.startsWith("http") ? "R" : "L", inputHtml, outputPdf);
                html2Pdf.close();
            }
        } catch (Throwable e) {
            log.error("PDF导出失败!", e);
        }
    }

    private static void help() {
        log.warn("------------------------------help------------------------------");
        log.warn("-P  executable path, example: -P D:\\software\\chrome\\chrome.exe");
        log.warn("-I  input html, example: -I D:\\input\\input.html");
        log.warn("-O  output pdf, example: -O D:\\output\\output.pdf");
        log.warn("----------------------------------------------------------------");
    }

}

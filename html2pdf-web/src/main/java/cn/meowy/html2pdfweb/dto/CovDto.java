package cn.meowy.html2pdfweb.dto;

import com.microsoft.playwright.Page;

/**
 * 转换实体
 *
 * @author Mr.Zou
 **/
public record CovDto(String inputType, String input, String outputType, String output, Page.PdfOptions option) {



}

# HTML转PDF工具

## 说明
### 针对动态加载的HTML需要等待网页渲染完毕后再执行导出，故整体思路围绕JAVA下如何获取渲染完毕的网页内容。
*** 工具思路
1. chromium加载渲染网页
2. playwright操作chromium进行导出

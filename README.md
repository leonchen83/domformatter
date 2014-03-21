dom-formatter
============

dom-formatter

version 0.0.1

DOM格式化工具

怎样使用

DomFormatter formatter = new DomFormatter("test1.txt", DomFormatter.class);
formatter.format();

由于采用递归下降进行的解析。太复杂的html解析会爆call stack 。所以运行时最好指定参数-Xss2048k.

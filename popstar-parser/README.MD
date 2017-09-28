# popstar游戏分析程序

该功能用clojure实现了一个简单的popstar游戏分析程序.

## 如何编译该程序
clone代码, 使用leiningen `lein uberjar` 来生成单独的jar文件. jar文件也可以[点击这里](https://github.com/shinima/popstar-parser/releases/download/v0.1/popstar-parser-0.1.0-standalone.jar)下载.

## 如何运行程序
生成jar文件之后, 使用 `java -jar xxx.jar <filename> <panelty>` 来运行程序. filename为游戏的状态文件名称, 默认为`input.json`, 可以从[http://shinima.pw/popstar/](http://shinima.pw/popstar/)拷贝文件的内容, `panelty`为可调参数, 默认为-2.

如果程序运行时间较长, 请尝试调整`panelty`的值. 一般可选下面几个值: `0`, `-2`, `-5`, `-10`.
如果程序运行结果不是很满意, 可以根据最后剩余的星星数量来选取`panelty`值重新尝试. 星星数量较多(20+)的时候, 可以将选择`panelty`为`-10`, 星星数量10+时, 可以选择`panelty`为`-5`, 星星数量接近0时, 可选择`panelty`为`0`重新尝试.

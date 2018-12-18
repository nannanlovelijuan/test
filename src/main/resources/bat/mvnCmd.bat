:: 进入项目根目录
cd D:\vscode\java\kafka-test
:: 清理；由于mvn本身也是BAT文件，并且其结束时执行了exit命令。要让mvn命令不使当前脚本自动退出，只需要在mvn之前加上call命令
echo ---------start clean project---------
call mvn clean
echo ---------start compile project---------
call mvn compile
echo ---------start package project---------
call mvn package
java -cp .\target\kafka-test-1.0-SNAPSHOT.jar www.ezrpro.com.App
pause


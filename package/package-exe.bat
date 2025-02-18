@echo off
rem 首先使用 Maven 打包
call mvn clean package

rem 使用 jpackage 创建运行时镜像
jpackage ^
  --type app-image ^
  --input target/ ^
  --main-jar RequestTool-1.0-SNAPSHOT.jar ^
  --main-class com.esmooc.RequestToolV2 ^
  --name RequestTool ^
  --app-version 1.0.0 ^
  --vendor "Your Company" ^
  --description "HTTP Request Tool" ^
  --icon src/main/resources/icon.ico ^
  --dest target/exe
@echo off
jpackage ^
  --input target/ ^
  --main-jar RequestTool-1.0-SNAPSHOT.jar ^
  --main-class com.esmooc.RequestToolV2 ^
  --name RequestTool ^
  --app-version 1.0.0 ^
  --vendor "1.0" ^
  --description "HTTP Request Tool" ^
  --icon src/main/resources/icon.ico ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu ^
  --win-menu-group "RequestTool"
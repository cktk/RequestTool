#!/bin/bash
jpackage \
  --input target/ \
  --main-jar RequestTool-1.0-SNAPSHOT.jar \
  --main-class com.esmooc.RequestToolV2 \
  --name RequestTool \
  --app-version 1.0.0 \
  --vendor "Your Company" \
  --description "HTTP Request Tool" \
  --icon src/main/resources/icon.icns \
  --mac-package-name "RequestTool" \
  --mac-package-identifier com.esmooc.requesttool
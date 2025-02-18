@echo off
echo Building with Maven...
call mvn clean package

echo Creating standalone EXE...
copy target\RequestTool-1.0-SNAPSHOT.jar target\RequestTool.jar

echo Done! You can find the executable at target\RequestTool.exe
pause 

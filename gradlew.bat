@echo off
set GRADLE_LIB=C:\Users\Administrator\WorkBuddy\2026-05-31-14-41-11\tmp\gradle-dist\gradle-8.5\lib
"%JAVA_HOME%/bin/java.exe" -cp "%GRADLE_LIB%\*" org.gradle.launcher.Main %*

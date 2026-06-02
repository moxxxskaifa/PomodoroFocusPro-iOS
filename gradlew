#!/bin/sh
# Gradle wrapper
# Determines the project&#x27;s Gradle version and runs it

# Determine the Java command
JAVACMD=${JAVA_HOME:+$JAVA_HOME/bin/}"java"
if [ ! -x "$JAVACMD" ]; then
    JAVACMD=java
fi

# Use gradle wrapper jar
APP_HOME=$( cd "${0%[/\]*}" > /dev/null; cd ..; pwd -P 2>/dev/null || pwd )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

"$JAVACMD"     -classpath "$CLASSPATH"     org.gradle.wrapper.GradleWrapperMain     "$@"

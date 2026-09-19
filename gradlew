#!/bin/sh
#
# Gradle wrapper bootstrap - minimal POSIX-compliant version
#

app_path="./gradlew"
APP_HOME="./"
[ -h "${app_path}" ] && app_path=$(readlink "${app_path}")
APP_BASE_NAME=$(basename "${app_path}")
cd "${app_path%/*}" || cd "/"
APP_HOME=$(pwd -P)

CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

# Find Java
if [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    JAVACMD="${JAVA_HOME}/bin/java"
else
    JAVACMD=$(command -v java 2>/dev/null)
    if [ -z "${JAVACMD}" ]; then
        JAVACMD="/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk/bin/java"
    fi
fi

# Default JVM options
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Build argument list: JVM opts + appname + classpath + main class + user args
set -- -Xmx64m -Xms64m
if [ -n "${JAVA_OPTS}" ]; then
    for opt in ${JAVA_OPTS}; do set -- "$opt" "$@"; done
fi
if [ -n "${GRADLE_OPTS}" ]; then
    for opt in ${GRADLE_OPTS}; do set -- "$opt" "$@"; done
fi
set -- -Dorg.gradle.appname="${APP_BASE_NAME}" "$@"
set -- -classpath "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"

exec "${JAVACMD}" "$@"

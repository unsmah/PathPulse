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

# JVM options for the java launcher itself.
# These MUST be passed before -classpath, NOT as arguments to
# GradleWrapperMain (which forwards them to the Gradle command line,
# where an unknown '-X' option is a hard error).
JVM_OPTS="-Xmx64m -Xms64m"
if [ -n "${JAVA_OPTS}" ]; then
    JVM_OPTS="${JAVA_OPTS} ${JVM_OPTS}"
fi
if [ -n "${GRADLE_OPTS}" ]; then
    JVM_OPTS="${GRADLE_OPTS} ${JVM_OPTS}"
fi

exec "${JAVACMD}" ${JVM_OPTS} \
    -Dorg.gradle.appname="${APP_BASE_NAME}" \
    -classpath "${CLASSPATH}" \
    org.gradle.wrapper.GradleWrapperMain "$@"


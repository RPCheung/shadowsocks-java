#!/usr/bin/env bash
SIP_HOME=$(dirname "$PWD")
export SIP_HOME

JAVA_OPTS="-server -Xmx2048m -Xms1024m -Xmn512m -Xss16m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${SIP_HOME}/oom-info.hprof -Dshadowsocks.crypto.plugin="
                                                                                                                                    #  -Dio.netty.leakDetection.level=advanced"
export JAVA_OPTS

${JAVA_HOME}/bin/java ${JAVA_OPTS} -classpath ${SIP_HOME}/conf:${SIP_HOME}/plugin/*:${SIP_HOME}/lib/* com.cheung.shadowsocks.ServerStart $1
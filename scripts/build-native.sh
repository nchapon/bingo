#!/bin/bash -e

cd "$( dirname "${BASH_SOURCE[0]}" )"

native-image --report-unsupported-elements-at-runtime \
    --initialize-at-build-time \
    --no-fallback \
    --native-image-info \
    --initialize-at-run-time=org.httpkit.client.ClientSslEngineFactory\$SSLHolder \
    --enable-url-protocols=http,https \
    -jar ../target/bingo-0.1.0-SNAPSHOT.jar \
    -H:+ReportExceptionStackTraces \
    -H:ReflectionConfigurationFiles=../resources/META-INF/native-image/logging.json \
    -H:Name=../target/bingo

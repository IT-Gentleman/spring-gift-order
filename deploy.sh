#!/bin/bash
BUILD_PATH=$(ls /home/ubuntu/deploy/*.jar | head -n 1)
JAR_NAME=$(basename $BUILD_PATH)
echo "> 새 애플리케이션: $JAR_NAME"

CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 실행 중인 애플리케이션이 없습니다."
else
    echo "> 실행 중인 애플리케이션 종료 (PID: $CURRENT_PID)"
    kill -15 $CURRENT_PID
    sleep 5
fi

DEPLOY_PATH=/home/ubuntu/
cp $BUILD_PATH $DEPLOY_PATH
cd $DEPLOY_PATH

echo "> 새 애플리케이션 배포"
DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
nohup java -jar -Dspring.profiles.active=prod $DEPLOY_JAR > /dev/null 2> /dev/null < /dev/null &

echo "> 배포 완료"
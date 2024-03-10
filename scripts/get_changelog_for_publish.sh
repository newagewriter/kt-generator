#!/bin/bash
CURRENT_TAG="$1"
echo "start: $CURRENT_TAG"
CHANGELOG=$(cat ./CHANGELOG.md)
#echo "Testdfsf
#      # 0.3.1
#      ewew Here is a
#      cos tam cos tam
#      # 0.3.0
#      String i dalje tez cos jest
#
#      nowa linja co z nia
#      # 0.2.0
#
#      kolejne linje
#      " | sed "s/.*$CURRENT_TAG//; s/# [0-9]*\.[0-9]*\.[0-9]*[.\n\r\t]*//"

tag_note=${CHANGELOG#*# ${CURRENT_TAG}}
two=${tag_note% [0-9]*}
echo "$two"
#echo "$CHANGELOG" | sed "s/.*$CURRENT_TAG//; s/# 0.3.0.*//"
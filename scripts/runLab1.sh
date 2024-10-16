#!/bin/bash

echo "Compiling the program..."
javac lab1/*.java

TARGET_CLASS=""
if [ -n "$1" ]; then
    TARGET_CLASS=$1
fi

echo "Running the program..."
java lab1.$TARGET_CLASS

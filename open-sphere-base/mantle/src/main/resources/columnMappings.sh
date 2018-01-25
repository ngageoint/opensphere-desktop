#! /bin/sh

# This script creates the JAR file containing jaxb classes used to parse a file conforming to columnMappings.xsd.
# Make sure to run this script from the directory in which the script lives.

XSD_FILE=columnMappings.xsd
JAR_FILE=columnMappings-2.0.jar
PKG_PATH=com/bit_sys/state/v2

# Generate jaxb source files
mkdir -p target/src
xjc -d target/src $XSD_FILE

# Compile
echo ; echo "Compiling"
mkdir -p target/classes
javac -d target/classes target/src/${PKG_PATH}/*.java

# Create JAR file
echo ; echo "Creating JAR file"
cd target/classes
jar cvf ../../../../../lib/${JAR_FILE} ${PKG_PATH}/*.class

# Cleanup
cd ../..
rm -fr target

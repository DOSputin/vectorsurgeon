#!/bin/sh

DIR=`echo $0 | sed 's?\(.*/\).*?\1?'`

if test ${DIR} = $0
then
	DIR=.
fi

CLASSPATH=${DIR}/vs.jar
export CLASSPATH

exec java com.prophetvs.editor.ControlMapper





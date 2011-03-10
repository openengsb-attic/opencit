#!/bin/sh
mvn release:prepare release:perform -Pfinal -Dmaven.test.skip=true -DconnectionUrl=scm:git:file://$PWD


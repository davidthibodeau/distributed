#!/bin/sh

export CLASSPATH=`pwd`
rmiregistry 2005 &
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:`pwd`/ serversrc.resImpl.RMFlightImpl 2005

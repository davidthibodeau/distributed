flight: 
	java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:`pwd`/ serversrc.resImpl.RMFlightImpl 2005
car:
	java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:`pwd`/ serversrc.resImpl.RMCarImpl 2005
hotel: 
	java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:`pwd`/ serversrc.resImpl.RMHotelImpl 2005
customer:
	java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:`pwd`/ serversrc.resImpl.RMCustomerImpl 2005
rmi: 
	export CLASSPATH=`pwd`
	rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 2005 &

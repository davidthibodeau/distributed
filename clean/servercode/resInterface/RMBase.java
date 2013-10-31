package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.RMInteger;
import serversrc.resImpl.ReservedItem;

public interface RMBase extends Remote {

	public boolean unreserveItem(int id, ReservedItem reserveditem)
		throws RemoteException;
	
	public boolean unreserveItem(int id, String key)
			throws RemoteException;
	
	public RMInteger reserveItem(int id, int customerID, String key, String location)
	    	throws RemoteException;
}

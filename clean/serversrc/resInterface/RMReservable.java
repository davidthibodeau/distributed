package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.RMInteger;
import serversrc.resImpl.TransactionAbortedException;

public interface RMReservable extends Remote {
	public boolean unreserveItem(int id, String key)
			throws RemoteException, TransactionAbortedException;
	
	public RMInteger reserveItem(int id, int customerID, String key, String location)
	    	throws RemoteException, TransactionAbortedException;

}
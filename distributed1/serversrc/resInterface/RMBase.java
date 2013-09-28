package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.ReservedItem;

public interface RMBase extends Remote {

	public boolean unreserveItem(int id, ReservedItem reserveditem)
		throws RemoteException;
}

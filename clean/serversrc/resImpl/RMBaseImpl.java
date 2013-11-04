package serversrc.resImpl;

import java.rmi.RemoteException;
import java.util.Enumeration;

import serversrc.resInterface.*;

public class RMBaseImpl implements RMBase {

	protected RMHashtable m_itemHT = new RMHashtable();
	protected RMHashtable m_transactionHT = new RMHashtable();

    // Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }
    
    
    // deletes the entire item
    protected boolean deleteItem(int id, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key );
    	if ( curObj == null ) {
			Trace.warn("RM::deleteItem( " + id + ", "  + key+") failed--item doesn't exist" );
			return false;
    	}
    	synchronized (curObj) {
    		if (curObj.getReserved()==0) {
    			removeData(id, curObj.getKey());
    			curObj.setDeleted(true);
    			Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
    			return true;
    		}
    		else {
    			Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
    			return false;
    		}
        }
    }
    
    public boolean unreserveItem(int id, ReservedItem reserveditem)
    		throws RemoteException{
    	ReservableItem item = (ReservableItem) readData(id, reserveditem.getKey());
    	if ( item == null ) {
			Trace.warn("RM::reserveItem( " + id + ", "  + reserveditem+") failed--item doesn't exist" );
			return false;
    	}
    	synchronized (item) {
    		Trace.info("RM::unreserveItem(" + id + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
    		item.setReserved(item.getReserved()-reserveditem.getCount());
    		item.setCount(item.getCount()+reserveditem.getCount());
    		return true;
    	}
    }
    
    //This function is called to cancel a reservation done at the same time the customer is deleted
    public boolean unreserveItem(int id, String key)
    		throws RemoteException{
    	ReservableItem item = (ReservableItem) readData(id, key);
    	if ( item == null ) {
			Trace.warn("RM::reserveItem( " + id + ", "  + key+") failed--item doesn't exist" );
			return false;
    	}
    	synchronized (item) {
    		Trace.info("RM::unreserveItem(" + id + ") has reserved " + key + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
    		item.setReserved(item.getReserved()-1);
    		item.setCount(item.getCount()+1);
    		return true;
    	}
    }

    // query the number of available seats/rooms/cars
    protected int queryNum(int id, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;  
        if ( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }    
    
    // query the price of an item
    protected int queryPrice(int id, String key) {
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0; 
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;        
    }
    
    // reserve an item
    // Synchronized: We don't want two clients to reserve the last item
    // The isDeleted field is to make sure the item was not deleted just before
    // we acquired the lock (but after we retrieved the object).
    // Returns the price of the item in a nullable integer using RMInteger.
    public RMInteger reserveItem(int id, int customerID, String key, String location)
    	throws RemoteException {
    	ReservableItem item = (ReservableItem)readData(id, key);
    	if ( item == null ) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return null;
    	}
    	synchronized (item) {
    		if (item.getCount()==0) {
    			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
    			return null;
    		} else if (item.isDeleted()) {
    			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--item has been deleted" );
    			return null;
    		} else {            
    			// decrease the number of available items in the storage
    			item.setCount(item.getCount() - 1);
    			item.setReserved(item.getReserved()+1);
    			return new RMInteger(item.getPrice());
    		}
    	}
    }
    
    public boolean commit(int id) throws RemoteException, InvalidTransactionException {
    	RMHashtable transaction = null;
    	synchronized(m_transactionHT){
    		transaction = (RMHashtable) m_transactionHT.remove(id);
    	}
    	if (transaction == null){
    		Trace.warn("RM::commit( " + id + ") failed--Transaction does not exist." );
    		throw new InvalidTransactionException();
    	}
    	for(Enumeration<Object> i = transaction.elements(); i.hasMoreElements(); ){
    		ReservableItem item = (ReservableItem) i.nextElement();
    		if(item.isDeleted())
    			removeData(id, item.getKey());
    		else
    			writeData(id,item.getKey(), item);
    	}
    	return true;
    }
    
    public void abort(int id) throws RemoteException, InvalidTransactionException{
    	RMHashtable transaction = null;
    	synchronized(m_transactionHT){
    		transaction = (RMHashtable) m_transactionHT.remove(id);
    	}
    	if (transaction == null){
    		Trace.warn("RM::abort( " + id + ") failed--Transaction does not exist." );
    		throw new InvalidTransactionException();
    	}
    }

}

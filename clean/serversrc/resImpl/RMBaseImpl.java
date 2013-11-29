package serversrc.resImpl;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import serversrc.resInterface.*;

public abstract class RMBaseImpl implements RMBase, RMReservable {

	protected RMHashtable m_itemHT = new RMHashtable();
	protected RMHashtable m_transactionHT = new RMHashtable();
	protected final String htFileName = this.rmType() + "/ht.rm";
	static int port;
	
    private String locationFile(int id) {
		return this.rmType() + "/" + id + ".tmp";
	}
    private Crash crashType = Crash.NO_CRASH;
	private boolean verbose = true;
    
    // Reads a data item
    protected abstract RMItem readData( int id, String key );
    
    protected abstract String rmType();
    
    protected boolean boot(){
		FileInputStream fis;
		ObjectInputStream ois;
		try {
			File htFile = new File(htFileName);
			if (!htFile.exists()) {
				File directory = new File(this.rmType());
				if (directory.mkdirs()) Trace.info("Directory Created:: Directory = " + this.rmType());
				if(htFile.createNewFile()) Trace.info("File Created:: Filename = " + htFileName);
				else Trace.info("File couldn't be created file Name:"); //something horrible happened
					
				FileOutputStream fos = new FileOutputStream(htFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				synchronized (m_itemHT) {
					oos.writeObject(m_itemHT);
				}
				oos.close();
			} else {
				fis = new FileInputStream(htFile);
				ois = new ObjectInputStream(fis);
				synchronized (m_itemHT) {
					m_itemHT = (RMHashtable) ois.readObject();
				}
				ois.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try{
    		String files;
    		String fold = this.rmType() + "/";
    		File folder = new File(fold);
    		File[] listOfFiles = folder.listFiles(); 

    		for (int i = 0; i < listOfFiles.length; i++)
    		{
    			RMHashtable ht = new RMHashtable();
    			if (listOfFiles[i].isFile()) 
    			{
    				files = listOfFiles[i].getName();
    				if (files.endsWith(".tmp"))
    				{
    					int id = 0;
    					Pattern p = Pattern.compile("[0-9]+");
    	    			Matcher m = p.matcher(files);
    	    			if (m.find()) {
    	    			  id = Integer.valueOf(m.group(0)).intValue();  // The matched substring
    	    			} else {
    	    				return false;
    	    			}
    	    			fis = new FileInputStream(fold + files);
    	        		ois = new ObjectInputStream(fis);
    	        		
    	        		ht = (RMHashtable) ois.readObject();
    	        		ois.close();
    	        		
    	        		synchronized(m_transactionHT){
    	        			m_transactionHT.put(id, ht);
    	        		}
    				}
    			}
    		}
    		return true;
    		
    		
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		return false;
		} catch(ClassNotFoundException e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
    		return false;
		}
    	
    }
    
    // Writes a data item
    protected void registerData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage
    protected RMItem deleteData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }

    // Writes a data item
    protected void writeData( int id, String key, RMItem value )
    {
        synchronized(m_transactionHT) {
        	RMHashtable trHT = (RMHashtable) m_transactionHT.get(id);
            trHT.put(key, value);
            m_transactionHT.put(id, trHT);
        }
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        	RMItem item = (RMItem)readData(id, key);
        	item.setDeleted(true);
        	writeData(id, key, item);
            return item;
    }
    
    // deletes the entire item
    protected boolean deleteItem(int id, String key) 
    		throws TransactionAbortedException
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key );
    	if ( curObj == null ) {
			Trace.warn("RM::deleteItem( " + id + ", "  + key+") failed--item doesn't exist" );
			throw new TransactionAbortedException(id);
    	}
    	if (curObj.getReserved()==0) {
    		removeData(id, curObj.getKey());
    		Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
    		return true;
    	}
    	else {
    		Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
    		throw new TransactionAbortedException(id);
    	}
    }

    //This function is called to cancel a reservation done at the same time the customer is deleted
    public boolean unreserveItem(int id, String key)
    		throws RemoteException, TransactionAbortedException{
    	ReservableItem item = (ReservableItem) readData(id, key);
    	if ( item == null ) {
    		Trace.warn("RM::reserveItem( " + id + ", "  + key+") failed--item doesn't exist" );
    		throw new TransactionAbortedException(id);
    	}
    	Trace.info("RM::unreserveItem(" + id + ") has reserved " + key + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
    	item.setReserved(item.getReserved()-1);
    	item.setCount(item.getCount()+1);
    	writeData(id,key,item);
    	return true;
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
    		throws RemoteException, TransactionAbortedException {
    	ReservableItem item = (ReservableItem)readData(id, key);
    	if ( item == null ) {
    		Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
    		throw new TransactionAbortedException(id);
    	}
    	if (item.getCount()==0) {
    		Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
    		throw new TransactionAbortedException(id);
    	} else if (item.isDeleted()) {
    		Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--item has been deleted" );
    		throw new TransactionAbortedException(id);
    	} else {            
    		// decrease the number of available items in the storage
    		item.setCount(item.getCount() - 1);
    		item.setReserved(item.getReserved()+1);
    		writeData(id,key,item);
    		return new RMInteger(item.getPrice());
    	}
    }
    
    public boolean prepare(int id) throws RemoteException {
       	Trace.info("Voting on transaction with id: " + id);
    	RMHashtable transaction = null;
    	synchronized(m_transactionHT){
    		transaction = (RMHashtable) m_transactionHT.get(id);
    	}
    	if(transaction == null){
    		Trace.warn("RM::prepare( " + id + ") failed--Transaction does not exist." );
    		return false;
    	}

    	if (crashType == Crash.BEFORE_VOTE) selfdestruct();
    	try {
    		File file = new File(locationFile(id));
    		if (!file.exists())
    			file.createNewFile();
    		else{
    			file.delete();
    			file.createNewFile();
    		}
    		FileOutputStream fos = new FileOutputStream(file);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		oos.writeObject(transaction);
    		oos.close();

    	} catch (IOException e) {
			Trace.warn("RM::prepare( " + id + ") failed--Could not write hashtable into file." );
			return false;
		}
    	return true;
    }

	public boolean commit(int id) throws RemoteException, InvalidTransactionException {
       	Trace.info("Commiting transaction with id: " + id);

		if (crashType == Crash.AFTER_DECISIONS) selfdestruct();
		RMHashtable transaction = null;
    	synchronized(m_transactionHT){
    		transaction = (RMHashtable) m_transactionHT.remove(id);
    	}
    	if (transaction == null){
    		Trace.warn("RM::commit( " + id + ") failed--Transaction does not exist." );
    		throw new InvalidTransactionException();
    	}
    	for(Enumeration<Object> i = transaction.elements(); i.hasMoreElements(); ){
    		RMItem item = (RMItem) i.nextElement();
    		if(item.getClass() == Customer.class){
    			Customer cust = (Customer)item;
    			if(cust.isDeleted())
    				deleteData(id, cust.getKey());
    			else
    				registerData(id, cust.getKey(), cust);
    		} else{
    			ReservableItem item1 = (ReservableItem) item;
    			if(item.isDeleted())
    				deleteData(id, item1.getKey());
    			else
    				registerData(id, item1.getKey(), item1);
    		}
    	}
    	try {
    		FileOutputStream fos = new FileOutputStream(htFileName);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		synchronized(m_itemHT){
    			oos.writeObject(m_itemHT);
    		}
    		oos.close();
    	} catch (IOException e) {
    		Trace.error("RM::Commit( " + id + ") failed--Could not write hashtable to file." );
    		return false;
    	}
		String filename = locationFile(id);
		File file = new File(filename);
		file.delete();
    	return true;
    }
    
    public void abort(int id) throws RemoteException, InvalidTransactionException{
       	Trace.info("Aborting transaction with id: " + id);
    	if (crashType == Crash.AFTER_DECISIONS) selfdestruct();
    	RMHashtable transaction = null;
    	synchronized(m_transactionHT){
    		transaction = (RMHashtable) m_transactionHT.remove(id);
    	}
    	if (transaction == null){
    		Trace.warn("RM::abort( " + id + ") failed--Transaction does not exist." );
    		throw new InvalidTransactionException();
    	}
    }
    
    public boolean enlist(int id) throws RemoteException {
    	synchronized(m_transactionHT) {
    		RMHashtable tr = (RMHashtable) m_transactionHT.get(id);
    		if(tr == null)
    			m_transactionHT.put(id, new RMHashtable());
        }
    	return true;
    }
    
	public void selfdestruct() throws RemoteException {
		System.exit(1);
	}
	
	public boolean heartbeat() throws RemoteException {
		if(verbose) Trace.info("RM::Heartbeat() running.");
		return true;
	}
	public void setCrashType(Crash crashType) throws RemoteException{
		this.crashType = crashType;
	}
	public Crash getCrashType() throws RemoteException{
		return crashType;
	}
	public void toggleHeart(boolean verbose){
		this.verbose = verbose;
	}
}

// -------------------------------// Kevin T. Manley// CSE 593// -------------------------------package serversrc.resImpl;import java.util.*;// A specialization of Hashtable with some//  extra diagnosticspublic class RMHashtable extends Hashtable{    RMHashtable() {      super();    }        /**	 * deeply clones a Map by cloning all the values.	 */    static RMHashtable deepCopy(RMHashtable ht){    	RMHashtable copy = new RMHashtable();    	if (ht != null){    		Enumeration e = ht.keys();     		if(e != null){    			while(e.hasMoreElements()) {    				    				String key = (String) e.nextElement();    				ReservedItem value = (ReservedItem) ht.get(key);    				ReservedItem newvalue = new ReservedItem(key, value.getLocation(), value.getCount(), value.getPrice(), value.getrType());    				copy.put(key, newvalue);    			}    		}    	}    	return copy;    }    @Override    public String toString()    {    	String s = "--- BEGIN RMHashtable ---\n";    	Object key = null;    	for (Enumeration e = keys(); e.hasMoreElements(); ) {    		key = e.nextElement();    		String value = (String)get( key );    		s = s + "[KEY='"+key+"']" + value + "\n";    	}    	s = s + "--- END RMHashtable ---";    	return s;    }    public void dump()    {    	System.out.println( toString() );    }}
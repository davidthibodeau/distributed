// -------------------------------// adapted from Kevin T. Manley// CSE 593// -------------------------------package serversrc.resImpl;import java.io.*;// Resource manager data itempublic abstract class RMItem implements Serializable{	private boolean m_deleted;	    RMItem() {			super();			m_deleted = false;    }        public RMItem(RMItem item) {		this.m_deleted = item.m_deleted;	}	public boolean isDeleted()     { return m_deleted;	}    public void setDeleted(boolean m_deleted)     { this.m_deleted = m_deleted; }}
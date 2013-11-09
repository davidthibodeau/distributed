package serversrc.resImpl;

public class TransactionAbortedException extends Exception {

	private int tid;
	
	public TransactionAbortedException(int tid){
		this.tid = tid;
	}
	
	public int getTid (){
		return tid;
	}
	
}

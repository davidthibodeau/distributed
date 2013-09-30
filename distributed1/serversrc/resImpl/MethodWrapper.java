/**
 * 
 */
package serversrc.resImpl;

import java.io.Serializable;
import java.util.Vector;

/**
 * 
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class MethodWrapper implements Serializable {
	private Vector args;
	private String method;
	/**
	 * @return the args
	 */
	
	MethodWrapper(String method, Vector args){
		this.method = method;
		this.args = args;
	}
	
	public Vector getArgs() {
		return args;
	}
	/**
	 * @param args the args to set
	 */
	public void setArgs(Vector args) {
		this.args = args;
	}
	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}
}

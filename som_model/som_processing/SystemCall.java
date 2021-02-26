
package som_processing;

import java.sql.Timestamp;

//Representation of a system call
public class SystemCall {
	
	public long timeStamp;
    public String name;
    public int tid;
    public long exitValue;
    
    public SystemCall(){
    	
    }
    
    public SystemCall(long timeStamp, String name, int tid){
    	this.timeStamp = timeStamp;
	    this.name = name;
	    this.tid = tid;
	    //original perfscope code
//	    this.exitValue = -1;
	    
	    //test code write by hao  long max value = 2147483647
	    this.exitValue = Long.MAX_VALUE;
	    
	    
    }

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public long getExitValue() {
		return exitValue;
	}

	public void setExitValue(long exitValue) {
		this.exitValue = exitValue;
	}
   
	/*
	public String sysCallInfo(Integer id){
		
		Timestamp ts = new Timestamp(timeStamp/1000000); 
		Timestamp te = new Timestamp(exitValue/1000000);
		//hao added the reason that timeStamp/10^6 is in getSystemCall() timeStampLong is divided by 10^6 to eliminate digit point
		return  "id: " + id + " " + ts.toString() + " ," + name + " ," + tid + " ," + te.toString() + "\n";
	}
	*/
	public String printSyscall(){
		return 	timeStamp + " " + name + " "  + tid + " " + exitValue +"\n";
	}
    
}

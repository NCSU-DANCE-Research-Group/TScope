/*******************************************************************************
 * Copyright InsightFinder Inc., 2017
 *******************************************************************************/
package som_processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Logger;


public class FuncLocPreProcessing {

	public static Logger logger = Logger.getLogger(FuncLocPreProcessing.class
			.getName());
	//public DataRetrievalUtility dru = new DataRetrievalUtility();

	/*
	 * Takes a list of system calls and splits the list based on large time gaps
	 * and TID.
	 */
	public List<List<List<SystemCall>>> outputLog(List<List<SystemCall>> systemCallList) throws IOException {
		if (systemCallList == null || systemCallList.isEmpty()) {
			return null;
		}
		
		int cnt = 0;
		long maxtime = systemCallList.get(0).get(0).getTimeStamp();
		long mintime = systemCallList.get(0).get(0).getTimeStamp();
		
		for (int i = 0; i < systemCallList.size(); i++) {
			for (int j = 0; j < systemCallList.get(i).size(); j++){
				cnt = cnt + 1;
				if (maxtime < systemCallList.get(i).get(j).getTimeStamp()){
					maxtime = systemCallList.get(i).get(j).getTimeStamp();
				}
				if (mintime > systemCallList.get(i).get(j).getTimeStamp()){
					mintime = systemCallList.get(i).get(j).getTimeStamp();
				}
			}
		}
		
		System.out.println("Number of syscalls in systemCallList:" + cnt);
		System.out.println("maxtime:" + maxtime);
		System.out.println("mintime:" + mintime);
		int totalinterval  = (int) ((maxtime - mintime)/600000000);
		totalinterval = totalinterval/100;
		System.out.println("total:" + totalinterval);
		// split by timestamp
		List<List<List<SystemCall>>> syswhole = new ArrayList<List<List<SystemCall>>>();

//		List<List<SystemCall>> syscallLists = new ArrayList<List<SystemCall>>();
		int startPosition = 0;
		int endPosition = 0;
		int thretime = 0;
		for (int i = 0; i < systemCallList.size(); i++) {
			List<List<SystemCall>> syscallLists = new ArrayList<List<SystemCall>>();
			startPosition = 0;
			endPosition = 0;
			for (int j = 0; j < systemCallList.get(i).size(); j++) {
				long diff = systemCallList.get(i).get(j).getTimeStamp() - mintime;
				int validx = (int) (diff/600000000);
				validx = validx/100;
				if (j == 0){
					thretime = validx;
				}
				if (thretime != validx) {
					endPosition = j;
				}
				if (startPosition != endPosition) {
			    	List<SystemCall> ExecutionUnitlist = systemCallList.get(i).subList(
						startPosition, endPosition);
			    	syscallLists.add(ExecutionUnitlist);
			    	startPosition = endPosition;
		    //		System.out.println("\n" + ExecutionUnitlist);
			    	}
			    
			    	if (j==systemCallList.get(i).size()-1){
			    		List<SystemCall> ExecutionUnitlist = systemCallList.get(i).subList(
							startPosition, j+1);
			    		syscallLists.add(ExecutionUnitlist);
			    		}
			    	
				
				thretime = validx;
			}
	//		List<List<SystemCall>> syscall_trans = new ArrayList<List<SystemCall>>();
	//		List<Integer> numsyscall = new ArrayList<Integer>();
	//		List<SystemCall> syscall_tmp = new ArrayList<SystemCall>();
	//		for (int i2 = 0; i2 < systemCallList.size(); i2++) {
	//			numsyscall.add(systemCallList.get(i2).size());
	//			syscall_tmp.addAll(systemCallList.get(i2));
	//		}
			
	//		int numrow = Collections.max(numsyscall);
	//		int numcolumn = systemCallList.size();
	//		for (int i3 = 0; i3 < numrow; i3++) {
	//			List<SystemCall> syscall_each = new ArrayList<SystemCall>();
	//			for (int j3 = 0; j3 < numcolumn; j3++) {
	//				 m,
	//			}
	//		}
			
			
			
			
//			for (int j = 0; j < systemCallList.get(i).size(); j++) {
//			    long diff = systemCallList.get(i).get(j).getTimeStamp() 
//			    		  - systemCallList.get(i).get(startPosition).getTimeStamp();
//			    if (diff > 600000000){
//			    	endPosition = j;
//			    	}
//			    if (startPosition != endPosition) {
//			    	List<SystemCall> ExecutionUnitlist = systemCallList.get(i).subList(
//						startPosition, endPosition);
//			    	syscallLists.add(ExecutionUnitlist);
//			    	startPosition = endPosition;
		    //		System.out.println("\n" + ExecutionUnitlist);
//			    	}
//			    else{
//			    	if (j==systemCallList.get(i).size()-1){
//			    		List<SystemCall> ExecutionUnitlist = systemCallList.get(i).subList(
//							startPosition, j+1);
//			    		syscallLists.add(ExecutionUnitlist);
//			    		}
//			    	}
//			    }
			syswhole.add(syscallLists);
		}
		
//		List<List<List<SystemCall>>> syscall_trans = new ArrayList<List<List<SystemCall>>>();
//		List<Integer> numsyscall = new ArrayList<Integer>();
//		for (int i2 = 0; i2 < syswhole.size(); i2++) {    //i2 is the index of time interval
//			numsyscall.add(syswhole.get(i2).size());
//			}
//		int numrow = Collections.max(numsyscall);
//		int numcolumn = syswhole.size();
//		System.out.println("numrow: " + numrow);
//		System.out.println("numcolumn: " + numcolumn);
//		for (int i = 0; i < numrow; i++){
//			List<List<SystemCall>> sys_unit = new ArrayList<List<SystemCall>>();
//			for (int j = 0; j < numcolumn; j++){
//				if (numsyscall.get(j)>i){
//					sys_unit.add(syswhole.get(j).get(i));
//				}
//			}
//			syscall_trans.add(sys_unit);	
//		}
		
		
		FileWriter f = null;
		BufferedWriter out=null;
		f = new FileWriter("/home/jhe16/Downloads/syslog_processing/eu.txt");
		out = new BufferedWriter(f);
		List<SystemCall> Vec;
		cnt = 0;
		int cnt2 = 0;
		for(int i = 0; i < syswhole.size(); i++){
	//		System.out.println("Tid of syscall_trans_" + i + ":" + syswhole.get(i).get(0).get(0).getTid());
			for(int k = 0; k < syswhole.get(i).size(); k++){ 
				out.write("tid="+syswhole.get(i).get(k).get(0).getTid()+","
						+syswhole.get(i).get(k).get(0).getTimeStamp()+":" + "\n");
				Vec = syswhole.get(i).get(k);
				cnt2 = cnt2 + 1;
				for(int j = 0 ; j < Vec.size(); j++){
					cnt = cnt + 1;
					out.write(Vec.get(j).getName() + "," + Vec.get(j).getTimeStamp() + "," + Vec.get(j).getTid() + "\n");
					}
				out.write("\n");	
				}
			}
		
		System.out.println("Number of syscalls in syscall_trans:" + cnt);
		System.out.println("Number of units in syscall_trans:" + cnt2);
		
		return syswhole;
	}

	public int getPercentile(List<Integer> listToCheck, double pValue) {
		int index = (int) (listToCheck.size() * pValue);
		Collections.sort(listToCheck);
		int cIndex = 0;
		int ret = 0;
		for (int item : listToCheck) {
			cIndex = cIndex + 1;
			if (cIndex == index) {
				ret = item;
				break;
			}
		}
		return ret;
	}

	public double calcSTD(double avg, List<Long> cList) {
		double cTotal = 0;
		for (long i : cList) {
			double cDiff = i - avg;
			cDiff = cDiff * cDiff;
			cTotal += cDiff;
		}
		cTotal = (1.0 * cTotal) / cList.size();
		return Math.sqrt(cTotal);
	}

	public long getMillisFromGMT(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			return sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			logger.severe("parse GMT timestamp to produce millis failed");
			return -1;
		}
	}

	// Parses the current line and puts the results into the system call data
	// structure format
	public SystemCall getSystemCall(String line) {
		if (line.equals("-1"))
			return new SystemCall(-1, "CONTEXT_SWITCH", -1);
		String timeString = line.split("\\]")[0].split("\\[")[1];
		String[] contextsRaw = line.split("\\{")[2].split("\\}")[0].split(",");
		String TID = "";
		for (String item : contextsRaw)
			if (item.contains("tid ="))
				TID = item.split("tid = ")[1].replaceAll("\\s", "");
		String timedouble = Long.toString(getMillisFromGMT(timeString
				.substring(0, timeString.length() - 6)))
				+ "."
				+ (timeString.substring(timeString.length() - 6));
		double timeStamp = Double.parseDouble(timedouble);
		long timeStampLong = (long) (timeStamp * 1000000);
		String[] systemCallTemp = line.split("\\{")[0].split(" ");
		String systemCall = systemCallTemp[systemCallTemp.length - 1]
				.replaceAll(":", "");
		return new SystemCall(timeStampLong, systemCall, Integer.parseInt(TID));
	}

	// Segments the input file based on process name while inserting context
	// switch markers into the list.
	// Hands resulting list to the outputLog function to be further segmented by
	// TID and large time gaps.
	public List<List<SystemCall>> getSyscallList(String fileName, String procNames,
			String language) throws IOException {
	/*
		InputStream instream = null;
		BufferedReader reader = null;
		List<SystemCall> syscallList = new ArrayList<SystemCall>();
		String line = "";
		try {
			instream = new FileInputStream(fileName);//for testing....
			logger.info("load " + fileName + " from storage successfully.");
			reader = new BufferedReader(new InputStreamReader(instream));
			while ((line = reader.readLine()) != null) {
				if 
			}
			}
	*/
			
		
	
		InputStream instream = null;
		BufferedReader reader = null;
		Map<String, Integer> pMap = new HashMap<String, Integer>();
		if (language.equalsIgnoreCase("c") || language.equalsIgnoreCase("c++")) {
			if (procNames.contains(",")) {
				String[] pTemp = procNames.split(",");
				for (String pName : pTemp)
					pMap.put(pName, 1);
			} else
				pMap.put(procNames, 1);
		} else if (language.equalsIgnoreCase("java")) {
			pMap.put("java", 1);
		}
		SystemCall previousSyscall = new SystemCall();
		boolean previousFlag = false;
		List<SystemCall> syscallList = new ArrayList<SystemCall>();
		Map<Integer, List<SystemCall>> scListMap = new HashMap<Integer, List<SystemCall>>();
		int pTID = -1;
		String line = "";
		try {
			 instream = new FileInputStream(fileName);//for testing....
			logger.info("load " + fileName + " from storage successfully.");
			reader = new BufferedReader(new InputStreamReader(instream));
			while ((line = reader.readLine()) != null) {
				// if(!scanner.hasNextLine()){
				// break;
				// }
				// String line = scanner.nextLine();
				boolean use = false;
				Iterator<Entry<String, Integer>> pIt = pMap.entrySet()
						.iterator();
				Entry<String, Integer> pEntry = null;
				while (pIt.hasNext()) {
					pEntry = pIt.next();
					String key = pEntry.getKey();
					if (line.contains(key)) {
						use = true;
						break;
					}
				}
				// if not sc.procName in pMap:
				if (!use) {
					if (line.contains("tid = ")) {
						// { tid = 1142, ppid = 1136, pid = 1142, procname =
						// "lttng-consumerd" }
						// { procname = "lttng-consumerd", ppid = 1772, pid =
						// 1778, tid = 1778 }
						// pTID =
						// Integer.parseInt(line.split("tid = ")[1].split(" \\},")[0]);
						String[] contextsRaw = line.split("\\{")[2]
								.split("\\}")[0].split(",");
						for (String item : contextsRaw)
							if (item.contains("tid ="))
								pTID = Integer.parseInt(item.split("tid = ")[1]
										.replaceAll("\\s", ""));
					}
					continue;
				}
				SystemCall sc = getSystemCall(line);
				if (!scListMap.containsKey(sc.tid)) {
					List<SystemCall> sclist = new ArrayList<SystemCall>();
					scListMap.put(sc.tid, sclist);
				}
				if (sc.tid != pTID) {// context switch
					SystemCall cs = getSystemCall("-1");
					cs.tid = sc.tid;
					List<SystemCall> sclist = scListMap.get(sc.tid);
					sclist.add(cs);
					scListMap.put(sc.tid, sclist);
				}
				List<SystemCall> sclist = scListMap.get(sc.tid);
				sclist.add(sc);
				scListMap.put(sc.tid, sclist);
				pTID = sc.tid;
			}
		} catch (Exception e) {
			logger.severe("Exception: " + e.toString());
		}
		// logger.info("scListMap size = " + scListMap.size());
		logger.info("Done creating list");
		try {
			Iterator<Entry<Integer, List<SystemCall>>> scListIt = scListMap
					.entrySet().iterator();
			Entry<Integer, List<SystemCall>> scListEntry = null;
			while (scListIt.hasNext()) {
				scListEntry = scListIt.next();
				List<SystemCall> syscalls = scListEntry.getValue();
				for (SystemCall sc2 : syscalls) {
					if (sc2.name.equals("CONTEXT_SWITCH")) {
						// syscallList.add(sc2);
						continue;
					}
					if ((sc2.name.contains("exit_syscall") || sc2.name
							.contains("syscall_exit")) && previousFlag) {
						if (sc2.tid != previousSyscall.tid) {
							previousSyscall = new SystemCall();
							previousFlag = false;
							continue;
						}
						previousSyscall.exitValue = sc2.timeStamp;
						syscallList.add(previousSyscall);
					} else {
						previousSyscall = sc2;
						previousFlag = true;
					}
				}
			}
		} catch (Exception e) {
			logger.severe("Exception: " + e.toString());
			scListMap.clear();
			return null;
		}
		scListMap.clear();
//		System.out.println(syscallList);
		Map<Integer, Integer> tmp = new HashMap<Integer, Integer>();
		List<List<SystemCall>> segsyslist = new ArrayList<List<SystemCall>>();
		for(int i = 0; i < syscallList.size(); i++){
			int currenttid = syscallList.get(i).getTid();
			tmp.put(currenttid, 0);
		}
		int idx = -1;
		for(int i = 0; i < syscallList.size(); i++){
			int currenttid = syscallList.get(i).getTid();
			if (tmp.get(currenttid)==0){
				idx = idx+1;
				List<SystemCall> addone = new ArrayList<SystemCall>();
				addone.add(syscallList.get(i));
				segsyslist.add(addone);
				tmp.put(currenttid, 1);
			}
			else{
				segsyslist.get(idx).add(syscallList.get(i));
			}
			
		}
 
		
		FileWriter f = null;
		BufferedWriter out=null;
		f = new FileWriter("/home/jhe16/Downloads/syslog_processing/sys.txt");
		out = new BufferedWriter(f);
		for(int i = 0; i < segsyslist.size(); i++){
			out.write(i + ":" + "\n");
			for(int j = 0; j < segsyslist.get(i).size(); j++){
			    out.write(segsyslist.get(i).get(j).getTimeStamp()+",");
			    out.write(segsyslist.get(i).get(j).getName() + ",");
			    out.write(segsyslist.get(i).get(j).getTid() + ",");
			    out.write("\n");	
			}
		}
		return segsyslist;
	}

	// Entry point, parses the arguments and calls the functions to segment the
	// input system call log.
	//"java" "hadoop,hdfs", "/home/.../lttngtrace.log"
//	public void startPreprocessing(String language,
	public List<List<List<SystemCall>>> startPreprocessing(String language,
			String procNames, String syscallLog) throws IOException {
		 logger.info("Segmenting log " + syscallLog);
		 double start_t = System.currentTimeMillis();
		List<List<SystemCall>> systemCallList = getSyscallList(syscallLog, procNames,
				language);
		logger.info("The Syscall List size = " + systemCallList.size());
		 double end_t = System.currentTimeMillis();
		 double elapsed = (end_t - start_t)/1000L;
	    logger.info("Total time: " + elapsed);
		List<List<List<SystemCall>>> syscallLists = outputLog(systemCallList);
		return syscallLists;
	}

}

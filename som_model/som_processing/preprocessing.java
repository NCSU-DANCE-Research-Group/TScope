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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class preprocessing {
	public static Logger logger = Logger.getLogger(FuncLocPreProcessing.class
			.getName());
	//public DataRetrievalUtility dru = new DataRetrievalUtility();

	/*
	 * Takes a list of system calls and splits the list based on large time gaps
	 * and TID.
	 */
	public List<List<SystemCall>> outputLog(List<SystemCall> systemCallList, long interval) throws IOException {
		if (systemCallList == null || systemCallList.isEmpty()) {
			return null;
		}


		 long maxtime = systemCallList.get(0).getTimeStamp();
	        long mintime = systemCallList.get(0).getTimeStamp();
	         
	        for (int i = 0; i < systemCallList.size(); i++) {
	            if (maxtime < systemCallList.get(i).getTimeStamp()){
	                maxtime = systemCallList.get(i).getTimeStamp();
	                }
	            if (mintime > systemCallList.get(i).getTimeStamp()){
	                mintime = systemCallList.get(i).getTimeStamp();
	                }
	        }
	         
	    System.out.println("Number of syscalls in systemCallList:" + systemCallList.size());
		System.out.println("maxtime:" + maxtime);
		System.out.println("mintime:" + mintime);
		int totalinterval  = (int) ((maxtime - mintime)/interval);
		System.out.println("total:" + totalinterval);
		// split by timestamp
		 List<List<SystemCall>> syswhole = new ArrayList<List<SystemCall>>();
		 
	        int startPosition = 0;
	        int endPosition = 0;
	        int thretime = 0;
	        for (int i = 0; i < systemCallList.size(); i++) {
	            long diff = systemCallList.get(i).getTimeStamp() - mintime;
	            int validx = (int) (diff/interval);
	            if (i == 0){
	                thretime = validx;
	                }
	            if (thretime != validx) {
	                endPosition = i;
	                }
	             
	            if (startPosition != endPosition) {
	                List<SystemCall> ExecutionUnitlist = systemCallList.subList(
	                        startPosition, endPosition);
	                syswhole.add(ExecutionUnitlist);
	                startPosition = endPosition;
	                }
	             
	            if (i==systemCallList.size()-1){
	                List<SystemCall> ExecutionUnitlist = systemCallList.subList(
	                            startPosition, i+1);
	                syswhole.add(ExecutionUnitlist);
	                }
	             
	            thretime = validx;
	        }       
	         
		
		System.out.println("Number of units:" + syswhole.size());
		FileWriter f = null;
		BufferedWriter out = null;
		f = new FileWriter("/home/jhe16/Downloads/syslog_processing/sort-unit.txt");
		out = new BufferedWriter(f);
		List<SystemCall> Vec;
        int cnt = 0;
        int cnt2 = 0;
        for(int i = 0; i < syswhole.size(); i++){
            out.write(syswhole.get(i).get(0).getTimeStamp()+":" + "\n");
            cnt2 = cnt2 + 1;
            for(int k = 0; k < syswhole.get(i).size(); k++){ 
                cnt = cnt + 1;
                out.write(syswhole.get(i).get(k).getName() + "," + syswhole.get(i).get(k).getTimeStamp() + ","
                + syswhole.get(i).get(k).getTid() + "\n");  
                out.write("\n");    
                }
            }
        out.flush();
        out.close();
         
        System.out.println("Number of syscalls in syswhole:" + cnt);
        System.out.println("Number of units in syswhole:" + cnt2);
		
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
	public List<SystemCall> getSyscallList(String fileName, String procNames,
			String language) throws IOException {
		
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
//	      System.out.println(syscallList);
	        System.out.println("begin sorting");
	        List<SystemCall> sortsyscalls = syscallList;
	         
//	      for(int i = 0; i < sortsyscalls.size()-1; i++){
//	          for(int j = i + 1; j < sortsyscalls.size(); j++){
//	              if (sortsyscalls.get(i).getTimeStamp()>sortsyscalls.get(j).getTimeStamp()){
	    //              System.out.println(sortsyscalls.get(i).getTimeStamp());
//	                  SystemCall tmp = sortsyscalls.get(i);
//	                  SystemCall tmp2 = sortsyscalls.get(j);
//	                  sortsyscalls.set(i, tmp2);
//	                  sortsyscalls.set(j, tmp);
//	              }
//	          }
//	      }
	        SortTimestamp.quickSort(sortsyscalls);
	        System.out.println("end sorting");
	         
	     
	        FileWriter f = null;
	        BufferedWriter out=null;
	        f = new FileWriter("/home/jhe16/Downloads/syslog_processing/sort-sys.txt");
	        out = new BufferedWriter(f);
	        for(int i = 0; i <sortsyscalls.size(); i++){
	            out.write(sortsyscalls.get(i).getTimeStamp()+",");
	            out.write(sortsyscalls.get(i).getName() + ",");
	            out.write(sortsyscalls.get(i).getTid() + ",");
	            out.write(sortsyscalls.get(i).getExitValue() + ",");
	            out.write("\n");
	        }
			
		return sortsyscalls;
	}

	// Entry point, parses the arguments and calls the functions to segment the
	// input system call log.
	//"java" "hadoop,hdfs", "/home/.../lttngtrace.log"
//	public void startPreprocessing(String language,
	public List<List<SystemCall>> startPreprocessing(String language,
			String procNames, String syscallLog, long interval) throws IOException {
		 logger.info("Segmenting log " + syscallLog);
		 double start_t = System.currentTimeMillis();
		List<SystemCall> systemCallList = getSyscallList(syscallLog, procNames,
				language);
		logger.info("The Syscall List size = " + systemCallList.size());
		 double end_t = System.currentTimeMillis();
		 double elapsed = (end_t - start_t)/1000L;
	    logger.info("Total time: " + elapsed);
		List<List<SystemCall>> syscallLists = outputLog(systemCallList, interval);
		return syscallLists;
	}

}

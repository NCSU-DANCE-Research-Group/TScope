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

public class FuncLocPreProcessingNoFilterNew {

	public static Logger logger = Logger.getLogger(FuncLocPreProcessingNoFilterNew.class.getName());
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
//		FileWriter f = null;
//		BufferedWriter out = null;
//		f = new FileWriter("/home/jhe16/Downloads/syslog_processing/sort-unit.txt");
//		out = new BufferedWriter(f);
//		List<SystemCall> Vec;
//        int cnt = 0;
//        int cnt2 = 0;
//        for(int i = 0; i < syswhole.size(); i++){
//            out.write(syswhole.get(i).get(0).getTimeStamp()+":" + "\n");
//            cnt2 = cnt2 + 1;
//            for(int k = 0; k < syswhole.get(i).size(); k++){ 
//                cnt = cnt + 1;
//                out.write(syswhole.get(i).get(k).getName() + "," + syswhole.get(i).get(k).getTimeStamp() + ","
//                + syswhole.get(i).get(k).getTid() + "\n");  
//                out.write("\n");    
//                }
//            }
//        out.flush();
//        out.close();
//         
//        System.out.println("Number of syscalls in syswhole:" + cnt);
//        System.out.println("Number of units in syswhole:" + cnt2);
		
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
		String PID = "";
		for (String item : contextsRaw){
			if (item.contains(" tid ="))
				TID = item.split(" tid = ")[1].replaceAll("\\s", "");
			if(item.contains(" pid ="))
				PID = item.split(" pid = ")[1].replaceAll("\\s", "");
		}
		long timeStampLong = getMillisFromGMT(timeString.substring(0, timeString.length() - 6)) 
				* 1000000 +
				+ Long.parseLong(timeString.substring(timeString.length() - 6));
		String[] systemCallTemp = line.split("\\{")[0].split(" ");
		String systemCall = systemCallTemp[systemCallTemp.length - 1].replaceAll(":", "");
		int TO = -1;
		int OP = -1;
		if(line.contains("timeout_msecs")){
			String[] contextsRaw2 = line.split("\\{")[3].split("\\}")[0].split(",");
			String timeout = "";
			for (String item : contextsRaw2)
				if (item.contains("timeout_msecs ="))
					timeout = item.split("timeout_msecs = ")[1].replaceAll("\\s", "");
			TO = Integer.parseInt(timeout);
		}
		if(line.contains(" timeout ")){
			String[] contextsRaw2 = line.split("\\{")[3].split("\\}")[0].split(",");
			String timeout = "";
			for (String item : contextsRaw2)
				if (item.contains(" timeout ="))
					timeout = item.split(" timeout = ")[1].replaceAll("\\s", "");
			TO = Integer.parseInt(timeout);
		}
		if(line.contains("futex") && line.contains("op =")){
			String[] contextsRaw2 = line.split("\\{")[3].split("\\}")[0].split(",");
			String op = "";
			for (String item : contextsRaw2)
				if (item.contains("op ="))
					op = item.split("op = ")[1].replaceAll("\\s", "");
			OP = Integer.parseInt(op);
		}
		return new SystemCall(timeStampLong, systemCall, Integer.parseInt(TID));
	}
	
	private boolean filterTOSyscalls(String line){
		if(line.contains("__newselect") || line.contains("_adjtimex") 
				|| line.contains("_alarm") || line.contains("_clock_adjtime") 
				|| line.contains("_clock_getres") || line.contains("_clock_gettime") 
				|| line.contains("_clock_nanosleep") || line.contains("_clock_settime") 
				|| line.contains("_connect") || line.contains("_epoll_ctl") 
				|| line.contains("_epoll_pwait") || line.contains("_epoll_wait") 
				|| line.contains("_fanotify_init") || line.contains("_fanotify_mark") 
				|| line.contains("_fdatasync") || line.contains("_flock") 
				|| line.contains("_fsync") ||line.contains("_futex") 
				|| line.contains("_futimesat") ||line.contains("_getitimer") 
				|| line.contains("_gettimeofday") ||line.contains("_inotify_add_watch") 
				|| line.contains("_inotify_init") ||line.contains("_inotify_init1") 
				|| line.contains("_inotify_rm_watch") ||line.contains("_io_getevents") 
				|| line.contains("_ipc") ||line.contains("_mlock") ||line.contains("_mlock2") 
				|| line.contains("_mlockall") ||line.contains("_mq_notify") 
				|| line.contains("_mq_open") ||line.contains("_mq_timedreceive") 
				|| line.contains("_mq_timedsend") ||line.contains("_msgctl") 
				|| line.contains("_msgget") ||line.contains("_msgrcv") 
				|| line.contains("_msgsnd") ||line.contains("_msync") 
				|| line.contains("_munlock") ||line.contains("_munlockall") 
				|| line.contains("_nanosleep") ||line.contains("_open") 
				|| line.contains("_open_by_handle_at") ||line.contains("_openat") 
				|| line.contains("_pause") ||line.contains("_pipe") 
				|| line.contains("_pipe2") ||line.contains("_poll") 
				|| line.contains("_ppoll") ||line.contains("_pselect6") 
				|| line.contains("_read") ||line.contains("_readv") 
				|| line.contains("_recv") ||line.contains("_recvfrom") 
				|| line.contains("_recvmsg") ||line.contains("_recvmmsg") 
				|| line.contains("_restart_syscall") ||line.contains("_rt_sigaction") 
				|| line.contains("_rt_sigpending") ||line.contains("_rt_sigprocmask") 
				|| line.contains("_rt_sigqueueinfo") ||line.contains("_rt_sigreturn") 
				|| line.contains("_rt_sigsuspend") ||line.contains("_rt_sigtimedwait") 
				|| line.contains("_sched_rr_get_interval") ||line.contains("_select") 
				|| line.contains("_semctl") ||line.contains("_semget")
				|| line.contains("_semop") ||line.contains("_semtimedop") 
				|| line.contains("_send") ||line.contains("_sendfile") 
				|| line.contains("_sendfile64") ||line.contains("_sendmmsg") 
				|| line.contains("_sendmsg") ||line.contains("_sendto") 
				|| line.contains("_setitimer") ||line.contains("_settimeofday")
				|| line.contains("_sigaction") ||line.contains("_sigaltstack") 
				|| line.contains("_signal") ||line.contains("_signalfd") 
				|| line.contains("_signalfd4") ||line.contains("_sigpending") 
				|| line.contains("_sigprocmask") ||line.contains("_sigreturn") 
				|| line.contains("_sigsuspend") ||line.contains("_socket") 
				|| line.contains("_socketcall") ||line.contains("_socketpair") 
				|| line.contains("_ssetmask") ||line.contains("_stime") 
				|| line.contains("_sync") ||line.contains("_sync_file_range") 
				|| line.contains("_sync_file_range2") ||line.contains("_syncfs") 
				|| line.contains("_tgkill") ||line.contains("_time") 
				|| line.contains("_timer_create") ||line.contains("_timer_delete") 
				|| line.contains("_timer_getoverrun") ||line.contains("_timer_gettime")
				|| line.contains("_timer_settime") ||line.contains("_timerfd_create") 
				|| line.contains("_timerfd_gettime") ||line.contains("_timerfd_settime") 
				|| line.contains("_times") ||line.contains("_tkill") 
				|| line.contains("_utimes") ||line.contains("_vfork") 
				|| line.contains("_wait4") ||line.contains("_waitid") ||line.contains("_waitpid") 
				|| line.contains("_write") || line.contains("_writev")){
			return true;
		}
		return false;
	}

	// Segments the input file based on process name while inserting context
	// switch markers into the list.
	// Hands resulting list to the outputLog function to be further segmented by
	// TID and large time gaps.
	public List<SystemCall> getSyscallList(String fileName, String procNames, String language) {
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
		//SystemCall previousSyscall = new SystemCall();
		//boolean previousFlag = false;
		List<SystemCall> syscallList = new ArrayList<SystemCall>();
		Map<Integer, List<SystemCall>> scListMap = new HashMap<Integer, List<SystemCall>>();
		int pTID = -1;
		String line = "";
		try {
			instream = new FileInputStream(fileName);//for testing....
			logger.info("load " + fileName + " from storage successfully.");
			reader = new BufferedReader(new InputStreamReader(instream));
			while ((line = reader.readLine()) != null) {
				//if(!filterTOSyscalls(line)){
				//	continue;
				//}
				boolean use = false;
				Iterator<Entry<String, Integer>> pIt = pMap.entrySet().iterator();
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
						String[] contextsRaw = line.split("\\{")[2].split("\\}")[0].split(",");
						for (String item : contextsRaw){
							if (item.contains("tid =")){
								pTID = Integer.parseInt(item.split("tid = ")[1].replaceAll("\\s", ""));
								break;
							}
						}
					}
					continue;
				}
				SystemCall sc = getSystemCall(line);
				//if(sc.timeout == 2147483647){
				//	System.out.println("Get the syscall_entry_poll.");
				//	SystemCall test = getSystemCall(line);
				//}
				if (!scListMap.containsKey(sc.tid)) {
					List<SystemCall> sclist = new ArrayList<SystemCall>();
					scListMap.put(sc.tid, sclist);
				}
				//if (sc.tid != pTID) {// context switch, add a new syscall entry
				//	SystemCall cs = getSystemCall("-1");
				//	cs.tid = sc.tid;
				//	List<SystemCall> sclist = scListMap.get(sc.tid);
				//	sclist.add(cs);
				//	scListMap.put(sc.tid, sclist);
				//}
				List<SystemCall> sclist = scListMap.get(sc.tid);
				sclist.add(sc);
				scListMap.put(sc.tid, sclist);
				pTID = sc.tid;
			}
		} catch (Exception e) {
			logger.severe("Exception: " + e.toString());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// logger.info("scListMap size = " + scListMap.size());
		logger.info("Done creating list");
		try {
			Iterator<Entry<Integer, List<SystemCall>>> scListIt = scListMap.entrySet().iterator();
			Entry<Integer, List<SystemCall>> scListEntry = null;
			while (scListIt.hasNext()) {
				scListEntry = scListIt.next();
				List<SystemCall> syscalls = scListEntry.getValue();
				Map<String, Integer> scmap = new HashMap<String, Integer>();
				int index = 0;
				for (SystemCall sc2 : syscalls) {
					//if (sc2.name.equals("CONTEXT_SWITCH")) {
					//	// syscallList.add(sc2);
					//	continue;
					//}
					if (sc2.name.contains("syscall_exit")) {
						String scStr = removeSubString(sc2.name) + sc2.tid;
						if(scmap.containsKey(scStr)){
							int position = scmap.get(scStr);
							SystemCall tempsyscall = syscallList.get(position);
							tempsyscall.exitValue = sc2.timeStamp;
							//if(sc2.name.contains("syscall_exit_futex")){
							//	tempsyscall.timeout = (int) (tempsyscall.exitValue - tempsyscall.timeStamp)/1000000;
							//}
							syscallList.set(position, tempsyscall);
							scmap.remove(scStr);
						}/* else { //only syscall_exit, there is no entry to pair with
							sc2.setExitValue(sc2.timeStamp);
							sc2.setTimeStamp(-1);
							sc2.setName(removeSubString(sc2.name));
							syscallList.add(index, sc2);
							//scmap.put(scStr, index);
							index++;
						}*/					
					} else if (sc2.name.contains("syscall_entry")){
						//previousSyscall = sc2;
						//if(sc2.name.contains("getsockname")){
						//	System.out.println("getsockname");
						//	System.out.println("index = " + index);
						//}
						sc2.setName(removeSubString(sc2.name));
						syscallList.add(index, sc2);
						String scStr = sc2.name + sc2.tid;
						scmap.put(scStr, index);
						index++;
					}
				}
			}
		} catch (Exception e) {
			logger.severe("Exception: " + e.toString());
			scListMap.clear();
			return null;
		}
		scListMap.clear();
		List<SystemCall> sortsyscalls = syscallList;
		SortTimestamp.quickSort(sortsyscalls);
		return sortsyscalls;
	}
	
	/*str is in the format of syscall_entry_poll or syscall_exit_poll*/
	public String removeSubString(String str){
		if(str.contains("_entry_")){
			str = str.replace("_entry_", "_");
		} else if (str.contains("_exit_")){
			str = str.replace("_exit_", "_");
		}
		return str;
	}

	// Entry point, parses the arguments and calls the functions to segment the
	// input system call log.
	public List<List<SystemCall>> startPreprocessing(String language,
			String procNames, String syscallLog, long interval) throws IOException {
		// logger.info("Segmenting log " + syscallLog);
		// double start_t = System.currentTimeMillis();
		List<SystemCall> systemCallList = getSyscallList(syscallLog, procNames, language);
		//for(SystemCall syscall : systemCallList){
		//	if(syscall.timeout == 2147483647){
		//		System.out.println("Get the syscall_entry_poll.");
		//		break;
		//	}
		//}
		logger.info("The Syscall List size = " + systemCallList.size());
		// double end_t = System.currentTimeMillis();
		// double elapsed = (end_t - start_t)/1000L;
		// logger.info("Total time: " + elapsed);
		List<List<SystemCall>> syscallLists = outputLog(systemCallList, interval);
		return syscallLists;
	}

}

/*******************************************************************************
 * Copyright InsightFinder Inc., 2017
 *******************************************************************************/
package som_processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Set;
import java.util.logging.Logger;



public class processing {
	
	
	private static Logger logger = Logger.getLogger(processing.class.getName()); 
//	private static List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();//Appearance List, which stores unique syscall types
//	static List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
//	static List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
//	static Map<Integer, String> systemcallMap = new HashMap<Integer, String>();

    /*
     * include doHierarchicalClustering, identifyAbnormalExecutionUnit and outputFunctions
     */
	//preprocessing output, "java". "hadoop,hdfs", ""
	public static void doAnalysis(List<List<List<SystemCall>>> wholesyscallLists) 
			throws IOException {
		if (wholesyscallLists.size() == 0){
			logger.warning("The input is empty, cannot do function localization analysis.");
		}
		
		List<List<SystemCall>> syscallLists = new ArrayList<List<SystemCall>>();
		List<Integer> numsyscalls = new ArrayList<Integer>();
		int caltmp = 0;
		numsyscalls.add(caltmp);
		int numeu = 0;
		for(int i = 0; i < wholesyscallLists.size(); i++){      //i is the index of thread
			syscallLists.addAll(wholesyscallLists.get(i));	
			caltmp = caltmp + wholesyscallLists.get(i).size();      //caltmp is the index of time interval
			numsyscalls.add(caltmp);
//			System.out.println(caltmp + "\n");
		}
		System.out.println("caltmp: " + caltmp);
		
		long maxtime = wholesyscallLists.get(0).get(0).get(0).getTimeStamp();
		long mintime = wholesyscallLists.get(0).get(0).get(0).getTimeStamp();
		
		for(int i = 0; i < syscallLists.size(); i++){      //i is the index of thread
			for(int j = 0; j < syscallLists.get(i).size(); j++){  
			if (maxtime < syscallLists.get(i).get(j).getTimeStamp()){
				maxtime = syscallLists.get(i).get(j).getTimeStamp();
				}
			if (mintime > syscallLists.get(i).get(j).getTimeStamp()){
				mintime = syscallLists.get(i).get(j).getTimeStamp();
				}
			}
			
		}
		
		System.out.println("maxtime:" + maxtime);
		System.out.println("mintime:" + mintime);	
		int totalinterval  = (int) ((maxtime - mintime)/600000000);
		totalinterval = totalinterval/100 + 1;
		System.out.println("total:" + totalinterval);
		
		
//		System.out.println(syscallLists);
		logger.info("Start getting appearance, execution time and frequency vector.");
		double start_t = System.currentTimeMillis();
		
		List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();
		List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
		List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
//	    Map<Integer, String> systemcallMap = new HashMap<Integer, String>();
			
		getSegmentedVectors(syscallLists, appearanceVector, syscallTimeVector, syscallCountVector);
		
		
		// printing out vectors. frequecy, time, appearance
				FileWriter file0=null;
				BufferedWriter out0=null;
				FileWriter file1=null;
				BufferedWriter out1=null;
				FileWriter file2=null;
				BufferedWriter out2=null;
				
	//			try{
					
					System.out.println("syscallCountVector size: " + syscallCountVector.size()+"\n");
					System.out.println("syscallTimeVector size: " + syscallTimeVector.size()+"\n");
					System.out.println("appearanceVector size: " + appearanceVector.size()+"\n");
					
					List<List<List<Integer>>> appearancesplit = new ArrayList<List<List<Integer>>>();
					List<List<List<Long>>> timesplit = new ArrayList<List<List<Long>>>();
					List<List<List<Long>>> countsplit = new ArrayList<List<List<Long>>>();
					for (int idxinte = 1; idxinte < numsyscalls.size(); idxinte++){
						appearancesplit.add(appearanceVector.subList(numsyscalls.get(idxinte-1), 
								numsyscalls.get(idxinte)));
						timesplit.add(syscallTimeVector.subList(numsyscalls.get(idxinte-1), 
								numsyscalls.get(idxinte)));
						countsplit.add(syscallCountVector.subList(numsyscalls.get(idxinte-1), 
								numsyscalls.get(idxinte)));    //split the vector according to the thread
					}
					
					System.out.println(appearancesplit.size()+"\n");
					System.out.println(timesplit.size()+"\n");
					System.out.println(countsplit.size()+"\n");
					
					
					int numheader = appearanceVector.get(0).size();
					
					System.out.println("numheader: " + numheader);
					System.out.println("appearancesplit size: " + appearancesplit.size());
					System.out.println("timesplit size: " + timesplit.size());
					System.out.println("countsplit size: " + countsplit.size());
					System.out.println("number of thread: " + wholesyscallLists.size());
					
					for (int idx = 0; idx < wholesyscallLists.size(); idx++){  //idx is the index of thread
						
						try{
						
						List<Long> eEUAppVec;	
						file0 = new FileWriter("/home/jhe16/Downloads/syslog_processing/"
						+ "thread_" + wholesyscallLists.get(idx).get(0).get(0).getTid() + "_freqvector.csv");
						out0 = new BufferedWriter(file0);
						
						FileWriter file04 = new FileWriter("/home/jhe16/Downloads/syslog_processing/"
								+ "thread_" + wholesyscallLists.get(idx).get(0).get(0).getTid() + "_freqvector_test.csv");
								BufferedWriter out04 = new BufferedWriter(file04);
						
						file1 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" 
						+ "thread_" + wholesyscallLists.get(idx).get(0).get(0).getTid() + "_timevector.csv");
						out1 = new BufferedWriter(file1);
						
						file2 = new FileWriter("/home/jhe16/Downloads/syslog_processing/"
						+ "thread_" + wholesyscallLists.get(idx).get(0).get(0).getTid() + "_appvector.csv");
						out2 = new BufferedWriter(file2);
						
		//				System.out.println("Tid of " + wholesyscallLists.get(idx).get(0).get(0).getTid());
							
						out0.write("timestamp" + ",");
						out1.write("timestamp" + ",");
						out2.write("timestamp" + ",");
						for (int idxheader = 0; idxheader < numheader-1; idxheader++){
							out0.write("sys-" + idxheader + ",");
							out1.write("sys-" + idxheader + ",");           // write the syscall number
							out2.write("sys-" + idxheader + ",");
						}
						
						out0.write("sys-" + (numheader-1));
						out1.write("sys-" + (numheader-1));           // last line does not contain coma
						out2.write("sys-" + (numheader-1));
						
						out0.write("\n");
						out1.write("\n");
						out2.write("\n");
						
						
						long writetime = wholesyscallLists.get(idx).get(0).get(0).getTimeStamp();
						int firstone = (int) ((writetime - mintime)/600000000);
						firstone = firstone/100;
						for(int i = 0; i < firstone; i++){          //add the previous time period thread doesn't last
							out0.write(mintime/1000 + i*60000000 +",");  //write the timestamp
							out1.write(mintime/1000 + i*60000000 +",");
							out2.write(mintime/1000 + i*60000000 +",");
							for(int j = 0 ; j < countsplit.get(0).get(0).size()-1; j++){
								out0.write("0" + ",");
								out1.write("0" + ",");
								out2.write("0" + ",");
							}
							out0.write("0");
							out1.write("0");
							out2.write("0");
							
							out0.write("\n");
							out1.write("\n");
							out2.write("\n");
							
						}
						
						int lower = firstone;
						writetime = wholesyscallLists.get(idx).get(countsplit.get(idx).size()-1).get(0).getTimeStamp();
						int upper = (int) ((writetime - mintime)/600000000);
						upper = upper/100;
						
						for (int pre = lower; pre < upper + 1; pre++){
							boolean sign = false;
							for(int i = 0; i < countsplit.get(idx).size(); i++){
								writetime = wholesyscallLists.get(idx).get(i).get(0).getTimeStamp();
								firstone = (int) ((writetime - mintime)/600000000);
								firstone = firstone/100;
								
								if (pre == firstone){
								   sign = true;
								   out0.write(mintime/1000 + firstone*60000000 +",");		
								   eEUAppVec = countsplit.get(idx).get(i);
							
							       for(int j = 0 ; j < eEUAppVec.size()-1; j++)
							      	   out0.write(eEUAppVec.get(j) + ",");
							       out0.write(eEUAppVec.get(eEUAppVec.size()-1) + "\n");
							       break;
							    
							   }
							}
							
							if (sign == false){
								out0.write(mintime/1000 + pre*60000000 +",");	
								for(int j = 0 ; j < countsplit.get(idx).get(0).size()-1; j++){
									out0.write("0" + ",");
								}
								out0.write("0" + "\n");								
							}
						}
						
						
						for (int pre = lower; pre < upper + 1; pre++){	
							boolean sign = false;
							for(int i1 = 0; i1 < timesplit.get(idx).size(); i1++){
								writetime = wholesyscallLists.get(idx).get(i1).get(0).getTimeStamp();
								firstone = (int) ((writetime - mintime)/600000000);
								firstone = firstone/100;
								
								if (pre == firstone){
									sign = true;
									out1.write(mintime/1000 + firstone*60000000 +",");
								    eEUAppVec = timesplit.get(idx).get(i1);
								
								    for(int j= 0 ; j < eEUAppVec.size()-1; j++){
								    	out1.write(eEUAppVec.get(j) + ",");
								    }
								    out1.write(eEUAppVec.get(eEUAppVec.size()-1) + "\n");
								    break;
								}	
							
							}
							
							if (sign == false){
								out1.write(mintime/1000 + pre*60000000 +",");	
								for(int j = 0 ; j < countsplit.get(idx).get(0).size()-1; j++){
									out1.write("0" + ",");
								}
								out1.write("0" + "\n");
							}
						}
						
						List<Integer> eEUAppVec1;
						for (int pre = lower; pre < upper + 1; pre++){
							boolean sign = false;
							for(int i2 = 0; i2 < appearancesplit.get(idx).size(); i2++){
								writetime = wholesyscallLists.get(idx).get(i2).get(0).getTimeStamp();
								firstone = (int) ((writetime - mintime)/600000000);
								firstone = firstone/100;
								
								if (pre == firstone){
									sign = true;
									out2.write(mintime/1000 + firstone*60000000 +",");
									eEUAppVec1 = appearancesplit.get(idx).get(i2);
									
									for(int j = 0 ; j < eEUAppVec1.size()-1; j++)
										out2.write(eEUAppVec1.get(j) + ",");
									out2.write(eEUAppVec1.get(eEUAppVec1.size()-1) + "\n");
									break;
									}										
							}
							
							if (sign == false){
								out2.write(mintime/1000 + pre*60000000 +",");	
								for(int j = 0 ; j < countsplit.get(idx).get(0).size()-1; j++){
									out2.write("0" + ",");
								}
								out2.write("0" + "\n");
							}
						}

						for(int i = upper + 1; i < totalinterval; i++){      //add the latter time period 
							out0.write(mintime/1000 + i*60000000 +",");     //write the timestamp
							out1.write(mintime/1000 + i*60000000 +","); 
							out2.write(mintime/1000 + i*60000000 +","); 
							for(int j = 0 ; j < appearancesplit.get(0).get(0).size(); j++){
								out0.write("0" + ",");
								out1.write("0" + ",");
								out2.write("0" + ",");
							}
							out0.write("0");
							out1.write("0");
							out2.write("0");
							
							out0.write("\n");
							out1.write("\n");
							out2.write("\n");
							
						}
						
					
						}catch(Exception e){
						e.printStackTrace();
						}finally{
							out0.flush();
							if(file0!=null)
								file0.close();
							if(out0!=null)
								out0.close();
							out1.flush();
							if(file1!=null)
								file1.close();
							if(out1!=null)
								out1.close();
							out2.flush();
							if(file2!=null)
								file2.close();
							if(out2!=null)
								out2.close();
							}		
				}
		
		double end_t = System.currentTimeMillis();
		logger.info("Finish getting appearance, execution time and frequency vector: " + (end_t - start_t)/1000L + " seconds.");
		
	
	}
		
	/*
	 * input: syscallists
	 * output: MAVector : unique syscall list
	 *         TimeVector: syscall average elapsed time vector
	 *         CountVector: syscall counts vector
	 */
	public static void getSegmentedVectors(List<List<SystemCall>> syscallLists,
			List<List<Integer>> MAVector, 
			List<List<Long>> TimeVector, 
			List<List<Long>> CountsVector){
		
		List<Map<String, Long>> allSegmentedRawCounts = new ArrayList<Map<String, Long>>();
		List<Map<String, Long>> allSegmentedRawTimes = new ArrayList<Map<String, Long>>();
		List<List<String>> sysCalls = new ArrayList<List<String>>();
		Map<Integer, String> systemcallMap = new HashMap<Integer, String>();
		getSegmentedRawResults(syscallLists, allSegmentedRawCounts, allSegmentedRawTimes, sysCalls);
		
		////for Syscalls: Get the unique elements
		List<String> uElems = new ArrayList<String>();
		for(int i = 0; i < sysCalls.size(); i++){
			List<String> cSet = sysCalls.get(i);
			for(int j = 0; j < cSet.size(); j++){
				String cSetEntry = cSet.get(j);
				if(!uElems.contains(cSetEntry))
					uElems.add(cSetEntry);
			}
		}
		//Build the Appearance Vectors
		for(int i = 0; i < sysCalls.size(); i++){
			List<Integer> currentMAV = new ArrayList<Integer>();
			List<String> cSet = sysCalls.get(i);
			for(int j = 0; j < uElems.size(); j++){
				if(!cSet.contains(uElems.get(j))){
					currentMAV.add(0);
				}else{
					currentMAV.add(1);
				}
			}
			MAVector.add(currentMAV);
		}		
		////for RawCounts: Get unique elements
		/*uElems = new ArrayList<String>();
		for(int i = 0; i < allSegmentedRawCounts.size(); i++){
			Map<String, Long> cMap = allSegmentedRawCounts.get(i);
			Iterator<Entry<String, Long>> cMapIt = cMap.entrySet().iterator();
			Entry<String, Long> cMapEntry = null;
			while(cMapIt.hasNext()){
				cMapEntry = cMapIt.next();
				if(!uElems.contains(cMapEntry.getKey())){ //syscall types
					uElems.add(cMapEntry.getKey());
				}
			}
		}*/
		//Build Vectors
		for(int i = 0; i < allSegmentedRawCounts.size(); i++){
			List<Long> cCount = new ArrayList<Long>();
			Map<String, Long> cMap = allSegmentedRawCounts.get(i);
			for(int j = 0; j < uElems.size(); j++){
				String uEEntry = uElems.get(j); //syscall type
				if(!cMap.containsKey(uEEntry))
					cCount.add((long) 0);
				else
					cCount.add(cMap.get(uEEntry));
			}
			CountsVector.add(cCount);
		}
		////for RawTime: Build the system call map
		for(int j = 0; j < uElems.size(); j++){
			systemcallMap.put(j, uElems.get(j));
		}
		//Build the Vectors
		for(int i = 0; i < allSegmentedRawCounts.size(); i++)
		{
			List<Long> cTime = new ArrayList<Long>();
			Map<String, Long> cCountMap = allSegmentedRawCounts.get(i);
			Map<String, Long> cTimeMap = allSegmentedRawTimes.get(i);
			for(int j = 0; j < uElems.size(); j++){
				String uEEntry = uElems.get(j);
				if(!cCountMap.containsKey(uEEntry))
					cTime.add((long) 0);
				else{
					long valToAdd =  cTimeMap.get(uEEntry) / cCountMap.get(uEEntry); //average time
					cTime.add(valToAdd);
				}
			}
			TimeVector.add(cTime);
		}
	}

	/*
	 * input: syscallLists
	 * output: allSegmentedRawCounts: a map of syscall type to the counts
	 *         allSegmentedRawTimes: a map of syscall type to the sum of elaspe time 
	 *         sysCalls: all syscall types, there are repetition.
	 */
	public static void getSegmentedRawResults(List<List<SystemCall>> syscallLists,
			List<Map<String, Long>> allSegmentedRawCounts,
			List<Map<String, Long>> allSegmentedRawTimes,
			List<List<String>> sysCalls) {
		try {
			for(List<SystemCall> syscallList : syscallLists){
				if (syscallList.size() == 0)
					continue;
				Map<String, Long> segmentedRawCount = new HashMap<String, Long>();
				Map<String, Long> segmentedRawTime = new HashMap<String, Long>();
				List<String> cSyscalls = new ArrayList<String>();
					for( SystemCall syscall : syscallList){
						if (syscall.getName().equalsIgnoreCase("sys_gettimeofday")
								|| syscall.getName().equalsIgnoreCase("sys_mmap")
								|| syscall.getName().equalsIgnoreCase("sys_mmap_pgoff"))
							continue;
						if (!cSyscalls.contains(syscall.getName()))
							cSyscalls.add(syscall.getName());
						if(segmentedRawCount.containsKey(syscall.getName()))
							segmentedRawCount.put(syscall.getName(), segmentedRawCount.get(syscall.getName()) + 1);
						else
							segmentedRawCount.put(syscall.getName(), (long) 1);
						long elapsedTime = syscall.getExitValue() - syscall.getTimeStamp();
						if(segmentedRawTime.containsKey(syscall.getName()))
							segmentedRawTime.put(syscall.getName(), segmentedRawTime.get(syscall.getName()) + elapsedTime);
						else
							segmentedRawTime.put(syscall.getName(), elapsedTime);
					}
					allSegmentedRawCounts.add(segmentedRawCount);
					allSegmentedRawTimes.add(segmentedRawTime);
					sysCalls.add(cSyscalls);
		//			System.out.println("here:" + cSyscalls);
			}
		}
		catch (Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.severe(stacktrace);
		}
	}
	
}

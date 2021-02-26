package som_processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;


public class processing2 {
	
	private static Logger logger = Logger.getLogger(processing2.class.getName()); 
//	FunLocPreProcessing PreProcessing = new FunLocPreProcessing();
//	List<List<SystemCall>> syscall = PreProcessing.startPreprocessing("java", "phoenix", "/Users/jzhe/Documents/trace/syscall_sys-phoenix-new.log");
	static List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();//Appearance List, which stores unique syscall types
	static List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
	static List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
	static Map<Integer, String> systemcallMap = new HashMap<Integer, String>();
	static Map<String, Long> allSegmentedRawCounts_final = new HashMap<String, Long>();
	static Map<String, Long> allSegmentedRawTimes_final = new HashMap<String, Long>();
	static List<String> sysCalls_final = new ArrayList<String>();
	
	
	public static void doAnalysis(List<SystemCall> syscallLists) 
			throws IOException{
		
		getSegmentedVectors(syscallLists, appearanceVector, syscallTimeVector, syscallCountVector);
		
		FileWriter file0=null;
		BufferedWriter out0=null;
		FileWriter file1=null;
		BufferedWriter out1=null;
		FileWriter file2=null;
		BufferedWriter out2=null;
	
	try{
	
		List<Long> eEUAppVec;	
		file0 = new FileWriter("/Users/jzhe/Documents/syscall-processing/pslog_freqvector.txt");
		out0 = new BufferedWriter(file0);
		
		
//		for(int i = 0; i < syscallCountVector.size(); i++){
			out0.write(syscallLists.get(0).getTimeStamp()+",");
			eEUAppVec = syscallCountVector.get(0);
			for(int j = 0 ; j < eEUAppVec.size(); j++)
				out0.write(eEUAppVec.get(j) + ",");
			out0.write("\n");	
//		}
		file1 = new FileWriter("/Users/jzhe/Documents/syscall-processing/pslog_timevector.txt");
		out1 = new BufferedWriter(file1);
			
//		for(int i1 = 0; i1 < syscallTimeVector.size(); i1++){
			out1.write(syscallLists.get(0).getTimeStamp()+",");
			eEUAppVec = syscallTimeVector.get(0);
			for(int j= 0 ; j < eEUAppVec.size(); j++)
				out1.write(eEUAppVec.get(j) + ",");
			out1.write("\n");	
//		}		
		file2 = new FileWriter("/Users/jzhe/Documents/syscall-processing/pslog_appvector.txt");
		out2 = new BufferedWriter(file2);
				
		List<Integer> eEUAppVec1;
//		for(int i2 = 0; i2 < appearanceVector.size(); i2++){
			out2.write(syscallLists.get(0).getTimeStamp()+",");
			eEUAppVec1 = appearanceVector.get(0);
			for(int j = 0 ; j < eEUAppVec1.size(); j++)
				out2.write(eEUAppVec1.get(j) + ",");
			out2.write("\n");						
			
//		}
		
		
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
	
	public static void getSegmentedVectors(List<SystemCall> syscallLists,
			List<List<Integer>> MAVector, 
			List<List<Long>> TimeVector, 
			List<List<Long>> CountsVector){
		
		Map<String, Long> allSegmentedRawCounts = new HashMap<String, Long>();
		Map<String, Long> allSegmentedRawTimes = new HashMap<String, Long>();
		List<String> sysCalls = new ArrayList<String>();
	//	List<String> calls2 = new ArrayList<String>();
		getSegmentedRawResults(syscallLists, allSegmentedRawCounts, allSegmentedRawTimes, sysCalls);
	//	System.out.println(sysCalls_final);
	//	System.out.println(allSegmentedRawCounts_final);
	//	System.out.println(allSegmentedRawTimes_final);
		
		////for Syscalls: Get the unique elements
		List<String> uElems = new ArrayList<String>();
//		for(int i = 0; i < sysCalls.size(); i++){
			List<String> cSet = new ArrayList<String>();
			cSet = sysCalls_final;
//			System.out.println(sysCalls);
			for(int j = 0; j < cSet.size(); j++){
				String cSetEntry = cSet.get(j);
				if(!uElems.contains(cSetEntry))
//					System.out.println(cSetEntry);
					uElems.add(cSetEntry);
			}
//		}
		//Build the Appearance Vectors
//		for(int i = 0; i < sysCalls.size(); i++){
			List<Integer> currentMAV = new ArrayList<Integer>();
			List<String> cSet1 = sysCalls_final;
			for(int j = 0; j < uElems.size(); j++){
				if(!cSet1.contains(uElems.get(j))){
					currentMAV.add(0);
				}else{
					currentMAV.add(1);
				}
			}
			MAVector.add(currentMAV);
//		}		
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
//		for(int i = 0; i < allSegmentedRawCounts.size(); i++){
			List<Long> cCount = new ArrayList<Long>();
			Map<String, Long> cMap = allSegmentedRawCounts_final;
			for(int j = 0; j < uElems.size(); j++){
				String uEEntry = uElems.get(j); //syscall type
				if(!cMap.containsKey(uEEntry))
					cCount.add((long) 0);
				else
					cCount.add(cMap.get(uEEntry));
			}
//			System.out.println(cCount);
			CountsVector.add(cCount);
//		}
		////for RawTime: Build the system call map
		for(int j = 0; j < uElems.size(); j++){
			systemcallMap.put(j, uElems.get(j));
		}
		//Build the Vectors
//		for(int i = 0; i < allSegmentedRawCounts.size(); i++)
//		{
			List<Long> cTime = new ArrayList<Long>();
			Map<String, Long> cCountMap = allSegmentedRawCounts_final;
			Map<String, Long> cTimeMap = allSegmentedRawTimes_final;
			for(int j = 0; j < uElems.size(); j++){
				String uEEntry = uElems.get(j);
				if(!cCountMap.containsKey(uEEntry))
					cTime.add((long) 0);
				else{
					long valToAdd =  cTimeMap.get(uEEntry) / cCountMap.get(uEEntry); //average time
					cTime.add(valToAdd);
				}
			}
//			System.out.println(cTime);
			TimeVector.add(cTime);
		}
//	}
	
	
	public static void getSegmentedRawResults(List<SystemCall> syscallLists,
			Map<String, Long> allSegmentedRawCounts,
			Map<String, Long> allSegmentedRawTimes,
			List<String> sysCalls) {
	//	Map<Integer, Integer> threads = new HashMap<Integer, Integer>();
//		int totalUnits = 0;
		try {
	//		int position = 0;
	//		for(List<SystemCall> syscallList : syscallLists){
		//		if (syscallLists.size() == 0)
		//			continue;
				Map<String, Long> segmentedRawCount = new HashMap<String, Long>();
				Map<String, Long> segmentedRawTime = new HashMap<String, Long>();
				List<String> cSyscalls = new ArrayList<String>();
//					int tid = syscallList.get(0).getTid();
//					threads.put(tid, 1);
//					totalUnits++;
				//	//Add the file position to the list of file positions for this thread
				//	if(!executionUnitList.containsKey(tid)){
				//		List<Integer> toAdd = new ArrayList<Integer>();
				//		toAdd.add(position);
			//			executionUnitList.put(tid, toAdd);
				//		//Store this unit's position in the file position vector
				//		unitPositionMap.put(position, 0);
				//	}
				//	else
				//	{
				//		List<Integer> cVec = executionUnitList.get(tid);
				//		cVec.add(position);
				//		executionUnitList.put(tid, cVec);
				//		//Store this unit's position in the file position vector
				//		unitPositionMap.put(position, cVec.size()-1);
				//	}
				//	tuIDs.add(position);
				//	threadIDMap.put(position, tid);
					for( SystemCall syscall : syscallLists){
				//		System.out.println(syscall);
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
					allSegmentedRawCounts = segmentedRawCount;
					allSegmentedRawTimes = segmentedRawTime;
					sysCalls = cSyscalls;
					sysCalls_final = sysCalls;
					allSegmentedRawCounts_final = allSegmentedRawCounts;
					allSegmentedRawTimes_final = allSegmentedRawTimes;
		//			System.out.println("here:" + sysCalls);
		//			return sysCalls;
		//			position += 1;
			
		}
		catch (Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.severe(stacktrace);
		}
		
//		return sysCalls;
//		int numThreads = 0;
//		numThreads = threads.size();
//		logger.info("Num Threads: " + numThreads + ", Num Units: " + totalUnits);
//		System.out.println(sysCalls);
	}

	
}

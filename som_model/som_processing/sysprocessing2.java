/*******************************************************************************
 * Copyright InsightFinder Inc., 2017
 *******************************************************************************/
package som_processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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


public class sysprocessing2 {
	
	public static class TUCluster{
		int averagedistance;
		int medoidIdentifier;
		int diameter;
		List<Integer>TUIds;
		
		TUCluster(){
			averagedistance = -1;
			medoidIdentifier = -1;
			diameter = -1;
			TUIds = new ArrayList<Integer>();
		}
	}
	
	public class Parameters {
		int threadID = 0;
		long unitID = 0;
		int labelVal = 0;
		double percentage = 0;
		long currentPosition = 0;
		long unitPosition = 0;
	}
	
	private static int DIAMETER_THRESHOLD = 4;
	static Map<Integer, List<Integer>> TUIdMap = new HashMap<Integer, List<Integer>>();
	//static Map<Integer, List<Long>> countMap = new HashMap<Integer, List<Long>>();
	//static Map<Integer, List<Long>> timeMap = new HashMap<Integer, List<Long>>();
	static Map<Integer, List<Integer>> executionUnitList = new HashMap<Integer, List<Integer>>(); //Per-thread list of file positions
	static Map<Integer, Integer> unitPositionMap = new HashMap<Integer, Integer>(); //Keeps a Mapping of unit ID -> List position
	//static Set<Integer> affectedThreads = new HashSet<Integer>();
//	static List<RealVector> medoids = new ArrayList<RealVector>();
	static List<TUCluster> rdaemonClusterVector = new ArrayList<TUCluster>(); //execution unit cluster vector
	static List<TUCluster> oldClusterVector = new ArrayList<TUCluster>();
	static List<Integer> tuIDs = new ArrayList<Integer>();
	static Map<Integer, Integer> threadIDMap = new HashMap<Integer, Integer>();
	static Map<Long, Double> percentageMap = new HashMap<Long, Double>();
	static Map<Integer, String> systemcallMap = new HashMap<Integer, String>();
	private static List<Integer> pidList = new ArrayList<Integer>();
	private static List<String> procNameList = new ArrayList<String>();
	private static String language = null;
    private static String outputFileFreq = null;
    private static String outputFileTime = null;
	private static Logger logger = Logger.getLogger(sysprocessing2.class.getName());  
	private static boolean doLog = false;
	static List<Integer> abnormalExecutionUnits = new ArrayList<Integer>();
	static List<Integer> abnormalFreqExecutionUnits = new ArrayList<Integer>();
	static List<Integer> abnormalTimeExecutionUnits = new ArrayList<Integer>();
	static Map<Integer, Long> abnormalExecutionUnitFreq = new HashMap<Integer, Long>();
	static Map<Integer, Long> abnormalExecutionUnitTime = new HashMap<Integer, Long>();
	static List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();//Appearance List, which stores unique syscall types
	static List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
	static List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
	
	
	public static void setLanguage(String language){
		sysprocessing2.language = language;
	}
    
	public void setPIDList(List<Integer> toSet){
		pidList = toSet;
	}
	
	public static void setPNameList(String toSet){
		//procNameList = toSet;
		String[] rawParsed = toSet.split(",");
		for(int i = 0; i < rawParsed.length; i++){
			procNameList.add(rawParsed[i]);
		}
	}
	
	public List<TUCluster> getRdaemonClusterVector(){
		return rdaemonClusterVector;
	}
	
	
	static double getEuclideanDistance(List<Long> r1, List<Long> r2)
	{
		double returnDistance = 0.0;
		for(int i = 0 ; i < r1.size(); i++)
		{
			long cDiff = r1.get(i) - r2.get(i);
			long valToAdd = cDiff * cDiff;
			returnDistance += valToAdd;
		}
		return Math.sqrt(returnDistance);
	}
	
	
	//Gets the ManhattenDistance from one array to another
	static int getManhattanDistance(List<Integer> r1, List<Integer> r2)
	{
		int returnDistance = 0;
		for(int i = 0 ; i < r1.size(); i++)
			returnDistance += Math.abs(r1.get(i) - r2.get(i));
		return returnDistance;
	}
	
	static long getManhattanDistanceLong(List<Long> r1, List<Long> r2)
	{
		long returnDistance = 0;
		for(int i = 0 ; i < r1.size(); i++)
			returnDistance += Math.abs(r1.get(i) - r2.get(i));
		return returnDistance;
	}
	
	//Gets the GraphEditDistance from one array to another
	long getGraphEditDistance(List<Long> r1, List<Long> r2)
	{
		long returnDistance = 0;
		for(int i = 0 ; i < r1.size(); i++)
		{
			long val1 = r1.get(i);
			long val2 = r2.get(i);
			if(val1 != val2)
				returnDistance++;
		}
		return returnDistance;

	}
	
	//Gets the standard deviation of an array
	double getStandardDeviation(long avg, List<Long> entries)
	{
		double total = 0.0;
		for(int i = 0; i < entries.size(); i++)
		{
			long cDiff = entries.get(i) - avg;
			cDiff = cDiff * cDiff;
			total += cDiff;
		}
		total = total / entries.size();
		return Math.sqrt(total);
	}
	
	//Gets the mean of an array
	long getMean(List<Long> values)
	{
		long total = 0;
		long count = (long)values.size();
		for(int i = 0; i < values.size(); i++)
		    total += values.get(i);
		return total / count;
	}
	

	static int getDistanceFromGlobalDistanceMatrix(int id1, int id2)
	{
		if(!TUIdMap.containsKey(id1))
			return -1;
		if(!TUIdMap.containsKey(id2))
			return -1;
		return getManhattanDistance(appearanceVector.get(id1), appearanceVector.get(id2));
	}
    
 

    /*
     * include doHierarchicalClustering, identifyAbnormalExecutionUnit and outputFunctions
     */
	//preprocessing output, "java". "hadoop,hdfs", ""
//	public static Object[] doAnalysis(List<List<SystemCall>> syscallLists, 
	public static List<Map<Integer, Map<String, Integer>>> doAnalysis(List<List<SystemCall>> wholesyscallLists, 
			String language, String procNames, List<String> profileNames, long gap, String base_loc) 
			throws IOException {
		if (wholesyscallLists.size() == 0){
			logger.warning("The input is empty, cannot do function localization analysis.");
			return null;
		}
		
		 long maxtime = wholesyscallLists.get(0).get(0).getTimeStamp();
	        long mintime = wholesyscallLists.get(0).get(0).getTimeStamp();
	         
	        for(int i = 0; i < wholesyscallLists.size(); i++){      
	            for(int j = 0; j < wholesyscallLists.get(i).size(); j++){  
	            if (maxtime < wholesyscallLists.get(i).get(j).getTimeStamp()){
	                maxtime = wholesyscallLists.get(i).get(j).getTimeStamp();
	                }
	            if (mintime > wholesyscallLists.get(i).get(j).getTimeStamp()){
	                mintime = wholesyscallLists.get(i).get(j).getTimeStamp();
	                }
	            }
	             
	        }
	        
		setLanguage(language);
		setPNameList(procNames);
		
		System.out.println("maxtime:" + maxtime);
		System.out.println("mintime:" + mintime);   
		int totalinterval  = (int) ((maxtime - mintime)/gap) + 1;
		System.out.println("total:" + totalinterval);
	        
		logger.info("Start getting appearance, execution time and frequency vector.");
		double start_t = System.currentTimeMillis();
		getSegmentedVectors(wholesyscallLists, appearanceVector, syscallTimeVector, syscallCountVector);
		
		
		//frequecy, time, appearance
		FileWriter file0=null;
		BufferedWriter out0=null;
		FileWriter file1=null;
		BufferedWriter out1=null;
		FileWriter file2=null;
		BufferedWriter out2=null;
		
		FileWriter file4 = null;
		BufferedWriter out4 = null;
		FileWriter file5 = null;
		BufferedWriter out5 = null;
		FileWriter file6 = null;
		BufferedWriter out6 = null;
		
		try{
			
			System.out.println("syscallCountVector size: " + syscallCountVector.size()+"\n");
			System.out.println("syscallTimeVector size: " + syscallTimeVector.size()+"\n");
			System.out.println("appearanceVector size: " + appearanceVector.size()+"\n");
			
			file0 = new FileWriter(base_loc + "11252-25_freqvector.csv", true);
			out0 = new BufferedWriter(file0);
				
			file4 = new FileWriter(base_loc 
			+ "11252-25_freqvector_test.csv", true);
			out4 = new BufferedWriter(file4);
				
			file1 = new FileWriter(base_loc + "11252-25_timevector.csv", true);
			out1 = new BufferedWriter(file1);
			
			file5 = new FileWriter(base_loc 
			+ "11252-25_timevector_test.csv", true);
			out5 = new BufferedWriter(file5);
			
			file2 = new FileWriter(base_loc + "11252-25_appvector.csv", true);
			out2 = new BufferedWriter(file2);
			
			file6 = new FileWriter(base_loc 
					+ "11252-25_appvector_test.csv", true);
			out6 = new BufferedWriter(file6);
					
			int numheader = syscallCountVector.get(0).size();
            System.out.println("numheader:" + numheader);
             
            int thres = totalinterval/2;
                     
            out0.write("timestamp" + ",");
            out1.write("timestamp" + ",");
            out2.write("timestamp" + ",");
             
            out4.write("timestamp" + ",");
            out5.write("timestamp" + ",");
            out6.write("timestamp" + ",");
             
            for (int idxheader = 0; idxheader < numheader-1; idxheader++){
                out0.write("sys-" + idxheader + ",");
                out1.write("sys-" + idxheader + ",");           // write the syscall number
                out2.write("sys-" + idxheader + ",");
                 
                out4.write("sys-" + idxheader + ",");
                out5.write("sys-" + idxheader + ",");           
                out6.write("sys-" + idxheader + ",");
                }
                 
            out0.write("sys-" + (numheader-1));
            out1.write("sys-" + (numheader-1));           // last line does not contain coma
            out2.write("sys-" + (numheader-1));
             
                out4.write("sys-" + (numheader-1));
                out5.write("sys-" + (numheader-1));           // last line does not contain coma
                out6.write("sys-" + (numheader-1));
                 
                out0.write("\n");
                out1.write("\n");
                out2.write("\n");
                 
                out4.write("\n");
                out5.write("\n");
                out6.write("\n");
                 
                long writetime = wholesyscallLists.get(0).get(0).getTimeStamp();
                int firstone = (int) ((writetime - mintime)/gap);
                int lower = firstone;
                writetime = wholesyscallLists.get(syscallCountVector.size()-1).get(0).getTimeStamp();
                int upper = (int) ((writetime - mintime)/gap);
                 
                System.out.println("lower:" + lower);
                System.out.println("upper:" + upper);
                 
                List<Long> eEUAppVec;
                for (int pre = lower; pre < thres + 1; pre++){    //write training data
                    boolean sign = false;
                    for(int i = 0; i < syscallCountVector.size(); i++){
                        writetime = wholesyscallLists.get(i).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                           sign = true;
                           out0.write(mintime/1000000 + firstone*(gap/1000000) +",");
                           out4.write(mintime/1000000 + firstone*(gap/1000000) +",");
                           eEUAppVec = syscallCountVector.get(i);
                     
                           for(int j = 0 ; j < eEUAppVec.size()-1; j++){
                               out0.write(eEUAppVec.get(j) + ",");
                               out4.write(eEUAppVec.get(j) + ",");
                           }
                           out0.write(eEUAppVec.get(eEUAppVec.size()-1) + "\n");
                           out4.write(eEUAppVec.get(eEUAppVec.size()-1) + "\n");
                           break;                               
                       }
                    }
                     
                    if (sign == false){
                        out0.write(mintime/1000000 + pre*(gap/1000000) +",");
                        out4.write(mintime/1000000 + pre*(gap/1000000) +",");
                        for(int j = 0 ; j < syscallCountVector.get(0).size()-1; j++){
                            out0.write("0" + ",");
                            out4.write("0" + ",");
                        }
                        out0.write("0" + "\n"); 
                        out4.write("0" + "\n");
                    }
        //          System.out.println("writing finished in the iteration: " + pre);
                }
                 
                for (int pre = thres + 1; pre < upper + 1; pre++){   //write test data
                    boolean sign = false;
                    for(int i = 0; i < syscallCountVector.size(); i++){
                        writetime = wholesyscallLists.get(i).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                           sign = true;
                           out4.write(mintime/1000000 + firstone*(gap/1000000) +",");        
                           eEUAppVec = syscallCountVector.get(i);
                     
                           for(int j = 0 ; j < eEUAppVec.size()-1; j++)
                               out4.write(eEUAppVec.get(j) + ",");
                           out4.write(eEUAppVec.get(eEUAppVec.size()-1) + "\n");
                           break;                               
                       }
                    }
                     
                    if (sign == false){
                        out4.write(mintime/1000000 + pre*(gap/1000000) +",");    
                        for(int j = 0 ; j < syscallCountVector.get(0).size()-1; j++){
                            out4.write("0" + ",");
                        }
                        out4.write("0" + "\n");                             
                    }
                }
                 
                 
                for (int pre = lower; pre < thres + 1; pre++){     //write training data for time vector
                    boolean sign = false;
                    for(int i1 = 0; i1 < syscallTimeVector.size(); i1++){
                        writetime = wholesyscallLists.get(i1).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                            sign = true;
                            out1.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            out5.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            eEUAppVec = syscallTimeVector.get(i1);
                         
                            for(int j= 0 ; j < eEUAppVec.size()-1; j++){
                                out1.write(eEUAppVec.get(j)/1000000 + ",");
                                out5.write(eEUAppVec.get(j)/1000000 + ",");
                            }
                            out1.write(eEUAppVec.get(eEUAppVec.size()-1)/1000000 + "\n");
                            out5.write(eEUAppVec.get(eEUAppVec.size()-1)/1000000 + "\n");
                            break;
                        }   
                     
                    }
                     
                    if (sign == false){
                        out1.write(mintime/1000000 + pre*(gap/1000000) +",");    
                        out5.write(mintime/1000000 + pre*(gap/1000000) +",");
                        for(int j = 0 ; j < syscallTimeVector.get(0).size()-1; j++){
                            out1.write("0" + ",");
                            out5.write("0" + ",");
                        }
                        out1.write("0" + "\n");
                        out5.write("0" + "\n");
                    }
                }
                 
                for (int pre = thres + 1; pre < upper + 1; pre++){   //write test data for time vector
                    boolean sign = false;
                    for(int i1 = 0; i1 < syscallTimeVector.size(); i1++){
                        writetime = wholesyscallLists.get(i1).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                            sign = true;
                            out5.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            eEUAppVec = syscallTimeVector.get(i1);
                         
                            for(int j= 0 ; j < eEUAppVec.size()-1; j++){
                                out5.write(eEUAppVec.get(j)/1000000 + ",");
                            }
                            out5.write(eEUAppVec.get(eEUAppVec.size()-1)/1000000 + "\n");
                            break;
                        }   
                     
                    }
                     
                    if (sign == false){
                        out5.write(mintime/1000000 + pre*(gap/1000000) +",");    
                        for(int j = 0 ; j < syscallTimeVector.get(0).size()-1; j++){
                            out5.write("0" + ",");
                        }
                        out5.write("0" + "\n");
                    }
                }
                 
                 
                List<Integer> eEUAppVec1;
                for (int pre = lower; pre < thres + 1; pre++){   //write training data for appearance vector
                    boolean sign = false;
                    for(int i2 = 0; i2 < appearanceVector.size(); i2++){
                        writetime = wholesyscallLists.get(i2).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                            sign = true;
                            out2.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            out6.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            eEUAppVec1 = appearanceVector.get(i2);
                             
                            for(int j = 0 ; j < eEUAppVec1.size()-1; j++){
                                out2.write(eEUAppVec1.get(j) + ",");
                                out6.write(eEUAppVec1.get(j) + ",");
                            }
                            out2.write(eEUAppVec1.get(eEUAppVec1.size()-1) + "\n");
                            out6.write(eEUAppVec1.get(eEUAppVec1.size()-1) + "\n");
                            break;
                            }                                       
                    }
                     
                    if (sign == false){
                        out2.write(mintime/1000000 + pre*(gap/1000000) +",");
                        out6.write(mintime/1000000 + pre*(gap/1000000) +",");
                        for(int j = 0 ; j < appearanceVector.get(0).size()-1; j++){
                            out2.write("0" + ",");
                            out6.write("0" + ",");
                        }
                        out2.write("0" + "\n");
                        out6.write("0" + "\n");
                    }
                }
                 
                 
                for (int pre = thres + 1; pre < upper + 1; pre++){    //write test data for appearance vector
                    boolean sign = false;
                    for(int i2 = 0; i2 < appearanceVector.size(); i2++){
                        writetime = wholesyscallLists.get(i2).get(0).getTimeStamp();
                        firstone = (int) ((writetime - mintime)/gap);
                         
                        if (pre == firstone){
                            sign = true;
                            out6.write(mintime/1000000 + firstone*(gap/1000000) +",");
                            eEUAppVec1 = appearanceVector.get(i2);
                             
                            for(int j = 0 ; j < eEUAppVec1.size()-1; j++)
                                out6.write(eEUAppVec1.get(j) + ",");
                            out6.write(eEUAppVec1.get(eEUAppVec1.size()-1) + "\n");
                            break;
                            }                                       
                    }
                     
                    if (sign == false){
                        out6.write(mintime/1000000 + pre*(gap/1000000) +",");    
                        for(int j = 0 ; j < appearanceVector.get(0).size()-1; j++){
                            out6.write("0" + ",");
                        }
                        out6.write("0" + "\n");
                    }
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
					
					out4.flush();
					if(file4!=null)
						file4.close();
					if(out4!=null)
						out4.close();
					out5.flush();
					if(file5!=null)
						file5.close();
					if(out5!=null)
						out5.close();
					out6.flush();
					if(file6!=null)
						file6.close();
					if(out6!=null)
						out6.close();
					}		
		
		
		
		double end_t = System.currentTimeMillis();
		logger.info("Finish getting appearance, execution time and frequency vector: " + (end_t - start_t)/1000L + " seconds.");
		
		
		appearanceVector.clear(); //clear appearance vector
		syscallTimeVector.clear(); //clear syscall time vector
		syscallCountVector.clear(); //clear syscall count vector
		
		return null;
		
	}
	
	public static void getMemory(){
		try{
			Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			StringBuilder sb = new StringBuilder();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			sb.append("free memory: " + format.format(freeMemory / 1024 / 1024) + "MB, ");
			sb.append("allocated memory: " + format.format(allocatedMemory / 1024 / 1024) + "MB, ");
			sb.append("max memory: " + format.format(maxMemory / 1024 / 1024) + "MB, ");
			sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "MB, ");
			String str = sb.toString();
			logger.info(str);
		} catch (Exception e){
			logger.warning(e.toString());
		}
	}
	
	
	
	
	public static void getSegmentedVectors(List<List<SystemCall>> syscallLists,
			List<List<Integer>> MAVector, 
			List<List<Long>> TimeVector, 
			List<List<Long>> CountsVector){
		
		List<Map<String, Long>> allSegmentedRawCounts = new ArrayList<Map<String, Long>>();
		List<Map<String, Long>> allSegmentedRawTimes = new ArrayList<Map<String, Long>>();
		List<List<String>> sysCalls = new ArrayList<List<String>>();
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
		//added by hao to shown the unique systemcall type
		System.out.println("================Unique system call shown below=====================");
		for(int i = 0; i < uElems.size(); i++){
			System.out.print(uElems.get(i)+",");
		}
		System.out.println("");
		
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
		Map<Integer, Integer> threads = new HashMap<Integer, Integer>();
		int totalUnits = 0;
		try {
			int position = 0;
			for(List<SystemCall> syscallList : syscallLists){
				if (syscallList.size() == 0)
					continue;
				Map<String, Long> segmentedRawCount = new HashMap<String, Long>();
				Map<String, Long> segmentedRawTime = new HashMap<String, Long>();
				List<String> cSyscalls = new ArrayList<String>();
					int tid = syscallList.get(0).getTid();
					threads.put(tid, 1);
					totalUnits++;
					//Add the file position to the list of file positions for this thread
					if(!executionUnitList.containsKey(tid)){
						List<Integer> toAdd = new ArrayList<Integer>();
						toAdd.add(position);
						executionUnitList.put(tid, toAdd);
						//Store this unit's position in the file position vector
						unitPositionMap.put(position, 0);
					}
					else
					{
						List<Integer> cVec = executionUnitList.get(tid);
						cVec.add(position);
						executionUnitList.put(tid, cVec);
						//Store this unit's position in the file position vector
						unitPositionMap.put(position, cVec.size()-1);
					}
					tuIDs.add(position);
					threadIDMap.put(position, tid);
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
					position += 1;
			}
		}
		catch (Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.severe(stacktrace);
		}
		int numThreads = 0;
		numThreads = threads.size();
		logger.info("Num Threads: " + numThreads + ", Num Units: " + totalUnits);
	}

	
    
	/*
	 * Put everything in the initial cluster
	 */
	public static TUCluster initialCluster(List<List<Integer>> appearanceVector){
		TUCluster cluster = new TUCluster();
		cluster.diameter = DIAMETER_THRESHOLD + 1;
		int i = 0;
		for(List<Integer> list : appearanceVector){
			cluster.TUIds.add(i);
			cluster.medoidIdentifier = i;
			TUIdMap.put(i, list);
			i++;
		}
		rdaemonClusterVector.add(cluster);//rdaemonClusterVecor has initial cluster first
		return cluster;
	}
	
}

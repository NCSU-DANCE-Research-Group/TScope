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



public class sysprocessing {
	
	
	private static Logger logger = Logger.getLogger(sysprocessing.class.getName()); 
//	private static List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();//Appearance List, which stores unique syscall types
//	static List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
//	static List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
//	static Map<Integer, String> systemcallMap = new HashMap<Integer, String>();

    /*
     * include doHierarchicalClustering, identifyAbnormalExecutionUnit and outputFunctions
     */
	//preprocessing output, "java". "hadoop,hdfs", ""
	public static void doAnalysis(List<List<SystemCall>> wholesyscallLists, long gap) 
            throws IOException {
        if (wholesyscallLists.size() == 0){
            logger.warning("The input is empty, cannot do function localization analysis.");
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
         
        System.out.println("maxtime:" + maxtime);
        System.out.println("mintime:" + mintime);   
        int totalinterval  = (int) ((maxtime - mintime)/gap) + 1;
        System.out.println("total:" + totalinterval);
		
		logger.info("Start getting appearance, execution time and frequency vector.");
		double start_t = System.currentTimeMillis();
		
		List<List<Integer>> appearanceVector = new ArrayList<List<Integer>>();
		List<List<Long>> syscallTimeVector = new ArrayList<List<Long>>();//System call execution times
		List<List<Long>> syscallCountVector = new ArrayList<List<Long>>();//System call counts
//	    Map<Integer, String> systemcallMap = new HashMap<Integer, String>();
			
		getSegmentedVectors(wholesyscallLists, appearanceVector, syscallTimeVector, syscallCountVector);
		
		
		// printing out vectors. frequecy, time, appearance
				FileWriter file0 = null;
				BufferedWriter out0 = null;
				FileWriter file1 = null;
				BufferedWriter out1 = null;
				FileWriter file2 = null;
				BufferedWriter out2 = null;
				
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
					
					file0 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" + "flume_freqvector.csv");
					out0 = new BufferedWriter(file0);
						
					file4 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" 
					+ "flume_freqvector_test.csv");
					out4 = new BufferedWriter(file4);
						
					file1 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" + "flume_timevector.csv");
					out1 = new BufferedWriter(file1);
					
					file5 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" 
					+ "flume_timevector_test.csv");
					out5 = new BufferedWriter(file5);
					
					file2 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" + "flume_appvector.csv");
					out2 = new BufferedWriter(file2);
					
					file6 = new FileWriter("/home/jhe16/Downloads/syslog_processing/" 
							+ "flume_appvector_test.csv");
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

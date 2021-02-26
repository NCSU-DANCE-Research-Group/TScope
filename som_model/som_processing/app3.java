package som_processing;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.util.Date;

public class app3 {
	public static void main( String[] args ) throws IOException
    {
		long gap = Long.parseLong(args[3]);
	//	simpliedpre prepro = new simpliedpre();
//		preprocessing prepro = new preprocessing();
//		List<List<SystemCall>> syscall = prepro.startPreprocessing("java", "hadoop", 
//				"/home/jhe16/Downloads/PerfScopeAEU/raw-syscall/syscall_hdfs10223.log", 100000000);
//		System.out.println("\n" + " Function processing Done");
		
//		sysprocessing vector = new sysprocessing();
//		vector.doAnalysis(syscall, 100000000);
//		
//		System.out.println("\n" + " Analysis Done");
                
                double start_t = System.currentTimeMillis();
		
		FuncLocPreProcessingNoFilterNew prepro = new FuncLocPreProcessingNoFilterNew();
//		List<List<SystemCall>> syscall = prepro.startPreprocessing("java", "hadoop", 
//				"/home/jhe16/Downloads/PerfScopeAEU/raw-syscall/syscall_Cass7886-2.1.log", 1000000000);
		List<List<SystemCall>> syscall = prepro.startPreprocessing(args[0], args[1], args[2], gap);
		filterTimeoutSyscalls filterSyscalls2 = new filterTimeoutSyscalls();
		List<List<SystemCall>> syscallLists = filterSyscalls2.filterTOSysOnEU(syscall);
                
//                List<Integer> fp = new ArrayList<Integer>();
//                for (int id = 161; id < 321; id++){
//                    fp.add(id);
//                }
//                fp.add(2);
//                fp.add(67);
//                fp.add(88);
//                fp.add(148);
//                fp.add(114);
//                fp.add(106);
//                fp.add(109);
//                fp.add(112);
//                fp.add(115);
//                fp.add(116);
//                fp.add(117);
//                fp.add(118);
//                fp.add(110);
//                fp.add(121);
//                fp.add(124);
//                fp.add(125);
//                fp.add(126);

                
//                for (int idx = 0; idx < fp.size(); idx++){
//                    int num = fp.get(idx);
//                    System.out.println("timeout argument syscall of " + num + ": ");
//                    filterTimeoutArgSyscalls2 timeoutarg = new filterTimeoutArgSyscalls2();
//                    timeoutarg.filterTOSysOnEU(syscallLists.get(num));
//                }

//                int cnt = 0;
//                for (int idx = 0; idx < fp.size(); idx++){
//                     int num = fp.get(idx);
//                     System.out.println("\n size of id " + num + " : " + syscallLists.get(num).size());
//                     cnt = cnt + syscallLists.get(num).size();                
//                } 
//                System.out.println("\n" + "####### Number of FP syscall: "+ cnt);

		System.out.println("\n" + " Function processing Done");
		
		sysprocessing2 vector = new sysprocessing2();
//		vector.doAnalysis(syscall, args[0], args[1], null, gap, args[4]);
		vector.doAnalysis(syscallLists, args[0], args[1], null, gap, args[4]);
		
                double end_t = System.currentTimeMillis();
                System.out.println("The total elapsed time: " + (end_t - start_t)/1000L + " seconds.");

		System.out.println("\n" + " Analysis Done");

    }

}

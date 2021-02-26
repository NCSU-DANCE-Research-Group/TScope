package som_processing;

import java.io.IOException;
import java.util.List;

public class app {
	public static void main( String[] args ) throws IOException
    {
		FuncLocPreProcessing PreProcessing = new FuncLocPreProcessing();
		List<List<List<SystemCall>>> syscall = PreProcessing.startPreprocessing("java", "hadoop", "/home/jhe16/Downloads/PerfScopeAEU/raw-syscall/syscall_11252.log");
//		PreProcessing.startPreprocessing("java", "phoenix", "/home/jhe16/Downloads/lttng/data/syscall_phoenix2496.log");
		System.out.println("\n" + " Function processing Done");
		
//		for(int i = 0; i < syscall.size(); i++){
//			processing vector = new processing();
//		    vector.doAnalysis(syscall.get(i));
//		}
		
		processing vector = new processing();
		vector.doAnalysis(syscall);
		
		System.out.println("\n" + " Analysis Done");
    }

}

package som_processing;

import java.io.IOException;
import java.util.List;


public class app2 {
	public static void main( String[] args ) throws IOException
    {
		FunLocPreProcessing2 PreProcessing = new FunLocPreProcessing2();
		List<SystemCall> syscall = PreProcessing.startPreprocessing("java", "phoenix", "/Users/jzhe/Documents/trace/syscall_sys-phoenix-new.log");
//		System.out.println(syscall);
		processing2 vector = new processing2();
		vector.doAnalysis(syscall);
    }

}

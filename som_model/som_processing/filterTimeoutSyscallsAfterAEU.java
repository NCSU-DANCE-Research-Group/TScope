package som_processing;

import java.util.ArrayList;
import java.util.List;

public class filterTimeoutSyscallsAfterAEU {

	boolean filterTOSysOnEU(List<SystemCall> EU){
	//	List<List<SystemCall>> filteredEUs = new ArrayList<List<SystemCall>>();
	//	for(List<SystemCall> EU : EUs){
	//		List<SystemCall> filteredEU = new ArrayList<SystemCall>();
			for(SystemCall syscall : EU){
				if(fileterSyscallsHasTOArgs(syscall.getName())){
	//				filteredEU.add(syscall);
					return true;
				}
			}
			return false;
//			if(filteredEU != null && filteredEU.size() > 0){
//				filteredEUs.add(filteredEU);
//			}
	//	}
	//	return filteredEUs;
	}
	
	private boolean fileterSyscallsHasTOArgs(String line){
		if(line.contains("_newselect") ||
				line.contains("_alarm") ||
				line.contains("_clock_nanosleep") ||
				line.contains("_epoll_pwait") ||
				line.contains("_epoll_wait") ||
				line.contains("_futex") ||
				line.contains("_io_getevents") ||
				line.contains("_mq_timedreceive") ||
				line.contains("_mq_timedsend") ||
				line.contains("_nanosleep") ||
				line.contains("_poll") ||
				line.contains("_ppoll") ||
				line.contains("_select") ||
				line.contains("_recvmmsg") ||
				line.contains("_rt_sigtimedwait") ||
				line.contains("_pselect") ||
				line.contains("_semtimedop") ||
				line.contains("_timerfd_settime") ||
				line.contains("_timer_settime"))
			return true;
		return false;
	}
}


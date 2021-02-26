package som_processing;

import java.util.ArrayList;
import java.util.List;

public class filterTimeoutSyscalls {

	List<List<SystemCall>> filterTOSysOnEU(List<List<SystemCall>> EUs){
		List<List<SystemCall>> filteredEUs = new ArrayList<List<SystemCall>>();
		for(List<SystemCall> EU : EUs){
			List<SystemCall> filteredEU = new ArrayList<SystemCall>();
			for(SystemCall syscall : EU){
                        //        if(testcasefileterSyscalls(syscall.getName())){
			//	if(filterTOSyscalls(syscall.getName())){
                                if(fileterSyscallsHasTOArgs(syscall.getName())){
					filteredEU.add(syscall);
				}
			}
			if(filteredEU != null && filteredEU.size() > 0){
				filteredEUs.add(filteredEU);
			}
		}
		return filteredEUs;
	}
	
	//116
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
	
	//64
	private boolean filterTOSyscallsSimplify(String line){
		if(line.contains("_clock_nanosleep") ||
				line.contains("_connect") ||
				line.contains("_epoll_pwait") ||
				line.contains("_epoll_wait") ||
				line.contains("_fdatasync") ||
				line.contains("_flock") ||
				line.contains("_fsync") ||
				line.contains("_futex") ||
				line.contains("_io_getevents") ||
				line.contains("_mlock") ||
				line.contains("_mlock2") ||
				line.contains("_mlockall") ||
				line.contains("_mq_notify") ||
				line.contains("_mq_timedreceive") ||
				line.contains("_mq_timedsend") ||
				line.contains("_msgrcv") ||
				line.contains("_msgsnd") ||
				line.contains("_msync") ||
				line.contains("_munlock") ||
				line.contains("_munlockall") ||
				line.contains("_nanosleep") ||
				line.contains("_open") ||
				line.contains("_openat") ||
				line.contains("_pause") ||
				line.contains("_poll") ||
				line.contains("_ppoll") ||
				line.contains("_read") ||
				line.contains("_readv") ||
				line.contains("_recv") ||
				line.contains("_recvfrom") ||
				line.contains("_recvmsg") ||
				line.contains("_recvmmsg") ||
				line.contains("_rt_sigaction") ||
				line.contains("_rt_sigpending") ||
				line.contains("_rt_sigprocmask") ||
				line.contains("_rt_sigsuspend") ||
				line.contains("_rt_sigtimedwait") ||
				line.contains("_select") ||
				line.contains("_semget") ||
				line.contains("_semop") ||
				line.contains("_semtimedop") ||
				line.contains("_send") ||
				line.contains("_sendfile") ||
				line.contains("_sendfile64") ||
				line.contains("_sendmmsg") ||
				line.contains("_sendmsg") ||
				line.contains("_sendto") ||
				line.contains("_setitimer") ||
				line.contains("_sigaction") ||
				line.contains("_sigprocmask") ||
				line.contains("_sigsuspend") ||
				line.contains("_socket") ||
				line.contains("_socketcall") ||
				line.contains("_socketpair") ||
				line.contains("_sync") ||
				line.contains("_sync_file_range") ||
				line.contains("_sync_file_range2") ||
				line.contains("_syncfs") ||
				line.contains("_timer_settime") ||
				line.contains("_timerfd_settime") ||
				line.contains("_waitid") ||
				line.contains("_waitpid") ||
				line.contains("_write") ||
				line.contains("_writev"))
			return true;
		return false;
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
                                line.contains("_timer_settime")
				)
			return true;
		return false;
	}

        private boolean testcasefileterSyscalls(String line){
		if(line.contains("_ioctl") || line.contains("_readlink") 
				|| line.contains("_read") || line.contains("_fcntl")
				|| line.contains("_execve") || line.contains("_getdents")
				|| line.contains("_newlstat") || line.contains("_fchdir")
				|| line.contains("_set_robust_list") || line.contains("_socket")
				|| line.contains("_getcwd") || line.contains("_sched_getaffinity")
				|| line.contains("_close") || line.contains("_setrlimit")
				|| line.contains("_set_tid_address") || line.contains("_madvise")
				|| line.contains("_exit_group") || line.contains("_recvmsg")
				|| line.contains("_sendmmsg") || line.contains("_ftruncate")
				|| line.contains("_getrlimit") || line.contains("_rt_sigaction")
				|| line.contains("_mkdir") || line.contains("_setsockopt")
				|| line.contains("_recvfrom") || line.contains("_lseek")
				|| line.contains("_mprotect") || line.contains("_unknown")
				|| line.contains("_listen") || line.contains("_write")
				|| line.contains("_gettid") || line.contains("_clone")
				|| line.contains("_access") || line.contains("_geteuid")
				|| line.contains("_newfstat") || line.contains("_sendto")
				|| line.contains("_unlink") || line.contains("_socketpair")
		        || line.contains("_bind") || line.contains("_mmap")
	         	|| line.contains("_exit") || line.contains("_poll")
	         	|| line.contains("_futex") || line.contains("_brk")
	        	|| line.contains("_sysinfo") || line.contains("_connect")
	         	|| line.contains("_getsockname") || line.contains("_dup2")
         		|| line.contains("_newuname") || line.contains("_munmap")
         		|| line.contains("_clock_getres") || line.contains("_shutdown")
         		|| line.contains("_newstat") || line.contains("_getsockopt")
           		|| line.contains("_rt_sigprocmask") || line.contains("_sched_yield")
	        	|| line.contains("_open") || line.contains("_send")             
                        || line.contains("_getuid") //59
           		|| line.contains("_msgctl") || line.contains("_msgsnd")
          		|| line.contains("_mq_open") || line.contains("_mq_timedsend")
         		|| line.contains("_recv") || line.contains("_msgget")
          		|| line.contains("_socketcall") || line.contains("_recvmmsg")
          		|| line.contains("_sendfile64") || line.contains("_sendfile")
        		|| line.contains("_sendmsg") || line.contains("_msgrcv")
          		|| line.contains("_mq_timedreceive") || line.contains("_pipe2")
           		|| line.contains("_ipc") || line.contains("_pipe")  
		        || line.contains("_mq_notify") || line.contains("_syncfs")    //network
  	        	|| line.contains("_tkill") || line.contains("_sigaltstack")
 	        	|| line.contains("_rt_sigpending") || line.contains("_wait4")
   	         	|| line.contains("_signalfd4") || line.contains("_sigaction")
        		|| line.contains("_rt_sigqueueinfo") || line.contains("_msync")
	        	|| line.contains("_rt_sigreturn") || line.contains("_sigpending")
        		|| line.contains("_fdatasync") || line.contains("_pause")
        		|| line.contains("_sigreturn") || line.contains("_rt_sigtimedwait")  
        		|| line.contains("_fsync") || line.contains("_waitid")
 	        	|| line.contains("_vfork") || line.contains("_waitpid")
   	         	|| line.contains("_tgkill") || line.contains("_rt_sigsuspend")
        		|| line.contains("_sigprocmask") || line.contains("_mlockall")
	        	|| line.contains("_munlock") || line.contains("_semctl")
        		|| line.contains("_semget") || line.contains("_munlockall")
        		|| line.contains("_semop") || line.contains("_mlock")  
        		|| line.contains("_sigsuspend") || line.contains("_mlock2")
 	        	|| line.contains("_sync_file_range2") || line.contains("_sync_file_range")
   	         	|| line.contains("_flock") || line.contains("_sync")
        		|| line.contains("_signalfd") || line.contains("_sched_rr_get_interval")
	        	|| line.contains("_semtimedop") || line.contains("_newselect")               //sync
        		|| line.contains("_alarm") || line.contains("_clock_nanosleep")
        		|| line.contains("_epoll_pwait") || line.contains("_epoll_wait")  
        		|| line.contains("_io_getevents") || line.contains("_nanosleep")
 	        	|| line.contains("_ppoll") || line.contains("_select")
   	         	|| line.contains("_pselect") || line.contains("_timerfd_settime")
        		|| line.contains("_timer_settime") )                                   //argument
			return true;
		return false;
	}

        private boolean interfilterSyscalls(String line){
		if(line.contains("_readlink") || line.contains("_read") 
				|| line.contains("_fcntl") || line.contains("_execve") 
				|| line.contains("_newlstat") || line.contains("_fchdir")
				|| line.contains("_set_robust_list") || line.contains("_socket")
				|| line.contains("_getcwd") || line.contains("_sched_getaffinity")
				|| line.contains("_close") || line.contains("_setrlimit")
			 	|| line.contains("_set_tid_address") || line.contains("_ftruncate")
				|| line.contains("_getrlimit") || line.contains("_rt_sigaction")
				|| line.contains("_mkdir") || line.contains("_lseek")
				|| line.contains("_mprotect") || line.contains("_unknown")
                || line.contains("_write") || line.contains("_gettid") 
                || line.contains("_clone") || line.contains("_access") 
                || line.contains("_geteuid") || line.contains("_newfstat")
                || line.contains("_mmap") || line.contains("_futex") 
                || line.contains("_brk") || line.contains("_sysinfo") 
                || line.contains("_connect") || line.contains("_newuname") 
                || line.contains("_munmap") || line.contains("_clock_getres")
         		|| line.contains("_newstat") || line.contains("_rt_sigprocmask") 
         		|| line.contains("_sched_yield") || line.contains("_open")   
         		|| line.contains("_getdents") || line.contains("_getuid")  //40
         		|| line.contains("_send")              
           		|| line.contains("_msgctl") || line.contains("_msgsnd")
          		|| line.contains("_mq_open") || line.contains("_mq_timedsend")
         		|| line.contains("_recv") || line.contains("_msgget")
          		|| line.contains("_socketcall") || line.contains("_recvmmsg")
          		|| line.contains("_sendfile64") || line.contains("_sendfile")
        		|| line.contains("_sendmsg") || line.contains("_msgrcv")
        		|| line.contains("_ioctl") || line.contains("_recvmsg")
        		|| line.contains("_sendmmsg") || line.contains("_setsockopt")
        		|| line.contains("_recvfrom") || line.contains("_sendto")
        		|| line.contains("_socketpair") || line.contains("_getsockname")
        		|| line.contains("_getsockopt")
          		|| line.contains("_mq_timedreceive") || line.contains("_pipe2")
           		|| line.contains("_ipc") || line.contains("_pipe")  
		        || line.contains("_mq_notify") || line.contains("_syncfs")    //network
  	        	|| line.contains("_tkill") || line.contains("_sigaltstack")
 	        	|| line.contains("_rt_sigpending") || line.contains("_wait4")
   	         	|| line.contains("_signalfd4") || line.contains("_sigaction")
        		|| line.contains("_rt_sigqueueinfo") || line.contains("_msync")
	        	|| line.contains("_rt_sigreturn") || line.contains("_sigpending")
        		|| line.contains("_fdatasync") || line.contains("_pause")
        		|| line.contains("_sigreturn") || line.contains("_rt_sigtimedwait")  
        		|| line.contains("_fsync") || line.contains("_waitid")
 	        	|| line.contains("_vfork") || line.contains("_waitpid")
   	         	|| line.contains("_tgkill") || line.contains("_rt_sigsuspend")
        		|| line.contains("_sigprocmask") || line.contains("_mlockall")
	        	|| line.contains("_munlock") || line.contains("_semctl")
        		|| line.contains("_semget") || line.contains("_munlockall")
        		|| line.contains("_semop") || line.contains("_mlock")  
        		|| line.contains("_sigsuspend") || line.contains("_mlock2")
 	        	|| line.contains("_sync_file_range2") || line.contains("_sync_file_range")
   	         	|| line.contains("_flock") || line.contains("_sync")
        		|| line.contains("_signalfd") || line.contains("_sched_rr_get_interval")
	        	|| line.contains("_semtimedop") || line.contains("_newselect")               //sync
        		|| line.contains("_alarm") || line.contains("_clock_nanosleep")
        		|| line.contains("_epoll_pwait") || line.contains("_epoll_wait")  
        		|| line.contains("_io_getevents") || line.contains("_nanosleep")
 	        	|| line.contains("_ppoll") || line.contains("_select")
 	        	|| line.contains("_poll")
   	         	|| line.contains("_pselect") || line.contains("_timerfd_settime")
        		|| line.contains("_timer_settime") )                                   //argument
			return true;
		return false;
	}

}


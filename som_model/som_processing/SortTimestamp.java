package som_processing;
 
import java.util.Arrays;
import java.util.List;
 
public class SortTimestamp {
    public static void quickSort(List<SystemCall> syscalls) {  
        _quickSort(syscalls, 0, syscalls.size() - 1);  
//      System.out.println(Arrays.toString(array) + " quickSort");  
    }  
       
       
    private static int getMiddle(List<SystemCall> syscalls, int low, int high) {  
        SystemCall tmp = syscalls.get(low);    //数组的第一个作为中轴  
        while (low < high) {  
            while (low < high && syscalls.get(high).getTimeStamp() >= tmp.getTimeStamp()) {  
                high--;  
            }  
       
            syscalls.set(low, syscalls.get(high));
    //        list[low] = list[high];   //比中轴小的记录移到低端  
            while (low < high && syscalls.get(low).getTimeStamp() <= tmp.getTimeStamp()) {  
                low++;  
            }  
       
            syscalls.set(high, syscalls.get(low));
//          list[high] = list[low];   //比中轴大的记录移到高端  
        }
        syscalls.set(low, tmp);
//      list[low] = tmp;              //中轴记录到尾  
        return low;                  //返回中轴的位置  
    }  
       
       
    private static void _quickSort(List<SystemCall> syscalls, int low, int high) {  
        if (low < high) {  
            int middle = getMiddle(syscalls, low, high);  //将list数组进行一分为二  
            _quickSort(syscalls, low, middle - 1);      //对低字表进行递归排序  
            _quickSort(syscalls, middle + 1, high);      //对高字表进行递归排序  
        }  
    }  
}

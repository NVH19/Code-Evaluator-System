
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class tinh_tong {
    public static void main(String[] args) throws FileNotFoundException{
        File src = new File("DATA.in");
        Scanner sc = new Scanner(src);
        long sum = 0;
        while(sc.hasNextLine()){
            String a = sc.next();
            try{
                sum += Integer.parseInt(a);
            }
            catch(Exception x){
            }
        }
        System.out.println(sum);
        sc.close();
    }
}



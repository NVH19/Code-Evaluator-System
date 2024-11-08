
import java.util.Scanner;

public class HinhChuNhat {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int m = sc.nextInt();
        if( n <= 0 || m<=0){
            System.out.println(0);
        }
        else{
            System.out.println((n+m)*2 +" " + n*m);
        }

    }
}


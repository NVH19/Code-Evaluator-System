import java.util.Scanner;

public class J01001 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String data = sc.nextLine();
        String tmp[] = data.split(" ");
        int a = Integer.parseInt(tmp[0]);
        int b = Integer.parseInt(tmp[1]);
        if(a<=0 || b<=0) {
            System.out.println("0");
        }
        else {
            int cv = (a+b)*2;
            int dt = a*b;
            System.out.println(cv + " " + dt);
        }
    }
}

//

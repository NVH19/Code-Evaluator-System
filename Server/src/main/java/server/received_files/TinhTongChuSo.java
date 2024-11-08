import java.util.Scanner;

public class TinhTongChuSo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(tinhTongChuSo(n));
        scanner.close();
    }

    // Hàm tính tổng các chữ số của N
    public static int tinhTongChuSo(int n) {
        int sum = 0;
        
        // Tách từng chữ số và cộng vào tổng
        while (n > 0) {
            sum += n % 10; // Lấy chữ số cuối của n
            n /= 10;       // Bỏ chữ số cuối của n
        }
        
        return sum;
    }
}

//

#include <iostream>
using namespace std;

int main() {
   
        long long N; // Sử dụng long long để tránh tràn số cho N lớn
        cin >> N;

        // Tính tổng S = N * (N + 1) / 2
        long long S = N * (N + 1) / 2;

        // In kết quả
        cout << S << endl;

}



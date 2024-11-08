#include <iostream>
using namespace std;

int main() {
    int a ,b;
    cin>>a>>b;
    if (a<=0 || b<=0)
    {
        cout<<"0";
    }
    else
    {
        int cv = (a+b)*2;
        int dt = a*b;
        cout<<cv<<" "<<dt<<endl;
    }
    return 0;
}
//

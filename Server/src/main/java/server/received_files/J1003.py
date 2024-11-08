def sum_of_digits(n):
    return sum(int(digit) for digit in str(n))

# Ví dụ
n = int(input())
print(sum_of_digits(n))



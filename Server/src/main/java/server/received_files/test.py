# Nhập vào hai số nguyên cách nhau một khoảng trống
input_data = input()

# Tách các giá trị và chuyển đổi thành số nguyên
length, width = map(int, input_data.split())

# Kiểm tra tính hợp lệ của chiều dài và chiều rộng
if length <= 0 or width <= 0:
    print(0)
else:
    # Tính chu vi và diện tích
    perimeter = 2 * (length + width)
    area = length * width
    # In kết quả ra màn hình
    print(perimeter, area)



# mid-project-404344380
mid-project-404344380 created by GitHub Classroom

Đề tài: Hệ thống Thi Code Online

1. Mô tả chung

Hệ thống thi code online là một nền tảng cho phép người dùng tham gia các cuộc thi lập trình trực tuyến. Hệ thống sẽ cung cấp các bài toán lập trình, người dùng gửi mã nguồn và hệ thống sẽ tự động biên dịch và chấm điểm dựa trên các bộ test case được cung cấp. Hệ thống cũng hỗ trợ tính năng xếp hạng, thống kê kết quả, và quản lý người dùng.

Các yêu cầu chính của hệ thống:
- server-client architecture: Hệ thống được xây dựng trên mô hình client-server, nơi client tương tác với người dùng và server quản lý dữ liệu, chấm bài, xử lý yêu cầu từ nhiều client cùng lúc.
- Mã hóa thông tin: Tất cả các dữ liệu trao đổi giữa client và server phải được mã hóa, đảm bảo tính bảo mật của thông tin người dùng và mã nguồn.
- Phân quyền và quản lý người dùng: Hệ thống có khả năng quản lý người dùng, bao gồm quyền tạo bài thi, tham gia thi, cũng như chặn hoặc giới hạn hoạt động người dùng.


2. Chức năng phía client

2.1. Đăng nhập tài khoản
- Người dùng có thể đăng nhập và truy cập các tính năng của hệ thống.

2.2. Tham gia các kỳ thi lập trình
- Người dùng có thể chọn các kỳ thi hiện có từ danh sách.
- Giao diện người dùng cung cấp bài toán lập trình và môi trường để nhập mã nguồn.
- Sau khi nộp bài, hệ thống sẽ thực hiện chấm điểm tự động và gửi kết quả về client.

2.3. Chấm điểm tự động
- Sau khi người dùng nộp mã nguồn, client sẽ gửi mã lên server để biên dịch và chạy qua các test case.
- Kết quả được hiển thị cho người dùng, bao gồm:
  - Điểm số đạt được.
  - Kết quả từng test case (pass/fail).

2.4. Xem xếp hạng
- Sau khi nộp bài, người dùng có thể xem xếp hạng của mình so với các thí sinh khác, dựa trên điểm số và thời gian hoàn thành.
- Hệ thống cũng cung cấp bảng tổng hợp thống kê kết quả thi, bao gồm:
  - Số lượng thí sinh tham gia.
  - Điểm số trung bình.
  - Điểm số cao nhất.


3. Chức năng phía server

3.1. Xử lý và quản lý thi lập trình
- Chấm bài tự động: server sẽ biên dịch và chạy mã nguồn trong môi trường sandbox để đảm bảo an toàn.
- Sandbox: Sử dụng công nghệ Docker để tạo môi trường an toàn cho việc biên dịch và thực thi mã.
- Mã nguồn của người thi được kiểm tra qua các test case có sẵn (test case công khai và test case ẩn) để chấm điểm.

3.2. Quản lý người dùng và cuộc thi
- Quản lý tài khoản người dùng: tạo, chỉnh sửa, hoặc khóa người dùng (block).
- Quản lý cuộc thi: tạo, chỉnh sửa, và kết thúc cuộc thi.
- Thực hiện các truy vấn về dữ liệu như số lượng thí sinh, điểm thi cao nhất, và thống kê chi tiết từng kỳ thi.


Công nghệ:
- Ngôn ngữ lập trình: Java cho server. React hoặc Vue.js cho client.
- Giao thức truyền tải: WebSocket hoặc HTTP/HTTPS.
- Biên dịch mã: Sử dụng Docker để thực thi mã nguồn trong môi trường ảo hóa an toàn.



Thành viên:

B21DCCN404-Nguyễn Văn Huân

B21DCCN344-Nguyễn Tiến Hiệp

B21DCCN380-Nguyễn Văn Hòa

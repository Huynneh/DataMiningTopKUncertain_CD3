# DataMiningTopKUncertain_CD3

# Chuyên đề nghiên cứu 3
Algorithms for mining Top-K frequent itemsets from uncertain databases
Khai thác Top-K Frequent Itemsets trên Cơ sở Dữ liệu Không Chắc Chắn

## Học viên: Tô Ngọc Huyền - 242805005

## 1. Giới thiệu

Dự án này cài đặt và so sánh nhiều thuật toán khai thác Top-K Frequent Itemsets trên cơ sở dữ liệu giao dịch không chắc chắn (Uncertain Database).

Hệ thống hỗ trợ:
- Tự động sinh dataset xác suất từ dữ liệu giao dịch gốc
- Chạy nhiều thuật toán khai thác Top-K
- Đo thời gian thực thi và bộ nhớ sử dụng
- Xuất kết quả chi tiết ra file và bảng tổng hợp CSV

## 2. Các thuật toán được cài đặt

Dự án bao gồm các thuật toán sau:
- U-Apriori
- U-FP-Growth
- U-HMine
- HybridTopKMiner (thuật toán lai, lựa chọn chiến lược dựa trên mật độ dữ liệu)

## 3. Cấu trúc thư mục dự án

DataMiningTopKUncertain_CD3/
│
├── datasets/
│   ├── origin/          # Dataset giao dịch gốc (.txt)
│   └── probability/     # Dataset xác suất được sinh tự động
│
├── outputs/             # Kết quả khai thác và bảng tổng hợp
│
├── src/
│   ├── main/            # Chương trình chính
│   ├── dal/             # Tầng xử lý dữ liệu
│   ├── bll/             # Tầng thuật toán
│   └── model/           # Các lớp mô hình dữ liệu
│
├── bin/                 # File .class sau khi biên dịch
└── README.md

## 4. Yêu cầu hệ thống

- Hệ điều hành: Windows / Linux / macOS
- Java Development Kit (JDK): JDK 8 trở lên (khuyến nghị JDK 11+)
- Tải JDK tại: https://www.oracle.com/java/technologies/javase-downloads.html

## 5. Cài đặt dự án

Mở Command Prompt với quyền admin và đi tới thư mục chứa dự án

```bash
  cd [thư-mục-chứa-dự-án]
```

Clone dự án về bằng Command Prompt 

```bash
  https://github.com/Huynneh/DataMiningTopKUncertain_CD3.git
```

Di chuyển vào thư mục dự án
```bash
cd DataMiningTopKUncertain_CD3
```

## Chạy chương trình

- Biên dịch chương trình:
```bash
javac -encoding UTF-8 -d bin src/main/*.java src/dal/*.java src/bll/*.java src/model/*.java
```

- Chạy chương trình
```bash
  java -cp bin main.Main
```

Sau khi chạy chương trình thành công, hệ thống sẽ hiển thị thông báo yêu cầu người dùng nhập giá trị k từ bàn phím nhằm xác định số lượng Top-K Frequent Itemsets cần khai thác. Khi người dùng nhập giá trị k và nhấn Enter, chương trình sẽ tự động lần lượt đọc toàn bộ các tập dữ liệu trong thư mục dữ liệu gốc, tiến hành sinh cơ sở dữ liệu xác suất tương ứng, sau đó thực thi các thuật toán khai thác Top-K Frequent Itemsets đã được cài đặt. Trong suốt quá trình này, hệ thống đồng thời đo thời gian thực thi và mức sử dụng bộ nhớ của từng thuật toán, cuối cùng xuất kết quả chi tiết ra các file kết quả và bảng tổng hợp phục vụ cho việc phân tích và so sánh hiệu năng.
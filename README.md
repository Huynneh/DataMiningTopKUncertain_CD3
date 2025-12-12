# DataMiningTopKUncertain_CD3

# Chuyên đề nghiên cứu 3
Algorithms for mining Top-K frequent itemsets from uncertain databases
Khai thác Top-K Frequent Itemsets trên Cơ sở Dữ liệu Không Chắc Chắn

## Học viên: Tô Ngọc Huyền - 242805005

## Nội dung
Dự án này cài đặt nhiều thuật toán khai thác Top-K Frequent Itemsets trên cơ sở dữ liệu giao dịch không chắc chắn, bao gồm:
- U-Apriori
- U-FP-Growth
- U-HMine
- HybridTopKMiner (kết hợp dựa trên mật độ dữ liệu)
Hệ thống tự động duyệt toàn bộ dataset, sinh bản dataset xác suất, chạy tất cả thuật toán và ghi kết quả ra file .txt.

## Cấu trúc dự án
TOPKUNCERTAINMINING
|
|--datasets/
|   |--origin/
|   |--probability/
|
|--outputs/
|
|--src/
    |--bll/
    |   |--HybridTopKMiner.java
    |   |--UAprioriTopK.java
    |   |--UFPGrowth.java
    |   |--UHMine.java
    |
    |--dal/
    |   |--DataReader.java
    |   |--DatasetGenerator.java
    |   |---DataWriter.java
    |
    |--main/
    |   |--Main.java
    |
    |--model/
        |--Itemset.java
        |--Result.java
        |--Transaction.java

## Cài đặt 
Cài đặt Java Development Kit (JDK) cho Windows nếu chưa có [Tại đây](https://www.oracle.com/java/technologies/javase/jdk15-archive-downloads.html)

Mở Command Prompt với quyền admin và đi tới thư mục chứa dự án

```bash
  cd [thư-mục-chứa-dự-án]
```

Clone dự án về bằng Command Prompt 

```bash
  https://github.com/Huynneh/DataMiningTopKUncertain_CD3.git
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

- Sau khi chạy chương trình thành công,....nhập k,....chạy các dataset tự động,...xuất file (đang làm)
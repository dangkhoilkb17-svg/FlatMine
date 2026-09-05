# FlatMine

**FlatMine** là Fabric mod cho **Minecraft Java Edition 1.21.1**, được thiết kế để tự động phá một vùng hình hộp chữ nhật 3D (cuboid) trong **Survival**.

## Tính năng

- Chọn **2 block làm 2 góc đối diện** để xác định toàn bộ vùng đào.
- Hỗ trợ vùng theo cả **X, Y và Z**, không giới hạn trong một mặt phẳng.
- Sau khi xác nhận, FlatMine tự động xử lý toàn bộ block trong vùng đã chọn.
- Quá trình đào được thực hiện **phía server**, phù hợp với cơ chế server-authoritative của Minecraft.
- Chỉ cho phép bắt đầu đào khi người chơi đang ở **Survival-like mode** và đang cầm **cúp hoặc xẻng**.
- Các block tương tác như container/block có giao diện được loại khỏi vùng tự động đào để tránh phá nhầm các block đang được sử dụng.

## Cơ chế phá block

FlatMine không cố mô phỏng thời gian đào Vanilla cho từng block. Tốc độ xử lý vùng được điều khiển bởi hệ thống mining job của mod.

Mọi block hợp lệ trong vùng đều có thể bị FlatMine phá. **Việc block có item/block drop hay không vẫn dựa trên cơ chế harvest và loot của Minecraft 1.21.1.**

Quy tắc độ bền của công cụ trong FlatMine:

| Kết quả phá block | Độ bền |
|---|---:|
| Block có drop hợp lệ | -1 |
| Block không có drop do không đủ điều kiện harvest | -2 |

Ví dụ:

- Cúp gỗ + Iron Ore → phá được, **không drop**, -2 durability.
- Cúp đá + Iron Ore → phá được, **drop**, -1 durability.
- Cúp đá + Diamond Ore → phá được, **không drop**, -2 durability.
- Cúp sắt + Diamond Ore → phá được, **drop**, -1 durability.
- Cúp kim cương + Dirt → phá được, **drop Dirt**, -1 durability.

FlatMine sử dụng các API drop/harvest của Minecraft để giữ các quy tắc loot của Vanilla thay vì tự tạo một bảng drop riêng.

## Giới hạn vùng

Vùng được xác định hoàn toàn từ 2 điểm đã chọn. Có thể giới hạn số block trước khi bắt đầu đào; khi giới hạn được áp dụng, vùng sẽ được thu nhỏ theo logic của mod.

## Yêu cầu

- Minecraft **1.21.1**
- Java **21**
- Fabric Loader **0.16.10 hoặc mới hơn**
- Fabric API

## Build từ source

```bash
./gradlew clean build
```

File `.jar` sau khi build nằm trong:

```text
build/libs/
```

## License

FlatMine được phát hành theo giấy phép **MIT**.

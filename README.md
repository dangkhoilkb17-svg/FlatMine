# FlatMine

**FlatMine** là Fabric mod cho **Minecraft Java Edition 1.21.1**, cho phép chọn hai block làm hai góc đối diện của một vùng hình hộp chữ nhật 3D (cuboid) và tự động xử lý toàn bộ block trong vùng.

## Tính năng

- Chọn **2 block làm 2 góc đối diện** để xác định vùng.
- Hỗ trợ vùng 3D theo **X, Y và Z**.
- Server tự động xử lý các block trong vùng đã chọn.
- Hỗ trợ giới hạn số lượng block trước khi bắt đầu.
- Có tùy chọn **tiêu hủy drop**.
- Các block tương tác/container được lọc để tránh phá nhầm block đang được sử dụng.

## Chế độ Survival

Trong **Survival / Survival-like**, FlatMine hoạt động với cơ chế mining riêng nhưng giữ quy tắc harvest và loot của Minecraft cho việc xác định drop.

### Cách phá block

- Mọi block hợp lệ trong vùng đều có thể bị FlatMine phá.
- Tốc độ xử lý không phụ thuộc vào tốc độ đào riêng của từng block như khi đào thủ công.
- Việc **có drop hay không** được xác định bằng điều kiện harvest của Minecraft 1.21.1 và loot của block.

### Độ bền công cụ

| Kết quả | Độ bền |
|---|---:|
| Có item/block drop | -1 |
| Không có drop do không đủ điều kiện harvest | -2 |

Ví dụ:

- Cúp gỗ + Iron Ore → phá được, **không drop**, -2 durability.
- Cúp đá + Iron Ore → phá được, **drop**, -1 durability.
- Cúp đá + Diamond Ore → phá được, **không drop**, -2 durability.
- Cúp sắt + Diamond Ore → phá được, **drop**, -1 durability.
- Cúp kim cương + Dirt → phá được, **drop Dirt**, -1 durability.

FlatMine dùng API harvest/loot của Minecraft thay vì tự tạo danh sách drop riêng, nên các quy tắc loot như Silk Touch/Fortune tiếp tục được xử lý bởi hệ thống của Minecraft.

## Chế độ Creative

Trong **Creative**, các cơ chế mining của Survival **không hoạt động**:

- Không kiểm tra cấp độ cúp để quyết định drop.
- Không xử lý drop.
- Không trừ độ bền.
- Không chạy cơ chế harvest của Survival.

Creative **chỉ được sử dụng chức năng tiêu hủy vùng**. Khi bật tùy chọn tiêu hủy drop, FlatMine xóa các block trong vùng mà không tạo item/XP drop và không yêu cầu cúp/xẻng.

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

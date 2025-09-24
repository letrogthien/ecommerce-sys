package com.chuadatten.wallet.vnpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IPNReturn {
// Merchant trả dữ liệu lại cho VNPAY bằng mã RspCode và Message định dạng JSON:
// Trong đó:
// RspCode là mã lỗi tình trạng cập nhật trạng thái thanh toán của giao dịch tại đầu IPN của merchant.
// Message là mô tả mã lỗi của RspCode
// Merchant cần tuân thủ theo các trường hợp kiểm và phản hồi lại RspCode cho VNPAY. Vui lòng tham khảo thêm tại code demo IPN của VNPAY
// Cơ chế retry IPN:
// Hệ thống VNPAY căn cứ theo RspCode phản hồi từ merchant để kết thúc luồng hay bật cơ chế retry
// RspCode: 00, 02 là mã lỗi IPN của merchant phản hồi đã cập nhật được tình trạng giao dịch. VNPAY kết thúc luồng
// RspCode: 01, 04, 97, 99 hoặc IPN timeout là mã lỗi IPN merchant không cập nhật được tình trạng giao dịch. VNPAY bật cơ chế retry IPN

    private String rspCode;
    private String message;
}

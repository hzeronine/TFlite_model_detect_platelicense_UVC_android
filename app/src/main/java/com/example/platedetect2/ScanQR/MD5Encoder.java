package com.example.platedetect2.ScanQR;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Encoder {

    // Phương thức để mã hóa một chuỗi thành MD5
    public static String encodeToMD5(String input) {
        try {
            // Tạo một đối tượng MessageDigest với thuật toán MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Cập nhật dữ liệu đầu vào cho MessageDigest
            md.update(input.getBytes());

            // Mã hóa dữ liệu và lấy mã băm MD5 dưới dạng mảng byte
            byte[] byteData = md.digest();

            // Chuyển đổi mảng byte thành dạng hexa
            BigInteger number = new BigInteger(1, byteData);
            String md5String = number.toString(16);

            // Thêm các số 0 vào trước chuỗi nếu cần thiết
            while (md5String.length() < 32) {
                md5String = "0" + md5String;
            }

            // Trả về chuỗi MD5 đã mã hóa
            return md5String;
        } catch (NoSuchAlgorithmException e) {
            // Xử lý nếu thuật toán MD5 không tồn tại
            e.printStackTrace();
            return null;
        }
    }

}
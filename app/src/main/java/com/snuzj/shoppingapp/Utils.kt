package com.snuzj.shoppingapp

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

object Utils {

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"
    //categories arraylist
    val categories = arrayOf(
        "Bất động sản", //real estate
        "Xe cộ", //vihicles
        "Đồ điện tử", //electronicDevices
        "Thú cưng" //pets
    )

    //real estate arraylist
    val realEstate = arrayOf(
        "Căn hộ/ Chung cư",
        "Nhà ở",
        "Đất",
        "Văn phòng, Mặt bằng kinh doanh",
        "Phòng trọ"
    )

    //vihicles arraylist
    val vihicles = arrayOf(
        "Ô tô",
        "Xe máy",
        "Xe tải, xe ben",
        "Xe điện",
        "Xe đạp",
        "Phương tiện khác",
        "Phụ tùng xe"
    )

    //electronic devices arraylist
    val electronicDevices = arrayOf(
        "Điện thoại",
        "Máy tính bảng",
        "Laptop",
        "Máy tính bàn",
        "Máy ảnh, máy quay",
        "Tivi, âm thanh",
        "Thiết bị đeo thông minh",
        "Phụ kiện(bàn phím, chuột,...)",
        "Linh kiện(RAM, CARD,...)",
    )

    //pets arraylist
    val pets = arrayOf(
        "Gà",
        "Chó",
        "Chim",
        "Mèo",
        "Thú cưng khác",
        "Phụ kiện, thức ăn, dịch vụ"
    )

    //conditions
    val conditions = arrayOf(
        "Mới",
        "Đã sử dụng",
        "Đã qua sửa chữa"
    )


    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    }

    fun getTimeStamp(): Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale("vi", "VN"))
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

}
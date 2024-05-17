package com.example.platedetect2.ScanQR;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.platedetect2.R;

public class Setup extends AppCompatActivity {
    private int selectedOption = 0; // Lựa chọn mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final TextView label = findViewById(R.id.label);

        // Thiết lập sự kiện click cho nhãn
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị hộp thoại lựa chọn
                AlertDialog.Builder builder = new AlertDialog.Builder(Setup.this);
                builder.setTitle("Chọn một cổng để bắt đầu: ");
                // Thiết lập các lựa chọn radio
                final String[] options = {"Cổng check in", "Cổng check out"};
                builder.setSingleChoiceItems(options, selectedOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Lưu lựa chọn được chọn
                        selectedOption = which;
                    }
                });
                // Thiết lập nút OK
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Xử lý khi người dùng bấm nút OK
                        switch (selectedOption) {
                            case 0:
                                Intent intent = new Intent(Setup.this, ScanCheckIn.class);
                                startActivity(intent);

                                break;
                            case 1:
                                // Xử lý cho lựa chọn 2
                                Intent intent2 = new Intent(Setup.this, ScanCheckOut.class);
                                startActivity(intent2);
                                break;
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
    }
}
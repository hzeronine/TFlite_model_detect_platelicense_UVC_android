package com.example.platedetect2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProgressHelper {
    private static AlertDialog dialog = null;
    private static ImageFilterView imageFilterView;
    private static TextView tvText;
    public static void showDialog(Context context, String message) {
        if(dialog == null){
            int llPadding = 30;
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(llPadding, llPadding, llPadding, llPadding);
            ll.setBackgroundResource(R.color.transparent);
            ll.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            ll.setLayoutParams(llParam);

            Bitmap bitmap = CreateQRBitmap(message);
            imageFilterView = new ImageFilterView(context);
            imageFilterView.setPadding(0, 0, llPadding, 0);
            imageFilterView.setImageBitmap(bitmap);
            imageFilterView.setLayoutParams(llParam);

            llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            tvText = new TextView(context);
            tvText.setText(message);
            tvText.setTextColor(Color.parseColor("#000000"));
            tvText.setTextSize(20);
            tvText.setLayoutParams(llParam);

            ll.addView(imageFilterView);
            ll.addView(tvText);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(ll);

            dialog = builder.create();

            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_transparent);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(layoutParams);
            }
        }
    }

    private static Bitmap CreateQRBitmap(String message) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap("content", BarcodeFormat.QR_CODE, 400, 400);
            return bitmap;
        } catch(Exception e) {

        }
        return null;
    }
    public static void UpdateImage(String message){
        Bitmap bitmap = CreateQRBitmap(message);
        if(bitmap != null && imageFilterView != null){
            imageFilterView.setImageBitmap(bitmap);
            tvText.setText(message);
        }

    }

    public static void showMessageDialog(Context context, String ContentMessage, CallbackMesageDialog callback ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Cảnh Báo !!");
        SpannableString message = new SpannableString(ContentMessage);
        message.setSpan(new ForegroundColorSpan(Color.BLACK), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.OnAccecpted();
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_button_time);
        alertDialog.show();
        setBlueButton(context, alertDialog);
    }

    private static void setBlueButton(Context context, AlertDialog alertDialog) {
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.blue));

        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(context.getResources().getColor(R.color.blue));
    }

    public static  boolean isDialogVisible(){
        if(dialog != null){
            return true;
        }
        return false;
    }
    public static  boolean isDialogGone(){
        if(!dialog.isShowing()){
            return true;
        }
        return false;
    }

    public static  void dismissDialog(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }
    public static void hidesDialog(){
        if(dialog != null){
            dialog.hide();
        }
    }
    public static void ShowDialog(){
        if(dialog != null){
            dialog.show();
        }
    }
    public interface CallbackMesageDialog {
        void OnAccecpted();
    }
}


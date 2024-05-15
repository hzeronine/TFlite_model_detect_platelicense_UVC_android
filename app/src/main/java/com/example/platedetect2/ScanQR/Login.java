package com.example.platedetect2.ScanQR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.platedetect2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo GoogleSignInOptions với yêu cầu lấy thông tin email của người dùng
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Khởi tạo GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Đăng ký sự kiện khi người dùng click vào nút đăng nhập bằng Google
        findViewById(R.id.btn_login_google).setOnClickListener(view -> signIn());

        //checkLoggedIn();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult();
            // Kiểm tra xem đăng nhập thành công hay không
            if (account != null) {
                String userEmail = account.getEmail(); // Lấy địa chỉ email từ tài khoản Google
                // Thực hiện yêu cầu API để lấy danh sách địa chỉ email từ API
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://c2se-14-sts-api.onrender.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                apiService.getEmailLocations().enqueue(new Callback<List<EmailLocation>>() {
                    @Override
                    public void onResponse(Call<List<EmailLocation>> call, Response<List<EmailLocation>> response) {
                        if (response.isSuccessful()) {
                            List<EmailLocation> emailLocations = response.body();
                            if (emailLocations != null && !emailLocations.isEmpty()) {
                                boolean isUserValid = false;
                                for (EmailLocation emailLocation : emailLocations) {
                                    if (emailLocation.getEmail().equals(userEmail)) {
                                        isUserValid = true;
                                        // Địa chỉ email hợp lệ, đăng nhập thành công
                                        saveLoginSession(account.getId(), account.getEmail()); // Lưu thông tin phiên đăng nhập vào SharedPreferences
                                        Log.d("Test: ", account.getId() + "acc: " + account.getEmail());
                                        Intent intent = new Intent(Login.this, ScanQR.class);
                                        // Gửi dữ liệu qua MainActivity
                                        intent.putExtra("user_email", emailLocation.getEmail());
                                        intent.putExtra("location_id", emailLocation.getLocationId());
                                        intent.putExtra("location_name", emailLocation.getLocationName());
                                        startActivity(intent);
                                    }
                                }
                                if (!isUserValid) {
                                    // Địa chỉ email không hợp lệ, hiển thị thông báo hoặc không cho phép đăng nhập
                                    Toast.makeText(Login.this, "Địa chỉ email chưa được đăng ký", Toast.LENGTH_SHORT).show();
                                    signOut(); // Đăng xuất người dùng
                                }
                            } else {
                                // Không có địa chỉ email nào từ API
                                Toast.makeText(Login.this, "Không có địa chỉ email từ API", Toast.LENGTH_SHORT).show();
                                signOut(); // Đăng xuất người dùng
                            }
                        } else {
                            // Xử lý lỗi khi gọi API
                            Toast.makeText(Login.this, "Lỗi khi gửi yêu cầu đến server", Toast.LENGTH_SHORT).show();
                            signOut(); // Đăng xuất người dùng
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EmailLocation>> call, Throwable t) {
                        // Xử lý lỗi khi gửi yêu cầu
                        Toast.makeText(Login.this, "Lỗi khi gửi yêu cầu đến server", Toast.LENGTH_SHORT).show();
                        signOut(); // Đăng xuất người dùng
                    }
                });
            } else {
                // Đăng nhập không thành công
                Toast.makeText(this, "Đăng nhập không thành công", Toast.LENGTH_SHORT).show();
                signOut(); // Đăng xuất người dùng
            }
        } catch (Exception e) {
            // Xử lý lỗi khi đăng nhập
            Log.e("GoogleSignIn", "signInResult:failed code=" + e.getMessage());
            Toast.makeText(this, "Đăng nhập không thành công !", Toast.LENGTH_SHORT).show();
            signOut(); // Đăng xuất người dùng
        }
    }



    private void saveLoginSession(String userId, String email) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.putString("email", email);
        editor.apply();
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        Log.d("GoogleSignIn", "Sign out successful");
                    } else {
                        Log.w("GoogleSignIn", "Sign out failed");
                    }
                });
    }
    @Override
    protected void onStop() {
        super.onStop();
        signOut();
    }
}
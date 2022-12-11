package com.example.mine;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class MultiImageActivity extends AppCompatActivity {
    private static final String TAG = "MultiImageActivity";
    ArrayList<Uri> uriList = new ArrayList<>();     // 이미지의 uri를 담을 ArrayList 객체

    RecyclerView recyclerView;  // 이미지를 보여줄 리사이클러뷰
    MultiImageAdapter adapter;  // 리사이클러뷰에 적용시킬 어댑터

    private LocalDate date;

    //사진을 불러오는 장소
    private int imgFrom = -1;

    protected String imageFileName;

    //Fragment
    private FragmentManager manager;
    private ImageFragment imageFragment;
    private TextFragment textFragment;

    private int fragmentNum = 1;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_image);

        date = (LocalDate) getIntent().getSerializableExtra("localDate");




        //Back버튼
        Button btn_back = findViewById(R.id.diary_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Text읽기 버튼
        Button btn_readText = findViewById(R.id.readText);
        btn_readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragmentNum == 1) {
                    fragmentNum = 0;
                    FragmentTransaction tf = manager.beginTransaction();
                    tf.replace(R.id.recyclerView, textFragment);
                    tf.commit();
                } else {
                    fragmentNum = 1;
                    FragmentTransaction tf = manager.beginTransaction();
                    tf.replace(R.id.recyclerView, imageFragment);
                    tf.commit();
                }
            }
        });


        //글쓰기 버튼
        Button btn_getText = findViewById(R.id.getText);
        btn_getText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                writing();
            }
        });


        // 앨범으로 이동하는 버튼
        Button btn_getImage = findViewById(R.id.getImage);
        btn_getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });
        recyclerView = findViewById(R.id.recyclerView);

        //Fragment
        manager = getSupportFragmentManager();
        imageFragment = new ImageFragment();
        textFragment = new TextFragment();
    }

    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {   // 어떤 이미지도 선택하지 않은 경우
            Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {   // 이미지를 하나라도 선택한 경우
            if (data.getClipData() == null) {     // 이미지를 하나만 선택한 경우
                Log.e("single choice: ", String.valueOf(data.getData()));
                Uri imageUri = data.getData();
                uriList.add(imageUri);
                uploadImg(imageUri);

                adapter = new MultiImageAdapter(uriList, getApplicationContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
            } else {      // 이미지를 여러장 선택한 경우
                ClipData clipData = data.getClipData();
                Log.e("clipData", String.valueOf(clipData.getItemCount()));

                if (clipData.getItemCount() > 10) {   // 선택한 이미지가 11장 이상인 경우
                    Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                } else {   // 선택한 이미지가 1장 이상 10장 이하인 경우
                    Log.e(TAG, "multiple choice");

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                        try {
                            uriList.add(imageUri);  //uri를 list에 담는다.
                            uploadImg(imageUri);
                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }

                    adapter = new MultiImageAdapter(uriList, getApplicationContext());
                    recyclerView.setAdapter(adapter);   // 리사이클러뷰에 어댑터 세팅
                    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));     // 리사이클러뷰 수평 스크롤 적용
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void writing() {
        Intent intent = new Intent(getApplicationContext(), WritingDiary.class);
        intent.putExtra("localDate", date);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void uploadImg(Uri uri) {
        UploadTask uploadTask = null;
        StorageReference reference;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String lockID = ((LogInActivity) LogInActivity.context_login).doc;
        System.out.println(lockID);
        DocumentReference docRef = db.collection("user_photo").document(lockID).collection("photo").document(String.valueOf(date));

        String timeStamp = String.valueOf(Instant.now());
        String imageFileName = "IMAGE_" + timeStamp + "_.png";

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {


            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                Map<String, Object> input = new HashMap<>();
                input.put(timeStamp,uri);
                docRef.update(input);
            }
        });



        reference = storage.getReference().child(lockID).child(imageFileName);
        uploadTask = reference.putFile(uri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               Log.e("tag","uploding!");
            }
        });
    }
}
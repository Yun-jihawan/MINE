package com.example.mine;

import static com.example.mine.R.layout.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ListView listview=findViewById(R.id.listview);
        List<String> list=new ArrayList<>();

        list.add("잠금 설정");
        list.add("닉네임 변경");
        list.add("로그아웃");
        list.add("회원 탈퇴");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //잠금설정
                if(i==0) {
                    Intent intent = new Intent(getApplicationContext(), AppLock.class);
                    startActivity(intent);
                }
                //닉네임변경
                if(i==1){
                 //다이어로그창
                }
                //로그아웃
                if(i==2){
                    new AlertDialog.Builder(Setting.this)
                            .setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                            .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    Intent intent = new Intent(Setting.this , LogInActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);

                                    SharedPreferences sharedPref = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.clear();
                                    editor.commit();
                                    finish();


                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }
                //회원탈퇴
                        if(i==3){
                    new AlertDialog.Builder(Setting.this).setMessage("회원 탈퇴를 하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                   //SHAREDpref에 저장된 값 삭제하려고 작성해 놓은 코드라 지우시면 안돼요~!
                                    Intent intent = new Intent(Setting.this , LogInActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    SharedPreferences sharedPref = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.clear();
                                    editor.commit();
                                    finish();
                                }


                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                }
                            })
                            .show();

                }

            }
        });
    }

}
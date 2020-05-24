package com.example.trakid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class
ChildList extends AppCompatActivity {


    List<ChildObject> childList;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_list);

        childList=new ArrayList<>();
        listView=findViewById(R.id.list_child);

        final ChildObject childObject=new ChildObject();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UserDevices");
        DatabaseReference myRef1 = myRef.child("RxpwhI5lBXd2P0D0G0Tl36xVCZk2");

        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                for(DataSnapshot chlidSnapshot:dataSnapshot.getChildren()){
                    HashMap<String,Object> value=(HashMap<String, Object>)chlidSnapshot.getValue();
                    ChildObject childObject=chlidSnapshot.getValue(ChildObject.class);


                    childList.add(new ChildObject((String)value.get("label"),(String)value.get("paircode")));
                    Log.e("datasnapshot of : ",chlidSnapshot.getKey());
                    Log.e("datasnapshot of : ",(String)value.get("label"));



                }
              ChildAdapter adapter=new ChildAdapter(ChildList.this,childList);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView,
//                                    View view,
//                                    int position,
//                                    long id) {
//
//                Intent intent=new Intent(ChildList.this,MainActivity.class);
//                intent.putExtra(MainActivity.EXTRA_PAIRCODE,childObject.getPaircode());
//                startActivity(intent);
//
//            }
//        };
//
//        listView.setOnItemClickListener(itemClickListener);
    }
}

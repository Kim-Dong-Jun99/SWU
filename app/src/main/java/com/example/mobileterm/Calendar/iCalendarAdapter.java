package com.example.mobileterm.Calendar;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mobileterm.BulletinBoard.BoardInfo;
import com.example.mobileterm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import android.app.AlertDialog;

public class iCalendarAdapter extends BaseAdapter {

    Context mContext;
    private static final String TAG = "iCalendarAdapter";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    CollectionReference docref = db.collection("Users").document(user.getUid()).collection("iSchedule");
//    private TextView scheduleTextView;
//    private TextView isdoneCheck;
    LayoutInflater inflater;
    private ArrayList<iCalendarItem> scheduleList;


    public iCalendarAdapter(ArrayList<iCalendarItem> arrayList) {
        this.scheduleList = new ArrayList<iCalendarItem>();
        this.scheduleList.addAll(arrayList);

    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.icontent, parent, false);
        }else{
            View view = new View(context);
            view = convertView;
        }

        TextView scheduleTextView = (TextView) convertView.findViewById(R.id.text1);
        CheckBox isdoneBox = (CheckBox) convertView.findViewById(R.id.isDone);

        iCalendarItem a = scheduleList.get(position);
        String docAA = a.getDocA();

        scheduleTextView.setText(a.getSchedule());


        LinearLayout eachlist = (LinearLayout) convertView.findViewById(R.id.eachlist);
        eachlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "80" + docAA);
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogTheme));

                builder.setTitle("Confirm").setMessage("삭제하시겠습니까?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        docref.document(docAA).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "successfully delete");
                                    //더이상 이거 안보이도록
                                    scheduleList.remove(a);
                                    notifyDataSetChanged();
                                    Toast.makeText(view.getContext(), "삭제 처리 되었습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Log.d(TAG, "삭제 취소 되었습니다" + docAA);
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        isdoneBox.setOnCheckedChangeListener(null);
        isdoneBox.setChecked(Boolean.parseBoolean(a.getIsDone()));

        isdoneBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(scheduleList.get(pos).getIsDone().equals("false")&&isChecked==true) { //리스트뷰 보여줄때 체크 안된 상태에서 체크 시!
                    a.setIsDone("true");
                    Toast.makeText(buttonView.getContext(), "완료 처리되었습니다", Toast.LENGTH_SHORT).show();
                    docref.document(docAA).set(a).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "successfully added");
                            }
                        }
                    });
                }
                else {
                    a.setIsDone("false");
                    docref.document(docAA).set(a).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "successfully added");
                            }
                        }
                    });
                    Toast.makeText(buttonView.getContext(), "완료 처리 취소되었습니다", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "else" + scheduleList.get(pos).getIsDone());
                }


            }
        });
        return convertView;

    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public iCalendarItem getItem(int position) {
        return scheduleList.get(position);
    }


    public void addItem(iCalendarItem newItem) // 일단 스케줄만 add 하도록 테스트
    {
        scheduleList.add(newItem);
        notifyDataSetChanged();


    }
}











//    public void addItem(iCalendarItem item) {
//        scheduleList.add(item);
//        notifyDataSetChanged();
//    }

//    public View getView(final int position, View itemView, ViewGroup parent) {
//        final Context context = parent.getContext();
//        final iCalendarItem a = scheduleList.get(position);
//        if (itemView == null) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            itemView = inflater.inflate(R.layout.icontent, parent, false);
//        } else {
//            View view = new View(context);
//            view = itemView;
//        }
//
//        scheduleTextView = (TextView) itemView.findViewById(R.id.text1);
//        isdoneCheck = (TextView) itemView.findViewById(R.id.isDone);
//
//        scheduleTextView.setText(a.getSchedule());
//        isdoneCheck.setText(a.getIsDone());
//        db = FirebaseFirestore.getInstance();
//
//        CollectionReference docref = db.collection("Users").document(curUser.getUid()).collection("iSchedule");
//
//
//        return itemView;
//
//    }


//    public iCalendarAdapter(Context context, ArrayList<iCalendarItem> scheduleList2) {
//        mContext = context;
//        this.scheduleList1 = new ArrayList<iCalendarItem>();
//        this.scheduleList1.addAll(scheduleList2);
//        inflater = LayoutInflater.from(mContext);
//        this.scheduleList = new ArrayList<iCalendarItem>();
//        this.scheduleList.addAll(scheduleList2);
//        Log.d(TAG, "ListViewAdapter : " + scheduleList.size());
//    }


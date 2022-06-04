package com.example.mobileterm.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mobileterm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class gCalendarAdapter extends BaseAdapter {
    Dialog dialogShow;
    Context mContext;
    private static final String TAG = "gCalendarAdapter";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    CollectionReference docref = db.collection("Schedule").document("aGroup").collection("gSchedule");

    LayoutInflater inflater;
    private ArrayList<gCalendarItem> scheduleList;

    public gCalendarAdapter(ArrayList<gCalendarItem> arrayList, Dialog dialogShow) {
        this.scheduleList = new ArrayList<gCalendarItem>();
        this.scheduleList.addAll(arrayList);
        this.dialogShow = dialogShow;
        this.dialogShow.setContentView(R.layout.dialog_show);
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
    public gCalendarItem getItem(int position) {
        return scheduleList.get(position);
    }


    public void addItem(gCalendarItem newItem) // 일단 스케줄만 add 하도록 테스트
    {
        scheduleList.add(newItem);
        notifyDataSetChanged();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gcontent, parent, false);
        }else{
            View view = new View(context);
            view = convertView;
        }

        TextView scheduleText = (TextView) convertView.findViewById(R.id.text1);

        gCalendarItem a = scheduleList.get(position);
        String docAA = a.getDocA();

        scheduleText.setText(a.getSchedule());

        LinearLayout eachlist = (LinearLayout) convertView.findViewById(R.id.eachlist);
        eachlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShow.show();

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogTheme));

                TextView placeText = (TextView)dialogShow.findViewById(R.id.placeText);
                TextView timeText = (TextView)dialogShow.findViewById(R.id.timeText);
                TextView scheduleText = (TextView)dialogShow.findViewById(R.id.scheduleText);
                Button del_btn = (Button) dialogShow.findViewById(R.id.del_btn);
                Button ok_btn = (Button) dialogShow.findViewById(R.id.ok_btn);

                placeText.setText("장소 : " + a.getPlace());
                timeText.setText("시간 : " +a.getTime());
                scheduleText.setText("일정 : " +a.getSchedule());

                del_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "button delete");
                        docref.document(docAA).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "successfully delete");
                                    //더이상 이거 안보이도록
                                    scheduleList.remove(a);
                                    notifyDataSetChanged();
                                    dialogShow.dismiss();
                                    Toast.makeText(view.getContext(), "삭제 처리되었습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                ok_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogShow.dismiss();
                    }
                });
            }
        });

        return convertView;

    }


}

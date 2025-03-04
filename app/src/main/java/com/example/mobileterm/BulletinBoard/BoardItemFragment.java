package com.example.mobileterm.BulletinBoard;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileterm.MainActivity;
import com.example.mobileterm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BoardItemFragment extends Fragment {
    private String did;
//    private BoardInfo selectedBoardItem;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser curUser;
    ListView commentListView;
    CommentListViewAdapter commentListViewAdapter;
    ArrayList<CommentInfo> arrayList = new ArrayList<CommentInfo>();
    String TAG = "BoardItemFragment";

    String userNickName;
    Dialog editCommentDialog;
    Dialog editItemDialog;
    Dialog newCommentDialog;

    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    long mnow;
    Date mDate;
    TextView titleTextViewBoardItem;
    TextView timeTextView ;
    TextView nameTextViewBoardItem;
    TextView contentTextViewBoardItem;
    TextView tagTextViewBoardItem;
    TextView likedCountTextViewBoardItem;

    ImageButton editItemButton;
    ImageButton deleteItemButton;

    long curLike;
    long updateLike;
    boolean notLiked;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_board_item_new, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();
        did = mainActivity.sendDid();
        userNickName = mainActivity.sendUserNickname();
        editItemDialog = new Dialog(getActivity());
        editItemDialog.setContentView(R.layout.edit_item_dialog);
        newCommentDialog = new Dialog(getActivity());
        newCommentDialog.setContentView(R.layout.new_comment_dialog);
        Log.d(TAG,did);
        commentListView = rootView.findViewById(R.id.commentView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();


        timeTextView = rootView.findViewById(R.id.timeTextView);
        nameTextViewBoardItem = rootView.findViewById(R.id.nameTextViewBoardItem);
        contentTextViewBoardItem = rootView.findViewById(R.id.contentTextViewBoardItem);
        titleTextViewBoardItem = rootView.findViewById(R.id.titleTextViewBoardItem);
        tagTextViewBoardItem = rootView.findViewById(R.id.tagTextViewBoardItem);
        ImageButton likeButton = rootView.findViewById(R.id.likeButton);
        likedCountTextViewBoardItem = rootView.findViewById(R.id.likedCountViewBoardItem);
        ImageButton addCommentButtonBoardItem = rootView.findViewById(R.id.addCommentButtonBoardItem);

        editItemButton = rootView.findViewById(R.id.editItemButton);
        deleteItemButton = rootView.findViewById(R.id.deleteItemButton);

        likeButton.setOnClickListener(onClickListener);
        addCommentButtonBoardItem.setOnClickListener(onClickListener);
        db.collection("BulletinBoard").document(did).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Log.d(TAG,"nickname"+userNickName);
                        titleTextViewBoardItem.setText((String) document.getData().get("title"));
                        contentTextViewBoardItem.setText((String) document.getData().get("content"));
                        timeTextView.setText((String) document.getData().get("writtenTime"));
                        nameTextViewBoardItem.setText((String) document.getData().get("name"));
                        likedCountTextViewBoardItem.setText((String)Long.toString((Long)document.getData().get("likedCount")));
                        tagTextViewBoardItem.setText("");
                        if (userNickName.equals((String) document.getData().get("name"))) {
                            editItemButton.setVisibility(View.VISIBLE);
                            deleteItemButton.setVisibility(View.VISIBLE);

                            editItemButton.setOnClickListener(onClickListener);
                            deleteItemButton.setOnClickListener(onClickListener);
                        }



                        ArrayList<CommentInfo> newArrayList = new ArrayList<CommentInfo>();
                        CollectionReference docRef = db.document("BulletinBoard/"+did).collection("Comments");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (document.exists()) {
                                            String name = (String) document.getData().get("name");
                                            String content = (String) document.getData().get("content");
                                            String writtenTime = (String) document.getData().get("writtenTime");
                                            CommentInfo data = new CommentInfo(content, name, writtenTime);
                                            boolean isSecret = (boolean) document.getData().get("secret");
                                            if (isSecret){
                                                if (userNickName.equals(name) || userNickName.equals(nameTextViewBoardItem.getText().toString())){
                                                    newArrayList.add(0,data);
                                                }else{
                                                    CommentInfo secretComment = new CommentInfo("비밀 댓글입니다. 댓글, 게시글 작성자만 볼 수 있습니다.","unknown", writtenTime);
                                                    newArrayList.add(0, secretComment);
                                                }
                                            }else{
                                                newArrayList.add(0,data);
                                            }

                                        }
                                    }
                                    if (!newArrayList.equals(arrayList)) {
                                        arrayList = newArrayList;
                                    }
                                    editCommentDialog = new Dialog(getActivity());
                                    commentListViewAdapter = new CommentListViewAdapter(rootView.getContext(), arrayList, userNickName, did, editCommentDialog);
                                    commentListView.setAdapter(commentListViewAdapter);
                                }
                            }
                        });

                        docRef = db.document("BulletinBoard/"+did).collection("BoardTags");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (document.exists()) {
                                            String tagName = (String) document.getData().get("name");
                                            String temp = tagTextViewBoardItem.getText().toString();
                                            tagTextViewBoardItem.setText(temp+tagName+" ");

                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });







        return rootView;
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.deleteItemButton:{
                    db.collection("BulletinBoard").document(did).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG,"delete success");
                            MainActivity activity = (MainActivity) getActivity();
                            activity.onFragmentChanged("deleted");
                        }
                    });
                    break;
                }
                case R.id.editItemButton:{
                    editItemDialog.show();

                    ImageButton endEditItem = editItemDialog.findViewById(R.id.endEditItem);
                    EditText titleItemEditText = editItemDialog.findViewById(R.id.titleItemEditText);
                    EditText contentItemEditText = editItemDialog.findViewById(R.id.contentItemEditText);
                    EditText tagItemEditText = editItemDialog.findViewById(R.id.tagItemEditText);


                    endEditItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String newDid = timeTextView.getText().toString()+" "+titleItemEditText.getText().toString();
                            BoardInfo changed = new BoardInfo(titleItemEditText.getText().toString(), contentItemEditText.getText().toString(), nameTextViewBoardItem.getText().toString(), newDid, timeTextView.getText().toString(), Long.parseLong(likedCountTextViewBoardItem.getText().toString()));

                            db.collection("BulletinBoard").document(newDid).set(changed).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "게시물 수정 성공했고 이제 삭제해야함");

                                    WriteBatch batch = db.batch();

                                    String temptags;
                                    String[] temptagIter;
                                    temptags = tagItemEditText.getText().toString();
                                    temptagIter = temptags.split("#");

                                    for (String tag: temptagIter) {
                                        if (tag.length() > 0){
                                            tag = "#"+tag.trim();
                                            DocumentReference tempref = db.collection("BulletinBoard").document(newDid).collection("BoardTags").document(tag);
                                            DocumentReference tempTagRef = db.collection("Tags").document(tag);
                                            DocumentReference tagDocRef = db.collection("Tags").document(tag).collection("tagDocs").document(newDid);
                                            BoardTags newTag = new BoardTags(tag);
                                            TagDocs newDoc = new TagDocs(newDid, tag);

                                            batch.set(tempref, newTag);
                                            batch.set(tempTagRef, newTag);
                                            batch.set(tagDocRef, newDoc);
                                        }

                                    }

                                    db.collection("BulletinBoard").document(did).collection("Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                QuerySnapshot tempResult = task.getResult();
                                                for (DocumentSnapshot tempDocSnap : tempResult){
                                                    CommentInfo tempUpdateComment = tempDocSnap.toObject(CommentInfo.class);
                                                    DocumentReference tempDocRef = db.collection("BulletinBoard").document(newDid).collection("Comments").document(tempUpdateComment.getWrittenTime()+tempUpdateComment.getName());

                                                    batch.set(tempDocRef, tempUpdateComment);
                                                }
                                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.e(TAG, "tag add sucess");
                                                            titleTextViewBoardItem.setText(titleItemEditText.getText().toString());
                                                            contentTextViewBoardItem.setText(contentItemEditText.getText().toString());
                                                            tagTextViewBoardItem.setText(tagTextViewBoardItem.getText().toString()+" "+tagItemEditText.getText().toString());
                                                            titleItemEditText.setText("");
                                                            tagItemEditText.setText("");
                                                            contentItemEditText.setText("");
                                                            db.collection("BulletinBoard").document(did).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Log.d(TAG, "삭제 성공");
                                                                    editItemDialog.dismiss();
                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });

                                }
                            });

//
                        }
                    });
                    break;
                }
                case R.id.addCommentButtonBoardItem:{
                    newCommentDialog.show();

                    ImageButton newCommentDone = newCommentDialog.findViewById(R.id.newCommentDone);
                    RadioGroup secretGroup = newCommentDialog.findViewById(R.id.secretGroup);
                    EditText newCommentEditText= newCommentDialog.findViewById(R.id.newCommentEditText);

                    newCommentDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String curTime = getTime();
                            CommentInfo newComment = new CommentInfo(newCommentEditText.getText().toString(), userNickName, curTime);
                            if (secretGroup.getCheckedRadioButtonId() == R.id.secret){
                                newComment.setSecret(true);
                            }

                            db.collection("BulletinBoard").document(timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString()).collection("Comments").document(curTime+nameTextViewBoardItem.getText().toString()).set(newComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    commentListViewAdapter.addComment(newComment);
                                    newCommentEditText.setText("");
                                    newCommentDialog.dismiss();
                                }
                            });
                        }
                    });

                    break;
                }
                case R.id.likeButton:{
                    notLiked = true;
                    db.collection("Users").document(curUser.getUid()).collection("likedBoardItem").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot documentSnapshot = task.getResult();
                                for (DocumentSnapshot document : documentSnapshot) {
                                    if (document.exists()) {
                                        Log.d(TAG,document.getId());
                                        if ((timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString()).equals(document.getId())){
                                            notLiked = false;
                                            break;
                                        }
                                    }
                                }
                                if (notLiked){
                                    String title = titleTextViewBoardItem.getText().toString();
                                    addToLikedItem(title);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(),"이미 좋아요 눌린 게시글입니다.",Toast.LENGTH_LONG).show();
                                    Log.d(TAG,"like already pressed");
                                }
                            }
                        }
                    });

                    break;

                }

            }
        }
    };



    public void addToLikedItem(String title){
         db.collection("Users").document(curUser.getUid()).collection("likedBoardItem").document(timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString()).set(new LikedBoardItem(title, timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "successfully added");
                    db.collection("BulletinBoard").document(timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){
                                    curLike = (Long) documentSnapshot.getData().get("likedCount");
                                    updateLike = curLike+1;
                                    db.collection("BulletinBoard").document(timeTextView.getText().toString()+" "+titleTextViewBoardItem.getText().toString()).update("likedCount", updateLike).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG,"liked done");
                                            Toast.makeText(getActivity().getApplicationContext(), "관심 게시글에 추가되었습니다.",Toast.LENGTH_LONG).show();
                                            likedCountTextViewBoardItem.setText(Long.toString(updateLike));
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
             }
         });
    }

    private String getTime() {
        mnow = System.currentTimeMillis();
        mDate = new Date(mnow);
        return mFormat.format(mDate);
    }
}

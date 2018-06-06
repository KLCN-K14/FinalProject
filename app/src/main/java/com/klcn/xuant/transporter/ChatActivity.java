package com.klcn.xuant.transporter;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.model.Chat;

import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<Chat> adapter;
    RelativeLayout activity_chat;

    //Add Emojicon
    EditText mEditChat;
    ImageButton mBtnIcon,mBtnSend;
    DatabaseReference customers;
    FirebaseAuth mFirebaseAuth;
    String email="";
    String name="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        activity_chat = (RelativeLayout)findViewById(R.id.activity_chat);

        mFirebaseAuth = FirebaseAuth.getInstance();
        customers = FirebaseDatabase.getInstance().getReference().child("Customers").child(mFirebaseAuth.getCurrentUser().getUid());

        customers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("email") != null)
                        email=map.get("email").toString();
                    if (map.get("name") != null)
                        name = map.get("name").toString();



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        //Add Emoji
        mBtnIcon = (ImageButton) findViewById(R.id.chat_add_btn);
        mBtnSend = (ImageButton)findViewById(R.id.chat_send_btn);
        mEditChat = (EditText) findViewById(R.id.chat_message_view);
//        emojIconActions = new EmojIconActions(getApplicationContext(),activity_main,emojiButton,emojiconEditText);
//        emojIconActions.ShowEmojicon();

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new Chat(mEditChat.getText().toString(),
                        email));
                mEditChat.setText("");
                mEditChat.requestFocus();
            }
        });

        //Check if not sign-in then navigate Signin page
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {

            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else
        {
            //Load content
            displayChatMessage();
        }


    }



    private void displayChatMessage() {

        ListView listOfMessage = (ListView) findViewById(R.id.messages_list);
        adapter = new FirebaseListAdapter<Chat>(this,Chat.class,R.layout.item_message,FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, Chat model, int position) {
                //Get references to the views of list_item.xml
                TextView messageText, messageUser, messageTime;
                messageText = (TextView) v.findViewById(R.id.message_text_layout);
                messageUser = (TextView) v.findViewById(R.id.name_text);
                messageTime = (TextView) v.findViewById(R.id.time_text_layout);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }

        };
        listOfMessage.setAdapter(adapter);
    }
}

package in.shreyas.dbs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;


public class Chat extends AppCompatActivity implements MessageRecieved{

    String name = "",token = "";
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    SharedPreferences preferences;
    String email,chatWith,givenName;
    Firebase reference1, reference2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        chatWith = intent.getStringExtra("chatWith");
        name = intent.getStringExtra("chatWithName");
        getSupportActionBar().setTitle(name);
        preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        email = preferences.getString("email","");
        givenName = preferences.getString("givenName","");

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://dbs-chat-fac75.firebaseio.com/messages/" + email + "_" + chatWith);
        reference2 = new Firebase("https://dbs-chat-fac75.firebaseio.com/messages/" + chatWith + "_" + email);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", givenName+"|"+email);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");

                }
            }
        });
        reference1.addChildEventListener(new MyFirebaseMessagingService(Chat.this) {


        });

    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);
        int pix = (int) (10 * Resources.getSystem().getDisplayMetrics().density);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setPadding(10,10,0,0);
            textView.setBackgroundResource(R.drawable.sent);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setPadding(10,10,10,0);
            textView.setBackgroundResource(R.drawable.received);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);

            }
        },200);
    }

    @Override
    public void OnMessageReceived(String userName, String UserEmail, String Message) {
        if(UserEmail.equals(email)){
            addMessageBox("You:-\n" + Message, 1);
        }
        else{
            addMessageBox(userName + ":-\n" + Message, 2);
        }
    }
}

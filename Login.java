package in.shreyas.dbs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class Login extends AppCompatActivity{

    SignInButton signInButton;

    private static final int RC_SIGN_IN = 789;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    String email;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         TextView t1=(TextView)findViewById(R.id.my_text_view);
        t1.setTextColor(Color.BLUE);
        t1.setText("WELCOME TO DUBLIN BUSINESS SCHOOL\n" +
                "Dublin Business School (DBS) specialises in the provision of career focused business and law education as well as the delivery of contemporary programmes in the areas of arts, media, social science, humanities and psychology. This is an exciting time for you as you lay the foundations for the rest of your life. At DBS we will do all that we can to help you to make the right choices for your future and a programme of study at DBS will give you the best possible start in your career.\n");
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        boolean isLogin = preferences.getBoolean("IsLogin",false);
        if(isLogin)
        {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Firebase.setAndroidContext(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void updateUI(GoogleSignInAccount account) {

        if(account != null) {
            try {
                final String personGivenName = account.getGivenName();
                String user1 = account.getEmail();

                final String user = user1.replaceAll("[^A-Za-z0-9 ]","_");

                final String pass = "7777";

                String url = "https://dbs-chat-fac75.firebaseio.com/users.json";

                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Firebase reference = new Firebase("https://dbs-chat-fac75.firebaseio.com/users");

                        if (s.equals("null")) {
                            Firebase child = reference.child(user);
                            child.child("password").setValue(pass);
                            child.child("username").setValue(personGivenName);
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("IsLogin", true).commit();
                            editor.putString("email", user).commit();
                            editor.putString("givenName", personGivenName).commit();
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                JSONObject obj = new JSONObject(s);


                                if (!obj.has(user)) {
                                    Firebase child = reference.child(user);
                                    child.child("password").setValue(pass);
                                    child.child("username").setValue(personGivenName);
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putBoolean("IsLogin", true).commit();
                                    editor.putString("email", user).commit();
                                    editor.putString("givenName", personGivenName).commit();
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putBoolean("IsLogin", true).commit();
                                    editor.putString("email", user).commit();
                                    editor.putString("givenName", personGivenName).commit();
                                    startActivity(intent);
                                    finish();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        dismissDialog();
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        System.out.println("" + volleyError);
                        dismissDialog();
                    }
                });

                RequestQueue rQueue = Volley.newRequestQueue(Login.this);
                rQueue.add(request);


                showDialog();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void loginResponce(String response) {
        dismissDialog();
        try {
            if (response != null) {
                JSONObject object = new JSONObject(response);
                String message = object.getString("message");
                if (message.equalsIgnoreCase("successful"))
                {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("IsLogin",true).commit();
                    editor.putString("email",email).commit();
                    startActivity(intent);
                    finish();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            super.onActivityResult(requestCode, resultCode, data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            updateUI(account);
        } catch (ApiException e) {
            updateUI(null);
        }
    }

    void showDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.loading_msg));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    void dismissDialog()
    {
        if(progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
    }
}

package in.shreyas.dbs;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyFirebaseMessagingService implements ChildEventListener {

    MessageRecieved callback;
    Context context;

    public MyFirebaseMessagingService(Context context) {
        this.context = context;
        callback = (MessageRecieved) context;
    }
    public MyFirebaseMessagingService(){

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Map map = dataSnapshot.getValue(Map.class);
        String message = map.get("message").toString();
        String User = map.get("user").toString();
        StringTokenizer st = new StringTokenizer(User, "|");
        String userName = st.nextToken();
        String userEmail ="";
        if(st.hasMoreTokens()) {
            userEmail = st.nextToken();
        }
        if(isAppIsInBackground(context)) {
            RemoteViews remoteViews = null;
            try {


                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.message_notification);


                Intent intent = new Intent(context, Chat.class);
                intent.putExtra("chatWith", userEmail);
                intent.putExtra("chatWithName", userName);
                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.dbs)
                        .setTicker("")
                        .setAutoCancel(true)
                        .setContentIntent(pIntent)
                        .setContent(remoteViews);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                remoteViews.setTextViewText(R.id.name, userName);
                remoteViews.setTextViewText(R.id.message, message);
                NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationmanager.notify(0, builder.build());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        callback.OnMessageReceived(userName,userEmail,message);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}

package com.example.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity
    implements com.opentok.android.Session.SessionListener,
        PublisherKit.PublisherListener

{

    private static String API_KEY = "46527842";
    private static String SESSION_ID = "1_MX40NjUyNzg0Mn5-MTU4MzM4NTg3OTA1Nn5ZajRBSzAvRmhuMEpOTmZ5UXNSNDdQQ0N-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjUyNzg0MiZzaWc9MjZlOTFhZGQzMDI0ODVlNjAyOTE5NDVkYzM5ZWRkZGEyZDYxYWQxMTpzZXNzaW9uX2lkPTFfTVg0ME5qVXlOemcwTW41LU1UVTRNek00TlRnM09UQTFObjVaYWpSQlN6QXZSbWh1TUVwT1RtWjVVWE5TTkRkUVEwTi1mZyZjcmVhdGVfdGltZT0xNTgzMzg1OTM1Jm5vbmNlPTAuODQ3NzkyNDIxMjAyMDg0JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1ODU5NzQzMzImaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSuscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String userID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            usersRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }

                        if(dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            usersRef.child(userID).child("Calling").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        else
                        {
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }


                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
          String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };

          if(EasyPermissions.hasPermissions(this,perms))
        {

            mPublisherViewController = findViewById(R.id.publisher_container);
            mSuscriberViewController = findViewById(R.id.subscriber_container);

            mSession = new com.opentok.android.Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        }
          else {
              EasyPermissions.requestPermissions(this, "hey this app needs Mic and Camera, Please allow",RC_VIDEO_APP_PERM, perms);


          }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(com.opentok.android.Session session)
    {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);

        }

        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(com.opentok.android.Session session) {

        Log.i(LOG_TAG, "Stream Disconnected");

    }

    @Override
    public void onStreamReceived(com.opentok.android.Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream Received");

        if(mSubscriber == null)
        {
           mSubscriber = new Subscriber.Builder(this, stream).build();
           mSession.subscribe(mSubscriber);
           mSuscriberViewController.addView(mSubscriber.getView());

        }

    }

    @Override
    public void onStreamDropped(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscriber != null)

        {
            mSubscriber = null;
            mSuscriberViewController.removeAllViews();
        }


    }

    @Override
    public void onError(com.opentok.android.Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");


    }
}

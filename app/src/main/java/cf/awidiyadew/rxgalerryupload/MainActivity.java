package cf.awidiyadew.rxgalerryupload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;


public class MainActivity extends AppCompatActivity implements OnPreparedListener {

    private static final int REQUEST_MEDIA = 100;
    private MediaItem mMediaItem;
    private EMVideoView emVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupVideoView();

        findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaOptions.Builder builder = new MediaOptions.Builder();
                MediaOptions options = builder.selectVideo().setMaxVideoDuration(5 * 1000)
                        .setShowWarningBeforeRecordVideo(true).build();

                if (options != null) {
                    MediaPickerActivity.open(MainActivity.this, REQUEST_MEDIA, options);

                }
            }
        });

    }

    private void setupVideoView() {
        emVideoView = (EMVideoView)findViewById(R.id.video_view);
        emVideoView.setOnPreparedListener(this);

        //For now we just picked an arbitrary item to play.  More can be found at
        //https://archive.org/details/more_animation
        emVideoView.setVideoURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/teambookmark-31bd3.appspot.com/o/video%2F399205.mp4?alt=media&token=c00612e9-9e8c-4bc1-aecf-4597586c822a"));
        emVideoView.showControls();
    }

    @Override
    public void onPrepared() {
        //Starts the video playback as soon as it is ready
        emVideoView.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == RESULT_OK) {

                List<MediaItem> mediaSelectedList = MediaPickerActivity
                        .getMediaItemSelected(data);

                if (mediaSelectedList != null){

                    mMediaItem = mediaSelectedList.get(0);

                    startUploadVideo(mMediaItem.getUriOrigin());
                }
            }
        }
    }

    private void startUploadVideo(Uri uri){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        StorageReference videoReference = firebaseStorage.getReference().child("video");

        UploadTask uploadTask = videoReference.child(uri.getLastPathSegment()+ ".mp4").putFile(uri);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(MainActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}

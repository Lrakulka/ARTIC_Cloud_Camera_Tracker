package tracker.camera.cloud.artic.artic_cloud_camera_tracker;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by List on 9/25/2016.
 * Handler Video and Photo making by device camera
 */

public class VideoService {
    // Preview of camera
    private SurfaceView surfaceView;
    // Device camera class
    private Camera camera;
    // Record video and audio
    private MediaRecorder mediaRecorder;
    // Dit of photo
    private File photoFile;
    // Dir of video
    private File videoFile;
    // Formatter of data
    private DateFormat dateFormat;
    // Dir of sdcard
    private File sdCardDir;

    public VideoService(Activity activity) {
        // Creating directory for videos and photos
        sdCardDir = Environment.getExternalStorageDirectory();
        sdCardDir = new File (sdCardDir.getAbsolutePath() + "/ARTIC_Cloud");
        sdCardDir.mkdirs();

        // Formatter for data. Used in name for video and photo
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        surfaceView = (SurfaceView) activity.findViewById(R.id.surfaceView);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    /**
     * Resume camera work
     */
    public void resume() {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    /**
     * Pause camera work
     */
    public void pause() {
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }

    /**
     * Make photo from camera
     */
    public void makePhoto() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                photoFile = new File(sdCardDir, "myphoto" + dateFormat.format(new Date()) + ".jpg");
                try {
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    fos.write(data);
                    fos.close();
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Start recording video
     */
    public void startRecord() {
        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }

    /**
     * Stop recording video
     */
    public void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
        }
    }

    private boolean prepareVideoRecorder() {
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        videoFile = new File(sdCardDir, "myvideo" + dateFormat.format(new Date()) + ".3gp");
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }
}
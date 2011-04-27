package com.android.camera.functional;

import com.android.camera.Camera;
import com.android.camera.R;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.KeyEvent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class ImageCaptureIntentTest extends ActivityInstrumentationTestCase2 <Camera> {
    private static final String TAG = "ImageCaptureIntentTest";
    private Intent mIntent;

    public ImageCaptureIntentTest() {
        super(Camera.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    @LargeTest
    public void testNoExtraOutput() throws Exception {
        setActivityIntent(mIntent);
        getActivity();

        takePicture();
        pressDone();

        assertTrue(getActivity().isFinishing());
        assertEquals(Activity.RESULT_OK, getActivity().getResultCode());
        Intent resultData = getActivity().getResultData();
        Bitmap bitmap = (Bitmap) resultData.getParcelableExtra("data");
        assertNotNull(bitmap);
        assertTrue(bitmap.getWidth() > 0);
        assertTrue(bitmap.getHeight() > 0);
    }

    @LargeTest
    public void testExtraOutput() throws Exception {
        File file = new File(Environment.getExternalStorageDirectory(),
            "test.jpg");
        BufferedInputStream stream = null;
        byte[] jpegData;

        try {
            mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            setActivityIntent(mIntent);
            getActivity();

            takePicture();
            pressDone();

            assertTrue(getActivity().isFinishing());
            assertEquals(Activity.RESULT_OK, getActivity().getResultCode());

            // Verify the jpeg file
            int fileLength = (int) file.length();
            assertTrue(fileLength > 0);
            jpegData = new byte[fileLength];
            stream = new BufferedInputStream(new FileInputStream(file));
            stream.read(jpegData);
        } finally {
            if (stream != null) stream.close();
            file.delete();
        }

        Bitmap b = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        assertTrue(b.getWidth() > 0);
        assertTrue(b.getHeight() > 0);
    }

    @LargeTest
    public void testRetake() throws Exception {
        setActivityIntent(mIntent);
        getActivity();

        takePicture();
        pressRetake();
        takePicture();
        pressDone();

        assertTrue(getActivity().isFinishing());
        assertEquals(Activity.RESULT_OK, getActivity().getResultCode());
        Intent resultData = getActivity().getResultData();
        Bitmap bitmap = (Bitmap) resultData.getParcelableExtra("data");
        assertNotNull(bitmap);
        assertTrue(bitmap.getWidth() > 0);
        assertTrue(bitmap.getHeight() > 0);
    }

    @LargeTest
    public void testCancel() throws Exception {
        setActivityIntent(mIntent);
        getActivity();

        pressCancel();

        assertTrue(getActivity().isFinishing());
        assertEquals(Activity.RESULT_CANCELED, getActivity().getResultCode());
    }

    @LargeTest
    public void testSnapshotCancel() throws Exception {
        setActivityIntent(mIntent);
        getActivity();

        takePicture();
        pressCancel();

        assertTrue(getActivity().isFinishing());
        assertEquals(Activity.RESULT_CANCELED, getActivity().getResultCode());
    }

    private void takePicture() throws Exception {
        getInstrumentation().sendKeySync(
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FOCUS));
        getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
        Thread.sleep(4000);
    }

    private void pressDone() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                getActivity().findViewById(R.id.btn_done).performClick();
            }
        });
    }

    private void pressRetake() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                getActivity().findViewById(R.id.btn_retake).performClick();
            }
        });
    }

    private void pressCancel() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                getActivity().findViewById(R.id.btn_cancel).performClick();
            }
        });
    }
}
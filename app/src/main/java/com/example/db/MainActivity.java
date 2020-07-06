package com.example.db;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import com.example.db.fragment.SketchFragment;
import com.itheima.library.PhotoView;

import java.io.File;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity {
    private final static String FRAGMENT_TAG = "SketchFragmentTag";


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CAMERA =2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private PhotoView mPhotoView;
    private AppCompatTextView appCompatTextView;



    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission( activity, Manifest.permission.WRITE_EXTERNAL_STORAGE );
        int permission1 = ActivityCompat.checkSelfPermission( activity, Manifest.permission.CAMERA );

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the u
            ActivityCompat.requestPermissions( activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE );

        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( activity, PERMISSIONS_STORAGE, REQUEST_CAMERA );
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        verifyStoragePermissions( this );
        ft.add( R.id.fl_main, new SketchFragment(), FRAGMENT_TAG ).commit();
        verifyStoragePermissions( this );
//        startCamera();
        // 解决相机找不到图片路径问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
    }

    // 添加右上角的actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 这里是调用menu文件夹中的main.xml，在主界面label右上角的三点里显示其他功能
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected( item );
        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag( FRAGMENT_TAG );
        return f.onOptionsItemSelected( item );
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag( FRAGMENT_TAG );
        f.onActivityResult( requestCode, resultCode, data );

        if (requestCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get( "data" );
        }

    }

    /**
     * 打开相机
     */
   /* public void startCamera() {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        startActivityForResult( intent, 1 );

    }*/


    // 启用图片缩放功能
//    photoView.enable();
//    // 禁用图片缩放功能 (默认为禁用，会跟普通的ImageView一样，缩放功能需手动调用enable()启用)
//    photoView.disenable();
//    // 获取图片信息
//    Info info = photoView.getInfo();
//    // 从普通的ImageView中获取Info
//    Info info = PhotoView.getImageViewInfo(ImageView);

}

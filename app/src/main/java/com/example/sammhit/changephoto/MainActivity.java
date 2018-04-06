package com.example.sammhit.changephoto;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{
    Bitmap bmp,bmp_original;
    EditText editText;
    String text;
    ImageView imageView;
    Intent intent;


    public void addImage(View view) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 23);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==1 && resultCode==RESULT_OK && data!=null){

            try {
                Uri selectedImage=data.getData();
                bmp_original = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                imageView.setImageBitmap(bmp_original);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap writeTextOverBitmap(){

        //Text from EditText
        text=editText.getText().toString();

        float scale=getApplicationContext().getResources().getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig=bmp_original.getConfig();
        //Set default config if null
        if(bitmapConfig==null){
            bitmapConfig= Bitmap.Config.ARGB_8888;

        }
        //Convert Resource immutable bitmap to mutable
        bmp=bmp_original.copy(bitmapConfig,true);

        //Create canvas
        Canvas canvas=new Canvas(bmp);

        //new antialiased paint
        TextPaint txtpaint= new TextPaint(Paint.ANTI_ALIAS_FLAG);
        txtpaint.setColor(Color.WHITE);
        txtpaint.setTextSize(60.0f*(float)scale);
        //set Text Width to Canvas width minus 40dp padding
        int textWidth=canvas.getWidth()-(int)(40*scale);
        StaticLayout textlayout= new StaticLayout(
                text,
                txtpaint,
                textWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false
        );

        int textHeight=textlayout.getHeight();

        //get position of text's top left corner
        float x=(bmp.getWidth()-textWidth)/2;
        float y=(bmp.getHeight()-textHeight)/2;


        //draw text to canvas center
        canvas.save();
        canvas.translate(x,y);
        textlayout.draw(canvas);
        canvas.restore();


        return bmp;
    }

    //onclick Change method
    public void change(View view){
          storeImage(writeTextOverBitmap());
          imageView.setImageBitmap(bmp);


    }

    public  void clear(View view){
        editText.setText("");
        imageView.setImageBitmap(bmp_original);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 23: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,1);
                    storeImage(writeTextOverBitmap());;

                } else {
                    Toast.makeText(MainActivity.this,"Permission NOt allowed",Toast.LENGTH_SHORT).show();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                return;
            }
        }
    }

    //Create a file to save the bitmap image
    private File getOutputMediaFile(){
        String timeStamp=new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        String mImageName="MI_"+ timeStamp +".png";
        File mediaStorageDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


        //Create directory if it doesn't exist`
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.i("Make Dir","could not make directory");
                return null;

            }
        }
        File mediFile=new File(mediaStorageDir,mImageName);
        return mediFile;




    }



    private void storeImage(Bitmap imageFile){

        File pictureFile= getOutputMediaFile();
        if (pictureFile==null){
            Log.i("error","Couldnot create file");
        }
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(pictureFile);
            imageFile.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            fileOutputStream.close();
            Toast.makeText(MainActivity.this, "Done!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.i("File Not Found",e.getMessage());
        } catch (IOException e) {
            Log.i("Error accessing File",e.getMessage());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=(EditText)findViewById(R.id.editText);
        imageView=(ImageView)findViewById(R.id.imageView);

    }
}

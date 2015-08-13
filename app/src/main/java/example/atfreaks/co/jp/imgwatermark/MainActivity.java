package example.atfreaks.co.jp.imgwatermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    String imageUrl;
    public static String storagePath = Environment.getExternalStorageDirectory().toString();
    //photo for repost
    Bitmap bm;
    //profile photo for watermark
    Bitmap bm2;
    public static ImageView mImageView;
    public static ImageView mImageViewBefore;
    public static TextView mBeforeTxt;
    public static TextView mAfterTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File imgFile = new  File(storagePath + "test2.jpg");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mBeforeTxt = (TextView) findViewById(R.id.before_txt);
        mAfterTxt = (TextView) findViewById(R.id.after_txt);

        bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        if (imgFile.exists()){
            mImageView.setImageBitmap(bm); // Load image into ImageView
        } else {
            Toast.makeText(this, "downloading and creating watermark...", Toast.LENGTH_LONG);
        }

        new DownloadImageTask().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void drawRectangleAtBottom(int fillHeight, int imageWidth, int imageHeight, Canvas canvas){

        Paint rectangle = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectangle.setStrokeWidth(2);
        rectangle.setColor(android.graphics.Color.RED);
        rectangle.setStyle(Paint.Style.FILL_AND_STROKE);
        rectangle.setAntiAlias(true);
        rectangle.setColor(Color.DKGRAY);
        rectangle.setAlpha(80);
        //points
        Point point1_draw = new Point();
        Point point2_draw = new Point();
        Point point3_draw = new Point();
        Point point4_draw = new Point();

        point1_draw.set(imageWidth,imageHeight);
        point2_draw.set(0,imageHeight);
        point3_draw.set(0,imageHeight - fillHeight);
        point4_draw.set(imageWidth,imageHeight - fillHeight);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        //drawing
        path.moveTo(point1_draw.x,point1_draw.y);
        path.lineTo(point2_draw.x,point2_draw.y);
        path.lineTo(point3_draw.x,point3_draw.y);
        path.lineTo(point4_draw.x,point4_draw.y);
        path.lineTo(point1_draw.x,point1_draw.y);
        path.close();

        //drawRectangle
        canvas.drawPath(path, rectangle);
    }

    /**
     * description: Creates a watermark to every bitmap given.
     *
     * @param src (source photo or the main photo this will be the image to be stamped on)
     * @param profile (the profile photo of the uploader, or any secondary image that will be stamped on the src photo)
     * @param watermark (watermark text)
     * @param location (the point to where to put the text)
     * @param alpha (transparency of the watermark)
     * @param size (size of the text on the watermark)
     * @param underline (do you want the text to be underlined? true or false)
     * @return
     */
    public static Bitmap mark(Bitmap src,Bitmap profile, String watermark, Point location, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        int fill_height = 80;
        int secondaryPicHeight = 70;
        int secondaryPicWidth = 70;
        //position of text after the image
        float txtPosition;

        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(false);

        //create the image paint
        Paint prof = new Paint(Paint.ANTI_ALIAS_FLAG);
        prof.setStrokeWidth(2);
        prof.setColor(android.graphics.Color.RED);
        prof.setAntiAlias(true);

        //resizing the profile photo
        int profWidth = 150;
        int profHeight = 150;
        float scaleWidth = ((float) secondaryPicWidth) /profile.getWidth();
        float scaleHeight = ((float) secondaryPicHeight) / profile.getHeight();
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        Log.i("DownloadEx","scaleX:" + scaleWidth);
        Log.i("DownloadEx","scaleY:" + scaleHeight);
        matrix.postScale(scaleWidth,scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(profile, 0, 0, profWidth, profHeight,
                matrix, false);

        //draw Rectangle at the bottom
        drawRectangleAtBottom(fill_height,w,h,canvas);
        //to know what position our text would be lets multiply the prof width to its scaling and add the margin in this case margin is 20
        txtPosition = (float)(profWidth * scaleWidth) + 20;
        canvas.drawText(watermark, txtPosition  , (h- fill_height) + 30, paint);
        canvas.drawText("Wonderful place", txtPosition , (h- fill_height) + 60, paint);
        canvas.drawBitmap(resizedBitmap, 10, (h- fill_height)+5, prof);

        mImageView.setImageBitmap(result);
        mAfterTxt.setText("Image after the watermark");
        return result;
    }

    class DownloadImageTask extends AsyncTask<Void, Void, List<Bitmap>> {

        private Exception exception;

        protected List<Bitmap> doInBackground(Void... voids) {
            try {
                List<Bitmap> bitmaps = new ArrayList<>();

                bm = getBitmapFromURL("https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/s640x640/sh0.08/e35/10732032_711829882282941_1891477841_n.jpg");
                Log.i("DownloadEx", "image downloading...");
                bm2 = getBitmapFromURL("https://igcdn-photos-d-a.akamaihd.net/hphotos-ak-xpf1/t51.2885-19/s150x150/11189132_1443169062676187_1025831344_a.jpg");
                Log.i("DownloadEx", "image 2 downloading...");


                bitmaps.add(bm);
                bitmaps.add(bm2);

                return  bitmaps;
            } catch (Exception e) {
                Log.i("DownloadEx",e.getMessage());
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(List<Bitmap> bitmaps) {
            // TODO: check this.exception
            // TODO: do something with the bm
            try{

                OutputStream fOut = null;
                File file = new File(storagePath, "test2"+".jpg"); // the File to save to
                fOut = new FileOutputStream(file);

                //load to imageview
                ImageView mImageViewBefore = (ImageView) findViewById(R.id.imageViewBefore)  ;
                mImageViewBefore.setImageBitmap(bm);

                mBeforeTxt.setText("Image Before the watermark");
                mAfterTxt.setText("Processing...");

                bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush();
                fOut.close(); // do not forget to close the stream

                String path = MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
                Log.i("DownloadEx",path);
                Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG);
                Point a = new Point(100,25);
                Bitmap bm_watermark;

                bm_watermark = mark(bm, bm2, "@emilenriquez", a, 100, 20, true);
                OutputStream fOut2 = null;
                File file2 = new File(storagePath, "test2"+".jpg"); // the File to save to
                fOut2 = new FileOutputStream(file);

                bm_watermark.compress(Bitmap.CompressFormat.JPEG, 100, fOut2); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                String path2 = MediaStore.Images.Media.insertImage(getContentResolver(),file2.getAbsolutePath(),"watermark","watermark");
                fOut2.flush();
                fOut2.close(); // do not forget to close the stream


            } catch (Exception e){
                Log.i("DownloadEx",e.getMessage());

            }


        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("DownloadEx",e.getMessage());
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}

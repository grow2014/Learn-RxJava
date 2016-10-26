package pro.kinect.lrxj;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BackgroundActivity extends AppCompatActivity {

    private String[] backgrounds;
    private int position;
    private ImageView ivBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Custom", "BackgroundActivity onCreate");
        setContentView(R.layout.activity_background);
        ivBackground = (ImageView) findViewById(R.id.ivBackground);


        /*Техзадание:
        0. Есть список картинок и состояние последней установленной картинки.
        1. При открытии Активити загружаем с сервера текущую по списку картинку, масштабируем и отображаем.
        2. При нажатии на кнопку "Next" загружаем следующую по списку картинку, масштабируем, отображаем, удаляем предыдущую.
        */


        backgrounds = getResources().getStringArray(R.array.backgrounds); //берём массив адресов картинок
        position = (int) (Math.random() * (backgrounds.length - 1)); //получаем первую позицию

        loadImageFromUrl(position); //загружаем картинку по этой позиции

    }


    private void loadImageFromUrl(int position){
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
//                Log.d("Custom", "Start loading");
                Bitmap result = null;
                try {
                    URL url = new URL(backgrounds[position]);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
                    // Raw height and width of image
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;

//                    Log.d("Custom", "imageWidth = " + imageWidth + ", imageHeight = " + imageHeight);

                    //Scaling

                    //Get the dimensions of the Screen
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    int screenHeight = size.y;

//                    Log.d("Custom", "screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

                    int scaleSize = 1;

                    if (imageHeight > screenWidth || imageWidth > screenHeight) {

                        final int halfHeight = imageHeight / 2;
                        final int halfWidth = imageHeight / 2;

                        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                        // height and width larger than the requested height and width.
                        while ((halfHeight / scaleSize) >= screenHeight
                                && (halfWidth / scaleSize) >= screenWidth) {
                            scaleSize *= 2;
                        }
                    }

//                    Log.d("Custom", "scaleSize = " + scaleSize);
                    options.inSampleSize = scaleSize;

                    // Decode bitmap with calculateSize set
                    options.inJustDecodeBounds = false;

                    result = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.e("Custom", "IOException error " + e.getMessage());
                }

//                if (result != null)
//                Log.d("Custom", "new imageWidth = " + result.getWidth() + ", new imageHeight = " + result.getHeight());

                return result;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) ivBackground.setImageBitmap(bitmap);
//                else Log.e("Custom", "Bitmap is empty");
            }
        }.execute((Void) null);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnNext :
                if (position < backgrounds.length - 2) loadImageFromUrl(++position);
                else loadImageFromUrl(position = 0);
            default: break;
        }
    }
}

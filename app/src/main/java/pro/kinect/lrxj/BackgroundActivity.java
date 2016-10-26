package pro.kinect.lrxj;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackgroundActivity extends AppCompatActivity {

    private String[] backgrounds;
    private int position;
    private ImageView ivBackground;
    private Observable<Bitmap> bitmapObservable;
    private Subscriber<Bitmap> subscriber;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Custom", "BackgroundActivity onCreate");
        setContentView(R.layout.activity_background);
        ivBackground = (ImageView) findViewById(R.id.ivBackground);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        backgrounds = getResources().getStringArray(R.array.backgrounds); //берём массив адресов картинок
        position = (int) (Math.random() * (backgrounds.length - 1)); //получаем первую позицию

        createObservable();
    }

    private void createObservable() {
        //Observable, действия которого основаны на переданной ему Observable.OnSubscribe<Bitmap>
        bitmapObservable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Log.d("Custom", "bitmapObservable -> call()");

                //сообщить сабскрайберу о том, что есть новые данные
                try {
                    subscriber.onNext(loadingBitmap(getPosition()));
                } catch (IOException e) {
                    subscriber.onError(e);
                }

                //А теперь сообщаем о том, что мы закончили и данных больше нет
                subscriber.onCompleted();
            }
        });
    }

    private Subscriber<Bitmap> getSubscriber() {
        return new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e("Custom", "subscriber onError() -> " + e.getMessage());
                progress_bar.setVisibility(View.GONE);

                stopLoading();
            }

            @Override
            public void onNext(Bitmap bitmap) {
                ivBackground.setImageBitmap(bitmap);
            }
        };
    }


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnStop:
                stopLoading();
                break;
            case R.id.btnStart:
                startLoading();
            default: break;
        }
    }

    private void startLoading() {
        Log.d("Custom", "startLoading");
        progress_bar.setVisibility(View.VISIBLE);

        if (subscriber == null) {
            subscriber = getSubscriber();

            bitmapObservable
                    .delay(1500, TimeUnit.MILLISECONDS)
                    .retryWhen(errors -> errors.flatMap(error -> {
                        // For IOExceptions, we  retry
                        if (error instanceof IOException) {
                            return Observable.just(null);
                        }

                        // For anything else, don't retry
                        return Observable.error(error);
                    }))
                    .subscribeOn(Schedulers.io()) //отдаем IO тред для работы в background
                    .observeOn(AndroidSchedulers.mainThread()) //говорим, что обсервить хотим в main thread
                    .repeat()
                    .subscribe(subscriber);
        }
    }

    /**
     * Remove the subscriber
     */
    private void stopLoading() {
        Log.d("Custom", "stopLoading");
        if (subscriber != null) {
            subscriber.unsubscribe();
            subscriber = null;
        }
    }

    /** Get next number of position
     * @return positions' number from backgrounds[]
     */
    private int getPosition() {
        if (position < backgrounds.length - 2) return (++position);
        else return (position = 0);
    }


    /**
     * Logic of downloading
     *
     * @param position number of pictures from backgrounds[]
     * @return Bitmap will be loaded and scaled
     * @throws IOException
     */
    private Bitmap loadingBitmap(int position) throws IOException {
        URL url = new URL(backgrounds[position]);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Log.d("Custom", "loadingBitmap() - getInputStream() from url " + url.toString());
        BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
        // Raw height and width of image
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;


        //Scaling
        //Get the dimensions of the Screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int scaleSize = 1;
        if (imageHeight > screenWidth || imageWidth > screenHeight) {
            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageHeight / 2;

            // Calculate the largest scaleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / scaleSize) >= screenHeight
                    && (halfWidth / scaleSize) >= screenWidth) {
                scaleSize *= 2;
            }
        }
        options.inSampleSize = scaleSize;

        // Decode bitmap with calculateSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
    }


    /**
     * Start download when Activity is opened
     */
    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    /**
     * Stop download when Activity lost focus
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopLoading();
    }
}

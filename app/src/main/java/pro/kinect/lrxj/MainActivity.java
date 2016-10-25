package pro.kinect.lrxj;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import rx.Observable;
import rx.Subscriber;

import static android.support.v4.util.PatternsCompat.EMAIL_ADDRESS;


public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private EditText etPassword;
    private EditText etEmail;
    private Observable<Boolean> emailValid;
    private Observable<Boolean> passwordValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword.setOnEditorActionListener(this);
        etEmail = (EditText) findViewById(R.id.etEmail);

        createObservables();
    }

    private void createObservables() {
        emailValid =
                Observable
                        .create(new Observable.OnSubscribe<CharSequence>() {
                            @Override
                            public void call(Subscriber<? super CharSequence> subscriber) {
                                etEmail.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        subscriber.onNext(s);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                });
                            }
                        })
                        .map(t -> EMAIL_ADDRESS.matcher(t).matches());
        emailValid
                .map(b -> b ? Color.BLACK : Color.RED)
                .subscribe(c -> etEmail.setTextColor(c));




        passwordValid = Observable.create(new Observable.OnSubscribe<CharSequence>() {
            @Override
            public void call(Subscriber<? super CharSequence> subscriber) {
                etPassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        subscriber.onNext(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
        }).map(t -> t.length() > 6);
        passwordValid.map(b -> b ? Color.BLACK : Color.RED)
                .subscribe(c -> etPassword.setTextColor(c));
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSend:
                showMessage(getString(R.string.button_pressed));
                break;
            default:
                break;
        }
    }

    private void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            onClick(findViewById(R.id.btnSend));
        }
        return false;
    }
}

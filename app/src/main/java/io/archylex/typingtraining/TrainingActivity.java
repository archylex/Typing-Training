package io.archylex.typingtraining;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;


public class TrainingActivity extends AppCompatActivity {
    private int half_length_text = 7;
    private String lesson = "";
    private int numChar = 0;
    private TextView tv;
    private long startTimer = 0;
    private TextView tvTime;
    private int  wrongChar = 0;
    private int successChair = 0;
    private TextView tvScore;
    private int timer_seconds = 1;
    private Button stopButton;
    private boolean isStarted = false;

    Handler timerHandler = new Handler();

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTimer;
            timer_seconds = (int) (millis / 1000);
            int minutes = timer_seconds / 60;
            int seconds = timer_seconds % 60;

            tvTime.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_training);

        lesson = readLesson(getIntent().getStringExtra("filename"));

        tv = findViewById(R.id.fullscreen_content);
        tv.setText(lesson);

        tvTime = findViewById(R.id.fullscreen_timer);

        tvScore = findViewById(R.id.fullscreen_score);

        updateText();

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(onStartStopTraining);
        stopButton.setFocusable(true);

        Button quitButton = findViewById(R.id.quit_button);
        quitButton.setOnClickListener(onQuit);
        quitButton.setFocusable(false);

        String[] enKeysArray = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "\\", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/"};
        String[] ruKeysArray = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "\\", "Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ", "Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д", "Ж", "Э", "Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю", "."};

        SharedPreferences prefs = this.getSharedPreferences("typingtraining", Context.MODE_PRIVATE);

        String lang = prefs.getString("lang", "rus");
        boolean isHands = prefs.getBoolean("hands", false);

        switch (lang) {
            case "rus":
                createKeyboard(ruKeysArray, isHands);
                break;
            case "eng":
                createKeyboard(enKeysArray, isHands);
                break;
        }

        lightKey(lesson.substring((numChar > 0) ? numChar : 0, numChar + 1).charAt(0), true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (isStarted) {
                return false;
            } else {
                stopButton.clearFocus();
                tv.requestFocus();
                startStopTraining();
                return super.onKeyDown(keyCode, event);
            }
        } else if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (isStarted) {
                startStopTraining();
                return super.onKeyDown(keyCode, event);
            } else {
                escapeToMain();
                return false;
            }
        } else {
            if (isStarted) {
                if ((char) event.getUnicodeChar() == lesson.charAt(numChar)) {
                    lightKey(lesson.substring((numChar > 0) ? numChar : 0, numChar + 1).charAt(0), false);
                    numChar++;
                    successChair++;
                    updateText();
                    lightKey(lesson.substring((numChar > 0) ? numChar : 0, numChar + 1).charAt(0), true);
                } else {
                    wrongChar++;
                    Toast.makeText(this, getResources().getString(R.string.wrong_result) + "!", Toast.LENGTH_SHORT).show();
                }

                updateScore();
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    private String readLesson(String file) {
        String str = "";
        try {
            InputStream inStream = getAssets().open(file);
            int size = inStream.available();
            byte[] buffer = new byte[size];
            inStream.read(buffer);
            str = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    private void updateText() {
        if (numChar >= lesson.length())
            numChar = 0;

        int startpos = (numChar < half_length_text) ? 0 : numChar - half_length_text;
        int endpos = (numChar + half_length_text > lesson.length()) ? lesson.length() : numChar + half_length_text;

        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1= new SpannableString(lesson.substring(startpos, numChar));
        str1.setSpan(new ForegroundColorSpan(Color.GRAY), 0, str1.length(), 0);
        builder.append(str1);

        SpannableString str2= new SpannableString("|");
        str2.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, str2.length(), 0);
        builder.append(str2);

        SpannableString str3= new SpannableString(lesson.substring(numChar, endpos));
        str3.setSpan(new ForegroundColorSpan(Color.WHITE), 0, str3.length(), 0);
        builder.append(str3);

        tv.setText( builder, TextView.BufferType.SPANNABLE);
    }

    private void updateScore() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1= new SpannableString(Integer.toString(wrongChar));
        str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
        builder.append(str1);

        SpannableString str2= new SpannableString("/");
        str2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, str2.length(), 0);
        builder.append(str2);

        SpannableString str3= new SpannableString(Integer.toString(successChair));
        str3.setSpan(new ForegroundColorSpan(Color.GREEN), 0, str3.length(), 0);
        builder.append(str3);

        tvScore.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private Point getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void createKeyboard(String[] textArray, boolean isHands) {
        LinearLayout linearLayout = findViewById(R.id.keyboard);
        int k = 13;
        int length = (int)(getScreenSize().y * 1.5f) / (k + k/4 + 1/4);

        LinearLayout[] keylines = {new LinearLayout(this), new LinearLayout(this), new LinearLayout(this), new LinearLayout(this), new LinearLayout(this)};

        for (int a = 0; a < keylines.length; a++) {
            keylines[a].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            keylines[a].setOrientation(LinearLayout.HORIZONTAL);
            keylines[a].setGravity(Gravity.CENTER);
            linearLayout.addView(keylines[a]);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, length / 16, 0, length / 16);
            keylines[a].setLayoutParams(params);
        }

        for (int a = 0; a < textArray.length; a++) {
            int ca = a + 1;
            int line = 0;
            while (ca > 0) {
                ca -= k - line;
                line++;
            }

            line -= 1;

            TextView textView = new TextView(this);
            textView.setText(textArray[a]);
            textView.setBackgroundResource(R.drawable.key);
            textView.setTextColor(Color.parseColor("#ff207d94"));
            textView.setWidth(length);
            textView.setHeight(length);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, length / 4);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setTag("mykey_" + textArray[a]);

            keylines[line].addView(textView);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(length / 16, 0, length / 16, 0);
            textView.setLayoutParams(params);
        }

        TextView textView = new TextView(this);
        textView.setText("■■■■■■■");
        textView.setBackgroundResource(R.drawable.key);
        textView.setTextColor(Color.parseColor("#00207d94"));
        textView.setWidth(6 * length);
        textView.setHeight(length);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, length / 4);
        textView.setGravity(Gravity.CENTER);
        textView.setTag("mykey_space");
        keylines[4].addView(textView);

        if (isHands) {
            ImageView leftHand = findViewById(R.id.left_hand);
            leftHand.setBackgroundResource(R.drawable.left_hand);
            leftHand.getLayoutParams().width = 23 * length / 4;
            leftHand.getLayoutParams().height = 14 * length / 4;
            leftHand.setX(10 * length / 4);
            leftHand.setY(2 * length / 4);

            ImageView rightHand = findViewById(R.id.right_hand);
            rightHand.setBackgroundResource(R.drawable.right_hand);
            rightHand.getLayoutParams().width = 23 * length / 4;
            rightHand.getLayoutParams().height = 14 * length / 4;
            rightHand.setX(-14 * length / 4);
            rightHand.setY(2 * length / 4);
        }
    }

    private void lightKey(char key, boolean on) {
        String keyTag = "";

        if (key == ' ') {
            keyTag = "space";
        } else {
            keyTag = String.valueOf(key).toUpperCase();
        }

        TextView ttt = this.findViewById(android.R.id.content).findViewWithTag("mykey_" + keyTag);

        if (ttt != null) {
            if (on) {
                ttt.setTextColor(Color.parseColor("#ffffffff"));
            } else {
                if (key != ' ')
                    ttt.setTextColor(Color.parseColor("#ff207d94"));
                else
                    ttt.setTextColor(Color.parseColor("#0000ccff"));
            }
        }
    }

    private void showResults() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.title_result);

        float speed = (float) successChair / (float) (timer_seconds > 0 ? timer_seconds : 1) * 60;

        alert.setMessage(getResources().getString(R.string.saccess_result) + ": " + Integer.toString(successChair) + "\n" +
                getResources().getString(R.string.wrong_result) + ": " + Integer.toString(wrongChar) + "\n" +
                getResources().getString(R.string.speed_result) + ": " + Integer.toString(Math.round(speed)) + " " +
                getResources().getString(R.string.speed_meters_result));

        alert.setPositiveButton(R.string.close_button_result, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private final View.OnClickListener onStartStopTraining = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startStopTraining();
        }
    };

    private final View.OnClickListener onQuit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            escapeToMain();
        }
    };

    private void escapeToMain() {
        this.finish();
    }

    private void startStopTraining() {
        if (isStarted) {
            isStarted = false;
            stopButton.setText(R.string.start_button_training);
            lightKey(lesson.substring((numChar > 0) ? numChar : 0, numChar + 1).charAt(0), false);
            showResults();
            startTimer = 0;
            timerHandler.removeCallbacks(timerRunnable);
            numChar = 0;
            wrongChar = 0;
            successChair = 0;
            tv.setFocusable(false);
            stopButton.setFocusable(true);
        } else {
            isStarted = true;
            stopButton.setText(R.string.stop_button_training);
            startTimer = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            updateText();
            updateScore();
            lightKey(lesson.substring((numChar > 0) ? numChar : 0, numChar + 1).charAt(0), true);
            stopButton.setFocusable(false);
            tv.setFocusable(true);
        }
    }
}

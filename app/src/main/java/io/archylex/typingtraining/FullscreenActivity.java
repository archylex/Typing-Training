package io.archylex.typingtraining;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class FullscreenActivity extends AppCompatActivity {
    private Spinner lessonsSpinner;
    Map<String, String> lessonsMap = new LinkedHashMap<>();

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

        setContentView(R.layout.activity_fullscreen);

        findViewById(R.id.start_training).setOnClickListener(onStartTraining);

        findViewById(R.id.settings_training).setOnClickListener(onStartSettings);

        lessonsSpinner = (Spinner) findViewById(R.id.lesson_spinner);
        updateSpinner();

        updateAttentionLayout();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateSpinner();
    }

    public void updateSpinner() {
        SharedPreferences prefs = this.getSharedPreferences("typingtraining", Context.MODE_PRIVATE);
        String lang = prefs.getString("lang", "rus");

        lessonsMap = getLessonsList(lang);

        if (!lessonsMap.isEmpty()) {
            Set<String> keys = lessonsMap.keySet();
            String[] lessonsArray = keys.toArray(new String[keys.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, lessonsArray);
            adapter.setDropDownViewResource(R.layout.lessons_spinner);
            lessonsSpinner.setAdapter(adapter);
        }
    }

    private void updateAttentionLayout() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodSubtype ims = imm.getCurrentInputMethodSubtype();
        String locale = ims.getLocale();

        TextView attentionLayout = findViewById(R.id.keyboard_attention);
        attentionLayout.setText("Your keyboard layout is \"" + locale + "\"");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE)
            updateAttentionLayout();

        return super.onKeyDown(keyCode, event);
    }

    private final View.OnClickListener onStartTraining = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startTraining(view);
        }
    };

    private final View.OnClickListener onStartSettings = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startSettings(view);
        }
    };

    public void startTraining(View view) {
        Intent intent = new Intent(this, TrainingActivity.class);
        String value = lessonsSpinner.getSelectedItem().toString();
        intent.putExtra("filename", lessonsMap.get(value));
        startActivity(intent);
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public Map<String, String> getLessonsList(String value) {
        Map<String, String> resMap = new LinkedHashMap<>();

        try {
            JSONArray jArray = new JSONArray(readJSONFromAsset());
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObj = jArray.getJSONObject(i);
                String lang = jObj.getString("lang");
                if (lang.equalsIgnoreCase(value)) {
                    JSONArray contentArray = jObj.getJSONArray("content");
                    for (int g = 0; g < contentArray.length(); g++) {
                        JSONObject contentObj = contentArray.getJSONObject(g);
                        resMap.put(contentObj.getString("title"), contentObj.getString("filename"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resMap;
    }

    private String readJSONFromAsset() {
        String json = null;

        try {
            InputStream is = getAssets().open("lessons.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }
}

package io.archylex.typingtraining;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;


public class SettingsActivity extends AppCompatActivity {
    private Spinner lang_spinner;
    private CheckBox hands_box;

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

        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = this.getSharedPreferences("typingtraining", Context.MODE_PRIVATE);

        findViewById(R.id.quit_from_settings).setOnClickListener(onEscapeToMain);

        lang_spinner = (Spinner) findViewById(R.id.lang_settings_spinner);

        String[] lessonsArray = {"rus", "eng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lessonsArray);
        adapter.setDropDownViewResource(R.layout.lessons_spinner);
        lang_spinner.setAdapter(adapter);

        String lang = prefs.getString("lang", "rus");
        int spinnerPosition = adapter.getPosition(lang);
        lang_spinner.setSelection(spinnerPosition);

        hands_box = findViewById(R.id.hands_settings_box);
        hands_box.setChecked(prefs.getBoolean("hands", false));
    }

    private final View.OnClickListener onEscapeToMain = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            escapeToMain(view);
        }
    };

    public void escapeToMain(View view) {
        saveSettings();
        this.finish();
    }

    private void saveSettings() {
        SharedPreferences prefs = this.getSharedPreferences("typingtraining", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lang", lang_spinner.getSelectedItem().toString());
        editor.putBoolean("hands", hands_box.isChecked());
        editor.commit();
    }
}

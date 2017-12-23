package javinator9889.bitcoinpools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * Created by Javinator9889 on 22/12/2017.
 */

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String FILENAME = "usrPref.dat";
    private static int ACTUAL = 1;
    private static int NEW_VALUE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setTitle(R.string.settingsTitle);

        /*File file = new File(this.getFilesDir(), FILENAME);
        file.setReadable(true);
        file.setWritable(true);*/

        TextView tv = (TextView) findViewById(R.id.textView5);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner2);

        try {
            FileInputStream inputStream = openFileInput(FILENAME);
            ACTUAL = inputStream.read();
            inputStream.close();
        } catch (IOException e) {
        }

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(ACTUAL - 1);
        String output = getString(R.string.cDays);
        tv.setText(output);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        NEW_VALUE = Integer.parseInt(parent.getItemAtPosition(position).toString());
        try {
            FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(Integer.parseInt(String.valueOf(parent.getItemAtPosition(position))));
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onBackPressed() {
        refresh();
        if (NEW_VALUE != ACTUAL) {
            Toast.makeText(this, R.string.prefUpdated, Toast.LENGTH_LONG).show();
        }
    }

    private void refresh() {
        Intent intentMain = new Intent(SpinnerActivity.this, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        SpinnerActivity.this.finish();
    }
}

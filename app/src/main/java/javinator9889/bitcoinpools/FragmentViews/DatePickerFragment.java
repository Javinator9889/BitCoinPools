package javinator9889.bitcoinpools.FragmentViews;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Javinator9889 on 29/01/2018.
 * Based on: https://android--code.blogspot.com.es/2015/08/android-datepickerdialog-set-max-date.html
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private int year;
    private int month;
    private int day;
    private boolean date_set = false;

    public DatePickerFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        final Calendar limitDate = Calendar.getInstance();
        limitDate.set(2010, 7, 17);

        if (!this.date_set) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH);
            this.day = calendar.get(Calendar.DAY_OF_MONTH);
            this.date_set = true;
        }

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, this.year, this.month, this.day);
        calendar.add(Calendar.DATE, 3);

        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dialog.getDatePicker().setMinDate(limitDate.getTimeInMillis());

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Toast.makeText(view.getContext(), "Day/Month/Year: " + day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
        this.day = day;
        this.month = month;
        this.year = year;
        this.date_set = true;
        //Tab2BTCChart.forceReload();
    }

    public String getDateFormatted() {
        if (this.date_set)
            return this.year + "-" + this.month + "-" + this.day;
        else
            return null;
    }
}

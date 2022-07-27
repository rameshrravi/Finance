package com.psr.financial.Utility;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Locale;

public class DateTextWatcher implements TextWatcher {

    /*private String current = "";
    private String ddmmyyyy = "DDMMYYYY";
    private Calendar cal = Calendar.getInstance();
    private EditText input;

    public DateTextWatcher(EditText input) {
        this.input = input;
        this.input.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().equals(current)) {
            return;
        }
        int pos = input.getSelectionEnd();
        String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
        String cleanC = current.replaceAll("[^\\d.]|\\.", "");

        int cl = clean.length();
        int sel = cl;
        for (int i = 2; i <= cl && i < 6; i += 2) {
            sel++;
            //pos++;
        }
        //Fix for pressing delete next to a forward slash
        if (clean.equals(cleanC)) {
            sel--;
            //pos--;
        }

        if (clean.length() < 8){
            clean = clean + ddmmyyyy.substring(clean.length());
        }else{
            //This part makes sure that when we finish entering numbers
            //the date is correct, fixing it otherwise
            int day  = Integer.parseInt(clean.substring(0,2));
            int mon  = Integer.parseInt(clean.substring(2,4));
            int year = Integer.parseInt(clean.substring(4,8));

            mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
            cal.set(Calendar.MONTH, mon-1);
            year = (year<1900)?1900:(year>2100)?2100:year;
            cal.set(Calendar.YEAR, year);
            // ^ first set year for the line below to work correctly
            //with leap years - otherwise, date e.g. 29/02/2012
            //would be automatically corrected to 28/02/2012

            day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
            clean = String.format("%02d%02d%02d",day, mon, year);
        }

        clean = String.format("%s/%s/%s", clean.substring(0, 2),
                clean.substring(2, 4),
                clean.substring(4, 8));

        sel = sel < 0 ? 0 : sel;
        current = clean;
        input.setText(current);
        input.setSelection(pos < current.length() ? pos : current.length()); //sel < current.length() ? sel : current.length());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }*/

    private static final int MAX_LENGTH = 8;
    private static final int MIN_LENGTH = 2;

    private String updatedText;
    private boolean editing;
    private EditText input;

    public DateTextWatcher(EditText input) {
        this.input = input;
        this.input.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (text.toString().equals(updatedText) || editing) return;

        int pos = input.getSelectionStart();
        String digits = text.toString().replaceAll("\\D", "");
        int length = digits.length();

        if (length <= MIN_LENGTH) {
            updatedText = digits;
            return;
        }

        if (length > MAX_LENGTH) {
            digits = digits.substring(0, MAX_LENGTH);
        }

        if (length <= 4) {
            String month = digits.substring(0, 2);
            String day = digits.substring(2);

            updatedText = String.format(Locale.US, "%s/%s", month, day);
        }
        else {
            String month = digits.substring(0, 2);
            String day = digits.substring(2, 4);
            String year = digits.substring(4);

            updatedText = String.format(Locale.US, "%s/%s/%s", month, day, year);
        }

        //input.setSelection(pos < updatedText.length() ? pos : updatedText.length());
    }

    @Override
    public void afterTextChanged(Editable editable) {

        if (editing) return;

        editing = true;

        editable.clear();
        editable.insert(0, updatedText);

        editing = false;
    }
}
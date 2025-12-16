package com.beeproductive.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

public class LogoTextView extends AppCompatTextView {

    public LogoTextView(Context context) {
        super(context);
        applyLogoStyle();
    }

    public LogoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyLogoStyle();
    }

    public LogoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyLogoStyle();
    }

    private void applyLogoStyle() {
        String text = "BeeProductive";
        SpannableString spannable = new SpannableString(text);

        Typeface beeFont = ResourcesCompat.getFont(
                getContext(),
                R.font.libre_baskerville_bold
        );

        Typeface productiveFont = ResourcesCompat.getFont(
                getContext(),
                R.font.libre_baskerville_italic
        );

        // Bee → bold font + yellow
        spannable.setSpan(
                new CustomTypefaceSpan(beeFont),
                0,
                3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#f8c800")),
                0,
                3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Productive → italic font + brown
        spannable.setSpan(
                new CustomTypefaceSpan(productiveFont),
                3,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#6b4701")),
                3,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        setText(spannable);
    }
}

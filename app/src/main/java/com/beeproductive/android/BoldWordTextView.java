package com.beeproductive.android;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class BoldWordTextView extends AppCompatTextView {

    public BoldWordTextView(Context context) {
        super(context);
    }

    public BoldWordTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoldWordTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Call this method to set text and bold a specific word
     */
    public void setTextWithBoldWord(String fullText, String wordToBold) {
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(wordToBold);
        if (start >= 0) {
            int end = start + wordToBold.length();
            spannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        setText(spannable);
    }
}

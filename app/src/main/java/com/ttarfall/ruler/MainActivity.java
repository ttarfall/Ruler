package com.ttarfall.ruler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ucs.ruler.RulerView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private RulerView rulerView;
    private TextView tvHelloWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvHelloWord = (TextView) findViewById(R.id.hello_word);
        rulerView = (RulerView) findViewById(R.id.ruler_top);
        rulerView.addOnCurrentUnitTextListener(new RulerView.OnCurrentUnitTextListener() {

            DecimalFormat format = new DecimalFormat("0.00");

            @Override
            public void onCurrentUnitText(float text, float unit, float legend) {
                tvHelloWord.setText(format.format(text));
            }
        });

        rulerView.setOnFormatUnitTextListener(new RulerView.OnFormatUnitTextListener() {
            private DecimalFormat format = new DecimalFormat("0");

            @Override
            public String onFormatText(float text, float unit, float legend) {
                return format.format(text);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        rulerView.setOnFormatUnitLegendTextListener(new RulerView.OnFormatUnitTextListener() {

            private DecimalFormat format = new DecimalFormat("0");

            @Override
            public String onFormatText(float text, float unit, float legend) {
                return "￥" + format.format(text);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        rulerView.setCurrentUnit(10);

//        ruler.setOnFormatUnitTextListener(new DefaultFormatUnitTextListener(2));
//        ruler.setOnFormatUnitLegendTextListener(new DefaultLegendFormatUnitTextListener(2));
    }
}

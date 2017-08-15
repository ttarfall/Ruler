package com.ucs.ruler;/**
 * Created by ttarfall on 2017/6/12.
 */

import java.text.DecimalFormat;

/**
 * @author ttarfall
 * @date 2017-06-12 11:52
 */
public class DefaultLegendFormatUnitTextListener implements RulerView.OnFormatUnitTextListener {
    /**
     * 有效数字位数
     */
    protected int digits = 0;
    private DecimalFormat format;

    public DefaultLegendFormatUnitTextListener(int digits) {
        this.digits = digits;
        StringBuffer b = new StringBuffer();
        if (digits > 0) {
            b.append("0");
            for (int i = 0; i < digits; i++) {
                if (i == 0)
                    b.append(".");
                b.append("0");
            }
        }
        format = new DecimalFormat(b.toString());
    }

    @Override
    public String onFormatText(float text, float unit, float legend) {
        return format.format(unit * legend) + "个单位长";
    }

    @Override
    public int getDecimalDigits() {
        return digits;
    }
}

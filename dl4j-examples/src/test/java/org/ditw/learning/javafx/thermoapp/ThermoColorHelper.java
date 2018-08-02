package org.ditw.learning.javafx.thermoapp;

public final class ThermoColorHelper {
    private final static Double _ThermoValMax = 80.0;
    private final static Double _ThermoValMin = -40.0;
    private final static Double _ThermoColorNeutralVal = 0.0;
    private final static Double _ThermoColorRangeBlue =
        _ThermoColorNeutralVal - _ThermoValMin;
    private final static Double _ThermoColorRangeRed =
        _ThermoValMax - _ThermoColorNeutralVal;
    public static String thermoVal2Color(Double thermoVal) {
        assert(thermoVal <= _ThermoValMax);
        assert(thermoVal >= _ThermoValMin);
        if (thermoVal > _ThermoColorNeutralVal) {
            Double negRate =
                1 - thermoVal / _ThermoColorRangeRed;
            Long negColor = Math.round(255*negRate);
            return String.format("#FF%02X%02X", negColor, negColor);
        }
        else if (thermoVal < _ThermoColorNeutralVal){
            Double negRate =
                1 - (_ThermoColorNeutralVal - thermoVal) / _ThermoColorRangeBlue;
            Long negColor = Math.round(255*negRate);
            return String.format("#%02X%02XFF", negColor, negColor);
        }
        else
            return "#FFFFFF";
    }

}

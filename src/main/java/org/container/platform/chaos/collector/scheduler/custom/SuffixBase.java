package org.container.platform.chaos.collector.scheduler.custom;

import java.util.HashMap;
import java.util.Map;

/**
 * SuffixBase 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-24
 */
public class SuffixBase {

    public static final Map<String, BaseExponent> suffixToBinary =
            new HashMap<String, BaseExponent>() {
                {
                    put("", new BaseExponent(2, 0, Quantity.Format.BINARY_SI));
                    put("Ki", new BaseExponent(2, 10, Quantity.Format.BINARY_SI));
                    put("Mi", new BaseExponent(2, 20, Quantity.Format.BINARY_SI));
                    put("Gi", new BaseExponent(2, 30, Quantity.Format.BINARY_SI));
                    put("Ti", new BaseExponent(2, 40, Quantity.Format.BINARY_SI));
                    put("Pi", new BaseExponent(2, 50, Quantity.Format.BINARY_SI));
                    put("Ei", new BaseExponent(2, 60, Quantity.Format.BINARY_SI));
                }
            };

    public static final Map<String, BaseExponent> suffixToDecimal =
            new HashMap<String, BaseExponent>() {
                {
                    put("n", new BaseExponent(10, -9, Quantity.Format.DECIMAL_SI));
                    put("u", new BaseExponent(10, -6, Quantity.Format.DECIMAL_SI));
                    put("m", new BaseExponent(10, -3, Quantity.Format.DECIMAL_SI));
                    put("", new BaseExponent(10, 0, Quantity.Format.DECIMAL_SI));
                    put("k", new BaseExponent(10, 3, Quantity.Format.DECIMAL_SI));
                    put("M", new BaseExponent(10, 6, Quantity.Format.DECIMAL_SI));
                    put("G", new BaseExponent(10, 9, Quantity.Format.DECIMAL_SI));
                    put("T", new BaseExponent(10, 12, Quantity.Format.DECIMAL_SI));
                    put("P", new BaseExponent(10, 15, Quantity.Format.DECIMAL_SI));
                    put("E", new BaseExponent(10, 18, Quantity.Format.DECIMAL_SI));
                }
            };


}

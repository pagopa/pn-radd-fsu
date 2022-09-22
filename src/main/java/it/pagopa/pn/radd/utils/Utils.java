package it.pagopa.pn.radd.utils;

import org.apache.commons.lang3.StringUtils;

public class Utils {

    private Utils(){}


    public static boolean checkPersonType(String personType) {
        boolean result = false;
        if (StringUtils.equals(personType, Const.PF) || StringUtils.equals(personType, Const.PG)) {
            result = true;
        }
        return result;
    }
}

package it.pagopa.pn.radd.utils;

import org.apache.commons.lang3.StringUtils;

import static it.pagopa.pn.radd.utils.OperationTypeEnum.ACT;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;

public class Utils {

    private Utils(){}


    public static boolean checkPersonType(String personType) {
        return StringUtils.equals(personType, Const.PF) || StringUtils.equals(personType, Const.PG);
    }

    public static boolean checkOperationType(String operationType) {
        return StringUtils.equals(operationType, ACT.name()) || StringUtils.equals(operationType, AOR.name());
    }
}

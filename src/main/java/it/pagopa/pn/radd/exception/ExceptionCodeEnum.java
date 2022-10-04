package it.pagopa.pn.radd.exception;

import lombok.Getter;

@Getter
public enum ExceptionCodeEnum {
    OK(0), NUMBER_1(1), NUMBER_2(2), NUMBER_3(3), KO(99);

    private final int code;

    ExceptionCodeEnum(int code) {
        this.code = code;
    }

    public static ExceptionCodeEnum fromValue(int value){
        for (ExceptionCodeEnum b: ExceptionCodeEnum.values()) {
            if (b.code == value) {
                return b;
            }
        }
        return KO;
    }

}

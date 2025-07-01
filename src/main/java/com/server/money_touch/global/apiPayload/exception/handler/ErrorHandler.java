package com.server.money_touch.global.apiPayload.exception.handler;

import com.server.money_touch.global.apiPayload.code.BaseErrorCode;
import com.server.money_touch.global.apiPayload.exception.GeneralException;

public class ErrorHandler extends GeneralException {
    public ErrorHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

package io.github.aikusoni.ats.user.api.constants;

import io.github.aikusoni.ats.spring.core.common.BaseErrorCode;
import lombok.Getter;

@Getter
public enum UserApiErrorCode implements BaseErrorCode {
    CUSTOM_ERROR_CODE(10000, 500),
    FAILED_TO_FIND_USER(30401, 401),
    USER_ALREADY_EXISTS(30409, 409),
    DATASOURCE_SECRET_UPDATE_FAILED(30501, 500),
    ;

    final int errorCode;
    final int statusCode;

    UserApiErrorCode(int errorCode, int statusCode) {
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}

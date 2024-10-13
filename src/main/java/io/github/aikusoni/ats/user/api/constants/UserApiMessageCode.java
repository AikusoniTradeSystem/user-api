package io.github.aikusoni.ats.user.api.constants;

import io.github.aikusoni.ats.spring.core.common.MessageCode;

import static io.github.aikusoni.ats.spring.core.common.MessageCode.of;

public interface UserApiMessageCode {
    MessageCode INVALID_AUTHENTICATION = of("user_api.invalid_authentication");
    MessageCode NEED_LOGIN_AUTH = of("user_api.need_login_auth");
    MessageCode PASSWORD_IS_TOO_OLD = of("user_api.password_is_too_old");
    MessageCode USER_ALREADY_EXISTS = of("user_api.user_already_exists");
    MessageCode USER_DELETE_SUCCESS = of("user_api.user_delete_success");
    MessageCode USER_NOT_FOUND = of("user_api.user_not_found");
    MessageCode USER_REGISTER_SUCCESS = of("user_api.user_register_success");
    MessageCode USER_UPDATE_SUCCESS = of("user_api.user_update_success");
}

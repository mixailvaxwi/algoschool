package com.algoschool.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Новый пароль отдаётся один раз, в момент сброса — ни в каком виде он больше
 * не хранится и не восстанавливается. Администратор должен передать его
 * пользователю вне системы.
 */
@Data
@Builder
public class PasswordResetResponse {
    private String newPassword;
}

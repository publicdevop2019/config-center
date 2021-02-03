package com.mt.common.validate;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final String NOT_NULL_MSG = "condition not match notNull";
    private static final String NOT_NULL_SET_MSG = "condition not match Set notNull";
    private static final String NOT_EMPTY_MSG = "condition not match notEmpty";
    private static final String NO_NULL_MEMBER_MSG = "condition not match noNullMember";
    private static final String NUM_GREATER_OR_EQUAL_TO_MSG = "condition not match greaterThanOrEqualTo";
    private static final String EMAIL_MSG = "condition not match isValidEmail";
    private static final String HAS_TEXT_MSG = "condition not match hasText";
    private static final String GREATER_OR_EQUAL_TO_MSG = "condition not match lengthGreaterThanOrEqualTo";
    private static final String LESS_OR_EQUAL_TO_MSG = "condition not match lengthLessThanOrEqualTo";
    private static final String TEXT_WHITE_LIST_MSG = "condition not match whitelistOnly";
    private static final Pattern TEXT_WHITE_LIST = Pattern.compile("[a-zA-Z0-9 +\\-x/:()\\u4E00-\\u9FFF]*");

    public static void notBlank(@Nullable String text, @Nullable String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message == null ? HAS_TEXT_MSG : message);
        }
    }

    public static void notBlank(@Nullable String text) {
        notBlank(text, null);
    }

    public static void notNull(@Nullable String text, @Nullable String message) {
        if (text == null) {
            throw new IllegalArgumentException(message == null ? NOT_NULL_MSG : message);
        }
    }

    public static void notNull(@Nullable String text) {
        notNull(text, null);
    }

    public static void notNull(@Nullable Set<?> objects, @Nullable String message) {
        if (objects == null) {
            throw new IllegalArgumentException(message == null ? NOT_NULL_SET_MSG : message);
        }
    }

    public static void notNull(@Nullable Set<?> text) {
        notNull(text, null);
    }

    public static void lengthGreaterThanOrEqualTo(@Nullable String text, Integer min, @Nullable String message) {
        notNull(text);
        int length = text.length();
        if (min > length) {
            throw new IllegalArgumentException(message == null ? GREATER_OR_EQUAL_TO_MSG : message);
        }
    }

    public static void lengthGreaterThanOrEqualTo(@Nullable String text, Integer min) {
        lengthGreaterThanOrEqualTo(text, min, null);
    }

    public static void lengthLessThanOrEqualTo(@Nullable String text, Integer max, @Nullable String message) {
        notNull(text);
        int length = text.length();
        if (max < length) {
            throw new IllegalArgumentException(message == null ? LESS_OR_EQUAL_TO_MSG : message);
        }
    }

    public static void lengthLessThanOrEqualTo(@Nullable String text, Integer max) {
        lengthLessThanOrEqualTo(text, max, null);
    }

    public static void whitelistOnly(@Nullable String text) {
        whitelistOnly(text, null);
    }

    public static void whitelistOnly(@Nullable String text, @Nullable String message) {
        notNull(text);
        Matcher matcher = TEXT_WHITE_LIST.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException(message == null ? TEXT_WHITE_LIST_MSG : message);
        }
    }

    public static void notEmpty(@Nullable Set<?> objects) {
        notEmpty(objects, null);
    }

    public static void notEmpty(@Nullable Set<?> objects, @Nullable String message) {
        notNull(objects);
        noNullMember(objects);
        if (objects.isEmpty()) {
            throw new IllegalArgumentException(message == null ? NOT_EMPTY_MSG : message);
        }

    }

    public static void noNullMember(@Nullable Set<?> objects) {
        noNullMember(objects, null);
    }

    public static void noNullMember(@Nullable Set<?> objects, @Nullable String message) {
        notNull(objects);
        if (objects.contains(null)) {
            throw new IllegalArgumentException(message == null ? NO_NULL_MEMBER_MSG : message);
        }

    }

    public static void greaterThanOrEqualTo(int value, int min) {
        greaterThanOrEqualTo(value, min, null);
    }

    public static void greaterThanOrEqualTo(int value, int min, @Nullable String message) {
        if (value < min) {
            throw new IllegalArgumentException(message == null ? NUM_GREATER_OR_EQUAL_TO_MSG : message);
        }
    }

    public static void isEmail(String email) {
        isEmail(email, null);
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static void isEmail(String email, @Nullable String message) {
        notNull(email);
        if (!EmailValidator.getInstance().isValid(email))
            throw new IllegalArgumentException(message == null ? EMAIL_MSG : message);
    }
}

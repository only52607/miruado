package com.ooooonly.miruado.utils

import com.fasterxml.jackson.annotation.JsonValue

fun a(){
    val statusCode:StatusCode = StatusCode.ACCEPT
    statusCode.statusCode
}

enum class StatusCode(val statusCode: Int, @get:JsonValue val statusMessage: String) {
    SUCCESS(200, "成功"),
    CREATED(201, "成功"),
    ACCEPT(202, "已接收"),
    DELETED(204, "删除成功"),
    FORBIDDEN(403, "权限失败"),

    CREATE_TOKEN_ERROR(999, "Token创建失败"), MISSING_REQUIRE_FIELD(
        1000,
        "参数缺失"
    ),
    PARAMETER_VALUE_RANGE_ERROR(1001, "取值范围错误"), HTTP_RESPONSE_ERROR(1002, "调用外部请求时，发生http请求错误"), JSON_ANALYSIS_ERROR(
        1003,
        "调用外部请求时，发生json数据解析错误"
    ),
    INTERNET_IO_ERROR(1004, "调用外部请求时，发生网络IO错误"), SEND_MESSAGE_FAILED(1005, "发送消息失败"), METHOD_NOT_SUPPORT(
        1006,
        "方法调用错误，不支持此方法调用"
    ),
    RESOURCE_UPDATE_FAILED(10001, "资源更新失败"), RESOURCE_INSERT_FAILED(10002, "资源添加失败"), RESOURCE_DELETE_FAILED(
        10003,
        "资源删除失败"
    ),
    RESOURCE_NOT_MESSAGE_EXIT(10004, "资源信息不存在"), RESOURCE_IMPORT_ERROR(10005, "资源导入失败"), ROLE_UPDATE_FAILED(
        10006,
        "角色更新失败"
    ),
    ROLE_INSERT_FAILED(10007, "角色添加失败"), ROLE_DELETE_FAILED(10008, "角色删除失败"), ADMIN_USER_NOT_FOUND(
        10100,
        "账户不存在"
    ),
    ADMIN_USER_WRONG_PASSWORD(10101, "登录密码错误"), ADMIN_USER_INSERT_FAILED(10102, "创建用户失败"), ADMIN_USER_UPDATE_FAILED(
        10103,
        "资源更新失败"
    ),
    ADMIN_USER_DELETE_FAILED(10104, "资源删除失败"), ROLE_LIST_NOT_EXISTS(10105, "角色列表不能为空"), TOKEN_NOT_AVAILABLE(
        200001,
        "AccessToken已被注销，请重新登录"
    ),
    TOKEN_IS_EMPTY(200002, "AccessToken为空，请检查参数"), ACCESS_TOKEN_EXPIRED(
        200003,
        "AccessToken已过期"
    ),
    REFRESH_TOKEN_EXPIRED(200004, "RefreshToken已过期"), INVALID_ACCESS_TOKEN_TYPE(
        200005,
        "token类型错误，请使用accessToken"
    ),
    INVALID_REFRESH_TOKEN_TYPE(20006, "token类型错误，请使用refresToken"), TOKEN_FORMAT_ERR(
        20007,
        "token格式错误"
    ),
    MAIN_ACCOUNT_NOT_DELETE(10201, "主账号不能被删除"), PASSPORT_PWD_ERROR(10202, "密码错误"), SEND_REQUEST_ERROR(
        10301,
        "发送请求失败"
    ),
    GET_RESPONSE_NULL(10302, "获取第三方响应结果为空"), NOT_OPERATE_AUTH(10401, "该用户无操作权限"), NOT_ADMIN_AUTH(
        10601,
        "该用户无此管理权限"
    ),
    ADMIN_TYPE_CONFILICT(10602, "主管理员不能同时为子管理员"), ADMIN_INSERT_FAILED(10605, "添加子管理员错误"), ADMIN_MODIFY_FAILED(
        10606,
        "修改子管理员错误"
    ),
    CREATE_APP_ERR(60000, "创建App失败"), CREATE_APP_SHORTCUTS_ERR(60001, "创建快捷方式失败"), OPEN_APP_ERR(
        60002,
        "开通应用失败"
    ),
    ASSIGN_PERMISSION_ERR(60003, "分配权限失败"), URL_SYNTAX_ERR(60004, "url语法错误"), ADD_NEW_LOGO_ERR(
        60005,
        "logo添加失败"
    ),
    EDIT_LOGO_ERR(60006, "logo修改失败"), CREATE_GROUP_ERR(60007, "创建分组失败"), SCHEDULER_PARAM_ERROR(
        70001,
        "taskId与outkey不得同时为空"
    ),
    SCHEDULER_TIME_OUT(70002, "当前时间已经超过定时器时间"), SCHEDULER_CREATE_ERROR(70003, "调度器创建任务失败"), ERROR(500, "系统错误");
}
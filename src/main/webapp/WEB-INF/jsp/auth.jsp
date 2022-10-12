<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['lang'].value}"/>
<fmt:setBundle basename="internationalization"/>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/auth.css" rel="stylesheet">
    <title><fmt:message key="auth.title"/></title>
</head>
<body>
<div class="alert alert-danger" id="error-alert" role="alert" style="text-align: center; display: none"></div>
<div class="alert alert-success" id="success-alert" role="alert" style="text-align: center; display: none"></div>

<div class="container offset-col-3 col-6 margin-top">
    <ul class="nav nav-pills mb-3 row" id="pills-tab" role="tablist">
        <li class="nav-item col-6" role="presentation">
            <button class="nav-link active col-12" id="pills-login-tab" data-bs-toggle="pill"
                    data-bs-target="#pills-login"
                    type="button" role="tab" aria-controls="pills-home" aria-selected="true">
                <fmt:message key="auth.login"/>
            </button>
        </li>
        <li class="nav-item col-6 col-6" role="presentation">
            <button class="nav-link col-12" id="pills-register-tab" data-bs-toggle="pill"
                    data-bs-target="#pills-register"
                    type="button" role="tab" aria-controls="pills-profile" aria-selected="false">
                <fmt:message key="auth.register"/>
            </button>
        </li>
    </ul>
    <div class="tab-content" id="pills-tabContent">
        <div class="tab-pane fade show active" id="pills-login" role="tabpanel" aria-labelledby="pills-login-tab">
            <form action="login" method="post" class="" novalidate>
                <div class="mb-3">
                    <input type="email" id="login-email" class="form-control" placeholder="Email@example.com" required>
                    <div class="invalid-feedback">
                        <fmt:message key="validation.email_incorrect"/>
                    </div>
                </div>
                <div class="mb-3">
                    <input type="password" id="login-password" class="form-control" placeholder="<fmt:message key="auth.password"/>">
                    <div class="invalid-feedback">
                        <fmt:message key="validation.min_length"/>: 6
                    </div>
                </div>
                <button type="submit" class="btn btn-primary col-12"><fmt:message key="auth.login"/></button>
            </form>
        </div>

        <div class="tab-pane fade" id="pills-register" role="tabpanel" aria-labelledby="pills-register-tab">
            <form action="register" method="post" novalidate>
                <div class="mb-3">
                    <input type="email" id="register-email" class="form-control" placeholder="Email@example.com"
                           required>
                    <div class="invalid-feedback">
                        <fmt:message key="validation.email_incorrect"/>
                    </div>
                </div>
                <div class="mb-3">
                    <input type="text" id="register-first-name" class="form-control" placeholder="<fmt:message key="auth.first_name"/>" required>
                    <div class="invalid-feedback">
                        <fmt:message key="validation.first_name_required"/>
                    </div>
                </div>
                <div class="mb-3">
                    <input type="text" id="register-last-name" class="form-control" placeholder="<fmt:message key="auth.last_name"/>" required>
                    <div class="invalid-feedback">
                        <fmt:message key="validation.last_name_required"/>
                    </div>
                </div>
                <div class="mb-3">
                    <input type="password" id="register-password" class="form-control" placeholder="<fmt:message key="auth.password"/>">
                    <div class="invalid-feedback">
                        <fmt:message key="validation.min_length"/>: 6
                    </div>
                </div>
                <div class="mb-3">
                    <input type="password" id="register-password-2" class="form-control" placeholder="<fmt:message key="auth.repeat_password"/>">
                    <div class="invalid-feedback">
                        <fmt:message key="validation.password_not_match"/>
                    </div>
                </div>
                <div class="mb-3">
                    <select id="register-role" class="form-select" aria-label="Default select example">
                        <option selected value="user"><fmt:message key="auth.user"/></option>
                        <option value="speaker"><fmt:message key="report.speaker"/></option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary col-12"><fmt:message key="auth.register"/></button>
            </form>
        </div>
    </div>
</div>

<div class="row position-fixed bottom-0 start-0 pb-5 width-100">
    <c:choose>
        <c:when test="${cookie['lang'].value == 'uk'}">
            <button id="ukrainian-language-btn" type="button" class="btn btn-primary col-2 offset-3 disabled">Українська</button>
            <button id="english-language-btn" type="button" class="btn btn-light col-2 offset-2">English</button>
        </c:when>
        <c:otherwise>
            <button id="ukrainian-language-btn" type="button" class="btn btn-light col-2 offset-3">Українська</button>
            <button id="english-language-btn" type="button" class="btn btn-primary col-2 offset-2 disabled">English</button>
        </c:otherwise>
    </c:choose>
</div>

<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/auth.js"></script>
</body>
</html>
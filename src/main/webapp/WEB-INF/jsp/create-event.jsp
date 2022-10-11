<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['lang'].value}"/>
<fmt:setBundle basename="internationalization"/>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/sidebar.css" rel="stylesheet">
    <link href="css/event.css" rel="stylesheet">
    <title><fmt:message key="event.create"/></title>
</head>
<body>

<my:sidebar/>

<div class="container-fluid">
    <div class="row">
        <div class="col-9 offset-3">
            <div class="container">
                <div class="row">
                    <div class="alert alert-danger" id="error-alert" role="alert" style="text-align: center; display: none"></div>
                    <div class="col-10 offset-1">
                        <h2 class="text-center mb-3 mt-2"><fmt:message key="event.new"/></h2>
                        <form>
                            <div class="mb-3">
                                <label for="title" class="form-label"><fmt:message key="event.title"/></label>
                                <input type="text" class="form-control" id="title">
                                <div class="invalid-feedback">
                                    <fmt:message key="validation.min_length"/>: 3
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="description" class="form-label"><fmt:message key="event.description"/></label>
                                <textarea type="text" id="description" class="form-control" rows="6"></textarea>
                                <div class="invalid-feedback">
                                    <fmt:message key="validation.min_length"/>: 20
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="place" class="form-label"><fmt:message key="event.place"/></label>
                                <input type="text" class="form-control" id="place">
                                <div class="invalid-feedback">
                                    <fmt:message key="validation.min_length"/>: 5
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="date" class="form-label"><fmt:message key="event.date"/></label>
                                <input type="datetime-local" id="date" class="form-control">
                                <div class="invalid-feedback">
                                    <fmt:message key="validation.required_future_date"/>
                                </div>
                            </div>
                            <button id="create-btn" type="submit" class="btn btn-primary col-12"><fmt:message key="event.create"/></button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/validate-functions.js"></script>
<script src="js/new-event.js"></script>
</body>
</html>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['lang'].value}"/>
<fmt:setBundle basename="internationalization"/>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/sidebar.css" rel="stylesheet">
    <link href="css/new-reports.css" rel="stylesheet">
    <title><fmt:message key="report.new_reports"/> (${reportWithEventList.size()})</title>
</head>
<body>

<my:sidebar/>

<div class="container-fluid">
    <div class="row">
        <div class="col-9 offset-3">
            <div class="alert alert-danger" id="error-alert" role="alert" style="text-align: center; display: none"></div>
            <div class="row">
                <div class="col-10 offset-1">
                    <c:forEach items="${reportWithEventList}" var="reportWithEvent">
                        <div class="mt-5 report" report-id="${reportWithEvent.report.id}">
                            <div class="report-item" event-id="${reportWithEvent.event.id}">
                                <h1><fmt:message key="report"/>: ${reportWithEvent.report.topic}</h1>
                                <c:choose>
                                    <c:when test="${sessionScope.user.role == 'MODERATOR'}">
                                        <p><fmt:message key="report.speaker"/>: ${reportWithEvent.report.speaker.firstName} ${reportWithEvent.report.speaker.lastName}</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p><fmt:message key="event.moderator"/>: ${reportWithEvent.event.moderator.firstName} ${reportWithEvent.event.moderator.lastName}</p>
                                    </c:otherwise>
                                </c:choose>
                                <h3><fmt:message key="event"/>: ${reportWithEvent.event.title}</h3>
                            </div>
                            <div class="row">
                                <c:choose>
                                    <c:when test="${sessionScope.user.role == 'MODERATOR'}">
                                        <button type="button" class="btn btn-outline-success col-3 offset-2 confirm-report-btn">
                                            <fmt:message key="event.confirm_report"/>
                                        </button>
                                        <button type="button" class="btn btn-outline-danger col-3 offset-2 cancel-report-btn">
                                            <fmt:message key="event.cancel_report"/>
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-outline-success col-5 confirm-report-btn"><fmt:message key="event.accept_report"/></button>
                                        <button type="button" class="btn btn-outline-danger col-5 offset-2 cancel-report-btn"><fmt:message key="event.reject_report"/></button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <hr>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/new-reports.js"></script>
</body>
</html>

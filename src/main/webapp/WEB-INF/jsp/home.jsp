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
    <link href="css/home.css" rel="stylesheet">
    <title><fmt:message key="home.title"/></title>
</head>
<body>

<my:sidebar/>

<div class="container-fluid">
    <div class="row">
        <div class="events row col-8 offset-3">
            <c:forEach items="${events}" var="event">
                <my:event-card event="${event}"/>
            </c:forEach>
        </div>


        <div class="offset-3 col-8">
            <div class="offset-4 col-2">
                <my:pagination page="${page}" pages="${pages}"/>
            </div>
        </div>
    </div>
</div>


<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/home.js"></script>
</body>
</html>
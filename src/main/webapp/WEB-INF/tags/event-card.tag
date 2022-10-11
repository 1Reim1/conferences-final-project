<%@tag pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['lang'].value}"/>
<fmt:setBundle basename="internationalization"/>
<%@attribute name="event" required="true" type="com.my.conferences.entity.Event" %>

<div class="col-6 event-card" event-id="${event.id}">
    <h3>${event.title}</h3>
    <p class="event-description">${event.description}</p>
    <hr>
    <div class="row event-info">
        <p class="col-4 text-center"><fmt:message key="event.participants"/>: <b>${event.participants.size()}</b></p>
        <p class="col-4 text-center"><fmt:message key="event.reports"/>: <b>${event.reports.size()}</b></p>
        <p class="col-4 text-center"><b><fmt:formatDate value="${event.date}" pattern="dd-MM-yyyy HH:mm"/></b></p>
    </div>
</div>
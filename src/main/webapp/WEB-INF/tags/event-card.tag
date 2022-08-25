<%@attribute name="event" required="true" type="com.my.conferences.entity.Event" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="col-6 event-card" event-id="${event.id}">
    <h3>${event.title}</h3>
    <p class="event-description">${event.description}</p>
    <hr>
    <div class="row event-info">
        <p class="col-4 text-center">Participants: <b>${event.participants}</b></p>
        <p class="col-4 text-center">Reports: <b>${event.reports}</b></p>
        <p class="col-4 text-center"><b><fmt:formatDate value="${event.date.time}" pattern="dd-MM-yyyy HH:mm"/></b></p>
    </div>
</div>
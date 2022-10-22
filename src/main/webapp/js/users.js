let role
let newRole
let userPage = 1
let emailQuery = ""
let userId

function appendUsers(users) {
    let tableBody = $("#table-body")
    users.forEach((user) => {
        tableBody.append('<tr><th>' + user.id + '</th>' +
            '<td>' + user.email + '</td>' +
            '<td>' + user.firstName + '</td>' +
            '<td>' + user.lastName + '</td>' +
            '<td><button type="button" class="btn btn-dark btn-sm col-8 table-role-btn" data-bs-toggle="modal" data-bs-target="#role-modal"> <span class="text-lowercase text-capitalize">' + user.role.toLowerCase() + '</span></button></td>>' +
            '</tr>')
    })
    setTableRoleBtnOnClickHandler()
}

$("#search-by-email").on("click", function () {
    userPage = 1
    emailQuery = $("#email-input").val()
    $("#load-more-btn").prop("disabled", false)
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "load-more-users",
            email_query: emailQuery,
            page: userPage
        },
        success: function (data) {
            let users = $.parseJSON(data)
            console.log(users)
            $("#table-body").empty()
            appendUsers(users)
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            console.log(textStatus)
            $("#table-body").empty()
        }
    })
})

$("#load-more-btn").on("click", function () {
    userPage++
    let btn = $(this)
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "load-more-users",
            email_query: emailQuery,
            page: userPage
        },
        success: function (data) {
            let users = $.parseJSON(data)
            console.log(users)
            appendUsers(users)
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            console.log(btn.attr("disabled", true))
        }
    })
})

function setTableRoleBtnOnClickHandler() {
    let btn = $(".table-role-btn")
    btn.off("click")
    btn.on("click", function () {
        let tr = $(this).closest("tr");

        userId = tr.children("th").text()
        let email = tr.children("td").eq(0).text()
        let firstName = tr.children("td").eq(1).text()
        let lastName = tr.children("td").eq(2).text()
        role = tr.find("button").text().trim().toLowerCase()

        tr = $("#role-modal tbody tr")
        tr.children("th").text(userId)
        tr.children("td").eq(0).text(email)
        tr.children("td").eq(1).text(firstName)
        tr.children("td").eq(2).text(lastName)

        let options = $("#modal-role-select > option")
        options.each(function () {
            if (role === $(this).text().trim().toLowerCase()) {
                $(this).prop("selected", true)
            } else {
                $(this).removeAttr("selected")
            }
        })
        $("#save-role-btn").attr("disabled", "")
    })
}

setTableRoleBtnOnClickHandler()

$("#modal-role-select").change(function () {
    newRole = $(this).find("option:selected").text().trim().toLowerCase()
    if (role === newRole) {
        $("#save-role-btn").attr("disabled", "")
    } else {
        $("#save-role-btn").removeAttr("disabled")
    }
})

function showModeratorEventsModal(events) {
    let moderatorEventsModal = new bootstrap.Modal(document.getElementById('moderator-events'))
    $("#events-count").text(events.length)
    let tableBody = $("#moderator-events tbody")
    tableBody.empty()
    events.forEach((event) => {
        let d = new Date(event.date)
        let dateString = ("0" + d.getDate()).slice(-2) + "-" + ("0" + (d.getMonth() + 1)).slice(-2) + "-" +
            d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
        tableBody.append('<tr><th>' + event.id + '</th>' +
            '<td><a href="event?id=' + event.id + '" target="_blank">' + event.title + '</a></td>' +
            '<td>' + dateString + '</td>' +
            '</tr>')
    })
    moderatorEventsModal.show()
}

function showSpeakerReportsModal(reportsWithEvents) {
    let moderatorEventsModal = new bootstrap.Modal(document.getElementById('speaker-reports'))
    $("#reports-count").text(reportsWithEvents.length)
    let tableBody = $("#speaker-reports tbody")
    tableBody.empty()
    reportsWithEvents.forEach((reportWithEvent) => {
        let d = new Date(reportWithEvent.date)
        let dateString = ("0" + d.getDate()).slice(-2) + "-" + ("0" + (d.getMonth() + 1)).slice(-2) + "-" +
            d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
        tableBody.append('<tr><th>' + reportWithEvent.id + '</th>' +
            '<td>' + reportWithEvent.topic + "</td>" +
            '<td><a href="event?id=' + reportWithEvent.event_id + '" target="_blank">' + reportWithEvent.title + '</a></td>' +
            '<td>' + dateString + '</td>' +
            '</tr>')
    })
    moderatorEventsModal.show()
}

function sendUserRoleChangeRequest() {
    alert("ok")
}

$("#save-role-btn").on("click", function () {
    // showModeratorEventsModal()
    // showSpeakerReportsModal()

    if (role === "moderator") {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "load-moderator-events",
                moderator_id: userId,
            },
            success: function (data) {
                let events = $.parseJSON(data)
                if (events.length === 0) {
                    console.log("no events")
                    sendUserRoleChangeRequest()
                    return;
                }

                console.log(events)
                showModeratorEventsModal(events)
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
            }
        })
    } else if (role === "speaker") {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "load-speaker-future-reports",
                speaker_id: userId,
            },
            success: function (data) {
                let reportsWithEvents = $.parseJSON(data)
                if (reportsWithEvents.length === 0) {
                    console.log("no events")
                    sendUserRoleChangeRequest()
                    return;
                }

                console.log(reportsWithEvents)
                showSpeakerReportsModal(reportsWithEvents)
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
            }
        })
    } else if (role === "user") {
        sendUserRoleChangeRequest()
    }
})

$(".okay-btn").on("click", function () {
    sendUserRoleChangeRequest()
})
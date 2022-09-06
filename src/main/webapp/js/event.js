$("#join-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "join",
            eventId: $("#event").attr("event-id"),
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            $("#error-alert").text(jqXhr.responseText)
            $("#error-alert").fadeIn("slow")
        }
    })
})

$("#leave-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "leave",
            eventId: $("#event").attr("event-id"),
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            $("#error-alert").text(jqXhr.responseText)
            $("#error-alert").fadeIn("slow")
        }
    })
})
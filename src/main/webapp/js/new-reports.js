$(".report-item").on("click", function(e) {
    window.location.href = "event?id=" + $(this).attr("event-id")
})

$(".cancel-report-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: "event",
        data: {
            command: "cancel-report",
            reportId: $(this).closest(".report").attr("report-id"),
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

$(".confirm-report-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: "event",
        data: {
            command: "confirm-report",
            reportId: $(this).closest(".report").attr("report-id"),
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
$(".cancel-report-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "cancel-report",
            report_id: $(this).closest(".report").attr("report-id"),
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
        url: window.location.href,
        data: {
            command: "confirm-report",
            report_id: $(this).closest(".report").attr("report-id"),
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

function validateTopic(selector) {
    if ($(selector).val().length < 3) {
        $(selector).removeClass("is-valid")
        $(selector).addClass("is-invalid")
        return false
    }
    $(selector).addClass("is-valid")
    $(selector).removeClass("is-invalid")
    return true
}

$("#report-topic").on("input autocompletechange", function (e) {
    validateTopic("#report-topic")
})

$("#offer-report-btn-speaker").on("click", function (e) {
    e.preventDefault()
    if(validateTopic("#report-topic")) {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "offer-report",
                event_id: $("#event").attr("event-id"),
                topic: $("#report-topic").val(),
                speaker_id: $("#report-topic").closest(".modal-body").attr("speaker-id")
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
    }
})
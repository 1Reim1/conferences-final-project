$(".cancel-report-btn").on('click', function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
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
        url: window.location.href,
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

$("#offer-report-btn").on("click", function (e) {
    if(validateTopic("#report-topic")) {
        alert("yes")
    }
})
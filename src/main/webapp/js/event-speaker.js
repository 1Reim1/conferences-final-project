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
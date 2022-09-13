function validateTitle(element) {
    if ($.trim(element.val()).length < 3) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

function validateDescription(element) {
    if ($.trim(element.val()).length < 20) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

function validatePlace(element) {
    if ($.trim(element.val()).length < 5) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

function validateDate(element) {
    let newEventDate = new Date(element.val()).getTime()
    let now = new Date().getTime()
    if (isNaN(newEventDate) || newEventDate < now) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}
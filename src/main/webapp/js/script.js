function validateEmail(selector) {
    if (!$(selector).val().toLowerCase().match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
        $(selector).removeClass("is-valid")
        $(selector).addClass("is-invalid")
        return false
    }
        $(selector).addClass("is-valid")
        $(selector).removeClass("is-invalid")
    return true
}

function validatePassword(selector) {
    if ($(selector).val().length < 6) {
        $(selector).removeClass("is-valid")
        $(selector).addClass("is-invalid")
        return false
    }
        $(selector).addClass("is-valid")
        $(selector).removeClass("is-invalid")
    return true
}

$("#login-password").on("input", function (e) {
    validatePassword("#login-password")
})

$("#login-email").on("input", function (e) {
    validateEmail("#login-email")
})

$("#pills-login > form").on("submit", function (e) {
    e.preventDefault()
    let email = validateEmail("#login-email")
    let password = validatePassword("#login-password")
    if (email && password) {
        alert("okay")
    }
})
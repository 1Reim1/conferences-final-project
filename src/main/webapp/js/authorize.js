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

function bindEmailValidation(selector) {
    $(selector).on("input autocompletechange", function (e) {
        validateEmail(selector)
    })
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

function bindPasswordValidation(selector) {
    $(selector).on("input autocompletechange", function (e) {
        validatePassword(selector)
    })

}

bindEmailValidation("#login-email")
bindPasswordValidation("#login-password")

$("#pills-login > form").on("submit", function (e) {
    e.preventDefault()
    let email = validateEmail("#login-email")
    let password = validatePassword("#login-password")
    if (email && password) {
        $("#error-alert").hide()
        $.ajax({
            type: "POST",
            url: $("#pills-login > form").attr("action"),
            data: {
                email: $("#login-email").val(),
                password: $("#login-password").val(),
            },
            success: function (data, status, xhr) {
                $("#success-alert").show()
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
                $("#error-alert").text(jqXhr.responseText)
                $("#error-alert").fadeIn("slow")
            }
            })
        }
    }
)
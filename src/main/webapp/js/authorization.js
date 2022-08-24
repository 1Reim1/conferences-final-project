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

function validateName(selector) {
    if (!$(selector).val().match(/^[a-zA-Z]{3,}$/)) {
        $(selector).removeClass("is-valid")
        $(selector).addClass("is-invalid")
        return false
    }
    $(selector).addClass("is-valid")
    $(selector).removeClass("is-invalid")
    return true
}

function bindNameValidation(selector) {
    $(selector).on("input autocompletechange", function (e) {
        validateName(selector)
    })
}

function passwordEqualsCheck() {
    if ($("#register-password-2").val() !== $("#register-password").val()) {
        $("#register-password-2").removeClass("is-valid")
        $("#register-password-2").addClass("is-invalid")
        return false
    }
    $("#register-password-2").addClass("is-valid")
    $("#register-password-2").removeClass("is-invalid")
    return true
}

bindEmailValidation("#login-email")
bindPasswordValidation("#login-password")

bindEmailValidation("#register-email")
bindNameValidation("#register-first-name")
bindNameValidation("#register-last-name")
bindPasswordValidation("#register-password")
$("#register-password-2").on("input autocompletechange", function (e) {
    passwordEqualsCheck()
})
$("#register-password").on("input autocompletechange", function (e) {
    passwordEqualsCheck()
})

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
                    $("#success-alert").text("You have successfully logged in")
                    $("#success-alert").show()
                    window.location.href = "home"
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

$("#pills-register > form").on("submit", function (e) {
        e.preventDefault()
        let email = validateEmail("#register-email")
        let firstName = validateName("#register-first-name")
        let lastName = validateName("#register-last-name")
        let password = validatePassword("#register-password")
        let password2 = passwordEqualsCheck()
        if (email && firstName && lastName && password && password2) {
            $("#error-alert").hide()
            $.ajax({
                type: "POST",
                url: $("#pills-register > form").attr("action"),
                data: {
                    email: $("#register-email").val(),
                    first_name: $("#register-first-name").val(),
                    last_name: $("#register-last-name").val(),
                    password: $("#register-password").val(),
                    password_repeated: $("#register-password-2").val(),
                    role: $("#register-role").val(),
                },
                success: function (data, status, xhr) {
                    $("#success-alert").text("You have successfully registered")
                    $("#success-alert").show()
                    window.location.href = "home"
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
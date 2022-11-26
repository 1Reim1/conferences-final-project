function showErrorAlert(text) {
    console.log(text)
    $("#error-alert").text(text)
    $("#error-alert").fadeIn("slow")
}

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

function validateRecaptcha(element, index) {
    let response = grecaptcha.getResponse(index)
    if (response.length === 0) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

$("#pills-login > form").on("submit", function (e) {
        e.preventDefault()
        let email = validateEmail("#login-email")
        let password = validatePassword("#login-password")
        let recaptcha = validateRecaptcha($("#login-recaptcha"), 0)
        if (email && password && recaptcha) {
            $("#error-alert").hide()
            $.ajax({
                type: "POST",
                url: window.location.href,
                data: {
                    command: "login",
                    email: $("#login-email").val(),
                    password: $("#login-password").val(),
                    g_recaptcha_response: grecaptcha.getResponse()
                },
                success: function (data, status, xhr) {
                    $("#success-alert").text("You have successfully logged in")
                    $("#success-alert").show()
                    window.location.href = "home"
                },
                error: function (jqXhr) {
                    showErrorAlert(jqXhr.responseText)
                    grecaptcha.reset();
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
    let recaptcha = validateRecaptcha($("#register-recaptcha"), 1)
    if (email && firstName && lastName && password && password2 && recaptcha) {
        $("#error-alert").hide()
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "register",
                email: $("#register-email").val(),
                first_name: $("#register-first-name").val(),
                last_name: $("#register-last-name").val(),
                password: $("#register-password").val(),
                role: $("#register-role").val(),
                g_recaptcha_response: grecaptcha.getResponse(1)
            },
            success: function (data, status, xhr) {
                $("#success-alert").text("You have successfully registered in")
                $("#success-alert").show()
                window.location.href = "home"
            },
            error: function (jqXhr) {
                showErrorAlert(jqXhr.responseText)
                grecaptcha.reset(1);
            }
        })
    }
})

let forgetEmail
let code;

function validateCode(element) {
    if (element.val().length !== 6) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

function showCodeInput() {
    $("#code-input-group").fadeIn("slow")
    $("#code-recaptcha").parent().fadeIn("slow")
}

function showNewPasswordInput() {
    $("#code-input-group").css("display", "none")
    $("#code-recaptcha").parent().css("display", "none")
    $("#new-password-input-group").fadeIn("slow")
    $("#save-password-btn").fadeIn("slow")
}

function hideForgotPasswordModal() {
    let modal = document.getElementById('forgot-password-modal')
    bootstrap.Modal.getInstance(modal).hide()
}

$("#forgot-password-modal input[type='email']").on("input autocompletechange", function (e) {
    validateEmail($(this))
})

$("#send-code-btn").on("click", function () {
    let input = $(this).parent().find("input")
    let btn = $(this)
    if (validateEmail(input)) {
        forgetEmail = input.val()
        $("#error-alert").hide()
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "send-verification-code",
                email: forgetEmail,
            },
            success: function () {
                btn.attr("disabled", true)
                input.attr("readonly", true)
                showCodeInput()
            },
            error: function (jqXhr) {
                hideForgotPasswordModal()
                showErrorAlert(jqXhr.responseText)
            }
        })
    }
})

$("#verify-code-btn").on("click", function () {
    let input = $(this).parent().find("input")
    if (validateCode(input) && validateRecaptcha($("#code-recaptcha"), 2)) {
        code = input.val()
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "verify-code",
                email: forgetEmail,
                code: code,
                g_recaptcha_response: grecaptcha.getResponse(2)
            },
            success: function (data) {
                console.log(data)
                if (data.trim() === "true") {
                    input.addClass("is-valid")
                    input.removeClass("is-invalid")
                    showNewPasswordInput()
                } else {
                    input.removeClass("is-valid")
                    input.addClass("is-invalid")
                }
            },
            error: function (jqXhr) {
                hideForgotPasswordModal()
                showErrorAlert(jqXhr.responseText)
                grecaptcha.reset(2);
            }
        })
    }
})

bindPasswordValidation("#new-password-input-group input")
$("#save-password-btn").on("click", function () {
    $(this).attr("disabled", true)
    let inputSelector = "#new-password-input-group input"
    let input = $(inputSelector)
    if (validatePassword(inputSelector)) {
        console.log(forgetEmail)
        console.log(code)
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "modify-password",
                email: forgetEmail,
                code: code,
                new_password: input.val().trim()
            },
            success: function () {
                window.location.href = "home"
            },
            error: function (jqXhr) {
                hideForgotPasswordModal()
                showErrorAlert(jqXhr.responseText)
            }
        })
    }
})
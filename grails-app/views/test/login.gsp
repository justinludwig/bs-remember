<!doctype html>
<html>
<head>
    <title>BS Remember Me Login</title>
</head>
<body>

    <h1>BS Remember Me Login</h1>
    <p style="color:red">${message?:''}</p>
    <g:form action="login">
        <g:hiddenField name="next" value="${params.next}" />
    <dl>
        <dt>Username: <dd><g:textField name="username" value="${params.username}" />
        <dt>Password: <dd><g:passwordField name="password" />
        <dt> <dd><label for="rememberme">
            <g:checkBox name="rememberme" checked="${params.rememberme}" />
            Remember Me
        </label>
        <dt> <dd><button type="submit">Login</button>
    </dl>
    </g:form>
</body>
</html>

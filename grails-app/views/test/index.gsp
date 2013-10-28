<!doctype html>
<html>
<head>
    <title>BS Remember Me Test</title>
</head>
<body>
    <h1>BS Remember Me Test</h1>
    <p>
    <rememberme:yes>
        Welcome back, <rememberme:user property="username" />!
        <g:link action="logout">Logout</g:link>
    </rememberme:yes>
    <rememberme:no>
        Hello new friend!
        <g:link action="login">Login</g:link>
    </rememberme:no>
    </p>
    <p><g:link action="user">Users Only</g:link></p>
</body>
</html>

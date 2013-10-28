<!doctype html>
<html>
<head>
    <title>BS Remember Me Users Only</title>
</head>
<body>

    <h1>BS Remember Me Users Only</h1>
    <rememberme:withUser>
    <dl>
        <dt>Username: <dd>${user.username}
        <dt>Token: <dd>${user.rememberMeToken}
        <dt>Until: <dd>${user.rememberMeUntil}
        <dt>
        <dt>Last Hit: <dd>${user.lastHit}
        <dt>Expires: <dd>${user.rememberMeExpires}
        <dt>Period: <dd>${user.rememberMePeriod}
        <dt>Previous Token: <dd>${user.rememberMePreviousToken}
    </dl>
    </rememberme:withUser>
    <p><g:link action="index">Home</g:link> | <g:link action="logout">Logout</g:link></p>
</body>
</html>

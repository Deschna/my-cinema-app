<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in · Cinema API</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/ui/css/demo-ui.css">
</head>
<body class="security-page">
    <main class="security-card">
        <h1>Cinema API</h1>
        <h2>Sign in</h2>
        <p class="security-notice" role="alert" ${param.error == null ? 'hidden' : ''}>
            Invalid email or password.
        </p>
        <p class="security-notice" role="status" ${param.logout == null ? 'hidden' : ''}>
            You have been signed out.
        </p>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <label for="username">Email</label>
            <input id="username" name="username" type="email" autocomplete="username" required autofocus>
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" required>
            <button type="submit">Sign in</button>
        </form>
        <a class="security-link"
           href="${pageContext.request.contextPath}/swagger-ui/index.html#/Authentication/register">
            No account? Register in Swagger
        </a>
    </main>
</body>
</html>

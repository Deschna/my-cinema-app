<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign out · Cinema API</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/ui/css/demo-ui.css">
</head>
<body class="security-page">
    <main class="security-card">
        <h1>Cinema API</h1>
        <h2>Sign out</h2>
        <p>Are you sure you want to sign out?</p>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <button type="submit">Sign out</button>
        </form>
        <a class="security-link" href="${pageContext.request.contextPath}/swagger-ui.html">Open Swagger</a>
    </main>
</body>
</html>

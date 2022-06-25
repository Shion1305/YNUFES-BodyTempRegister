<%@ page import="com.shion1305.ynufes.bodytemp2022.ProcessorManager" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %><%--
  Created by IntelliJ IDEA.
  User: shion
  Date: 2022/05/29
  Time: 17:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <link href="index.css" type="text/css" rel="stylesheet">
  <title>Title</title>
</head>
<body>
<header>
  <h1>Lineボット検温記録システム メンテナンス用ページ</h1>
</header>
<div id="main-frame">
  <div>
    <h2>現在の状況</h2>
    <table id="status-table">
      <tr>
        <th>設定名</th>
        <th>稼働状況</th>
      </tr>
      <% HashMap<String, Boolean> processes = ProcessorManager.getConfigurationList();
        for (Map.Entry<String, Boolean> p : processes.entrySet()) {%>
      <tr>
        <td>
          <%=p.getKey()%>
        </td>
        <td>
          <%=p.getValue() ? "稼働中" : "停止"%>
        </td>
      </tr>
      <% }%>
    </table>
  </div>
  <form action="${pageContext.request.contextPath}/maintenance/reload" method="post">
    <p>パスワードを入力してリロード</p>
    <input type="password" name="pass">
    <input type="submit">
  </form>
</div>
</body>
</html>
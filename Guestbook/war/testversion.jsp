<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="guestbook.Greeting" %>
<%@ page import="guestbook.PMF" %>

<html>
  <head>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  </head>

  <body>

<center>
<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    out.println("<h3>Welcome to find different type of GPhone here.</h3>");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Greeting.class.getName() + " order by count asc";
    List<Greeting> greetings = (List<Greeting>) pm.newQuery(query).execute();
%>

<table border cellspacing=2 cellpadding=5 bgcolor=#DDDDDD>
<tr>
<th>Vendor</th>
<th>Hardware</th>
<th>Memory</th>
<th>Screen</th>
<th>Camera(M pixels)</th>
<th>user count</th>
</tr>

<%
    for (Greeting g : greetings) {
%>
<tr>
<td><%= g.vendor %></td>
<td><%= g.hardware %></td>
<td><%= g.memtotal %></td>
<td><%= g.resolution %></td>
<td><%= g.camera %></td>
<td><%= g.count %></td>
</tr>
<%
    }
    pm.close();
%>

</table>

<%-- we should comment web page input on real server 
<hr>
    <form action="/sign" method="post"> 
      <textarea name="processor" rows="1" cols="8"></textarea>
      <textarea name="bogomips" rows="1" cols="10"></textarea>
      <textarea name="hardware" rows="1" cols="10"></textarea>
      <textarea name="memtotal" rows="1" cols="10"></textarea>
      <textarea name="resolution" rows="1" cols="10"></textarea>
      <textarea name="camera" rows="1" cols="10"></textarea>
      <textarea name="sensors" rows="1" cols="10"></textarea>
      <textarea name="vendor" rows="1" cols="10"></textarea>
      <textarea name="imei" rows="1" cols="10"></textarea>
      <div><input type="submit" value="Post your GPhone's hardware info" /></div>
    </form> 
--%>
</center>

<%--
    out.println("<h3 align=right>jtbuaa@gmail.com</h3>");
--%>

  </body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="guestbook.Greeting" %>
<%@ page import="guestbook.CounterFactory" %>
<%@ page import="guestbook.ShardedCounter" %>
<%@ page import="guestbook.PMF" %>

<html>
  <head>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  </head>

  <body>

<center>
<%
    out.println("<h3>Welcome to find kinds of GPhone here.</h3>");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Greeting.class.getName() + " where sdkversion > '6' order by sdkversion desc";
    List<Greeting> greetings = (List<Greeting>) pm.newQuery(query).execute();
%>

<table border cellspacing=2 cellpadding=5 bgcolor=#DDDDDD>
<tr>
<th>Vendor</th>
<th>API level</th>
<th>Memory</th>
<th>Screen</th>
<th>Mega Pixel</th>
</tr>

<%
    for (Greeting g : greetings) {
%>
<tr>
<td><%= g.vendor %></td>
<td><%= g.sdkversion %></td>
<td><%= g.memtotal %></td>
<td><%= g.resolution %></td>
<td><%= g.camera %></td>
</tr>
<%
    }
    pm.close();
%>

</table>

<br/>
<%
    CounterFactory factory = new CounterFactory();
    ShardedCounter counter = factory.getCounter("user counter");
    if (counter == null) {
        counter = factory.createCounter("user counter");
        counter.addShard();
    }
    counter.increment();
    out.println("<h4>you are using trial version of system info client.");
    out.println("<h4>you can get the full version from");
    out.println("<a href=https://market.android.com/details?id=system.info>here</a>.");
%>

</center>

<%--
    out.println("<h3 align=right>jtbuaa@gmail.com</h3>");
--%>

  </body>
</html>

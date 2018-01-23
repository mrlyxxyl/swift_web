<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<form action="file/upload.do" enctype="multipart/form-data" method="post">
    <input type="file" name="file"/>
    <input type="submit" value="上传"/>
</form>

<form action="file/download.do" method="get">
    <input type="text" name="fileName"/>
    <input type="submit" value="下载"/>
</form>
</body>
</html>
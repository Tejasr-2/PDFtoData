<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
<title>Pdf to Data</title>

</head>
<body>
	<div align="center" class="container-fluid">
		<div class="col-md-5">
			<h3>Upload PDF file to Convert</h3>
			<br/>
			<form action="upload" method="post" enctype="multipart/form-data">
				<input type="file" name="file" />
				<br/><br/> 
				<input type="submit"  class="btn btn-primary"/>
			</form>
		</div>
		<div class="col-md-7">
			<br /> <br />
			<c:if test="${fechedData != null}">
				<h2>Fetched Data</h2>
				<table class="table table-bordered">
					<c:forEach items="${fechedData}" var="data">
						<tr>
							<td>${data.key}</td>
							<td>${data.value}</td>
						</tr>
					</c:forEach>

				</table>
			</c:if>
		</div>
	</div>

</body>
</html>
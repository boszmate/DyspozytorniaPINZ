<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html>
<%@include file="fragments/head.jsp"%>
<%@include file="fragments/header.jsp"%>
<body>
<main>
    <section class="mainBody">
        <div class="row">
            <div class="col-sm-12">
                <div class="mainText">
                    <c:url value="/login" var="to_login"/>
                    <form:form class="form-horizontal" modelAttribute="loginForm" action="${to_login}" method="post">
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="userName" cssClass="form-control" placeHolder="Login"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:password path="userPassword" cssClass="form-control" placeHolder="Password"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox"> Zapamietaj
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <input type="submit" class="btn btn-default" value="Zaloguj">
                                <button type="reset" class="btn btn-default">Wyczysc</button>
                            </div>
                        </div>
                    </form:form>
                    <a href="<j:url value="/register"/>"><p>Zarejestruj sie</p></a>
                </div>
            </div>
        </div>
    </section>
</main>
</body>
<%@include file="fragments/footer.jsp"%>
</html>
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
                    <form:form class="form-horizontal" action="/register" modelAttribute="registerForm" method="post">
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="storeLabel" cssClass="form-control" placeholder="Siec sklepow"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="userName" cssClass="form-control" placeholder="Nazwa użytkownika"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="firstName" cssClass="form-control" placeholder="Imie"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="lastName" cssClass="form-control" placeholder="Nazwisko"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:input path="email" cssClass="form-control" placeholder="Email"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:password path="password" cssClass="form-control" placeholder="Hasło"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <form:password path="confirmPassword" class="form-control" placeholder="Powtórz hasło"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-10 col-sm-8 col-md-6 col-lg-4 col-centered">
                                <input type="submit" class="btn btn-default" value="Zarejestruj sie">
                                <button type="reset" class="btn btn-default">Wyczysc</button>
                            </div>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </section>
</main>
</body>
<%@include file="fragments/footer.jsp"%>
</html>
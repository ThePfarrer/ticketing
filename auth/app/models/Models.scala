package models

import octopus.dsl._

case class Person(email: String, password: String)

case class Error(message: String, field: String = "")

case class Errors(errors: Array[Error])

//val emailPattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"
//
//implicit val signUpValidator: Validator[SignUp] = Validator[SignUp]
//.rule(_.email.matches(emailPattern), "Invalid email address")
//.rule(_.password.trim.length < 4, "Password must be between at least 4 characters")
//.rule(_.password.trim.length > 4, "Password must be between at most 20 characters")

package controllers

import models._
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.parser._
import io.circe.syntax._
import octopus.dsl._
import octopus.syntax._
import org.mindrot.jbcrypt.BCrypt
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.libs.circe.Circe

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (cc: ControllerComponents, personDao: PersonDao)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with Circe {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method will be
   * called when the application receives a `GET` request with a path of `/`.
   */

  val emailPattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"
  val key          = "asdf"
  val algo         = JwtAlgorithm.HS256

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok("Hi there!")
  }

  def currentUser() = Action.async { implicit request: Request[AnyContent] =>
    val userOption = request.session.get("jwt")

    userOption match {
      case Some(user) =>
        val payload = JwtJson.decodeJson(user, key, Seq(algo)).get.value.head
        Future(Ok(s"""{currentUser: $payload}""".toString))
//        Future(Ok(Map("currentUser" -> JwtJson.decodeJson(user, key, Seq(algo)).get.value).toString()))
      case None | Some(_) => Future(Unauthorized(Map("currentUser" -> None).asJson))
    }
  }

  def signout() = Action { implicit request: Request[AnyContent] =>
    Ok("""{}""".asJson).withNewSession
  }

  def signin() = Action.async { implicit request: Request[AnyContent] =>
    val jsonData = request.body.asJson.get.toString()

    decode[Person](jsonData) match {
      case Right(value) =>
        implicit val signInValidator: Validator[Person] = Validator[Person]
          .rule(_.email.matches(emailPattern), "Email must be valid")
          .rule(_.password.trim.nonEmpty, "You must supply a password")

        if (value.validate.toFieldErrMapping.isEmpty) {
          personDao.getPerson(value.email).flatMap {
            case Some(person) if BCrypt.checkpw(value.password, person.password) =>
              // Generate JWT
              val claim     = s"""{"id": "yeye", "email": ${value.email}"""
              val personJwt = JwtJson.encode(claim, key, algo)

              Future(Ok(person.asJson).withSession(Session(Map("jwt" -> personJwt))))

            case None | Some(_) => Future.successful(BadRequest("Invalid credentials"))

          }
        } else
          Future.successful(BadRequest(Errors(value.validate.toFieldErrMapping.map { res =>
            Error(res._2)
          }.toArray).asJson))

      case Left(_) => Future.successful(BadRequest("Invalid input"))
    }
  }

  def signup() = Action.async { implicit request: Request[AnyContent] =>
    val jsonData = request.body.asJson.get.toString()
//
    decode[Person](jsonData) match {
      case Right(value) =>
        implicit val signUpValidator: Validator[Person] = Validator[Person]
          .rule(_.email.matches(emailPattern), "Invalid email address")
          .rule(_.password.trim.length >= 4, "Password must be between at least 4 characters")
          .rule(_.password.trim.length < 20, "Password must be between at most 20 characters")

        if (value.validate.toFieldErrMapping.isEmpty) {
          personDao.getPerson(value.email).flatMap {
            case Some(_) => Future.successful(Conflict("Email in use"))
            case None =>
              val passwordHash = BCrypt.hashpw(value.password.trim, BCrypt.gensalt())
              val newPerson    = Person(value.email, passwordHash)
              val futureInsert = personDao.insert(newPerson)

              // Generate JWT
              val claim     = s"""{"id": "yeye", "email": ${value.email}"""
              val personJwt = JwtJson.encode(claim, key, algo)

              // Store it on session object
//              request.session(personJwt)
              futureInsert.map(_ => Created(newPerson.asJson).withSession(Session(Map("jwt" -> personJwt))))
          }
        } else
          Future.successful(BadRequest(Errors(value.validate.toFieldErrMapping.map { res =>
            Error(res._2)
          }.toArray).asJson))

      case Left(_) => Future.successful(BadRequest("Invalid input"))
    }
  }

}

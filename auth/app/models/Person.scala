package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * Table description of table persons. Objects of this class serve as prototypes
 * for rows in queries.
 */
class PersonsTable(tag: Tag) extends Table[Person](tag, "persons") {
  def * = (email, password) <> (Person.tupled, Person.unapply)

  /** Database column name SqlType(varchar), PrimaryKey */
  val email: Rep[String] = column[String]("email", O.PrimaryKey)

  /** Database column password SqlType(varchar) */
  val password: Rep[String] = column[String]("password")

}

class PersonDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
  executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  lazy val persons = TableQuery[PersonsTable]

  def all(): Future[Seq[Person]] = db.run(persons.result)

  def insert(user: Person): Future[Unit]               = db.run(persons insertOrUpdate user).map(_ => ())
  def getPerson(email: String): Future[Option[Person]] = db.run(persons.filter(_.email === email).result.headOption)

}

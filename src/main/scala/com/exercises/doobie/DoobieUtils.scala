package com.exercises.doobie

import cats.effect._
import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import Model._

object DoobieUtils {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val transactor: Resource[IO, H2Transactor[IO]] = {
    def url = "jdbc:h2:mem:"
    val user = "sa"
    val pass = ""

    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](1)
      bc <- Blocker[IO]
      xa <- H2Transactor.newH2Transactor[IO](url, user, pass, ec, bc)
    } yield xa
  }

  object CountryTable {

    val createCountryTable: ConnectionIO[Int] =
      sql"""
          CREATE TABLE IF NOT EXISTS country (
          code        VARCHAR(64),
          name        VARCHAR(255),
          population  INT,
          gnp         DECIMAL(10,2)
          )
       """.update.run

    def insertCountries(countries: List[Country]): ConnectionIO[Int] =
      Update[Country](
        "insert into country (code, name, population, gnp) values (?,?,?,?)"
      ).updateMany(countries)

    def dropCountries(): ConnectionIO[Int] =
      sql"delete from country where 1 = 1".update.run

    def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
      transactor.use(
        (createCountryTable *> dropCountries *> insertCountries(countries) *> f)
          .transact[IO]
      )
  }

  object PeopleTable {

    val dropPeopleTable: ConnectionIO[Int] =
      sql"""DROP TABLE IF EXISTS  people""".update.run

    val createPeopleTable: ConnectionIO[Int] =
      sql"""
          CREATE TABLE IF NOT EXISTS people (
          id        SERIAL,
          name VARCHAR NOT NULL UNIQUE,
          age  INT
          )
       """.update.run

    def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
      transactor.use((dropPeopleTable *> createPeopleTable *> f).transact[IO])
  }
}

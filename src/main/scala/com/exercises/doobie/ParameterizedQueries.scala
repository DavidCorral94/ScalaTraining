package com.exercises.doobie

import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO, IOApp}
import doobie.{ConnectionIO, Fragments}
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import com.exercises.doobie.Connecting.transactor
import com.exercises.doobie.DoobieUtils.CountryTable._
import com.exercises.doobie.Model._
import cats.implicits._

object ParameterizedQueries extends IOApp {

  /**
    * code    name                      population    gnp
    * "DEU"  "Germany"                    82164700    2133367.00
    * "ESP"  "Spain"                      39441700          null
    * "FRA"  "France",                    59225700    1424285.00
    * "GBR"  "United Kingdom"             59623400    1378330.00
    * "USA"  "United States of America"  278357000    8510700.00
    */

  def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
    transactor.use(
      (createCountryTable *> dropCountries() *> insertCountries(countries) *> f)
        .transact[IO]
    )

  def biggerThan(minPop: Int): doobie.Query0[Country] =
    sql"""select code, name, population, gnp
            from country
            where population > $minPop
            order by population asc""".query[Country]

  def populationIn(range: Range): doobie.Query0[Country] =
    sql"""select code, name, population, gnp
            from country
            where population > ${range.min} and population < ${range.max}
            order by population asc
            """.query[Country]

  def populationIn(
      range: Range,
      codes: NonEmptyList[String]
  ): doobie.Query0[Country] = {
    val q = fr"""select code, name, population, gnp
           from country
           where population > ${range.min}
           and   population < ${range.max}
           and   """ ++ Fragments.in(fr"code", codes) // code IN (...)
    q.query[Country]
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val countriesName = transactorBlock(biggerThan(75000000).to[List])
      .unsafeRunSync()
      .map(_.name)

    println(countriesName)
    assert(countriesName == List("Germany", "United States of America"))

    val countriesNameRange =
      transactorBlock(populationIn(25000000 to 75000000).to[List])
        .unsafeRunSync()
        .map(_.name)

    println(countriesNameRange)
    assert(countriesNameRange == List("Spain", "France", "United Kingdom"))

    val countriesNameRangeIn = transactorBlock(
      populationIn(25000000 to 75000000, NonEmptyList.of("ESP", "USA", "FRA"))
        .to[List]
    ).unsafeRunSync()
      .map(_.name)

    println(countriesNameRangeIn)
    assert(countriesNameRangeIn == List("Spain", "France"))

    IO.pure(ExitCode.Success)
  }
}

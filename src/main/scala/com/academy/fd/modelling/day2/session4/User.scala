package com.academy.fd.modelling.day2.session4

import UserValidationRules.{Email, EmailPredicate}
import cats.data.EitherNec
import cats.syntax.either._
import cats.syntax.parallel._
import com.academy.fd.modelling.day2.session4.Errors._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.numeric.PosLong
import eu.timepit.refined.types.string.NonEmptyString

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object UserValidationRules {
  type EmailPredicate = MatchesRegex["""\b[\w\.-]+@[\w\.-]+\.\w{2,4}\b"""]
  type Email = String Refined EmailPredicate
}

case class User(
    id: PosLong,
    name: NonEmptyString,
    email: Email,
    birthday: LocalDate
)

object User {

  def create(
      id: Long,
      name: String,
      email: String,
      birthday: LocalDate
  ): EitherNec[DomainError, User] =
    (
      refineV[Positive](id)
        .leftMap(message => NonPositiveValueProvided("id", message))
        .toEitherNec,
      refineV[NonEmpty](name)
        .leftMap(message => EmptyValueProvided("name", message))
        .toEitherNec,
      refineV[EmailPredicate](email)
        .leftMap(message => EmailNotValid(message, message))
        .toEitherNec,
      Either
        .cond(
          ChronoUnit.YEARS.between(birthday, LocalDate.now) >= 18,
          birthday,
          UserUnder18(birthday)
        )
        .toEitherNec
    ).parMapN((id, name, email, birthday) => User(id, name, email, birthday))
}

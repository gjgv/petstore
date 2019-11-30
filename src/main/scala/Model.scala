package petstore

sealed trait Response
case class Error(
  errorType: String,
  message: Option[String]
)
case class Success[A](
  message: Option[String],
  value: A
)

case class Pet(
  id: Int,
  name: String
)

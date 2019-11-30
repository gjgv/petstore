package petstore

import cats.effect.IO
import net.liftweb.json._
import net.liftweb.json.Serialization.write

object Main extends cask.MainRoutes {
  implicit val formats = DefaultFormats

  lazy val jdbcUrl = DbInitializer.createDb.unsafeRunSync()
  lazy val db = new DbManagement(jdbcUrl)

  private val defaultError = Error(
    errorType = "PetNotFound",
    message = Some("Pet does not exist")
  )

  @cask.get("/")
  def defaultPage() = {
    val response = Error(
      errorType = "UndefinedCall",
      message = Some("Nothing's here!")
    )

    write(response)
  }

  @cask.get("/pet")
  def getPets() = try {
    (for {
      pets <- db.getPets
      status = Success[Seq[Pet]](
        message = Some("List of pets"),
        value = pets
      )
      response <- IO.pure(write(status))
    } yield response).unsafeRunSync
  } catch {
    case e: Exception =>
      e.printStackTrace()
      val response = Error(
        errorType = "CouldNotRetrievePets",
        message = Some("Failed to get pets!")
      )

      write(response)
  }

  @cask.post("/pet")
  def addPet(req: cask.Request) = try {
    val petName = new String(req.readAllBytes())
    db.addPet(petName).unsafeRunSync()

    val response = Success[Unit](
      message = Some(s"Successfully add $petName as a new pet!"),
      value = ()
    )

    write(response)

  } catch {
    case e: Exception =>
      e.printStackTrace()
    val response = Error(
      errorType = "AddPetFailed",
      message = Some("Failed to add pet!")
    )

    write(response)
  }

  @cask.get("/pet/:petId")
  def getPet(petId: Int) = try {
    (for {
      pet <- db.getPet(petId)
      status = pet match {
        case Some(p) =>
          Success[Pet](
            message = Some("Pet found"),
            value = p
          )
        case None => defaultError
      }
      response <- IO.pure(write(status))
    } yield response).unsafeRunSync
  } catch {
    case e: Exception =>
      e.printStackTrace()
      val response = Error(
        errorType = "CouldNotRetrievePet",
        message = Some("Failed to get pet!")
      )

      write(response)
  }

  @cask.put("/pet/:petId")
  def updatePet(petId: Int, req: cask.Request) = try {
    val petName = new String(req.readAllBytes())

    (for {
      pet <- db.getPet(petId)
      status <- pet match {
        case Some(p) =>
          db.updatePet(p.id, petName).map(_ =>
            Success[Unit](
              message = Some("Pet updated"),
              value = ()
            )
          )
        case None => IO.pure(defaultError)
      }
      response <- IO.pure(write(status))
    } yield response).unsafeRunSync
  } catch {
    case e: Exception =>
      e.printStackTrace()
      val response = Error(
        errorType = "PetUpdateFailed",
        message = Some("Failed to update pet!")
      )

      write(response)
  }

  @cask.post("/pet/:petId")
  def deletePet(petId: Int) = try {
    (for {
      pet <- db.getPet(petId)
      status <- pet match {
        case Some(p) =>
          db.deletePet(p.id).map(_ =>
            Success[Unit](
              message = Some("Pet deleted"),
              value = ()
            )
          )
        case None => IO.pure(defaultError)
      }
      response <- IO.pure(write(status))
    } yield response).unsafeRunSync
  } catch {
    case e: Exception =>
      e.printStackTrace()
      val response = Error(
        errorType = "PetDeletionFailed",
        message = Some("Failed to delete pet!")
      )

      write(response)
  }

  initialize()
}
package petstore

import java.sql.{PreparedStatement, ResultSet}

import cats.effect.IO

class DbManagement(jdbcUrl: String) {
  def addPet(name: String): IO[Unit] = {
    val sql = s"insert into petstore.pets(name) values ('$name')"

    DbUtils.runDbQuery(jdbcUrl) { conn =>
      val statement = conn.prepareStatement(sql)
      try {
        statement.executeUpdate()
        ()
      } catch {
        case e: Exception =>
          println(s"Insert failed for: $name")
          e.printStackTrace()
      } finally {
        statement.close()
      }
    }
  }

  def getPet(id: Int): IO[Option[Pet]] = {
    val condition = s"where id = $id"

    loadPets(condition).map(_.headOption)
  }

  def getPets: IO[Seq[Pet]] = {
    loadPets("")
  }

  def updatePet(id: Int, name: String): IO[Unit] = {
    val sql = s"update petstore.pets set name = '$name' where id = $id"

    DbUtils.runDbQuery(jdbcUrl) { conn =>
      val statement = conn.createStatement()
      try {
        statement.execute(sql)
      } catch {
        case e: Exception =>
          println(s"Update failed for ID $id")
          e.printStackTrace()
      }
      finally {
        statement.close()
      }
    }
  }

  def deletePet(id: Int): IO[Unit] = {
    val sql = s"delete from petstore.pets where id = $id"

    DbUtils.runDbQuery(jdbcUrl) { conn =>
      val statement = conn.createStatement()
      try {
        statement.execute(sql)
      } catch {
        case e: Exception =>
          println(s"Delete failed for ID $id")
          e.printStackTrace()
      }
      finally {
        statement.close()
      }
    }
  }

  private def loadPets(stringCond: String): IO[Seq[Pet]] = IO.delay {
    val sql = s"select id, name from petstore.pets $stringCond".trim
    var entries: Vector[Option[Pet]] = Vector()

    DbUtils.runDbQuery(jdbcUrl) { conn =>
      val statement = conn.createStatement()

      try {
        val loadStatement: PreparedStatement = conn.prepareStatement(sql)
        val results: ResultSet = loadStatement.executeQuery()

        println(results)

        while(results.next()) {
          val idOpt = Option(results.getInt("id"))
          val nameOpt = Option(results.getString("name"))

          val pet = for {
            id <- idOpt
            name <- nameOpt
          } yield Pet(id, name)

          entries = entries :+ pet
        }
      } catch {
        case e: Exception =>
          println("Failed to load pets")
          e.printStackTrace()
      }
      finally {
        statement.close()
      }
    }.unsafeRunSync()

    entries.flatten.toSeq
  }
}

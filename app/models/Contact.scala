package model

import java.util.concurrent.TimeoutException

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import service.ContactService
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class Contact(id: Long, name: String, phone: String)

case class AddContactFormData(name: String, phone: String)

case class EditContactFormData(id: Long, name: String, phone: String)

case class SearchContactFormData(name: String)

object AddContactForm {

  val form = Form(
    mapping(
      "name" -> text.verifying("Укажите имя", name => !name.trim.isEmpty) // Не использовал nonEmptyText(maxLength=50), т.к. так и не получилось указать своё сообщение в conf/messages вместо дефолтного
                      .verifying("Не более 50 символов", name => name.trim.length < 51)
                      .verifying("Контакт с таким именем уже существует", name => verifyUniqueName(name)),
      "phone" -> text.verifying("Укажите валидный номер телефона", phone => phone.matches("\\d{1,11}"))
                      .verifying("Контакт с таким телефоном уже существует", phone => verifyUniquePhone(phone))
    )(AddContactFormData.apply)(AddContactFormData.unapply)
  )

  def verifyUniqueName(name: String): Boolean = {
    try{
      val contactOpt = Await.result(ContactService.getByName(name), 5000.millis)
      contactOpt.isEmpty
    } catch {
      case toe: TimeoutException => false
      // TODO: Обработать по-человечески: добавить error в форму или перенаправить на страницу с сообщением
    }
  }

  def verifyUniquePhone(phone: String): Boolean ={
    try{
      val contactOpt = Await.result(ContactService.getByPhone(phone), 5000.millis)
      contactOpt.isEmpty
    } catch {
      case toe: TimeoutException => false
      // TODO: Обработать по-человечески: добавить error в форму или перенаправить на страницу с сообщением
    }
  }

}

object EditContactForm {

  val form = Form(
    mapping(
      "id" -> longNumber, // TODO: Добавить валидацию id на наличие такой записи в базе
      "name" -> text.verifying("Укажите имя", name => !name.trim.isEmpty)
                .verifying("Не более 50 символов", name => name.trim.length < 51),
      "phone" -> text.verifying("Укажите валидный номер телефона", phone => phone.matches("\\d{1,11}"))
    )(EditContactFormData.apply)(EditContactFormData.unapply)
  )


  def validateForUnique(form: Form[EditContactFormData]): Form[EditContactFormData] = {
    val id: Long = form("id").value.get.toLong
    val name: String = form("name").value.get
    val phone: String = form("phone").value.get
    val duplicateContacts = Await.result(ContactService.searchByNameOrPhone(name, phone), 5000.millis).filter(contact => contact.id != id)
    val duplicateByName: Boolean = duplicateContacts.exists(contact => contact.name == name)
    val duplicateByPhone: Boolean = duplicateContacts.exists(contact => contact.phone == phone)

    val nameErrorMessage = "Контакт с таким именем уже существует"
    val phoneErrorMessage = "Контакт с таким телефоном уже существует"

    if (duplicateByName && duplicateByPhone){
      form.withError("name", nameErrorMessage).withError("phone", phoneErrorMessage)
    } else if (duplicateByName) {
      form.withError("name", nameErrorMessage)
    } else if (duplicateByPhone) {
      form.withError("phone", phoneErrorMessage)
    } else {
      form
    }
  }

}

object SearchContactForm {

  val form = Form(
    mapping(
      "name" -> text.verifying("Максимальная длина запроса - 50 символов", name => name.length < 51)
    )(SearchContactFormData.apply)(SearchContactFormData.unapply)
  )

}


class ContactTableDef(tag: Tag) extends Table[Contact](tag, "contacts") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def name = column[String]("name")
  def phone = column[String]("phone")

  override def * =
    (id, name, phone) <>(Contact.tupled, Contact.unapply)
}

object Contacts {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val contacts: TableQuery[ContactTableDef] = TableQuery[ContactTableDef]

  def add(contact: Contact): Future[String] = {
    dbConfig.db.run(contacts += contact).map(res => "Contact successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(contacts.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Contact]] = {
    dbConfig.db.run(contacts.filter(_.id === id).result.headOption)
  }

  def getByName(name: String): Future[Option[Contact]] = {
    dbConfig.db.run(contacts.filter(_.name === name).result.headOption)
  }

  def searchByName(name: String): Future[Seq[Contact]] = {
    dbConfig.db.run(contacts.filter(_.name.toLowerCase like s"%${name.toLowerCase}%").sortBy(_.id.desc).result)
  }

  def getByPhone(phone: String): Future[Option[Contact]] = {
    dbConfig.db.run(contacts.filter(_.phone === phone).result.headOption)
  }

  def searchByNameOrPhone(name: String, phone: String): Future[Seq[Contact]] = {
    dbConfig.db.run(contacts.filter(contact => contact.name === name || contact.phone === phone).result)
  }

  def listAll: Future[Seq[Contact]] = {
    dbConfig.db.run(contacts.sortBy(_.id.desc).result)
  }

  def updateContact(contact: Contact): Future[Unit] = {
    val contactToUpdate: Contact = contact.copy(contact.id)
    dbConfig.db.run(contacts.filter(_.id === contact.id).update(contactToUpdate)).map(_ => ())
  }

}
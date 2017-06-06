package service

import model.{Contact, Contacts}
import scala.concurrent.Future

object ContactService {

  def addContact(contact: Contact): Future[String] = {
    Contacts.add(contact)
  }

  def deleteContact(id: Long): Future[Int] = {
    Contacts.delete(id)
  }

  def getContact(id: Long): Future[Option[Contact]] = {
    Contacts.get(id)
  }

  def getByName(name: String): Future[Option[Contact]] = {
    Contacts.getByName(name)
  }

  def getByPhone(phone: String): Future[Option[Contact]] = {
    Contacts.getByPhone(phone)
  }

  def searchByNameOrPhone(name: String, phone: String): Future[Seq[Contact]] = {
    Contacts.searchByNameOrPhone(name, phone)
  }

  def listAllContacts: Future[Seq[Contact]] = {
    Contacts.listAll
  }

  def searchByName(name:String): Future[Seq[Contact]] = {
    Contacts.searchByName(name)
  }

  def updateContact(contact: Contact) = {
    Contacts.updateContact(contact)
  }

}

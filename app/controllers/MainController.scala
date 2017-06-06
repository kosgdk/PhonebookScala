package controllers

import javax.inject.Inject

import model._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import service.ContactService

import scala.concurrent.ExecutionContext.Implicits.global

class MainController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def indexPage(searchquery: String = "") = Action.async { implicit request =>
    ContactService.searchByName(searchquery) map { contacts =>
      Ok(views.html.index(SearchContactForm.form,
                          AddContactForm.form,
                          EditContactForm.form,
                          contacts, None, searchquery))
    }
  }

  def addContact() = Action.async { implicit request =>
    val searchquery = request.body.asFormUrlEncoded.get("searchquery").mkString
    AddContactForm.form.bindFromRequest.fold(
      errorAddContactForm => ContactService.searchByName(searchquery)
                                            .map { contacts =>Ok(views.html.index(SearchContactForm.form,
                                                                                  errorAddContactForm,
                                                                                  EditContactForm.form,
                                                                                  contacts, None, searchquery))},
      data => {val newContact = Contact(0, data.name, data.phone)
              ContactService.addContact(newContact).map(result => Redirect(routes.MainController.indexPage(searchquery)))
              }
    )
  }

  def searchContact() = Action.async { implicit request =>
    val searchquery = request.body.asFormUrlEncoded.get("searchquery").mkString
    SearchContactForm.form.bindFromRequest.fold(
      errorSearchContactForm => ContactService.searchByName("")
                                              .map { contacts =>Ok(views.html.index(errorSearchContactForm,
                                                                                    AddContactForm.form,
                                                                                    EditContactForm.form,
                                                                                    contacts, None, searchquery))},
      data => ContactService.searchByName(data.name)
                            .map { contacts =>Ok(views.html.index(SearchContactForm.form,
                                                                  AddContactForm.form,
                                                                  EditContactForm.form,
                                                                  contacts, None, data.name))}
    )
  }

  def deleteContact(id: Long, searchquery: String) = Action.async { implicit request =>
    ContactService.deleteContact(id) map { result =>
      Redirect(routes.MainController.indexPage(searchquery))
    }
  }


  def editContactPage(id: Long, searchquery: String) = Action.async { implicit request =>
    ContactService.searchByName(searchquery) map {contacts =>
      val contactsMap = contacts.map(contact => contact.id -> contact).toMap
      if (contactsMap.contains(id)){
        val contactToEdit = contactsMap.get(id)
        val editContactFormData = EditContactFormData(contactToEdit.get.id, contactToEdit.get.name, contactToEdit.get.phone)
        val editContactForm = EditContactForm.form.fill(editContactFormData)
        Ok(views.html.index(SearchContactForm.form,
                            AddContactForm.form,
                            editContactForm,
                            contacts, Option(id), searchquery))
      }else{
        Redirect(routes.MainController.indexPage(searchquery)) // TODO: Вернуть страницу с сообщением об ошибке "Контакт не найден"
      }
    }

  }


  def updateContact() = Action.async { implicit request =>
    val searchquery = request.body.asFormUrlEncoded.get("searchquery").mkString
    EditContactForm.form.bindFromRequest.fold(
      errorEditContactForm => ContactService.searchByName(searchquery)
                                            .map { contacts =>Ok(views.html.index(SearchContactForm.form,
                                                                                  AddContactForm.form,
                                                                                  errorEditContactForm,
                                                                                  contacts,
                                                                                  errorEditContactForm.data.get("id").map(_.toLong),
                                                                                  searchquery))},
      data => {
        val form = EditContactForm.validateForUnique(EditContactForm.form.fill(data))
        if (form.hasErrors){
          ContactService.searchByName(searchquery).map { contacts =>Ok(views.html.index(SearchContactForm.form,
                                                                              AddContactForm.form,
                                                                              form,
                                                                              contacts,
                                                                              form.data.get("id").map(_.toLong),
                                                                              searchquery))}
        } else {
          val newContact = Contact(data.id, data.name, data.phone)
          ContactService.updateContact(newContact).map(result => Redirect(routes.MainController.indexPage(searchquery)))
        }
      }
    )
  }

}
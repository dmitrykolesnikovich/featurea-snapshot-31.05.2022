package featurea.ktor

import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

// http://www.simplejavamail.org
// http://albert-kuo.blogspot.com/2016/04/java-mail-caused-by-javaxmailauthentica.html
fun sendByGmail(
    appCode: String,
    smtp: String,
    from: String,
    to: String,
    toName: String,
    fromName: String,
    title: String,
    text: String
) {
    try {
        val mail = EmailBuilder.startingBlank()
            .from(fromName, from)
            .to(toName, to)
            .withSubject(title)
            .withHTMLText(text)
            .buildEmail()
        val mailer = MailerBuilder
            .withSMTPServer("smtp.gmail.com", 587, smtp, appCode)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .buildMailer()
        mailer.sendMail(mail)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

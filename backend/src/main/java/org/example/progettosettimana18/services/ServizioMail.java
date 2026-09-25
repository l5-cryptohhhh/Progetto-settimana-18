package org.example.progettosettimana18.services;

import jakarta.mail.internet.MimeMessage;
import org.example.progettosettimana18.entities.AvvisoPrezzo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class ServizioMail {

    private static final Locale ITALIA = Locale.of("it", "IT");

    private final JavaMailSender postino;
    private final ITemplateEngine motoreTemplate;
    private final String indirizzoMittente;
    private final String urlFrontend;

    public ServizioMail(JavaMailSender postino,
                        ITemplateEngine motoreTemplate,
                        @Value("${app.mail.mittente}") String indirizzoMittente,
                        @Value("${app.frontend.url}") String urlFrontend) {
        this.postino = postino;
        this.motoreTemplate = motoreTemplate;
        this.indirizzoMittente = indirizzoMittente;
        this.urlFrontend = urlFrontend;
    }

    /**
     * Il nome dell'utente e la descrizione dell'auto finiscono dentro il template,
     * che li stampa con th:text: Thymeleaf fa l'escape di ogni valore, quindi
     * un nome scritto come "&lt;script&gt;..." arriva nella mail come testo e basta.
     */
    public void inviaAvvisoPrezzo(AvvisoPrezzo avviso, BigDecimal prezzoVecchio) {
        var auto = avviso.getAuto();
        var utente = avviso.getUtente();

        Context contesto = new Context(ITALIA);
        contesto.setVariable("nome", utente.getNome());
        contesto.setVariable("auto", auto.getMarca() + " " + auto.getModello());
        contesto.setVariable("anno", auto.getAnno());
        contesto.setVariable("prezzoVecchio", formatta(prezzoVecchio));
        contesto.setVariable("prezzoNuovo", formatta(auto.getPrezzo()));
        contesto.setVariable("soglia", formatta(avviso.getSogliaPrezzo()));
        contesto.setVariable("linkAuto", urlFrontend + "/auto/" + auto.getId());
        // Nel link c'e' il token casuale, non l'id dell'avviso.
        contesto.setVariable("linkDisattivazione", urlFrontend + "/disattiva-avviso/" + avviso.getTokenDisattivazione());

        String corpo = motoreTemplate.process("mail/avviso-prezzo", contesto);

        try {
            MimeMessage messaggio = postino.createMimeMessage();
            MimeMessageHelper aiutante = new MimeMessageHelper(messaggio, false, "UTF-8");
            // Nella casella compare "Autoven" come mittente, non l'indirizzo nudo.
            aiutante.setFrom(indirizzoMittente, "Autoven");
            aiutante.setTo(utente.getEmail());
            aiutante.setSubject("Autoven | Prezzo sceso: " + auto.getMarca() + " " + auto.getModello());
            aiutante.setText(corpo, true);
            postino.send(messaggio);
        } catch (Exception ex) {
            // Niente indirizzo email nel messaggio dell'eccezione: chi la logga
            // non deve ritrovarsi la rubrica degli utenti dentro i log.
            throw new IllegalStateException("invio della mail fallito per l'avviso " + avviso.getId(), ex);
        }
    }

    private String formatta(BigDecimal importo) {
        if (importo == null) return "-";
        // Come sul sito e nel manuale di marca: simbolo davanti, niente centesimi se sono zero.
        NumberFormat numero = NumberFormat.getNumberInstance(ITALIA);
        numero.setMinimumFractionDigits(0);
        numero.setMaximumFractionDigits(2);
        return "€ " + numero.format(importo);
    }
}

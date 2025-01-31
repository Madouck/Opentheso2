package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bean.candidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.*;
import javax.annotation.PreDestroy;
import javax.mail.*;
import javax.mail.internet.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;
    @Inject
    private LanguageBean langueBean;
    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject private MailBean mailBean;
    
    
    private String email;
    private List<NodeUser> nodeUsers;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeUsers != null) {
            nodeUsers.clear();
            nodeUsers = null;
        }
        email = null;
    }

    public void getParticipantsInConversation() {
        nodeUsers = new MessageDao().getParticipantsByCandidat(
                connect.getPoolConnexion(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());

        if (CollectionUtils.isEmpty(nodeUsers)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg8"));
        }
//        PrimeFaces.current().ajax().update("candidatForm");
//        PrimeFaces.current().executeScript("PF('participantsList').show();");
    }

    public void sendMessage() {
        if (candidatBean.getInitialCandidat() == null) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg7"));
            return;
        }

        if (StringUtils.isEmpty(candidatBean.getMessage())) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg1"));
            return;
        }

        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        messageDto.setNom(candidatBean.getCurrentUser().getUsername().toUpperCase());
        messageDto.setMsg(candidatBean.getMessage());

        MessageDao messageDao = new MessageDao();
        messageDao.addNewMessage(connect.getPoolConnexion(),
                candidatBean.getMessage(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());

        
        //// envoie de mail aux participants à la discussion
        
        String subject = "Nouveau message module candidat";
        String message = "Vous avez participé à la discussion pour ce candidat "
                + candidatBean.getCandidatSelected().getNomPref() + ", " 
                + " id= " + candidatBean.getCandidatSelected().getIdConcepte()
                + ". Sachez qu’un nouveau message a été posté.";

        getParticipantsInConversation();
        if(nodeUsers != null) {
            for (NodeUser nodeUser : nodeUsers) {
                if(nodeUser.isIsAlertMail()) {
                    if (!mailBean.sendMail(nodeUser.getMail(), subject,  message)) {
                  //     candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("Erreur d'envoie de mail pour " + nodeUser.getName() + ", veuillez contacter l'administrateur"));
                    }
                }
            }
        }

        reloadMessage();

        candidatBean.setMessage("");
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg2"));
    }

    public void reloadMessage() {
        candidatBean.getCandidatSelected().setMessages(new MessageDao().getAllMessagesByCandidat(
                connect.getPoolConnexion(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser()));
    }

    private Properties getPrefMail() {
        Properties props;
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            props = new Properties();
            props.setProperty("mail.transport.protocol", bundlePref.getString("protocolMail"));
            props.setProperty("mail.smtp.host", bundlePref.getString("hostMail"));
            props.setProperty("mail.smtp.port", bundlePref.getString("portMail"));
            props.setProperty("mail.smtp.auth", bundlePref.getString("authMail"));
            props.setProperty("mailFrom", bundlePref.getString("mailFrom"));
            props.setProperty("transportMail", bundlePref.getString("transportMail"));

            return props;
        } catch (Exception e) {
        }
        return null;
    }

    public void sendInvitation() {
        Properties props = getPrefMail();
        if (props == null) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, "Absence des préférences pour le serveur Mail");
            return;
        }
        if (StringUtils.isEmpty(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg3"));
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg4"));
        } else {

//            String from = "opentheso@mom.fr";
//            String host = "smtp.mom.fr";

            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", props.getProperty("hostMail"));

            Session session = Session.getDefaultInstance(properties);

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(props.getProperty("mailFrom")));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Invitation à une conversation !");
                message.setText("C'est le body du message");
                // Send message
                Transport.send(message);
                candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg5"));
            } catch (MessagingException mex) {
                candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg6"));
            }
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<NodeUser> getNodeUsers() {
        return nodeUsers;
    }

    public void setNodeUsers(List<NodeUser> nodeUsers) {
        this.nodeUsers = nodeUsers;
    }



}

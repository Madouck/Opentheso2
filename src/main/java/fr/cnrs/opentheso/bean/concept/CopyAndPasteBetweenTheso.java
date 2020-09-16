/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.CopyAndPasteBetweenThesoHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "copyAndPasteBetweenTheso")
@SessionScoped
public class CopyAndPasteBetweenTheso implements Serializable {

    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;
    @Inject private Tree tree;
    @Inject private RoleOnThesoBean roleOnThesoBean;    


    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private NodeConcept nodeConceptDrop;
    
    private ArrayList<String> conceptsToCopy;
    
    private String idThesoOrigin;
    
    private boolean isDropToRoot;
    
    // pour savoir si on récupère les identifiants pérennes ou non
    private String identifierType = "sans";    

    public CopyAndPasteBetweenTheso() {
    }

    public void reset() {
        isCopyOn = false;
        isValidPaste = false;
        nodeConceptDrag = null;
        nodeConceptDrop = null;
        isDropToRoot = false;
        idThesoOrigin = null;
        conceptsToCopy = null;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void initInfo() {

    }

    /**
     * permet de préparer le concept ou la branche pour le déplacement vers un autre endroit #MR
     *
     */
    public void onStartCopy() {
        
        // controler les déplacements non autorisés 
        
        FacesMessage msg;
        nodeConceptDrag = conceptBean.getNodeConcept();
        isCopyOn = true;
        idThesoOrigin = selectedTheso.getCurrentIdTheso();
        if (nodeConceptDrag == null) {
            return;
        }
        
        ConceptHelper conceptHelper = new ConceptHelper();        
        conceptsToCopy = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()); 
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Copier "
                + nodeConceptDrag.getTerm().getLexical_value() + " (" + nodeConceptDrag.getConcept().getIdConcept() + ")");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * permet de vérifier si le déplacement est valide 
     */
    public void validatePaste() {
        if(selectedTheso.getCurrentIdTheso().equalsIgnoreCase(idThesoOrigin)) {
            return;
        }
        
        FacesMessage msg;
        isValidPaste = false;
        ConceptHelper conceptHelper = new ConceptHelper();

        // on vérifie si les ids des concepts à copier n'existent pas déjà dans le thésaurus cible.
        for (String idConcept : conceptsToCopy) {
            if(conceptHelper.isIdExiste(connect.getPoolConnexion(), idConcept, selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'identifiant " + idConcept + " existe déjà dans le thésaurus cible, c'est interdit !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;                
            }
        }
        isValidPaste = true;
    }
    
    /**
     * permet de coller la branche copiée précédement sous le concept en cours
     * déplacements valides: - d'un concept à un concept - de la racine à un
     * concept ou TopConcept #MR
     * Ne marche que pour Couper/coller (pas de Drag and drop)
     *
     */
    public void paste() {
        FacesMessage msg;  
     
        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!copyToRoot()) return;
        } else {
            if (conceptBean.getNodeConcept() == null) {
                return;
            }
            // cas de déplacement d'un concept à concept        
            if(!copyToConcept()) return;
        }
        

        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexical_value()
                            + " -> "
                            + "Root");
        else
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexical_value()
                        + " -> "
                        + conceptBean.getNodeConcept().getTerm().getLexical_value());

  //      reloadTree();                
   //     reloadConcept();        
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    //    PrimeFaces.current().executeScript("PF('copyAndPasteBetweenTheso').hide();");
        reset();
    }    
    
    /**
     * deplacement entre concepts
     * @return 
     */
    private boolean copyToConcept(){
        // cas de déplacement d'un concept à concept
        FacesMessage msg;

        CopyAndPasteBetweenThesoHelper copyAndPasteBetweenThesoHelper = new CopyAndPasteBetweenThesoHelper();
        
            if(!copyAndPasteBetweenThesoHelper.pasteBranchLikeNT(connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    idThesoOrigin,
                    nodeConceptDrag.getConcept().getIdConcept(),
                    identifierType, 
                    currentUser.getNodeUser().getIdUser(),
                    roleOnThesoBean.getNodePreference())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    languageBean.getMsg("tools.copyBranch"), "Erreur de copie")); 
            } 
        return true;
    }
    
    private boolean copyToRoot(){
        FacesMessage msg;
        ConceptHelper conceptHelper = new ConceptHelper();         
        ArrayList<String> oldBtToDelete = new ArrayList<>();
/*
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptHelper.setTopConcept(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    oldIdBT,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }*/
        return true;
    }

    private void reloadConcept(){
        PrimeFaces pf = PrimeFaces.current();

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeConceptDrag.getConcept().getIdConcept());  

        // si le concept n'est pas déployé à doite, alors on ne fait rien
        if(conceptBean.getNodeConcept() != null){
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formRightTab:viewTabConcept:conceptView");
            }
        }      
    }
    
    private void reloadTree(){
        PrimeFaces pf = PrimeFaces.current();
        String lang;
        if (conceptBean.getNodeConcept() != null) {
            lang = conceptBean.getSelectedLang();
        } else {
            lang = selectedTheso.getCurrentLang();
        }

        tree.initAndExpandTreeToPath(nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                lang);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formLeftTab:tabTree:tree");
        }
        pf.executeScript("srollToSelected();");
    }
    

  
    public void rollBackAfterErrorOrCancelDragDrop() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
    }

    public boolean isIsCopyOn() {
        return isCopyOn;
    }

    public void setIsCopyOn(boolean isCopyOn) {
        this.isCopyOn = isCopyOn;
    }

    public NodeConcept getCuttedConcept() {
        return nodeConceptDrag;
    }

    public void setCuttedConcept(NodeConcept cuttedConcept) {
        this.nodeConceptDrag = cuttedConcept;
    }

    public NodeConcept getDropppedConcept() {
        return nodeConceptDrop;
    }

    public void setDropppedConcept(NodeConcept dropppedConcept) {
        this.nodeConceptDrop = dropppedConcept;
    }

    public NodeConcept getNodeConceptDrag() {
        return nodeConceptDrag;
    }

    public void setNodeConceptDrag(NodeConcept nodeConceptDrag) {
        this.nodeConceptDrag = nodeConceptDrag;
    }

    public NodeConcept getNodeConceptDrop() {
        return nodeConceptDrop;
    }

    public void setNodeConceptDrop(NodeConcept nodeConceptDrop) {
        this.nodeConceptDrop = nodeConceptDrop;
    }

    public boolean isIsDropToRoot() {
        return isDropToRoot;
    }

    public void setIsDropToRoot(boolean isDropToRoot) {
        this.isDropToRoot = isDropToRoot;
    }

    public boolean isIsValidPaste() {
        return isValidPaste;
    }

    public void setIsValidPaste(boolean isValidPaste) {
        this.isValidPaste = isValidPaste;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdThesoOrigin() {
        return idThesoOrigin;
    }

    public void setIdThesoOrigin(String idThesoOrigin) {
        this.idThesoOrigin = idThesoOrigin;
    }



}

package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import org.primefaces.PrimeFaces;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

@Named(value = "sessionControl")
@SessionScoped
public class SessionControl implements Serializable {

    @Inject
    private CurrentUser currentUser;

    @Inject
    private TreeGroups treeGroups;

    @Inject
    private TreeConcepts treeConcepts;

    @Inject
    private Tree tree;

    @Inject
    private ListIndex listIndex;

    @Inject
    private SelectedTheso selectedTheso;

    private final int DEFAULT_TIMEOUT_IN_MIN = 10;

    public void isTimeout() throws IOException {
        if (FacesContext.getCurrentInstance() == null) {
            return;
        }
        if (currentUser != null && currentUser.getNodeUser() != null) {
            currentUser.disconnect();
        } else {
            
        }

        //Vider le cache
        clearComponent();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        externalContext.invalidateSession();


        if(treeGroups != null) {
            PrimeFaces.current().executeScript("PF('treeWidget').clearCache();");
            PrimeFaces.current().executeScript("PF('groupWidget').clearCache();");
            PrimeFaces.current().executeScript("PF('conceptTreeWidget').clearCache();");            
            treeGroups.reset();
            treeConcepts.reset();
            tree.reset();
            listIndex.reset();
            selectedTheso.init();
            
        }
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());        
        FacesContext.getCurrentInstance().renderResponse();
 //       System.gc();

        
        // Rafraîchissement de la page

        System.gc();
        System.gc();
    //    System.runFinalization ();
    }

    private void clearGarbageCollector() {
        //récupérer les informations du système
        Runtime r = Runtime.getRuntime();
        try {
            //appeler les méthodes finalize() des objets
            try {
                //appeler les méthodes finalize() des objets
                r.runFinalization();
                //vider la mémoire
                r.gc();
                System.gc();
            } catch (Exception e) {
                //logger
                System.out.println("Erreur lors du vidage de la mémoire en mode forcé runFinalization");
            }
        } catch (Exception e) {
            //logger
            System.out.println("Erreur lors du vidage de la mémoire en mode forcé viderGarbageCollector");
        }
    }

    public void clearComponent() {
        UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();

        // for JSF 2 getFacetsAndChildren instead of only JSF 1 getChildren
        Iterator<UIComponent> children = root.getFacetsAndChildren();
        clearAllComponentInChilds(children);
    }

    private void clearAllComponentInChilds(Iterator<UIComponent> childrenIt) {

        while (childrenIt.hasNext()) {
            UIComponent component = childrenIt.next();
            if (component != null) {
                if (component instanceof HtmlInputText) {
                    HtmlInputText com = (HtmlInputText) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectOneMenu) {
                    HtmlSelectOneMenu com = (HtmlSelectOneMenu) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectBooleanCheckbox) {
                    HtmlSelectBooleanCheckbox com = (HtmlSelectBooleanCheckbox) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectManyCheckbox) {
                    HtmlSelectManyCheckbox com = (HtmlSelectManyCheckbox) component;
                    com.resetValue();
                }

                clearAllComponentInChilds(component.getFacetsAndChildren());
            }
        }

    }

    public int getTimeout() {
        int minNbr;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            minNbr = Integer.parseInt(bundlePref.getString("timeout_nbr_minute"));
        } catch (Exception e) {
            minNbr = DEFAULT_TIMEOUT_IN_MIN;
        }
        return (minNbr * 60 * 1000);
    }

}


package fr.cnrs.opentheso.core.exports;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import javax.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 */
public class UriHelper {

    private NodePreference nodePreference;
    private String idTheso;
    private HikariDataSource ds;
    
    public UriHelper(HikariDataSource ds, NodePreference nodePreference, String idTheso) {
        this.nodePreference = nodePreference;
        this.idTheso = idTheso;
        this.ds = ds;
    }
    
    
    
    /**
     * Cette fonction permet de retourner l'URI du groupe
     * Choix de l'URI pour l'export : 1 seule URI est possible pour l'export par groupe
     * 
     * @param idGroup
     * @param idArk
     * @param idHandle
     * @return 
     */
    public String getUriForGroup(String idGroup, String idArk, String idHandle) {
        String uri = "";
        if (idTheso == null || nodePreference == null) {
            return uri;
        }
        
        // type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (!StringUtils.isEmpty(idArk)) {
                uri = nodePreference.getOriginalUri()+ "/" + idArk;
                return uri;
            }
        }
        
        // type Handle
        if(nodePreference.isOriginalUriIsHandle()){
            if (!StringUtils.isEmpty(idHandle)) {
                uri = "https://hdl.handle.net/" + idHandle;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if(!StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + idGroup
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idg=" + idGroup
                            + "&idt=" + idTheso;
        }
        return uri;
    }    
    
    /**
     * Cette fonction permet de retourner l'URI du concept
     * Choix de l'URI pour l'export : 1 seule URI est possible pour l'export par concept 
     * @param idConcept
     * @param idArk
     * @param idHandle
     * @return 
     */
    public String getUriForConcept(String idConcept, String idArk, String idHandle) {
        String uri = "";
        if (idTheso == null || nodePreference == null) {
            return uri;
        }
        
        // type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (!StringUtils.isEmpty(idArk)) {
                uri = nodePreference.getOriginalUri()+ "/" + idArk;
                return uri;
            }
        }
        
        // type Handle
        if(nodePreference.isOriginalUriIsHandle()){
            if (!StringUtils.isEmpty(idHandle)) {
                uri = "https://hdl.handle.net/" + idHandle;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if(!StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            uri = nodePreference.getOriginalUri() + "/?idc=" + idConcept
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idc=" + idConcept
                            + "&idt=" + idTheso;
        }
        return uri;
    }
    
    public String getIdArk(String idConcept) {
        if(nodePreference.isOriginalUriIsArk()){
            ConceptHelper conceptHelper = new ConceptHelper();
            return conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso);
        }
        return null;
    }
    
    /**
     * permet de retourner le Path de l'application
     * exp:  //http://localhost:8082/opentheso2
     * @return
     */
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            return nodePreference.getOriginalUri();
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        return path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }    
    
}

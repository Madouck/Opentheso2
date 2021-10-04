/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

/**
 *
 * @author Miled.Rousset
 */
public class PreferencesHelper {

    /**
     * permet de retourner le code JavaScript de GoogleAnalytics
     */
    public String getCodeGoogleAnalytics(HikariDataSource ds) {
        String codeAnalytics = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("SELECT googleanalytics FROM info")) {
                    if (resultSet.next()) {
                        codeAnalytics = resultSet.getString("googleanalytics");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codeAnalytics;
    }

    /**
     * Permet de mettre à jour le code javaScript de GoogleAnalytics
     */
    public boolean setCodeGoogleAnalytics(HikariDataSource ds, String codeJavaScript) {

        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        codeJavaScript = stringPlus.addQuotes(codeJavaScript);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update info set googleanalytics = '" + codeJavaScript + "'");
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de retourner les préferences d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public NodePreference getThesaurusPreferences(HikariDataSource ds, String idThesaurus) {
        NodePreference np = null;

        if (idThesaurus == null || idThesaurus.isEmpty()) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, "IdThesaurus = " + idThesaurus + " Erreur à vérifier");
            return np;
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM preferences where id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        np = new NodePreference();
                        np.setSourceLang(resultSet.getString("source_lang"));
                        np.setIdentifierType(resultSet.getInt("identifier_type"));
                        np.setPreferredName(resultSet.getString("preferredname"));
                        np.setAuto_expand_tree(resultSet.getBoolean("auto_expand_tree"));

                        np.setSort_by_notation(resultSet.getBoolean("sort_by_notation"));
                        //np.setTree_cache(resultSet.getBoolean("tree_cache"));

                        // Ark
                        np.setUseArk(resultSet.getBoolean("use_ark"));
                        np.setServeurArk(resultSet.getString("server_ark"));
                        np.setUriArk(resultSet.getString("uri_ark"));
                        np.setIdNaan(resultSet.getString("id_naan"));
                        np.setPrefixArk(resultSet.getString("prefix_ark"));
                        np.setUserArk(resultSet.getString("user_ark"));
                        np.setPassArk(resultSet.getString("pass_ark"));
                        np.setGenerateHandle(resultSet.getBoolean("generate_handle"));

                        // Handle
                        np.setUseHandle(resultSet.getBoolean("use_handle"));
                        np.setUserHandle(resultSet.getString("user_handle"));
                        np.setPassHandle(resultSet.getString("pass_handle"));
                        np.setPathKeyHandle(resultSet.getString("path_key_handle"));
                        np.setPathCertHandle(resultSet.getString("path_cert_handle"));
                        np.setUrlApiHandle(resultSet.getString("url_api_handle"));
                        np.setPrefixIdHandle(resultSet.getString("prefix_handle"));
                        np.setPrivatePrefixHandle(resultSet.getString("private_prefix_handle"));

                        np.setPathImage(resultSet.getString("path_image"));
                        np.setDossierResize(resultSet.getString("dossier_resize"));
                        np.setBddActive(resultSet.getBoolean("bdd_active"));
                        np.setBddUseId(resultSet.getBoolean("bdd_use_id"));
                        np.setUrlBdd(resultSet.getString("url_bdd"));
                        np.setUrlCounterBdd(resultSet.getString("url_counter_bdd"));
                        np.setZ3950actif(resultSet.getBoolean("z3950actif"));
                        np.setCollectionAdresse(resultSet.getString("collection_adresse"));
                        np.setNoticeUrl(resultSet.getString("notice_url"));
                        np.setUrlEncode(resultSet.getString("url_encode"));
                        np.setPathNotice1(resultSet.getString("path_notice1"));
                        np.setPathNotice2(resultSet.getString("path_notice2"));
                        np.setCheminSite(resultSet.getString("chemin_site"));
                        np.setWebservices(resultSet.getBoolean("webservices"));
                        np.setOriginalUri(resultSet.getString("original_uri"));
                        np.setOriginalUriIsArk(resultSet.getBoolean("original_uri_is_ark"));
                        np.setOriginalUriIsHandle(resultSet.getBoolean("original_uri_is_handle"));
                        //np.setOriginalUriIsDoi(resultSet.getBoolean("original_uri_is_doi"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return np;
    }

    /**
     * Permet de retourner la langue de préference pour un thésaurus
     */
    public String getWorkLanguageOfTheso(HikariDataSource ds, String idThesourus) {
        String workLanguage = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "select source_lang from preferences where id_thesaurus = '" + idThesourus + "'")) {
                    if (resultSet.next()) {
                        workLanguage = resultSet.getString("source_lang");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return workLanguage;
    }

    /**
     * Permet de mettre à jour la langue source pour un thésaurus
     */
    public boolean setWorkLanguageOfTheso(HikariDataSource ds, String idLang, String idTheso) {

        boolean status = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update preferences set source_lang = '" + idLang
                        + "' where id_thesaurus = '" + idTheso + "'");
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean isWebservicesOn(HikariDataSource ds, String idThesaurus) {
        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "SELECT webservices FROM preferences where id_thesaurus = '" + idThesaurus + "'")) {
                    if (resultSet.next()) {
                        status = resultSet.getBoolean("webservices");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * retourne l'id du thésaurus d'après son nom dans les préferances
     */
    public String getIdThesaurusFromName(HikariDataSource ds, String thesoName) {
        String idTheso = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select id_thesaurus from preferences where preferredname ilike '"
                        + thesoName + "'")) {

                    if (resultSet.next()) {
                        idTheso = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idTheso;
    }

    /**
     * Cette fonction permet d'initialiser les préférences d'un thésaurus
     */
    public void initPreferences(HikariDataSource ds, String idThesaurus, String workLanguage) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into preferences (id_thesaurus,source_lang, preferredname)"
                        + " values ('" + idThesaurus + "', '" + workLanguage + "','" + idThesaurus + "')");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Permet de mettre à jour toutes les préférence
     * @param ds
     * @param np
     * @param idThesaurus
     * @return 
     */
    public boolean updateAllPreferenceUser(HikariDataSource ds, NodePreference np, String idThesaurus) {
        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        np = normalizeDatas(np);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "update preferences set "
                        + "source_lang='" + stringPlus.convertString(np.getSourceLang()) + "'"
                        //   + ", nb_alert_cdt='" + np.getNbAlertCdt() + "'"
                        //   + ", alert_cdt='" + np.isAlertCdt() + "'"
                        + ", identifier_type='" + np.getIdentifierType() + "'"
                        + ", preferredname='" + np.getPreferredName() + "'"
                        + ", auto_expand_tree='" + np.isAuto_expand_tree() + "'"
                        + ", tree_cache='" + np.isTree_cache() + "'"
                        + ", sort_by_notation='" + np.isSort_by_notation() + "'"
                        // Ark
                        + ", use_ark='" + np.isUseArk() + "'"
                        + ", server_ark='" + stringPlus.convertString(np.getServeurArk()) + "'"
                        + ", uri_ark='" + stringPlus.convertString(np.getUriArk()) + "'"
                        + ", id_naan='" + np.getIdNaan() + "'"
                        + ", prefix_ark ='" + np.getPrefixArk() + "'"
                        + ", user_ark='" + np.getUserArk() + "'"
                        //+ ", pass_ark='" + MD5Password.getEncodedPassword(np.getPassArk()) + "'"
                        + ", pass_ark='" + np.getPassArk() + "'"
                        + ", generate_handle='false'" //+ np.isGenerateHandle() + "'"
                        // Handle
                        + ", use_handle = '" + np.isUseHandle() + "'"
                        + ", user_handle = '" + np.getUserHandle() + "'"
                        + ", pass_handle = '" + np.getPassHandle() + "'"
                        + ", path_key_handle = '" + np.getPathKeyHandle() + "'"
                        + ", path_cert_handle = '" + np.getPathCertHandle() + "'"
                        + ", url_api_handle = '" + np.getUrlApiHandle() + "'"
                        + ", prefix_handle = '" + np.getPrefixIdHandle() + "'"
                        + ", private_prefix_handle = '" + np.getPrivatePrefixHandle() + "'"
                        + ", path_image='" + stringPlus.convertString(np.getPathImage()) + "'"
                        + ", dossier_resize='" + stringPlus.convertString(np.getDossierResize()) + "'"
                        + ", bdd_active='" + np.isBddActive() + "'"
                        + ", bdd_use_id='" + np.isBddUseId() + "'"
                        + ", url_bdd='" + stringPlus.convertString(np.getUrlBdd()) + "'"
                        + ", url_counter_bdd='" + stringPlus.convertString(np.getUrlCounterBdd()) + "'"
                        + ", z3950actif='" + np.isZ3950actif() + "'"
                        + ", collection_adresse='" + stringPlus.convertString(np.getCollectionAdresse()) + "'"
                        + ", notice_url='" + stringPlus.convertString(np.getNoticeUrl()) + "'"
                        + ", url_encode='" + stringPlus.convertString(np.getUrlEncode()) + "'"
                        + ", path_notice1='" + stringPlus.convertString(np.getPathNotice1()) + "'"
                        + ", path_notice2='" + stringPlus.convertString(np.getPathNotice2()) + "'"
                        + ", chemin_site='" + stringPlus.convertString(np.getCheminSite()) + "'"
                        + ", webservices='" + np.isWebservices() + "'"
                        + ", original_uri='" + stringPlus.convertString(np.getOriginalUri()) + "'"
                        + ", original_uri_is_ark=" + np.isOriginalUriIsArk()
                        + ", original_uri_is_handle=" + np.isOriginalUriIsHandle()
                        + ", original_uri_is_doi=" + np.isOriginalUriIsDoi()
                        + " WHERE"
                        + " id_thesaurus = '" + idThesaurus + "'";
                stmt.executeUpdate(query);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;
    }

    /**
     * Permet d'ajouter une nouvelle préférence
     *
     * @param ds
     * @param np
     * @param idThesaurus
     * @return 
     */
    public boolean addPreference(HikariDataSource ds, NodePreference np, String idThesaurus) {
        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        np = normalizeDatas(np);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "insert into preferences("
                        + "id_thesaurus, source_lang, identifier_type, path_image,"
                        + " dossier_resize, bdd_active, bdd_use_id, url_bdd,"
                        + " url_counter_bdd, z3950actif, collection_adresse,"
                        + " notice_url, url_encode, path_notice1, path_notice2,"
                        + " chemin_site, webservices, use_ark, server_ark, uri_ark,"
                        + " id_naan, prefix_ark, user_ark, pass_ark, generate_handle,"
                        + " use_handle,"
                        + " user_handle, pass_handle, path_key_handle, path_cert_handle,"
                        + " url_api_handle, prefix_handle, private_prefix_handle, preferredname, auto_expand_tree, original_uri,"
                        + " original_uri_is_ark, original_uri_is_handle,original_uri_is_doi, tree_cache, sort_by_notation)"
                        + " values('" + idThesaurus + "'"
                        + ",'" + stringPlus.convertString(np.getSourceLang()) + "'"
                        + ",'" + np.getIdentifierType() + "'"
                        + ",'" + stringPlus.convertString(np.getPathImage()) + "'"
                        + ",'" + stringPlus.convertString(np.getDossierResize()) + "'"
                        + ",'" + np.isBddActive() + "'"
                        + ",'" + np.isBddUseId() + "'"
                        + ",'" + stringPlus.convertString(np.getUrlBdd()) + "'"
                        + ",'" + stringPlus.convertString(np.getUrlCounterBdd()) + "'"
                        + ",'" + np.isZ3950actif() + "'"
                        + ",'" + stringPlus.convertString(np.getCollectionAdresse()) + "'"
                        + ",'" + stringPlus.convertString(np.getNoticeUrl()) + "'"
                        + ",'" + stringPlus.convertString(np.getUrlEncode()) + "'"
                        + ",'" + stringPlus.convertString(np.getPathNotice1()) + "'"
                        + ",'" + stringPlus.convertString(np.getPathNotice2()) + "'"
                        + ",'" + stringPlus.convertString(np.getCheminSite()) + "'"
                        + ",'" + np.isWebservices() + "'"
                        // Ark
                        + ",'" + np.isUseArk() + "'"
                        + ",'" + stringPlus.convertString(np.getServeurArk()) + "'"
                        + ",'" + stringPlus.convertString(np.getUriArk()) + "'"
                        + ",'" + np.getIdNaan() + "'"
                        + ",'" + np.getPrefixArk() + "'"
                        + ",'" + np.getUserArk() + "'"
                        //+ ", pass_ark='" + MD5Password.getEncodedPassword(np.getPassArk()) + "'"
                        + ",'" + np.getPassArk() + "'"
                        + ",'" + np.isGenerateHandle() + "'"
                        // Handle
                        + ",'" + np.isUseHandle() + "'"
                        + ",'" + np.getUserHandle() + "'"
                        + ",'" + np.getPassHandle() + "'"
                        + ",'" + np.getPathKeyHandle() + "'"
                        + ",'" + np.getPathCertHandle() + "'"
                        + ",'" + np.getUrlApiHandle() + "'"
                        + ",'" + np.getPrefixIdHandle() + "'"
                        + ",'" + np.getPrivatePrefixHandle() + "'"
                        + ",'" + np.getPreferredName() + "'"
                        + "," + np.isAuto_expand_tree()
                        + ",'" + np.getOriginalUri() + "'"
                        + "," + np.isOriginalUriIsArk()
                        + "," + np.isOriginalUriIsHandle()
                        + "," + np.isOriginalUriIsDoi()
                        + ",'" + np.isTree_cache() + "'"
                        + ",'" + np.isSort_by_notation() + "'"
                        + ")";
                stmt.executeUpdate(query);
                status = true;

            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;
    }

    /**
     * permet de nettoyer les "/" et préparer les paramètres correctement
     */
    private NodePreference normalizeDatas(NodePreference nodePreference) {

        // vérification des "/" à la fin 
        if (!nodePreference.getCheminSite().isEmpty()) {
            if (!nodePreference.getCheminSite().substring(nodePreference.getCheminSite().length() - 1, nodePreference.getCheminSite().length()).equalsIgnoreCase("/")) {
                nodePreference.setCheminSite(nodePreference.getCheminSite() + "/");
            }
        }
        if (!nodePreference.getServeurArk().isEmpty()) {
            if (!nodePreference.getServeurArk().substring(nodePreference.getServeurArk().length() - 1, nodePreference.getServeurArk().length()).equalsIgnoreCase("/")) {
                nodePreference.setServeurArk(nodePreference.getServeurArk() + "/");
            }
        }
        if (!nodePreference.getPathImage().isEmpty()) {
            if (!nodePreference.getPathImage().substring(nodePreference.getPathImage().length() - 1, nodePreference.getPathImage().length()).equalsIgnoreCase("/")) {
                nodePreference.setPathImage(nodePreference.getPathImage() + "/");
            }
        }
        return nodePreference;
    }

}

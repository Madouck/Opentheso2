/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.csv;

import fr.cnrs.opentheso.skosapi.SKOSCreator;
import fr.cnrs.opentheso.skosapi.SKOSDate;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSNotation;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import java.io.ByteArrayOutputStream;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;


public class WriteCSV {

    private String seperate;
    private BufferedWriter writer;
    private ByteArrayOutputStream output;

    /**
     * export un thésaurus en format csv
     *
     * @param xmlDocument
     * @param langs
     */
    public WriteCSV(SKOSXmlDocument xmlDocument, List<String> langs, String seperate) {

        try {
            this.seperate = seperate;
            // create a writer
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            // write header record
            StringBuilder header = new StringBuilder();
            header.append("URI").append(seperate)
                    .append("rdf:type").append(seperate);

            langs.forEach((lang) -> {
                header.append("skos:prefLabel@").append(lang).append(seperate);
            });

            langs.forEach((lang) -> {
                header.append("skos:altLabel@").append(lang).append(seperate);
            });

            header.append("skos:hiddenLabel@").append(langs.get(0)).append(seperate);

            langs.forEach((lang) -> {
                header.append("skos:definition@").append(lang).append(seperate);
            });

            header.append("skos:scopeNote@").append(langs.get(0)).append(seperate)
                    .append("skos:notation").append(seperate)
                    .append("skos:narrower").append(seperate)
                    .append("skos:broader").append(seperate)
                    .append("skos:related").append(seperate)
                    .append("skos:exactMatch").append(seperate)
                    .append("skos:closeMatch").append(seperate)
                    .append("geo:lat").append(seperate)
                    .append("geo:long").append(seperate)
                    .append("skos:member").append(seperate)
                    .append("dct:created").append(seperate)
                    .append("dct:modified").append(seperate);

            writer.write(header.toString());
            writer.newLine();

            xmlDocument.getGroupList().forEach(groupe -> {
                try {
                    writeResource(groupe, "skos:Collection", langs);
                } catch (Exception e){ }
            });

            // write all concepts
            xmlDocument.getConceptList().forEach(concept -> {
                try {
                    writeResource(concept, "skos:Concept", langs);
                } catch (Exception e){ }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {

        }
    }

    private void writeResource(SKOSResource skosResource, String type, List<String> langs) throws IOException {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(skosResource.getUri()).append(seperate) //URI
                .append(type).append(seperate) ;//rdf:type
        
        for (String lang : langs) {
            stringBuffer.append(getLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.prefLabel)).append(seperate); //skos:prefLabel
        }
        
        for (String lang : langs) {
            stringBuffer.append(getLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.altLabel)).append(seperate);  //skos:altLabel
        }
        
        stringBuffer.append(getLabelValue(skosResource.getLabelsList(), langs.get(0), SKOSProperty.hiddenLabel)).append(seperate);//hiddenLabel
        
        for (String lang : langs) {
            stringBuffer.append("'" + getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.definition) + "'").append(seperate);//definition
        }
        
        stringBuffer.append(getDocumentationValue(skosResource.getDocumentationsList(), langs.get(0), SKOSProperty.scopeNote)).append(seperate)//scopeNote
                .append(getNotation(skosResource.getNotationList())).append(seperate) //notation
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.narrower)).append(seperate) //narrower
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.broader)).append(seperate) //broader
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.related)).append(seperate) //related
                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.exactMatch)).append(seperate) //exactMatch
                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.closeMatch)).append(seperate) //closeMatch
                .append(getLatValue(skosResource.getGPSCoordinates())).append(seperate)//geo:lat
                .append(getLongValue(skosResource.getGPSCoordinates())).append(seperate)//geo:long
                .append(getMemberValue(skosResource.getCreatorList())).append(seperate)//skos:member
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.created)).append(seperate)//sdct:created
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.modified)).append(seperate);//dct:modified
        writer.write(stringBuffer.toString());
        writer.newLine();
    }

    private String getLatValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            return coordinates.getLat();
        }
        return "";
    }

    private String getLongValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            return coordinates.getLon();
        }
        return "";
    }

    private String getMemberValue(List<SKOSCreator> creators) {
        if (!CollectionUtils.isEmpty(creators)) {
            return creators.get(0).getCreator();
        }
        return "";
    }

    private String getRelationGivenValue(List<SKOSRelation> relations, int propertie) {
        String value = "";
        for (SKOSRelation relation : relations) {
            if (relation.getProperty() == propertie) {
                value = relation.getTargetUri();
                break;
            }
        }
        return value;
    }

    private String getAlligementValue(List<SKOSMatch> matchs, int propertie) {
        String value = "";
        for (SKOSMatch date : matchs) {
            if (date.getProperty() == propertie) {
                value = date.getValue();
                break;
            }
        }
        return value;
    }

    public String getNotation(List<SKOSNotation> notations) {
        if (!CollectionUtils.isEmpty(notations)) {
            return notations.get(0).getNotation();
        }
        return "";
    }

    private String getDateValue(List<SKOSDate> dates, int propertie) {
        String value = "";
        for (SKOSDate date : dates) {
            if (date.getProperty() == propertie) {
                value = date.getDate();
                break;
            }
        }
        return value;
    }

    private String getLabelValue(List<SKOSLabel> labels, String lang, int propertie) {
        String value = "";
        for (SKOSLabel label : labels) {
            if (label.getProperty() == propertie && label.getLanguage().equals(lang)) {
                value = label.getLabel();
                break;
            }
        }
        return value;
    }

    private String getDocumentationValue(ArrayList<SKOSDocumentation> documentations, String lang, int propertie) {
        String value = "";
        for (SKOSDocumentation document : documentations) {
            if (document.getProperty() == propertie && document.getLanguage().equals(lang)) {
                value = document.getText();
                break;
            }
        }
        return value;
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }

}

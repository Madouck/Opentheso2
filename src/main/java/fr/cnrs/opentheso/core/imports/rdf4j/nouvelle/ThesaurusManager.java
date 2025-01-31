package fr.cnrs.opentheso.core.imports.rdf4j.nouvelle;

import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.eclipse.rdf4j.model.Resource;


public class ThesaurusManager {


    public void addStructData(SKOSResource skosResource, Resource subject, SKOSXmlDocument sKOSXmlDocument, int prop) {

        skosResource.setProperty(prop);

        switch (prop) {
            case SKOSProperty.ConceptScheme:
                sKOSXmlDocument.setConceptScheme(skosResource);
                break;
            case SKOSProperty.FACET:
                sKOSXmlDocument.addFacet(skosResource);
                break;
            case SKOSProperty.ConceptGroup:
            case SKOSProperty.Collection:
            case SKOSProperty.Theme:
            case SKOSProperty.MicroThesaurus:
                sKOSXmlDocument.addGroup(skosResource);
                break;
            case SKOSProperty.Concept:
                sKOSXmlDocument.addconcept(skosResource);
                break;
            case SKOSProperty.FoafImage:
                sKOSXmlDocument.addFoafImage(skosResource);
                break;
        }
    }
}

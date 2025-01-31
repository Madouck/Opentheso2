package fr.cnrs.opentheso.core.exports.rdf4j;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;

import fr.cnrs.opentheso.skosapi.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.OWL;


public class WriteRdf4j {

    public final static String DELIMINATE = "##";
    public final static String STATUS_TAG = "status";
    public final static String DISCUSSION_TAG = "message";
    public final static String VOTE_TAG = "vote";

    private Model model;
    private ModelBuilder builder;
    private SKOSXmlDocument xmlDocument;
    private ValueFactory vf;

    
    public WriteRdf4j(SKOSXmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
        vf = SimpleValueFactory.getInstance();
        loadModel();
        writeModel();
        model = builder.build();
    }

    public void closeCache() {
        model.clear();
        model = null;
        vf = null;
        builder = null;
    }

    private void loadModel() {
        builder = new ModelBuilder();
        builder.setNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
        builder.setNamespace("dc", "http://purl.org/dc/elements/1.1/");
        builder.setNamespace("dcterms", "http://purl.org/dc/terms/");
        builder.setNamespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        builder.setNamespace("iso-thes", "http://purl.org/iso25964/skos-thes#");
        builder.setNamespace("opentheso", "http://purl.org/umu/uneskos#");
        builder.setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        builder.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
    }

    private void writeModel() {
        writeConceptScheme();
        writeGroup();
        writeFacet();
        writeConcept();
    }

    private void writeConcept() {
        for (SKOSResource sResource : xmlDocument.getConceptList()) {
            builder.subject(vf.createIRI(sResource.getUri()));
            builder.add(RDF.TYPE, SKOS.CONCEPT);
            writeLabel(sResource);
            writeRelation(sResource);
            writeMatch(sResource);
            writeNotation(sResource);
            writeDate(sResource);
            writeIdentifier(sResource);
            writePath(sResource);
            writeAgent(sResource);
            writeDocumentation(sResource);
            writeGPS(sResource);
            writeExternalResources(sResource);

            writeStatusCandidat(sResource);
            writeDiscussions(sResource);
            writeVotes(sResource);

            writeStatus(sResource);
            writeReplaces(sResource);

            writeNodeImage(sResource);
        }
    }

    private void writeNodeImage(SKOSResource resource) {
        for (NodeImage nodeImage : resource.getNodeImage()) {
            if (StringUtils.isEmpty(nodeImage.getUri())) {
                continue;
            }

            builder.subject(vf.createIRI(nodeImage.getUri().replaceAll(" ", "%20")));
            builder.add(RDF.TYPE, FOAF.IMAGE);
            builder.add(DCTERMS.IDENTIFIER, resource.getSdc().getIdentifier());
            if (!StringUtils.isEmpty(nodeImage.getImageName())) {
                builder.add(DCTERMS.TITLE, nodeImage.getImageName());
            }
            if (!StringUtils.isEmpty(nodeImage.getCopyRight())) {
                builder.add(DCTERMS.RIGHTS, nodeImage.getCopyRight());
            }
        }
    }

    private void writeReplaces(SKOSResource resource) {
        for (SKOSReplaces replace : resource.getsKOSReplaces()) {
            switch (replace.getProperty()) {
                case SKOSProperty.isReplacedBy:
                    builder.add(DCTERMS.IS_REPLACED_BY, vf.createIRI(replace.getTargetUri()));
                    break;
                case SKOSProperty.replaces:
                    builder.add(DCTERMS.REPLACES, vf.createIRI(replace.getTargetUri()));
                    break;
            }
        }
    }

    private void writeStatus(SKOSResource resource) {
        if (SKOSProperty.deprecated == resource.getStatus()) {
            builder.add(OWL.DEPRECATED, true);
        }
    }

    private void writeFacet() {
        for (SKOSResource facet : xmlDocument.getFacetList()) {
            builder.subject(vf.createIRI(facet.getUri()));
            builder.add(RDF.TYPE, vf.createIRI("http://purl.org/iso25964/skos-thes#ThesaurusArray"));
            writeRelation(facet);
            writeLabel(facet);
            writeDate(facet);
        }
    }

    private void writeGroup() {
        for (SKOSResource group : xmlDocument.getGroupList()) {
            builder.subject(vf.createIRI(group.getUri()));
            builder.add(RDF.TYPE, SKOS.COLLECTION);
            writeLabel(group);
            writeRelation(group);
            writeMatch(group);
            writeNotation(group);
            writeDate(group);
            writeIdentifier(group);
            writeAgent(group);
            writeDocumentation(group);
            writeGPS(group);

        }
    }

    private void writeConceptScheme() {
        if (xmlDocument.getConceptScheme() == null) {
            return;
        }
        SKOSResource conceptScheme = xmlDocument.getConceptScheme();
        builder.subject(vf.createIRI(conceptScheme.getUri()));//createURI(conceptScheme.getUri()));

        builder.add(RDF.TYPE, SKOS.CONCEPT_SCHEME);

        writeLabel(conceptScheme);
        writeRelation(conceptScheme);
        writeMatch(conceptScheme);
        writeNotation(conceptScheme);
        writeDate(conceptScheme);
        writeIdentifier(conceptScheme);
        writeAgent(conceptScheme);
        writeDocumentation(conceptScheme);
        writeGPS(conceptScheme);
        writeDcTerms(conceptScheme);
    }

    private void writeGPS(SKOSResource resource) {
        if (StringUtils.isNotEmpty(resource.getGPSCoordinates().getLat())
                && StringUtils.isNotEmpty(resource.getGPSCoordinates().getLon())) {
            builder.add("geo:lat", Double.valueOf(resource.getGPSCoordinates().getLat()));
            builder.add("geo:long", Double.valueOf(resource.getGPSCoordinates().getLon()));
        }
    }

    private void writeExternalResources(SKOSResource resource) {
        for (String externalResource : resource.getExternalResources()) {
            builder.add(DCTERMS.SOURCE, externalResource);
        }
    }

    private void writeDocumentation(SKOSResource resource) {
        for (SKOSDocumentation doc : resource.getDocumentationsList()) {
            switch (doc.getProperty()) {
                case SKOSProperty.definition:
                    builder.add(SKOS.DEFINITION, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.scopeNote:
                    builder.add(SKOS.SCOPE_NOTE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.example:
                    builder.add(SKOS.EXAMPLE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.historyNote:
                    builder.add(SKOS.HISTORY_NOTE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.editorialNote:
                    builder.add(SKOS.EDITORIAL_NOTE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.changeNote:
                    builder.add(SKOS.CHANGE_NOTE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
                case SKOSProperty.note:
                    builder.add(SKOS.NOTE, vf.createLiteral(doc.getText(), doc.getLanguage()));
                    break;
            }
        }
    }

    private void writeAgent(SKOSResource resource) {
        for (SKOSAgent agent : resource.getAgentList()) {
            if (ObjectUtils.isNotEmpty(agent)) {
                switch (agent.getProperty()) {
                    case SKOSProperty.creator:
                        if(!StringUtils.isEmpty(agent.getAgent())){
                            builder.add(DCTERMS.CREATOR, agent.getAgent());
                        }
                        break;
                    case SKOSProperty.contributor:
                        if(!StringUtils.isEmpty(agent.getAgent())){
                            builder.add(DCTERMS.CONTRIBUTOR, agent.getAgent());
                        }
                        break;                        
                    default:
                        throw new AssertionError();
                }
            }
        }
    }

    private void writeDate(SKOSResource resource) {
        for (SKOSDate date : resource.getDateList()) {
            switch (date.getProperty()) {
                case SKOSProperty.created:
                    builder.add(DCTERMS.CREATED, vf.createLiteral(date.getDate(), XMLSchema.DATE));
                    break;
                case SKOSProperty.modified:
                    builder.add(DCTERMS.MODIFIED, vf.createLiteral(date.getDate(), XMLSchema.DATE));
                    break;
                case SKOSProperty.date:
                    builder.add(DCTERMS.DATE, vf.createLiteral(date.getDate(), XMLSchema.DATE));
                    break;
            }
        }
    }

    /**
     * Pour écrire le chemin complet / autopostage
     */
    private void writePath(SKOSResource resource) {
        if (CollectionUtils.isNotEmpty(resource.getPaths())) {
            for (String path : resource.getPaths()) {
                builder.add(DCTERMS.DESCRIPTION, path.trim());
            }
        }
    }

    private void writeIdentifier(SKOSResource resource) {
        if (ObjectUtils.isNotEmpty(resource.getSdc())) {
            builder.add(DCTERMS.IDENTIFIER, resource.getSdc().getIdentifier());
        }
    }

    private void writeNotation(SKOSResource resource) {
        for (SKOSNotation notation : resource.getNotationList()) {
            if (ObjectUtils.isNotEmpty(notation) && StringUtils.isNotEmpty(notation.getNotation())) {
                builder.add(SKOS.NOTATION, notation.getNotation());
            }
        }
    }

    private void writeDiscussions(SKOSResource resource) {
        for (SKOSDiscussion discussion : resource.getMessages()) {
            builder.add(SKOS.NOTE, vf.createLiteral(discussion.getDate() 
                    + DELIMINATE 
                    + discussion.getIdUser() 
                    + DELIMINATE 
                    + discussion.getMsg(), DISCUSSION_TAG));
        }
    }

    private void writeVotes(SKOSResource resource) {
        for (SKOSVote vote : resource.getVotes()) {
            builder.add(SKOS.NOTE, vf.createLiteral(vote.getIdThesaurus() + DELIMINATE + vote.getIdConcept() + DELIMINATE + vote.getValueNote()
                    + DELIMINATE + vote.getIdUser() + DELIMINATE + vote.getTypeVote(), VOTE_TAG));
        }
    }

    private void writeLabel(SKOSResource resource) {
        for (SKOSLabel label : resource.getLabelsList()) {
            switch (label.getProperty()) {
                case SKOSProperty.prefLabel:
                    builder.add(SKOS.PREF_LABEL, vf.createLiteral(label.getLabel(), label.getLanguage()));
                    break;
                case SKOSProperty.altLabel:
                    builder.add(SKOS.ALT_LABEL, vf.createLiteral(label.getLabel(), label.getLanguage()));
                    break;
                case SKOSProperty.hiddenLabel:
                    builder.add(SKOS.HIDDEN_LABEL, vf.createLiteral(label.getLabel(), label.getLanguage()));
                    break;
            }
        }
    }

    private void writeDcTerms(SKOSResource resource) {
        if (StringUtils.isNotEmpty(resource.getThesaurus().getTitle())) {
            builder.add(DCTERMS.TITLE, resource.getThesaurus().getTitle());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getCreator())) {
            builder.add(DCTERMS.CREATOR, resource.getThesaurus().getCreator());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getContributor())) {
            builder.add(DCTERMS.CONTRIBUTOR, resource.getThesaurus().getContributor());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getPublisher())) {
            builder.add(DCTERMS.PUBLISHER, resource.getThesaurus().getPublisher());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getDescription())) {
            builder.add(DCTERMS.DESCRIPTION, resource.getThesaurus().getDescription());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getType())) {
            builder.add(DCTERMS.TYPE, resource.getThesaurus().getType());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getRights())) {
            builder.add(DCTERMS.RIGHTS, resource.getThesaurus().getRights());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getSubject())) {
            builder.add(DCTERMS.SUBJECT, resource.getThesaurus().getSubject());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getCoverage())) {
            builder.add(DCTERMS.COVERAGE, resource.getThesaurus().getCoverage());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getLanguage())) {
            builder.add(DCTERMS.LANGUAGE, resource.getThesaurus().getLanguage());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getRelation())) {
            builder.add(DCTERMS.RELATION, resource.getThesaurus().getRelation());
        }

        if (StringUtils.isNotEmpty(resource.getThesaurus().getSource())) {
            builder.add(DCTERMS.SOURCE, resource.getThesaurus().getSource());
        }
    }

    private void writeMatch(SKOSResource resource) {
        for (SKOSMatch match : resource.getMatchList()) {
            switch (match.getProperty()) {
                case SKOSProperty.exactMatch:
                    builder.add(SKOS.EXACT_MATCH, vf.createIRI(match.getValue()));
                    break;
                case SKOSProperty.closeMatch:
                    builder.add(SKOS.CLOSE_MATCH, vf.createIRI(match.getValue()));
                    break;
                case SKOSProperty.broadMatch:
                    builder.add(SKOS.BROAD_MATCH, vf.createIRI(match.getValue()));
                    break;
                case SKOSProperty.relatedMatch:
                    builder.add(SKOS.RELATED_MATCH, vf.createIRI(match.getValue()));
                    break;
                case SKOSProperty.narrowMatch:
                    builder.add(SKOS.NARROW_MATCH, vf.createIRI(match.getValue()));
                    break;
            }
        }
    }

    private void writeStatusCandidat(SKOSResource resource) {
        if (resource.getSkosStatus() != null && !StringUtils.isEmpty(resource.getSkosStatus().getIdStatus())) {
            builder.add(SKOS.NOTE, vf.createLiteral(resource.getSkosStatus().getIdStatus() + DELIMINATE + resource.getSkosStatus().getMessage()
                    + DELIMINATE + resource.getSkosStatus().getIdUser() + DELIMINATE + resource.getSkosStatus().getDate(), STATUS_TAG));
        }
    }

    private void writeRelation(SKOSResource resource) {

        for (SKOSRelation relation : resource.getRelationsList()) {
            switch (relation.getProperty()) {
                case SKOSProperty.member:
                    builder.add(SKOS.MEMBER, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.broader:
                    builder.add(SKOS.BROADER, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.broaderGeneric:
                    builder.add("iso-thes:broaderGeneric", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.broaderInstantial:
                    builder.add("iso-thes:broaderInstantial", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.broaderPartitive:
                    builder.add("iso-thes:broaderPartitive", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.narrower:
                    builder.add(SKOS.NARROWER, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.narrowerGeneric:
                    builder.add("iso-thes:narrowerGeneric", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.narrowerInstantial:
                    builder.add("iso-thes:narrowerInstantial", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.narrowerPartitive:
                    builder.add("iso-thes:narrowerPartitive", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.related:
                    builder.add(SKOS.RELATED, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.relatedHasPart:
                    builder.add("iso-thes:relatedHasPart", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.relatedPartOf:
                    builder.add("iso-thes:relatedPartOf", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.hasTopConcept:
                    builder.add(SKOS.HAS_TOP_CONCEPT, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.inScheme:
                    builder.add(SKOS.IN_SCHEME, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.topConceptOf:
                    builder.add(SKOS.TOP_CONCEPT_OF, vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.subGroup:
                    builder.add("iso-thes:subGroup", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.microThesaurusOf:
                    builder.add("iso-thes:microThesaurusOf", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.superGroup:
                    builder.add("iso-thes:superGroup", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.memberOf:
                    builder.add("opentheso:memberOf", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.superOrdinate:
                    builder.add("iso-thes:superOrdinate", vf.createIRI(relation.getTargetUri()));
                    break;
                case SKOSProperty.subordinateArray:
                    builder.add("iso-thes:subordinateArray", vf.createIRI(relation.getTargetUri()));
            }
        }
    }
    
    public Model getModel() {
        return model;
    }

    public SKOSXmlDocument getsKOSXmlDocument() {
        return xmlDocument;
    }

    public void setsKOSXmlDocument(SKOSXmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

}

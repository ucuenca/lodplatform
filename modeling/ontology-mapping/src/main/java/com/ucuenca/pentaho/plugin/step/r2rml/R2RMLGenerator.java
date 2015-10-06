package com.ucuenca.pentaho.plugin.step.r2rml;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;
import com.ucuenca.misctools.DatabaseLoader;
import com.ucuenca.misctools.LOVApiV2;
import com.ucuenca.misctools.PrefixCCLookUp;
import com.ucuenca.pentaho.plugin.step.ontologymapping.OntoMapData;
import com.ucuenca.pentaho.plugin.step.ontologymapping.OntoMapMeta;

/**
 * R2RML mapping file processor
 * @author depcc
 *
 */
public class R2RMLGenerator {
	
	private OntoMapMeta meta;
	private OntoMapData data;
	
	private String sqlClassificationRows = "SELECT ID, ONTOLOGY, ENTITY, RELATIVE_URI, URI_FIELD_ID,"
			+ " DATAFIELD_1, DATAVALUE_1, DATAFIELD_2, DATAVALUE_2, DATAFIELD_3, DATAVALUE_3 FROM ";
	private String sqlAnnotationRows = "SELECT ID, ENTITY_CLASSID, ONTOLOGY, PROPERTY, EXTRACTIONFIELD,"
			+ " DATAFIELD, DATAVALUE, DATATYPE, LANGUAGE  FROM ";
	private String sqlRelationRows = "SELECT ID, ENTITY_CLASSID_1, ONTOLOGY, PROPERTY, ENTITY_CLASSID_2"
			+ " FROM ";
	
	private static final String FIELD_AS_FUNCTION_PATTERN = "(.*)\\$\\{(.*)\\}(.*)";
	private static final String FIELD_EXTRACTION_PATTERN = "\\$\\{(.*)\\}";
	
	private String baseURI;
	private Model r2rmlModel;
	private Map<String, String[]> prefixes = new HashMap<String, String[]>();
	private Map<String, Object[]> mappedEntities = new HashMap<String, Object[]>();
	
	public R2RMLGenerator(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		try {
			meta = (OntoMapMeta) smi;
			data = (OntoMapData) sdi;
			baseURI =  meta.getMapBaseURI();
			sqlClassificationRows += data.CLASSIFICATIONTABLE + " WHERE TRANSID = ? AND STEPID = ?";
			sqlAnnotationRows += data.ANNOTATIONTABLE + " WHERE TRANSID = ? AND STEPID = ? AND ENTITY_CLASSID = ?";
			sqlRelationRows += data.RELATIONTABLE + " WHERE TRANSID = ? AND STEPID = ?";
			r2rmlModel = ModelFactory.createDefaultModel();
			this.setModelDefaultNS();
			DatabaseLoader.getConnection();
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Assing default namespace prefixes to model
	 * @throws Exception
	 */
	private void setModelDefaultNS() throws Exception {
		for(NSPrefix prefix:NSPrefix.values()) {
			r2rmlModel.setNsPrefix(prefix.getPrefix(), prefix.getURI());
		}
	}
	
	/**
	 * R2RML mapping file processor
	 * @throws KettleException
	 */
	public void process() throws KettleException {
		try {
			ResultSet rsClass = this.getClassificationRows();
			while(rsClass.next()) {
				ClassificationBean classBean = new ClassificationBean(rsClass);
				String relativeURI = classBean.getRelativeUri() != null && classBean.getRelativeUri().length() >0
						&& classBean.getRelativeUri().matches("([a-z0-9]*)(\\/|#)") ? classBean.getRelativeUri():"";
				String id = classBean.getId();
				String entityId = classBean.getUriFieldId();
				String entityRealId = "";
				if(entityId.matches(FIELD_AS_FUNCTION_PATTERN)) { //there is a function involved with a field
					Pattern pattern = Pattern.compile(FIELD_EXTRACTION_PATTERN); // extract field name
			        Matcher matcher = pattern.matcher(entityId);
			        if (matcher.find()) {
			        	String field = matcher.group(1).toUpperCase().replaceAll(" ", "_");
			        	/*Pattern patternFnc = Pattern.compile("\\.(.*)"); // extract its function
			            Matcher matcherFnc = patternFnc.matcher(id);
			            String function = matcherFnc.find() ? matcherFnc.group(1):null;
			            */
			            /*String sqlField = entityId.replaceAll("\\$\\{(.*)\\}", classBean.getFieldName(field));
			            entityRealId = this.processFieldFunction(data.CLASSIFICATIONTABLE, 
			            		sqlField, "ID = '" + id + "'");*/
			        	
			        	//entityRealId = entityId.replaceAll("\\$\\{(.*)\\}", classBean.getFieldName(field));
			        	entityRealId = entityId.replaceAll(FIELD_EXTRACTION_PATTERN, field);        
			        }
				} else {
					entityRealId = entityId.toUpperCase().replaceAll(" ", "_");
				}
				String vocPrefix = classBean.getOntology();
				//this.getOntologyURI(vocPrefix);
				this.getLOVOntologyURI(vocPrefix);
				String vocEntity = classBean.getEntity();
				Resource resource = r2rmlModel.createResource("#TriplesMap"+ id);
				resource.addProperty(RR.logicalTable, 
						r2rmlModel.createResource()
							.addProperty(RR.sqlQuery, this.getEntitySQLDefinition(entityRealId, classBean))
					)
					.addProperty(RR.subjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.template, this.baseURI + relativeURI + "{" + id + "_ID}")
							.addProperty(RR.cclass, /*this.getOntologyURI(vocPrefix) +*/
									ResourceFactory.createProperty( prefixes.get(vocPrefix)[0], 
											vocEntity.split(prefixes.get(vocPrefix)[0])[1] )
									)
					);
				mappedEntities.put(id, new Object[]{relativeURI, entityRealId, classBean});

				this.processAnnotationModeling(id);
			}
			this.processRelationModeling();
			finishProcess();
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Mapping Annotation Rules
	 * @param classId Classification Record ID
	 * @throws Exception
	 */
	private void processAnnotationModeling(String classId)throws Exception {
		String entityIDfromRelation = getEntityFieldIDfromParent(classId);
		ResultSet rs = this.getAnnotationRows(classId);
		while(rs.next()) {
			AnnotationBean annBean =  new AnnotationBean(rs);
			String id = annBean.getId();
			String vocPrefix = annBean.getOntology();
			String vocProperty = annBean.getProperty();
			//this.getOntologyURI(vocPrefix);
			this.getLOVOntologyURI(vocPrefix);
			Resource resource = r2rmlModel.createResource("#TriplesMap"+ id);
			
			Resource objectMapResource = r2rmlModel.createResource();
			objectMapResource.addProperty(RR.column, id + "_DATA");
			if(annBean.getDatatype().length() > 0) {
				objectMapResource.addProperty(RR.datatype, 
						ResourceFactory.createProperty( XSD.getURI(), annBean.getDatatype() ));
			}
			if(annBean.getLanguage().length() > 0) {
				objectMapResource.addProperty(RR.language, annBean.getLanguage() );
			}
			String sqlDefinition = entityIDfromRelation != null ? 
					this.getPropertySQLDefinition(classId, entityIDfromRelation, annBean):
						this.getPropertySQLDefinition(classId, annBean);
			
			resource.addProperty(RR.logicalTable, 
					r2rmlModel.createResource()
						.addProperty(RR.sqlQuery, sqlDefinition)
				)
				.addProperty(RR.subjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.template, this.baseURI + mappedEntities.get(classId)[0] + "{" + classId + "_ID}")
				)
				.addProperty(RR.predicateObjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.predicate,
									ResourceFactory.createProperty( prefixes.get(vocPrefix)[0], 
											vocProperty.split(prefixes.get(vocPrefix)[0])[1] )
							)
							.addProperty(RR.objectMap, objectMapResource)
				);
		}
	}
	
	/**
	 * Obtaints the FieldID for Child-Defined Entities
	 * @param classID classification ID that establish the Child-defined Entity
	 * @return
	 * @throws Exception
	 */
	private String getEntityFieldIDfromParent(String classID)throws Exception {
		String fieldID = null;
		String relationQuery = "SELECT ENTITY_CLASSID_1 FROM RELATIONMAPPING WHERE ";
		String[] condField = new String[]{"TRANSID", "STEPID", "ENTITY_CLASSID_2"};
		String[] condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getParentStepMeta().getName().toUpperCase(), classID};
		relationQuery = this.generateSQLPredicate(relationQuery, condField, condValue);
		String parentRelationQuery = "SELECT URI_FIELD_ID FROM CLASSMAPPING WHERE ";
		condField = new String[]{"TRANSID", "STEPID"};
		condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getParentStepMeta().getName().toUpperCase()};
		parentRelationQuery = this.generateSQLPredicate(parentRelationQuery, condField, condValue);
		parentRelationQuery += " AND ID IN(" + relationQuery + ")";
		ResultSet rs = DatabaseLoader.executeQuery(parentRelationQuery);
		while(rs.next()) {
			String entityId = rs.getString(1);
			if(entityId.matches(FIELD_AS_FUNCTION_PATTERN)) { //there is a function involved with a field
				Pattern pattern = Pattern.compile(FIELD_EXTRACTION_PATTERN); // extract field name
		        Matcher matcher = pattern.matcher(entityId);
		        if (matcher.find()) {
		        	fieldID = matcher.group(1).toUpperCase().replaceAll(" ", "_");        
		        }
			} else {
				fieldID = entityId.toUpperCase().replaceAll(" ", "_");
			}
			break;
		}
		return fieldID;
	}
	
	/**
	 * Mapping Relation Rules
	 * @throws Exception
	 */
	private void processRelationModeling()throws Exception {
		ResultSet rs = this.getRelationRows();
		while(rs.next()) {
			RelationBean relBean =  new RelationBean(rs);
			String id = relBean.getId();
			String entity1 = relBean.getEntityclassid1();
			String vocPrefix = relBean.getOntology();
			String vocProperty = relBean.getProperty();
			String entity2 = relBean.getEntityclassid2();
			Object []entityProp1 = mappedEntities.get(entity1);
			Object []entityProp2 = mappedEntities.get(entity2);
			//this.getOntologyURI(vocPrefix);
			this.getLOVOntologyURI(vocPrefix);
			Resource resource = r2rmlModel.createResource("#TriplesMap"+ id);
			resource.addProperty(RR.logicalTable, 
					r2rmlModel.createResource()
						.addProperty(RR.sqlQuery, this.getRelationSQLDefinition(relBean))
				)
				.addProperty(RR.subjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.template, this.baseURI + (String)entityProp1[0] + "{" + entity1 + "_ID}")
				);
				//fixing problem with URI without LOD URI name conventions
				Property vocab = (vocProperty.split(prefixes.get(vocPrefix)[0]).length > 1) ?
						ResourceFactory.createProperty( prefixes.get(vocPrefix)[0], 
								vocProperty.split(prefixes.get(vocPrefix)[0])[1] )
						:ResourceFactory.createProperty(vocProperty);
				resource.addProperty(RR.predicateObjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.predicate, vocab)
							.addProperty(RR.objectMap,
									r2rmlModel.createResource()
									.addProperty(RR.template, this.baseURI + (String)entityProp2[0] + "{" + entity2 + "_ID}")
							)
				);
		}
	}
	
	/**
	 * Get Classification rules from Database
	 * @return
	 * @throws Exception
	 */
	private ResultSet getClassificationRows() throws Exception {
		return DatabaseLoader.executeQuery(sqlClassificationRows, 
				new Object[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), meta.getParentStepMeta().getName().toUpperCase()});
	}
	
	/**
	 * Generate SQL Query for table definition on entities
	 * @param entityId ID Field
	 * @param classBean Classification bean
	 * @return SQL Query as String
	 * @throws Exception
	 */
	private String getEntitySQLDefinition(String entityId, ClassificationBean classBean) throws Exception {
		String classId = classBean.getId();
		String sql = "SELECT " + entityId.toUpperCase() + " AS " + classId + "_ID FROM " + meta.getDataDbTable()
				+ " WHERE ";
		String[] condField = new String[]{"TRANSID", "STEPID", classBean.getDatafield1(), classBean.getDatafield2(), classBean.getDatafield3()};
		String[] condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getDataStepName().toUpperCase(), classBean.getDatavalue1(), classBean.getDatavalue2(), classBean.getDatavalue3()};
		
		return this.generateSQLPredicate(sql, condField, condValue);
	}
	
	/**
	 * Get Annotation rules from Database
	 * @param classId Classification rule id
	 * @return
	 * @throws Exception
	 */
	private ResultSet getAnnotationRows(String classId) throws Exception {
		return DatabaseLoader.executeQuery(sqlAnnotationRows, 
				new Object[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getParentStepMeta().getName().toUpperCase(), classId});
	}
	
	/**
	 * Generate SQL Query for table definition on properties
	 * @param classId Entity field ID
	 * @param annBean Annotation bean
	 * @return SQL query as String
	 * @throws Exception
	 */
	private String getPropertySQLDefinition(String classId, AnnotationBean annBean) throws Exception {
		String annId = annBean.getId();
		String sql = "SELECT " + annBean.getExtractionfield().toUpperCase() + " AS " + annId + "_DATA, "
				+ ((String)mappedEntities.get(classId)[1]).toUpperCase() + " AS " + classId + "_ID"
				+ " FROM " + meta.getDataDbTable()
				+ " WHERE ";
		String[] condField = new String[]{"TRANSID", "STEPID", annBean.getDatafield()};
		String[] condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getDataStepName().toUpperCase(), annBean.getDatavalue()};
		
		return this.generateSQLPredicate(sql, condField, condValue);
	}
	
	/**
	 * Generates SQL Query for Child-defined Entity properties
	 * @param classId Entity Field ID
	 * @param fieldIDfromRelation Field ID of Parent Entity
	 * @param annBean Annotation field
	 * @return
	 * @throws Exception
	 */
	private String getPropertySQLDefinition(String classId, String fieldIDfromRelation, AnnotationBean annBean) throws Exception {
		ClassificationBean classBean = (ClassificationBean)mappedEntities.get(classId)[2];
		String entityUriID = classBean.getUriFieldId();
		String entityID = entityUriID.matches(FIELD_AS_FUNCTION_PATTERN) ?
				entityUriID.replaceAll(FIELD_EXTRACTION_PATTERN, fieldIDfromRelation)
				:fieldIDfromRelation.toUpperCase().replaceAll(" ", "_");
		String annId = annBean.getId();
		String sql = "SELECT " + annBean.getExtractionfield().toUpperCase() + " AS " + annId + "_DATA, "
				+ entityID + " AS " + classId + "_ID"
				+ " FROM " + meta.getDataDbTable()
				+ " WHERE ";
		String[] condField = new String[]{"TRANSID", "STEPID", annBean.getDatafield()};
		String[] condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getDataStepName().toUpperCase(), annBean.getDatavalue()};
		
		return this.generateSQLPredicate(sql, condField, condValue);
	}
	
	/**
	 * Get Relation rules from Database
	 * @return
	 * @throws Exception
	 */
	private ResultSet getRelationRows() throws Exception {
		return DatabaseLoader.executeQuery(sqlRelationRows, 
				new Object[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getParentStepMeta().getName().toUpperCase()});
	}
	
	/**
	 * Generate SQL Query for table definition to generate relation mapping between entities
	 * @param relBean Relation bean
	 * @return SQL query as String
	 * @throws Exception
	 */
	private String getRelationSQLDefinition(RelationBean relBean) throws Exception {
		String entityId1 = relBean.getEntityclassid1();
		String entityId2 = relBean.getEntityclassid2();
		Object[] entityProp1 = mappedEntities.get(entityId1);
		Object[] entityProp2 = mappedEntities.get(entityId2);
		String sql = "SELECT " + ((String)entityProp1[1]).toUpperCase() + " AS " + entityId1 + "_ID, "
				+ ((String)entityProp2[1]).toUpperCase() + " AS " + entityId2 + "_ID"
				+ " FROM " + meta.getDataDbTable()
				+ " WHERE ";
		ClassificationBean classBean2 = (ClassificationBean)entityProp2[2];
		String[] condField = new String[]{"TRANSID", "STEPID", classBean2.getDatafield1(),
				classBean2.getDatafield2(), classBean2.getDatafield3()};
		String[] condValue = new String[]{meta.getParentStepMeta().getParentTransMeta().getName().toUpperCase(), 
				meta.getDataStepName().toUpperCase(), classBean2.getDatavalue1(), 
					classBean2.getDatavalue2(), classBean2.getDatavalue3()};
		
		return this.generateSQLPredicate(sql, condField, condValue);
	}
	
	/**
	 * SQL predicate constructor for table definitions
	 * @param sqlQuery SQL query defitinion without predicate
	 * @param condFields Conditional fields
	 * @param condValues Data fields
	 * @return
	 */
	private String generateSQLPredicate(String sqlQuery, String[] condFields, String condValues[]) {
		for(int i=0;i<condFields.length;i++) {
			if(condFields[i] != null && condFields[i].length() > 0 
					&& condValues[i] != null && condValues[i].length() > 0) {
				sqlQuery += (i == 0) ? "": " AND ";
				sqlQuery += condFields[i].toUpperCase() 
						+ ((i < 2) ? " = '":" LIKE '") 
						+ condValues[i] + "'";
			}
		}
		return sqlQuery;
	}
	
	/**
	 * Query ontology base URI from prefix.cc service and added it to the prefix stack
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	private String getOntologyURI(String prefix) throws Exception {
		prefix = prefix.trim();
		String URI = "";
		if(prefixes.containsKey(prefix)) {
			URI = prefixes.get(prefix)[0];
		} else {
			URI = PrefixCCLookUp.queryService(prefix);
			prefixes.put(prefix, new String[]{URI});
			r2rmlModel.setNsPrefix(prefix, URI);
		}
		return URI;
	}
	
	/**
	 * Query ontology base URI from LOV service and added it to the prefix stack
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	private String getLOVOntologyURI(String prefix) throws Exception {
		prefix = prefix.trim();
		String URI = "";
		if(prefixes.containsKey(prefix)) {
			URI = prefixes.get(prefix)[0];
		} else {
			List<String> URIList = LOVApiV2.vocabularySearch(prefix);
			prefixes.put(prefix, URIList.toArray(new String[URIList.size()]));
			URI = URIList.get(0);
			r2rmlModel.setNsPrefix(prefix, URI);
		}
		return URI;
	}
	
	/**
	 * Final process to generate R2RML file 
	 * @throws Exception
	 */
	private void finishProcess() throws Exception{
		String url = meta.getOutputDir() + System.getProperty("file.separator") + meta.getOutFileName();
		FileOutputStream out = new FileOutputStream(url);
		r2rmlModel.write(out, "TURTLE");
		DatabaseLoader.closeConnection();
	}
	
	/**
	 * Return R2RML model sentences as a List
	 * @return ArrayList with model sententes
	 */
	public List<String[]> getModelSentences() {
		List<String[]> rowSet = new ArrayList<String[]>();
		StmtIterator iter = r2rmlModel.listStatements();
		while (iter.hasNext()) {
		    Statement stmt      = iter.nextStatement();
		    Resource  subject   = stmt.getSubject();
		    Property  predicate = stmt.getPredicate();
		    RDFNode   object    = stmt.getObject();
	    	rowSet.add( new String[]{subject.toString(), predicate.toString(), object.toString()} );
		}
		return rowSet;
	}

}

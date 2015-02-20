package com.ucuenca.pentaho.plugin.step.r2rml;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.ucuenca.misctools.DatabaseLoader;
import com.ucuenca.misctools.PrefixCCLookUp;
import com.ucuenca.pentaho.plugin.step.ontologymapping.OntoMapData;
import com.ucuenca.pentaho.plugin.step.ontologymapping.OntoMapMeta;

public class R2RMLGenerator {
	
	private OntoMapMeta meta;
	private OntoMapData data;
	
	private String sqlClassificationRows = "SELECT ID, ONTOLOGY, ENTITY, RELATIVE_URI, URI_FIELD_ID, "
			+ "DATAFIELD_1, DATAVALUE_1, DATAFIELD_2, DATAVALUE_2, DATAFIELD_3, DATAVALUE_3 FROM ";
	private String sqlAnnotationRows = "SELECT ID, ENTITY_CLASSID, ONTOLOGY, PROPERTY,"
			+ " DATAFIELD, DATAVALUE FROM ";
	private String sqlRelationRows = "SELECT * FROM ";
	
	private String baseURI;
	private Model r2rmlModel;
	private Map<String, String> prefixes = new HashMap<String, String>();
	
	private static R2RMLGenerator instance;
	
	private R2RMLGenerator(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
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
	
	private void setModelDefaultNS() throws Exception {
		for(NSPrefix prefix:NSPrefix.values()) {
			r2rmlModel.setNsPrefix(prefix.getPrefix(), prefix.getURI());
		}
	}
	
	public static R2RMLGenerator getInstance(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		if(instance == null) {
			instance = new R2RMLGenerator(smi, sdi);
		}
		return instance;
	}
	
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
				if(entityId.matches("(.*)\\$\\{(.*)\\}(.*)")) { //there is a function involved with a field
					Pattern pattern = Pattern.compile("\\$\\{(.*)\\}"); // extract field name
			        Matcher matcher = pattern.matcher(entityId);
			        if (matcher.find()) {
			        	String field = StringUtils.capitalize( matcher.group(1) );
			        	/*Pattern patternFnc = Pattern.compile("\\.(.*)"); // extract its function
			            Matcher matcherFnc = patternFnc.matcher(id);
			            String function = matcherFnc.find() ? matcherFnc.group(1):null;
			            */
			            /*String sqlField = entityId.replaceAll("\\$\\{(.*)\\}", classBean.getFieldName(field));
			            entityRealId = this.processFieldFunction(data.CLASSIFICATIONTABLE, 
			            		sqlField, "ID = '" + id + "'");*/
			        	entityRealId = entityId.replaceAll("\\$\\{(.*)\\}", classBean.getFieldName(field));
			        	
			            
			        }
				} else {
					entityRealId = entityId;
				}
				String vocPrefix = classBean.getOntology();
				//this.getOntologyURI(vocPrefix);
				String vocEntity = classBean.getEntity();
				Resource resource = r2rmlModel.createResource("<#TriplesMap"+id);
				resource.addProperty(RR.logicalTable, 
						r2rmlModel.createResource()
							.addProperty(RR.tableName, meta.getDataDbTable())
					)
					.addProperty(RR.subjectMap,
						r2rmlModel.createResource()
							.addProperty(RR.template, this.baseURI + relativeURI + "{" + entityRealId + "}")
							.addProperty(RR.cclass, this.getOntologyURI(vocPrefix) + vocEntity)
					);
				/*r2rmlModel.createResource(this.baseURI + relativeURI + entityRealId)
						.addProperty(RDF.type, this.getOntologyURI(vocPrefix) + vocEntity);*/
				this.processAnnotationModeling(id, resource);
				//r2rmlModel.createStatement(null, null, null);
			}
			finishProcess();
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	private void processAnnotationModeling(String classId, Resource parent)throws Exception {
		ResultSet rs = this.getAnnotationRows(classId);
		while(rs.next()) {
			AnnotationBean annBean =  new AnnotationBean(rs);
			//String id = annBean.getId();
			String vocPrefix = annBean.getOntology();
			parent.addProperty(RR.predicateObjectMap, 
				r2rmlModel.createResource()
					.addProperty(RR.predicate, this.getOntologyURI(vocPrefix) + annBean.getProperty())
					.addProperty(RR.objectMap,
						r2rmlModel.createResource()
						.addProperty(RR.column, annBean.getDatafield())
					)
				);
			
		}
	}
	
	private ResultSet getClassificationRows() throws Exception {
		return DatabaseLoader.executeQuery(sqlClassificationRows, 
				new Object[]{data.getTransName(), data.getStepName()});
	}
	
	private ResultSet getAnnotationRows(String classId) throws Exception {
		return DatabaseLoader.executeQuery(sqlAnnotationRows, 
				new Object[]{data.getTransName(), data.getStepName(), classId});
	}
	
	private String processFieldFunction(String table, String field, String pk) throws KettleException {
		/*Method method = bean.getClass().getDeclaredMethod("get" + field);
		String value = (String)method.invoke(bean);*/
		String returnValue = null;
		String sqlField = "SELECT " + field.toUpperCase() + " FROM " 
		+ table + " WHERE TRANSID = ? AND STEPID = ?" + "" + pk;
		try {
			ResultSet rs = DatabaseLoader.executeQuery(sqlField, 
					new Object[]{data.getTransName(), data.getStepName()});
			while(rs.next()) {
				returnValue = rs.getString(1);
				break;
			}
		}catch(Exception e) {
			throw new KettleException("ERROR WHILE TRYING TO EXECUTE FUNCTION MANIPULATION ==>" + field, e);
		}
		return returnValue;
	}
	
	private String getOntologyURI(String prefix) throws Exception {
		String URI = "";
		if(prefixes.containsKey(prefix)) {
			URI = prefixes.get(prefix);
		} else {
			URI = PrefixCCLookUp.queryService(prefix);
			prefixes.put(prefix, URI);
		}
		return URI;
	}
	
	private void finishProcess() throws Exception{
		r2rmlModel.write(System.out, "N-TRIPLES");
		DatabaseLoader.closeConnection();
	}

}

package config.api.pack;

import java.io.*;
import java.util.Properties;

public class Config {

	private Properties properties;
	private String ProjectPath;
	private String ontschema;
	private String dataset;
	private String tdb;
	private String index;
	
	public Config(String ontschema, String dataset, String tdb, String index, String dbdocpath, String ProjectPath) {
		this.ontschema = ontschema;
		this.ProjectPath = ProjectPath;
		this.dataset = dataset;
		this.tdb = tdb; 
		this.index = index;
	}
	

	public Config() {
		this.ProjectPath = "D:\\workspace\\WhyNot";
		Properties property=new Properties();
    	String configFilePath = ProjectPath + "/config/config.properties"; 
    	
    	try {
    		 FileInputStream inputFile = new FileInputStream(configFilePath);
             property.load(inputFile);
             setProperties(property);
             inputFile.close();
        } catch (FileNotFoundException ex) {
             System.out.println("Can not find the configuration file.");
             ex.printStackTrace();
        } catch (IOException ex) {
             System.out.println("Loading configuration file failed!");
             ex.printStackTrace();
        }
    	this.ontschema = property.getProperty("ontschema");
		this.dataset = property.getProperty("dataset");
		this.tdb = property.getProperty("tdb");
		this.index = property.getProperty("index");
	}
	
	public boolean StoreNewConfig(String ProjectPath) throws IOException{
		String configFilePath = ProjectPath + "/conf/config.properties"; 
		
		Properties p= this.properties;
		try {
			FileOutputStream fos = new FileOutputStream(configFilePath);
			p.store(fos, null);
		} catch (IOException e) {
			System.out.println("IO Failure.");
			return false;
		}
		return true;		
	}
	
	public Properties getProperties() {
		return properties;
	}


	public void setProperties(Properties properties) {
		this.properties = properties;
	}


	public String getProjectPath() {
		return ProjectPath;
	}


	public void setProjectPath(String projectPath) {
		ProjectPath = projectPath;
	}

	
	public String getOntschema() {
		return ontschema;
	}


	public void setOntschema(String ontschema) {
		this.ontschema = ontschema;
	}


	public String getDataset() {
		return dataset;
	}


	public void setDataset(String dataset) {
		this.dataset = dataset;
	}


	public String getTdb() {
		return tdb;
	}


	public void setTdb(String tdb) {
		this.tdb = tdb;
	}


	public String getIndex() {
		return index;
	}


	public void setIndex(String index) {
		this.index = index;
	}

}

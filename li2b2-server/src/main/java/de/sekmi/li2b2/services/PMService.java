package de.sekmi.li2b2.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import de.sekmi.li2b2.api.pm.ProjectManager;
import de.sekmi.li2b2.api.pm.User;
import de.sekmi.li2b2.api.pm.Project;

import de.sekmi.li2b2.hive.Credentials;
import de.sekmi.li2b2.hive.HiveException;
import de.sekmi.li2b2.hive.HiveRequest;
import de.sekmi.li2b2.hive.HiveResponse;
import de.sekmi.li2b2.hive.I2b2Constants;
import de.sekmi.li2b2.hive.pm.Cell;
import de.sekmi.li2b2.hive.pm.Param;
import de.sekmi.li2b2.hive.pm.UserProject;

@Singleton
@Path(PMService.SERVICE_URL)
public class PMService extends AbstractPMService{
	private static final Logger log = Logger.getLogger(PMService.class.getName());
	public static final String SERVICE_URL = "/i2b2/services/PMService/";

	private List<Cell> otherCells;
	private ProjectManager manager;
	
	public PMService() throws HiveException{
		otherCells = new ArrayList<>(4);
		registerCell(new Cell("ONT", "OntologyService", OntologyService.SERVICE_PATH));
		registerCell(new Cell("WORK", "WorkplaceSevice", WorkplaceService.SERVICE_PATH));
		registerCell(new Cell("CRC", "QueryToolService", AbstractCRCService.SERVICE_PATH));
	}
	

	@Inject
	public void setProjectManager(ProjectManager manager){
		this.manager = manager;
	}
	public void registerCell(Cell cell){
		otherCells.add(cell);
	}
	/**
	 * Information used by the official webclient (as of v1.7.07c):
	 * project/[id,role='DATA_AGG',]
	 * cell_data[id,name,project_path,url], cell_data/param
	 * @param requestBody
	 * @return
	 * @throws HiveException
	 * @throws ParserConfigurationException
	 * @throws JAXBException 
	 */
	@POST
	@Path("getServices")
	@Produces(MediaType.APPLICATION_XML)
	public Response getServices(InputStream requestBody, @Context UriInfo uri) throws HiveException, ParserConfigurationException, JAXBException{
		return super.handleRequest(requestBody);
	}

	@Override
	public String getCellId() {
		return "PM";
	}

	private static void appendProject(Element parent, Project project){
		Element el = parent.getOwnerDocument().createElement("project");
		parent.appendChild(el);
		el.setAttribute("id", project.getId());
		appendTextElement(el, "name", project.getName());
		appendTextElement(el, "key", "K_"+project.getId()); // XXX what for?
		//appendTextElement(el, "wiki", "li2b2");
		appendTextElement(el, "description", "About "+project.getName());
		appendTextElement(el, "path", project.getPath());
		appendTextElement(el, "user_name", "demo"); // XXX what for?
	}

	@Override
	protected void getAllProject(HiveResponse response) {
		Element el = response.addBodyElement(I2b2Constants.PM_NS, "projects");
		for( Project project : manager.getProjects() ){
			appendProject(el, project);
		}
		
	}


	@Override
	protected void getProject(HiveResponse response, String projectId, String path) {
		// TODO Auto-generated method stub
		
	}


	private static void appendRole(Element parent, String project, String user, String role){
		Element el = parent.getOwnerDocument().createElement("role");
		parent.appendChild(el);
		appendTextElement(el, "project_id", project);
		appendTextElement(el, "user_name", user);
		appendTextElement(el, "role", role);
	}
	private static void appendRoles(Element parent, Project project, User user){
		for( String role : project.getUserRoles(user) ){
			appendRole(parent, project.getId(), user.getName(), role);
		}		
	}
	@Override
	protected void getAllRoles(HiveResponse response, String projectId, String userId) {
		Element el = response.addBodyElement(I2b2Constants.PM_NS, "roles");
		if( projectId == null ){
			// roles for all projects
			if( userId == null ){
				// all projects and all users
				
			}else{
				// all projects for specific users
			}
		}else{
			// roles for specific project
			Project project = manager.getProjectById(projectId);
			if( userId == null ){
				// roles for all users in the specified project
				for( User user : manager.getUsers() ){
					appendRoles(el, project, user);
				}
			}else{
				// roles for specific user in project
				User user = manager.getUserById(userId);
				appendRoles(el, project, user);
			}
		}
	}


	@Override
	protected void getAllProjectParams(HiveResponse response, String projectId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void getAllHive(HiveResponse response) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void getAllCells(HiveResponse response, String projectId) throws JAXBException {
		JAXBContext jaxb = JAXBContext.newInstance(Cell.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		Element el = response.addBodyElement(I2b2Constants.PM_NS, "cell_datas");
		for( Cell cell : otherCells ){
			marshaller.marshal(cell, el);
		}
	}


	@Override
	protected void getCell(HiveResponse response, String id, String path) {
		// TODO Auto-generated method stub
		
	}

	private void appendUser(Element parent, User user){
		// TODO compare to official response (elements and order)
		Element el = parent.getOwnerDocument().createElementNS("","user");
		parent.appendChild(el);
		appendTextElement(el, "full_name", user.getFullName());
		appendTextElement(el, "user_name", user.getName());
		appendTextElement(el, "email", "not@supported.yet");
		appendTextElement(el, "domain", user.getDomain());
		appendTextElement(el, "is_admin", "true");
	}

	@Override
	protected void getAllUsers(HiveResponse response) {
		Element el = response.addBodyElement(I2b2Constants.PM_NS, "users");
		for( User user : manager.getUsers() ){
			appendUser(el, user);
		}
	}


	@Override
	protected void getUser(HiveResponse response, String userId) {
		// TODO does the official server send a 'users' wrapper?
		Element el = response.addBodyElement(I2b2Constants.PM_NS, "users");
		User user = manager.getUserById(userId);
		if( user != null ){
			appendUser(el, user);
		}
	}


	@Override
	protected void getUserConfiguration(HiveRequest req, HiveResponse resp, String projectId) throws JAXBException {
		Credentials cred = req.getSecurity();
		User user;
		if( cred.isToken() ){
			// need session manager
			user = null;
		}else if( manager != null ){
			// check user manager
			user = manager.getUserById(cred.getUser());
			if( user != null && user.hasPassword(cred.getPassword().toCharArray()) ){
				// user authenticated
				log.info("Valid user login: "+cred.getUser());
				// TODO create session
			}else{
				// user or password not valid
				log.info("Invalid credentials: "+cred.getUser());
				user = null;
			}
		}else{
			user = null;
			// no user manager
		}
		if( user == null ){
			// login failed
			resp.setResultStatus("ERROR", "Authentication failed");
			return;
		}
		JAXBContext jaxb = JAXBContext.newInstance(Cell.class,UserProject.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		// webclient only users configure/[full_name|is_admin], project/[name|path}
		Element el = resp.addBodyElement(I2b2Constants.PM_NS, "configure");
		appendTextElement(el, "environment", "DEVELOPMENT");
		appendTextElement(el, "helpURL", "https://github.com/rwm/li2b2");
		// user info
		Element ue = (Element)el.appendChild(el.getOwnerDocument().createElementNS("","user"));
		appendTextElement(ue, "full_name", user.getFullName());
		appendTextElement(ue, "user_name", user.getName());
		// TODO session/password
		appendTextElement(ue, "password", "SessionKey:XXXX");
		appendTextElement(ue, "domain", user.getDomain());
		appendTextElement(ue, "is_admin", "true");
		// add projects
		for( Project project : user.getProjects() ){
			// fill project
			UserProject up = new UserProject();
			up.id = project.getId();
			up.name = project.getName();
			up.path = project.getPath();
			Set<String> roles = project.getUserRoles(user);
			up.role = new String[roles.size()];
			roles.toArray(up.role);
			// param demo
			up.params = new Param[]{
					new Param("announcement","This is a demo of the <span style='color:orange;font-weight:bold'>li2b2 server</span>.")
					,new Param("Software","<span style='color:orange;font-weight:bold'>li2b2 server</span>")
			};
			// append
			marshaller.marshal(up, ue);
		}
		appendTextElement(el, "domain_name", cred.getDomain());
		appendTextElement(el, "domain_id", "i2b2");
		appendTextElement(el, "active", "true");

		// add cells
		Element cells = (Element)el.appendChild(el.getOwnerDocument().createElementNS("","cell_datas"));
		for( Cell cell : otherCells ){
			marshaller.marshal(cell, cells);
		}
		// TODO remove xmlns="" from elements 'user', 'cell_datas' (which was needed for JAXB)
		
//		for( Cell cell : this.otherCells ){
//			marshaller.marshal(cell, cells);
//		}
	}


	@Override
	protected void setPassword(HiveResponse response, Credentials cred, String newPassword) {
		User user = manager.getUserById(cred.getUser());
		if( user == null ){
			response.setResultStatus("ERROR", "User not found");
		}
		user.setPassword(newPassword.toCharArray());
	}


	@Override
	protected void setProject(HiveResponse response, String id, String name, String key, String wiki,
			String description, String path) {
		Project project = manager.getProjectById(id);
		if( project == null ){
			// create project
			manager.addProject(id, name);
		}else{
			// TODO set path
		}
		// TODO set other attributes
	}


	@Override
	protected void setUser(HiveResponse response, String userId, String fullName, String email, boolean admin,
			String password) {
		User user = manager.getUserById(userId);
		if( user == null ){
			user = manager.addUser(userId);
		}
		// TODO set other attributes
		user.setPassword(password.toCharArray());
	}
}

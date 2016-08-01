package de.sekmi.li2b2.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.sekmi.li2b2.hive.HiveException;
import de.sekmi.li2b2.hive.HiveRequest;

@Path(QueryToolService.SERVICE_PATH)
public class QueryToolService extends AbstractService{

	private static final Logger log = Logger.getLogger(QueryToolService.class.getName());
	public static final String SERVICE_PATH="/i2b2/services/QueryToolService/";
	
	public QueryToolService() throws HiveException {
		super();
		// TODO Auto-generated constructor stub
	}

	@POST
	@Path("request")
	public Response request(InputStream requestBody) throws HiveException{
		HiveRequest req = parseRequest(requestBody);
		Element psm_header = (Element)req.getMessageBody().getFirstChild();
		Element request = (Element)req.getMessageBody().getLastChild();
		// get request type
		NodeList nl = psm_header.getElementsByTagName("request_type");
		String type = null;
		if( nl.getLength() != 0 ){
			type = nl.item(0).getTextContent();
		}
//
//		Element req = null;
//		if( sib != null && sib.getNodeType() == Node.ELEMENT_NODE ){
//			req = (Element)sib;
//		}
		
		return request(type, request);
	}
	
	private Response request(String type, Element request){
		String rtype = null;
		if( request != null ){
			rtype = request.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
		}
		log.info("Request:"+type+" type="+rtype);
		if( type.equals("CRC_QRY_getResultType") ){
			InputStream xml = getClass().getResourceAsStream("/templates/crc/resulttype.xml");
			if( xml == null ){
				log.warning("resulttype.xml not found");
			}
			return Response.ok(xml).build();
		}else if( type.equals("CRC_QRY_getQueryMasterList_fromUserId") ){
			return Response.ok(getClass().getResourceAsStream("/templates/crc/masterlist.xml")).build();
		}else if( type.equals("CRC_QRY_runQueryInstance_fromQueryDefinition") ){
			// XXX
			log.info("Run query: "+request.getChildNodes().item(0).getNodeName()+", "+request.getChildNodes().item(1).getNodeName());
			return Response.ok(getClass().getResourceAsStream("/templates/crc/master_instance_result.xml")).build();
		}else if( type.equals("CRC_QRY_deleteQueryMaster") ){
			// XXX
			return Response.noContent().build();
		}else{
			// XXX
			return Response.noContent().build();			
		}
	}

	@Override
	public String getCellId() {
		return "CRC";
	}

}
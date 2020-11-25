package org.bonitasoft.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.properties.BonitaProperties;
import org.bonitasoft.web.extension.page.PageResourceProvider;
import org.json.simple.JSONValue;

public class ExplorerParameters {

    static Logger logger = Logger.getLogger(ExplorerParameters.class.getName());

    private final static BEvent eventSaveParametersError = new BEvent(ExplorerParameters.class.getName(), 1, Level.ERROR,
            "Save parameters failed", "Error during save parameters", "Parameters are not saved", "Check the exception");
    private final static BEvent eventSaveParametersOk = new BEvent(ExplorerParameters.class.getName(), 2, Level.SUCCESS,
            "Parameters saved", "Parameters saved with success");

    private final static BEvent eventLoadParametersError = new BEvent(ExplorerParameters.class.getName(), 3, Level.ERROR,
            "Load parameters error", "Error when parameters are loaded", "Parameters can't be retrieved", "Check the exception");

    private PageResourceProvider pageResourceProvider;

    private Map<String, Object> parameters = new HashMap<>();

    public ExplorerParameters(PageResourceProvider pageResourceProvider) {
        this.pageResourceProvider = pageResourceProvider;
    }

    public enum TYPEDATASOURCE  { BONITASERVER, EXTERNAL }
    public TYPEDATASOURCE getTypeExternalDatasource () {
        try
        {
            String typeDatasource= (String) parameters.get("typedatasource");
            return TYPEDATASOURCE.valueOf( typeDatasource);
        } catch(Exception e ) {
            return TYPEDATASOURCE.EXTERNAL;
        }
    }
    public String getExternalDataSource() {
        return (String) parameters.get("datasource");
    }

    /**
     * Policy Overview : use the Process Overview or the Display Variable?
     * 
     *
     */
    public enum POLICYOVERVIEW { DISPLAY, PROCESSOVERVIEW};
    public POLICYOVERVIEW getPolicyOverview() {
        try {
            POLICYOVERVIEW policyOverview = POLICYOVERVIEW.valueOf( parameters.get("overviewpolicy").toString().toUpperCase());
            return policyOverview;
        }
        catch(Exception e) {
            return POLICYOVERVIEW.DISPLAY;
        }
    }
    
    public String getBonitaServerUrl() {
        return (String) parameters.get("bonitaserverurl");
    }
    /**
     * 
     * @param parameters
     */
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public List<BEvent> load(boolean firstLoad) {
        BonitaProperties bonitaProperties = new BonitaProperties(pageResourceProvider);
        if (firstLoad)
            bonitaProperties.setCheckDatabase(true);
        else
            bonitaProperties.setCheckDatabase(false);

        parameters.clear();
        List<BEvent> listEvents = new ArrayList<>();
        try {
            listEvents.addAll(bonitaProperties.load());
            
            String options = bonitaProperties.getProperty("options");
            if (options!=null) {
                @SuppressWarnings("unchecked")
                Map<String,Object> optionMap = (Map<String, Object>) JSONValue.parse(options);                
                parameters.putAll( optionMap);
            }
        } catch (Exception e) {
            logger.severe("Exception " + e.toString());
            listEvents.add(new BEvent(eventLoadParametersError, e, "Error :" + e.getMessage()));
        }

        return listEvents;
    }

    public List<BEvent> save() {
        BonitaProperties bonitaProperties = new BonitaProperties(pageResourceProvider);
        bonitaProperties.setCheckDatabase(false); // already done at load
        List<BEvent> listEvents = new ArrayList<>();
        try {
            listEvents.addAll(bonitaProperties.load());
            
            String options = JSONValue.toJSONString(parameters);
            bonitaProperties.setProperty("options", options);
            listEvents.addAll(bonitaProperties.store());
            listEvents.add(eventSaveParametersOk);
        } catch (Exception e) {
            logger.severe("Exception " + e.toString());
            listEvents.add(new BEvent(eventSaveParametersError, e, "Error :" + e.getMessage()));
        }
        return listEvents;
    }
}

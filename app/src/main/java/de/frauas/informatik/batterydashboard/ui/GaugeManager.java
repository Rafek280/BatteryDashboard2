package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import de.frauas.informatik.batterydashboard.enums.GaugeMetric;
import de.frauas.informatik.batterydashboard.enums.GaugeType;

/**
 * This is a Singleton. The only instance belongs to the running UiService.
 *
 * Responsibilities:
 * - manages dashboard configurations (= set of gaugeBlueprints for the gauges to be displayed, each with their positions, metric and type)</br>
 * - manages active Gauges (visible in UI)</br>
 * - instantiate a config (making gauges from blueprints) to be then displayed by the uiService</br>
 * - save a changed config</br>
 * - add a gauge (chosen from configuration window) to the current config</br>
 * - update the current config when a new gauge is added</br>
 * - save the current positions of all displayed gauges to the current config</br>
 * - request a UI update from the UiService when a new gauge was added. This is done by sending a broadcast,
 *   that the UiService has subscribed to. (learn how to do that
 *   <a href="https://www.websmithing.com/2011/02/01/how-to-update-the-ui-in-an-android-activity-using-data-from-a-background-service/">here (Tutorial)</a>)
 *.
 * @see UiService
 * @see GaugeBlueprint
 * @see Gauge
 * @see Dashboard
 *
 * @author filzinge@stud.fra-uas.de
 */

public class GaugeManager {
    private static final String TAG = "DashboardManager instance";
    private Hashtable<String, DashboardConfiguration> dashboardConfigs;
    private DashboardConfiguration currentConfig;
    private ArrayList<Gauge> activeGauges;
    private ArrayList<Gauge> clone;
    static final String BROADCAST_ACTION = "requestConfigUpdate";
    private static Intent intent = new Intent(BROADCAST_ACTION);
    private boolean IsDeleteMode;

    // singleton implementation
    private static GaugeManager instance;
    static GaugeManager getInstance(){
        if(instance == null){
            instance = new GaugeManager();
        }
        return instance;
    }

    public void testPrint(){
        System.out.println("HaLLLWOWOOWOWOWOWOWOWOWOWOW");
    }
    // constructor
    private GaugeManager(){
        dashboardConfigs = new Hashtable<>();
        activeGauges = new ArrayList<>();
        // TODO get dashboardConfigs from somewhere. xml?
        saveDashConfig();
        // making a default config here...
        ArrayList<GaugeBlueprint> gauges = new ArrayList<>();

        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.GRAPHICAL, 20, 20));
        gauges.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.GRAPHICAL, 180, 40));
        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.GRAPHICAL, 20, 160));
        //gauges.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.BIG_NUMBER, 180, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.TEXT_ONLY, 20, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_TEMPS, GaugeType.TEXT_ONLY, 170, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.TEXT_ONLY, 20, 440));
        gauges.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.BIG_NUMBER, 170, 140));

        DashboardConfiguration testConfig = new DashboardConfiguration(gauges, "default", true);

        currentConfig = saveConfig(testConfig);
        //getActiveGauges();
        //getCurrentBlueprints();
        getConfigDescriptions();
        getActiveGaugeDescriptions();
        System.out.println("NUEEEEEEEEEEEEEEEEEEEEDKIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIING");
        System.out.println(activeGauges);
    }


        /**oooooooooooooooooooooooooooooooooooooo
     *
     * load custom made  confings for testing
     *
     * oooooooooooooooooooooooooooooooooooooo
     */
    public void testLoadConfig() {


       // activeGauges.clear();

        updateCurrentConfig(getConfig("rafek"));

    }

    public void testDelete(Context context){


        activeGauges.clear();
        // save current positions (could have been changed)
        savePositions();
        // request UI update at service
        context.sendBroadcast(intent);
    }

    public void updateUI(Context context){
        context.sendBroadcast(intent);
    }

    ArrayList<Gauge> instantiateConfigBlueprints(Context context){
        Gauge g;
        for (GaugeBlueprint b : currentConfig.gauges) {
            g = new Gauge(context, b);
            activeGauges.add(g);
        }
        return activeGauges;
    }

    ArrayList<Gauge> getActiveGauges(){
        return activeGauges;
    }


    private void savePositions(){
        ArrayList<GaugeBlueprint> blueprints = new ArrayList<>();
        getActiveGauges();
        for(Gauge g : activeGauges){
            blueprints.add(g.getBlueprint());
        }
        updateCurrentConfig(new DashboardConfiguration(blueprints));
    }

    public ArrayList<GaugeBlueprint> getCurrentBlueprints(){

        ArrayList<GaugeBlueprint> blueprints = new ArrayList<>();
        getActiveGauges();
        for(Gauge g : activeGauges){
            GaugeBlueprint b = g.getBlueprint();
            b.setX(g.getX());
            b.setY(g.getY());
            blueprints.add(b);
        }



        System.out.println(blueprints);
        System.out.println(activeGauges);
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        return blueprints;

    }

    public DashboardConfiguration getConfig(String name){


        DashboardConfiguration tempConfig = dashboardConfigs.get(name);

        return tempConfig;

    }

    public void saveDashConfig(){


        ArrayList<GaugeBlueprint> gauges = new ArrayList<>();

        gauges= getCurrentBlueprints();


        DashboardConfiguration testConfig1 = new DashboardConfiguration(gauges, "rafek", true);
       // dashboardConfigs.put( "rafek", testConfig1);
        saveConfig(testConfig1);



    }
    private DashboardConfiguration saveConfig(DashboardConfiguration config){
        // this method ensures that the hashtable dashboardConfigs only contains unique string keys (name)
        // In case the name already exists, an incrementing integer is added to the given name.
        // the saved config with the new name is returned and should be used as currentConfig then.
        String name = config.name;
        int i = 1;
        while(dashboardConfigs.contains(config.name)){
            name = config.name+i;
            config.setName(name);
            i++;
        }
        dashboardConfigs.put(name, config);
        return dashboardConfigs.get(name);
    }

    private void updateCurrentConfig(DashboardConfiguration newConfig){
        String currentName = currentConfig.name;
        newConfig.setName(currentName);
        dashboardConfigs.replace(currentName, newConfig);
        currentConfig = dashboardConfigs.get(currentName);

    }

    void addGaugeToCurrent(Context context, GaugeBlueprint blueprint){
        Gauge g = new Gauge(context, blueprint);
        activeGauges.add(g);
        // save current positions (could have been changed)
        savePositions();
        // request UI update at service
        context.sendBroadcast(intent);
    }

    void deleteGauge(Context context, Gauge del){
        Log.d("GaugeManager", "Lösche "+del.toString());

        ArrayList<Gauge> newActiveGauges = new ArrayList<>();
        for (Gauge g : activeGauges) {
            if(g.toString().equals(del.toString())){
                del.deactivateDeleteMode();
            }
        }
        activeGauges.remove(del);
        // save current positions (could have been changed)
        savePositions();
        // request UI update at service
        context.sendBroadcast(intent);
    }

    boolean toggleDeleteMode(){
        if(IsDeleteMode){
            deactivateDeleteMode();
        }else {
            for (Gauge g : activeGauges) {
                g.activateDeleteMode();
            }
            IsDeleteMode = true;
        }
        return IsDeleteMode;
    }

    void deactivateDeleteMode(){
        for (Gauge g : activeGauges) {
            g.deactivateDeleteMode();
        }
        IsDeleteMode = false;
    }

    private void printConfigNames(){
        // for debugging, can be reused to return configs' names for config window UI :)
        StringBuilder s = new StringBuilder(System.lineSeparator());
        Set<String> keys = dashboardConfigs.keySet();
        for(String name : keys){
            s.append(name).append(System.lineSeparator());
        }
        Log.d("Saved Configs: ", s.toString());
    }

    private ArrayList<String> getConfigDescriptions(){
        ArrayList<String> desc = new ArrayList<>();
        Collection<DashboardConfiguration> configs = dashboardConfigs.values();
        for(DashboardConfiguration c : configs){
           // desc.add(c.name + " (" + c.gaugeT + ")");
        }
        return desc;
    }

    ArrayList<String> getActiveGaugeDescriptions(){
        ArrayList<String> list = new ArrayList<>();
        for(Gauge g : activeGauges){
            list.add(g.gaugeMetric.label + " (" + g.gaugeType.description + ")");
        }
        return list;
    }

}

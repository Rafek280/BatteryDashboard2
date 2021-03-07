package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
        static final String BROADCAST_ACTION = "requestConfigUpdate";
    private static Intent intent = new Intent(BROADCAST_ACTION);
    private boolean IsDeleteMode;
    private ArrayList<String> customUserProfile;
    private ArrayList<GaugeBlueprint> currentBlueprints = new ArrayList<>();
    private ArrayList<GaugeBlueprint> gauges = new ArrayList<>();

    // singleton implementation
    private static GaugeManager instance;
    static GaugeManager getInstance(){
        if(instance == null){
            instance = new GaugeManager();
        }
        return instance;
    }
    static GaugeManager getInstance2(){
        if(instance == null){
            instance = new GaugeManager();
        }
        return instance;
    }


    // constructor
    private GaugeManager(){
        dashboardConfigs = new Hashtable<>();
        activeGauges = new ArrayList<>();

        customUserProfile = new ArrayList<String>();
        customUserProfile.add("Kapazität grafisch 180 40");
        customUserProfile.add("Leistung grafisch 20 20");
        customUserProfile.add("Spannung grafisch 20 160");
        customUserProfile.add("DrivingAmperage großeZahl 180 300");
        customUserProfile.add("Zellspannungen Text 20 300");
        customUserProfile.add("Zelltemperaturen Text 180 440");
        customUserProfile.add("Spannung Text 20 440");
        customUserProfile.add("Charger-Temp großeZahl 180 160");
        // TODO get dashboardConfigs from somewhere. xml?

        saveDashConfig();
        // making a default config here...


        //LOAD CUSTOM PROFILES
        loadCustomUserProfilesFromAPI();
        /*gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.GRAPHICAL, 20, 20));
        gauges.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.GRAPHICAL, 180, 40));
        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.GRAPHICAL, 20, 160));
        //gauges.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.BIG_NUMBER, 180, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.TEXT_ONLY, 20, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_TEMPS, GaugeType.TEXT_ONLY, 170, 300));
        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.TEXT_ONLY, 20, 440));
        gauges.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.BIG_NUMBER, 170, 140));
    */
        DashboardConfiguration testConfig = new DashboardConfiguration(gauges, "default", true);

        currentConfig = saveConfig(testConfig);
        //convertGaugesToStrings();
       // getActiveGauges();
        //getCurrentBlueprints();
        //getConfigDescriptions();
       // getActiveGaugeDescriptions();
    }


        /**oooooooooooooooooooooooooooooooooooooo
     *
     * load custom made  confings for testing
     *
     *
     */
    public void loadCustomConfig() {


       // activeGauges.clear();

        updateCurrentConfig(getConfig("CustomProfil"));

    }

    public void testLoadAPI(){
        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.GRAPHICAL, 180, 40));
    }

    /**
     * load custom user profiles, this takes the customUserProfile ArrrayList of Strings,
     * splits the strings on empty spaces and therefore gets the parameters which we need
     * the Gauge Type, the Gauge Visual Repesentation Type, the X Pos, the Y pos
     *
     */
    public void loadCustomUserProfilesFromAPI(){
        //convertGaugesToStrings();


        //hier String von API reinspeichern in customUserProfile

        for(String s: customUserProfile){

            String GaugeArt = "";
            String GaugeVisual = "";
            int XPos = 180;
            int YPos = 40;

            String[] GaugeObjekt = s.split(" ");
            System.out.println("GaugeObjekt:");
            System.out.println(Arrays.toString(GaugeObjekt));

            //extract all 4 paramts into variables
            GaugeArt = GaugeObjekt[0];
            GaugeVisual = GaugeObjekt[1];
            XPos = Integer.parseInt(GaugeObjekt[2]);
            YPos = Integer.parseInt(GaugeObjekt[3]);

            //now add gauge based on params
            //if you can simplify this code, then youre free to go >D
            switch (GaugeArt){

                case "Kapazität":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Spannung":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Durchschnitt-Geschwindigkeit":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Geschwindigkeit":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Leistung":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "DrivingAmperage":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Tageskilometer":

                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.TAGES_KILOMETER_ZAEHLER, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.TAGES_KILOMETER_ZAEHLER, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Charger-Temp":

                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Aktueller-Verbrauch":

                        gauges.add(new GaugeBlueprint(GaugeMetric.CONSUMPTION, GaugeType.BIG_NUMBER, XPos, YPos));

                        break;
                case "Verbrauch":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSVERBRAUCH, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSVERBRAUCH, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }


                case "Reichweite":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.RANGE, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.RANGE, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.RANGE, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }

                case "Zellspannungen":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("großeZahl")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.BIG_NUMBER, XPos, YPos));
                        break;
                    }
                    if(GaugeVisual.equals("Text")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
                    }


                case "Gefahrene-Kilometer":
                    if(GaugeVisual.equals("grafisch")){
                        gauges.add(new GaugeBlueprint(GaugeMetric.ODOMETER, GaugeType.GRAPHICAL, XPos, YPos));
                        break;
                    }



                case "Zelltemperaturen":

                        gauges.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.TEXT_ONLY, XPos, YPos));
                        break;
            }


            }



        }



    /**
     * converts the GaugeBlueprints ArrayList of Objects into a String ArrayList,
     * in order to send(put) this as a User Parameter in the API
     * User Profiles are saved in API in Object User in the Parameter UserProfil
     * Clearing arraylist on each call is necessary in order to have
     * the most recent gauge state always represented and saved
     */

    public void convertGaugesToStrings() {
        customUserProfile.clear();
        currentBlueprints = getCurrentBlueprints();

        for(GaugeBlueprint b : currentBlueprints){
            customUserProfile.add(b.toString());
        }
        System.out.println("DIE USERPROFILE SIND:/n");
        System.out.println(customUserProfile);
    }



    public void loadStatistiken() {

        ArrayList<GaugeBlueprint> gauges2 = new ArrayList<>();

        gauges2.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.GRAPHICAL, 20, 60));
        gauges2.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSVERBRAUCH, GaugeType.BIG_NUMBER, 180, 60));

        DashboardConfiguration testConfig = new DashboardConfiguration(gauges2, "default2", true);

        currentConfig = saveConfig(testConfig);
        updateCurrentConfig(getConfig("default2"));

    }

    /**
     * deletes the current active gauges
     * when loading new custom profile
     * @param context
     */
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



        //System.out.println(blueprints);
       // System.out.println(activeGauges);


        return blueprints;

    }

    public DashboardConfiguration getConfig(String name){


        DashboardConfiguration tempConfig = dashboardConfigs.get(name);

        return tempConfig;

    }

    public void saveDashConfig(){


        ArrayList<GaugeBlueprint> gauges ;

        gauges= getCurrentBlueprints();


        DashboardConfiguration testConfig1 = new DashboardConfiguration(gauges, "CustomProfil", true);

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

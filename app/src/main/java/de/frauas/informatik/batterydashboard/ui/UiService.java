package de.frauas.informatik.batterydashboard.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;

import com.example.batterydashboard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.frauas.informatik.batterydashboard.models.Battery;
import de.frauas.informatik.batterydashboard.background.BatteryDataService;
import de.frauas.informatik.batterydashboard.enums.GaugeType;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/**
 * This is the service that is started by the MainActivity. MainActivity then finishes itself and
 * this service basically *is* the application.</br>
 * The reason for the app being designed as a service and not an activity is, that the dashboard must always be on top of
 * any other GUI and an activity cannot provide this functionality because it subject to Lifecycle events like e.g. Pause...
 * @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle">Android Activity documentation</a> </br></br>
 *
 * This service is responsible for drawing UI elements on the screen. It covers one third of the screen (346px) with a dashboard.
 * This service also starts and binds the DataService, that will provide data through a shared battery object.</br>
 * The functionality is distributed to two services for SoC reasons (separation of concern).
 * @see Battery
 * @see BatteryDataService
 *
 * </br>
 * This class implements PopupMenu.OnMenuItemClickListener interface, so that a popup menu can be shown when the exit app button is tapped.</br>
 *
 * @author filzinge@stud.fra-uas.de
 */


public class UiService extends Service implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "UiService";
    private Intent batteryDataServiceIntent;
    public static final int FLAG_NONBLOCKING_OVERLAY = FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE;
    private Dashboard dashboard;
    private ConfigWindow configWindow;
    private boolean configWindowVisible = false;
    private GaugeManager gaugeManager;
    private BatteryDataService dataService;
    private boolean isDataServiceBound = false;
    de.frauas.informatik.batterydashboard.models.Battery battery;
    private final Handler handler = new Handler();
    private WindowManager windowManager;
    private boolean IsInDeleteMode;
    int uiUpdateFrequency = 1000; // 2000 = every 2 seconds, 1000 = every second
    private PopupMenu popupExitMenu;


    private PopupMenu popupProfileMenu;


    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BatteryDataService.LocalBinder binder = (BatteryDataService.LocalBinder) service;
            dataService = binder.getService();
            isDataServiceBound = true;
            dataService.setBattery(battery);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isDataServiceBound = false;
            Log.i(TAG, "BatteryDataService disconnected.");
            // restart the BatteryDataService here?
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        // create battery object to model battery data
        // the battery object will be passed to the dataService and be shared by both services
        battery = new Battery(getResources());
        batteryDataServiceIntent = new Intent(this, BatteryDataService.class);

        // bind to data service
        bindService(batteryDataServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // register a receiver to receive update requests from DashboardManager if config changed
        BroadcastReceiver dashboardConfigUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "update request received.");
                updateDashboardConfig();
            }
        };
        registerReceiver(dashboardConfigUpdateReceiver, new IntentFilter(GaugeManager.BROADCAST_ACTION));

        // create the UI
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                346,
                MATCH_PARENT, //  WRAP_CONTENT height
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                FLAG_NONBLOCKING_OVERLAY,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.END | Gravity.TOP;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //assert windowManager != null;
        dashboard = new Dashboard(this);
        windowManager.addView(dashboard, params);

        // get DashboardManager (Singleton) and load Gauges from Blueprints in a dashboard config
        gaugeManager = GaugeManager.getInstance();
        loadDashboardConfig(gaugeManager.instantiateConfigBlueprints(this));

        // bind buttons etc
        this.setExitButton(dashboard.getExitButton());
        this.setConfigToggler(dashboard.getConfigButton());
        this.setProfileButton(dashboard.getProfileButton());
        this.setDeleteModeButton(dashboard.getContent().findViewById(R.id.delete_btn)); // TESTING!
    }

    /**
     * loads the gauges of the given list to the dashboard, meaning it adds the views to the dashboard frame.
     * @param gauges list of Gauge objects
     */
    private void loadDashboardConfig(ArrayList<Gauge> gauges){
        for(Gauge g : gauges){
            dashboard.getDashboardFrame().addView(g);
        }
    }

    /**
     * updates the currently shown configuration by first removing all views and then adding the active gauges from the gaugeManager.</br>
     * This method is called whenever the gaugeManager requests a UI update because it made changes to its active gauges.
     */
    private void updateDashboardConfig(){
        dashboard.getDashboardFrame().removeAllViews();
        loadDashboardConfig(gaugeManager.getActiveGauges());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(dashboardValuesUpdate);
        handler.postDelayed(dashboardValuesUpdate, 1000); // 1 second delay

        return START_NOT_STICKY;
        //return Service.START_REDELIVER_INTENT; //>> service will always be restarted
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection); // A
        stopService(batteryDataServiceIntent);
        isDataServiceBound = false;
    }

    /**
     * A runnable to update the UI as often as uiUpdateFrequency specifies.
     * @see this.uiUpdateFrequency
     */
    private Runnable dashboardValuesUpdate = new Runnable() {
        public void run() {
            for(Gauge g : gaugeManager.getActiveGauges()){
                updateGauge(g);
            }
            handler.postDelayed(this, uiUpdateFrequency);
        }
    };


    /**
     * Method to update a gauge by passing it as param. </br>
     * Gets the value(s) for the gauge's metric from battery and calls update on gauge.
     * @param gauge to be updated
     */
    private void updateGauge(Gauge gauge) {// Das ist nur für die Werte
        switch (gauge.gaugeMetric) {
            case VOLTAGE:
                gauge.update(battery.voltageSum());
                break;
            case POWER:
                gauge.update(battery.power());
                break;
            case DRIVING_AMP:
                gauge.update(battery.drivingAmperage());
                break;
            case CHARGER_TEMP:
                if(gauge.gaugeType == GaugeType.GRAPHICAL){
                    gauge.update(battery.getChargerTemperatures());
                } else {
                    gauge.update(battery.getAvrgChargerTemp());
                }
                break;
            case CONSUMPTION:
                /* TODO implement in battery: Durchschnittsverbrauch
                 (aus der Leistung berechnet, seit Fahrzeugstart (Zündung an),
                 Werte jede Sekunde aufaddieren, dann durch Zeit teilen) */
                break;
            case RANGE:
                /* TODO implement in battery: Reichweite
                 (entweder auf Basis Durchschnittsverbrauch oder anderer Algorithmus
                 (z.B. Basis ist Durchschnitt, aber adaptiert auf die letzten 5 min ...)
                 Das machen die Autos auch unterschiedlich. In diesem Semester aber
                 am besten am Durchschnittsverbrauch orientiert.
                 Hier wird auch der Ladezustand benötigt (Capacity) */
                break;
            case CELL_VOLTAGES:
                gauge.update(battery.getCellVoltages());
                break;
            case ODOMETER:
                // TODO implement in battery: (Tages-)kilometerzähler (seit Zündung an)
                break;
            case CAPACITY:
                gauge.update(battery.capacity());
                break;
            case CELL_TEMPS:
                gauge.update(battery.getCellTemps());
            default:
                break;
        }
    }

    /**
     * profile button for saving and loading configs
     * in work
     */
    private void setProfileButton(Button btn) {
        btn.setOnClickListener((l) -> {
            popupProfileMenu = new PopupMenu(this, btn);
            popupProfileMenu.setOnMenuItemClickListener(this);
            MenuInflater inflater = popupProfileMenu.getMenuInflater();
            inflater.inflate(R.menu.profile_menu, popupProfileMenu.getMenu());
            popupProfileMenu.show();
        });
    }



    /**
     * Makes the specified Button the control that closes the app.
     * In the anonymous OnClickListener a popup menu is inflated from an xml file in resources/menu.
     * @param btn
     */
    private void setExitButton(Button btn) {
        btn.setOnClickListener((l) -> {
            popupExitMenu = new PopupMenu(this, btn);
            popupExitMenu.setOnMenuItemClickListener(this);
            MenuInflater inflater = popupExitMenu.getMenuInflater();
            inflater.inflate(R.menu.close_app_menu, popupExitMenu.getMenu());
            popupExitMenu.show();
        });
    }

    /**
     * stops the services and exits the app
     * TODO this method needs some testing to see if services really both stop :)
     */
    private void exitApp(){
        dataService.onDestroy();
        dataService.stopSelf();
        this.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        this.stopSelf();
        System.exit(0);
    }

    /**
     * Makes the specified ImageButton the control that toggles the dashboard's deleteMode.
     * @param btn An ImageButton without background (will be set to a glow in "on" state)
     */
    private void setDeleteModeButton(ImageButton btn) {
        btn.setOnClickListener((l) -> {
            IsInDeleteMode = gaugeManager.toggleDeleteMode();
            if(IsInDeleteMode){
                btn.setBackgroundResource(R.drawable.gradient_bg_40percent);
            }
            else{
                btn.setBackgroundResource(0);
            }
        });
    }

    /**
     * This method sets the control that toggles the configuration/add window. </br>
     * An additional control for closing it can be set in this method. (see line comments) </br>
     * The method also exits the deleteMode if it was on.
     * @param btn
     */

    private void setConfigToggler(ImageButton btn) {
        btn.setOnClickListener((l) -> {
            if(!configWindowVisible) {
                configWindowVisible = true;

                // create the config window
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        600,
                        MATCH_PARENT, //  WRAP_CONTENT height
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        FLAG_NONBLOCKING_OVERLAY,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.END | Gravity.TOP;

                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                configWindow = new ConfigWindow(this);
                windowManager.addView(configWindow, params);

                // set up the expandable list
                ViewGroup content = configWindow.getContent();
                List<String> expandableListTitle;
                ExpandableListView expandableListView = content.findViewById(R.id.expandableListView);
                HashMap<String, List<GaugeBlueprint>> expandableListDetail = ExpListGaugeDataProvider.getAvailableGaugesData();
                expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                ExpandableListAdapter expandableListAdapter = new ExpListForGaugeBlueprintsAdapter(this, expandableListTitle, expandableListDetail);
                expandableListView.setAdapter(expandableListAdapter);

                // end DeleteMode if on
                if(IsInDeleteMode){
                    dashboard.getContent().findViewById(R.id.delete_btn).callOnClick();
                }

                // set up closing functionality
                setConfigToggler(configWindow.getCloseBtn());
                // the following causes the window to also close when clicking on the darkened dashboard
                setConfigToggler(configWindow.getContent().findViewById(R.id.config_window_close_scrim));
            }
            else{
                configWindowVisible = false;
                windowManager.removeView(configWindow);
            }
        });
    }

    // The overridden method from the implemented PopupMenu.OnMenuItemClickListener interface to assign functionality to
    // the close menu's items


    public void loadSavedConfig(){
        //gaugeManager.testLoadConfig();
        updateDashboardConfig();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit_app:
                exitApp();
                return true;
            case R.id.menu_cancel:
                popupExitMenu.dismiss();
                return true;
            case R.id.menu_load:
                gaugeManager.testLoadConfig();
                gaugeManager.instantiateConfigBlueprints(this);
                gaugeManager.updateUI(this);

                return true;
            case R.id.menu_save:
                gaugeManager.saveDashConfig();
                return true;
            case R.id.menu_clear:
                gaugeManager.testDelete(this);

                return true;

            default:
                return false;
        }
    }

}

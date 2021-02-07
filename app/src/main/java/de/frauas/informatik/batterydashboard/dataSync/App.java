package de.frauas.informatik.batterydashboard.dataSync;


import android.app.Application;
import android.content.Context;

//import android.support.multidex.MultiDex;
//import com.facebook.stetho.Stetho;
//import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import de.frauas.informatik.batterydashboard.dataSync.Models.Credentials;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
       // MultiDex.install(this);
    }

    public void initRealm(){

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        //byte[] key = new byte[64];
        //new SecureRandom().nextBytes(key);

        RealmConfiguration config = new RealmConfiguration.Builder()
                //.encryptionKey(key)
                .build();
        Realm.setDefaultConfiguration(config);
        Credentials credentials = new Credentials();
        credentials.setUsername("CarDevice");
        credentials.setPassword("1234567890");
        RestClient restClient = new RestClient();
        restClient.setMyToken(App.this, credentials);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfiguration);

       /*Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build()
        );*/
    }
}

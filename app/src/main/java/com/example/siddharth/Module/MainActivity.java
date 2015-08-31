package com.example.siddharth.Module;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    public String dest = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t=(TextView)findViewById(R.id.textView5);
        t.setMovementMethod(new ScrollingMovementMethod());
        display();
    }

    private void display() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                TextView t = (TextView) findViewById(R.id.textView4);
                String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                t.setText(" " + ip + " ");
                t = (TextView) findViewById(R.id.textView2);
                t.setText(wifiManager.getConnectionInfo().getSSID());
                break;
            }
        }
    }

    private void write(String data) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyCache.txt");

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private class ASyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            //dialog = ProgressDialog.show(MainFeedActivity.this, null, "Posting...");
        }

        protected void onPostExecute(Void result) {
            Log.v("jbj", "oop");
            refresh();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.v("v", "Scanning for devices");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView t = (TextView) findViewById(R.id.textView5);
                    t.append("\n\n" + "Scanning for devices");
                }
            });
            Runtime runtime = Runtime.getRuntime();
            Process proc = null;
            while (true) {
                int x = (int) (Math.random() * 10000);
                x = x % 200;
                x = x + 30;
                dest = "192.168.1." + x;
                Log.v("v", dest);

                try {
                    proc = runtime.exec("ping -c 1 " + dest);
                    proc.waitFor();
                } catch (Exception e) {
                    write("\n" + e.getStackTrace());
                }
                int exit = proc.exitValue();
                if (exit == 1) {
                    Log.v("v", "found " + dest);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView t = (TextView) findViewById(R.id.textView5);
                            t.append("\n" + "Found empty node");
                        }
                    });


                    Process proc2 = null;
                    try {
                        proc2 = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                        proc2.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int exit2 = proc2.exitValue();
                    if (exit2 == 0) {
                        Log.v("v", "connected");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView t = (TextView) findViewById(R.id.textView5);
                                t.append("\n" + "Connected");
                            }
                        });

                        break;
                    } else {
                        Log.v("v", "no connection");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView t = (TextView) findViewById(R.id.textView5);
                                t.append("\n" + "No connection .. retrying");
                            }
                        });
                    }
                }
            }
            return null;
        }
    }
    public void onClick(View v) {
        new ASyncTask().execute();
    }

    public void refresh() {
        try {
            WifiConfiguration wifiConf = null;
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConf = conf;
                    setIpAssignment("STATIC", wifiConf);
                    setIpAddress(InetAddress.getByName(dest), 24, wifiConf);
                    setGateway(InetAddress.getByName("192.168.1.1"), wifiConf);
                    setDNS(InetAddress.getByName("8.8.8.8"), wifiConf);
                    wifiManager.updateNetwork(wifiConf);
                    wifiManager.saveConfiguration();
                    wifiManager.disconnect();
                    Log.v("fdf", wifiConf.SSID);
                    TextView t = (TextView) findViewById(R.id.textView4);
                    t.setText(" " + dest + " ");
                    t = (TextView) findViewById(R.id.textView2);
                    t.setText(wifiManager.getConnectionInfo().getSSID());
                    break;
                }
            }

        } catch (Exception e) {
            write("\n" + e.getStackTrace());

            System.exit(0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(InetAddress.class, int.class);
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass.getConstructor(InetAddress.class);
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }
}

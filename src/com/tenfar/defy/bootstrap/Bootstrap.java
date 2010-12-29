package com.tenfar.defy.bootstrap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.tenfar.defy.bootstrap.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Bootstrap extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        unzipAssets();

        Button flash = (Button)findViewById(R.id.flash);
        flash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filesDir = getFilesDir().getAbsolutePath();
                String busybox = filesDir + "/busybox";
                String hijack = filesDir + "/hijack";
                String updatebinary = filesDir + "/update-binary";
                String recoveryzip = filesDir + "/update-recovery.zip";
                String adbd = filesDir + "/adbd";
                String logwrapperbin = filesDir + "/logwrapper.bin";
                String secinit = filesDir + "/2nd-init";
                String bash = filesDir + "/bash";
                String fuck = filesDir + "/fuck";
                String init_prep_keypad = filesDir + "/init_prep_keypad.sh";
                String defaultprop = filesDir + "/default.prop";
                String initrc = filesDir + "/init.rc";
                String umtsrc = filesDir + "/init.mapphone_umts.rc";
                
                StringBuilder command = new StringBuilder();
                command.append(busybox + " mount -oremount,rw /system ; ");
                command.append(busybox + " cp " + hijack + " /system/bin/hijack ; ");
                command.append(busybox + " cp " + logwrapperbin + " /system/bin/logwrapper.bin ; ");
                command.append("cd /system/bin ; rm logwrapper ; ln -s hijack logwrapper ; ");
                command.append(busybox + " cp " + bash + " /system/bin/bash ; ");
                command.append(busybox + " cp " + fuck + " /system/bin/fuck ; ");
                command.append(busybox + " chmod 777  /system/bin/bash  ; ");
                command.append(busybox + " cp " + secinit + " /system/bin/2nd-init ; ");
                command.append(busybox + " cp " + init_prep_keypad + " /system/bin/init_prep_keypad.sh ; ");
                command.append("cd /system/bin ; rm sh ; ln -s bash sh ; ");
                command.append("cd /system/etc ; mkdir rootfs ; ");
                command.append(busybox + " cp " + defaultprop + " /system/etc/rootfs/default.prop ; ");
                command.append(busybox + " cp " + initrc + " /system/etc/rootfs/init.rc ; ");
                command.append(busybox + " cp " + umtsrc + " /system/etc/rootfs/init.mapphone_umts.rc ; ");
                command.append(busybox + " cp " + busybox + " /system/xbin/busybox ; ");  
                command.append("cd /system/xbin ; /system/xbin/busybox --install /system/xbin ; ");
                command.append(" sync; ");
                command.append(busybox + " mount -oremount,ro /system ; ");
                command.append(busybox + " cp " + updatebinary + " /pds/update-binary ; ");
                command.append(busybox + " cp " + recoveryzip + " /pds/update-recovery.zip ; ");
                command.append(busybox + " cp " + hijack + " /pds/hijack ; ");
                
              //  restart adbd as root
                command.append(busybox + " mount -orw,remount / ; ");
                command.append("mv /sbin/adbd /sbin/adbd.old ; ");
                command.append(busybox + " cp " +  adbd + " /sbin/adbd ; ");
                command.append(busybox + " mount -oro,remount / ; ");
                command.append(busybox + " kill $(ps | " + busybox + " grep adbd) ;");

                // prevent recovery from booting here
                command.append("rm /data/.recovery_mode ; ");
                command.append("sync; ");
                
                AlertDialog.Builder builder = new Builder(Bootstrap.this);
                builder.setPositiveButton(android.R.string.ok, null);
                try {
                    Helper.runSuCommand(Bootstrap.this, command.toString());
                    builder.setMessage("Install Success!");
                }
                catch (Exception e) {
                    builder.setTitle("Install Failure");
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.create().show();
            }
        });
        
        Button reboot_recovery = (Button)findViewById(R.id.reboot_recovery);
        reboot_recovery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder command = new StringBuilder();
                command.append("echo 1 > /data/.recovery_mode ; ");
                command.append("sync; ");
                command.append("reboot; ");
                try {
                    Helper.runSuCommand(Bootstrap.this, command.toString());
                }
                catch (Exception e) {
                    AlertDialog.Builder builder = new Builder(Bootstrap.this);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setTitle("reboot Failure");
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        Button reboot_normal = (Button)findViewById(R.id.reboot_normal);
        reboot_normal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder command = new StringBuilder();
                command.append("rm /data/.recovery_mode ; ");
                command.append("stop ssmgrd;");
                command.append("/system/bin/sleep 10;");
                command.append("sync; ");
                command.append("/system/bin/reboot; ");
                try {
                    Helper.runSuCommand(Bootstrap.this, command.toString());
                }
                catch (Exception e) {
                    AlertDialog.Builder builder = new Builder(Bootstrap.this);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setTitle("Reboot Failure");
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        Button reboot_poweroff = (Button)findViewById(R.id.reboot_poweroff);
        reboot_poweroff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder command = new StringBuilder();
                command.append("rm /data/.recovery_mode ; ");
                command.append("stop ssmgrd;");
                command.append("/system/bin/sleep 10;");
                command.append("sync; ");
                command.append("/system/bin/reboot -p; ");
                try {
                    Helper.runSuCommand(Bootstrap.this, command.toString());
                }
                catch (Exception e) {
                    AlertDialog.Builder builder = new Builder(Bootstrap.this);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setTitle("Failure");
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
    }

    final static String LOGTAG = "DefyRecovery";
    final static String ZIP_FILTER = "assets";
    
    void unzipAssets() {
        String apkPath = getPackageCodePath();
        String mAppRoot = getFilesDir().toString();
        try {
            File zipFile = new File(apkPath);
            long zipLastModified = zipFile.lastModified();
            ZipFile zip = new ZipFile(apkPath);
            Vector<ZipEntry> files = getAssets(zip);
            int zipFilterLength = ZIP_FILTER.length();
            
            Enumeration<?> entries = files.elements();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String path = entry.getName().substring(zipFilterLength);
                File outputFile = new File(mAppRoot, path);
                outputFile.getParentFile().mkdirs();

                if (outputFile.exists() && entry.getSize() == outputFile.length() && zipLastModified < outputFile.lastModified())
                    continue;
                FileOutputStream fos = new FileOutputStream(outputFile);
                copyStreams(zip.getInputStream(entry), fos);
                Runtime.getRuntime().exec("chmod 755 " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(LOGTAG, "Error: " + e.getMessage());
        }
    }

    static final int BUFSIZE = 5192;

    void copyStreams(InputStream is, FileOutputStream fos) {
        BufferedOutputStream os = null;
        try {
            byte data[] = new byte[BUFSIZE];
            int count;
            os = new BufferedOutputStream(fos, BUFSIZE);
            while ((count = is.read(data, 0, BUFSIZE)) != -1) {
                os.write(data, 0, count);
            }
            os.flush();
        } catch (IOException e) {
            Log.e(LOGTAG, "Exception while copying: " + e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e2) {
                Log.e(LOGTAG, "Exception while closing the stream: " + e2);
            }
        }
    }

    public Vector<ZipEntry> getAssets(ZipFile zip) {
        Vector<ZipEntry> list = new Vector<ZipEntry>();
        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().startsWith(ZIP_FILTER)) {
                list.add(entry);
            }
        }
        return list;
    }
}

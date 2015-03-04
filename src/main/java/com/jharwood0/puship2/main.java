/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jharwood0.puship2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.iharder.jpushbullet2.*;

/**
 *
 * @author josh
 */
public class main {

    public static String getExternalIP() {
        BufferedReader in = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); //you get the IP as a String
            return "===== External IP =====\n" + ip;
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "ERROR! getting IP :(";
    }

    public static String getInternalIP() {
        String out = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                out += ("===== " + ni.toString() + " =====\n");
                out += ("Display Name = " + ni.getDisplayName() + "\n");
                out += ("Is up = " + ni.isUp() + "\n");
                out += ("List of Interface Addresses:\n");
                List<InterfaceAddress> list = ni.getInterfaceAddresses();
                Iterator<InterfaceAddress> it = list.iterator();

                while (it.hasNext()) {
                    InterfaceAddress ia = it.next();
                    out += ("Address = " + ia.getAddress() + "\n");
                }
            }
            return out;
        } catch (SocketException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERROR! Error getting internal IP!:(";

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PushbulletException, InterruptedException {

        String api = "API HERE";
        String nick = "xbian";
        Device target = null;

        System.out.println("=====================");

        System.out.println("Welcome to PushIP!");
        System.out.println("Using APIKEY: " + api);
        System.out.println("Using Name: " + nick);

        System.out.println("=====================");

        Boolean exist = false;

        PushbulletClient client = new PushbulletClient(api);

        List<Device> devices = client.getDevices();
        for (Device device : devices) {

            if (device.getNickname() != null && device.getNickname().equals(nick)) {
                exist = true;
                target = device;
                break;
            }
        }

        if (!exist) {
            System.out.println("Adding device to your account...");
            client.createDevice(nick);

            for (Device device : devices) {
                if (device.getNickname() != null && device.getNickname().equals(nick)) {
                    System.out.print("Success!");
                    target = device;
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("Error getting device...Exiting");
            return;
        }

        
        System.out.println("Starting listening mode...");
        double latest = 0;
        while (true) {
            List<Push> pushes = client.getNewPushes();
            for (Push push : pushes) {
                if (push.getTarget_device_iden() != null && push.getTarget_device_iden().equals(target.getIden()) && push.getCreated() > latest) {
                    if(latest == 0){
                        latest = push.getCreated();
                        break;
                    }
                    System.out.println("Recieved!");
                    System.out.println("getting IP...");
                    client.sendNoteUsingEmail(push.getSender_email(), nick + " - Response", getExternalIP() + "\n" + getInternalIP());

                    latest = push.getCreated();
                }
            }
            sleep(0x800);
        }
    }

}

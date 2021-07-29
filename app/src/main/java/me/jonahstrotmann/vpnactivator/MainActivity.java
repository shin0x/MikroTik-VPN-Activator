package me.jonahstrotmann.vpnactivator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLSocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

public class MainActivity extends AppCompatActivity{

    String password = ""; //
    String user = "api";
    String host = "192.168.88.1";
    ApiConnection connection;
    ImageButton on_off_btn;
    Boolean state = false;
    // used VPN Profiles: US, NL
    String country = "US";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }



        //connect to router

        try {
            connection = ApiConnection.connect(host);
            connection.login(user, password);

        } catch (MikrotikApiException e) {
            show_error();
        }

        on_off_btn = (ImageButton) findViewById(R.id.on_off_btn);
        getStatus();

        //initialize Button
        on_off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStatus();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    show_error();
                }
                getStatus();
            }

        });
        final ImageButton change_btn = (ImageButton) findViewById(R.id.ChangeCountryBtn);
        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Objects.equals(country, "US")){
                   country = "NL";
                   change_btn.setBackground(getDrawable(R.drawable.nl));
                    Toast.makeText(MainActivity.this, "Changed to Netherland VPN", Toast.LENGTH_LONG).show();
                }
                else{
                    country = "US";
                    change_btn.setBackground(getDrawable(R.drawable.us));
                    Toast.makeText(MainActivity.this, "Changed to United States VPN", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void show_error(){
            final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Connection Error. By Pressing OK the App will retry to connect");
            dlgAlert.setTitle("MikroTik VPN Connector");
            dlgAlert.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Thread.sleep(100);
                        try {
                            connection = ApiConnection.connect(host);
                            connection.login(user, password);

                        } catch (MikrotikApiException e) {
                            show_error();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
    }

    private void toggleStatus() {
        //if not connected then connect
        if(!state){
            try {
                connection.execute("/ip/ipsec/peer/set .id=" + country + " disabled=no");
            } catch (MikrotikApiException e) {
                show_error();
            }
        }
        //when connected then disconnect
        else{
            try {
                connection.execute("/ip/ipsec/peer/set .id=NL" + " disabled=yes");
                connection.execute("/ip/ipsec/peer/set .id=US" + " disabled=yes");
            } catch (MikrotikApiException e) {
                show_error();
            }
        }
    }
    private void getStatus(){
        List<Map<String, String>> rs;
        try {
            rs = connection.execute("/ip/ipsec/peer/print");
            String[] status = new String[2];
            int i = 0;
            for (Map<String,String> r : rs) {
                status[i] = r.get("disabled");
                i++;
            }
            if(Objects.equals(status[0], "true") && Objects.equals(status[1], "true")) {
                on_off_btn.setBackground(getDrawable(R.drawable.off));
                state = false;
            }
            else {
                on_off_btn.setBackground(getDrawable(R.drawable.on));
                state = true;
            }
        }
        catch (MikrotikApiException e) {
            show_error();
        }
    }
}
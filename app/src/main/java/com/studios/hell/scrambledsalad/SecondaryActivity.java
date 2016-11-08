package com.studios.hell.scrambledsalad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;


class ScrambledSalad {
    private SharedPreferences prefs;
    private String target_folder;
    private Button button;
    private TextView fname_text;
    private Context context;
    private SecureRandom random;

    ScrambledSalad (String folder, Button button, TextView fname_text, Context ctx) {
        this.button = button;
        this.fname_text = fname_text;
        this.context = ctx;
        this.random = new SecureRandom();

        this.setFolder(folder);
    }

    public void setFolder(String new_folder) {
        this.target_folder = new_folder;
        this.prefs = this.context.getSharedPreferences(
                new String(Base64.encode(new_folder.getBytes(), 0)),
                Context.MODE_PRIVATE
        );

        this.fname_text.setText((new File(new_folder)).getName());
        this.setState();
    }

    private boolean isScrambled() {
        Map<String, ?> scrambled_data = this.prefs.getAll();
        return !scrambled_data.isEmpty();
    }

    private void setState() {
        boolean state = this.isScrambled();
        if(state) {
            this.button.setText("Toss");
        }
        else {
            this.button.setText("Scramble");
        }
    }

    private ArrayList<File> listdir_target_folder() {
        ArrayList<File> listdir = new ArrayList<>();

        File[] all_files_in_dir = new File(this.target_folder).listFiles();
        if (all_files_in_dir == null) {
            return listdir;
        }
        for (File file : all_files_in_dir) {
            if (file.isFile()) {
                listdir.add(file);
            }
        }
        return listdir;
    }

    private void scramble() {
        ArrayList<File> target_foler_listdir = this.listdir_target_folder();
        SharedPreferences.Editor scrambled_data = this.prefs.edit();
        File log_file = new File(this.target_folder, this.context.getString(R.string.log_filename));
        String log_content_agent = "";

        boolean did_rename = true;


        for (File file_to_scramble : target_foler_listdir) {
            String scrambled_filename = "SS_";
            scrambled_filename += new BigInteger(130, random).toString(32);
            byte[] encoded_scrambled_filename = Base64.encode(scrambled_filename.getBytes(), 0);
            byte[] encoded_filename = Base64.encode(file_to_scramble.getName().getBytes(), 0);

            scrambled_data.putString(
                    new String(encoded_filename),
                    new String(encoded_scrambled_filename)
            );

            did_rename = did_rename && file_to_scramble.renameTo(
                   new File(this.target_folder, scrambled_filename)
            );

            log_content_agent += new String(encoded_filename);
            log_content_agent += " ";
            log_content_agent += new String(encoded_scrambled_filename);
        }
        scrambled_data.apply();
        this.setState();

        try {
            FileOutputStream log_content = new FileOutputStream(log_file);
            log_content.write(log_content_agent.getBytes());
            log_content.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    private void toss() {
        Map<String, ?> all_filename_entries = this.prefs.getAll();
        File log_file = new File(this.target_folder, this.context.getString(R.string.log_filename));
        boolean did_rename = true;

        for (String filename_entry : all_filename_entries.keySet()) {
            String original_filename = new String(Base64.decode(filename_entry.getBytes(), 0));
            String encoded_scrambled_filename = this.prefs.getString(filename_entry, "");  // Default should NEVER happen
            String scrambled_filename = new String(Base64.decode(encoded_scrambled_filename.getBytes(), 0));


            File file_to_rename = new File(this.target_folder, scrambled_filename);

            did_rename = did_rename && file_to_rename.renameTo(
                    new File(this.target_folder, original_filename)
            );
        }

        SharedPreferences.Editor scrambled_data = this.prefs.edit();
        scrambled_data.clear();
        scrambled_data.apply();
        this.setState();

        log_file.delete();
    }

    public void scrambleOrToss() {
        if(this.isScrambled()) {
            this.toss();
        }
        else {
            this.scramble();
        }
    }
}


public class SecondaryActivity extends AppCompatActivity {
    static final int ACTIVITY_CHOOSE_FILE = 3;
    private boolean should_lock_app = false;
    private ScrambledSalad ss;
    private SharedPreferences prefs;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.should_lock_app) {
            finish();
        }
        else {
            this.should_lock_app = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        this.prefs = getSharedPreferences(getString(R.string.system_prefs), MODE_PRIVATE);
        this.ss = new ScrambledSalad(
                prefs.getString(getString(R.string.folder_key), getString(R.string.default_folder)),
                (Button) findViewById(R.id.scramble_button),
                (TextView) findViewById(R.id.folderName),
                this
        );
    }

    public void scrambleOrToss(View view) {
        this.ss.scrambleOrToss();
    }

    public void changeFolder(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(this.prefs.getString(
                getString(R.string.folder_key),
               "/storage"
        ));

        intent.setDataAndType(uri, "text/csv");

        this.should_lock_app = false;
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.folder_dialog_title)),
                ACTIVITY_CHOOSE_FILE
        );
//        AlertDialog folder_dialog = new AlertDialog.Builder(this).create();
//        folder_dialog.setTitle(getString(R.string.folder_dialog_title));
//
//        final EditText folder_input = new EditText(this);
//        folder_input.setInputType(InputType.TYPE_CLASS_TEXT);
//        folder_input.setText(getString(R.string.default_folder));
//        folder_dialog.setView(folder_input);
//        folder_dialog.setButton(
//                DialogInterface.BUTTON_POSITIVE,
//                getString(R.string.folder_dialog_button_positive),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        folder_text = folder_input.getText().toString();
//
//                        SharedPreferences.Editor prefs_editor = ss.prefs.edit();
//                    }
//                }
//        );
//        folder_dialog.show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if(requestCode == ACTIVITY_CHOOSE_FILE)
        {
            Uri uri = data.getData();
            String new_folder = (new File(uri.getPath())).getParent();  //Get parent directory

            ss.setFolder(new_folder);

            SharedPreferences.Editor prefs_editor = this.prefs.edit();
            prefs_editor.putString(getString(R.string.folder_key), new_folder);
            prefs_editor.apply();
        }
    }
}

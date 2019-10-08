package com.openclassrooms.savemytrip.tripbook;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.base.BaseActivity;
import com.openclassrooms.savemytrip.utils.StorageUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class TripBookActivity extends BaseActivity {

    //FOR DESIGN
    @BindView(R.id.trip_book_activity_external_choice) LinearLayout linearLayoutExternalChoice;
    @BindView(R.id.trip_book_activity_internal_choice) LinearLayout linearLayoutInternalChoice;
    @BindView(R.id.trip_book_activity_radio_external) RadioButton radioButtonExternalChoice;
    @BindView(R.id.trip_book_activity_radio_public) RadioButton radioButtonExternalPublicChoice;
    @BindView(R.id.trip_book_activity_radio_volatile) RadioButton radioButtonInternalVolatileChoice;
    @BindView(R.id.trip_book_activity_edit_text) EditText editText;

    // FILE PURPOSE
    private static final String FILENAME = "tripBook.txt";
    private static final String FOLDERNAME = "bookTrip";
    // 1 - PERMISSION PURPOSE
    private static final int RC_STORAGE_WRITE_PERMS = 100;
    private static final String AUTHORITY="com.openclassrooms.savemytrip.fileprovider";


    @Override
    public int getLayoutContentViewID() { return R.layout.activity_trip_book; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureToolbar();
        // 6 - Read from storage when starting
        this.readFromStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                // 3 - Call the sharing when user clicked on share button
                this.shareFile();
                return true;
            case R.id.action_save:
                this.save();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnCheckedChanged({R.id.trip_book_activity_radio_internal, R.id.trip_book_activity_radio_external,
                       R.id.trip_book_activity_radio_private, R.id.trip_book_activity_radio_public,
                       R.id.trip_book_activity_radio_normal, R.id.trip_book_activity_radio_volatile})
    public void onClickRadioButton(CompoundButton button, boolean isChecked){
        if (isChecked) {
            switch (button.getId()) {
                case R.id.trip_book_activity_radio_internal:
                    this.linearLayoutExternalChoice.setVisibility(View.GONE);
                    this.linearLayoutInternalChoice.setVisibility(View.VISIBLE);
                    break;
                case R.id.trip_book_activity_radio_external:
                    this.linearLayoutExternalChoice.setVisibility(View.VISIBLE);
                    this.linearLayoutInternalChoice.setVisibility(View.GONE);
                    break;
            }
        }
        // 7 - Read from storage after user clicked on radio buttons
        this.readFromStorage();
    }

    // 4 - Save after user clicked on button
    private void save(){
        if (this.radioButtonExternalChoice.isChecked()){
            this.writeOnExternalStorage(); //Save on external storage
        } else {
            // 3 - Save on internal storage
            this.writeOnInternalStorage();
        }
    }

    @Override
    // 2 - After permission granted or refused
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // ----------------------------------
    // UTILS - STORAGE
    // ----------------------------------

    // 2 - Read from storage
    @AfterPermissionGranted(RC_STORAGE_WRITE_PERMS)
    private void readFromStorage(){
        // 3 - CHECK PERMISSION
        if (!EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this, getString(R.string.title_permission), RC_STORAGE_WRITE_PERMS, WRITE_EXTERNAL_STORAGE);
            return;
        }

        if (this.radioButtonExternalChoice.isChecked()){
            if (StorageUtils.isExternalStorageReadable()){
                // EXTERNAL
                if (radioButtonExternalPublicChoice.isChecked()){
                    // External - Public
                    this.editText.setText(StorageUtils.getTextFromStorage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),this, FILENAME, FOLDERNAME));
                } else {
                    // External - Privatex
                    this.editText.setText(StorageUtils.getTextFromStorage(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), this, FILENAME, FOLDERNAME));
                }
            }
        } else {
            // 2 - Read from internal storage
            if (radioButtonInternalVolatileChoice.isChecked()){
                // Cache
                this.editText.setText(StorageUtils.getTextFromStorage(getCacheDir(), this, FILENAME, FOLDERNAME));
            } else {
                // Normal
                this.editText.setText(StorageUtils.getTextFromStorage(getFilesDir(), this, FILENAME, FOLDERNAME));
            }
        }
    }

    // 3 - Write on external storage
    private void writeOnExternalStorage(){
        if (StorageUtils.isExternalStorageWritable()){
            if (radioButtonExternalPublicChoice.isChecked()){
                StorageUtils.setTextInStorage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), this, FILENAME, FOLDERNAME, this.editText.getText().toString());
            } else {
                StorageUtils.setTextInStorage(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), this, FILENAME, FOLDERNAME, this.editText.getText().toString());
            }
        } else {
            Toast.makeText(this, getString(R.string.external_storage_impossible_create_file), Toast.LENGTH_LONG).show();
        }
    }

    // 1 - Write on internal storage
    private void writeOnInternalStorage(){
        if (radioButtonInternalVolatileChoice.isChecked()){
            StorageUtils.setTextInStorage(getCacheDir(), this, FILENAME, FOLDERNAME, this.editText.getText().toString());
        } else {
            StorageUtils.setTextInStorage(getFilesDir(), this, FILENAME, FOLDERNAME, this.editText.getText().toString());
        }
    }

    // ----------------------------------
    // SHARE FILE
    // ----------------------------------
    // 2 - Share the internal file
    private void shareFile(){
        File internalFile = StorageUtils.getFileFromStorage(getFilesDir(),this, FILENAME, FOLDERNAME);
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), AUTHORITY, internalFile);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.trip_book_share)));
    }
}

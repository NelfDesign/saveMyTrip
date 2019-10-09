package com.openclassrooms.savemytrip.todolist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.base.BaseActivity;
import com.openclassrooms.savemytrip.injections.Injection;
import com.openclassrooms.savemytrip.injections.ViewModelFactory;
import com.openclassrooms.savemytrip.models.Item;
import com.openclassrooms.savemytrip.models.User;
import com.openclassrooms.savemytrip.utils.ItemClickSupport;

import java.io.FileNotFoundException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class TodoListActivity extends BaseActivity implements ItemAdapter.Listener {

    // FOR DESIGN
    @BindView(R.id.todo_list_activity_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.todo_list_activity_spinner) Spinner spinner;
    @BindView(R.id.todo_list_activity_edit_text) EditText editText;
    @BindView(R.id.todo_list_activity_button_gallery) ImageView mImageButton;
    @BindView(R.id.todo_list_activity_header_profile_image) ImageView profileImage;
    @BindView(R.id.todo_list_activity_header_profile_text) TextView profileText;

    // 1 - FOR DATA
    private ItemViewModel itemViewModel;
    private ItemAdapter adapter;
    private static int USER_ID = 1;
    private static final int RC_STORAGE_READ_PERMS = 100;

    Uri selectedPictureUri;

    @Override
    public int getLayoutContentViewID() { return R.layout.activity_todo_list; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureToolbar();
        this.configureSpinner();

        //  Configure RecyclerView & ViewModel
        this.configureRecyclerView();
        this.configureViewModel();

        // Get current user & items from Database
        this.getCurrentUser(USER_ID);
        this.getItems(USER_ID);

        //click on the button image
        mImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);
        });

        //Check if the permission is granted for the gallery
        if (!EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)){
            EasyPermissions.requestPermissions(this,
                    getString(R.string.title_permission),
                    RC_STORAGE_READ_PERMS, READ_EXTERNAL_STORAGE);
            return;
        }

    }

    // -------------------
    // ACTIONS
    // -------------------

    @OnClick(R.id.todo_list_activity_button_add)
    public void onClickAddButton() {
        // 7 - Create item after user clicked on button
        this.createItem(); }

    @Override
    public void onClickDeleteButton(int position) {
        // 7 - Delete item after user clicked on button
        this.deleteItem(this.adapter.getItem(position));
    }

    /**
     * define the clck on the share button to send informations
     * @param position
     */
    @Override
    public void onClickshareButton(int position) {
        Item item = this.adapter.getItem(position);
        Uri uri = Uri.parse(item.getImageUri());

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager)));
    }
    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                Glide.with(this)
                        .load(bitmap)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mImageButton);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            selectedPictureUri = targetUri;
        }
    }

    // -------------------
    // DATA
    // -------------------

    // 2 - Configuring ViewModel
    private void configureViewModel(){
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(this);
        this.itemViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ItemViewModel.class);
        this.itemViewModel.init(USER_ID);
    }

    // ---

    // 3 - Get Current User
    private void getCurrentUser(int userId){
        this.itemViewModel.getUser(userId).observe(this, this::updateHeader);
    }

    // ---

    // 3 - Get all items for a user
    private void getItems(int userId){
        this.itemViewModel.getItems(userId).observe(this, this::updateItemsList);
    }

    // 3 - Create a new item
    private void createItem(){
        Item item;
        if (selectedPictureUri == null) {
            item = new Item(this.editText.getText().toString(), this.spinner.getSelectedItemPosition(), USER_ID, null);
        } else {
            item = new Item(this.editText.getText().toString(), this.spinner.getSelectedItemPosition(), USER_ID, selectedPictureUri.toString());
        }

        this.editText.setText("");
        selectedPictureUri = Uri.parse("");
        mImageButton.setImageResource(R.drawable.ic_gallery_icon_foreground);
        this.itemViewModel.createItem(item);
    }

    // 3 - Delete an item
    private void deleteItem(Item item){
        this.itemViewModel.deleteItem(item.getId());
    }

    // 3 - Update an item (selected or not)
    private void updateItem(Item item){
        item.setSelected(!item.getSelected());
        this.itemViewModel.updateItem(item);
    }

    // -------------------
    // UI
    // -------------------

    private void configureSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // 4 - Configure RecyclerView
    private void configureRecyclerView(){
        this.adapter = new ItemAdapter(this);
        this.recyclerView.setAdapter(this.adapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemClickSupport.addTo(recyclerView, R.layout.activity_todo_list_item)
                .setOnItemClickListener((recyclerView1, position, v) -> this.updateItem(this.adapter.getItem(position)));
    }

    // 5 - Update header (username & picture)
    private void updateHeader(User user){
        this.profileText.setText(user.getUsername());
        Glide.with(this)
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(this.profileImage);
    }

    // 6 - Update the list of items
    private void updateItemsList(List<Item> items){
        this.adapter.updateData(items);
    }
}

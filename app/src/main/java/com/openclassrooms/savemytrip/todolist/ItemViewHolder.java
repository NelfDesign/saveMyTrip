package com.openclassrooms.savemytrip.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.models.Item;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nelfdesign at 07/10/2019
 * com.openclassrooms.savemytrip.todolist
 */
public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.activity_todo_list_item_text)
    TextView textView;
    @BindView(R.id.activity_todo_list_item_image)
    ImageView imageView;
    @BindView(R.id.activity_todo_list_item_remove)
    ImageButton imageButton;
    @BindView(R.id.activity_todo_list_selected_image)
    ImageView selectedImage;
    @BindView(R.id.activity_todo_list_share_button)
    ImageView shareButton;

    // FOR DATA
    private WeakReference<ItemAdapter.Listener> callbackWeakRef;

    public ItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithItem(Item item, ItemAdapter.Listener callback){
        this.callbackWeakRef = new WeakReference<ItemAdapter.Listener>(callback);
        this.textView.setText(item.getText());
        //set callback for the button
        this.imageButton.setOnClickListener(this);
        this.shareButton.setOnClickListener(this);
        //transform string image to bipmap
        Bitmap bitmap = BitmapFactory.decodeFile(item.getImageUri());
        this.selectedImage.setImageBitmap(bitmap);

        switch (item.getCategory()){
            case 0: // TO VISIT
                this.imageView.setBackgroundResource(R.drawable.ic_room_black_24px);
                break;
            case 1: // IDEAS
                this.imageView.setBackgroundResource(R.drawable.ic_lightbulb_outline_black_24px);
                break;
            case 2: // RESTAURANTS
                this.imageView.setBackgroundResource(R.drawable.ic_local_cafe_black_24px);
                break;
        }
        if (item.getSelected()){
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        //check if there is an image to show this and share button
        if (item.getImageUri() == null){
            selectedImage.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);
        } else {
            selectedImage.setImageURI(Uri.parse(item.getImageUri()));
        }

    }

    /**
     * switch on the share button on the item view ( delete or share) and apply the interface method
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_todo_list_item_remove:
                ItemAdapter.Listener callback = callbackWeakRef.get();
                if (callback != null){
                    callback.onClickDeleteButton(getAdapterPosition());
                }
                break;
            case R.id.activity_todo_list_share_button:
                ItemAdapter.Listener callback2 = callbackWeakRef.get();
                if (callback2 != null) {
                    callback2.onClickshareButton(getAdapterPosition());
                }
                break;
        }
    }
}

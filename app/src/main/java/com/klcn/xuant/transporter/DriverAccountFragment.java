package com.klcn.xuant.transporter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.utils.Base64Utils;
import com.klcn.xuant.transporter.utils.ConvertBitmap;
import com.klcn.xuant.transporter.utils.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverAccountFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.txt_name)
    TextView mTxtName;
    @BindView(R.id.txt_name_car)
    TextView mTxtNameCar;
    @BindView(R.id.txt_edit)
    TextView mTxtEdit;
    @BindView(R.id.txt_edit_car)
    TextView mTxtEditCar;
    @BindView(R.id.img_avatar)
    CircleImageView mImgAvatar;
    @BindView(R.id.panel_waybill)
    RelativeLayout mPanelWaybill;
    @BindView(R.id.panel_documents)
    RelativeLayout mPanelDocument;
    @BindView(R.id.panel_about)
    RelativeLayout mPanelAbout;
    @BindView(R.id.panel_sign_out)
    RelativeLayout mPanelSignOut;
    @BindView(R.id.img_avatar_car)
    CircleImageView mImgVehicle;

    FirebaseAuth mFirebaseAuth;
    String encodedString = "";
    DatabaseReference drivers;

    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    private boolean isChangeAvatar = false;

    public static DriverAccountFragment newInstance() {
        DriverAccountFragment fragment = new DriverAccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_account, container, false);
        ButterKnife.bind(this, view);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mPanelSignOut.setOnClickListener(this);
        mPanelAbout.setOnClickListener(this);
        mPanelDocument.setOnClickListener(this);
        mPanelWaybill.setOnClickListener(this);
        mImgAvatar.setOnClickListener(this);
        mImgVehicle.setOnClickListener(this);
        mTxtEdit.setOnClickListener(this);
        mTxtEditCar.setOnClickListener(this);

        drivers = FirebaseDatabase.getInstance().getReference().child(Common.drivers_tbl).child(mFirebaseAuth.getCurrentUser().getUid());

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        getUserInfo();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.panel_about:
                Toast.makeText(getContext(), "Panel about", Toast.LENGTH_LONG).show();
                break;
            case R.id.panel_documents:
                Toast.makeText(getContext(), "Panel document", Toast.LENGTH_LONG).show();
                break;
            case R.id.panel_sign_out:
                Toast.makeText(getContext(), "Panel sign out", Toast.LENGTH_LONG).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    mFirebaseAuth.signOut();
                    Intent intent = new Intent(getContext(), ChooseTypeUserActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                break;
            case R.id.panel_waybill:
                Toast.makeText(getContext(), "Panel waybill", Toast.LENGTH_LONG).show();
                break;
            case R.id.img_avatar:
                isChangeAvatar = true;
                selectImage();
                break;
            case R.id.img_avatar_car:
                isChangeAvatar = false;
                selectImage();
                break;
            case R.id.txt_edit:
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mTxtName.setText(userInputDialogEditText.getText().toString());
                                drivers.child("name").setValue(mTxtName.getText().toString());
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                break;
            case R.id.txt_edit_car:
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View view1 = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilderUserInput1 = new AlertDialog.Builder(getContext());
                alertDialogBuilderUserInput1.setView(view1);

                final EditText userInputDialogEditText1 = (EditText) view1.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput1
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mTxtNameCar.setText(userInputDialogEditText1.getText().toString());
                                drivers.child("nameVehicle").setValue(mTxtNameCar.getText().toString());
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid1 = alertDialogBuilderUserInput1.create();
                alertDialogAndroid1.show();
                break;
        }
    }

    //Change avatar
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {

                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getContext());

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_FILE) {
            onSelectFromGalleryResult(data);
            final Uri imgFile = data.getData();
            filePath = imgFile;
            if (isChangeAvatar) {
                mImgAvatar.setImageURI(filePath);
            } else {
                mImgVehicle.setImageURI(filePath);

            }
            uploadImage();

        } else if (requestCode == REQUEST_CAMERA) {
            onCaptureImageResult(data);
            final Uri imgFileCam = data.getData();
            filePath = imgFileCam;
            if (isChangeAvatar) {
                mImgAvatar.setImageURI(filePath);
            } else {
                mImgVehicle.setImageURI(filePath);

            }
            uploadImage();
        }

    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isChangeAvatar) {
            mImgAvatar.setImageBitmap(thumbnail);
        } else {
            mImgVehicle.setImageBitmap(thumbnail);


        }


    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        ConvertBitmap myBitMap = new ConvertBitmap(getContext());
        Bitmap bitmap = null;
        try {
            bitmap = myBitMap.decodeUri(data.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (isChangeAvatar) {
            mImgAvatar.setImageBitmap(bitmap);
        } else {
            mImgVehicle.setImageBitmap(bitmap);

        }

    }

    private void getUserInfo() {
        drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null)
                        mTxtName.setText(map.get("name").toString());

                    if (map.get("nameVehicle") != null)
                        mTxtNameCar.setText(map.get("nameVehicle").toString());


                    if (map.get("imgUrl") != null && getActivity()!=null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.avavtar)
                                .error(R.drawable.avavtar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getActivity()).load(map.get("imgUrl").toString()).apply(options).into(mImgAvatar);
                    }
                    if (map.get("imgVehicle") != null && getActivity()!=null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.car_ava)
                                .error(R.drawable.car_ava)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getActivity()).load(map.get("imgVehicle").toString()).apply(options).into(mImgVehicle);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


    }

    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");

            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("driver_images/" + UUID.randomUUID().toString()).child(mFirebaseAuth.getCurrentUser().getUid());

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final UploadTask uploadTask = ref.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Map newImg = new HashMap();
                                if (isChangeAvatar) {
                                    newImg.put("imgUrl", task.getResult().toString());

                                } else {
                                    newImg.put("imgVehicle", task.getResult().toString());
                                }
                                drivers.updateChildren(newImg);
                            } else {

                            }
                        }
                    });

                }
            });
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

}

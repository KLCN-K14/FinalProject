package com.klcn.xuant.transporter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.utils.ConvertBitmap;
import com.klcn.xuant.transporter.utils.Utility;
import com.rengwuxian.materialedittext.MaterialEditText;

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

public class DriverAccountActivity extends AppCompatActivity implements View.OnClickListener {

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
    @BindView(R.id.container)
    LinearLayout mRoot;
    @BindView(R.id.img_avatar_car)
    CircleImageView mImgVehicle;

    FirebaseAuth mFirebaseAuth;
    String encodedString = "";
    DatabaseReference mDBDriver;
    Driver mDriver;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    private boolean isChangeAvatar = false;

    String serviceVehicle = "";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_account);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mFirebaseAuth = FirebaseAuth.getInstance();

        mPanelSignOut.setOnClickListener(this);
        mPanelAbout.setOnClickListener(this);
        mPanelDocument.setOnClickListener(this);
        mPanelWaybill.setOnClickListener(this);
        mImgAvatar.setOnClickListener(this);
        mImgVehicle.setOnClickListener(this);
        mTxtEdit.setOnClickListener(this);
        mTxtEditCar.setOnClickListener(this);

        mDBDriver = FirebaseDatabase.getInstance().getReference().child(Common.drivers_tbl)
                .child(mFirebaseAuth.getCurrentUser().getUid());

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        getUserInfo();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.panel_about:

                

                break;
            case R.id.panel_documents:
                showDialogChangePassword();
                break;
            case R.id.panel_sign_out:
                setResult(RESULT_OK,null);
                finish();
                break;
            case R.id.panel_waybill:
                Intent intent = new Intent(this,DriverWalletActivity.class);
                startActivity(intent);
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
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.layout_change_name_driver, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
                alertDialogBuilderUserInput.setView(mView);
                alertDialogBuilderUserInput.setTitle("CHANGE NAME");

                final MaterialEditText userInputDialogEditText =  mView.findViewById(R.id.userInputDialog);

                userInputDialogEditText.setText(mDriver.getName().toString());
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mTxtName.setText(userInputDialogEditText.getText().toString());
                                mDBDriver.child("name").setValue(mTxtName.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar.make(mRoot,"Update Success",Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(mRoot,""+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                    }
                                });
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
                LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
                View view1 = layoutInflater.inflate(R.layout.layout_change_car_driver, null);
                AlertDialog.Builder alertDialogBuilderUserInput1 = new AlertDialog.Builder(this);
                alertDialogBuilderUserInput1.setView(view1);
                alertDialogBuilderUserInput1.setTitle("CHANGE VEHICLE");

                final MaterialEditText userInputDialogEditText1 = (MaterialEditText) view1.findViewById(R.id.userInputDialog);
                final MaterialEditText licensePlateInput= (MaterialEditText) view1.findViewById(R.id.edit_license_plate);

                userInputDialogEditText1.setText(mDriver.getNameVehicle().toString());
                licensePlateInput.setText(mDriver.getLicensePlate().toString());
                final MaterialSpinner spinnerService= (MaterialSpinner) view1.findViewById(R.id.spinner_service);
                alertDialogBuilderUserInput1
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mTxtNameCar.setText(userInputDialogEditText1.getText().toString());
                                mDBDriver.child("nameVehicle").setValue(mTxtNameCar.getText().toString());
                                mDBDriver.child("licensePlate").setValue(licensePlateInput.getText().toString());
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("serviceVehicle",serviceVehicle);
                                mDBDriver.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar.make(mRoot,"Update Success",Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(mRoot,""+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                spinnerService.setItems("Transport Standard", "Transport Premium");
                spinnerService.setSelectedIndex(0);
                if (mDriver != null) {
                    if (mDriver.getServiceVehicle().equals(Common.service_vehicle_standard)) {
                        spinnerService.setSelectedIndex(0);
                        serviceVehicle = "Transport Standard";

                    } else {
                        spinnerService.setSelectedIndex(1);
                        serviceVehicle = "Transport Standard";
                    }
                }

                spinnerService.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                    @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                        serviceVehicle = item;
                    }
                });

                AlertDialog alertDialogAndroid1 = alertDialogBuilderUserInput1.create();
                alertDialogAndroid1.show();
                break;
        }
    }

    private void showDialogChangePassword() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGE PASSWORD");
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View changePassLayout = inflater.inflate(R.layout.layout_change_password,null);

        builder.setView(changePassLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        final MaterialEditText oldPass =  changePassLayout.findViewById(R.id.edt_old_password);
        final MaterialEditText newPass =  changePassLayout.findViewById(R.id.edt_new_password);
        final MaterialEditText renewPass =  changePassLayout.findViewById(R.id.edt_renew_password);
        final TextView txtChange =  changePassLayout.findViewById(R.id.txt_change);
        final TextView txtCancel =  changePassLayout.findViewById(R.id.txt_cancel);

        // Set button dialog
        txtChange.setOnClickListener(view -> {
            if(TextUtils.isEmpty(oldPass.getText())){
                Toast.makeText(getApplicationContext(), "Please enter old password",Toast.LENGTH_SHORT)
                        .show();
            }else if(TextUtils.isEmpty(newPass.getText())){
                Toast.makeText(getApplicationContext(), "Please enter new password",Toast.LENGTH_SHORT)
                        .show();
            }else if(TextUtils.isEmpty(renewPass.getText())){
                Toast.makeText(getApplicationContext(), "Please confirm password",Toast.LENGTH_SHORT)
                        .show();
            }else if(renewPass.getText().length()<6){
                Toast.makeText(getApplicationContext(), "Password must have more 6 charater",Toast.LENGTH_SHORT)
                        .show();
            }else if(!renewPass.getText().toString().equals(newPass.getText().toString())){
                Toast.makeText(getApplicationContext(), "New password does not match",Toast.LENGTH_SHORT)
                        .show();
            }else{
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                AuthCredential credential = EmailAuthProvider
                        .getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                oldPass.getText().toString());

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                                Snackbar.make(mRoot,"Change Success",Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        txtCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getApplicationContext());

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

        ConvertBitmap myBitMap = new ConvertBitmap(getApplicationContext());
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
        mDBDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    mDriver = dataSnapshot.getValue(Driver.class);

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null)
                        mTxtName.setText(map.get("name").toString());

                    if (map.get("nameVehicle") != null)
                        mTxtNameCar.setText(map.get("nameVehicle").toString());


                    if (map.get("imgUrl") != null && this!=null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.avavtar)
                                .error(R.drawable.avavtar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getApplicationContext()).load(map.get("imgUrl").toString()).apply(options).into(mImgAvatar);
                    }
                    if (map.get("imgVehicle") != null && this!=null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.car_ava)
                                .error(R.drawable.car_ava)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getApplicationContext()).load(map.get("imgVehicle").toString()).apply(options).into(mImgVehicle);
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
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");

            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("driver_images/" + UUID.randomUUID().toString()).child(mFirebaseAuth.getCurrentUser().getUid());

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
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
                                mDBDriver.updateChildren(newImg);
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
                    Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

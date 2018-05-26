package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverAccountFragment extends Fragment implements View.OnClickListener{

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

    FirebaseAuth mFirebaseAuth;

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
        ButterKnife.bind(this,view);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mPanelSignOut.setOnClickListener(this);
        mPanelAbout.setOnClickListener(this);
        mPanelDocument.setOnClickListener(this);
        mPanelWaybill.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_about:
                Toast.makeText(getContext(),"Panel about",Toast.LENGTH_LONG).show();
                break;
            case R.id.panel_documents:
                Toast.makeText(getContext(),"Panel document",Toast.LENGTH_LONG).show();
                break;
            case R.id.panel_sign_out:
                Toast.makeText(getContext(),"Panel sign out",Toast.LENGTH_LONG).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    mFirebaseAuth.signOut();
                    Intent intent = new Intent(getContext(),ChooseTypeUserActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                break;
            case R.id.panel_waybill:
                Toast.makeText(getContext(),"Panel waybill",Toast.LENGTH_LONG).show();
                break;
        }
    }
}

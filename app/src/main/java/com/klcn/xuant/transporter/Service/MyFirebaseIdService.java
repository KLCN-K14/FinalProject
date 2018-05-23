package com.klcn.xuant.transporter.Service;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Token;

public class MyFirebaseIdService extends FirebaseInstanceIdService{


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        updateToken(refreshToken);
    }

    private void updateToken(String refreshToken) {
        DatabaseReference dbToken = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        Token token = new Token(refreshToken);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
        }
    }
}

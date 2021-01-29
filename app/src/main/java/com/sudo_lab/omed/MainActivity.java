package com.sudo_lab.omed;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AccountAuthService mAuthService;
    private AccountAuthParams mAuthParam;
    public static final String TAG = "Account Kit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.account_signinToken).setOnClickListener(this);
        findViewById(R.id.account_signInCode).setOnClickListener(this);
        findViewById(R.id.account_silent_signin).setOnClickListener(this);
        findViewById(R.id.account_signout).setOnClickListener(this);
        findViewById(R.id.cancel_authorization).setOnClickListener(this);


    }

    public void signInToken(){
        AccountAuthParams authParams;
         AccountAuthParamsHelper authParamsHelper = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setIdToken(); //ID Token signIn ID

        authParams = authParamsHelper.createParams();

        AccountAuthService service = AccountAuthManager.getService(MainActivity.this, authParams);
        startActivityForResult(service.getSignInIntent(), 2222);

    }

    public void signInCode(){
        AccountAuthParams authParams;
        AccountAuthParamsHelper authParamsHelper = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode(); //ID Code Auth Code

        authParams = authParamsHelper.createParams();

        AccountAuthService service = AccountAuthManager.getService(MainActivity.this, mAuthParam);
        startActivityForResult(service.getSignInIntent(), 8888);

    }

    private void signOut(){
// service indicates the AccountAuthService instance generated using the getService method during the sign-in authorization.
        Task<Void> signOutTask = mAuthService.signOut();
        signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                Log.i(TAG, "signOut complete");
            }
        });

        signOutTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
            }
        });

    }

    private void silentSignIn() {
        AccountAuthParams authParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).createParams(); //ID Code Auth Code

        AccountAuthService service = AccountAuthManager.getService(MainActivity.this,authParams);
        Task<AuthAccount> task = service.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                Log.i(TAG, "displayName :"+authAccount.getDisplayName()+"silentSignIn success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // The sign-in failed. Try to sign in explicitly using getSignInIntent().
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException)e;
                    Log.i("Account Kit", "sign failed status:" + apiException.getStatusCode());

                }
            }
        });

    }
    private void cancelAuthorization() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .createParams();
        mAuthService = AccountAuthManager.getService(MainActivity.this, mAuthParam);
        Task<Void> task = mAuthService.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "cancelAuthorization success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
            }
        });
    }




    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_signinToken:
                signInToken();
                break;
            case R.id.account_signout:
                signOut();
                break;
            case R.id.account_signInCode:
                signInCode();
                break;
            case R.id.account_silent_signin:
                silentSignIn();
                break;
            case R.id.cancel_authorization:
                cancelAuthorization();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Process the sign-in result and obtain Authorization Code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                //The sign-in is successful, and the user's HUAWEI ID information and authorization code are obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, "Authorization code:" + authAccount.getAuthorizationCode());
            } else {
                // The sign-in failed.
                Log.e(TAG, "sign in failed : " + ((ApiException)authAccountTask .getException()).getStatusCode());
            }
        }
    }

    private void addLogFragment() {
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        final LogFragment fragment = new LogFragment();
        transaction.replace(R.id.framelog, fragment);
        transaction.commit();
    }



}
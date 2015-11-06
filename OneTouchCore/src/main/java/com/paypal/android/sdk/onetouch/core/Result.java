package com.paypal.android.sdk.onetouch.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides all the information associated with a login request for an authorization code.<br/>
 */
public final class Result implements Parcelable {
    private static final String TAG = Result.class.getSimpleName();

    private final String mEnvironment;
    private final ResultType mResultType;
    private final ResponseType mResponseType;
    private final JSONObject mResponse;
    private final String mUserEmail;
    private final Throwable mError;

    /**
     * Construct a PayPalOneTouchResult for a success
     */
    Result(String environment, ResponseType responseType, JSONObject response, String userEmail) {
        this(ResultType.Success, environment, responseType, response, userEmail, null);
    }

    /**
     * Construct a PayPalOneTouchResult for a failure
     *
     * @param error The error to return to the caller
     * @note the error may come from the remote authenticator or directly from lib-otc (e.g. due to response parsing errors)
     */
    Result(Throwable error) {
        this(ResultType.Error, null, null, null, null, error);
    }

    /**
     * Construct a PayPalOneTouchResult for a cancellation
     */
    Result() {
        this(ResultType.Cancel, null, null, null, null, null);
    }

    /**
     * Construct a PayPalOneTouchResult for any generic data.  For internal use only.
     */
    private Result(ResultType resultType, String environment, ResponseType responseType, JSONObject response, String userEmail, Throwable error) {
        assert resultType != null;
        this.mEnvironment = environment;
        this.mResultType = resultType;
        this.mResponseType = responseType;
        this.mResponse = response;
        this.mUserEmail = userEmail;
        this.mError = error;
    }

    public ResultType getResultType() {
        return mResultType;
    }

    /**
     * @return The JSON object to send to your server.
     */
    public JSONObject getResponse() {
        try {
            JSONObject client = new JSONObject();
            client.put("environment", mEnvironment);
            client.put("paypal_sdk_version", BuildConfig.PRODUCT_VERSION);
            client.put("platform", "Android");
            client.put("product_name", BuildConfig.PRODUCT_NAME);


            JSONObject response = new JSONObject();
            response.put("client", client);

            if(null != mResponse) {
                JSONObject result = mResponse;
                response.put("response", result);
            }

            if(null != mResponseType) {
                response.put("response_type", mResponseType.name());
            }

            if(null != mUserEmail) {
                JSONObject user = new JSONObject();
                user.put("display_string", mUserEmail);
                response.put("user", user);
            }

            Log.e(TAG, "Success encoding JSON" + response);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, "Error encoding JSON", e);
            return null;
        }
    }

    /**
     * @return the error if the response type is error
     */
    public Throwable getError() {
        return mError;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mEnvironment);
        dest.writeValue(this.mResultType);
        dest.writeValue(this.mResponseType);
        if(null != this.mResponse) {
            dest.writeValue(this.mResponse.toString());
        } else {
            dest.writeValue(null);
        }
        dest.writeValue(this.mUserEmail);
        dest.writeValue(this.mError);
    }

    private Result(Parcel in) {
        this.mEnvironment = (String) in.readValue(null);
        this.mResultType = (ResultType) in.readValue(ResultType.class.getClassLoader());
        this.mResponseType = (ResponseType) in.readValue(ResponseType.class.getClassLoader());

        JSONObject jsonResponse = null;
        try {
            String jsonString = (String) in.readValue(null);
            if(null != jsonString){
                jsonResponse = new JSONObject(jsonString);
            }
        } catch (JSONException e) {
            Log.d(TAG, "Failed to read parceled JSON for mResponse: " + e);
        }
        this.mResponse = jsonResponse;

        this.mUserEmail = (String) in.readValue(null);
        this.mError = (Throwable) in.readValue(null);
    }

    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
}
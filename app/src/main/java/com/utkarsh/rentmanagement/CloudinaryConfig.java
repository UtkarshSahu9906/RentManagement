package com.utkarsh.rentmanagement;

import android.content.Context;
import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {

    public static void initCloudinary(Context context) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dzjwrkopb");
        config.put("api_key", "573213361223711");
        config.put("api_secret", "0FWs3egA_YbsfRBo3qC7KIjhf8A");
        config.put("secure", "true");

        MediaManager.init(context, config);
    }
}